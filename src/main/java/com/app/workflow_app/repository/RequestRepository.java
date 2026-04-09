package com.app.workflow_app.repository;

import com.app.workflow_app.model.Request;
import com.app.workflow_app.model.RequestStatus;
import com.app.workflow_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequester(User requester);

    List<Request> findByStatus(RequestStatus status);
}
