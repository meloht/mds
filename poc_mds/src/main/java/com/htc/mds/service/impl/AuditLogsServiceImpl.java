package com.htc.mds.service.impl;

import com.htc.mds.dao.AuditLogsRepository;
import com.htc.mds.dao.MessageRepository;
import com.htc.mds.dao.TemplateMapRepository;
import com.htc.mds.entity.Attachment;
import com.htc.mds.entity.AuditLogs;
import com.htc.mds.entity.Message;
import com.htc.mds.entity.TemplateMap;
import com.htc.mds.model.BaseMessageInfo;
import com.htc.mds.service.AuditLogsService;
import com.htc.mds.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AuditLogsServiceImpl implements AuditLogsService {

    private AuditLogsRepository _auditLogsRepository;
    private TemplateMapRepository _templateMapRepository;
    private MessageRepository _messageRepository;

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public AuditLogsServiceImpl(
            AuditLogsRepository auditLogs,
            TemplateMapRepository templateMapRepository,
            MessageRepository messageRepository
    ) {
        _auditLogsRepository = auditLogs;
        _messageRepository = messageRepository;
        _templateMapRepository = templateMapRepository;
    }

    @Override
    public void saveAuditLogs(Object obj, String functionName, String messageId) {

        if (obj == null)
            return;

        String json = JsonUtils.toString(obj);

        if (json == null || json.length() == 0)
            return;

        AuditLogs auditLogs = new AuditLogs();
        auditLogs.setCreateTimestamp(new Date());
        auditLogs.setFunctionName(functionName);
        auditLogs.setJsonLog(json);
        auditLogs.setMessageId(messageId);

        try {
            _auditLogsRepository.save(auditLogs);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

    }

    @Override
    public void saveStringAuditLogs(String info, String functionName, String messageId) {

        try {
            AuditLogs auditLogs = new AuditLogs();
            auditLogs.setCreateTimestamp(new Date());
            auditLogs.setFunctionName(functionName);
            auditLogs.setJsonLog(info);
            auditLogs.setMessageId(messageId);

            _auditLogsRepository.save(auditLogs);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void SaveMessageLog(BaseMessageInfo info, List<Attachment> listFile, String functionName, String messageId) {

        saveAuditLogs(info, functionName, messageId);
        if (listFile != null && listFile.size() > 0) {
            for (Attachment item : listFile) {
                saveAuditLogs(item, functionName, messageId);
            }

        }

    }

    public List<AuditLogs> queryLogByKeyword(String key, String clientId) {

        List<TemplateMap> mapList = _templateMapRepository.getTemplateMapsByValueLike(key, clientId);

        List<Message> messageList = _messageRepository.findMessagesByMessageFromAndMessageTextLike(clientId, key);

        List<String> ids = new ArrayList<>();

        if (mapList != null && mapList.size() > 0) {
            for (TemplateMap map : mapList) {
                if (!ids.contains(map.getMessageId())) {
                    ids.add(map.getMessageId());
                }

            }
        }
        if (messageList != null && messageList.size() > 0) {
            for (Message map : messageList) {
                if (!ids.contains(map.getId())) {
                    ids.add(map.getId());
                }

            }
        }
        List<AuditLogs> logs= _auditLogsRepository.getAuditLogsByMessageIdIn(ids);

        return logs;
    }
}
