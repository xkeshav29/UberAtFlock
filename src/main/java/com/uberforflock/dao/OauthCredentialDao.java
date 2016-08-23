package com.uberforflock.dao;

import com.uberforflock.model.OauthCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Created by kumarke on 8/23/16.
 */
@Service
public class OauthCredentialDao {
    private static final int SEC_TO_MILLI = 1000;

    private static final Logger logger = LoggerFactory.getLogger(OauthCredentialDao.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public OauthCredential getCredential(String userId) {
        String query = "SELECT access_token,refresh_token,expiry,display_name FROM " +
                "oauth_token WHERE user_id=?";
        return this.jdbcTemplate.queryForObject(query, new Object[]{userId}, new OAuthMapper());
    }

    public void setCredential(String userId, OauthCredential credential) {
        Timestamp expiry;
        if (credential.getExpiry() == null || credential.getExpiry().equals(0L))
            expiry = null;
        else
            expiry = new Timestamp(System.currentTimeMillis() + credential.getExpiry() * SEC_TO_MILLI);
        String query = "INSERT INTO oauth_token(user_id, display_name, access_token, refresh_token, expiry) VALUES (?,?,?,?,?)" +
                "ON DUPLICATE KEY UPDATE access_token=VALUES(access_token), refresh_token=VALUES(refresh_token), expiry=VALUES(expiry), updated=NOW()";

        jdbcTemplate.update(query, userId, credential.getDisplayName(), credential.getAccessToken(), credential.getRefreshToken(), expiry);
    }

    public void setRefreshedCredential(String userId, String accessToken, Long expiresIn) {
        Long expiry = System.currentTimeMillis() + expiresIn * SEC_TO_MILLI;
        String query = "UPDATE oauth_token SET access_token=?, expiry=?, updated=NOW()" +
                "WHERE user_id=?";
        Object[] params = new Object[]{accessToken, new Timestamp(expiry), userId};
        jdbcTemplate.update(query, params);
    }

    public void delete(String user) {
        String query = "DELETE FROM oauth_token WHERE user_id=?";
        jdbcTemplate.update(query, user);
        logger.info("Deleted oauth credentials for user {} for app {}", user);
    }

    private static final class OAuthMapper implements RowMapper<OauthCredential> {
        public OauthCredential mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new OauthCredential(rs.getString("display_name"),
                    rs.getString("access_token"),
                    rs.getString("refresh_token"),
                    rs.getTimestamp("expiry") == null ? null : rs.getTimestamp("expiry").getTime()
            );
        }
    }
}
