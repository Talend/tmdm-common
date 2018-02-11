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

import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.ValidationError;
import org.talend.mdm.commmon.metadata.ValidationHandler;
import org.w3c.dom.Element;

/**
 * This rule ensures no foreign key is part of type's key. A type key is always defined from values defined
 * <b>inside</b> the type.
 */
class ForeignKeyCannotBeKey implements ValidationRule {

    private final ReferenceFieldMetadata field;

    public ForeignKeyCannotBeKey(ReferenceFieldMetadata field) {
        this.field = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        if (field.isKey()) {
            handler.error(field, "Key field cannot be a foreign key element.",
                    field.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                    field.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                    field.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                    ValidationError.FIELD_KEY_CANNOT_BE_FOREIGN_KEY);
            return false;
        }
        return true;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
}
