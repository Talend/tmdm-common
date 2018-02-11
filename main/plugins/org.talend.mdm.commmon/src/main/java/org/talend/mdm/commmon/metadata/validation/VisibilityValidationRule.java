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

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.*;
import org.w3c.dom.Element;

/**
 * This rule checks whether a field is mandatory (field or all parents). If there's a visibility rule UI may
 */
public class VisibilityValidationRule implements ValidationRule {

    private final FieldMetadata field;

    public VisibilityValidationRule(FieldMetadata field) {
        this.field = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        if(StringUtils.isNotEmpty(field.getVisibilityRule()) && isMandatory(field)) {
            handler.warning(field, "Mandatory field may not visible during record edition due to visibility rule.",
                    field.<Element>getData(MetadataRepository.XSD_DOM_ELEMENT),
                    field.<Integer>getData(MetadataRepository.XSD_LINE_NUMBER),
                    field.<Integer>getData(MetadataRepository.XSD_COLUMN_NUMBER),
                    ValidationError.MANDATORY_FIELD_MAY_NOT_BE_VISIBLE);
        }
        return true;
    }

    private static boolean isMandatory(FieldMetadata field) {
        ComplexTypeMetadata entity = field.getContainingType().getEntity();
        ComplexTypeMetadata containingType = field.getContainingType();
        if (containingType.equals(entity)) {
            // Already at entity level, returns isMandatory() result.
            return field.isMandatory();
        }
        while(!containingType.equals(entity)) {
            FieldMetadata container = containingType.getContainer();
            if(!container.isMandatory()) {
                return false;
            }
            containingType = container.getContainingType();
        }
        return true;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
}
