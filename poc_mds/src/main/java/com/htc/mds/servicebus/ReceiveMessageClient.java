package com.htc.mds.servicebus;

import com.azure.messaging.servicebus.*;
import com.htc.mds.blobstorage.BlobStorageClient;
import com.htc.mds.dao.MessageRepository;
import com.htc.mds.dao.RecipientsStatusRepository;
import com.htc.mds.entity.Message;
import com.htc.mds.entity.RecipientsStatus;
import com.htc.mds.model.MessageStatus;
import com.htc.mds.model.MessageType;
import com.htc.mds.model.QueueMessageBody;
import com.htc.mds.service.AuditLogsService;
import com.htc.mds.service.MessageChannelFactory;
import com.htc.mds.service.SendMessageChannelService;
import com.htc.mds.util.JsonUtils;
import com.microsoft.bot.integration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class ReceiveMessageClient {


    private String _connectionString;
    private String _queueName;
    private Configuration _withConfiguration;
    private MessageChannelFactory _factory;
    private MessageRepository _messageRepository;
    private AuditLogsService _auditLogs;
    private BlobStorageClient _blobStorageClient;
    private RecipientsStatusRepository _recipientsStatusRepository;

    Logger logger = LoggerFactory.getLogger(getClass());

    public ReceiveMessageClient(
            Configuration configuration,
            MessageChannelFactory factory,
            MessageRepository messageRepository,
            AuditLogsService auditLogs,
            BlobStorageClient blobStorageClient,
            RecipientsStatusRepository recipientsStatusRepository
    ) {
        _blobStorageClient = blobStorageClient;
        _withConfiguration = configuration;
        _connectionString = _withConfiguration.getProperty("ServiceBusConnectionString");
        _queueName = _withConfiguration.getProperty("QueueName");
        _factory = factory;
        _messageRepository = messageRepository;
        _auditLogs = auditLogs;
        _recipientsStatusRepository = recipientsStatusRepository;
    }

    public void init() {

        Consumer<ServiceBusReceivedMessageContext> processMessage = messageContext -> {

            QueueMessageBody body = messageContext.getMessage().getBody().toObject(QueueMessageBody.class);

            if (body != null) {


                logger.info("receive message from service bus " + body.getMessageInfo().getId());
                _auditLogs.saveStringAuditLogs("receive message from service bus", "ReceiveMessageClient", body.getMessageInfo().getId());
                logger.info("receive message from service bus " + JsonUtils.toString(body.getMessageInfo()));

                boolean blRead = ReadContentFromBlob(body);
                if (!blRead)
                    return;

                saveMessage(body.getMessageInfo());

                buildRecipientList(body);

                MessageType messageType = MessageType.intToEnum(body.getMessageInfo().getMessageType());
                if (messageType != null) {
                    _auditLogs.saveStringAuditLogs("send message to client", "ReceiveMessageClient", body.getMessageInfo().getId());
                    logger.info("send message to client: " + messageType.getName() + " " + body.getMessageInfo().getId());
                    SendMessageChannelService channelService = _factory.getChannel(messageType);
                    boolean bl = channelService.sendMessageToClient(body);
                    if (bl) {
                        messageContext.complete();
                        updateMessageStatus(body.getMessageInfo(), MessageStatus.Success.getValue());
                    } else {
                        updateMessageStatus(body.getMessageInfo());
                    }
                }
            }

        };

        Consumer<ServiceBusErrorContext> processError = errorContext -> {

            logger.error(errorContext.getException().toString(), errorContext.getException());

        };

// create the processor client via the builder and its sub-builder
        ServiceBusProcessorClient processorClient = new ServiceBusClientBuilder()
                .connectionString(_connectionString)
                .processor()
                .disableAutoComplete()
                .queueName(_queueName)
                .processMessage(processMessage)
                .processError(processError)
                .buildProcessorClient();


        // Starts the processor in the background and returns immediately
        processorClient.start();
    }

    private boolean ReadContentFromBlob(QueueMessageBody body) {
        if (body.getIsOverLimitSize()) {
            try {
                logger.info("begin ReadContentFromBlob");
                _auditLogs.saveStringAuditLogs("begin ReadContentFromBlob",
                        "ReceiveMessageClient", body.getMessageInfo().getId());

                String content = _blobStorageClient.downloadText(body.getBlobGuid());
                body.getMessageInfo().setMessageText(content);
                return true;
            } catch (Exception ex) {
                logger.info("ReadContentFromBlob failed " + ex.getMessage());
                _auditLogs.saveStringAuditLogs("ReadContentFromBlob failed " + ex.getMessage(),
                        "ReceiveMessageClient", body.getMessageInfo().getId());
                return false;
            }
        }
        return true;
    }


    private void saveMessage(Message body) {

        boolean bl = _messageRepository.existsById(body.getId());
        if (!bl) {
            _auditLogs.saveStringAuditLogs("saveMessage", "ReceiveMessageClient", body.getId());
            logger.info("saveMessage " + body.getId());
            _messageRepository.save(body);
        }
    }

    private void updateMessageStatus(Message body, int messageStatus) {
        try {
            logger.info("updateMessageStatus " + body.getId());
            _auditLogs.saveStringAuditLogs("updateMessageStatus", "ReceiveMessageClient", body.getId());
            _messageRepository.updateMessageStatusAndCompleteTimeById(messageStatus, new Date(), body.getId());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            logger.info("updateMessageStatus failed " + ex.getMessage() + " " + body.getId());
            _auditLogs.saveStringAuditLogs("updateMessageStatus failed "+ ex.getMessage(), "ReceiveMessageClient", body.getId());
        }


    }

    private void updateMessageStatus(Message body) {
        List<RecipientsStatus> recipientsStatusList = _recipientsStatusRepository.getRecipientsStatusesByMessageId(body.getId());
        int numSuccess = 0;
        for (RecipientsStatus item : recipientsStatusList) {
            if (item.getReceiveStatus() == 1) {
                numSuccess++;
            }
        }
        int messageStatus = MessageStatus.Success.getValue();
        if (numSuccess == recipientsStatusList.size()) {
            messageStatus = MessageStatus.Success.getValue();
        }
        if (numSuccess == 0) {
            messageStatus = MessageStatus.Failed.getValue();
        }
        if (numSuccess > 0 && numSuccess < recipientsStatusList.size()) {
            messageStatus = MessageStatus.PartialSuccess.getValue();
        }

        updateMessageStatus(body, messageStatus);
    }

    private void buildRecipientList(QueueMessageBody body) {
        List<RecipientsStatus> recipientsStatusList = _recipientsStatusRepository.getRecipientsStatusesByMessageId(body.getMessageInfo().getId());
        List<String> list = new ArrayList<String>();
        for (RecipientsStatus item : recipientsStatusList) {
            list.add(item.getUserMail());
        }
        body.setRecipients(list);
    }


}
