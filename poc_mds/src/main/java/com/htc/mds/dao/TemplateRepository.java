package com.htc.mds.dao;

import com.htc.mds.entity.Message;
import com.htc.mds.entity.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface TemplateRepository extends JpaRepository<MessageTemplate, Long> {

    List<MessageTemplate> getMessageTemplatesByTemplateNameAndClientId(String templateName, String clientId);

}
