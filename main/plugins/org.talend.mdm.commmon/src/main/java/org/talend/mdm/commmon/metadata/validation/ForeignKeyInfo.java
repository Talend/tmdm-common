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

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.*;
import org.w3c.dom.Element;

/**
 * Performs several checks on the foreign key info:
 * <ul>
 * <li>Foreign key info is typed with a primitive XSD type.</li>
 * <li>Foreign key info is not a repeatable element.</li>
 * <li>Foreign key info references an element in referenced type.</li>
 * </ul>
 */
class ForeignKeyInfo implements ValidationRule {

    private final ReferenceFieldMetadata field;

    public ForeignKeyInfo(ReferenceFieldMetadata field) {
        this.field = field;
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        // Foreign key info checks
        for (FieldMetadata foreignKeyInfo : field.getForeignKeyInfoFields()) {
            if(StringUtils.isNotEmpty(field.getForeignKeyFilter())) {
                boolean isValidForeignKeyInfo = ValidationFactory.getRule(foreignKeyInfo).perform(NoOpValidationHandler.INSTANCE);
                if(!isValidForeignKeyInfo) {
                    handler.warning(foreignKeyInfo, "Foreign key info is invalid (but foreign key filter may make it valid).",
                            foreignKeyInfo.<Element>getData(MetadataRepository.XSD_DOM_ELEMENT),
                            foreignKeyInfo.<Integer>getData(MetadataRepository.XSD_LINE_NUMBER),
                            foreignKeyInfo.<Integer>getData(MetadataRepository.XSD_COLUMN_NUMBER),
                            ValidationError.FOREIGN_KEY_INFO_INVALID);
                }
            } else if (!ValidationFactory.getRule(foreignKeyInfo).perform(handler)) {
                continue;
            }
            if (!MetadataUtils.isPrimitiveTypeField(foreignKeyInfo)) {
                handler.warning(foreignKeyInfo, "Foreign key info is not typed as primitive XSD.",
                        foreignKeyInfo.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                        foreignKeyInfo.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                        foreignKeyInfo.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.FOREIGN_KEY_INFO_NOT_PRIMITIVE_XSD_TYPED);
            }
            if (foreignKeyInfo.isMany()) {
                handler.warning(foreignKeyInfo, "Foreign key info should not be a repeatable element.",
                        foreignKeyInfo.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                        foreignKeyInfo.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                        foreignKeyInfo.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.FOREIGN_KEY_INFO_REPEATABLE);
            }
            if (foreignKeyInfo.getContainingType() != null) {
                ComplexTypeMetadata foreignKeyInfoContainingType = foreignKeyInfo.getContainingType().getEntity();
                if (foreignKeyInfoContainingType.isInstantiable()
                        && !foreignKeyInfoContainingType.equals(field.getReferencedType())) {
                    handler.error(foreignKeyInfo, "Foreign key info must reference an element in referenced type.",
                            foreignKeyInfo.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT),
                            foreignKeyInfo.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER),
                            foreignKeyInfo.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER),
                            ValidationError.FOREIGN_KEY_INFO_NOT_REFERENCING_FK_TYPE);
                }
            }
        }
        return true;
    }

    @Override
    public boolean continueOnFail() {
        return false;
    }
}
