/*
 * Copyright (C) 2006-2013 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata;

import java.util.*;

/**
 *
 */
public class SoftIdFieldRef implements FieldMetadata {

    private final MetadataRepository repository;

    private final String typeName;

    private final String fieldName;

    private final Map<String, Object> additionalData = new HashMap<String, Object>();

    private FieldMetadata frozenField;

    public SoftIdFieldRef(MetadataRepository metadataRepository, String typeName) {
        this(metadataRepository, typeName, null);
    }

    public SoftIdFieldRef(MetadataRepository metadataRepository, String typeName, String fieldName) {
        repository = metadataRepository;
        this.typeName = typeName;
        this.fieldName = fieldName;
    }

    private FieldMetadata getField() {
        if (frozenField == null) {
            DefaultValidationHandler handler = new DefaultValidationHandler();
            freeze(handler);
            handler.end();
        }
        return frozenField;
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
        if (fieldName != null) {
            return fieldName;
        } else {
            return getField().getName();
        }
    }

    @Override
    public boolean isKey() {
        return true; // No need to look for the field, this is an id
    }

    @Override
    public TypeMetadata getType() {
        return getField().getType();
    }

    @Override
    public ComplexTypeMetadata getContainingType() {
        return getField().getContainingType();
    }

    @Override
    public void setContainingType(ComplexTypeMetadata typeMetadata) {
        getField().setContainingType(typeMetadata);
    }

    @Override
    public FieldMetadata freeze(ValidationHandler handler) {
        if (frozenField != null) {
            return frozenField;
        }
        ComplexTypeMetadata type = (ComplexTypeMetadata) repository.getType(typeName);
        if (type == null) {
            type = (ComplexTypeMetadata) repository.getNonInstantiableType(repository.getUserNamespace(), typeName);
        }
        if (type == null) {
            handler.fatal((TypeMetadata) null, "Type '" + typeName + "' does not exist.", -1, -1, ValidationError.TYPE_DOES_NOT_EXIST);
            return this;
        }
        Collection<FieldMetadata> keyFields = type.getKeyFields();
        if (fieldName == null) {
            if (keyFields.size() == 1) {
                frozenField = keyFields.iterator().next();
            } else {
                frozenField = new CompoundFieldMetadata(keyFields.toArray(new FieldMetadata[keyFields.size()]));
            }
        } else {
            frozenField = type.getField(fieldName);
        }
        Set<Map.Entry<String, Object>> data = additionalData.entrySet();
        for (Map.Entry<String, Object> currentData : data) {
            frozenField.setData(currentData.getKey(), currentData.getValue());
        }
        return frozenField;
    }

    @Override
    public void promoteToKey() {
        getField().promoteToKey();
    }

    @Override
    public void validate(ValidationHandler handler) {
        ComplexTypeMetadata type = (ComplexTypeMetadata) repository.getType(typeName);
        if (type == null) {
            type = (ComplexTypeMetadata) repository.getNonInstantiableType(repository.getUserNamespace(), typeName);
        }
        if (type == null) {
            handler.error(this,
                    "Type '" + typeName + "' does not exist",
                    (Integer) additionalData.get(MetadataRepository.XSD_LINE_NUMBER),
                    (Integer) additionalData.get(MetadataRepository.XSD_COLUMN_NUMBER),
                    ValidationError.TYPE_DOES_NOT_EXIST);
            return;
        }
        Collection<FieldMetadata> keyFields = type.getKeyFields();
        if (keyFields.isEmpty()) {
            handler.error(type,
                    "Type '" + typeName + "' does not own a key and no FK field was defined.",
                    type.<Integer>getData(MetadataRepository.XSD_LINE_NUMBER),
                    type.<Integer>getData(MetadataRepository.XSD_COLUMN_NUMBER),
                    ValidationError.TYPE_DOES_NOT_OWN_KEY);
        }
    }

    @Override
    public TypeMetadata getDeclaringType() {
        return getField().getDeclaringType();
    }

    @Override
    public void adopt(ComplexTypeMetadata metadata, MetadataRepository repository) {
        getField().adopt(metadata, repository);
    }

    @Override
    public FieldMetadata copy(MetadataRepository repository) {
        return new SoftIdFieldRef(this.repository, typeName);
    }

    @Override
    public List<String> getHideUsers() {
        return getField().getHideUsers();
    }

    @Override
    public List<String> getWriteUsers() {
        return getField().getHideUsers();
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
        return getField().toString();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof FieldMetadata && getField().equals(o);
    }
}