package com.htc.mds.service.impl;

import com.htc.mds.blobstorage.BlobInputStreamSource;
import com.htc.mds.blobstorage.BlobStorageClient;
import com.htc.mds.entity.Attachment;
import com.htc.mds.entity.Message;
import com.htc.mds.entity.MessageTemplate;
import com.htc.mds.model.MessageContentType;
import com.htc.mds.model.QueueMessageBody;
import com.htc.mds.model.TemplateMapDto;
import com.htc.mds.service.MailService;
import com.htc.mds.util.CommUtils;
import com.htc.mds.util.ConstValue;
import com.microsoft.bot.integration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private Configuration _withConfiguration;
    private BlobStorageClient _blobStorageClient;

    private TemplateEngine _templateEngine;
    private String from;


    @Autowired
    public MailServiceImpl(
            JavaMailSender mailSender,
            Configuration configuration,
            TemplateEngine templateEngine,
            BlobStorageClient blobStorageClient) {
        this.mailSender = mailSender;
        _templateEngine = templateEngine;
        this._withConfiguration = configuration;
        _blobStorageClient = blobStorageClient;
        from = _withConfiguration.getProperty("spring.mail.properties.from");
    }

    @Override
    public void sendText(Message msg, List<String> userList) {

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(from);

        simpleMailSetTo(userList, simpleMailMessage);
        simpleMailMessage.setSubject(msg.getSubject());
        simpleMailMessage.setText(msg.getMessageText());

        mailSender.send(simpleMailMessage);
    }

    @Override
    public void sendHtmlMail(Message msg, List<String> userList) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);

        mailSetTo(userList, helper);
        helper.setSubject(msg.getSubject());

        if (msg.getContentType() == MessageContentType.Html.getValue()) {
            helper.setText(msg.getMessageText(), true);
        } else {
            helper.setText(msg.getMessageText(), false);
        }

        mailSender.send(message);

    }

    private void simpleMailSetTo(List<String> recipients, SimpleMailMessage helper) {

        helper.setTo(recipients.toArray(new String[0]));
    }

    private void mailSetTo(List<String> recipients, MimeMessageHelper helper) throws MessagingException {
        helper.setTo(recipients.toArray(new String[0]));

    }

    @Override
    public void sendAttachmentsMailWithInputStream(QueueMessageBody msg, String content) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        mailSetTo(msg.getRecipients(), helper);
        helper.setSubject(msg.getMessageInfo().getSubject());

        if (msg.getMessageInfo().getContentType() == MessageContentType.Html.getValue()
                || msg.getMessageInfo().getContentType() == MessageContentType.Template.getValue()) {
            helper.setText(content, true);
        } else {
            helper.setText(content, false);
        }

        if (msg.getAttachmentFileList() != null && msg.getAttachmentFileList().size() > 0) {
            for (Attachment item : msg.getAttachmentFileList()) {
                helper.addAttachment(item.getName(), new BlobInputStreamSource(_blobStorageClient, item.getFileName()));
            }
        }


        mailSender.send(message);

    }

    @Override
    public void sendTemplateMail(QueueMessageBody msg, MessageTemplate template, TemplateMapDto map) throws IOException, MessagingException {

        Context context = new Context();

        context.setVariables(map.getBodyMap());
        String emailContent = _templateEngine.process(template.getTemplate(), context);

        if (map.getSubjectMap() != null && map.getSubjectMap().size() > 0) {
            VelocityEngine velocityEngine = new VelocityEngine();
            VelocityContext contextDb = new VelocityContext();
            for (Map.Entry<String, Object> entry : map.getSubjectMap().entrySet()) {
                contextDb.put(entry.getKey(), entry.getValue());
            }
            StringWriter sw = new StringWriter();
            velocityEngine.evaluate(contextDb, sw, template.getTemplateName(), template.getSubjectTemplate());

            if (sw.toString() != null && sw.toString().length() > 0) {
                msg.getMessageInfo().setSubject(sw.toString());
            }

        }


        sendAttachmentsMailWithInputStream(msg, emailContent);
    }


    @Override
    public void sendInlineResourceMail(List<String> to, String subject, String content, String rscPath, String rscId) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);

            mailSetTo(to, helper);
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource res = new FileSystemResource(new File(rscPath));
            helper.addInline(rscId, res);

            mailSender.send(message);
            System.out.println("InlineResourceMail SUCCESS");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("InlineResourceMai FAILED");
        }
    }

    @Override
    public void sendAttachmentsMail(List<String> to, String subject, String content, String filePath) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            mailSetTo(to, helper);
            helper.setSubject(subject);
            helper.setText(content, true);

            FileSystemResource file = new FileSystemResource(new File(filePath));
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
            helper.addAttachment(fileName, file);

            mailSender.send(message);
            System.out.println("Attachments mail SUCCESS");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Attachments mail FAILED");
        }
    }
}
