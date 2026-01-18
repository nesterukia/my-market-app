package com.github.nesterukia.mymarket.dao;

import com.github.nesterukia.mymarket.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
