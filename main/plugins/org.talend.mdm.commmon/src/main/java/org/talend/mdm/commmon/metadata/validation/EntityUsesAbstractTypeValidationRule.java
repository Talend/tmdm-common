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
 * This rule return error when entity uses an abstract type.
 */
class EntityUsesAbstractTypeValidationRule implements ValidationRule {

    private final ComplexTypeMetadata type;

    EntityUsesAbstractTypeValidationRule(ComplexTypeMetadata type) {
        this.type = type;
    }

	@Override
	public boolean perform(ValidationHandler handler) {
		if (((ComplexTypeMetadataImpl)type).isAbstract()) {
			handler.error( type, "Entity '" + type.getName() + "' is using an abstract reusable type.",
					type.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
					type.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
					type.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
					ValidationError.ENTITY_CANNOT_USE_ABSTRACT_REUSABLE_TYPE);
			return false;
		}
		return true;
	}

	@Override
	public boolean continueOnFail() {
		return false;
	}
}
