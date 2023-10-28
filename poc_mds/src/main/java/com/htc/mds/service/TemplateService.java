package com.htc.mds.service;

import com.htc.mds.entity.MessageTemplate;
import com.htc.mds.model.TemplateDto;
import com.htc.mds.model.TemplateQuery;
import com.htc.mds.model.TemplateUpdateDto;

import java.util.List;

public interface TemplateService {

    MessageTemplate saveTemplate(TemplateDto templateDto,String clientId);

    void modifyTemplate(TemplateUpdateDto templateDto);

    MessageTemplate getMessageTemplateById(long id);

    List<MessageTemplate> getMessageTemplatesByName(TemplateQuery templateQuery,String clientId);
    
    List<MessageTemplate> getMessageTemplatesByName(String templateName,String clientId);
}
