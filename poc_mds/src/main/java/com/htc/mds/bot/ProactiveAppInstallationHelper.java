package com.htc.mds.bot;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.google.gson.JsonPrimitive;
import com.htc.mds.model.TeamAppResult;
import com.htc.mds.model.TeamBotConfig;
import com.htc.mds.service.AuditLogsService;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.Chat;
import com.microsoft.graph.models.User;
import com.microsoft.graph.models.UserScopeTeamsAppInstallation;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserScopeTeamsAppInstallationCollectionPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ProactiveAppInstallationHelper {

    Logger logger = LoggerFactory.getLogger(getClass());
    private AuditLogsService _auditLogs;

    public ProactiveAppInstallationHelper(AuditLogsService auditLogs) {
        _auditLogs = auditLogs;
    }

    private GraphServiceClient getAuthenticatedClient(TeamBotConfig botConfig) {

        final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(botConfig.getMicrosoftAppId())
                .clientSecret(botConfig.getMicrosoftAppPassword())
                .tenantId(botConfig.getTenantId())
                .build();
        List<String> scopes = new ArrayList<String>();
        scopes.add("https://graph.microsoft.com/.default");
        final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(scopes, clientSecretCredential);
        final GraphServiceClient graphClient =
                GraphServiceClient
                        .builder()
                        .authenticationProvider(tokenCredentialAuthProvider)
                        .buildClient();

        return graphClient;
    }

    private void appInstallationForPersonal(String userId, TeamBotConfig botConfig, String messageId) {
        GraphServiceClient graphClient = getAuthenticatedClient(botConfig);
        try {
            logger.info("appInstallationForPersonal " + userId);
            _auditLogs.saveStringAuditLogs("appInstallationForPersonal", "ProactiveAppInstallationHelper", messageId);
            UserScopeTeamsAppInstallation userScopeTeamsAppInstallation = new UserScopeTeamsAppInstallation();
            userScopeTeamsAppInstallation.additionalDataManager().put("teamsApp@odata.bind", new JsonPrimitive("https://graph.microsoft.com/v1.0/appCatalogs/teamsApps/" + botConfig.getAppCatalogTeamAppId()));

            graphClient.users(userId).teamwork().installedApps()
                    .buildRequest()
                    .post(userScopeTeamsAppInstallation);
        } catch (GraphServiceException ex) {
            logger.error(ex.getMessage(), ex);
            if (ex.getError().error.code.equals("Conflict")) {
                triggerConversationUpdate(userId, botConfig, messageId);
            }

        }
    }

    private TeamAppResult getAppInstallationForPersonal(String userId, TeamBotConfig botConfig) {
        logger.info("getAppInstallationForPersonal " + userId);

        GraphServiceClient graphClient = getAuthenticatedClient(botConfig);
        TeamAppResult result = new TeamAppResult();
        result.setResult(false);
        try {
            UserScopeTeamsAppInstallationCollectionPage installedApps = graphClient.users(userId).teamwork().installedApps()
                    .buildRequest()
                    .filter("teamsApp/id eq '" + botConfig.getAppCatalogTeamAppId() + "'")
                    .expand("teamsAppDefinition")
                    .get();
            if (installedApps != null && installedApps.getCurrentPage() != null && installedApps.getCurrentPage().size() > 0) {

                UserScopeTeamsAppInstallation installedApp = installedApps.getCurrentPage().get(0);
                if (installedApp != null) {
                    result.setResult(true);
                    result.setAppId(installedApp.id);
                }

            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return result;
    }

    private void triggerConversationUpdate(String userId, String appId, TeamBotConfig botConfig, String messageId) {
        GraphServiceClient graphClient = getAuthenticatedClient(botConfig);
        try {

            logger.info("triggerConversationUpdate " + userId);
            _auditLogs.saveStringAuditLogs("triggerConversationUpdate", "ProactiveAppInstallationHelper", messageId);
            Chat chat = graphClient.users(userId).teamwork().installedApps(appId).chat()
                    .buildRequest()
                    .get();

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void triggerConversationUpdate(String userId, TeamBotConfig botConfig, String messageId) {

        TeamAppResult appResult = getAppInstallationForPersonal(userId, botConfig);
        if (appResult.getResult()) {
            triggerConversationUpdate(userId, appResult.getAppId(), botConfig, messageId);
        }
    }

    public void installedAppsInPersonalScopeByUserMail(String userId, TeamBotConfig botConfig, String messageId) {

        try {
            logger.info("installedAppsInPersonalScopeByUserMail " + userId);
            _auditLogs.saveStringAuditLogs("installedAppsInPersonalScopeByUserMail", "ProactiveAppInstallationHelper", messageId);
            TeamAppResult appResult = getAppInstallationForPersonal(userId, botConfig);
            if (appResult.getResult()) {
                triggerConversationUpdate(userId, appResult.getAppId(), botConfig, messageId);
            } else {
                appInstallationForPersonal(userId, botConfig, messageId);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

    }

    public String getUserIdByMail(String userMail, TeamBotConfig botConfig) {
        GraphServiceClient graphClient = getAuthenticatedClient(botConfig);
        try {
            logger.info("graphClient getUserIdByMail " + userMail);
            User user = graphClient.users(userMail).buildRequest().get();
            logger.info("graphClient getUserIdByMail userMail:" + userMail + " userId:" + user.id);
            return user.id;

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }


}