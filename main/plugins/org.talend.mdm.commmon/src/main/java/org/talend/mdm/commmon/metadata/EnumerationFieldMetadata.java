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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.validation.ValidationFactory;
import org.talend.mdm.commmon.metadata.validation.ValidationRule;

/**
 *
 */
public class EnumerationFieldMetadata extends MetadataExtensions implements FieldMetadata {

    private boolean isKey;

    private TypeMetadata fieldType;

    private final List<String> allowWriteUsers;

    private final List<String> hideUsers;

    private final TypeMetadata declaringType;

    private final boolean isMany;

    private final boolean isMandatory;

    private final Map<Locale, String> localeToLabel = new HashMap<Locale, String>();

    private final Map<Locale, String> localeToDescription = new HashMap<Locale, String>();

    private ComplexTypeMetadata containingType;

    private final String name;

    private boolean isFrozen;

    private int cachedHashCode;

    private String visibilityRule;

    public EnumerationFieldMetadata(ComplexTypeMetadata containingType,
                                    boolean isKey,
                                    boolean isMany, boolean isMandatory, String name,
                                    TypeMetadata fieldType,
                                    List<String> allowWriteUsers,
                                    List<String> hideUsers,
                                    String visibilityRule) {
        this.containingType = containingType;
        this.declaringType = containingType;
        this.isKey = isKey;
        this.isMany = isMany;
        this.isMandatory = isMandatory;
        this.name = name;
        this.fieldType = fieldType;
        this.allowWriteUsers = allowWriteUsers;
        this.hideUsers = hideUsers;
        this.visibilityRule = visibilityRule;
    }

    public String getName() {
        return name;
    }

    public boolean isKey() {
        return isKey;
    }

    public TypeMetadata getType() {
        return fieldType;
    }

    public ComplexTypeMetadata getContainingType() {
        return containingType;
    }

    public void setContainingType(ComplexTypeMetadata typeMetadata) {
        this.containingType = typeMetadata;
    }

    public FieldMetadata freeze() {
        if (isFrozen) {
            return this;
        }
        isFrozen = true;
        fieldType = fieldType.freeze();
        return this;
    }

    public void promoteToKey() {
        isKey = true;
    }

    @Override
    public void validate(ValidationHandler handler) {
        ValidationFactory.getRule(this).perform(handler);
    }

    @Override
    public ValidationRule createValidationRule() {
        return ValidationFactory.getRule(this);
    }

    @Override
    public String getPath() {
        FieldMetadata containingField = containingType.getContainer();
        if (containingField != null) {
            return containingField.getPath() + '/' + name;
        } else {
            return name;
        }
    }

    @Override
    public String getEntityTypeName() {
        return containingType.getEntity().getName();
    }

    @Override
    public void registerName(Locale locale, String name) {
        localeToLabel.put(locale, name);
    }

    @Override
    public String getName(Locale locale) {
        String localizedName = localeToLabel.get(locale);
        if (localizedName == null) {
            return getName();
        }
        return localizedName;
    }

    @Override
    public String getVisibilityRule() {
        return visibilityRule;
    }

    public TypeMetadata getDeclaringType() {
        return declaringType;
    }

    public void adopt(ComplexTypeMetadata metadata) {
        FieldMetadata copy = copy();
        copy.setContainingType(metadata);
    }

    public FieldMetadata copy() {
        EnumerationFieldMetadata copy = new EnumerationFieldMetadata(containingType,
                isKey(),
                isMany,
                isMandatory,
                name,
                fieldType,
                allowWriteUsers,
                hideUsers,
                visibilityRule);
        copy.localeToLabel.putAll(localeToLabel);
        copy.localeToDescription.putAll(localeToDescription);
        if (dataMap != null) {
            copy.dataMap = new HashMap<String, Object>(dataMap);
        }
        return copy;
    }

    public List<String> getHideUsers() {
        return hideUsers;
    }

    public List<String> getWriteUsers() {
        return allowWriteUsers;
    }

    public boolean isMany() {
        return isMany;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public <T> T accept(MetadataVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "EnumerationFieldMetadata{" +  //$NON-NLS-1$
                "declaringType=" + declaringType + //$NON-NLS-1$
                ", containingType=" + containingType + //$NON-NLS-1$
                ", is key=" + isKey + //$NON-NLS-1$
                ", name ='" + name + '\'' + //$NON-NLS-1$
                ", type name ='" + fieldType.getName() + '\'' +  //$NON-NLS-1$
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EnumerationFieldMetadata)) {
            return false;
        }

        EnumerationFieldMetadata that = (EnumerationFieldMetadata) o;

        if (isKey != that.isKey) return false;
        if (isMandatory != that.isMandatory) return false;
        if (isMany != that.isMany) return false;
        if (containingType != null ? !containingType.equals(that.containingType) : that.containingType != null)
            return false;
        if (declaringType != null ? !declaringType.equals(that.declaringType) : that.declaringType != null)
            return false;
        if (fieldType != null ? !fieldType.equals(that.fieldType) : that.fieldType != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        if (isFrozen && cachedHashCode != 0) {
            return cachedHashCode;
        }
        int result = (isKey ? 1 : 0);
        result = 31 * result + (fieldType != null ? fieldType.hashCode() : 0);
        result = 31 * result + (declaringType != null ? declaringType.hashCode() : 0);
        result = 31 * result + (isMany ? 1 : 0);
        result = 31 * result + (isMandatory ? 1 : 0);
        result = 31 * result + (containingType != null ? containingType.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        cachedHashCode = result;
        return result;
    }

    @Override
    public void registerDescription(Locale locale, String description) {
        localeToDescription.put(locale, description);
    }

    @Override
    public String getDescription(Locale locale) {
        String localizedDescription = localeToDescription.get(locale);
        if (localizedDescription == null) {
            return StringUtils.EMPTY;
        }
        return localizedDescription;
    }

    @Override
    public List<String> getNoAddRoles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getNoRemoveRoles() {
        throw new UnsupportedOperationException();
    }
}
