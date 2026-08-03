package com.library.repository;

import com.library.model.Message;
import com.library.model.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByOrderByCreatedAtDesc();

    List<Message> findByStatusOrderByCreatedAtDesc(MessageStatus status);

    long countByStatus(MessageStatus status);
}
