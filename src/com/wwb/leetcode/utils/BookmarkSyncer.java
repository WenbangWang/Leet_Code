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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;

public class BookmarkSyncer {
    private static final String WINDOWS_PATH = "C:\\Users\\Wenbang Wang\\AppData\\Roaming\\JetBrains\\IdeaIC2022.1\\workspace\\";
    private static final String MAC_PATH = "/Users/wenbangwang/Library/Application Support/JetBrains/IdeaIC2022.1/workspace/";

    public static void main(String[] args) {
        writeToFile();
    }

    private static void writeToFile() {
        try {
            //Whatever the file path is.
            File statText = new File("bookmarks.xml");
            FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw);
            // wipe
            w.write("");

            printNode(getCurrentBookmarksNode(), is);

            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getIdeaPath() {
        String userDirectory = System.getProperty("user.dir");

        return String.join(File.separator, Arrays.asList(userDirectory, ".idea"));
    }

    private static String getProjectId() {
        String workspaceFileName = String.join(File.separator, Arrays.asList(getIdeaPath(), "workspace.xml"));

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
        System.out.println("Current path: " + path);
        String projectId = getProjectId();
        System.out.println("Current project id: " + projectId);

        String workspaceFileName = path + projectId + ".xml";

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
                    component.normalize();
                    return component;
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("No bookmarks found");
    }

    private static void printNode(Node node, OutputStream output) {
        try {
            StreamResult result = new StreamResult(output);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // pretty print
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.transform(new DOMSource(node), result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
