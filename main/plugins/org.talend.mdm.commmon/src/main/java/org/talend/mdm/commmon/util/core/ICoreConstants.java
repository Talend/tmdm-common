/*
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.util.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("nls")
public interface ICoreConstants {

    String X_Schematron = "X_Schematron";

    /**
     * the default svn name in VersionSystem
     */
    String DEFAULT_SVN = "DEFAULT_SVN";

    String DEFAULT_CATEGORY_ROOT = "category";

    /**
     * cross referencing init datacluster & datamodel
     */
    String CrossReferencing_datacluster = "crossreferencing";

    String CrossReferencing_datamodel = "crossreferencing";

    String SYSTEM_ROLE_PREFIX = "System_";

    String SYSTEM_INTERACTIVE_ROLE = SYSTEM_ROLE_PREFIX + "Interactive";

    String SYSTEM_WEB_ROLE = SYSTEM_ROLE_PREFIX + "Web";

    String SYSTEM_VIEW_ROLE = SYSTEM_ROLE_PREFIX + "View";

    String SYSTEM_ADMIN_ROLE = SYSTEM_ROLE_PREFIX + "Admin";

    String ADMIN_PERMISSION = "administration";

    String SUPER_ADMIN = "Super_Admin";

    List<String> SYSTEM_ROLE_LIST = Arrays.asList(SYSTEM_ADMIN_ROLE, SYSTEM_INTERACTIVE_ROLE, SYSTEM_WEB_ROLE, ADMIN_PERMISSION);

    String AUTHENTICATED_PERMISSION = "authenticated";

    String UI_AUTHENTICATED_PERMISSION = "UIAuthenticated";

    String TALEND_NAMESPACE = "http://www.talend.com/mdm";

    public static class rolesConvert {

        public static final HashMap<String, String> oldRoleToNewRoleMap = new HashMap<String, String>();

        static {
            oldRoleToNewRoleMap.put("Default_Admin", ICoreConstants.SYSTEM_ADMIN_ROLE);
            oldRoleToNewRoleMap.put("Default_User", ICoreConstants.SYSTEM_INTERACTIVE_ROLE);
            oldRoleToNewRoleMap.put("Default_Viewer", ICoreConstants.SYSTEM_VIEW_ROLE);
        }
    }

}
