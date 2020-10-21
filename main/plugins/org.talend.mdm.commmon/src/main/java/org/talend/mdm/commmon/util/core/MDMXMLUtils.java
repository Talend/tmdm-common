/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.commmon.util.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.exception.XmlBeanDefinitionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class MDMXMLUtils {

    private static final Logger LOGGER = Logger.getLogger(MDMXMLUtils.class);

    public static final String FEATURE_DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";

    public static final String FEATURE_LOAD_EXTERNAL = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    public static final String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";

    public static final String FEATURE_EXTERNAL_PARAM_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

    public static final String FEATURE_DEFER_NODE_EXPANSION = "http://apache.org/xml/features/dom/defer-node-expansion";

    public static final String PROPERTY_IS_SUPPORT_EXTERNAL_ENTITIES = "javax.xml.stream.isSupportingExternalEntities";

    public static final String PROPERTY_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    public static final String PROPERTY_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    public static final String PROPERTY_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    private static final DocumentBuilderFactory DOC_BUILDER_FACTORY;

    private static final DocumentBuilderFactory DOC_BUILDER_FACTORY_WITH_NAMESPACE;

    private static final XMLReader XML_READER;

    private static final SAXParserFactory SAX_PARSER_FACTORY;

    private static final XMLInputFactory XML_INPUT_FACTORY;

    private static final TransformerFactory TRANSFORMER_FACTORY;

    static {
        try {
            DOC_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
            DOC_BUILDER_FACTORY.setIgnoringComments(true);
            DOC_BUILDER_FACTORY.setExpandEntityReferences(false);
            DOC_BUILDER_FACTORY.setFeature(FEATURE_DISALLOW_DOCTYPE, true);

            DOC_BUILDER_FACTORY_WITH_NAMESPACE = DocumentBuilderFactory.newInstance();
            DOC_BUILDER_FACTORY_WITH_NAMESPACE.setNamespaceAware(true);
            DOC_BUILDER_FACTORY_WITH_NAMESPACE.setIgnoringComments(true);
            DOC_BUILDER_FACTORY_WITH_NAMESPACE.setExpandEntityReferences(false);
            DOC_BUILDER_FACTORY_WITH_NAMESPACE.setFeature(FEATURE_DISALLOW_DOCTYPE, true);
        } catch (Exception e) {
            throw new XmlBeanDefinitionException("Error occurred while initializing DocumentBuilderFactory", e);
        }

        try {
            XML_READER = XMLReaderFactory.createXMLReader();
            XML_READER.setFeature(FEATURE_DISALLOW_DOCTYPE, true);
            XML_READER.setFeature(FEATURE_LOAD_EXTERNAL, false);
            XML_READER.setFeature(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
            XML_READER.setFeature(FEATURE_EXTERNAL_PARAM_ENTITIES, false);
            XML_READER.setContentHandler(new DefaultHandler());
        } catch (Exception e) {
            throw new XmlBeanDefinitionException("Error occurred while initializing XMLReader", e);
        }

        try {
            SAX_PARSER_FACTORY = SAXParserFactory.newInstance();
            SAX_PARSER_FACTORY.setFeature(FEATURE_DISALLOW_DOCTYPE,true);
            SAX_PARSER_FACTORY.setFeature(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
            SAX_PARSER_FACTORY.setFeature(FEATURE_EXTERNAL_PARAM_ENTITIES, false);
            SAX_PARSER_FACTORY.setFeature(FEATURE_LOAD_EXTERNAL, false);
            SAX_PARSER_FACTORY.setValidating(false);
            SAX_PARSER_FACTORY.setNamespaceAware(true);
        } catch (Exception e) {
            throw new XmlBeanDefinitionException("Error occurred while initializing SAXParserFactory", e);
        }

        try {
            TRANSFORMER_FACTORY = TransformerFactory.newInstance();
            TRANSFORMER_FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (Exception e) {
            throw new XmlBeanDefinitionException("Error occurred while initializing TransformerFactory, \n caused by ", e);
        }

        XML_INPUT_FACTORY = XMLInputFactory.newInstance();
        XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        XML_INPUT_FACTORY.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        XML_INPUT_FACTORY.setProperty(PROPERTY_IS_SUPPORT_EXTERNAL_ENTITIES, false);
    }

    public static XMLStreamReader createXMLStreamReader(InputStream inputStream) {
        try {
            return XML_INPUT_FACTORY.createXMLStreamReader(inputStream);
        } catch (Exception e) {
            throw new XmlBeanDefinitionException("Error occurred while creating a new XMLStreamReader from InputStream.", e);
        }
    }

    public static XMLEventReader createXMLEventReader(InputStream inputStream) {
        try {
            return XML_INPUT_FACTORY.createXMLEventReader(inputStream);
        } catch (Exception e) {
            throw new XmlBeanDefinitionException("Error occurred while creating a new XMLEventReader from InputStream.", e);
        }
    }

    public static XMLEventReader createXMLEventReader(StringReader source) {
        try {
            return XML_INPUT_FACTORY.createXMLEventReader(source);
        } catch (Exception e) {
            throw new XmlBeanDefinitionException("Error occurred while creating a new XMLEventReader from StringReader.", e);
        }
    }

    public static SAXParser getSAXParser() {
        try {
            return SAX_PARSER_FACTORY.newSAXParser();
        } catch (Exception e) {
            throw new XmlBeanDefinitionException("Error occurred while creating a SAXParserFactory from SAXParser.", e);
        }
    }

    public static Transformer getTransformer() {
        try {
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.toString());
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            return transformer;
        } catch (Exception e) {
            throw new XmlBeanDefinitionException("Error occurred while creating a Transformer from TransformerFactory. \n caused by ", e);
        }
    }

    public static XMLReader getXMLReader() {
        return getXMLReader(new DefaultHandler());
    }

    public static XMLReader getXMLReader(DefaultHandler handler) {
        XML_READER.setContentHandler(handler);
        return XML_READER;
    }

    private static DocumentBuilderFactory getDocumentBuilderFactory() {
        return DOC_BUILDER_FACTORY;
    }

    private static DocumentBuilderFactory getDocumentBuilderFactoryWithNamespace() {
        return DOC_BUILDER_FACTORY_WITH_NAMESPACE;
    }

    public static Optional<DocumentBuilder> getDocumentBuilder() {
        DocumentBuilder builder = null;
        try {
            builder = getDocumentBuilderFactory().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XmlBeanDefinitionException("Error occurred while using DocumentBuilderFactory to create a DocumentBuilder.", e);
        }
        return Optional.ofNullable(builder);
    }

    public static Optional<DocumentBuilder> getDocumentBuilderWithNamespace() {
        DocumentBuilder builder = null;
        try {
            builder = getDocumentBuilderFactoryWithNamespace().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XmlBeanDefinitionException("Error occurred while using DocumentBuilderFactory to create a DocumentBuilder.", e);
        }
        return Optional.ofNullable(builder);
    }

    public static boolean isExistExtEntity(InputStream stream) {
        boolean results = false;
        try {
            String result = new BufferedReader(new InputStreamReader(stream))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
            results = isExistExtEntity(result);
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurred." + e);
        }
        return results;
    }

    /**
     * Take a security measurements against XML external entity attacks.
     *  {"<!ENTITY desc SYSTEM \"ect/passwd\">,
     * "<!ENTITY desc SYSTEM>", "<!ENTITY desc public>", "<!ENTITY desc system \"file:abc.txt\">", "<!ENTITY desc public
     * \"http://www.baidu.com\">", "<!ENTITY abc public>", "<!entity public \"http://www.baidu.com\">", }
     *
     * @param rawXml
     * @return
     */
    public static boolean isExistExtEntity(String rawXml) {
        if (rawXml == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("<!ENTITY\\s+\\S*\\s+[SYSTEM|PUBLIC]{1}.+?>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rawXml);
        return matcher.find();
    }

    /**
     * Utility class, extract some part of an XML. If you have an xml and want to extract some part of that. you may use
     * below {{@link #unwrap()} to get it. like:
     * 
     * <pre>
     * &lt;root&gt;
     *  &lt;Product&gt;
     *      &lt;ID&gt;11&lt;/ID&gt;
     *      &lt;Name&gt;alan&lt;/Name&gt;
     *  &lt;/Product&gt;
     *  &lt;Product&gt;
     *      &lt;ID&gt;22&lt;/ID&gt;
     *      &lt;Name&gt;emily&lt;/Name&gt;
     *  &lt;/Product&gt;
     * &lt;/root&gt;
     * </pre>
     * 
     * using <b>[tagname:"Product",index:1]</b>, will get part of xml as follows:
     * 
     * <pre>
     *  &lt;Product&gt;
     *      &lt;ID&gt;22&lt;/ID&gt;
     *      &lt;Name&gt;emily&lt;/Name&gt;
     *  &lt;/Product&gt;
     * </pre>
     * 
     * @param doc : XML
     * @param tagName : root node name of part of XML.
     * @param index : child
     * @return : the part of xml which comes inside <b>tagName</b>
     * @throws ParserConfigurationException
     */
    public static Document unwrap(Document doc, String tagName, int index) throws ParserConfigurationException {
        assert doc != null : "Document is null";
        assert tagName != null : "tagName is null";
        assert index >= 0 : "Index (zero based) must be greater than or equal to zero";
        NodeList nodeList = doc.getElementsByTagName(tagName);
        if (nodeList.getLength() <= index) {
            // only return the whole xml
            LOGGER.warn("Index [" + index + "] is invalid because the value is out of bounds " + nodeList.getLength()
                    + ", so only return the raw xml");
            return doc;
        }
        Node node = nodeList.item(index);
        Optional<DocumentBuilder> dBuilder = getDocumentBuilder();
        if (dBuilder.isPresent()) {
            Document result = dBuilder.get().newDocument();
            Node importNode = result.importNode(node, true);
            result.appendChild(importNode);
            return result;
        } else {
            return doc;
        }
    }

    /**
     * Utility class to convert outputstream to a string using String initializer in Java.
     * <p>
     * If you want to unwrap a node, you can use another method {{@link #unwrap()} to get the part of xml,then you can
     * perform the following method {#{@link transformXMLToString} to simply convert the OutputStream to finalString
     * using String's toString which created a string writer.
     * 
     * @param doc : the xml want to convert to string
     * @return a String that want to pull out
     * @throws Exception
     */
    public static String transformXMLToString(Document doc) throws Exception {
        assert doc != null : "Document is null";
        Transformer transformer = getTransformer();
        DOMSource source = new DOMSource(doc);
        String content = StringUtils.EMPTY;
        try (StringWriter stringWriter = new StringWriter();) {
            StreamResult result = new StreamResult(stringWriter);
            transformer.transform(source, result);
            content = stringWriter.toString();
        }
        return content;
    }
}
