package com.uberforflock.service;

import com.uberforflock.dao.CsrfTokenDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by kumarke on 8/23/16.
 */
@Service
public class CsrfTokenService{

    private CsrfTokenDao csrfTokenDao;

    @Autowired
    public CsrfTokenService(CsrfTokenDao csrfTokenDao) {
        this.csrfTokenDao = csrfTokenDao;
    }

    public String getCsrfToken(String userGuid) {
        String prevToken = csrfTokenDao.getIfPresent(userGuid);
        String csrfStateToken = (prevToken != null) ? prevToken : new BigInteger(130, new SecureRandom()).toString(32);
        csrfTokenDao.put(userGuid, csrfStateToken);
        return csrfStateToken;
    }

    public boolean isValidToken(String csrfToken, String userGuid) {
        if (csrfToken != null && csrfToken.equals(csrfTokenDao.getIfPresent(userGuid)))
            return true;
        return false;
    }

}
