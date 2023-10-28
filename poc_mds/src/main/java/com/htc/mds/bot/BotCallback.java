package com.htc.mds.bot;

import com.htc.mds.entity.Attachment;
import com.htc.mds.model.MessageContentType;
import com.htc.mds.model.QueueMessageBody;
import com.microsoft.bot.builder.BotCallbackHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.Serialization;
import com.microsoft.bot.schema.TextFormatTypes;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class BotCallback implements BotCallbackHandler {

    private QueueMessageBody _message;
    private Configuration _withConfiguration;
    Logger logger = LoggerFactory.getLogger(getClass());

    public BotCallback(QueueMessageBody message, Configuration withConfiguration) {
        _message = message;
        _withConfiguration = withConfiguration;
    }

    @Override
    public CompletableFuture<Void> invoke(TurnContext turnContext) {

        if (_message.getMessageInfo().getContentType() == MessageContentType.TeamsCard.getValue()) {
            Activity activity = createAdaptiveCardAttachment(_message.getMessageInfo().getMessageText());
            return turnContext.sendActivity(activity).thenApply(resourceResponses -> null);
        } else {
            Activity message = getSendMessageFormat(_message);
            return turnContext.sendActivity(message).thenApply(resourceResponses -> null);
        }


    }

    private Activity createAdaptiveCardAttachment(String adaptiveCardJson) {
        try {

            com.microsoft.bot.schema.Attachment attachment = new com.microsoft.bot.schema.Attachment();
            attachment.setContentType("application/vnd.microsoft.card.adaptive");
            attachment.setContent(Serialization.jsonToTree(adaptiveCardJson));
            Activity activity = MessageFactory.attachment(attachment);
            return activity;

        } catch (IOException e) {
            logger.error(e.getMessage(), e);

        }
        return MessageFactory.attachment(new com.microsoft.bot.schema.Attachment());
    }

    private Activity getSendMessageFormat(QueueMessageBody messageBody) {

        Activity message = Activity.createMessageActivity();
        if(messageBody.getMessageInfo().getContentType()==MessageContentType.Markdown.getValue()){
            message.setTextFormat(TextFormatTypes.MARKDOWN);
        }
        else
        {
            message.setTextFormat(TextFormatTypes.PLAIN);
        }

        String msg = messageBody.getMessageInfo().getMessageText();

        if (messageBody.getAttachmentFileList() != null && messageBody.getAttachmentFileList().size() > 0) {

            message.setTextFormat(TextFormatTypes.MARKDOWN);
            for (Attachment item : messageBody.getAttachmentFileList()) {
                msg += " \n* Attachment: [" + item.getName() + "]("
                        + _withConfiguration.getProperty("DownloadHostUrl")
                        + "/files/downloadFile/" + item.getFileName()+") \n";
            }
        }
        message.setText(msg);
        logger.info(msg);
        return message;
    }
}
