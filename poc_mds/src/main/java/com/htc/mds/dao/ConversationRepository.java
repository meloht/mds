package com.htc.mds.dao;

import com.htc.mds.entity.TeamConversationRef;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;


@Transactional
public interface ConversationRepository extends JpaRepository<TeamConversationRef, String> {


}
