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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

class Parser {
    private static final int DET_MODE = 0;
    private static final int SUM_MODE = 1;

    private static HashMap<String, Node> nodes;
    private static HashMap<String, String[]> update;

    private static MainWindow window;
    private static ArrayList<File> inputFiles;
    private static File workingDirectory;
    private static File remainderFile;

    /**
     * Initialize some constants inside Parser
     *
     * @param wnd        Window, called this method. In this case - main window of application
     * @param wDirectory Directory, marked as working by user
     * @param rFile      File, containing updated information about goods
     */
    static void initConstants(MainWindow wnd, File wDirectory, File rFile) {
        window = wnd;
        workingDirectory = wDirectory;
        remainderFile = rFile;
        inputFiles = buildFilesArray();
        nodes = buildNodesMap("offer");
    }

    /**
     * Method to build ArrayList with target files
     *
     * @return target files
     */
    private static ArrayList<File> buildFilesArray() {
        ArrayList<File> result = new ArrayList<>();
        rAddFilesToArray(workingDirectory, result);
        return result;
    }

    /**
     * Recursive method to find every xml in working folder and its subfolders.
     *
     * @param workingDirectory directory to find target xml`s
     * @param result           ArrayList link, to place founded File objects
     */
    private static void rAddFilesToArray(File workingDirectory, ArrayList<File> result) {
        File[] fileList = workingDirectory.listFiles();
        if (fileList == null) return;
        for (File file : fileList) {
            String name = file.getName();
            int lastDotIndex = name.lastIndexOf('.');
            String ext = (lastDotIndex == -1) ? "" : name.substring(lastDotIndex + 1);
            if (file.isDirectory())
                rAddFilesToArray(file, result);
            if (!file.equals(remainderFile) && ext.equals("xml")) {
                result.add(file);
            }
        }
    }

    /**
     * Method to build HashMap with nodes. Key is ID of node.
     *
     * @param parentName tagname to find in xml inputFiles
     * @return Hashmap with nodes and its unique ID as keys
     */
    private static HashMap<String, Node> buildNodesMap(String parentName) {
        HashMap<String, Node> result = new HashMap<>();
        try {
            if (parentName.equals("offer")) window.putLog("Обрабатываемые файлы:");
            for (File file2parse : inputFiles) {
                if (parentName.equals("offer")) printFileInfo(file2parse);
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse(file2parse);
                Node head = document.getDocumentElement();
                NodeList nodes = head.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    NodeList childs = nodes.item(i).getChildNodes();
                    String id = null;
                    for (int j = 0; j < childs.getLength(); j++) {
                        if (childs.item(j).getNodeName().equals(TableEntry.tags[0]))
                            id = childs.item(j).getTextContent();
                    }

                    if (id != null) {
                        if (id.length() > 4) {
                            if (result.containsKey(id)) window.putLog("*** -> " + id + " - ID дубликат");
                            if (nodes.item(i).getNodeName().equals(parentName)) result.put(id, nodes.item(i));
                        }
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            reportException(Thread.currentThread(),
                    new RuntimeException(e.getCause() + "\n\t" + e.getLocalizedMessage()));
        } catch (SAXException e) {
            reportException(Thread.currentThread(),
                    new RuntimeException("SAXexception: " + e.getMessage()));

        } catch (IOException e) {
            reportException(Thread.currentThread(),
                    new RuntimeException(e.getCause() + "\n\t" + e.getMessage() + ". " + e.getStackTrace()[0]));
        }
        return result;
    }

    /**
     * main-like procedure sequence to read every input file, get nodes, parse it and write back to target files.
     */
    static void parseIt() {
        try {
            long start = System.nanoTime();
            int[] modes = {DET_MODE, SUM_MODE};
            String[] filePaths = {"\\catalogue_products.xml", "\\products_to_categories.xml"};
            update = buildUpdateMap(remainderFile);
            window.putLog("-------------------------------------\n" +
                    updateNodes(nodes, update) + "\nФайлы для загрузки:");
            for (int i = 0; i < filePaths.length; i++) {
                // Define location for output file
                File outputFile = new File(workingDirectory.getParent() + filePaths[i]);
                pushDocumentToFile(outputFile, nodes, modes[i]);
                printFileInfo(outputFile);
            }
            window.putLog("-------------------------------------\nВсего обработано уникальных записей: " +
                    nodes.size());
            long time = (System.nanoTime() - start) / 1000000;
            window.putLog("Завершено без ошибок за " + time + " мс");
            nodes = null;
            //window.workshop.setParseButtonDisabled();
        } catch (TransformerException e) {
            reportException(Thread.currentThread(),
                    new RuntimeException(e.getCause() + " (" + e.getException() + ")\n\t" + e.getMessageAndLocation()));
        } catch (ParserConfigurationException e) {
            reportException(Thread.currentThread(),
                    new RuntimeException(e.getCause() + "\n\t" + e.getLocalizedMessage()));
        } catch (SAXException e) {
            reportException(Thread.currentThread(),
                    new RuntimeException("SAXexception: " + e.getMessage()));

        } catch (IOException e) {
            reportException(Thread.currentThread(),
                    new RuntimeException(e.getCause() + "\n\t" + e.getMessage() + ". " + e.getStackTrace()[0]));
        }
    }

    /**
     * Method to build update HashMap
     *
     * @param remainderFile actual information about nodes
     * @return Map with pairs ID - String array with actual parameters for ID
     * @throws ParserConfigurationException Parser Configuration Exc
     * @throws IOException                  IO Exc
     * @throws SAXException                 SAX Exc
     */

    private static HashMap<String, String[]> buildUpdateMap(File remainderFile)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(remainderFile);
        Node head = document.getDocumentElement();
        NodeList updatedOffers = head.getChildNodes();
        HashMap<String, String[]> result = new HashMap<>();
        // Build update map
        for (int i = 0; i < updatedOffers.getLength(); i++) {
            Node rfOffer = updatedOffers.item(i);
            if (rfOffer.getNodeName().equals("offer")) {
                NodeList rfOfferChilds = rfOffer.getChildNodes();
                String[] values = new String[5];
                String id = null;
                for (int j = 0; j < rfOfferChilds.getLength(); j++) {
                    Node rfOfferChild = rfOfferChilds.item(j);
                    switch (rfOfferChild.getNodeName()) {
                        case "id":
                            id = rfOfferChild.getTextContent();
                            break;
                        case "price":
                            values[0] = rfOfferChild.getTextContent();
                            break;
                        case "quantity":
                            values[1] = rfOfferChild.getTextContent();
                            break;
                        case "status":
                            values[2] = rfOfferChild.getTextContent();
                            break;
                        case "novelty":
                            values[3] = rfOfferChild.getTextContent();
                            break;
                        case "priority":
                            values[4] = rfOfferChild.getTextContent();
                            break;
                        default:
                            break;
                    }
                }
                if (id != null) result.put(id, values);
            }
        }
        return result;
    }

    /**
     * Method to update parsed nodes with actual parameters from remainderFile
     *
     * @param nodes  list of nodes to update
     * @param update actual information about nodes in corresponding ArrayList
     * @return string with information about update process results
     */
    private static String updateNodes(HashMap<String, Node> nodes, HashMap<String, String[]> update) {
        int count = 0;
        StringBuilder sb = new StringBuilder();
        for (String key : nodes.keySet()) {
            if (update.containsKey(key)) {
                count++;
                String[] updt = update.get(key);
                NodeList childs = nodes.get(key).getChildNodes();
                for (int i = 0; i < childs.getLength(); i++) {
                    String nodeName = childs.item(i).getNodeName();
                    switch (nodeName) {
                        case "price":
                            childs.item(i).setTextContent(updt[0]);
                            break;
                        case "quantity":
                            childs.item(i).setTextContent(updt[1]);
                            break;
                        case "status":
                            childs.item(i).setTextContent(updt[2]);
                            break;
                        case "novelty":
                            childs.item(i).setTextContent(updt[3]);
                            break;
                        case "priority":
                            childs.item(i).setTextContent(updt[4]);
                            break;
                        default:
                            break;
                    }
                }
            } else if (key.length() > 4) {
                if (sb.length() == 0) sb.append("*** В остатках отсутствуют: ");
                sb.append(key);
                sb.append(", ");
            }
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append(".\nЧисло  обновленных записей: ");
        sb.append(count);
        return sb.toString();
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
    private static void pushDocumentToFile(File outputFile, HashMap<String, Node> nodes, int mode)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {
        // Get node list
        Collection<Node> nodeList = nodes.values();
        // Fill files by default (bicycle-bicycle)
        fillFileByDefault(outputFile);
        // Builders for file
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        // DOM tree
        Document document = documentBuilder.parse(outputFile);
        // Get parent element (it was created by fillFileByDefault method)
        Node newNode = document.getDocumentElement();
        for (Node node : nodeList)
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

    private static void writeFile(Document document, File file) throws TransformerException, IOException {
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

    private static void addNodeToFile(Node node, Node newHead, Document document, int mode) {
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
     * Only to print info about file into text area
     *
     * @param file file to print info about
     */
    private static void printFileInfo(File file) {
        window.putLog(String.format("[%8db] Modified %s  %s", file.length(),
                new SimpleDateFormat("YYYY-MM-dd HH:mm").format(new Date(file.lastModified())),
                file.getPath()));
    }

    /**
     * @param file this file will be filled by template
     * @throws IOException IOException
     */
    private static void fillFileByDefault(File file) throws IOException {
        Date nowDate = new Date();
        String outputFileString = "<?xml version =\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<КоммерческаяИнформация ВерсияСхемы=\"2.03\" ДатаФормирования=\"" +
                new SimpleDateFormat("YYYY-MM-dd").format(nowDate) + "\">\n</КоммерческаяИнформация>";

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), "UTF8"));
        bw.write(outputFileString);
        bw.flush();
    }

    /**
     * Method to display information about runtime exception
     *
     * @param t thread, generated exception
     * @param e exception
     */

    private static void reportException(Thread t, Throwable e) {
        String message = "Unexpected situation in thread " + t.getName() + ".\n\t" +
                e.getStackTrace()[0] + " " + e.getMessage();
        JOptionPane.showMessageDialog(null, message, "Exception", JOptionPane.ERROR_MESSAGE);
    }
}
