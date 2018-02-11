/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata;

public enum NoSupportTypes {

    G_ID("ID"), //$NON-NLS-1$
    G_LANGUAGE("language"), //$NON-NLS-1$
    G_NORMALIZED_STRING("normalizedString"), //$NON-NLS-1$
    G_TOKEN("token"), //$NON-NLS-1$
    G_IDREF("IDREF"), //$NON-NLS-1$
    G_IDREFS("IDREFS"), //$NON-NLS-1$
    G_ENTITIES("ENTITIES"), //$NON-NLS-1$
    G_NMTOKEN("NMTOKEN"), //$NON-NLS-1$
    G_QNAME("QName"), //$NON-NLS-1$
    G_NAME("Name"), //$NON-NLS-1$
    G_YEAR_MONTH("gYearMonth"), //$NON-NLS-1$
    G_YEAR("gYear"), //$NON-NLS-1$
    G_MONTH_DAY("gMonthDay"), //$NON-NLS-1$
    G_DAY("gDay"), //$NON-NLS-1$
    G_MONTH("gMonth"); //$NON-NLS-1$

    private final String type;

    NoSupportTypes(String type) {
        this.type = type;
    }

    public static NoSupportTypes getType(String name) {
        for (NoSupportTypes type : values()) {
            if (type.type.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
