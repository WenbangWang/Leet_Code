package com.wwb.leetcode.utils.bookmark;

import org.w3c.dom.Node;

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

// Sync bookmarks from local un-version-controlled file to version control.
public class BookmarkSyncer {
    private static final String VERSION_CONTROL_BOOKMARK_FILE_NAME = "bookmarks.xml";

    public static void main(String[] args) {
        writeToFile();
    }

    private static void writeToFile() {
        File statText = new File(VERSION_CONTROL_BOOKMARK_FILE_NAME);
        try(FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);
            Writer w = new BufferedWriter(osw)) {
            var localBookmarks = new LocalBookmarks();
            // wipe
            w.write("");

            printNode(localBookmarks.sortBookMarkByFileName(localBookmarks.getBookmarksNode()), is);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
