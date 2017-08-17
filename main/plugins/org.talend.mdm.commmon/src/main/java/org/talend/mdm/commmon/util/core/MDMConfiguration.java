/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Handles the mdm.conf file
 */
public final class MDMConfiguration {
    
    /**
     * This is the MDM (mdm.conf) configuration property to indicate current server is running in a clustered
     * environment. Setting this property to <code>true</code> may have impacts on the choice of implementation for
     * internal components.
     * 
     * @see com.amalto.core.save.generator.AutoIncrementGenerator
     */
    private static final String SYSTEM_CLUSTER = "system.cluster"; //$NON-NLS-1$

    public static final String ADMIN_PASSWORD = "admin.password"; //$NON-NLS-1$

    public static final String TECHNICAL_PASSWORD = "technical.password"; //$NON-NLS-1$

    public static final String TDS_ROOT_URL = "tds.root.url"; //$NON-NLS-1$

    public static final String TDS_PASSWORD = "tds.password"; //$NON-NLS-1$

    public static final String HZ_GROUP_PASSWORD = "hz.group.password"; //$NON-NLS-1$
    
    public static final String OIDC_CLIENT_SECRET = "oidc.client.secret";

    public static final String MAX_EXPORT_COUNT = "1000"; //$NON-NLS-1$

    public static final String MAX_IMPORT_COUNT = "1000"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(MDMConfiguration.class);

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

    public static boolean isIamEnabled() {
        Properties properties = MDMConfiguration.getConfiguration();
        return Boolean.TRUE.toString().equalsIgnoreCase(properties.getProperty("iam.enabled")); //$NON-NLS-1$
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
            LOGGER.info("MDM Configuration: found in '" + file.getAbsolutePath() + "'."); //$NON-NLS-1$ //$NON-NLS-2$           
            try {
                PropertiesConfiguration config = new PropertiesConfiguration();
                config.setDelimiterParsingDisabled(true);
                config.load(file);
                // Decrypt the passwords in mdm.conf
                config.setProperty(ADMIN_PASSWORD, Crypt.decrypt(config.getString(ADMIN_PASSWORD)));
                config.setProperty(TECHNICAL_PASSWORD, Crypt.decrypt(config.getString(TECHNICAL_PASSWORD)));
                config.setProperty(TDS_PASSWORD, Crypt.decrypt(config.getString(TDS_PASSWORD)));
                config.setProperty(HZ_GROUP_PASSWORD, Crypt.decrypt(config.getString(HZ_GROUP_PASSWORD)));
                properties = ConfigurationConverter.getProperties(config);
            } catch (Exception e) {
                if (!ignoreIfNotFound) {
                    throw new IllegalStateException("Unable to load MDM configuration from '" //$NON-NLS-1$
                            + file.getAbsolutePath() + "'", e); //$NON-NLS-1$
                }
                LOGGER.warn("Unable to load MDM configuration from '" + file.getAbsolutePath() + "'", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            if (!ignoreIfNotFound) {
                throw new IllegalStateException("Unable to load MDM configuration from '" + file.getAbsolutePath() //$NON-NLS-1$
                        + "'"); //$NON-NLS-1$
            }
            LOGGER.warn("Unable to load MDM configuration from '" + file.getAbsolutePath() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
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
            properties.store(out, "MDM configuration file"); //$NON-NLS-1$
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

    public static EDBType getDBType() {
        Object dbType = getConfiguration().get("xmldb.type"); //$NON-NLS-1$
        if (dbType != null && dbType.toString().equals(EDBType.QIZX.getName())) {
            return EDBType.QIZX;
        }
        return EDBType.EXIST;
    }

    public static boolean isExistDb() {
        Object dbType = getConfiguration().get("xmldb.type"); //$NON-NLS-1$
        return !(dbType != null && !dbType.toString().equals(EDBType.EXIST.getName()));
    }

    public static String getAdminPassword() {
        String password = getConfiguration().getProperty("admin.password"); //$NON-NLS-1$
        password = password == null ? "talend" : password; //$NON-NLS-1$
        return password;
    }

    public static String getAdminUser() {
        String user = getConfiguration().getProperty("admin.user"); //$NON-NLS-1$
        user = user == null ? "admin" : user; //$NON-NLS-1$
        return user;
    }

    public static int getAutoEntityFindThreshold() {
        String value = getConfiguration().getProperty("autoentityfind.item.max"); //$NON-NLS-1$
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

}
