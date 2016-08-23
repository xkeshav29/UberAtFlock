package com.uberforflock.util;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by kumarke on 8/23/16.
 */

public class ContextPatternLayout extends PatternLayout {

    protected String host;

    protected String getHostname() {
        if (host == null) {
            try {
                InetAddress address = InetAddress.getLocalHost();
                this.host = address.getHostName();
            } catch (UnknownHostException e) {
                this.host = "localhost";
            }
        }
        return host;
    }

    @Override
    protected PatternParser createPatternParser(String pattern) {
        return new PatternParser(pattern) {

            @Override
            protected void finalizeConverter(char c) {
                PatternConverter pc = null;

                switch (c) {
                    case 'h':
                        pc = new PatternConverter() {
                            @Override
                            protected String convert(LoggingEvent event) {
                                return getHostname();
                            }
                        };
                        break;
                }

                if (pc == null)
                    super.finalizeConverter(c);
                else
                    addConverter(pc);
            }
        };
    }

}
