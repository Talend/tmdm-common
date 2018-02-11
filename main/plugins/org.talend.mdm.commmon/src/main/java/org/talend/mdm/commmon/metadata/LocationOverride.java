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

import org.w3c.dom.Element;

public class LocationOverride implements ValidationHandler {

    private final ValidationHandler handler;

    private final Element xmlElement;

    private final Integer line;

    private final Integer column;

    private final FieldMetadata fieldMetadata;

    public LocationOverride(FieldMetadata fieldMetadata, ValidationHandler handler, Element xmlElement, Integer line, Integer column) {
        this.fieldMetadata = fieldMetadata;
        this.handler = handler;
        this.xmlElement = xmlElement;
        this.line = line;
        this.column = column;
    }

    @Override
    public void error(TypeMetadata type, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        if (error == ValidationError.TYPE_DOES_NOT_EXIST || error == ValidationError.TYPE_DOES_NOT_OWN_FIELD) {
            if (lineNumber == null || lineNumber < 0) {
                handler.error(fieldMetadata.getContainingType(), message, xmlElement, line, column, error);
            } else {
                handler.error(fieldMetadata.getContainingType(), message, element, lineNumber, columnNumber, error);
            }
        } else {
            handler.error(type, message, xmlElement, line, column, error);
        }
    }

    public void warning(TypeMetadata type, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        handler.warning(type, message, xmlElement, line, column, error);
    }

    public void end() {
        handler.end();
    }

    public int getErrorCount() {
        return handler.getErrorCount();
    }

    public void fatal(FieldMetadata field, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        handler.fatal(field, message, xmlElement, line, column, error);
    }

    @Override
    public void error(FieldMetadata field, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        if (error == ValidationError.TYPE_DOES_NOT_EXIST || error == ValidationError.TYPE_DOES_NOT_OWN_FIELD) {
            if (lineNumber == null || lineNumber < 0) {
                handler.error(fieldMetadata, message, xmlElement, line, column, error);
            } else {
                handler.error(fieldMetadata, message, element, lineNumber, columnNumber, error);
            }
        } else {
            handler.error(field, message, xmlElement, line, column, error);
        }
    }

    public void warning(FieldMetadata field, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        handler.warning(field, message, xmlElement, line, column, error);
    }

    public void fatal(TypeMetadata type, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        handler.fatal(type, message, xmlElement, line, column, error);
    }
}
