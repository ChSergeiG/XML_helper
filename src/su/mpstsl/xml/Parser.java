package su.mpstsl.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class Parser {

    private ArrayList<Node> nodes;
    private MainWindow window;
    // Area in main window
    private JTextArea outputArea;
    private File[] inputFiles;
    private File wd;
    private int numberOfInputFiles;

    /**
     * Parser constructor.
     * @param window main window of this app
     * @param wd working directory, we are working in and near
     */
    Parser(MainWindow window, File wd) {
        this.window = window;
        this.wd = wd;
        nodes = new ArrayList<>();
        outputArea = window.workshop.textArea;
        inputFiles = wd.listFiles();
        try {
            numberOfInputFiles = inputFiles.length;
        } catch (NullPointerException e) {
            outputArea.append("NullPointerException\n");
        }
    }

    /**
     * Main method to parse files in 'File wd'.
     */

    void parseIt() {
        Node newDHead, newSHead;
        // Define location for output files as a relative directory to directory with pointed files
        File outputDetailedFile = new File(wd.getParent() + "\\catalogue_products.xml");
        File outputSummaryFile = new File(wd.getParent() + "\\products_to_categories.xml");
        // Fill files by default (bicycle-bicycle)
        fillFileByDefault(outputDetailedFile);
        fillFileByDefault(outputSummaryFile);
        outputArea.append("Files to parse:\n");
        for (File file : inputFiles)
            outputArea.append(file.getAbsolutePath() + "\n");
        outputArea.append("-------------------------------------\n");
        outputArea.append("Output files:\n" + outputDetailedFile.getAbsolutePath() + "\n" +
                outputSummaryFile.getAbsolutePath() + "\n" + "-------------------------------------\n");
        try {
            // Builders for detailed and summary files
            DocumentBuilder detDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            DocumentBuilder sumDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // DOM trees
            Document detDocument = detDocumentBuilder.parse(outputDetailedFile);
            Document sumDocument = sumDocumentBuilder.parse(outputSummaryFile);
            // Get parent element (it was created by fillFileByDefault method)
            newDHead = detDocument.getDocumentElement();
            newSHead = sumDocument.getDocumentElement();
            // Parse all files in target folder
            for (File file : inputFiles) {
                // For every file in folder create document builder and document to build
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse(file);
                // Root element for "file"
                Node head = document.getDocumentElement();
                // Get all child nodes from root and place it in list
                NodeList offers = head.getChildNodes();
                for (int i = 0; i < offers.getLength(); i++) {
                    Node offer = offers.item(i);
                    // consider only nodes with <offer> tag
                    if (offer.getNodeName().equals("offer")) {
                        offer.normalize();
                        nodes.add(offer);
                        outputArea.append("+");
                    }
                }
            }
            // Every extracted node writes in summary and detailed file
            for (Node node : nodes) {
                NodeList childList = node.getChildNodes();
                // building node for detailed file
                Element detNode = detDocument.createElement("offer");
                for (int i = 0; i < childList.getLength(); i++) {
                    Node current = childList.item(i);
                    for (int j = 0; j < TableEntry.ENTRY_SIZE; j++) {
                        if (current.getNodeName().equals(TableEntry.tags[j])) {
                            Element element = detDocument.createElement(current.getNodeName());
                            element.setTextContent(current.getTextContent());
                            detNode.appendChild(element);
                        }
                    }
                }
                // building node for summary file
                Element sumNode = sumDocument.createElement("item");
                Element id = sumDocument.createElement("id");
                Element parent = sumDocument.createElement("parent");
                Element innerItem = sumDocument.createElement("item");
                parent.appendChild(innerItem);
                sumNode.appendChild(id);
                sumNode.appendChild(parent);
                for (int i = 0; i < childList.getLength(); i++) {
                    Node current = childList.item(i);
                    switch (current.getNodeName()) {
                        case "id":
                            id.setTextContent(current.getTextContent());
                            break;
                        case "parent":
                            innerItem.setTextContent(current.getTextContent());
                            break;
                        default:
                            break;
                    }
                }
                newDHead.appendChild(detNode);
                newSHead.appendChild(sumNode);
            }
            // Write detailed file
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(detDocument);
            FileOutputStream fos = new FileOutputStream(outputDetailedFile);
            StreamResult result = new StreamResult(fos);
            tr.transform(source, result);
            fos.close();
            // Write summary file
            tr = TransformerFactory.newInstance().newTransformer();
            source = new DOMSource(sumDocument);
            fos = new FileOutputStream(outputSummaryFile);
            result = new StreamResult(fos);
            tr.transform(source, result);
            fos.close();
            outputArea.append("\n");
        } catch (TransformerConfigurationException e) {
            outputArea.append("TransformerConfigurationException\n");
            return;
        } catch (TransformerException e) {
            outputArea.append("TransformerException\n");
            return;
        } catch (ParserConfigurationException e) {
            outputArea.append("ParserConfigurationException\n");
            return;
        } catch (SAXException e) {
            outputArea.append("SAXException\n");
            return;
        } catch (IOException e) {
            outputArea.append("IOException\n");
            return;
        }
        outputArea.append("#nodes: " + nodes.size() + "\n");
        outputArea.append("Completed without errors\n");

        window.workshop.setParseButtonDisabled();
    }

    /**
     *
     * @param f this file will be filled by template
     */
    private void fillFileByDefault(File f) {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
        Date nowDate = new Date();
        String outputFilesHeader = "<?xml version =\"1.0\" encoding=\"UTF-8\"?>\n<КоммерческаяИнформация ВерсияСхемы=\"2.03\" ДатаФормирования=\"";
        String outputFileDate = sdf.format(nowDate);
        String outputFilesFooter = "\">\n</КоммерческаяИнформация>";
        try {
            FileWriter outputFW = new FileWriter(f);
            outputFW.write(outputFilesHeader + outputFileDate + outputFilesFooter);
            outputFW.close();
        } catch (IOException e) {
            outputArea.append(e.toString());
        }
    }
}
