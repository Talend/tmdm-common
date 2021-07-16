/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import java.util.concurrent.ConcurrentLinkedQueue;

import org.talend.mdm.commmon.metadata.validation.ValidationFactory;
import org.talend.mdm.commmon.metadata.validation.ValidationRule;
import org.w3c.dom.Element;

/**
 *
 */
public class SimpleTypeMetadata extends MetadataExtensions implements TypeMetadata {

    private final String nameSpace;

    private final Collection<TypeMetadata> superTypes = new ConcurrentLinkedQueue<TypeMetadata>();

    private String name;

    public SimpleTypeMetadata(String nameSpace, String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null.");
        }
        this.name = name;
        this.nameSpace = nameSpace;
    }

    public Collection<TypeMetadata> getSuperTypes() {
        return superTypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        throw new IllegalStateException("Cannot change name after type was created.");
    }

    public String getNamespace() {
        return nameSpace;
    }

    public boolean isAssignableFrom(TypeMetadata type) {
        // Check one level of inheritance
        Collection<TypeMetadata> superTypes = getSuperTypes();
        for (TypeMetadata superType : superTypes) {
            if (type.getName().equals(superType.getName())) {
                return true;
            }
        }
        // Checks in type inheritance hierarchy.
        for (TypeMetadata superType : superTypes) {
            if (superType.isAssignableFrom(type)) {
                return true;
            }
        }
        return getName().equals(type.getName());
    }

    public TypeMetadata copy() {
        SimpleTypeMetadata copy = new SimpleTypeMetadata(nameSpace, name);
        for (TypeMetadata superType : superTypes) {
            copy.addSuperType(superType.copy());
        }
        return copy;
    }

    public TypeMetadata copyShallow() {
        return new SimpleTypeMetadata(nameSpace, name);
    }

    public TypeMetadata freeze() {
        if (!superTypes.isEmpty()) {
            Collection<TypeMetadata> thisSuperTypes = new ConcurrentLinkedQueue<TypeMetadata>(superTypes);
            superTypes.clear();
            for (TypeMetadata superType : thisSuperTypes) {
                if (superType != null && isInstantiable() == superType.isInstantiable()) {
                    superType = superType.freeze();
                    superTypes.add(superType);
                }
            }
        }
        return this;
    }

    @Override
    public boolean isInstantiable() {
        return false;
    }

    @Override
    public boolean isFrozen() {
        return true;
    }

    @Override
    public void validate(ValidationHandler handler) {
        if (!superTypes.isEmpty()) {
            for (TypeMetadata superType : superTypes) {
                if (isInstantiable() == superType.isInstantiable()) {
                    superType.validate(handler);
                } else {
                    handler.error(superType,
                            "Non instantiable type cannot inherits from entity type.",
                            superType.<Element>getData(MetadataRepository.XSD_DOM_ELEMENT),
                            superType.<Integer>getData(MetadataRepository.XSD_LINE_NUMBER),
                            superType.<Integer>getData(MetadataRepository.XSD_COLUMN_NUMBER),
                            ValidationError.ENTITY_CANNOT_INHERIT_FROM_REUSABLE_TYPE);
                }
            }
        }
    }

    @Override
    public ValidationRule createValidationRule() {
        return ValidationFactory.getRule(this);
    }

    @Override
    public void setInstantiable(boolean isInstantiable) {
        if (isInstantiable) {
            throw new IllegalStateException("Simple types can not be instantiated.");
        }
    }

    public void addSuperType(TypeMetadata superType) {
        superTypes.add(superType);
    }

    public <T> T accept(MetadataVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return '[' + nameSpace + ':' + name + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleTypeMetadata)) return false;

        SimpleTypeMetadata that = (SimpleTypeMetadata) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (nameSpace != null ? !nameSpace.equals(that.nameSpace) : that.nameSpace != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nameSpace != null ? nameSpace.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
