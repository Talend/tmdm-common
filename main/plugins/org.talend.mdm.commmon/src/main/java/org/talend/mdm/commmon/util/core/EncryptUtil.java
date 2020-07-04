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

import static org.talend.mdm.commmon.util.core.MDMConfiguration.ADMIN_PASSWORD;
import static org.talend.mdm.commmon.util.core.MDMConfiguration.HZ_GROUP_PASSWORD;
import static org.talend.mdm.commmon.util.core.MDMConfiguration.OIDC_CLIENT_SECRET;
import static org.talend.mdm.commmon.util.core.MDMConfiguration.SCIM_PASSWORD;
import static org.talend.mdm.commmon.util.core.MDMConfiguration.TDS_PASSWORD;
import static org.talend.mdm.commmon.util.core.MDMConfiguration.TECHNICAL_PASSWORD;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;;

public class EncryptUtil {

    private static final Logger LOGGER = LogManager.getLogger(EncryptUtil.class);

    private static String DB_DEFAULT_DATASOURCE = "db.default.datasource"; //$NON-NLS-1$

    public static String ACTIVEMQ_PASSWORD = "mdm.routing.engine.broker.password"; //$NON-NLS-1$

    private static boolean updated = false;

    private static String dataSourceName;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            return;
        }
        encrypt(args[0]);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean encrypt(String path) {
        Map<String, String[]> propertiesFileMap = new HashMap<String, String[]>();

        String[] mdmProperties = { ADMIN_PASSWORD, TECHNICAL_PASSWORD, TDS_PASSWORD, HZ_GROUP_PASSWORD, ACTIVEMQ_PASSWORD,
                OIDC_CLIENT_SECRET, SCIM_PASSWORD };
        propertiesFileMap.put("mdm.conf", mdmProperties); //$NON-NLS-1$

        Iterator it = propertiesFileMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String[]> entry = (Entry<String, String[]>) it.next();
            encryptProperties(path + entry.getKey(), entry.getValue());
        }

        encyptXML(path + "datasources.xml"); //$NON-NLS-1$
        return true;
    }

    public static void encryptProperties(String location, String[] properties) {
        try {
            File file = new File(location);
            if (file.exists()) {
                PropertiesConfiguration config = new PropertiesConfiguration();
                config.setDelimiterParsingDisabled(true);
                config.setEncoding(StandardCharsets.UTF_8.name());
                config.load(file);
                if (file.getName().equals("mdm.conf")) { //$NON-NLS-1$
                    dataSourceName = config.getString(DB_DEFAULT_DATASOURCE) == null ? StringUtils.EMPTY : config
                            .getString(DB_DEFAULT_DATASOURCE);
                }
                updated = false;
                for (String property : properties) {
                    String password = config.getString(property);
                    if (StringUtils.isNotEmpty(password) && !password.endsWith(Crypt.ENCRYPT)) {
                        password = Crypt.encrypt(password);
                        config.setProperty(property, password);
                        updated = true;
                    }
                }
                if (updated) {
                    config.save(file);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Encrypt password in '" + location + "' error.", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public static void encyptXML(String location) {
        try {
            File file = new File(location);
            if (file.exists()) {
                XMLConfiguration config = new XMLConfiguration();
                config.setDelimiterParsingDisabled(true);
                config.setEncoding(StandardCharsets.UTF_8.name());
                config.load(file);
                List<Object> dataSources = config.getList("datasource.[@name]"); //$NON-NLS-1$
                int index = -1;
                for (int i = 0; i < dataSources.size(); i++) {
                    if (dataSources.get(i).equals(dataSourceName)) {
                        index = i;
                        break;
                    }
                }
                updated = false;
                if (index >= 0) {
                    HierarchicalConfiguration sub = config.configurationAt("datasource(" + index + ")"); //$NON-NLS-1$//$NON-NLS-2$
                    encryptByXpath(sub, "master.rdbms-configuration.connection-password"); //$NON-NLS-1$
                    encryptByXpath(sub, "master.rdbms-configuration.init.connection-password"); //$NON-NLS-1$
                    encryptByXpath(sub, "staging.rdbms-configuration.connection-password"); //$NON-NLS-1$
                    encryptByXpath(sub, "staging.rdbms-configuration.init.connection-password"); //$NON-NLS-1$
                    encryptByXpath(sub, "system.rdbms-configuration.connection-password"); //$NON-NLS-1$
                    encryptByXpath(sub, "system.rdbms-configuration.init.connection-password"); //$NON-NLS-1$
                }
                if (updated) {
                    config.save(file);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Encrypt password in '" + location + "' error."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static void encryptByXpath(HierarchicalConfiguration config, String xpath) throws Exception {
        String password = config.getString(xpath);
        if (StringUtils.isNotEmpty(password) && !password.endsWith(Crypt.ENCRYPT)) {
            config.setProperty(xpath, Crypt.encrypt(password));
            updated = true;
        }
    }
}
