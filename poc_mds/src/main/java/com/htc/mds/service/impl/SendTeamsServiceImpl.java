package com.htc.mds.service.impl;

import com.htc.mds.bot.BotCallback;
import com.htc.mds.bot.ConversationReferences;
import com.htc.mds.bot.ProactiveAppInstallationHelper;
import com.htc.mds.dao.ConversationRepository;
import com.htc.mds.dao.RecipientsStatusRepository;
import com.htc.mds.entity.Attachment;
import com.htc.mds.entity.RecipientsStatus;
import com.htc.mds.entity.TeamConversationRef;
import com.htc.mds.model.QueueMessageBody;
import com.htc.mds.model.TeamBotConfig;
import com.htc.mds.model.UserReceiveStatus;
import com.htc.mds.service.AuditLogsService;
import com.htc.mds.service.SendMessageChannelService;
import com.htc.mds.util.CommUtils;
import com.htc.mds.util.ConstValue;
import com.htc.mds.util.JsonUtils;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.ConversationReference;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component(ConstValue.Teams)
public class SendTeamsServiceImpl implements SendMessageChannelService {

    private final BotFrameworkHttpAdapter _adapter;
    private ConversationReferences _conversationReferences;
    private ProactiveAppInstallationHelper _helper;
    private TeamBotConfig _teamBotConfig;
    private ConversationRepository _conversationRepository;
    private Configuration _withConfiguration;
    private AuditLogsService _auditLogs;
    private RecipientsStatusRepository _recipientsStatusRepository;

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public SendTeamsServiceImpl(
            BotFrameworkHttpAdapter withAdapter,
            ConversationReferences withReferences,
            TeamBotConfig teamBotConfig,
            AuditLogsService auditLogs,
            ConversationRepository conversationRepository,
            Configuration withConfiguration,
            RecipientsStatusRepository recipientsStatusRepository
    ) {

        _adapter = withAdapter;
        _conversationReferences = withReferences;
        _teamBotConfig = teamBotConfig;
        _conversationRepository = conversationRepository;
        _withConfiguration = withConfiguration;
        _auditLogs = auditLogs;
        _recipientsStatusRepository = recipientsStatusRepository;
        _helper = new ProactiveAppInstallationHelper(auditLogs);

    }

    @Override
    public boolean sendMessageToClient(QueueMessageBody message) {
        int num = 0;
        int exNum = 0;
        try {
            List<String> userWaitList = getWaitReceiveUser(message.getMessageInfo().getId());
            exNum = userWaitList.size();

            for (String userMail : userWaitList) {
                String userId = _helper.getUserIdByMail(userMail, _teamBotConfig);

                if (StringUtils.isNotEmpty(userId)) {

                    getConversationReferencesFromDb(userId, message.getMessageInfo().getId(), userMail);

                    if (!_conversationReferences.containsKey(userId)) {
                        String log = "_conversationReferences not exist " + userId + " " + userMail;
                        logger.info(log);
                        _auditLogs.saveStringAuditLogs(log,
                                "SendTeamsServiceImpl", message.getMessageInfo().getId());
                        _helper.installedAppsInPersonalScopeByUserMail(userId, _teamBotConfig, message.getMessageInfo().getId());
                    }

                    if (_conversationReferences.containsKey(userId)) {
                        String log = "_conversationReferences  exist " + userId + " " + userMail;
                        logger.info(log);
                        _auditLogs.saveStringAuditLogs(log,
                                "SendTeamsServiceImpl", message.getMessageInfo().getId());
                        boolean bl = sendMessageToBot(userId, message, userMail);
                        if (bl) {
                            updateRecipientsStatus(message.getMessageInfo().getId(), userMail);
                            num++;
                        }
                    }
                }
            }


        } catch (Exception ex) {
            String logs = "sendMessageToClient failed " + ex.getMessage() + " " + CommUtils.getUserMailString(message.getRecipients());
            logger.info(logs);
            _auditLogs.saveStringAuditLogs(logs, "SendTeamsServiceImpl"
                    , message.getMessageInfo().getId());
            logger.error(ex.getMessage(), ex);
            return false;
        }
        if (num == exNum) {
            return true;
        }
        return false;

    }

    private boolean sendMessageToBot(String userId, QueueMessageBody message, String userMail) {
        ConversationReference reference = _conversationReferences.get(userId);

        try {
            String log = "sendMessageToBot " + userMail;
            logger.info(log);
            _auditLogs.saveStringAuditLogs(log, "SendTeamsServiceImpl", message.getMessageInfo().getId());
            _adapter.continueConversation(
                    _teamBotConfig.getMicrosoftAppId(), reference,
                    new BotCallback(message, _withConfiguration));
            return true;
        } catch (Exception ex) {

            String log = "sendMessageToBot failed " + ex.getMessage() + " " + userMail;
            logger.info(log);

            _auditLogs.saveStringAuditLogs(log, "SendTeamsServiceImpl"
                    , message.getMessageInfo().getId());
            logger.error(ex.getMessage(), ex);
            deleteRef(userId, message.getMessageInfo().getId(), userMail);
        }
        return false;
    }

    private void getConversationReferencesFromDb(String userId, String messageId, String userMail) {

        if (!_conversationReferences.containsKey(userId)) {
            String log = "_conversationReferences not exist " + userId + " " + userMail;
            logger.info(log);
            _auditLogs.saveStringAuditLogs(log, "SendTeamsServiceImpl", messageId);
            boolean bl = _conversationRepository.existsById(userId);
            if (bl) {
                String log2 = "getConversationReferencesFromDb  " + userId + " " + userMail;
                logger.info(log2);
                _auditLogs.saveStringAuditLogs(log2, "SendTeamsServiceImpl", messageId);
                TeamConversationRef teamRef = _conversationRepository.getById(userId);
                if (teamRef != null) {
                    String log3 = "getConversationReferencesFromDb  exist " + userId + " " + userMail;
                    logger.info(log3);
                    _auditLogs.saveStringAuditLogs(log3, "SendTeamsServiceImpl", messageId);
                    ConversationReference reference = JsonUtils.toObject(teamRef.getJsonData(), ConversationReference.class);
                    _conversationReferences.put(userId, reference);
                }
            }

        }

    }

    private void deleteRef(String userId, String messageId, String userMail) {

        try {
            String logs = "_conversationReferences deleteRef " + userId + " " + userMail;
            logger.info(logs);
            _auditLogs.saveStringAuditLogs(logs, "SendTeamsServiceImpl", messageId);
            _conversationRepository.deleteById(userId);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            String logs = "_conversationReferences deleteRef failed " + ex.getMessage() + " " + userId + " " + userMail;
            logger.info(logs);
            _auditLogs.saveStringAuditLogs(logs, "SendTeamsServiceImpl", messageId);
        }

    }

    private void updateRecipientsStatus(String messageId, String userMail) {
        RecipientsStatus recipientsStatus = _recipientsStatusRepository.getRecipientsStatusByMessageIdAndAndUserMail(messageId, userMail);
        recipientsStatus.setReceiveStatus(UserReceiveStatus.ReceiveSuccess.getValue());
        _recipientsStatusRepository.save(recipientsStatus);
    }

    private List<String> getWaitReceiveUser(String messageId) {
        List<String> userList = new ArrayList<String>();
        List<RecipientsStatus> list = _recipientsStatusRepository.getRecipientsStatusesByMessageId(messageId);
        for (RecipientsStatus item : list) {
            if (item.getReceiveStatus() == UserReceiveStatus.ReceiveFailed.getValue()) {
                userList.add(item.getUserMail());
            }
        }
        return userList;
    }


}
