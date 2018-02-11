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

import java.util.List;

import org.talend.mdm.commmon.metadata.*;
import org.w3c.dom.Element;

/**
 * Performs checks on the type primary key info:
 * <ul>
 * <li>Primary key info refers to a field of the same entity.</li>
 * <li>Primary key info element is not a repeatable element.</li>
 * <li>Primary key info is a field with a primitive XSD type.</li>
 * </ul>
 */
class PrimaryKeyInfoValidationRule implements ValidationRule {

    private final ComplexTypeMetadata type;

    PrimaryKeyInfoValidationRule(ComplexTypeMetadata type) {
        this.type = type;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        boolean success = true;
        List<FieldMetadata> primaryKeyInfo = type.getPrimaryKeyInfo();
        TypeMetadata superType = MetadataUtils.getSuperConcreteType(type);
        for (FieldMetadata pkInfo : primaryKeyInfo) {
            // PK Info must be defined in the entity (can't reference other entity field).
            TypeMetadata pkInfoEntity = MetadataUtils.getSuperConcreteType(pkInfo.getContainingType().getEntity());
            if (!superType.equals(pkInfoEntity)) {
                handler.error(type, "Primary key info must refer a field of the same entity.",
                        pkInfo.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                        pkInfo.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                        pkInfo.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.PRIMARY_KEY_INFO_NOT_IN_ENTITY);
                success &= false;
                continue;
            }
            // Order matters here: check if field is correct (exists) before checking isMany().
            ValidationFactory.getRule(pkInfo).perform(handler);
            // No need to check isMany() if field definition is already wrong.
            if (pkInfo.isMany()) {
                handler.error(type, "Primary key info element cannot be a repeatable element.",
                        pkInfo.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                        pkInfo.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                        pkInfo.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.PRIMARY_KEY_INFO_CANNOT_BE_REPEATABLE);
                success &= false;
                continue;
            }
            if (!MetadataUtils.isPrimitiveTypeField(pkInfo)) {
                handler.warning(type, "Primary key info should refer to a field with a primitive XSD type.",
                        pkInfo.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                        pkInfo.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                        pkInfo.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.PRIMARY_KEY_INFO_TYPE_NOT_PRIMITIVE);
            }
        }
        return success;
    }

    @Override
    public boolean continueOnFail() {
        return true;
    }
}
