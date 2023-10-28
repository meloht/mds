package com.htc.mds.config;


import com.htc.mds.filter.Http401ForbiddenEntryPoint;
import com.htc.mds.filter.PreAuthTokenHeaderFilter;
import com.htc.mds.model.AuthClientInfo;
import com.htc.mds.util.JsonUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.ExceptionTranslationFilter;

import java.io.IOException;
import java.util.HashMap;

@Configuration
@EnableWebSecurity
public class AuthTokenSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${httpAuth.TokenName}")
    private String authHeaderName;

    @Value("${httpAuth.ssoUrl}")
    private String ssoUrl;

    Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        PreAuthTokenHeaderFilter filter = new PreAuthTokenHeaderFilter(authHeaderName);

        filter.setAuthenticationManager(new AuthenticationManager() {
            @Override
            public Authentication authenticate(Authentication authentication)
                    throws AuthenticationException {
                AuthClientInfo principal = (AuthClientInfo) authentication.getPrincipal();

                if (!authAccessToken(principal)) {
                    throw new BadCredentialsException("The accessToken is invalid or expired.");

                }

                authentication.setAuthenticated(true);
                return authentication;
            }
        });
        
		httpSecurity.authorizeRequests()
			.antMatchers("/message/**", "/template/**")
			.authenticated()
			.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.csrf()
			.disable();
		
		httpSecurity.addFilter(filter)
			.addFilterBefore(new ExceptionTranslationFilter(
					new Http401ForbiddenEntryPoint()),filter.getClass());
    }

    @Override
    public void configure(WebSecurity web){
        web.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                // allow anonymous resource requests
                .antMatchers(
                        "/",
                        "/files/downloadFile/**",
                        "/api/messages/**",
                        "/error/**",
                        "/*.html",
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/h2-console/**",

                        // swagger
                        "/swagger-ui/index.html",
                        "/swagger-ui/**",
                        "/webjars/**",
                        // swagger api json
                        "/v3/api-docs",
                        //用来获取支持的动作
                        "/swagger-resources/configuration/ui",
                        //用来获取api-docs的URI
                        "/swagger-resources",
                        //安全选项
                        "/swagger-resources/configuration/security",
                        "/swagger-resources/**"
                );
    }

    private boolean authAccessToken(AuthClientInfo authClientInfo) {
        String url = ssoUrl + "profile?access_token=" + authClientInfo.getToken();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            HttpEntity entity1 = response.getEntity();
            String json = EntityUtils.toString(entity1);

            logger.info("authAccessToken:" + json);

            HashMap<String, Object> map = JsonUtils.toObject(json, HashMap.class);

            if (map.containsKey("error")) {
                return false;
            }

            if (map.containsKey("id")) {
                authClientInfo.setAuthStatus(true);
                authClientInfo.setClientId(String.valueOf(map.get("id")));
                return true;
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ee) {
                    logger.error(ee.getMessage(), ee);
                }

            }

        }
        return false;

    }
}
