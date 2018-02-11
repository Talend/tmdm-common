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
 * Checks whether a foreign key uses a content length restriction. This is not an error, just a warning, but XML
 * documents contains '[' and ']' around key values, thus max length should take this into account (but data model
 * designers tend to forget this).
 */
class ForeignKeyMaxLength implements ValidationRule {

    private final ReferenceFieldMetadata field;

    ForeignKeyMaxLength(ReferenceFieldMetadata field) {
        this.field = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        if (field.getType().getData(MetadataRepository.DATA_MAX_LENGTH) != null) {
            handler.warning(field, "FK field '" + field.getName()
                    + "' uses max length restriction. Make sure to include square brackets in max length value.",
                    field.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                    field.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                    field.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER), ValidationError.FOREIGN_KEY_USES_MAX_LENGTH);
            return false;
        }
        return true;
    }

    @Override
    public boolean continueOnFail() {
        return true;
    }
}
