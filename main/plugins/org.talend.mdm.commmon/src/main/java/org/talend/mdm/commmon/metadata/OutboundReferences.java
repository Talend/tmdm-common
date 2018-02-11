/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import java.util.HashSet;
import java.util.Set;

/**
 * Returns the fields that references other concept. References are returned as a {@link java.util.Set} of
 * {@link org.talend.mdm.commmon.metadata.ReferenceFieldMetadata}.
 */
public class OutboundReferences extends DefaultMetadataVisitor<Set<ReferenceFieldMetadata>> {

    // Internal: for optimization purpose prevents checking a type more than once.
    private final Set<TypeMetadata> checkedTypes = new HashSet<TypeMetadata>();

    // Foreign key fields list to be returned at end of visit.
    private final Set<ReferenceFieldMetadata> fieldToCheck = new HashSet<ReferenceFieldMetadata>();

    @Override
    public Set<ReferenceFieldMetadata> visit(ContainedComplexTypeMetadata containedType) {
        super.visit(containedType);
        return fieldToCheck;
    }

    @Override
    public Set<ReferenceFieldMetadata> visit(ComplexTypeMetadata metadata) {
        if (!checkedTypes.contains(metadata)) {
            checkedTypes.add(metadata);
            super.visit(metadata);
        }
        return fieldToCheck;
    }

    @Override
    public Set<ReferenceFieldMetadata> visit(ReferenceFieldMetadata metadata) {
        fieldToCheck.add(metadata);
        for (ComplexTypeMetadata subType : metadata.getReferencedType().getSubTypes()) {
            subType.accept(this);
        }
        return fieldToCheck;
    }

    @Override
    public Set<ReferenceFieldMetadata> visit(SimpleTypeMetadata typeMetadata) {
        return fieldToCheck;
    }

    @Override
    public Set<ReferenceFieldMetadata> visit(MetadataRepository repository) {
        Collection<TypeMetadata> types = repository.getTypes();
        for (TypeMetadata type : types) {
            type.accept(this);
        }
        return fieldToCheck;
    }

    @Override
    public Set<ReferenceFieldMetadata> visit(FieldMetadata metadata) {
        super.visit(metadata);
        return fieldToCheck;
    }

    @Override
    public Set<ReferenceFieldMetadata> visit(SimpleTypeFieldMetadata metadata) {
        super.visit(metadata);
        return fieldToCheck;
    }

    @Override
    public Set<ReferenceFieldMetadata> visit(EnumerationFieldMetadata metadata) {
        super.visit(metadata);
        return fieldToCheck;
    }

    @Override
    public Set<ReferenceFieldMetadata> visit(ContainedTypeFieldMetadata metadata) {
        super.visit(metadata);
        for (ComplexTypeMetadata subType : metadata.getContainedType().getSubTypes()) {
            subType.accept(this);
        }
        return fieldToCheck;
    }
}
