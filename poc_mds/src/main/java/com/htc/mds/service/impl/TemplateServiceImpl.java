package com.htc.mds.service.impl;

import com.htc.mds.dao.TemplateRepository;
import com.htc.mds.entity.MessageTemplate;
import com.htc.mds.model.TemplateDto;
import com.htc.mds.model.TemplateQuery;
import com.htc.mds.model.TemplateUpdateDto;
import com.htc.mds.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TemplateServiceImpl implements TemplateService {

    private TemplateRepository _templateRepository;

    @Autowired
    public TemplateServiceImpl(TemplateRepository templateRepository
    ) {
        _templateRepository = templateRepository;
    }

    @Override
    public MessageTemplate saveTemplate(TemplateDto templateDto, String clientId) {
        MessageTemplate template = buildMessageTemplate(templateDto, clientId);
        template = _templateRepository.save(template);
        return template;
    }

    @Override
    public void modifyTemplate(TemplateUpdateDto templateDto) {

        MessageTemplate template = _templateRepository.getById(templateDto.getId());
        template.setTemplateName(templateDto.getTemplateName());
        template.setTemplate(templateDto.getTemplateContent());
        template.setSubjectTemplate(templateDto.getSubjectTemplate());
        template.setModifyTimestamp(new Date());
        _templateRepository.save(template);

    }

    @Override
    public MessageTemplate getMessageTemplateById(long id) {
        return _templateRepository.getById(id);
    }

    @Override
    public List<MessageTemplate> getMessageTemplatesByName(TemplateQuery templateQuery, String clientId) {
        return _templateRepository.getMessageTemplatesByTemplateNameAndClientId(templateQuery.getName(), clientId);
    }

    @Override
    public List<MessageTemplate> getMessageTemplatesByName(String templateName, String clientId) {
        return _templateRepository.getMessageTemplatesByTemplateNameAndClientId(templateName, clientId);
    }
    
    private MessageTemplate buildMessageTemplate(TemplateDto templateDto, String clientId) {

        MessageTemplate template = new MessageTemplate();
        template.setCreateTimestamp(new Date());
        template.setClientId(clientId);
        template.setTemplateName(templateDto.getTemplateName());
        template.setTemplate(templateDto.getTemplateContent());
        template.setModifyTimestamp(new Date());
        template.setSubjectTemplate(templateDto.getSubjectTemplate());

        return template;
    }


}
