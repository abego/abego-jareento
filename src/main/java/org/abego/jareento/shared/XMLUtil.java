package org.abego.jareento.shared;

import org.abego.jareento.base.JareentoException;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class XMLUtil {
    @Nullable
    public static String childText(Node node, String... path) {
        Node actNode = node;
        for (String nodeName : path) {
            NodeList nodes = actNode.getChildNodes();
            Node c = firstNodeWithNodeName(nodes, nodeName);
            if (c == null) {
                return null;
            }
            actNode = c;
        }
        return actNode.getTextContent();
    }

    @Nullable
    public static Node firstNodeWithNodeName(NodeList nodes, String nodeName) {
        int n = nodes.getLength();
        for (int i = 0; i < n; i++) {
            Node c = nodes.item(i);
            if (c.getNodeName().equals(nodeName)) {
                return c;
            }
        }
        return null;
    }

    public static Element getXMLDocumentElement(File xmlFile) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            return doc.getDocumentElement();
        } catch (ParserConfigurationException | IOException |
                 SAXException e) {
            throw new JareentoException(
                    "Error when reading %s".formatted(xmlFile.getAbsolutePath()));
        }
    }
}
