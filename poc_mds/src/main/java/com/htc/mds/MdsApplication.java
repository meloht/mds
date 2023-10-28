package com.htc.mds;

import com.htc.mds.blobstorage.BlobStorageClient;
import com.htc.mds.bot.ConversationReferences;
import com.htc.mds.bot.ProactiveBot;
import com.htc.mds.dao.ConversationRepository;
import com.htc.mds.dao.MessageRepository;
import com.htc.mds.dao.RecipientsStatusRepository;
import com.htc.mds.service.AuditLogsService;
import com.htc.mds.service.MessageChannelFactory;
import com.htc.mds.servicebus.ReceiveMessageClient;
import com.htc.mds.servicebus.SendMessageClient;
import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.integration.AdapterWithErrorHandler;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.integration.spring.BotController;
import com.microsoft.bot.integration.spring.BotDependencyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@Import({BotController.class})
public class MdsApplication extends BotDependencyConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(MdsApplication.class, args);
    }


    /**
     * Returns the Bot for this application.
     *
     * <p>
     * The @Component annotation could be used on the Bot class instead of this method
     * with the @Bean annotation.
     * </p>
     *
     * @return The Bot implementation for this application.
     */
    @Bean
    public Bot getBot(ConversationReferences conversationReferences,
                      Configuration configuration,
                      ConversationRepository conversationRepository) {
        return new ProactiveBot(conversationReferences, configuration, conversationRepository);
    }

    /**
     * The shared ConversationReference Map. This hold a list of conversations for
     * the bot.
     *
     * @return A ConversationReferences object.
     */
    @Bean
    public ConversationReferences getConversationReferences() {
        return new ConversationReferences();
    }

    /**
     * Returns a custom Adapter that provides error handling.
     *
     * @param configuration The Configuration object to use.
     * @return An error handling BotFrameworkHttpAdapter.
     */
    @Override
    public BotFrameworkHttpAdapter getBotFrameworkHttpAdaptor(Configuration configuration) {
        return new AdapterWithErrorHandler(configuration);
    }

    @Bean
    public SendMessageClient getSendMessageClient(Configuration configuration, BlobStorageClient blobStorageClient) {
        SendMessageClient client = new SendMessageClient(configuration, blobStorageClient);
        client.init();
        return client;
    }

    @Bean
    public ReceiveMessageClient getReceiveMessageClient(Configuration configuration,
                                                        MessageChannelFactory factory,
                                                        MessageRepository messageRepository,
                                                        AuditLogsService auditLogs,
                                                        BlobStorageClient blobStorageClient,
                                                        RecipientsStatusRepository recipientsStatusRepository
    ) {
        ReceiveMessageClient client = new ReceiveMessageClient(configuration, factory,
                messageRepository, auditLogs,blobStorageClient,recipientsStatusRepository);
        client.init();
        return client;
    }

    @Bean
    public BlobStorageClient getBlobStorageClient(Configuration configuration) {
        BlobStorageClient client = new BlobStorageClient(configuration);
        client.init();
        return client;
    }

}
