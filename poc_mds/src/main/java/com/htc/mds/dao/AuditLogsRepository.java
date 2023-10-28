package com.htc.mds.dao;

import com.htc.mds.entity.AuditLogs;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface AuditLogsRepository extends JpaRepository<AuditLogs, Long> {

    List<AuditLogs> getAuditLogsByMessageIdIn(List<String> messageIds);
}
