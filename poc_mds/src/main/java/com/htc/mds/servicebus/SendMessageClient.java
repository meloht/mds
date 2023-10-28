package com.htc.mds.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.htc.mds.blobstorage.BlobStorageClient;
import com.htc.mds.model.QueueMessageBody;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.bot.integration.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

public class SendMessageClient {


    private String connectionString;
    private String queueName;
    private BlobStorageClient _blobStorageClient;
    private ServiceBusSenderClient _sender;
    private Configuration _withConfiguration;

    public SendMessageClient(Configuration configuration, BlobStorageClient blobStorageClient) {
        _withConfiguration = configuration;
        _blobStorageClient = blobStorageClient;
        connectionString = _withConfiguration.getProperty("ServiceBusConnectionString");
        queueName = _withConfiguration.getProperty("QueueName");
    }

    public void init() {
        _sender = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildClient();

    }

    public void sendMessage(QueueMessageBody msg) {

        BinaryData data = BinaryData.fromObject(msg);
        long size = data.toBytes().length;
        long standardSize = 1024 * 240;//server bus message size < 256KB

        if (size > standardSize) {
            msg.setIsOverLimitSize(true);
            msg.setBlobGuid(UUID.randomUUID().toString());
            saveToBlob(msg.getBlobGuid(), msg.getMessageInfo().getMessageText());
            msg.getMessageInfo().setMessageText("");
            data = BinaryData.fromObject(msg);

        } else {
            msg.setIsOverLimitSize(false);
        }
        // create a message to send
        ServiceBusMessage message = new ServiceBusMessage(data);
        // send the message
        _sender.sendMessage(message);
    }

    private void saveToBlob(String guid, String content) {
        boolean bl = _blobStorageClient.uploadText(guid, content);
        if (!bl) {
            throw new RuntimeException("blob Storage uploadText failed ");
        }
    }


}
