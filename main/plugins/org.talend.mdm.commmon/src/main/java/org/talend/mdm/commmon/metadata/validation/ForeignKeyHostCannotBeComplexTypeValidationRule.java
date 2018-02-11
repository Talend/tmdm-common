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
package org.talend.mdm.commmon.metadata.validation;

import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ValidationError;
import org.talend.mdm.commmon.metadata.ValidationHandler;
import org.w3c.dom.Element;


/**
 * @author sbliu
 *
 */
public class ForeignKeyHostCannotBeComplexTypeValidationRule implements ValidationRule {

    private final ContainedTypeFieldMetadata field;

    public ForeignKeyHostCannotBeComplexTypeValidationRule(ContainedTypeFieldMetadata field) {
        this.field = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        if (field.isReference()) {
            String name = field.getName();
            handler.error(field, "Foreign key cannot set on complex type field " + name + ".",
                    field.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                    field.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                    field.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER), ValidationError.FOREIGN_KEY_HOST_INVALID);
            return false;
        }
        return true;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }

}
