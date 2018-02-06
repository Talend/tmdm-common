// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.commmon.util.core;


/**
 * @author sbliu
 */
@SuppressWarnings("nls")
public interface PermissionConstants {
    String PERMISSIONTYPE_WRITE = "Write Access";
    String PERMISSIONTYPE_HIDE = "No Access";
    String PERMISSIONTYPE_DENY_CREATE = "No Create Access";
    String PERMISSIONTYPE_DENY_DELETE_PHYSICAL = "No Physic Delete";
    String PERMISSIONTYPE_DENY_DELETE_LOGICAL = "No Logical Delete";
    String PERMISSIONTYPE_WORKFLOW_ACCESS = "Workflow Access";
    //
    String VALIDATION_PERMISSION_MARKER = "validation.permission.validated";
    //
    String PERMISSION_ROLE_SYSTEM_PREFIX = "system_";
    String PERMISSION_ROLE_ADMINISTRATION = "administration";
    String PERMISSION_ROLE_SUPER_ADMIN = "super_admin";
}
