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

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ValidationError;
import org.talend.mdm.commmon.metadata.ValidationHandler;
import org.w3c.dom.Element;


/**
 * Performs several checks on the Entity & ReusableType naming convention:
 * <ul>
 * <li>ComplexType name should not contain '-'.</li>
 * </ul>
 * TODO: implement generic database table & column naming convention rules
 */
class TypeNamingValidationRule implements ValidationRule {

    private final ComplexTypeMetadata type;

    TypeNamingValidationRule(ComplexTypeMetadata type) {
        this.type = type;
    }
    
    @Override
    public boolean perform(ValidationHandler handler) {
        String name = type.getName();
        if(name.contains("-")) { //$NON-NLS-1$
            handler.warning(type, "Type '" + type.getName() + "' should not contain '-'.",
                    type.<Element>getData(MetadataRepository.XSD_DOM_ELEMENT),
                    type.<Integer>getData(MetadataRepository.XSD_LINE_NUMBER),
                    type.<Integer>getData(MetadataRepository.XSD_COLUMN_NUMBER), ValidationError.TYPE_INVALID_NAME);
            return false;
        }
        return true;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
    

}
