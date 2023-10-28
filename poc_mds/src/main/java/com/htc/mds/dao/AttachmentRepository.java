package com.htc.mds.dao;

import com.htc.mds.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;


@Transactional
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    Attachment getAttachmentByFileName(String fileName);
}
