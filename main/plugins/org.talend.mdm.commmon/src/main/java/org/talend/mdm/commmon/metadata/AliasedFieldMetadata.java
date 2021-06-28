/*
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */
package org.talend.mdm.commmon.metadata;

import org.talend.mdm.commmon.metadata.validation.ValidationFactory;

import java.util.List;

public class AliasedFieldMetadata extends SimpleTypeFieldMetadata {

    private final String realFieldName;
    private final FieldMetadata aliasedField;

    public AliasedFieldMetadata(ComplexTypeMetadata containingType,
                                boolean isKey,
                                boolean isMany,
                                boolean isMandatory,
                                String name,
                                TypeMetadata fieldType,
                                List<String> allowWriteUsers,
                                List<String> hideUsers,
                                FieldMetadata aliasedField) {
        super(containingType,
                isKey,
                isMany,
                isMandatory,
                name,
                fieldType,
                allowWriteUsers,
                hideUsers,
                aliasedField.getVisibilityRule());
        this.realFieldName = aliasedField.getName();
        this.aliasedField = aliasedField;
    }

    public FieldMetadata getAliasedField() {
        return this.aliasedField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AliasedFieldMetadata)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AliasedFieldMetadata that = (AliasedFieldMetadata) o;
        return !(realFieldName != null ? !realFieldName.equals(that.realFieldName) : that.realFieldName != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (realFieldName != null ? realFieldName.hashCode() : 0);
        return result;
    }

    @Override
    public void validate(ValidationHandler handler) {
        ValidationFactory.getRule(this).perform(handler);
    }
}
