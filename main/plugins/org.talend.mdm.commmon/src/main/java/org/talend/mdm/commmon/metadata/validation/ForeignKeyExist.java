/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.validation;

import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.ValidationHandler;

class ForeignKeyExist implements ValidationRule {

    private final ReferenceFieldMetadata field;

    public ForeignKeyExist(ReferenceFieldMetadata field) {
        this.field = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        return ValidationFactory.getRule(field.getReferencedField()).perform(handler)
                && ValidationFactory.getRule(field.getReferencedType()).perform(handler);
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
}
