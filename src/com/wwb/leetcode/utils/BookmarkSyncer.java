package com.wwb.leetcode.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

public class BookmarkSyncer {
    private static final String WINDOWS_PATH = "C:\\Users\\Wenbang Wang\\AppData\\Roaming\\JetBrains\\IdeaIC2022.1\\workspace\\";
    private static final String MAC_PATH = "";

    public static void main(String[] args) {
        String path = System.getProperty("os.name").toLowerCase().contains("windows") ? WINDOWS_PATH : MAC_PATH;
        System.out.println(path + getProjectId() + ".xml");

       printNode(getCurrentBookmarksNode());
    }

    private static String getProjectId() {
        String userDirectory = System.getProperty("user.dir");
        String workspaceFileName = String.join(File.separator, Arrays.asList(userDirectory, ".idea", "workspace.xml"));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(workspaceFileName));

            var components = doc.getElementsByTagName("component");

            for (int i = 0; i < components.getLength(); i++) {
                var component = components.item(i);
                var projectIdComponent = component.getAttributes().getNamedItem("name");

                if (projectIdComponent.getTextContent().equals("ProjectId")) {
                    return component.getAttributes().getNamedItem("id").getTextContent();
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("No project ID found");
    }

    private static Node getCurrentBookmarksNode() {
        String path = System.getProperty("os.name").toLowerCase().contains("windows") ? WINDOWS_PATH : MAC_PATH;
        String workspaceFileName = path + getProjectId() + ".xml";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(workspaceFileName));

            var components = doc.getElementsByTagName("component");

            for (int i = 0; i < components.getLength(); i++) {
                var component = components.item(i);
                var bookmarksManagerComponent = component.getAttributes().getNamedItem("name");

                if (bookmarksManagerComponent.getTextContent().equals("BookmarksManager")) {
                    return component;
//                    var options = component.getno();
//                    System.out.println(options.getLength());
//
//                    for (int j = 0; j < options.getLength(); j++) {
//                        var option = options.item(j);
//                        var bookmarksOptionComponent = option.getAttributes().getNamedItem("name");
//
//                        if (bookmarksOptionComponent.getTextContent().equals("groups")) {
//                            return option;
//                        }

//                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("No bookmarks found");
    }

    private static void printNode(Node node) {
        try {
            StreamResult result = new StreamResult(new StringWriter());
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // pretty print
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.transform(new DOMSource(node), result);

            System.out.println(result.getWriter());
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
