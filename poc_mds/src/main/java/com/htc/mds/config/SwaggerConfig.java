package com.htc.mds.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.*;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${httpAuth.TokenName}")
    private String authHeaderName;

    @Bean(value = "authTokenApi")
    public Docket authTokenApi(){

        return new Docket(DocumentationType.OAS_30).apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.htc.mds.controller.authToken"))
                .paths(PathSelectors.any()).build()
                .groupName("auth token api")
                .globalRequestParameters(
                        Collections.singletonList(new RequestParameterBuilder()
                        .name(authHeaderName)
                        .description("token")
                        .in(ParameterType.HEADER)
                        .required(true)
                        .query(q->q.model(m->m.scalarModel(ScalarType.STRING)))
                        .build())).ignoredParameterTypes(HttpServletResponse.class, HttpServletRequest.class);
    }

    @Bean(value = "publicApi")
    public Docket publicApi(){

        return new Docket(DocumentationType.OAS_30).apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.htc.mds.controller.noToken"))
                .paths(PathSelectors.any()).build()
                .groupName("public api")
                .ignoredParameterTypes(HttpServletResponse.class, HttpServletRequest.class);
    }

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("MDS API Doc")
                .description("This is a restful api document of MDS.")
                .version("1.0")
                .build();
    }
}
