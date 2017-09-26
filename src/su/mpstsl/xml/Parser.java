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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

class Parser {

    private ArrayList<Node> nodes;
    private MainWindow window;
    // Area in main window
    private JTextArea outputArea;
    private File[] inputFiles;
    private File wd;
    private SimpleDateFormat sdf;

    /**
     * Parser constructor.
     *
     * @param window main window of this app
     * @param wd     working directory, we are working in and near
     */
    Parser(MainWindow window, File wd) {
        this.window = window;
        this.wd = wd;
        nodes = new ArrayList<>();
        outputArea = window.workshop.textArea;
        inputFiles = wd.listFiles();
        checkFiles();
    }

    /**
     * Check file extensions
     */

    private void checkFiles() {
        File[] checkedFiles = new File[0];
        for (File file : inputFiles) {
            String name = file.getName();
            if (name.substring(name.length() - 4, name.length()).equals(".xml"))
                checkedFiles = pushFileToArray(checkedFiles, file);
        }
        inputFiles = checkedFiles;
    }

    /**
     * Push file into file array
     *
     * @param array array with files
     * @param file  file to push
     * @return updated array
     */

    private File[] pushFileToArray(File[] array, File file) {
        File[] newFileArray = Arrays.copyOf(array, array.length + 1);
        newFileArray[array.length] = file;
        return newFileArray;
    }

    /**
     * General method to parse files in 'File wd'.
     */

    void parseIt() {
        long start = System.nanoTime();
        int nodeAmount;
        Node newDHead, newSHead;
        // Define location for output files as a relative directory to directory with pointed files
        File outputDetailedFile = new File(wd.getParent() + "\\catalogue_products.xml");
        File outputSummaryFile = new File(wd.getParent() + "\\products_to_categories.xml");
        // Fill files by default (bicycle-bicycle)
        fillFileByDefault(outputDetailedFile);
        fillFileByDefault(outputSummaryFile);
        sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        outputArea.append("Files to parse:\n");
        for (File file : inputFiles)
            printFileInfo(file);
        outputArea.append("-------------------------------------\nOutput files:\n");
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
            for (File file : inputFiles)
                readFile(file);
            nodeAmount = nodes.size();
            // Every extracted node writes in summary and detailed file
            for (Node node : nodes) {
                addNodeToFiles(node, newDHead, newSHead, detDocument, sumDocument);
            }
            nodes = null;
            // Write detailed file
            writeFile(detDocument, outputDetailedFile);
            // Write summary file
            writeFile(sumDocument, outputSummaryFile);
            printFileInfo(outputDetailedFile);
            printFileInfo(outputSummaryFile);
        } catch (Exception e) {
            outputArea.append(e.getMessage() + "\n");
            return;
        }
        outputArea.append("-------------------------------------\n#nodes: " + nodeAmount + "\n");
        long time = (System.nanoTime() - start) / 1000 / 1000;
        outputArea.append("Completed without errors in " + time + " ms\n");
        window.workshop.setParseButtonDisabled();
    }

    /**
     * Method to write document into file
     *
     * @param detDocument document ready to write into file
     * @param file        write here
     * @throws TransformerException TransformerException
     * @throws IOException          IOException
     * @see TransformerException
     * @see IOException
     */

    private void writeFile(Document detDocument, File file) throws TransformerException, IOException {
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(detDocument);
        FileOutputStream fos = new FileOutputStream(file);
        StreamResult result = new StreamResult(fos);
        tr.transform(source, result);
        fos.close();
    }

    /**
     * Method to convert node into new formatted nodes. After this writes these nodes into document
     *
     * @param node        input node ready to convert
     * @param newDHead    new node for detailed document
     * @param newSHead    new node for summary document
     * @param detDocument detailed document
     * @param sumDocument summary document
     */

    private void addNodeToFiles(Node node, Node newDHead, Node newSHead, Document detDocument, Document sumDocument) {
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
            if (current.getNodeName().equals("id"))
                id.setTextContent(current.getTextContent());
            else if (current.getNodeName().equals("parent"))
                innerItem.setTextContent(current.getTextContent());

        }
        newDHead.appendChild(detNode);
        newSHead.appendChild(sumNode);
    }

    /**
     * Method to read file and write "offer" nodes to global ArrayList&lt;Nodes&gt; nodes
     *
     * @param file file to read
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws IOException                  IOException
     * @throws SAXException                 SAXException
     * @see ParserConfigurationException
     * @see IOException
     * @see SAXException
     */

    private void readFile(File file) throws ParserConfigurationException, IOException, SAXException {
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
            }
        }
    }

    /**
     * Only to print info about file into text area
     *
     * @param file file to print info about
     */
    private void printFileInfo(File file) {
        outputArea.append(String.format("%s\n[%6dKb] Modified %s\n", file.getAbsolutePath(),
                file.length() / 1024, sdf.format(new Date(file.lastModified()))));
    }

    /**
     * Write template to given file
     *
     * @param file this file will be filled by template
     */
    private void fillFileByDefault(File file) {
        sdf = new SimpleDateFormat("YYYY-MM-dd");
        Date nowDate = new Date();
        String outputFileHeader = "<?xml version =\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<КоммерческаяИнформация ВерсияСхемы=\"2.03\" ДатаФормирования=\"";
        String outputFileDate = sdf.format(nowDate);
        String outputFileFooter = "\">\n</КоммерческаяИнформация>";
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), "UTF8"));
            bw.write(outputFileHeader);
            bw.write(outputFileDate);
            bw.write(outputFileFooter);
            bw.flush();
        } catch (Exception e) {
            outputArea.append(e.toString());
        }
    }
}
