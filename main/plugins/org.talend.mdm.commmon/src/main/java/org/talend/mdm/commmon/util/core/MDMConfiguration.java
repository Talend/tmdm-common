/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.util.core;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the mdm.conf file
 */
@SuppressWarnings("nls")
public final class MDMConfiguration {

    /**
     * This is the MDM (mdm.conf) configuration property to indicate current server is running in a clustered
     * environment. Setting this property to <code>true</code> may have impacts on the choice of implementation for
     * internal components.
     *
     * @see com.amalto.core.save.generator.AutoIncrementGenerator
     */
    public static final String SYSTEM_CLUSTER = "system.cluster";

    public static final String ADMIN_PASSWORD = "admin.password";

    public static final String TECHNICAL_PASSWORD = "technical.password";

    public static final String HZ_GROUP_NAME = "hz.group.name";

    public static final String HZ_GROUP_PASSWORD = "hz.group.password";

    public static final String MAX_EXPORT_COUNT = "1000";

    public static final String MAX_IMPORT_COUNT = "1000";

    /**
     * TDS Configuration
     */
    private static final String TDS_ROOT_URL = "tds.root.url";

    private static final String TDS_USER = "tds.user";

    public static final String TDS_PASSWORD = "tds.password";

    private static final String TDS_CORE_URL = "tds.core.url";

    private static final String TDS_SCHEMA_URL = "tds.schema.url";

    private static final String TDS_API_VERSION = "tds.api.version";

    public static final String OIDC_CLIENT_SECRET = "oidc.client.secret";

    public static final String SCIM_USER = "scim.username";

    public static final String SCIM_PASSWORD = "scim.password";

    private static final Logger LOGGER = LoggerFactory.getLogger(MDMConfiguration.class);

    private static MDMConfiguration instance;

    private String location;

    private Properties properties = null;

    private MDMConfiguration(String location) {
        this.location = location;
    }

    public static synchronized MDMConfiguration createConfiguration(String location, boolean ignoreIfNotFound) {
        if (instance != null) {
            throw new IllegalStateException();
        }
        instance = new MDMConfiguration(location);
        instance.getProperties(true, ignoreIfNotFound);
        return instance;
    }

    public static synchronized Properties getConfiguration() {
        return getConfiguration(false);
    }

    public static synchronized Properties getConfiguration(boolean reload) {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance.getProperties(reload, false);
    }

    public static synchronized void save() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        instance.saveProperties();
    }

    public static boolean isClusterEnabled(){
        Properties properties = MDMConfiguration.getConfiguration();
        return Boolean.parseBoolean(properties.getProperty(SYSTEM_CLUSTER, Boolean.FALSE.toString()));
    }

    public static String getTdsRootUrl() {
        Properties properties = MDMConfiguration.getConfiguration();
        return properties.getProperty(TDS_ROOT_URL);
    }

    public static String getTdsUser() {
        Properties properties = MDMConfiguration.getConfiguration();
        return properties.getProperty(TDS_USER);
    }

    public static String getTdsPassword() {
        Properties properties = MDMConfiguration.getConfiguration();
        return properties.getProperty(TDS_PASSWORD);
    }

    public static String getTdsCoreUrl() {
        Properties properties = MDMConfiguration.getConfiguration();
        return properties.getProperty(TDS_CORE_URL);
    }

    public static String getTdsSchemaUrl() {
        Properties properties = MDMConfiguration.getConfiguration();
        return properties.getProperty(TDS_SCHEMA_URL);
    }

    public static String getTdsApiVersion() {
        Properties properties = MDMConfiguration.getConfiguration();
        return properties.getProperty(TDS_API_VERSION);
    }

    private Properties getProperties(boolean reload, boolean ignoreIfNotFound) {
        if (reload) {
            properties = null;
        }
        if (properties != null) {
            return properties;
        }
        properties = new Properties();

        File file = new File(location);
        if (file.exists()) {
            LOGGER.info("MDM Configuration: found in '" + file.getAbsolutePath() + "'.");
            try {
                PropertiesConfiguration config = new PropertiesConfiguration();
                config.setDelimiterParsingDisabled(true);
                config.setEncoding(StandardCharsets.UTF_8.name());
                config.load(file);
                // Decrypt the passwords in mdm.conf
                config.setProperty(ADMIN_PASSWORD, Crypt.decrypt(config.getString(ADMIN_PASSWORD)));
                config.setProperty(TECHNICAL_PASSWORD, Crypt.decrypt(config.getString(TECHNICAL_PASSWORD)));
                config.setProperty(TDS_PASSWORD, Crypt.decrypt(config.getString(TDS_PASSWORD)));
                config.setProperty(HZ_GROUP_PASSWORD, Crypt.decrypt(config.getString(HZ_GROUP_PASSWORD)));
                config.setProperty(SCIM_PASSWORD, Crypt.decrypt(config.getString(SCIM_PASSWORD)));
                properties = ConfigurationConverter.getProperties(config);
            } catch (Exception e) {
                if (!ignoreIfNotFound) {
                    throw new IllegalStateException("Unable to load MDM configuration from '" + file.getAbsolutePath() + "'", e);
                }
                LOGGER.warn("Unable to load MDM configuration from '" + file.getAbsolutePath() + "'", e);
            }
        } else {
            if (!ignoreIfNotFound) {
                throw new IllegalStateException("Unable to load MDM configuration from '" + file.getAbsolutePath() + "'");
            }
            LOGGER.warn("Unable to load MDM configuration from '" + file.getAbsolutePath() + "'");
        }
        return properties;
    }

    /**
     * save configure file
     */
    private void saveProperties() {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(location);
            properties.store(out, "MDM configuration file");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public static String getAdminPassword() {
        String password = getConfiguration().getProperty("admin.password");
        password = password == null ? "talend" : password;
        return password;
    }

    public static String getAdminUser() {
        String user = getConfiguration().getProperty("admin.user");
        user = user == null ? "admin" : user;
        return user;
    }

    public static int getAutoEntityFindThreshold() {
        String value = getConfiguration().getProperty("autoentityfind.item.max");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

}
