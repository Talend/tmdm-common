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
 * This rules warns user when a reusable type is never used.
 */
class UnusedReusableTypeValidationRule implements ValidationRule {

    private final ComplexTypeMetadata type;

    UnusedReusableTypeValidationRule(ComplexTypeMetadata type) {
        this.type = type;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        if (MetadataUtils.countEntityUsageCount(type) == 0) {
            // TMDM-7865: Don't report not used type if type has sub types (check on sub types would be enough)
            if (!type.getSubTypes().isEmpty()) {
                return true;
            }
            //TMDM-8997: Don't report anonymous type (it always has parent and its parent will be validated before it, check on parent would be enough)
            if(MetadataUtils.isAnonymousType(type)) {
                return true;
            }
            // TMDM-7672: Sub type might not be used, but super type is
            ComplexTypeMetadata topLevelType = (ComplexTypeMetadata) MetadataUtils.getSuperConcreteType(type);
            if (MetadataUtils.countEntityUsageCount(topLevelType) == 0) {
                // Type isn't directly used nor is its super type, reports a warning
                handler.warning(type, "Type '" + type.getName() + "' is never used in an entity type.",
                        type.<Element>getData(MetadataRepository.XSD_DOM_ELEMENT),
                        type.<Integer>getData(MetadataRepository.XSD_LINE_NUMBER),
                        type.<Integer>getData(MetadataRepository.XSD_COLUMN_NUMBER), ValidationError.UNUSED_REUSABLE_TYPE);
                return false;
            }
            return true;
        }
        return true;
    }

    @Override
    public boolean continueOnFail() {
        return true;
    }
}
