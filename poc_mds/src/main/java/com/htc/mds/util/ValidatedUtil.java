package com.htc.mds.util;

import com.htc.mds.model.BaseMessageInfo;
import com.htc.mds.model.MessageContentType;
import com.htc.mds.model.MessageType;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Set;

public class ValidatedUtil {

    public static void validObject(Object bean, Validator validator) {
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if (!constraintViolationSet.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation violation : constraintViolationSet) {
                sb.append(violation.getMessage());
            }

            throw new ValidationException(sb.toString());
        }
    }

    public static void validSubject(BaseMessageInfo info) {

        if (info.getMessageType() == MessageType.Mail.getValue()) {
            if (StringUtils.isEmpty(info.getSubject())) {
                throw new ValidationException("send mail message,subject cannot be empty!");
            }
        }
        if (info.getContentType() == MessageContentType.Template.getValue()) {
            if (info.getTemplateMap() == null || info.getTemplateMap().size() == 0) {
                throw new ValidationException("send template mail message,TemplateMap cannot be empty!");
            }
        }

        if (info.getContentType() != MessageContentType.Template.getValue()) {
            if (StringUtils.isEmpty(info.getMessage())) {
                throw new ValidationException("message cannot be empty!");
            }
        }
        if (info.getMessageType() == MessageType.Teams.getValue()) {
            if (info.getContentType() == MessageContentType.Template.getValue()
                    || info.getContentType() == MessageContentType.Html.getValue()) {
                throw new ValidationException("send teams message,ContentType should be Text,TeamsCard or Markdown !");
            }
        }
        if (info.getMessageType() == MessageType.Mail.getValue()) {
            if (info.getContentType() == MessageContentType.TeamsCard.getValue()||
                    info.getContentType() == MessageContentType.Markdown.getValue()) {
                throw new ValidationException("send mail message,ContentType should be Text,Html,Template !");
            }
        }
    }
}
