/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.metadata.validation;

import java.util.Collections;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.xsd.util.XSDParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ValidationError;
import org.talend.mdm.commmon.metadata.ValidationHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * MDM does not take into account &lt;xsd:attribute&gt; declarations in the XSD (only elements are parsed). This warning indicates
 * this to the user.
 */
class XSDAttributeValidationRule implements ValidationRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(XSDAttributeValidationRule.class);

    private final ComplexTypeMetadata type;

    private final XPath xPath;

    public XSDAttributeValidationRule(ComplexTypeMetadata type) {
        this.type = type;
        xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {

            @Override
            public String getNamespaceURI(String prefix) {
                if ("xsd".equals(prefix)) { //$NON-NLS-1$
                    return XMLConstants.W3C_XML_SCHEMA_NS_URI;
                }
                return null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return "xsd";
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return Collections.singletonList("xsd").iterator();
            }
        });
    }

    @Override
    public boolean perform(ValidationHandler handler) {
        Element element = type.<Element> getData(MetadataRepository.XSD_DOM_ELEMENT);
        if (element == null) {
            return true; // No need to check anything if no DOM element
        }
        Integer lineNumber = type.<Integer> getData(MetadataRepository.XSD_LINE_NUMBER);
        Integer columnNumber = type.<Integer> getData(MetadataRepository.XSD_COLUMN_NUMBER);
        try {
            NodeList nodeSet = (NodeList) xPath.evaluate("//xsd:attribute", element, XPathConstants.NODESET); //$NON-NLS-1$
            for (int i = 0; i < nodeSet.getLength(); i++) {
                handler.warning(type, "Entity type '" + type.getName()
                        + "' uses XSD attribute but attributes are ignored by MDM.", (Element) nodeSet.item(i),
                        XSDParser.getStartLine(nodeSet.item(i)), XSDParser.getStartColumn(nodeSet.item(i)),
                        ValidationError.TYPE_USE_XSD_ATTRIBUTES);
            }
            return nodeSet.getLength() == 0;
        } catch (XPathExpressionException e) {
            // Never stop the validation, but logs the exception anyway.
            LOGGER.error("Unexpected exception during XSD attributes check", e);
            handler.error(type, "Could not check type '" + type.getName() + "' for XSD attributes", element, lineNumber,
                    columnNumber, ValidationError.UNCAUGHT_ERROR);
            return false;
        }
    }

    @Override
    public boolean continueOnFail() {
        return true;
    }
}
