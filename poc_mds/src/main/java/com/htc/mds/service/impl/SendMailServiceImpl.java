package com.htc.mds.service.impl;

import com.htc.mds.dao.AuditLogsRepository;
import com.htc.mds.dao.RecipientsStatusRepository;
import com.htc.mds.dao.TemplateMapRepository;
import com.htc.mds.dao.TemplateRepository;
import com.htc.mds.entity.MessageTemplate;
import com.htc.mds.entity.RecipientsStatus;
import com.htc.mds.entity.TemplateMap;
import com.htc.mds.model.*;
import com.htc.mds.service.AuditLogsService;
import com.htc.mds.service.MailService;
import com.htc.mds.util.CommUtils;
import com.htc.mds.util.ConstValue;
import com.htc.mds.service.SendMessageChannelService;
import com.microsoft.bot.integration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(ConstValue.Mail)
public class SendMailServiceImpl implements SendMessageChannelService {


    private MailService _mailService;
    private AuditLogsService _auditLogs;
    private TemplateMapRepository _templateMapRepository;
    private TemplateRepository _templateRepository;
    private RecipientsStatusRepository _recipientsStatusRepository;

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public SendMailServiceImpl(
            MailService mailService,
            AuditLogsService auditLogsRepository,
            TemplateMapRepository templateMapRepository,
            TemplateRepository templateRepository,
            RecipientsStatusRepository recipientsStatusRepository
    ) {
        this._mailService = mailService;
        this._auditLogs = auditLogsRepository;
        _templateMapRepository = templateMapRepository;
        _templateRepository = templateRepository;
        _recipientsStatusRepository = recipientsStatusRepository;
    }


    @Override
    public boolean sendMessageToClient(QueueMessageBody message) {

        String userListString = CommUtils.getUserMailString(message.getRecipients());
        try {

            if (message.getMessageInfo().getContentType() == MessageContentType.Template.getValue()) {

                String logs = "sendMessageToClient sendTemplateMail " + userListString;

                logger.info(logs);
                _auditLogs.saveStringAuditLogs(logs, "SendMailServiceImpl"
                        , message.getMessageInfo().getId());

                MessageTemplate template = _templateRepository.getById(message.getMessageInfo().getTemplateId());
                TemplateMapDto maps = getTemplateMap(message.getMessageInfo().getId(), message.getMessageInfo().getTemplateId());
                _mailService.sendTemplateMail(message, template, maps);

            } else {
                String logs = "sendMessageToClient sendAttachmentsMailWithInputStream " + userListString;
                logger.info(logs);
                _auditLogs.saveStringAuditLogs(logs, "SendMailServiceImpl"
                        , message.getMessageInfo().getId());
                _mailService.sendAttachmentsMailWithInputStream(message, message.getMessageInfo().getMessageText());
            }

            updateRecipientsStatus(message.getMessageInfo().getId());

            return true;
        } catch (Exception ex) {
            String log = "sendMessageToClient failed " + ex.getMessage() + " " + userListString;
            logger.info(log);
            _auditLogs.saveStringAuditLogs(log, "SendMailServiceImpl"
                    , message.getMessageInfo().getId());
            logger.error(ex.toString());
        }

        return false;
    }

    private TemplateMapDto getTemplateMap(String messageId, long templateId) {
        TemplateMapDto dto = new TemplateMapDto();

        Map<String, Object> mapBody = new HashMap<String, Object>();
        Map<String, Object> mapSubject = new HashMap<String, Object>();

        List<TemplateMap> mapList = _templateMapRepository.getTemplateMapsByTemplateIdAndMessageId(templateId, messageId);
        if (mapList != null && mapList.size() > 0) {
            for (TemplateMap item : mapList) {
                if (item.getMapType() == TemplateMapType.Body.getValue()) {
                    mapBody.put(item.getName(), item.getValue());
                }
                if (item.getMapType() == TemplateMapType.Subject.getValue()) {
                    mapSubject.put(item.getName(), item.getValue());
                }

            }
        }
        dto.setBodyMap(mapBody);
        dto.setSubjectMap(mapSubject);

        return dto;
    }

    private void updateRecipientsStatus(String messageId) {
        List<RecipientsStatus> list = _recipientsStatusRepository.getRecipientsStatusesByMessageId(messageId);
        for (RecipientsStatus item : list) {
            item.setReceiveStatus(UserReceiveStatus.ReceiveSuccess.getValue());
        }
        _recipientsStatusRepository.saveAll(list);
    }
}
