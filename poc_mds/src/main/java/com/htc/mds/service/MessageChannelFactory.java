package com.htc.mds.service;

import com.htc.mds.model.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageChannelFactory {

    private Map<String, SendMessageChannelService> _sendMessageChannelMap;

    @Autowired
    public MessageChannelFactory(Map<String, SendMessageChannelService> sendMessageChannelMap) {
        _sendMessageChannelMap = sendMessageChannelMap;
    }

    public SendMessageChannelService getChannel(MessageType messageType) {
        SendMessageChannelService channelService = _sendMessageChannelMap.get(messageType.getName());
        if(channelService == null) {
            throw new RuntimeException("no SendMessageChannelService defined");
        }
        return channelService;
    }

}
