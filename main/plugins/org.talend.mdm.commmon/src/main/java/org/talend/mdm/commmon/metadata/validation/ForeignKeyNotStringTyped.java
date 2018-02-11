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

import org.talend.mdm.commmon.metadata.*;
import org.w3c.dom.Element;

class ForeignKeyNotStringTyped implements ValidationRule {

    private final ReferenceFieldMetadata field;

    ForeignKeyNotStringTyped(ReferenceFieldMetadata field) {
        this.field = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        TypeMetadata type = MetadataUtils.getSuperConcreteType(field.getType());
        if (!Types.STRING.equals(type.getName())) {
            handler.error(field, "FK field '" + field.getName()
                    + "' is invalid because it isn't typed as string (nor a string restriction).",
                    field.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                    field.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                    field.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER), ValidationError.FOREIGN_KEY_NOT_STRING_TYPED);
            return false;
        }
        return true;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
}
