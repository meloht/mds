package com.htc.mds.service;

import com.htc.mds.entity.Attachment;
import com.htc.mds.entity.Message;
import com.htc.mds.entity.MessageTemplate;
import com.htc.mds.model.QueueMessageBody;
import com.htc.mds.model.TemplateMapDto;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface MailService {

    void sendText(Message msg,List<String> userList) throws MessagingException;

    void sendHtmlMail(Message msg,List<String> userList) throws MessagingException;

    void sendAttachmentsMail(List<String> to, String subject, String content, String filePath);

    void sendInlineResourceMail(List<String> to, String subject, String content, String rscPath, String rscId);

    void sendAttachmentsMailWithInputStream(QueueMessageBody msg, String content) throws MessagingException, IOException;

    void sendTemplateMail(QueueMessageBody msg, MessageTemplate template, TemplateMapDto map) throws IOException, MessagingException;
}
