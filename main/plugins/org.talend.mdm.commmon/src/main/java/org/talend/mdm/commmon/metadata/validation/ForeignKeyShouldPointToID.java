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

import java.util.Iterator;

import org.talend.mdm.commmon.metadata.*;
import org.w3c.dom.Element;

/**
 * Ensure a foreign key points to a key field (can't point to another element in referenced type).
 */
class ForeignKeyShouldPointToID implements ValidationRule {

    private final ReferenceFieldMetadata field;

    ForeignKeyShouldPointToID(ReferenceFieldMetadata field) {
        this.field = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        // FK can not be non-PK check
        if (!field.getReferencedField().isKey()) {
            // Compute valid PK fields as help for user
            StringBuilder referencedTypePK = new StringBuilder();
            Iterator<FieldMetadata> keyFields = field.getReferencedType().getKeyFields().iterator();
            while (keyFields.hasNext()) {
                referencedTypePK.append('\'').append(keyFields.next().getName()).append('\'');
                if (keyFields.hasNext()) {
                    referencedTypePK.append(' ');
                }
            }
            // Reports error
            handler.warning(field, "Foreign key should point to a primary key (recommended choices are: " + referencedTypePK
                    + ")", field.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                    field.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                    field.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                    ValidationError.FOREIGN_KEY_SHOULD_POINT_TO_PRIMARY_KEY);
        }
        return true;
    }

    @Override
    public boolean continueOnFail() {
        return true;
    }
}
