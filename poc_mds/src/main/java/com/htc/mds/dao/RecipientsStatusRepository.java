package com.htc.mds.dao;

import com.htc.mds.entity.RecipientsStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface RecipientsStatusRepository extends JpaRepository<RecipientsStatus, Long> {

    RecipientsStatus getRecipientsStatusByMessageIdAndAndUserMail(String messageId, String userMail);

    List<RecipientsStatus> getRecipientsStatusesByMessageId(String messageId);

}
