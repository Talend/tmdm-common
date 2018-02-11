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

import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.UnresolvedTypeMetadata;
import org.talend.mdm.commmon.metadata.ValidationError;
import org.talend.mdm.commmon.metadata.ValidationHandler;
import org.w3c.dom.Element;

/**
 * Reports errors for a unresolved type.
 */
class UnresolvedType implements ValidationRule {

    private final UnresolvedTypeMetadata type;

    UnresolvedType(UnresolvedTypeMetadata type) {
        this.type = type;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        handler.error(type, "Type '" + type.getTypeName() + "' does not exist",
                type.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                type.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                type.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER), ValidationError.TYPE_DOES_NOT_EXIST);
        return false;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
}
