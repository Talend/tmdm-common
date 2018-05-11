/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.xsd;

import javax.xml.XMLConstants;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDConcreteComponent;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTypeDefinition;

/**
 * A 'walker' for the parsed XML schema (calls the right methods on
 * {@link org.talend.mdm.commmon.metadata.xsd.XSDVisitor visitor}.
 */
public class XmlSchemaWalker {

    public static void walk(XSDSchema xmlSchema, XSDVisitor visitor) {
        visitor.visitSchema(xmlSchema);
        // Visit element first (create MDM entity types)
        EList elements = xmlSchema.getElementDeclarations();
        for (Object element : elements) {
            walk(((XSDElementDeclaration) element), visitor);
        }
        // Visit remaining types (sometimes used in case of inheritance by entity types).
        EList types = xmlSchema.getTypeDefinitions();
        for (Object type : types) {
            walk(((XSDTypeDefinition) type), visitor);
        }
    }

    public static void walk(XSDTypeDefinition type, XSDVisitor visitor) {
        if (type == null) {
            return;
        }
        if (type instanceof XSDSimpleTypeDefinition) {
            walk(((XSDSimpleTypeDefinition) type), visitor);
        } else if (type instanceof XSDComplexTypeDefinition) {
            if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.getTargetNamespace())) {
                return;
            }
            walk(((XSDComplexTypeDefinition) type), visitor);
        } else {
            throw new IllegalArgumentException("Not supported XML Schema type: " + type.getClass().getName());
        }
    }

    public static void walk(XSDElementDeclaration element, XSDVisitor visitor) {
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(element.getTargetNamespace())) {
            return;
        }
        visitor.visitElement(element);
    }

    private static void walk(XSDSimpleTypeDefinition xmlSchemaType, XSDVisitor visitor) {
        visitor.visitSimpleType(xmlSchemaType);
    }

    private static void walk(XSDComplexTypeDefinition xmlSchemaType, XSDVisitor visitor) {
        if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(xmlSchemaType.getTargetNamespace())) {
            return;
        }
        visitor.visitComplexType(xmlSchemaType);
    }

    public static void walk(XSDConcreteComponent component, XSDVisitor visitor) {
        if (component instanceof XSDElementDeclaration) {
            walk(((XSDElementDeclaration) component), visitor);
        }
    }
}
