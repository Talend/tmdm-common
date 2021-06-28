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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.commmon.metadata.validation.ValidationFactory;
import org.talend.mdm.commmon.metadata.validation.ValidationRule;
import org.w3c.dom.Element;

/**
 * Represents a reference to a {@link ComplexTypeMetadata} type where methods are evaluated using
 * {@link MetadataRepository#getComplexType(String)} calls. This is useful to reference types that might not be already
 * parsed by {@link MetadataRepository#load(java.io.InputStream)}.
 */
public class SoftTypeRef implements ComplexTypeMetadata {

    private final MetadataRepository repository;

    private final String typeName;

    private final FieldMetadata fieldRef;

    private final String namespace;

    private final boolean instantiable;

    private final Map<String, Object> additionalData = new HashMap<String, Object>();

    private TypeMetadata frozenType;

    private final List<ComplexTypeMetadata> usages = new ArrayList<ComplexTypeMetadata>();

    private SoftTypeRef(MetadataRepository repository, FieldMetadata fieldRef) {
        if (fieldRef == null) {
            throw new IllegalArgumentException("Field reference cannot be null.");
        }
        this.repository = repository;
        this.typeName = null;
        this.namespace = null;
        this.fieldRef = fieldRef;
        this.instantiable = true;
    }

    public SoftTypeRef(MetadataRepository repository, String namespace, String typeName, boolean isInstantiable) {
        if (typeName == null) {
            throw new IllegalArgumentException("Type name cannot be null.");
        }
        this.repository = repository;
        this.typeName = typeName;
        this.namespace = namespace;
        this.fieldRef = null;
        this.instantiable = isInstantiable;
    }

    private TypeMetadata getType() {
        if (typeName != null) {
            TypeMetadata type;
            if (instantiable) {
                type = repository.getType(namespace, typeName);
            } else {
                type = repository.getNonInstantiableType(namespace, typeName);
            }
            return type;
        } else {
            return fieldRef.getContainingType();
        }
    }

    private ComplexTypeMetadata getTypeAsComplex() {
        TypeMetadata type = getType();
        if (!(type instanceof ComplexTypeMetadata)) {
            throw new IllegalArgumentException("Type '" + typeName + "' was expected to be a complex type (but was "
                    + type.getClass().getName() + ").");
        }
        return (ComplexTypeMetadata) type;
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
    public Collection<TypeMetadata> getSuperTypes() {
        return getType().getSuperTypes();
    }

    @Override
    public String getName() {
        return typeName;
    }

    @Override
    public void setName(String name) {
        getType().setName(name);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public FieldMetadata getField(String fieldName) {
        return getTypeAsComplex().getField(fieldName);
    }

    @Override
    public Collection<FieldMetadata> getFields() {
        return getTypeAsComplex().getFields();
    }

    @Override
    public boolean isAssignableFrom(TypeMetadata type) {
        return getType().isAssignableFrom(type);
    }

    @Override
    public TypeMetadata copy() {
        if (typeName != null) {
            return new SoftTypeRef(repository, namespace, typeName, instantiable);
        } else {
            return new SoftTypeRef(repository, fieldRef.copy());
        }
    }

    @Override
    public TypeMetadata copyShallow() {
        return this;
    }

    @Override
    public TypeMetadata freeze() {
        if (frozenType == null) {
            TypeMetadata type = getType();
            if (type == null) {
                UnresolvedTypeMetadata typeMetadata = new UnresolvedTypeMetadata(typeName);
                Set<Map.Entry<String, Object>> data = additionalData.entrySet();
                for (Map.Entry<String, Object> currentData : data) {
                    typeMetadata.setData(currentData.getKey(), currentData.getValue());
                }
                return typeMetadata;
            }
            frozenType = type.freeze();
            Set<Map.Entry<String, Object>> data = additionalData.entrySet();
            for (Map.Entry<String, Object> currentData : data) {
                frozenType.setData(currentData.getKey(), currentData.getValue());
            }
            if (frozenType instanceof ComplexTypeMetadata) {
                for (ComplexTypeMetadata usage : usages) {
                    ((ComplexTypeMetadata) frozenType).declareUsage(usage);
                }
            }
        }
        return frozenType;
    }

    @Override
    public void addSuperType(TypeMetadata superType) {
        getType().addSuperType(superType);
    }

    @Override
    public <T> T accept(MetadataVisitor<T> visitor) {
        return getType().accept(visitor);
    }

    @Override
    public String toString() {
        if (typeName != null) {
            return typeName;
        } else if (fieldRef != null) {
            return fieldRef.toString();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean isInstantiable() {
        return instantiable;
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void validate(ValidationHandler handler) {
        TypeMetadata type;
        if (instantiable) {
            type = repository.getType(namespace, typeName);
        } else {
            type = repository.getNonInstantiableType(namespace, typeName);
        }
        if (type == null) {
            if (instantiable) {
                handler.error((TypeMetadata) null,
                        "Entity type '" + typeName + "' (namespace: '" + namespace + "') is not present in type repository.",
                        (Element) additionalData.get(MetadataRepository.XSD_DOM_ELEMENT),
                        (Integer) additionalData.get(MetadataRepository.XSD_LINE_NUMBER),
                        (Integer) additionalData.get(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.TYPE_DOES_NOT_EXIST);
            } else {
                handler.error((TypeMetadata) null,
                        "Non entity type '" + typeName + "' (namespace: '" + namespace + "') is not present in type repository.",
                        (Element) additionalData.get(MetadataRepository.XSD_DOM_ELEMENT),
                        (Integer) additionalData.get(MetadataRepository.XSD_LINE_NUMBER),
                        (Integer) additionalData.get(MetadataRepository.XSD_COLUMN_NUMBER),
                        ValidationError.TYPE_DOES_NOT_EXIST);
            }
        }
    }

    @Override
    public ValidationRule createValidationRule() {
        return ValidationFactory.getRule(this);
    }

    @Override
    public void setInstantiable(boolean isInstantiable) {
        getType().setInstantiable(isInstantiable);
    }

    @Override
    public String getName(Locale locale) {
        return getTypeAsComplex().getName(locale);
    }

    @Override
    public ComplexTypeMetadata getEntity() {
        return getTypeAsComplex().getEntity();
    }

    @Override
    public FieldMetadata getContainer() {
        return getTypeAsComplex().getContainer();
    }

    @Override
    public void setContainer(FieldMetadata field) {
        getTypeAsComplex().setContainer(field);
    }

    @Override
    public Collection<FieldMetadata> getKeyFields() {
        return getTypeAsComplex().getKeyFields();
    }

    @Override
    public void addField(FieldMetadata fieldMetadata) {
        getTypeAsComplex().addField(fieldMetadata);
    }

    @Override
    public List<String> getWriteUsers() {
        return getTypeAsComplex().getWriteUsers();
    }

    @Override
    public List<String> getHideUsers() {
        return getTypeAsComplex().getHideUsers();
    }

    @Override
    public List<String> getDenyCreate() {
        return getTypeAsComplex().getDenyCreate();
    }

    @Override
    public List<String> getDenyDelete(DeleteType type) {
        return getTypeAsComplex().getDenyDelete(type);
    }

    @Override
    public String getSchematron() {
        return getTypeAsComplex().getSchematron();
    }

    @Override
    public List<FieldMetadata> getPrimaryKeyInfo() {
        return getTypeAsComplex().getPrimaryKeyInfo();
    }

    @Override
    public boolean hasField(String fieldName) {
        return getType() instanceof ComplexTypeMetadata && getTypeAsComplex().hasField(fieldName);
    }

    @Override
    public Collection<ComplexTypeMetadata> getSubTypes() {
        return getTypeAsComplex().getSubTypes();
    }

    @Override
    public Collection<ComplexTypeMetadata> getDirectSubTypes() {
        return getTypeAsComplex().getDirectSubTypes();
    }

    @Override
    public void registerSubType(ComplexTypeMetadata type) {
    }

    @Override
    public List<FieldMetadata> getLookupFields() {
        return getTypeAsComplex().getLookupFields();
    }

    @Override
    public void declareUsage(ComplexTypeMetadata usage) {
        usages.add(usage);
    }

    @Override
    public Collection<ComplexTypeMetadata> getUsages() {
        return usages;
    }

    @Override
    public void setSubTypes(List<ComplexTypeMetadata> subTypes) {
        getTypeAsComplex().setSubTypes(subTypes);
    }

    @Override
    public void registerName(Locale locale, String label) {
        getTypeAsComplex().registerName(locale, label);
    }

    @Override
    public void registerKey(FieldMetadata keyField) {
        getTypeAsComplex().registerKey(keyField);
    }

    @Override
    public int hashCode() {
        TypeMetadata type = getType();
        if (type != null) {
            return type.hashCode();
        } else {
            int result = typeName.hashCode();
            result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
            result = 31 * result + (instantiable ? 1 : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SoftTypeRef)) {
            return false;
        }

        SoftTypeRef that = (SoftTypeRef) o;

        if (instantiable != that.instantiable) {
            return false;
        }
        if (namespace != null ? !namespace.equals(that.namespace) : that.namespace != null) {
            return false;
        }
        if (!typeName.equals(that.typeName)) {
            return false;
        }

        return true;
    }

    @Override
    public String getDescription(Locale locale) {
        return getTypeAsComplex().getDescription(locale);
    }

    @Override
    public void registerDescription(Locale locale, String description) {
        getTypeAsComplex().registerDescription(locale, description);
    }

    @Override
    public List<Category> getCategories() {
        return Collections.<Category> emptyList();
    }
}
