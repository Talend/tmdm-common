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

import javax.xml.XMLConstants;

import org.talend.mdm.commmon.metadata.*;
import org.w3c.dom.Element;

/**
 * Check whether the type containing the <code>field</code> actually exists. The <code>field</code> may be a unresolved
 * field because its containing type does not exist, this rule ensures such problem is reported.
 */
class FieldTypeValidationRule implements ValidationRule {

    private final FieldMetadata field;

    FieldTypeValidationRule(FieldMetadata field) {
        this.field = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        TypeMetadata currentType = field.getType();
        if (currentType == null) {
            handler.error(field, "Type '" + field.getType().getName() + "' does not exist",
                    field.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                    field.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                    field.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER), ValidationError.TYPE_DOES_NOT_EXIST);
            return false;
        }
        if (!XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(currentType.getNamespace())) {
            while (ValidationFactory.getRule(currentType).perform(handler) && !currentType.getSuperTypes().isEmpty()) {
                TypeMetadata superType = currentType.getSuperTypes().iterator().next();
                if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(superType.getNamespace())
                        && (Types.ANY_TYPE.equals(superType.getName()) || Types.ANY_SIMPLE_TYPE.equals(superType.getName()))) {
                    break;
                }
                currentType = superType;
            }
        }
        return ValidationFactory.getRule(currentType).perform(handler);
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
}
