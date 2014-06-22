/*
 * Copyright (C) 2006-2014 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.validation;

import org.talend.mdm.commmon.metadata.*;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;

class LookupFieldsValidationRule implements ValidationRule {

    private final ComplexTypeMetadata type;

    LookupFieldsValidationRule(ComplexTypeMetadata type) {
        this.type = type;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        boolean success = true;
        for (FieldMetadata lookupField : type.getLookupFields()) {
            if(!ValidationFactory.getRule(lookupField).perform(handler)) {
                continue;
            }
            // Lookup field must be defined in the entity (can't reference other entity field).
            ComplexTypeMetadata containingType = lookupField.freeze().getContainingType().getEntity();
            if (!type.equals(containingType)) {
                handler.error(type,
                        "Lookup field info must refer a field of the same entity.",
                        lookupField.<Element>getData(MetadataRepository.XSD_DOM_ELEMENT),
                        lookupField.<Integer>getData(MetadataRepository.XSD_LINE_NUMBER),
                        lookupField.<Integer>getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.LOOKUP_FIELD_NOT_IN_ENTITY);
                success &= false;
                continue;
            }
            if (lookupField.isKey()) {
                handler.error(type,
                        "Lookup field cannot be in entity key.",
                        lookupField.<Element>getData(MetadataRepository.XSD_DOM_ELEMENT),
                        lookupField.<Integer>getData(MetadataRepository.XSD_LINE_NUMBER),
                        lookupField.<Integer>getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.LOOKUP_FIELD_CANNOT_BE_KEY);
                success &= false;
                continue;
            }
            // Order matters here: check if field is correct (exists) before checking isMany().
            lookupField.validate(handler);
            if (!isPrimitiveTypeField(lookupField)) {
                handler.error(type,
                        "Lookup field must be a simple typed element.",
                        lookupField.<Element>getData(MetadataRepository.XSD_DOM_ELEMENT),
                        lookupField.<Integer>getData(MetadataRepository.XSD_LINE_NUMBER),
                        lookupField.<Integer>getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.LOOKUP_FIELD_MUST_BE_SIMPLE_TYPE);
                success &= false;
            }
        }
        return success;
    }

    private static boolean isPrimitiveTypeField(FieldMetadata lookupField) {
        TypeMetadata currentType = lookupField.getType();
        if (!XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(currentType.getNamespace())) {
            while (!currentType.getSuperTypes().isEmpty()) {
                TypeMetadata superType = currentType.getSuperTypes().iterator().next();
                if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(superType.getNamespace())
                        && (Types.ANY_TYPE.equals(superType.getName())
                        || Types.ANY_SIMPLE_TYPE.equals(superType.getName()))) {
                    break;
                }
                currentType = superType;
            }
        }
        return XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(currentType.getNamespace());
    }

    @Override
    public boolean continueOnFail() {
        return true;
    }
}
