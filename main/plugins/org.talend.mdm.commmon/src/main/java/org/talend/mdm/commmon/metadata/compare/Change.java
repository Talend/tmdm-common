/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.compare;

import java.util.Locale;

import org.talend.mdm.commmon.metadata.MetadataVisitable;

public abstract class Change {

    protected static final String MESSAGE_BUNDLE_NAME = "org.talend.mdm.commmon.metadata.compare.i18n.messages"; //$NON-NLS-1$

    protected final MetadataVisitable element;

    Change(MetadataVisitable element) {
        this.element = element;
    }

    public MetadataVisitable getElement() {
        return element;
    }

    public abstract String getMessage(Locale locale);
}
