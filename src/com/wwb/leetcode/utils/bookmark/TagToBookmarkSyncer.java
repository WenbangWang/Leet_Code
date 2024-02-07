package com.wwb.leetcode.utils.bookmark;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagToBookmarkSyncer {
    private static final String BASE = "src/com/wwb/leetcode/";
    private static final String TAGS_SOURCE = BASE + "tags";
    private static final String EASY_SOURCE = BASE + "easy";
    private static final String HARD_SOURCE = BASE + "hard";
    private static final String MEDIUM_SOURCE = BASE + "medium";

    private static final String BOOKMARK_STATE_TEMPLATE =
        "<BookmarkState>" +
        "  <attributes><entry key=\"url\" value=\"file://$PROJECT_DIR$/%s\" /></attributes>" +
        "  <option name=\"description\" value=\"\" />" +
        "  <option name=\"provider\" value=\"com.intellij.ide.bookmark.providers.LineBookmarkProvider\" />" +
        "</BookmarkState>";
    private static final String BOOKMARK_OPTION_TEMPLATE =
        "<GroupState>" +
        " <option name=\"bookmarks\">" +
        "    %s" +
        "  </option>" +
        " <option name=\"name\" value=\"%s\" />" +
        "</GroupState>";
    private static final String BOOKMARK_GROUP_STATE_TEMPLATE =
        "<GroupState>%s</GroupState>";
    private static final String VERSION_CONTROL_BOOKMARK_FILE_NAME = "bookmarks.xml";

    public static void main(String[] args) {
        File tags = new File(TAGS_SOURCE);
        File easy = new File(EASY_SOURCE);
        File hard = new File(HARD_SOURCE);
        File medium = new File(MEDIUM_SOURCE);

        var groupedFiles = groupFiles(tags);

        linkTagToActualFiles(groupedFiles, getFiles(easy), getFiles(medium), getFiles(hard));
        var localBookmarks = new LocalBookmarks();

        System.out.println(groupedFiles);
        System.out.println(generateBookmarkXMLString(groupedFiles));

        File statText = new File(VERSION_CONTROL_BOOKMARK_FILE_NAME);
        try(FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw)) {
            // wipe
            w.write("");

            var node = localBookmarks.getBookmarksNode();

            var newDoc = convertStringToXml(generateBookmarkXMLString(groupedFiles));

            var elements = newDoc.getElementsByTagName("GroupState");

//            node.appendChild(elements.item(0));
            node.getOwnerDocument().importNode(elements.item(0), true);

            System.out.println(node.getChildNodes().getLength());
            NodeList list = node.getChildNodes();

            StringSelection stringSelection = new StringSelection(generateBookmarkXMLString(groupedFiles));
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
//            for (int i = 0; i < list.getLength(); i++) {
//                Node node = list.item(i);
//
//                if (node.getNodeName().equals())
//            }
//            node.getOwnerDocument().insertBefore(node, elements.item(0));

            try {
                StreamResult result = new StreamResult(is);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();

                // pretty print
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                transformer.transform(new DOMSource(elements.item(0)), result);
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Document convertStringToXml(String xmlString) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder builder = dbf.newDocumentBuilder();

            Document doc = builder.parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"utf-8\"?><option name=\"groups\">" + xmlString + "</option>")));

            return doc;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

    }

    private static String generateBookmarkXMLString(Map<String, List<String>> groupedFiles) {
        StringBuilder sb = new StringBuilder();

        for (var entry : groupedFiles.entrySet()) {
            StringBuilder bookmarks = new StringBuilder();
            for (String file : entry.getValue()) {
                bookmarks.append(String.format(BOOKMARK_STATE_TEMPLATE, file));
                bookmarks.append("\n");
            }

            sb.append(String.format(BOOKMARK_OPTION_TEMPLATE, bookmarks, entry.getKey()));
            sb.append("\n");
        }

        return sb.toString();
    }

    private static void linkTagToActualFiles(Map<String, List<String>> tagFiles,
                                             List<String> easyFiles,
                                             List<String> mediumFiles,
                                             List<String> hardFiles) {
        for (List<String> files : tagFiles.values()) {
            for (int i = 0; i < files.size(); i++) {
                if (replace(easyFiles, files, EASY_SOURCE, i)) {
                    continue;
                }

                if (replace(mediumFiles, files, MEDIUM_SOURCE, i)) {
                    continue;
                }

                replace(hardFiles, files, HARD_SOURCE, i);
            }
        }
    }

    private static boolean replace(List<String> easyFiles, List<String> files, String source, int i) {
        for (String file : easyFiles) {
            if (files.get(i).equals(file)) {
                files.set(i, source + "/" + file);
                return true;
            }
        }

        return false;
    }

    private static List<String> getFiles(File folder) {
        List<String> result = new ArrayList<>();
        for (File f : folder.listFiles()) {
            if (f.isFile()) {
                result.add(f.getName());
            }
        }

        return result;
    }

    private static Map<String, List<String>> groupFiles(File folder) {
        Map<String, List<String>> result = new HashMap<>();

        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                List<String> fileNames = new ArrayList<>();

                for (File file : f.listFiles()) {
                    fileNames.add(file.getName());
                }

                result.put(f.getName(), fileNames);
            }
        }

        return result;
    }
}
