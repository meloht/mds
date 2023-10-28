package com.htc.mds.dao;

import com.htc.mds.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Transactional
public interface MessageRepository extends JpaRepository<Message, String> {

    @Query("update Message set Status= ?1, CompleteTime=?2 where Id= ?3 ")
    @Modifying
    void updateMessageStatusAndCompleteTimeById(int status, Date completeTime, String id);

    @Query(nativeQuery = true,value = "SELECT Id,MessageType,Subject,MessageFrom,MessageText,SendAt,CreateTimestamp,Status,CompleteTime,ContentType,TemplateId from Message where MessageFrom=:clientId and MessageText like %:valueword%  ")
    List<Message> findMessagesByMessageFromAndMessageTextLike(@Param("clientId") String messageFrom,@Param("valueword") String messageText);
}
