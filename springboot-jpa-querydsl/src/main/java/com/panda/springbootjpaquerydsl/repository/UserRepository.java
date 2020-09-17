package com.panda.springbootjpaquerydsl.repository;

import com.panda.springbootjpaquerydsl.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, Long> {
}
