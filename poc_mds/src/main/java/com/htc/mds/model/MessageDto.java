package com.htc.mds.model;

import com.htc.mds.entity.Message;
import com.htc.mds.entity.RecipientsStatus;

import java.util.List;

public class MessageDto {
    private Message message;
    private List<RecipientsStatus> recipientsStatus;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public List<RecipientsStatus> getRecipientsStatus() {
        return recipientsStatus;
    }

    public void setRecipientsStatus(List<RecipientsStatus> recipientsStatus) {
        this.recipientsStatus = recipientsStatus;
    }
}
