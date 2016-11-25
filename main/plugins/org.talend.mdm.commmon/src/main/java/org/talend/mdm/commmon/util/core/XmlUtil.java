package org.talend.mdm.commmon.util.core;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.w3c.dom.Node;

public class XmlUtil {

    private static final Logger logger = Logger.getLogger(XmlUtil.class);

    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private static final TransformerFactory saxonTransformerFactory = new net.sf.saxon.TransformerFactoryImpl();

    public static Transformer generateTransformer() throws TransformerConfigurationException {
        return transformerFactory.newTransformer();
    }

    public static Transformer generateTransformer(boolean isOmitXmlDeclaration) throws TransformerConfigurationException {
        Transformer transformer = generateTransformer();
        if (isOmitXmlDeclaration) {
            transformer.setOutputProperty("omit-xml-declaration", "yes");
        } else {
            transformer.setOutputProperty("omit-xml-declaration", "no");
        }
        return transformer;
    }

    public static Transformer generateTransformer(boolean isOmitXmlDeclaration, boolean isIndent)
            throws TransformerConfigurationException {
        Transformer transformer = generateTransformer(isOmitXmlDeclaration);
        if (isIndent) {
            transformer.setOutputProperty("indent", "yes");
        } else {
            transformer.setOutputProperty("indent", "no");
        }
        return transformer;
    }

    public static String nodeToString(Node n, boolean isOmitXmlDeclaration, boolean isIndent) throws TransformerException {
        StringWriter sw = new StringWriter();
        Transformer transformer = generateTransformer(isOmitXmlDeclaration, isIndent);
        transformer.transform(new DOMSource(n), new StreamResult(sw));
        return sw.toString();
    }

    public static String nodeToString(Node n) throws TransformerException {
        return XmlUtil.nodeToString(n, true, true);
    }

    public static Document styleDocument(Document document, String stylesheet) throws Exception {
        // load the transformer using JAXP
        Transformer transformer = saxonTransformerFactory.newTransformer(new StreamSource(new StringReader(stylesheet)));

        // now lets style the given document
        DocumentSource source = new DocumentSource(document);
        DocumentResult result = new DocumentResult();
        transformer.transform(source, result);

        // return the transformed document
        Document transformedDoc = result.getDocument();

        if (logger.isDebugEnabled()) {
            logger.debug("The xml file style transformed successfully ");
        }
        return transformedDoc;
    }
}
