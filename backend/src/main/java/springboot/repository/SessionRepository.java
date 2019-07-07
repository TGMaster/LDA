package com.spring.boot.repository;

import com.spring.boot.model.entity.Session;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface SessionRepository extends CrudRepository<Session, String> {
    Session findByTokenId(String accessToken);
    Session findByAccountLoginId(String accountLoginId);
}
