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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;

/**
 *
 */
@SuppressWarnings({"HardCodedStringLiteral", "nls"})
public class ConsoleDumpMetadataVisitor extends DefaultMetadataVisitor<Void> {

    private final Set<TypeMetadata> processedTypes = new HashSet<TypeMetadata>();

    private int indent = 0;

    private Logger logger;

    public ConsoleDumpMetadataVisitor() {
    }

    public ConsoleDumpMetadataVisitor(Logger logger) {
        this.logger = logger;
    }

    private void log(String message) {
        if (logger != null) {
            StringBuilder indentString = new StringBuilder();
            for (int i = 0; i < indent; i++) {
                indentString.append('\t');
            }
            indentString.append(message);
            logger.info(indentString.toString());
        } else {
            for (int i = 0; i < indent; i++) {
                System.out.print('\t');
            }
            System.out.println(message);
        }
    }

    public Void visit(ComplexTypeMetadata complexType) {
        if (processedTypes.contains(complexType)) {
            return null;
        } else {
            processedTypes.add(complexType);
        }
        log("[Type] " + complexType.getName()); //$NON-NLS-1$
        String keyFields = "";
        for (FieldMetadata keyFieldMetadata : complexType.getKeyFields()) {
            keyFields += keyFieldMetadata.getName() + " "; //$NON-NLS-1$
        }
        if (!keyFields.isEmpty()) {
            log("\t[Key fields] " + keyFields); //$NON-NLS-1$
        }

        String superTypes = ""; //$NON-NLS-1$
        for (TypeMetadata superType : complexType.getSuperTypes()) {
            superTypes += superType.getName() + " "; //$NON-NLS-1$
        }
        if (!superTypes.isEmpty()) {
            log("[Super types] " + superTypes); //$NON-NLS-1$
        }

        indent++;
        {
            super.visit(complexType);
        }
        indent--;

        if (complexType.isInstantiable()) {
            processedTypes.clear();
        }
        return null;
    }

    @Override
    public Void visit(ReferenceFieldMetadata referenceField) {
        if (referenceField.isKey()) {
            log("[Field (FK) (Key)] " + referenceField.getName()); //$NON-NLS-1$
        } else {
            try {
                log("[Field (FK -> " + referenceField.getDeclaringType().getName() + " to " + referenceField.getReferencedType().getName() + ")] " + referenceField.getName() + (referenceField.isMany() ? "*" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            } catch (Exception e) {
                throw new RuntimeException("Exception during display of field '" + referenceField.getName() + "' of type '" + referenceField.getContainingType().getName() + "'.", e);
            }
        }

        log("\t[Referenced field(s)]"); //$NON-NLS-1$
        indent += 2;
        referenceField.getReferencedField().accept(this);
        indent -= 2;

        log("\t[FKIntegrity=" + referenceField.isFKIntegrity() + " / FKIntegrityOverride=" + referenceField.allowFKIntegrityOverride() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (referenceField.hasForeignKeyInfo()) {
            for (FieldMetadata fieldMetadata : referenceField.getForeignKeyInfoFields()) {
                log("\t[FKInfo=" + fieldMetadata.getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        log("\t[Full path: " + referenceField.getEntityTypeName() + "/" + referenceField.getPath() + "]");
        logUsers(referenceField);
        return null;
    }

    public Void visit(SimpleTypeFieldMetadata simpleField) {
        if (simpleField.isKey()) {
            log("[Field (Simple) (Key) -> " + simpleField.getType().getName() + "] " + simpleField.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            log("[Field (Simple) -> " + simpleField.getType().getName() + "] " + simpleField.getName() + (simpleField.isMany() ? "*" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        logUsers(simpleField);
        log("\t[Full path: " + simpleField.getEntityTypeName() + "/" + simpleField.getPath() + "]");
        return null;
    }

    public Void visit(EnumerationFieldMetadata enumField) {
        log("[Field (Enumeration) -> " + enumField.getType().getName() + "] " + enumField.getName()); //$NON-NLS-1$ //$NON-NLS-2$
        logUsers(enumField);
        log("\t[Full path: " + enumField.getEntityTypeName() + "/" + enumField.getPath() + "]");
        return null;
    }

    @Override
    public Void visit(ContainedTypeFieldMetadata containedField) {
        ComplexTypeMetadata containedType = containedField.getContainedType();
        log("[Field (Contained type) -> " + containedType.getName() + "] " + containedField.getName() + (containedField.isMany() ? "*" : ""));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        logUsers(containedField);
        log("\t[Full path: " + containedField.getEntityTypeName() + "/" + containedField.getPath() + "]");
        indent++;
        {
            containedType.accept(this);
            if (!containedType.getSubTypes().isEmpty()) {
                log("[Sub Types]");
                indent++;
                for (ComplexTypeMetadata subType : containedType.getSubTypes()) {
                    subType.accept(this);
                }
                indent--;
            }
        }
        indent--;

        return null;
    }

    @Override
    public Void visit(ContainedComplexTypeMetadata containedType) {
        log("\tResolved: " + containedType.isHasFrozenUsages());
        return visit((ComplexTypeMetadata) containedType);
    }

    private void logUsers(FieldMetadata metadata) {
        if (!metadata.getHideUsers().isEmpty()) {
            log("\t[Hide users: " + metadata.getHideUsers() + "]");  //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!metadata.getWriteUsers().isEmpty()) {
            log("\t[Allow write users: " + metadata.getWriteUsers() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
