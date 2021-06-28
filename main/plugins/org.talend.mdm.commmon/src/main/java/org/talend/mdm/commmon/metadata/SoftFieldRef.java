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
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.validation.ValidationFactory;
import org.talend.mdm.commmon.metadata.validation.ValidationRule;
import org.w3c.dom.Element;

/**
 *
 */
public class SoftFieldRef implements FieldMetadata {

    private final MetadataRepository repository;

    private final String containingTypeName;

    private final String fieldName;

    private final Map<String, Object> additionalData = new HashMap<String, Object>();

    private final Map<Locale, String> localeToLabel = new HashMap<Locale, String>();

    private final Map<Locale, String> localeToDescription = new HashMap<Locale, String>();

    public SoftFieldRef(MetadataRepository metadataRepository, String fieldName, String containingTypeName) {
        this.repository = metadataRepository;
        this.containingTypeName = containingTypeName;
        this.fieldName = fieldName;
    }

    private FieldMetadata getField() {
        return freeze();
    }

    @Override
    public synchronized void setData(String key, Object data) {
        additionalData.put(key, data);
    }

    @Override
    public <X> X getData(String key) {
        return (X) additionalData.get(key);
    }

    @Override
    public String getName() {
        return fieldName;
    }

    @Override
    public boolean isKey() {
        return getField().isKey();
    }

    @Override
    public TypeMetadata getType() {
        return getField().getType();
    }

    @Override
    public ComplexTypeMetadata getContainingType() {
        return new SoftTypeRef(repository, repository.getUserNamespace(), containingTypeName, true);
    }

    @Override
    public void setContainingType(ComplexTypeMetadata typeMetadata) {
        getField().setContainingType(typeMetadata);
    }

    @Override
    public FieldMetadata freeze() {
        ComplexTypeMetadata type = repository.getComplexType(containingTypeName);
        FieldMetadata frozenField;
        if (type == null) {
            frozenField = new UnresolvedFieldMetadata(fieldName, false, new UnresolvedTypeMetadata(containingTypeName));
        } else if (!type.hasField(fieldName)) {
            frozenField = new UnresolvedFieldMetadata(fieldName, false, type);
        } else {
            frozenField = type.getField(fieldName).freeze().copy();
        }
        // Add additional data (line number...).
        Set<Map.Entry<String,Object>> data = additionalData.entrySet();
        for (Map.Entry<String, Object> currentData : data) {
            frozenField.setData(currentData.getKey(), currentData.getValue());
        }
        for (Map.Entry<Locale, String> entry : localeToLabel.entrySet()) {
            frozenField.registerName(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Locale, String> entry : localeToDescription.entrySet()) {
            frozenField.registerDescription(entry.getKey(), entry.getValue());
        }
        return frozenField;
    }

    @Override
    public void promoteToKey() {
        getField().promoteToKey();
    }

    @Override
    public void validate(ValidationHandler handler) {
        // Get line and column numbers
        Integer lineNumberObject = (Integer) additionalData.get(MetadataRepository.XSD_LINE_NUMBER);
        Integer columnNumberObject = (Integer) additionalData.get(MetadataRepository.XSD_COLUMN_NUMBER);
        Element xmlElement = (Element) additionalData.get(MetadataRepository.XSD_DOM_ELEMENT);
        if (columnNumberObject == null) {
            columnNumberObject = -1;
        }
        if (lineNumberObject == null) {
            lineNumberObject = -1;
        }
        // References should no longer exist (all should be frozen at this point).
        handler.error(this, "Field reference should no longer exist in model", xmlElement, lineNumberObject, columnNumberObject,
                ValidationError.UNCAUGHT_ERROR);
    }

    @Override
    public ValidationRule createValidationRule() {
        return ValidationFactory.getRule(this);
    }

    @Override
    public String getPath() {
        return getField().getPath();
    }

    @Override
    public String getEntityTypeName() {
        return getField().getEntityTypeName();
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
        return getField().getVisibilityRule();
    }

    @Override
    public TypeMetadata getDeclaringType() {
        return getField().getDeclaringType();
    }

    @Override
    public void adopt(ComplexTypeMetadata metadata) {
        FieldMetadata copy = getField().copy();
        copy.setContainingType(metadata);
        metadata.addField(copy);
    }

    @Override
    public FieldMetadata copy() {
        return new SoftFieldRef(repository, fieldName, containingTypeName);
    }

    @Override
    public List<String> getHideUsers() {
        return getField().getHideUsers();
    }

    @Override
    public List<String> getWriteUsers() {
        return getField().getWriteUsers();
    }

    @Override
    public boolean isMany() {
        return getField().isMany();
    }

    @Override
    public boolean isMandatory() {
        return getField().isMandatory();
    }

    @Override
    public <T> T accept(MetadataVisitor<T> visitor) {
        return getField().accept(visitor);
    }

    @Override
    public String toString() {
        return getContainingType().toString() + "/" + fieldName; //$NON-NLS-1$
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof FieldMetadata && getField().equals(o);
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
