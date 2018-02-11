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

import java.util.Collection;

/**
 * This rule checks if a {@link org.talend.mdm.commmon.metadata.FieldMetadata field} is not cyclic structure and its
 * declaring type is inherited.
 */
public class CircleFieldInheritanceRule implements ValidationRule {

    private final FieldMetadata field;

    public CircleFieldInheritanceRule(FieldMetadata field) {
        this.field = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        ComplexTypeMetadata declaringType = (ComplexTypeMetadata) field.getDeclaringType();
        Collection<ComplexTypeMetadata> subTypes = declaringType.getSubTypes();
        if (subTypes.size() > 0 && MetadataRepository.isCircle(field.getContainingType(), null)) {
            handler.error(
                    field,
                    "Inheritance type with cyclic structure is not supported.", //$NON-NLS-1$
                    field.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                    field.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                    field.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                    ValidationError.INHERITANCE_TYPE_CANNOT_INCLUDE_CIRCLE_FIELD);
            return false;
        }
        return true;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
}
