/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.validation;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadataImpl;
import org.talend.mdm.commmon.metadata.CompoundFieldMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataExtensible;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.SoftFieldRef;
import org.talend.mdm.commmon.metadata.SoftIdFieldRef;
import org.talend.mdm.commmon.metadata.SoftTypeRef;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.UnresolvedFieldMetadata;
import org.talend.mdm.commmon.metadata.UnresolvedTypeMetadata;

public class ValidationFactory {

    private static final String VALIDATION_MARKER = "validation.validated"; //$NON-NLS-1$

    private static boolean isValidated(MetadataExtensible metadataElement) {
        return BooleanUtils.isTrue(metadataElement.<Boolean> getData(VALIDATION_MARKER));
    }

    public static ValidationRule getRule(FieldMetadata field) {
        if (isValidated(field)) {
            if (field instanceof UnresolvedFieldMetadata) {
                return NoOpValidationRule.FAIL;
            } else {
                return NoOpValidationRule.SUCCESS;
            }
        }
        field.setData(VALIDATION_MARKER, true);
        return field.createValidationRule();
    }

    public static ValidationRule getRule(MetadataRepository repository) {
        return new CircularDependencyValidationRule(repository);
    }

    public static ValidationRule getRule(SoftFieldRef field) {
        throw new IllegalArgumentException("Soft references must be frozen before validation.");
    }

    public static ValidationRule getRule(CompoundFieldMetadata field) {
        return new FieldTypeValidationRule(field);
    }

    public static ValidationRule getRule(ReferenceFieldMetadata field) {
        List<ValidationRule> rules = new LinkedList<ValidationRule>();
        rules.add(new FieldInheritanceOverrideRule(field)); // All fields common rule
        rules.add(new FieldTypeValidationRule(field)); // All fields common rule
        rules.add(new PermissionValidationRule(field));
        rules.add(new ForeignKeyExist(field));
        rules.add(new ForeignKeyMaxLength(field));
        rules.add(new ForeignKeyCannotBeKey(field));
        rules.add(new ForeignKeyNotStringTyped(field));
        rules.add(new ForeignKeyInfo(field));
        // Warning
        rules.add(new ForeignKeyShouldPointToID(field));
        return new CompositeValidationRule(rules.toArray(new ValidationRule[rules.size()]));
    }

    public static ValidationRule getRule(SimpleTypeFieldMetadata field) {
        List<ValidationRule> rules = new LinkedList<ValidationRule>();
        rules.add(new FieldInheritanceOverrideRule(field));
        rules.add(new FieldTypeValidationRule(field));
        rules.add(new VisibilityValidationRule(field));
        rules.add(new PermissionValidationRule(field));
        return new CompositeValidationRule(rules.toArray(new ValidationRule[rules.size()]));
    }

    public static ValidationRule getRule(SoftIdFieldRef field) {
        throw new IllegalArgumentException("Soft references must be frozen before validation.");
    }

    public static ValidationRule getRule(UnresolvedFieldMetadata field) {
        return new UnresolvedField(field);
    }

    public static ValidationRule getRule(ContainedTypeFieldMetadata field) {
        List<ValidationRule> rules = new LinkedList<ValidationRule>();
        rules.add(new FieldInheritanceOverrideRule(field));
        rules.add(new FieldTypeValidationRule(field));
        rules.add(new ForeignKeyHostCannotBeComplexTypeValidationRule(field));
        rules.add(new VisibilityValidationRule(field));
        rules.add(new CircleFieldInheritanceRule(field));
        rules.add(new PermissionValidationRule(field));
        return new CompositeValidationRule(rules.toArray(new ValidationRule[rules.size()]));
    }

    public static ValidationRule getRule(EnumerationFieldMetadata field) {
        List<ValidationRule> rules = new LinkedList<ValidationRule>();
        rules.add(new FieldInheritanceOverrideRule(field));
        rules.add(new FieldTypeValidationRule(field));
        rules.add(new VisibilityValidationRule(field));
        rules.add(new PermissionValidationRule(field));
        return new CompositeValidationRule(rules.toArray(new ValidationRule[rules.size()]));
    }

    public static ValidationRule getRule(TypeMetadata type) {
        if (isValidated(type)) {
            if (type instanceof UnresolvedTypeMetadata) {
                return NoOpValidationRule.FAIL;
            } else {
                return NoOpValidationRule.SUCCESS;
            }
        }
        type.setData(VALIDATION_MARKER, true);
        return type.createValidationRule();
    }

    public static ValidationRule getRule(SoftTypeRef type) {
        throw new IllegalArgumentException("Soft references must be frozen before validation.");
    }

    public static ValidationRule getRule(ComplexTypeMetadataImpl type) {
        List<ValidationRule> rules = new LinkedList<ValidationRule>();
        rules.add(new TypeNamingValidationRule(type));
        rules.add(new SuperTypeValidationRule(type));
        rules.add(new KeyFieldsValidationRule(type));
        rules.add(new LookupFieldsValidationRule(type));
        rules.add(new PrimaryKeyInfoValidationRule(type));
        rules.add(new XSDAttributeValidationRule(type));
        rules.add(new PermissionValidationRule(type));
        if (!type.isInstantiable()) {
            rules.add(new UnusedReusableTypeValidationRule(type));
        } else {
        	rules.add(new EntityUsesAbstractTypeValidationRule(type));
        }
        return new CompositeValidationRule(rules.toArray(new ValidationRule[rules.size()]));
    }

    public static ValidationRule getRule(UnresolvedTypeMetadata unresolvedType) {
        return new UnresolvedType(unresolvedType);
    }
}
