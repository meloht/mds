package com.htc.mds.service.impl;

import com.htc.mds.blobstorage.BlobStorageClient;
import com.htc.mds.dao.AttachmentRepository;
import com.htc.mds.dao.MessageRepository;
import com.htc.mds.dao.RecipientsStatusRepository;
import com.htc.mds.dao.TemplateMapRepository;
import com.htc.mds.entity.Attachment;
import com.htc.mds.entity.Message;
import com.htc.mds.entity.RecipientsStatus;
import com.htc.mds.entity.TemplateMap;
import com.htc.mds.model.*;
import com.htc.mds.service.AuditLogsService;
import com.htc.mds.service.MessageService;
import com.htc.mds.servicebus.SendMessageClient;
import com.htc.mds.util.CommUtils;
import com.microsoft.bot.integration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.*;

@Service
public class MessageServiceImpl implements MessageService {

    private AttachmentRepository _attachmentRepository;
    private SendMessageClient _sendMessageClient;
    private BlobStorageClient _blobStorageClient;
    private Configuration _withConfiguration;
    private MessageRepository _messageRepository;
    private AuditLogsService _auditLogs;
    private TemplateMapRepository _templateMapRepository;
    private RecipientsStatusRepository _recipientsStatusRepository;

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public MessageServiceImpl(
            AttachmentRepository attachmentRepository,
            SendMessageClient sendMessageClient,
            Configuration configuration,
            MessageRepository messageRepository,
            AuditLogsService auditLogs,
            TemplateMapRepository templateMapRepository,
            RecipientsStatusRepository recipientsStatusRepository,
            BlobStorageClient blobStorageClient) {
        _attachmentRepository = attachmentRepository;
        _sendMessageClient = sendMessageClient;
        _blobStorageClient = blobStorageClient;
        _withConfiguration = configuration;
        _messageRepository = messageRepository;
        _auditLogs = auditLogs;
        _templateMapRepository = templateMapRepository;
        _recipientsStatusRepository = recipientsStatusRepository;
    }

    @Override
    public MessageResponse deliverMessageWithFiles(BaseMessageInfo messageInfo, String clientId, List<MultipartFile> files) throws IOException {

        MessageResponse messageResponse = new MessageResponse();
        Message message = buildMessage(messageInfo, clientId);
        messageResponse.setMessageId(message.getId());
        _auditLogs.saveStringAuditLogs("deliverMessage begin", "MessageService deliverMessageWithFiles", message.getId());

        saveRecipientsStatus(messageInfo.getRecipients(), message);

        if (messageInfo.getContentType() == MessageContentType.Template.getValue()) {
            _auditLogs.saveStringAuditLogs("saveTemplateMap", "MessageService deliverMessageWithFiles", message.getId());
            saveTemplateMapBase(message.getId(), message.getTemplateId(), messageInfo.getTemplateMap(), TemplateMapType.Body);

            if (messageInfo.getSubjectMap() != null && messageInfo.getSubjectMap().size() > 0) {
                _auditLogs.saveStringAuditLogs("saveTemplateSubjectMap", "MessageService deliverMessageWithFiles", message.getId());
                saveTemplateMapBase(message.getId(), message.getTemplateId(), messageInfo.getSubjectMap(), TemplateMapType.Subject);
            }
        }

        List<Attachment> listFile = new ArrayList<Attachment>();
        if (files != null && files.size() > 0) {

            for (MultipartFile file : files) {

                Attachment attachment = buildAttachment(file, message.getId());
                listFile.add(attachment);

                boolean bl = _blobStorageClient.uploadFile(attachment.getFileName(), file.getInputStream(), file.getSize());

                String result = bl == true ? MessageCode.SUCCESS : MessageCode.FAILED;
                _auditLogs.saveStringAuditLogs("save attachment to blob " + attachment.getName() + " " + result,
                        "MessageService deliverMessageWithFiles", message.getId());
                if (!bl) {
                    throw new RuntimeException("blob Storage uploadText failed ");
                }
            }
            _auditLogs.saveStringAuditLogs("save attachment list to db", "MessageService deliverMessageWithFiles", message.getId());
            logger.info("save attachment list to db");
            _attachmentRepository.saveAll(listFile);

        }
        _auditLogs.SaveMessageLog(messageInfo, listFile, "MessageService deliverMessageWithFiles", message.getId());

        QueueMessageBody queueMessageBody = buildQueueMessage(message, listFile);

        logger.info("sendMessage to service bus queue begin");
        _auditLogs.saveStringAuditLogs("sendMessage to service bus queue begin", "MessageService deliverMessageWithFiles", message.getId());
        _sendMessageClient.sendMessage(queueMessageBody);
        logger.info("sendMessage to service bus queue end");
        _auditLogs.saveStringAuditLogs("sendMessage to service bus queue end", "MessageService deliverMessageWithFiles", message.getId());
        return messageResponse;
    }


    @Override
    public void DownloadFile(String id, HttpServletResponse response) throws IOException {
        Attachment attachment = _attachmentRepository.getAttachmentByFileName(id);
        if (attachment != null) {
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(attachment.getName().getBytes("utf-8")));
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Length", "" + attachment.getFileSize());

            _blobStorageClient.downloadFile(id, response.getOutputStream());
        }
    }

    private Attachment buildAttachment(MultipartFile file, String messageId) {
        Attachment attachment = new Attachment();
        attachment.setCreateTimestamp(new Date());
        attachment.setMessageId(messageId);

        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(file.getOriginalFilename());
        if (StringUtils.isEmpty(mimeType)) {
            attachment.setContentType("application/octet-stream");
        } else {
            attachment.setContentType(mimeType);
        }

        attachment.setFileName(UUID.randomUUID().toString());
        attachment.setFileSize(file.getSize());
        attachment.setName(file.getOriginalFilename());

        return attachment;
    }

    private Message buildMessage(BaseMessageInfo body, String clientId) {

        Message message = new Message();
        message.setMessageFrom(clientId);
        message.setMessageText(body.getMessage());
        message.setMessageType(body.getMessageType());
        message.setSubject(body.getSubject());
        message.setContentType(body.getContentType());
        message.setStatus(MessageStatus.Waiting.getValue());
        message.setTemplateId(body.getTemplateId());

        if (body.getSendAt() == null) {
            message.setSendAt(new Date());
        } else {
            message.setSendAt(body.getSendAt());
        }

        message.setCreateTimestamp(new Date());

        message.setId(UUID.randomUUID().toString());
        return message;
    }


    private void saveTemplateMapBase(String messageId, long templateId, Map<String, String> map, TemplateMapType mapType) {
        List<TemplateMap> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            TemplateMap templateMap = new TemplateMap();
            templateMap.setCreateTimestamp(new Date());
            templateMap.setName(entry.getKey());
            templateMap.setValue(entry.getValue());
            templateMap.setMessageId(messageId);
            templateMap.setMapType(mapType.getValue());
            templateMap.setTemplateId(templateId);
            list.add(templateMap);
        }
        _templateMapRepository.saveAll(list);
    }

    private QueueMessageBody buildQueueMessage(Message message, List<Attachment> listFile) {
        QueueMessageBody messageBody = new QueueMessageBody();
        messageBody.setMessageInfo(message);

        if (listFile == null || listFile.size() == 0) {
            messageBody.setAttachmentFileList(new ArrayList<Attachment>());
        } else {
            messageBody.setAttachmentFileList(listFile);
        }
        return messageBody;
    }

    @Override
    public MessageDto getMessageById(String id) {
        MessageDto messageDto = new MessageDto();
        Message message = _messageRepository.getById(id);
        List<RecipientsStatus> recipientsStatusList = _recipientsStatusRepository.getRecipientsStatusesByMessageId(id);

        messageDto.setMessage(message);
        messageDto.setRecipientsStatus(recipientsStatusList);

        return messageDto;
    }

    private void saveRecipientsStatus(List<String> recipients, Message message) {

        List<RecipientsStatus> list = new ArrayList<RecipientsStatus>();
        for (String item : recipients) {
            RecipientsStatus recipientsStatus = new RecipientsStatus();
            recipientsStatus.setCreateTimestamp(new Date());
            recipientsStatus.setModifyTimestamp(new Date());
            recipientsStatus.setMessageId(message.getId());
            recipientsStatus.setUserMail(item);
            recipientsStatus.setReceiveStatus(UserReceiveStatus.ReceiveFailed.getValue());

            list.add(recipientsStatus);
        }

        _recipientsStatusRepository.saveAll(list);
    }


}
