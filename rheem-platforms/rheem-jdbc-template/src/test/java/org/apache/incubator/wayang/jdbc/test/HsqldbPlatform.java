package org.apache.incubator.wayang.jdbc.test;

import org.apache.incubator.wayang.jdbc.platform.JdbcPlatformTemplate;

/**
 * {@link JdbcPlatformTemplate} implementation based on HSQLDB for test purposes.
 */
public class HsqldbPlatform extends JdbcPlatformTemplate {

    private static final HsqldbPlatform instance = new HsqldbPlatform();

    public HsqldbPlatform() {
        super("HSQLDB (test)", "hsqldb");
    }

    public static HsqldbPlatform getInstance() {
        return instance;
    }

    @Override
    protected String getJdbcDriverClassName() {
        return org.hsqldb.jdbcDriver.class.getName();
    }
}
