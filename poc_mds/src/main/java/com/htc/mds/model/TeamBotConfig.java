package com.htc.mds.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Configuration
public class TeamBotConfig {

    @Value("${MicrosoftAppId}")
    private String microsoftAppId;

    @Value("${MicrosoftAppPassword}")
    private String microsoftAppPassword;

    @Value("${AppCatalogTeamAppId}")
    private String appCatalogTeamAppId;

    @Value("${TenantId}")
    private String tenantId;

    public String getMicrosoftAppId() {
        return microsoftAppId;
    }

    public void setMicrosoftAppId(String microsoftAppId) {
        this.microsoftAppId = microsoftAppId;
    }

    public String getMicrosoftAppPassword() {
        return microsoftAppPassword;
    }

    public void setMicrosoftAppPassword(String microsoftAppPassword) {
        this.microsoftAppPassword = microsoftAppPassword;
    }

    public String getAppCatalogTeamAppId() {
        return appCatalogTeamAppId;
    }

    public void setAppCatalogTeamAppId(String appCatalogTeamAppId) {
        this.appCatalogTeamAppId = appCatalogTeamAppId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
