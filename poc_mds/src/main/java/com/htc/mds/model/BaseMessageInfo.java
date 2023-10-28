package com.htc.mds.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ApiModel
public class BaseMessageInfo {

    @ApiModelProperty(value = "recipient list:mail format, max count 50")
    @NotNull(message = "recipients cannot be empty")
    @Size(max = 50,min = 1,message = "max size 50")
    private List<
            @Email(message = "The mailbox is not in the correct format")
            @Valid String> recipients;

    @ApiModelProperty(value = "message content")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    @ApiModelProperty(value = "messageType must be between 1 and 2, Teams=1,Mail=2")
    @NotNull(message = "messageType cannot be null")
    @Range(min = 1, max = 2, message = "messageType must be between 1 and 2, Teams=1,Mail=2")
    private int messageType;

    @ApiModelProperty(value = "contentType must be between 0 and 4, text=0,html=1,teamsCard=2,Template=3,Markdown=4")
    @NotNull(message = "contentType cannot be null")
    @Range(min = 0, max = 4, message = "contentType must be between 0 and 4, text=0,html=1,teamsCard=2,Template=3,Markdown=4")
    private int contentType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private long templateId;

    @ApiModelProperty(value = "template params hashMap")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> templateMap;

    @ApiModelProperty(value = "subject params hashMap")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> subjectMap;

    @ApiModelProperty(value = "mail subject,max size=200")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Size(max = 200,message = "max size 200")
    private String subject;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date sendAt;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getSendAt() {
        return sendAt;
    }

    public void setSendAt(Date sendAt) {
        this.sendAt = sendAt;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(long templateId) {
        this.templateId = templateId;
    }

    public Map<String, String> getTemplateMap() {
        return templateMap;
    }

    public void setTemplateMap(Map<String, String> templateMap) {
        this.templateMap = templateMap;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public Map<String, String> getSubjectMap() {
        return subjectMap;
    }

    public void setSubjectMap(Map<String, String> subjectMap) {
        this.subjectMap = subjectMap;
    }
}
