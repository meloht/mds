package com.htc.mds.controller.authToken;

import com.htc.mds.entity.AuditLogs;
import com.htc.mds.model.MessageCode;
import com.htc.mds.model.MessageResponse;
import com.htc.mds.model.ResponseResult;
import com.htc.mds.service.AuditLogsService;
import com.htc.mds.util.CommUtils;
import com.htc.mds.util.JsonUtils;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(value = "Logs")
@RestController
public class LogsController {

    Logger logger = LoggerFactory.getLogger(getClass());

    private AuditLogsService _auditLogsService;

    public LogsController(AuditLogsService auditLogsService) {
        _auditLogsService = auditLogsService;
    }

    @GetMapping(value = "/Logs/query/{key}")
    public ResponseResult<List<AuditLogs>> query(@PathVariable String key) {
        ResponseResult<List<AuditLogs>> result;
        try {
            String clientId = CommUtils.getClientId();
            logger.info("query begin:" + key + " clientId:" + clientId);
            List<AuditLogs> logs = _auditLogsService.queryLogByKeyword(key, clientId);

            result = ResponseResult.Result(MessageCode.SUCCESS, logs);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result = ResponseResult.Result(MessageCode.FAILED, ex.getMessage());
        }
        logger.info("query end:" + JsonUtils.toString(result));

        return result;
    }
}
