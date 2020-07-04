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

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;

import java.util.*;

public class DefaultValidationHandler implements ValidationHandler {

    private static final Logger LOGGER = LogManager.getLogger(DefaultValidationHandler.class);

    private final Map<ValidationError, MultiKeyMap> errors = new HashMap<ValidationError, MultiKeyMap>();

    private int errorCount;

    private void addErrorMessage(String message, Integer lineNumber, Integer columnNumber, ValidationError error) {
        MultiKeyMap currentErrors = errors.get(error);
        if (currentErrors == null) {
            currentErrors = new MultiKeyMap();
            errors.put(error, currentErrors);
        }
        currentErrors.put(lineNumber, columnNumber, message + " (line: " + lineNumber + " / column: " + columnNumber + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public void fatal(FieldMetadata field, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        throw new RuntimeException(message);
    }

    @Override
    public void error(FieldMetadata field, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        addErrorMessage(message, lineNumber, columnNumber, error);
        errorCount++;
    }

    @Override
    public void warning(FieldMetadata field, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        LOGGER.warn(message);
    }

    @Override
    public void fatal(TypeMetadata type, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        throw new RuntimeException(message);
    }

    @Override
    public void error(TypeMetadata type, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        addErrorMessage(message, lineNumber, columnNumber, error);
        errorCount++;
    }

    @Override
    public void warning(TypeMetadata type, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
        LOGGER.warn(message);
    }

    @Override
    public void end() {
        Collection<String> messages = getMessages();
        if (!messages.isEmpty()) {
            StringBuilder aggregatedMessages = new StringBuilder();
            aggregatedMessages.append('\t');
            for (String message : messages) {
                aggregatedMessages.append(message).append('\n').append('\t').append('\t');
            }
            throw new RuntimeException("Data model is invalid:\n\t" + aggregatedMessages.toString());
        }
    }

    @Override
    public int getErrorCount() {
        return errorCount;
    }

    public Collection<String> getMessages() {
        Collection<String> messages = new LinkedList<String>();
        for (Map.Entry<ValidationError, MultiKeyMap> error : errors.entrySet()) {
            messages.addAll(error.getValue().values());
        }
        return messages;
    }
}
