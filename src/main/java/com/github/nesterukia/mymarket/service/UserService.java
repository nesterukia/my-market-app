package com.github.nesterukia.mymarket.service;

import com.github.nesterukia.mymarket.dao.UserRepository;
import com.github.nesterukia.mymarket.domain.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.nesterukia.mymarket.utils.UserUtils.USER_ID_COOKIE;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getOrCreate(Long userId, HttpServletResponse response) {
        if (userId == null) {
            User newUser = userRepository.save(new User());
            Cookie cookie = new Cookie(USER_ID_COOKIE, newUser.getId().toString());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            int oneDayAge = 60 * 60 * 24;
            cookie.setMaxAge(oneDayAge);
            response.addCookie(cookie);
            return newUser;
        } else {
            return userRepository.findById(userId).orElseThrow();
        }
    }
}
