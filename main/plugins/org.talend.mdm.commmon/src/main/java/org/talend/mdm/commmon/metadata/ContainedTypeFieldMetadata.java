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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.validation.CompositeValidationRule;
import org.talend.mdm.commmon.metadata.validation.ValidationFactory;
import org.talend.mdm.commmon.metadata.validation.ValidationRule;

/**
 *
 */
public class ContainedTypeFieldMetadata extends MetadataExtensions implements FieldMetadata {

    private final boolean isMany;

    private String name;

    private final List<String> allowWriteUsers;

    private final List<String> hideUsers;

    private final boolean isMandatory;

    private final Map<Locale, String> localeToLabel = new HashMap<Locale, String>();

    private final Map<Locale, String> localeToDescription = new HashMap<Locale, String>();

    private TypeMetadata declaringType;

    private ComplexTypeMetadata fieldType;

    private ComplexTypeMetadata containingType;

    private boolean isFrozen;

    private int cachedHashCode;

    private String visibilityRule;

    private boolean isReference;

    private boolean isFieldReferenceToEntity;// if is a field referenced to another entity

    public ContainedTypeFieldMetadata(ComplexTypeMetadata containingType,
            boolean isMany,
            boolean isMandatory,
            String name,
            ComplexTypeMetadata fieldType,
            List<String> allowWriteUsers,
            List<String> hideUsers,
            String visibilityRule) {
        this(containingType, isMany, isMandatory, name, fieldType, false, false, allowWriteUsers, hideUsers,
                visibilityRule);
    }

    public ContainedTypeFieldMetadata(ComplexTypeMetadata containingType,
            boolean isMany,
            boolean isMandatory,
            String name,
            ComplexTypeMetadata fieldType,
            boolean isReference,
            List<String> allowWriteUsers, List<String> hideUsers, String visibilityRule) {
        this(containingType, isMany, isMandatory, name, fieldType, isReference, false, allowWriteUsers, hideUsers,
                 visibilityRule);
    }

    public ContainedTypeFieldMetadata(ComplexTypeMetadata containingType, boolean isMany, boolean isMandatory, String name,
            ComplexTypeMetadata fieldType, boolean isReference,
            boolean isFieldReferenceToEntity, List<String> allowWriteUsers,
            List<String> hideUsers,
            String visibilityRule) {
        if (fieldType == null) {
            throw new IllegalArgumentException("Contained type cannot be null.");
        }
        this.visibilityRule = visibilityRule;
        this.isMandatory = isMandatory;
        this.fieldType = ContainedComplexTypeMetadata.contain(fieldType, this);
        this.isReference = isReference;
        this.isFieldReferenceToEntity = isFieldReferenceToEntity;
        this.containingType = containingType;
        this.declaringType = containingType;
        this.isMany = isMany;
        this.name = name;
        this.allowWriteUsers = allowWriteUsers;
        this.hideUsers = hideUsers;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isKey() {
        return false;
    }

    @Override
    public TypeMetadata getType() {
        return fieldType;
    }

    public void setFieldType(ComplexTypeMetadata fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public ComplexTypeMetadata getContainingType() {
        return containingType;
    }

    @Override
    public void setContainingType(ComplexTypeMetadata typeMetadata) {
        this.containingType = typeMetadata;
    }

    @Override
    public FieldMetadata freeze() {
        if (isFrozen) {
            return this;
        }
        isFrozen = true;
        fieldType = (ComplexTypeMetadata) fieldType.freeze();
        return this;
    }

    @Override
    public void promoteToKey() {
        throw new UnsupportedOperationException("Contained type field can't be promoted to key.");
    }

    @Override
    public void validate(ValidationHandler handler) {
        ValidationFactory.getRule(this).perform(handler);
    }

    @Override
    public ValidationRule createValidationRule() {
        List<ValidationRule> rules = new LinkedList<ValidationRule>();
        rules.add(ValidationFactory.getRule(this));
        Collection<FieldMetadata> fields = fieldType.getFields();
        for (FieldMetadata field : fields) {
            rules.add(ValidationFactory.getRule(field));
        }
        return new CompositeValidationRule(rules.toArray(new ValidationRule[rules.size()]));
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

    @Override
    public TypeMetadata getDeclaringType() {
        return declaringType;
    }

    @Override
    public void adopt(ComplexTypeMetadata metadata) {
        FieldMetadata copy = copy();
        copy.setContainingType(metadata);
        // TMDM-8166, when entity's type inherits another type, need to recursively adopt its contained type's fields
        if (metadata.getEntity().isInstantiable()) {
            ComplexTypeMetadata copyContainedType = (ComplexTypeMetadata) copy.getType().copy();
            FieldMetadata container = ((ContainedComplexTypeMetadata) copy.getType()).getContainer();
            ComplexTypeMetadata copyFiledType = ContainedComplexTypeMetadata.contain(copyContainedType, container);
            if (!MetadataRepository.isCircle(copyFiledType, null)) {
                for (FieldMetadata copyField : copyContainedType.getFields()) {
                    copyField.adopt(copyFiledType);
                }
            }
            ((ContainedTypeFieldMetadata) copy).setFieldType(copyFiledType);
        }
        metadata.addField(copy);
    }

    @Override
    public FieldMetadata copy() {
        ContainedTypeFieldMetadata copy;
        if (fieldType instanceof ContainedComplexTypeMetadata) {
            copy = new ContainedTypeFieldMetadata(containingType, isMany, isMandatory, name,
                    ((ContainedComplexTypeMetadata) fieldType).getContainedType(), isReference, isFieldReferenceToEntity,
                    allowWriteUsers, hideUsers, visibilityRule);
        } else {
            copy = new ContainedTypeFieldMetadata(containingType, isMany, isMandatory, name, fieldType, isReference,
                    isFieldReferenceToEntity, allowWriteUsers, hideUsers, visibilityRule);
        }
        copy.localeToLabel.putAll(localeToLabel);
        copy.localeToDescription.putAll(localeToDescription);
        if (dataMap != null) {
            copy.dataMap = new HashMap<String, Object>(dataMap);
        }
        copy.declaringType = this.declaringType;
        return copy;
    }

    @Override
    public List<String> getHideUsers() {
        return hideUsers;
    }

    @Override
    public List<String> getWriteUsers() {
        return allowWriteUsers;
    }

    @Override
    public boolean isMany() {
        return isMany;
    }

    @Override
    public boolean isMandatory() {
        return isMandatory;
    }

    public boolean isReference() {
        return this.isReference;
    }

    public boolean isFieldReferenceToEntity() {
        return this.isFieldReferenceToEntity;
    }

    @Override
    public <T> T accept(MetadataVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Contained {" +  //$NON-NLS-1$
                "declaringType=" + declaringType +   //$NON-NLS-1$
                ", containingType=" + containingType +   //$NON-NLS-1$
                ", name='" + name + '\'' +  //$NON-NLS-1$
                ", isMany=" + isMany +  //$NON-NLS-1$
                ", fieldTypeName='" + fieldType.getName() + '\'' + //$NON-NLS-1$
                '}';
    }

    public ComplexTypeMetadata getContainedType() {
        return fieldType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContainedTypeFieldMetadata)) {
            return false;
        }

        ContainedTypeFieldMetadata that = (ContainedTypeFieldMetadata) o;

        if (isMandatory != that.isMandatory) {
            return false;
        }
        if (isMany != that.isMany) {
            return false;
        }
        if (containingType != null ? !containingType.equals(that.containingType) : that.containingType != null) {
            return false;
        }
        if (declaringType != null ? !declaringType.equals(that.declaringType) : that.declaringType != null) {
            return false;
        }
        if (fieldType != null ? !fieldType.equals(that.fieldType) : that.fieldType != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (isFrozen && cachedHashCode != 0) {
            return cachedHashCode;
        }
        int result = (isMany ? 1 : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (declaringType != null ? declaringType.hashCode() : 0);
        result = 31 * result + (containingType != null ? containingType.hashCode() : 0);
        result = 31 * result + (fieldType != null ? fieldType.hashCode() : 0);
        result = 31 * result + (isMandatory ? 1 : 0);
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
