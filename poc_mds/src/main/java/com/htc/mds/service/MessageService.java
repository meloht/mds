package com.htc.mds.service;

import com.htc.mds.entity.Message;
import com.htc.mds.model.BaseMessageInfo;
import com.htc.mds.model.MessageDto;
import com.htc.mds.model.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface MessageService {

    MessageResponse deliverMessageWithFiles(BaseMessageInfo messageInfo, String clientId, List<MultipartFile> files) throws IOException;

    void DownloadFile(String id, HttpServletResponse response) throws IOException;

    MessageDto getMessageById(String id);
}
