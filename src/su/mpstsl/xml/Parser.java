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

class Parser implements Thread.UncaughtExceptionHandler {
    private static final int DET_MODE = 0;
    private static final int SUM_MODE = 1;

    private ArrayList<Node> nodes;
    private MainWindow window;

    private JTextArea outputArea;
    private File[] inputFiles;
    private File wd;

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
        try {
            long start = System.nanoTime();
            int[] modes = {DET_MODE, SUM_MODE};
            String[] filePaths = {"\\catalogue_products.xml", "\\products_to_categories.xml"};
            outputArea.append("Files to parse:\n");
            // Parse all files in target folder
            for (File file : inputFiles) {
                readFile(file);
                printFileInfo(file);
            }
            outputArea.append("-------------------------------------\nOutput files:\n");
            for (int i = 0; i < filePaths.length; i++) {
                // Define location for output file
                File outputFile = new File(wd.getParent() + filePaths[i]);
                pushDocumentToFile(outputFile, nodes, modes[i]);
                printFileInfo(outputFile);
            }
            outputArea.append("-------------------------------------\n#nodes: " + nodes.size() + "\n");
            long time = (System.nanoTime() - start) / 1000 / 1000;
            outputArea.append("Completed without errors in " + time + " ms\n");
            nodes = null;
            window.workshop.setParseButtonDisabled();
        } catch (TransformerException e) {
            uncaughtException(Thread.currentThread(),
                    new RuntimeException(e.getCause() + " (" + e.getException() + ")\n\t" + e.getMessageAndLocation()));
        } catch (ParserConfigurationException e) {
            uncaughtException(Thread.currentThread(),
                    new RuntimeException(e.getCause() + "\n\t" + e.getLocalizedMessage()));
        } catch (SAXException e) {
            uncaughtException(Thread.currentThread(),
                    new RuntimeException("SAXexception: " + e.getMessage()));

        } catch (IOException e) {
            uncaughtException(Thread.currentThread(),
                    new RuntimeException(e.getCause() + "\n\t" + e.getMessage() + ". " + e.getStackTrace()[0]));
        }
    }

    /**
     * @param outputFile file to write in
     * @param nodes      ArrayList of nodes to push
     * @param mode       mode detailed/summary0
     * @throws IOException                  IOException
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws SAXException                 SAXException
     * @throws TransformerException         TransformerException
     */
    private void pushDocumentToFile(File outputFile, ArrayList<Node> nodes, int mode)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {
        // Fill files by default (bicycle-bicycle)
        fillFileByDefault(outputFile);
        // Builders for file
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        // DOM tree
        Document document = documentBuilder.parse(outputFile);
        // Get parent element (it was created by fillFileByDefault method)
        Node newNode = document.getDocumentElement();
        for (Node node : nodes)
            addNodeToFile(node, newNode, document, mode);
        // Write  file
        writeFile(document, outputFile);
    }

    /**
     * Method to write document into file
     *
     * @param document document ready to write into file
     * @param file     write here
     * @throws TransformerException TransformerException
     * @throws IOException          IOException
     */

    private void writeFile(Document document, File file) throws TransformerException, IOException {
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(document);
        FileOutputStream fos = new FileOutputStream(file);
        StreamResult result = new StreamResult(fos);
        tr.transform(source, result);
        fos.close();
    }

    /**
     * Method to convert node into new formatted nodes. After this writes these nodes into document
     *
     * @param node     input node ready to convert
     * @param newHead  new node for detailed document
     * @param document detailed document
     * @param mode     mode number here in static section
     */

    private void addNodeToFile(Node node, Node newHead, Document document, int mode) {
        NodeList childList = node.getChildNodes();
        switch (mode) {
            case DET_MODE:
                // building node for detailed file
                Element detNode = document.createElement("offer");
                for (int i = 0; i < childList.getLength(); i++) {
                    Node current = childList.item(i);
                    for (int j = 0; j < TableEntry.ENTRY_SIZE; j++) {
                        if (current.getNodeName().equals(TableEntry.tags[j])) {
                            Element element = document.createElement(current.getNodeName());
                            element.setTextContent(current.getTextContent());
                            detNode.appendChild(element);
                        }
                    }
                }
                newHead.appendChild(detNode);
                break;
            case SUM_MODE:
                // building node for summary file
                Element sumNode = document.createElement("item");
                Element id = document.createElement("id");
                Element parent = document.createElement("parent");
                Element innerItem = document.createElement("item");
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
                newHead.appendChild(sumNode);
                break;
            default:
                break;
        }
    }

    /**
     * Method to read file and write "offer" nodes to global ArrayList&lt;Nodes&gt; nodes
     *
     * @param file file to read
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws IOException                  IOException
     * @throws SAXException                 SAXException
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
        outputArea.append(String.format("%s\n[%6dKb] Modified %s\n", file.getAbsolutePath(), file.length() / 1024,
                new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date(file.lastModified()))));
    }

    /**
     * @param file this file will be filled by template
     * @throws IOException IOException
     */
    private void fillFileByDefault(File file) throws IOException {
        Date nowDate = new Date();
        String outputFileString = "<?xml version =\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<КоммерческаяИнформация ВерсияСхемы=\"2.03\" ДатаФормирования=\"" +
                new SimpleDateFormat("YYYY-MM-dd").format(nowDate) + "\">\n</КоммерческаяИнформация>";

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), "UTF8"));
        bw.write(outputFileString);
        bw.flush();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String message = "Unexpected situation in thread " + t.getName() + ".\n\t" +
                e.getStackTrace()[0] + " " + e.getMessage();
        JOptionPane.showMessageDialog(null, message, "Exception", JOptionPane.ERROR_MESSAGE);
    }
}
