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

/**
 * Reports errors for a unresolved element.
 */
class UnresolvedField implements ValidationRule {

    private final UnresolvedFieldMetadata field;

    UnresolvedField(UnresolvedFieldMetadata field) {
        this.field = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        LocationOverride override = new LocationOverride(field, handler,
                field.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                field.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                field.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER));
        if (ValidationFactory.getRule(field.getContainingType()).perform(override)) {
            handler.error(field, "Type '" + field.getContainingType().getName() + "' does not own field '" + field.getName()
                    + "'.", field.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                    field.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                    field.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER), ValidationError.TYPE_DOES_NOT_OWN_FIELD);
        }
        return false;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
}
