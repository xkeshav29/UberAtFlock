package com.uberforflock.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * Created by devesh.k on 23/08/16.
 */
@Service
public class UserTokenDao {
    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenDao.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public  void addUserToken(String userId , String userToken){
        jdbcTemplate.update("INSERT INTO installs (userid , usertoken) VALUE (? , ?)",userId,userToken);
    }

    public  String getUserToken(String userId){
        String userToken = jdbcTemplate.queryForObject("SELECT usertoken FROM installs WHERE userid = ? ", new Object[]{userId}, String.class);
        return userToken;
    }
}
