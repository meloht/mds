package com.htc.mds.dao;

import com.htc.mds.entity.MessageTemplate;
import com.htc.mds.entity.TemplateMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface TemplateMapRepository extends JpaRepository<TemplateMap, Long> {
    List<TemplateMap> getTemplateMapsByTemplateIdAndMessageId(long templateId, String messageId);

    @Query(nativeQuery = true,value=" select a.id,a.templateId,a.MessageId,a.Name,a.Value,a.CreateTimestamp,a.MapType "+
           " from TemplateMap a inner join MessageTemplate b on a.TemplateId=b.id  where a.value like %:valueword% and b.ClientId=:client ")
    List<TemplateMap> getTemplateMapsByValueLike(@Param("valueword") String value, @Param("client") String clientId);
}
