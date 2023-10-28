package com.htc.mds.controller.authToken;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.htc.mds.entity.MessageTemplate;
import com.htc.mds.model.MessageCode;
import com.htc.mds.model.ResponseResult;
import com.htc.mds.model.TemplateDto;
import com.htc.mds.model.TemplateQuery;
import com.htc.mds.model.TemplateResponse;
import com.htc.mds.model.TemplateUpdateDto;
import com.htc.mds.service.TemplateService;
import com.htc.mds.util.CommUtils;
import com.htc.mds.util.JsonUtils;

import io.swagger.annotations.Api;

@Api(value = "Template")
@RestController
public class TemplateController {

    Logger logger = LoggerFactory.getLogger(getClass());
    private TemplateService _templateService;

    @Autowired
    public TemplateController(TemplateService templateService) {
        _templateService = templateService;
    }

    @GetMapping(value = "/template/{id}", produces = "application/json;charset=UTF-8")
    public ResponseResult<MessageTemplate> getTemplateById(@PathVariable long id) {

        ResponseResult<MessageTemplate> result;
        try {
            logger.info("getTemplateById begin:" + id);
            MessageTemplate template = _templateService.getMessageTemplateById(id);
            result = ResponseResult.Result(MessageCode.SUCCESS, template);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result = ResponseResult.Result(MessageCode.FAILED, ex.getMessage());
        }
        logger.info("getTemplateById end:" + JsonUtils.toString(result));
        return result;
    }


    @PostMapping(value = "/template", consumes = "application/json", produces = "application/json;charset=UTF-8")
    public ResponseResult<TemplateResponse> addTemplate(
            @Validated
            @RequestBody TemplateDto template, BindingResult bindingResult) {

        ResponseResult<TemplateResponse> result;
        try {
            logger.info("addTemplate begin:" + JsonUtils.toString(template));

            String clientId = CommUtils.getClientId();

            if (bindingResult.hasErrors()) {
                String errorMsg = bindingResult.getFieldError().getDefaultMessage();
                result = ResponseResult.Result(MessageCode.FAILED, errorMsg);
                return result;
            }

            TemplateResponse templateResponse = new TemplateResponse();

            MessageTemplate messageTemplate = _templateService.saveTemplate(template, clientId);

            templateResponse.setTemplateId(messageTemplate.getId());
            result = ResponseResult.Result(MessageCode.SUCCESS, templateResponse);

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result = ResponseResult.Result(MessageCode.FAILED, ex.getMessage());
        }
        logger.info("addTemplate end:" + JsonUtils.toString(result));
        return result;
    }

    @PutMapping(value = "/template", consumes = "application/json", produces = "application/json;charset=UTF-8")
    public ResponseResult<TemplateResponse> updateTemplate(
            @Validated
            @RequestBody TemplateUpdateDto template, BindingResult bindingResult) {

        ResponseResult<TemplateResponse> result;
        try {

            logger.info("updateTemplate begin:" + JsonUtils.toString(template));

            if (bindingResult.hasErrors()) {
                String errorMsg = bindingResult.getFieldError().getDefaultMessage();
                result = ResponseResult.Result(MessageCode.FAILED, errorMsg);
                return result;
            }

            TemplateResponse templateResponse = new TemplateResponse();

            _templateService.modifyTemplate(template);

            templateResponse.setTemplateId(template.getId());
            result = ResponseResult.Result(MessageCode.SUCCESS, templateResponse);

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result = ResponseResult.Result(MessageCode.FAILED, ex.getMessage());
        }
        logger.info("updateTemplate end:" + JsonUtils.toString(result));
        return result;
    }

    @GetMapping(value = "/template", produces = "application/json;charset=UTF-8")
    public ResponseResult<List<MessageTemplate>> getTemplateListByName(
            @RequestParam(name="name") @NotBlank(message = "name cannot be empty") @Size(max = 60,message = "max size 60") String templateName) {
        
    	ResponseResult<List<MessageTemplate>> result;
        try {
            logger.info("getTemplateListByName begin:" + JsonUtils.toString(templateName));
            String clientId = CommUtils.getClientId();
            List<MessageTemplate> list = _templateService.getMessageTemplatesByName(templateName, clientId);
            result = ResponseResult.Result(MessageCode.SUCCESS, list);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result = ResponseResult.Result(MessageCode.FAILED, ex.getMessage());
        }

        logger.info("getTemplateListByName end:" + JsonUtils.toString(result));
        return result;
    }
    
}
