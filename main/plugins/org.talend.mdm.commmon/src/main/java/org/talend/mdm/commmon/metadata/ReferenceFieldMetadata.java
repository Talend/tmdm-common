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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.validation.ValidationFactory;
import org.talend.mdm.commmon.metadata.validation.ValidationRule;
import org.w3c.dom.Element;

public class ReferenceFieldMetadata extends MetadataExtensions implements FieldMetadata {

    private final boolean isMany;

    private final boolean allowFKIntegrityOverride;

    private final boolean isFKIntegrity;

    private final List<String> hideUsers;

    private final List<String> writeUsers;

    private final String foreignKeyFilter;

    private final boolean isMandatory;

    private final String name;

    private TypeMetadata declaringType;

    private TypeMetadata fieldType;

    private boolean isKey;

    private FieldMetadata referencedField;

    private List<FieldMetadata> foreignKeyInfoFields = Collections.emptyList();

    private String foreignKeyInfoFormat;

    private final Map<Locale, String> localeToLabel = new HashMap<Locale, String>();

    private final Map<Locale, String> localeToDescription = new HashMap<Locale, String>();

    private ComplexTypeMetadata referencedType;

    private ComplexTypeMetadata containingType;

    private boolean isFrozen;

    private int cachedHashCode;

    private String visibilityRule;
    
    private final List<String> noAddRoles;

    private final List<String> noRemoveRoles;

    private boolean isFKMainRender;

    public ReferenceFieldMetadata(ComplexTypeMetadata containingType,
                                  boolean isKey,
                                  boolean isMany,
                                  boolean isMandatory,
                                  String name,
                                  ComplexTypeMetadata referencedType,
                                  FieldMetadata referencedField,
                                  List<FieldMetadata> foreignKeyInfo,
                                  String foreignKeyInfoFormat,
                                  boolean fkIntegrity,
                                  boolean allowFKIntegrityOverride,
                                  TypeMetadata fieldType,
                                  List<String> allowWriteUsers,
                                  List<String> hideUsers,
                                  String foreignKeyFilter,
                                  String visibilityRule) {
        this(containingType,
                isKey,
                isMany,
                isMandatory,
                name,
                referencedType,
                referencedField,
                foreignKeyInfo,
                foreignKeyInfoFormat,
                fkIntegrity,
                allowFKIntegrityOverride,
                fieldType,
                allowWriteUsers,
                hideUsers,
                foreignKeyFilter,
                visibilityRule,
                Collections.<String>emptyList(),
                Collections.<String>emptyList());
    }

    public ReferenceFieldMetadata(ComplexTypeMetadata containingType,
                                  boolean isKey,
                                  boolean isMany,
                                  boolean isMandatory,
                                  String name,
                                  ComplexTypeMetadata referencedType,
                                  FieldMetadata referencedField,
                                  List<FieldMetadata> foreignKeyInfo,
                                  String foreignKeyInfoFormat,
                                  boolean fkIntegrity,
                                  boolean allowFKIntegrityOverride,
                                  TypeMetadata fieldType,
                                  List<String> allowWriteUsers,
                                  List<String> hideUsers,
                                  String foreignKeyFilter,
                                  String visibilityRule,
                                  List<String> noAddRoles,
                                  List<String> noRemoveRoles) {
        this(containingType,
                isKey,
                isMany,
                isMandatory,
                name,
                referencedType,
                referencedField,
                foreignKeyInfo,
                foreignKeyInfoFormat,
                fkIntegrity,
                allowFKIntegrityOverride,
                fieldType,
                allowWriteUsers,
                hideUsers,
                foreignKeyFilter,
                visibilityRule,
                noAddRoles,
                noRemoveRoles,
                false);
    }

    public ReferenceFieldMetadata(ComplexTypeMetadata containingType,
                                  boolean isKey,
                                  boolean isMany,
                                  boolean isMandatory,
                                  String name,
                                  ComplexTypeMetadata referencedType,
                                  FieldMetadata referencedField,
                                  List<FieldMetadata> foreignKeyInfo,
                                  String foreignKeyInfoFormat,
                                  boolean fkIntegrity,
                                  boolean allowFKIntegrityOverride,
                                  TypeMetadata fieldType,
                                  List<String> allowWriteUsers,
                                  List<String> hideUsers,
                                  String foreignKeyFilter,
                                  String visibilityRule,
                                  List<String> noAddRoles,
                                  List<String> noRemoveRoles,
                                  boolean isFKMainRender) {
        this.isMandatory = isMandatory;
        this.name = name;
        this.referencedField = referencedField;
        this.foreignKeyInfoFields = foreignKeyInfo;
        this.foreignKeyInfoFormat = foreignKeyInfoFormat;
        this.containingType = containingType;
        this.declaringType = containingType;
        this.allowFKIntegrityOverride = allowFKIntegrityOverride;
        this.isFKIntegrity = fkIntegrity;
        this.referencedType = referencedType;
        this.isKey = isKey;
        this.isMany = isMany;
        this.referencedType = referencedType;
        this.fieldType = fieldType;
        this.writeUsers = allowWriteUsers;
        this.hideUsers = hideUsers;
        this.foreignKeyFilter = foreignKeyFilter;
        this.visibilityRule = visibilityRule;
        this.noAddRoles = noAddRoles;
        this.noRemoveRoles = noRemoveRoles;
        this.isFKMainRender = isFKMainRender;
    }

    public String getForeignKeyFilter() {
        return foreignKeyFilter;
    }

    public FieldMetadata getReferencedField() {
        return referencedField;
    }

    public void setReferencedField(FieldMetadata referencedField) {
        this.referencedField = referencedField;
    }

    public ComplexTypeMetadata getReferencedType() {
        return referencedType;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean hasForeignKeyInfo() {
        return !foreignKeyInfoFields.isEmpty();
    }

    public String getForeignKeyInfoFormat() {
        return this.foreignKeyInfoFormat;
    }

    public List<FieldMetadata> getForeignKeyInfoFields() {
        return foreignKeyInfoFields;
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
        fieldType = fieldType.freeze();
        if (!foreignKeyInfoFields.isEmpty()) {
            List<FieldMetadata> frozenFKInfo = new ArrayList<FieldMetadata>(foreignKeyInfoFields.size());
            for (FieldMetadata fieldMetadata : foreignKeyInfoFields) {
                FieldMetadata freeze = fieldMetadata.freeze();
                if (freeze != null) {
                    frozenFKInfo.add(freeze);
                }
            }
            foreignKeyInfoFields = frozenFKInfo;
        }
        referencedType = (ComplexTypeMetadata) referencedType.freeze();
        referencedField = referencedField.freeze();
        return this;
    }

    @Override
    public void promoteToKey() {
        isKey = true;
    }

    @Override
    public void validate(ValidationHandler handler) {
        // When referenced field has errors, client expects the reference field as error iso. the referenced field.
        Element element = this.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT);
        Integer lineNumber = this.<Integer>getData(MetadataRepository.XSD_LINE_NUMBER);
        Integer columnNumber = this.<Integer>getData(MetadataRepository.XSD_COLUMN_NUMBER);
        LocationOverride override = new LocationOverride(this, handler, element, lineNumber, columnNumber);
        ValidationFactory.getRule(referencedField).perform(override);
        // Validates this side of the relationship
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

    @Override
    public TypeMetadata getDeclaringType() {
        return declaringType;
    }

    public void setDeclaringType(TypeMetadata declaringType) {
        this.declaringType = declaringType;
    }

    public boolean isFKIntegrity() {
        return isFKIntegrity;
    }

    public boolean allowFKIntegrityOverride() {
        return allowFKIntegrityOverride;
    }

    @Override
    public void adopt(ComplexTypeMetadata metadata) {
        FieldMetadata copy = copy();
        copy.setContainingType(metadata);
        metadata.addField(copy);
    }

    @Override
    public TypeMetadata getType() {
        return fieldType;
    }

    @Override
    public boolean isKey() {
        return isKey;
    }

    @Override
    public <T> T accept(MetadataVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public FieldMetadata copy() {
        FieldMetadata referencedFieldCopy = referencedField.copy();
        List<FieldMetadata> foreignKeyInfoCopy;
        if (hasForeignKeyInfo()) {
            foreignKeyInfoCopy = new ArrayList<FieldMetadata>(foreignKeyInfoFields.size());
            for (FieldMetadata foreignKeyInfoField : foreignKeyInfoFields) {
                foreignKeyInfoCopy.add(foreignKeyInfoField.copy());
            }
        } else {
            foreignKeyInfoCopy = Collections.emptyList();
        }
        ReferenceFieldMetadata copy = new ReferenceFieldMetadata(containingType,
                isKey,
                isMany,
                isMandatory,
                name,
                referencedType,
                referencedFieldCopy,
                foreignKeyInfoCopy,
                foreignKeyInfoFormat,
                isFKIntegrity,
                allowFKIntegrityOverride,
                fieldType,
                writeUsers,
                hideUsers, 
                foreignKeyFilter,
                visibilityRule,
                noAddRoles,
                noRemoveRoles,
                isFKMainRender);
        copy.localeToLabel.putAll(localeToLabel);
        copy.localeToDescription.putAll(localeToDescription);
        if (dataMap != null) {
            copy.dataMap = new HashMap<String, Object>(dataMap);
        }
        return copy;
    }

    @Override
    public String toString() {
        return "Reference {" + //$NON-NLS-1$
                "containing type= " + containingType + //$NON-NLS-1$
                ", declaring type=" + declaringType + //$NON-NLS-1$
                ", name='" + name + '\'' + //$NON-NLS-1$
                ", isKey=" + isKey + //$NON-NLS-1$
                ", is many=" + isMany + //$NON-NLS-1$
                ", referenced type= " + referencedType + //$NON-NLS-1$
                ", referenced field= " + referencedField + //$NON-NLS-1$
                ", foreign key info='" + foreignKeyInfoFields + '\'' + //$NON-NLS-1$
                ", foreign key info format= " + foreignKeyInfoFormat + //$NON-NLS-1$
                ", allow FK integrity override= " + allowFKIntegrityOverride + //$NON-NLS-1$
                ", check FK integrity= " + isFKIntegrity + //$NON-NLS-1$
                '}';
    }

    @Override
    public boolean isMany() {
        return isMany;
    }

    @Override
    public boolean isMandatory() {
        return isMandatory;
    }

    @Override
    public List<String> getHideUsers() {
        return hideUsers;
    }

    @Override
    public List<String> getWriteUsers() {
        return writeUsers;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReferenceFieldMetadata)) {
            return false;
        }

        ReferenceFieldMetadata that = (ReferenceFieldMetadata) o;

        if (allowFKIntegrityOverride != that.allowFKIntegrityOverride) {
            return false;
        }
        if (isFKIntegrity != that.isFKIntegrity) {
            return false;
        }
        if (isKey != that.isKey) {
            return false;
        }
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
        if (foreignKeyInfoFields != null ? !foreignKeyInfoFields.equals(that.foreignKeyInfoFields) : that.foreignKeyInfoFields != null) {
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
        int result = (isKey ? 1 : 0);
        result = 31 * result + (isMany ? 1 : 0);
        result = 31 * result + (foreignKeyInfoFields != null ? foreignKeyInfoFields.hashCode() : 0);
        result = 31 * result + (containingType != null ? containingType.hashCode() : 0);
        result = 31 * result + (declaringType != null ? declaringType.hashCode() : 0);
        result = 31 * result + (allowFKIntegrityOverride ? 1 : 0);
        result = 31 * result + (isFKIntegrity ? 1 : 0);
        result = 31 * result + (isMandatory ? 1 : 0);
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
        return noAddRoles;
    }

    @Override
    public List<String> getNoRemoveRoles() {
        return noRemoveRoles;
    }

    public boolean isFKMainRender() {
        return isFKMainRender;
    }
}
