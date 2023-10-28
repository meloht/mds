package com.htc.mds.service;

import com.htc.mds.entity.Attachment;
import com.htc.mds.entity.AuditLogs;
import com.htc.mds.model.BaseMessageInfo;

import java.util.List;

public interface AuditLogsService {
    void saveAuditLogs(Object obj, String functionName,String messageId);

    void saveStringAuditLogs(String info, String functionName,String messageId);
    void SaveMessageLog(BaseMessageInfo info, List<Attachment> listFile, String functionName, String messageId);

    List<AuditLogs> queryLogByKeyword(String key, String clientId);
}
