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

/**
 * Visitor for all type information classes in org.talend.mdm.commmon.metadata package.
 */
public interface MetadataVisitor<T> {

    T visit(MetadataRepository repository);

    T visit(SimpleTypeMetadata simpleType);

    T visit(ComplexTypeMetadata complexType);

    T visit(ContainedComplexTypeMetadata containedType);

    T visit(SimpleTypeFieldMetadata simpleField);

    T visit(EnumerationFieldMetadata enumField);

    T visit(ReferenceFieldMetadata referenceField);

    T visit(ContainedTypeFieldMetadata containedField);

    T visit(FieldMetadata fieldMetadata);
}
