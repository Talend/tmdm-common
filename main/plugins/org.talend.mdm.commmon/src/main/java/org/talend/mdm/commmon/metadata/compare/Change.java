/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.compare;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.talend.mdm.commmon.metadata.MetadataVisitable;

public abstract class Change {

    public static final String CHANGE_TO_CLOB = "changToClob"; //$NON-NLS-1$

    public static final String HAS_NULL_VALUE = "hasNullValue"; //$NON-NLS-1$

    public static final String TEXT_TO_TEXT = "textToText"; //$NON-NLS-1$

    protected static final String MESSAGE_BUNDLE_NAME = "org.talend.mdm.commmon.metadata.compare.i18n.messages"; //$NON-NLS-1$

    protected final MetadataVisitable element;

    Change(MetadataVisitable element) {
        this.element = element;
    }

    public MetadataVisitable getElement() {
        return element;
    }

    public abstract String getMessage(Locale locale);

    private Map<String, Object> data = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        Change obj = (Change) o;
        return this.element.equals(obj.element);
    }

    @Override
    public int hashCode() {
        return 31 * this.element.hashCode();
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void addData(String key, Object value){
        this.data.put(key, value);
    }
}
