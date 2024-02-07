package com.wwb.leetcode.utils.bookmark;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalBookmarks {
    private static final String WINDOWS_PATH = "C:\\Users\\Wenbang Wang\\AppData\\Roaming\\JetBrains\\IdeaIC2023.2\\workspace\\";
    private static final String MAC_PATH = "/Users/wenbangwang/Library/Application Support/JetBrains/IdeaIC2023.2/workspace/";

    private String workspaceFileName;
    private Pattern pattern;

    public LocalBookmarks() {
        this(getDefaultWorkspaceFileName());
    }

    public LocalBookmarks(String workspaceFileName) {
        this.workspaceFileName = workspaceFileName;
        this.pattern = Pattern.compile(".*No(\\d+)\\.java$");
    }

    // return <component name="BookmarksManager">...</component>
    public Node getBookmarksNode() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
//            // setIgnoringElementContentWhitespace will only work if the parser is in validating mode
//            dbf.setValidating(true);
//            dbf.setIgnoringElementContentWhitespace(true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(this.workspaceFileName));

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

    public Node sortBookMarkByFileName(Node componentNode) {
        List<Node> groupStateNodes = this.getBookmarkGroupStateNodes(componentNode);

        for (Node node : groupStateNodes) {
            NodeList optionNodes = node.getChildNodes();

            for (int i = 0; i < optionNodes.getLength(); i++) {
                Node optionNode = optionNodes.item(i);

                if (optionNode.getNodeName().equals("option") &&
                    optionNode.getAttributes().getNamedItem("name").getTextContent().equals("bookmarks")) {
                    NodeList bookmarkStateNodes = optionNode.getChildNodes();
                    List<Node> bookmarkStateNodeList = new ArrayList<>();

                    for (int j = 0; j < bookmarkStateNodes.getLength(); j++) {
                        Node bookmarkStateNode = bookmarkStateNodes.item(j);

                        if (bookmarkStateNode.getNodeName().equals("BookmarkState")) {
                            bookmarkStateNodeList.add(bookmarkStateNode);
                        }
                    }

                    while(optionNode.hasChildNodes()) {
                        optionNode.removeChild(optionNode.getFirstChild());
                    }
                    bookmarkStateNodeList.sort((n1, n2) -> {
                        String s1 = this.getBookmarkFileName(n1);
                        String s2 = this.getBookmarkFileName(n2);
                        Matcher m1 = this.pattern.matcher(s1);
                        Matcher m2 = this.pattern.matcher(s2);
                        boolean f1 = m1.find();
                        boolean f2 = m2.find();

                        if (f1 && f2) {
                            return Integer.compare(Integer.parseInt(m1.group(1)), Integer.parseInt(m2.group(1)));
                        }

                        // standard file name "No(\d+)" should always come first
                        if (f1) {
                            return -1;
                        }

                        if (f2) {
                            return 1;
                        }

                        return s1.compareTo(s2);
                    });

                    for (Node n : bookmarkStateNodeList) {
                        optionNode.appendChild(n);
                    }
                }
            }
        }

        return componentNode;
    }

    private String getBookmarkFileName(Node bookmarkStateNode) {
        NodeList childNodes = bookmarkStateNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);

            if (node.getNodeName().equals("attributes")) {
                NodeList attributesChildNodes = node.getChildNodes();

                for (int j = 0; j < attributesChildNodes.getLength(); j++) {
                    Node entryNode = attributesChildNodes.item(j);

                    if (entryNode.getNodeName().equals("entry")) {

                        return entryNode.getAttributes().getNamedItem("value").getTextContent();
                    }
                }
            }
        }

        throw new RuntimeException("Did not find book mark file name from BookmarkState");
    }

    private List<Node> getBookmarkGroupStateNodes(Node bookmarksNode) {
        NodeList childNodes = bookmarksNode.getChildNodes();

        List<Node> groupStateNodes = new ArrayList<>();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeName().equals("option")) {
                NodeList groupsOptionChildNodes = node.getChildNodes();

                for (int j = 0; j < groupsOptionChildNodes.getLength(); j++) {
                    Node optionNode = groupsOptionChildNodes.item(j);

                    if (optionNode.getNodeName().equals("GroupState")) {
                        groupStateNodes.add(optionNode);
                    }
                }
            }
        }

        return groupStateNodes;
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

    private static String getIdeaPath() {
        String userDirectory = System.getProperty("user.dir");

        return String.join(File.separator, Arrays.asList(userDirectory, ".idea"));
    }

    private static String getDefaultWorkspaceFileName() {
        String path = System.getProperty("os.name").toLowerCase().contains("windows") ? WINDOWS_PATH : MAC_PATH;
        System.out.println("Current path: " + path);
        String projectId = getProjectId();
        System.out.println("Current project id: " + projectId);

        return path + projectId + ".xml";
    }
}
