package com.uberforflock.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by kumarke on 8/23/16.
 */

@Service
public class CsrfTokenDao {

    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenDao.class);
    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public String getIfPresent(String user) {
        String query = "SELECT token FROM oauth_csrf_token_cache WHERE user_id=? AND expiry>NOW()";
        try {
            return this.jdbcTemplate.queryForObject(query, new Object[]{user}, new CsrfTokenMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            logger.error("Could not get csrf token from db for user {}", user, e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public void put(String user, String token) {
        String query = "INSERT INTO oauth_csrf_token_cache(user_id, token, expiry) " +
                "VALUES (?,?,DATE_ADD(NOW(), INTERVAL 30 MINUTE))" +
                "ON DUPLICATE KEY UPDATE token=?, expiry=DATE_ADD(NOW(), INTERVAL 30 MINUTE)";
        try {
            jdbcTemplate.update(query, user, token, token);
        } catch (Exception e) {
            logger.error("Could not persist csrf_token for user {userId}", user, e);
            throw new RuntimeException(e.getMessage());
        }

    }

    public void invalidate(String user) {
        String query = "DELETE FROM oauth_csrf_token_cache WHERE user_id=?";
        jdbcTemplate.update(query, user);
    }

    private static final class CsrfTokenMapper implements RowMapper<String> {
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("token");
        }
    }
}
