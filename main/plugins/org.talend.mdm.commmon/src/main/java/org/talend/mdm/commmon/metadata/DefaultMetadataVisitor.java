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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Default visitor for data model classes in package org.talend.mdm.commmon.metadata.
 */
public class DefaultMetadataVisitor<T> implements MetadataVisitor<T> {

    private static final Logger LOGGER = LogManager.getLogger(DefaultMetadataVisitor.class);

    protected static boolean isDatabaseMandatory(FieldMetadata field, TypeMetadata declaringType) {
        boolean isDatabaseMandatory = field.isMandatory() && declaringType.isInstantiable();
        if (field.isMandatory() && !isDatabaseMandatory) {
            LOGGER.warn("Field '" + field.getName() + "' is mandatory but constraint cannot be enforced in database schema."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return isDatabaseMandatory;
    }

    /**
     * Visit all types located in <code>repository</code>.
     * @param repository A {@link MetadataRepository}.
     * @return Result typed as T.
     */
    public T visit(MetadataRepository repository) {
        for (TypeMetadata type : repository.getUserComplexTypes()) {
            type.accept(this);
        }
        for (TypeMetadata type : repository.getNonInstantiableTypes()) {
            type.accept(this);
        }
        return null;
    }

    /**
     * @param simpleType A simple typed field.
     * @return Result typed as T.
     */
    public T visit(SimpleTypeMetadata simpleType) {
        return null;
    }

    /**
     * Visit all fields declared in <code>complexType</code>.
     * @param complexType A complex type.
     * @return Result typed as T.
     */
    public T visit(ComplexTypeMetadata complexType) {
        List<FieldMetadata> copy = new ArrayList<FieldMetadata>(complexType.getFields());
        Collection<FieldMetadata> keyFields = complexType.getKeyFields();
        for (FieldMetadata keyField : keyFields) {
            keyField.accept(this);
            copy.remove(keyField);
        }
        for (FieldMetadata field : copy) {
            field.accept(this);
        }
        return null;
    }

    /**
     * Visit all field declared in contained type.
     * @param containedType A contained type.
     * @return Result typed as T.
     */
    public T visit(ContainedComplexTypeMetadata containedType) {
        for (FieldMetadata field : containedType.getFields()) {
            field.accept(this);
        }
        return null;
    }

    public T visit(ReferenceFieldMetadata referenceField) {
        return null;
    }

    public T visit(ContainedTypeFieldMetadata containedField) {
        return containedField.getContainedType().accept(this);
    }

    public T visit(FieldMetadata fieldMetadata) {
        return null;
    }

    public T visit(SimpleTypeFieldMetadata simpleField) {
        return null;
    }

    public T visit(EnumerationFieldMetadata enumField) {
        return null;
    }

}
