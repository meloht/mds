package com.htc.mds.controller.noToken;

import com.htc.mds.service.MessageService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Api(value = "Files")
@RestController
public class FilesController {

    Logger logger = LoggerFactory.getLogger(getClass());
    private MessageService _messageService;

    @Autowired
    public FilesController(MessageService messageService) {
        this._messageService = messageService;
    }

    @GetMapping(value = "/files/downloadFile/{id}")
    public void DownloadFile(@PathVariable String id, HttpServletResponse response) {

        try {
            logger.info("DownloadFile begin:" + id);
            _messageService.DownloadFile(id, response);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        logger.info("DownloadFile end");
    }
}
