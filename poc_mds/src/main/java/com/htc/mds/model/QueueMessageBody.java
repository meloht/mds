package com.htc.mds.model;

import com.htc.mds.entity.Attachment;
import com.htc.mds.entity.Message;

import java.util.HashMap;
import java.util.List;

public class QueueMessageBody {

    private Message messageInfo;
    private List<Attachment> attachmentFileList;
    private boolean isOverLimitSize;

    private List<String> recipients;
    private String blobGuid;

    public Message getMessageInfo() {
        return messageInfo;
    }

    public void setMessageInfo(Message messageInfo) {
        this.messageInfo = messageInfo;
    }


    public List<Attachment> getAttachmentFileList() {
        return attachmentFileList;
    }

    public void setAttachmentFileList(List<Attachment> attachmentFileList) {
        this.attachmentFileList = attachmentFileList;
    }

    public boolean getIsOverLimitSize() {
        return isOverLimitSize;
    }

    public void setIsOverLimitSize(boolean overLimitSize) {
        isOverLimitSize = overLimitSize;
    }

    public String getBlobGuid() {
        return blobGuid;
    }

    public void setBlobGuid(String blobGuid) {
        this.blobGuid = blobGuid;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }
}
