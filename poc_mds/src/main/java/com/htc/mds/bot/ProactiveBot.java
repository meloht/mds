package com.htc.mds.bot;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.htc.mds.dao.ConversationRepository;
import com.htc.mds.entity.TeamConversationRef;
import com.htc.mds.util.JsonUtils;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.teams.TeamsActivityHandler;
import com.microsoft.bot.builder.teams.TeamsInfo;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import com.microsoft.bot.schema.teams.TeamsPagedMembersResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProactiveBot extends TeamsActivityHandler {

    @Value("${server.port:3978}")
    private int port;

    private Configuration _withConfiguration;
    private ConversationRepository _conversationRepository;

    Logger logger = LoggerFactory.getLogger(getClass());

    // Message to send to users when the bot receives a Conversation Update event
    private final String welcomeMessage =
            "Welcome to the Proactive Bot sample.  Navigate to http://localhost:%d/api/notify to proactively message everyone who has previously messaged this bot.";

    private ConversationReferences conversationReferences;

    public ProactiveBot(ConversationReferences withReferences,
                        Configuration withConfiguration,
                        ConversationRepository conversationRepository) {
        conversationReferences = withReferences;
        _withConfiguration = withConfiguration;
        _conversationRepository = conversationRepository;
    }

    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {

        addConversationReference(turnContext.getActivity());

        // Echo back what the user said
        return turnContext
                .sendActivity(MessageFactory.text(String.format("You sent '%s'", turnContext.getActivity().getText())))
                .thenApply(sendResult -> null);
    }

    @Override
    protected CompletableFuture<Void> onMembersAdded(
            List<ChannelAccount> membersAdded,
            TurnContext turnContext
    ) {
        for (ChannelAccount member : membersAdded) {
            if (member.getId() != turnContext.getActivity().getRecipient().getId()) {
                // Add current user to conversation reference.
                addConversationReference(turnContext.getActivity());
            }
        }

        return super.onMembersAdded(membersAdded, turnContext);
    }

    @Override
    protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
        addConversationReference(turnContext.getActivity());
        return super.onConversationUpdateActivity(turnContext);
    }

    private void addConversationReference(Activity activity) {

        ConversationReference conversationReference = activity.getConversationReference();
        logger.info("addConversationReference " + conversationReference.getUser().getAadObjectId());
        conversationReferences.put(conversationReference.getUser().getAadObjectId(), conversationReference);

        saveConversationReferences(conversationReference);
    }


    private void saveConversationReferences(ConversationReference reference) {

        try {
            boolean bl = _conversationRepository.existsById(reference.getUser().getAadObjectId());

            if (!bl) {
                logger.info("saveConversationReferences " + reference.getUser().getAadObjectId());
                TeamConversationRef teamRef = new TeamConversationRef();
                teamRef.setCreateTimestamp(new Date());
                teamRef.setId(reference.getUser().getAadObjectId());
                String json = JsonUtils.toString(reference);

                teamRef.setJsonData(json);

                _conversationRepository.save(teamRef);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }


    }


}
