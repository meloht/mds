package com.htc.mds.service;

import com.htc.mds.model.QueueMessageBody;

public interface SendMessageChannelService {
    boolean sendMessageToClient(QueueMessageBody message);
}
