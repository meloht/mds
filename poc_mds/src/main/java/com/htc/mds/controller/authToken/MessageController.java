package com.htc.mds.controller.authToken;


import java.util.List;

import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.htc.mds.model.BaseMessageInfo;
import com.htc.mds.model.MessageCode;
import com.htc.mds.model.MessageDto;
import com.htc.mds.model.MessageResponse;
import com.htc.mds.model.ResponseResult;
import com.htc.mds.service.MessageService;
import com.htc.mds.util.CommUtils;
import com.htc.mds.util.JsonUtils;
import com.htc.mds.util.ValidatedUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(value = "Deliver Message")
@RestController
public class MessageController {

    private MessageService _messageService;

    private Validator _validator;
    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public MessageController(
            Validator validator,
            MessageService messageService

    ) {
        _validator = validator;
        this._messageService = messageService;

    }

    @ApiOperation(value = "Send Message Without Attachment")
    @PostMapping(value = "/message", consumes = "application/json", produces = "application/json;charset=UTF-8")
    public ResponseResult<MessageResponse> deliverMessage(
            @ApiParam(value = "MessageInfo", required = true)
            @Validated
            @RequestBody BaseMessageInfo message, BindingResult bindingResult) {

        logger.info("deliverMessage begin:" + JsonUtils.toString(message));
        ResponseResult<MessageResponse> result;

        try {

            String clientId = CommUtils.getClientId();
            if (bindingResult.hasErrors()) {
                String errorMsg = bindingResult.getFieldError().getDefaultMessage();
                result = ResponseResult.Result(MessageCode.FAILED, errorMsg);
                return result;
            }
            ValidatedUtil.validSubject(message);
            MessageResponse response = _messageService.deliverMessageWithFiles(message, clientId, null);

            result = ResponseResult.Result(MessageCode.SUCCESS, response);

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result = ResponseResult.Result(MessageCode.FAILED, ex.getMessage());
        }

        logger.info("deliverMessage end:" + JsonUtils.toString(result));

        return result;
    }

    @ApiOperation(value = "Send Message With Attachment")
    @PostMapping(value = "/message", consumes = "multipart/form-data", produces = "application/json;charset=UTF-8")
    public ResponseResult<MessageResponse> deliverMessageWithFiles(
            @ApiParam("MessageInfo json string")
            @RequestParam("payload") String payload,
            @RequestParam("files") List<MultipartFile> files) {
        ResponseResult<MessageResponse> result;
        try {

            BaseMessageInfo deliverMessage = JsonUtils.toObject(payload, BaseMessageInfo.class);
            if (deliverMessage == null) {
                throw new RuntimeException("no message request parameter");
            }

            logger.info("deliverMessageWithFiles begin:" + payload);

            String clientId = CommUtils.getClientId();

            ValidatedUtil.validObject(deliverMessage, _validator);

            MessageResponse response = _messageService.deliverMessageWithFiles(deliverMessage, clientId, files);

            result = ResponseResult.Result(MessageCode.SUCCESS, response);

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result = ResponseResult.Result(MessageCode.FAILED, ex.getMessage());
        }
        logger.info("deliverMessageWithFiles end:" + JsonUtils.toString(result));
        return result;
    }


    @ApiOperation(value = "Get Message Process Log")
    @GetMapping(value = "/message/{id}", produces = "application/json;charset=UTF-8")
    public ResponseResult<MessageDto> getMessageById(@PathVariable String id) {

        ResponseResult<MessageDto> result;
        try {
            logger.info("getMessageById begin:" + id);
            MessageDto message = _messageService.getMessageById(id);
            result = ResponseResult.Result(MessageCode.SUCCESS, message);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result = ResponseResult.Result(MessageCode.FAILED, ex.getMessage());
        }
        logger.info("getMessageById end:" + JsonUtils.toString(result));
        return result;
    }

}
