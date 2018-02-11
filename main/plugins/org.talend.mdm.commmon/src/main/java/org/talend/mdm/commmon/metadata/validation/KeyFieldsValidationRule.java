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

class KeyFieldsValidationRule implements ValidationRule {

    private final ComplexTypeMetadata type;

    KeyFieldsValidationRule(ComplexTypeMetadata type) {
        this.type = type;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        boolean succeeded = true;
        for (FieldMetadata keyField : type.getKeyFields()) {
            if (!ValidationFactory.getRule(keyField).perform(handler)) {
                continue;
            }
            if (!keyField.isMandatory()) {
                handler.error(keyField,
                        "Key field must be mandatory (field '" + keyField.getName() + "' in type '" + type.getName()
                                + "' is optional)", keyField.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                        keyField.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                        keyField.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.FIELD_KEY_MUST_BE_MANDATORY);
            }
            if (keyField.isMany()) {
                handler.error(keyField, "Key field cannot be a repeatable element.",
                        keyField.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                        keyField.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                        keyField.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.FIELD_KEY_CANNOT_BE_REPEATABLE);
                succeeded &= false;
            }
            if (!keyField.isMandatory()) {
                handler.error(keyField, "Key field must be a mandatory element.",
                        keyField.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                        keyField.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                        keyField.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.FIELD_KEY_MUST_BE_MANDATORY);
                succeeded &= false;
            }
        }
        return succeeded;
    }

    @Override
    public boolean continueOnFail() {
        return true;
    }
}
