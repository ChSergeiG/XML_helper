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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

class Parser {
    private static final int DET_MODE = 0;
    private static final int SUM_MODE = 1;
    private static final int DOUBLES_MODE = 2;

    private static final int OFFER = 0;
    private static final int DOUBLES = 1;

    private static HashMap<String, Node> offerNodes;
    private static HashMap<String, Node> doublesNodes;

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
        offerNodes = buildNodesMap(OFFER);
        doublesNodes = buildNodesMap(DOUBLES);
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

    private static HashMap<String, Node> buildNodesMap(int mode) {
        HashMap<String, Node> result = new HashMap<>();
        try {
            if (mode == OFFER) window.putLog("Обрабатываемые файлы:");
            for (File file2parse : inputFiles) {
                if (mode == OFFER) printFileInfo(file2parse);
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = documentBuilder.parse(file2parse);
                Node head = document.getDocumentElement();
                NodeList nodes = head.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (mode == OFFER && !nodes.item(i).getNodeName().equals("offer")) continue;
                    if (mode == DOUBLES && !nodes.item(i).getNodeName().equals("item")) continue;
                    NodeList childs = nodes.item(i).getChildNodes();
                    String id = null;
                    for (int j = 0; j < childs.getLength(); j++) {
                        if (childs.item(j).getNodeName().equals(TableEntry.tags[0]))
                            id = childs.item(j).getTextContent();
                    }
                    if (id != null) {
                        if (id.length() > 4) {
                            if (mode == OFFER && result.containsKey(id))
                                window.putLog("*** -> " + id + " - ID дубликат");
                            result.put(id, nodes.item(i));
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
     * main-like procedure sequence to read every input file, get offerNodes, parse it and write back to target files.
     */
    static void parseIt() {
        try {
            long start = System.nanoTime();
            int[] modes = {DET_MODE, SUM_MODE, DOUBLES_MODE};
            String[] filePaths = {"\\catalogue_products.xml", "\\products_to_categories.xml", "\\doubles.xml"};
            HashMap<String, String[]> update = buildUpdateMap(remainderFile);
            window.putLog("-------------------------------------\n" + updateNodes(offerNodes, update) +
                    "\nФайлы для загрузки:");
            for (int i = 0; i < filePaths.length; i++) {
                // Define location for output file
                File outputFile = new File(workingDirectory.getParent() + filePaths[i]);
                pushDocumentToFile(outputFile, modes[i]);
                printFileInfo(outputFile);
            }
            window.putLog("-------------------------------------\nВсего обработано уникальных записей: " +
                    offerNodes.size());
            long time = (System.nanoTime() - start) / 1000000;
            window.putLog("Завершено без ошибок за " + time + " мс.");
            offerNodes = null;
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
     * @param remainderFile actual information about offerNodes
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
     * Method to update parsed offerNodes with actual parameters from remainderFile
     *
     * @param nodes  list of offerNodes to update
     * @param update actual information about offerNodes in corresponding ArrayList
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
                update.remove(key);
            } else if (key.length() > 4) {
                if (sb.length() == 0) sb.append("*** В исходниках есть, в остатках отсутствуют: ");
                sb.append(key);
                sb.append(", ");
            }
        }
        if (sb.length() > 2) sb.delete(sb.length() - 2, sb.length());
        sb.append("\n*** В остатках есть, в исходниках отсутствуют: ");
        for (String key : update.keySet()) {
            if (key.length() > 4) {
                sb.append(key);
                sb.append(", ");
            }
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("\nЧисло  обновленных записей: ");
        sb.append(count);
        return sb.toString();
    }

    private static void pushDocumentToFile(File outputFile, int mode)
            throws IOException, ParserConfigurationException, SAXException, TransformerException {
        Collection<Node> nodeList;
        // Get node list
        if (mode == DET_MODE || mode == SUM_MODE)
            nodeList = offerNodes.values();
        else if (mode == DOUBLES_MODE)
            nodeList = doublesNodes.values();
        else {
            reportException(Thread.currentThread(), new RuntimeException("Wrong mode"));
            return;
        }
        // Fill files by default (bicycle-bicycle)
        fillFileByDefault(outputFile);
        // Builders for file
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        // DOM tree
        Document document = documentBuilder.parse(outputFile);
        // Get parent element (it was created by fillFileByDefault method)
        Node documentHead = document.getDocumentElement();
        for (Node node : nodeList)
            addNodeToDocument(node, documentHead, document, mode);
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

        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty(OutputKeys.INDENT, "yes");

        document.setXmlStandalone(true);

        DOMSource source = new DOMSource(document);
        FileOutputStream fos = new FileOutputStream(file);
        StreamResult result = new StreamResult(fos);
        tr.transform(source, result);
        fos.close();
    }

    /**
     * Method to convert node into new formatted offerNodes. After this writes these offerNodes into document
     *
     * @param node     input node ready to convert
     * @param newHead  new node for detailed document
     * @param document detailed document
     * @param mode     mode number here in static section
     */

    private static void addNodeToDocument(Node node, Node newHead, Document document, int mode) {
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
                Element sumID = document.createElement("id");
                Element sumParent = document.createElement("parent");
                Element innerItem = document.createElement("item");
                sumParent.appendChild(innerItem);
                sumNode.appendChild(sumID);
                sumNode.appendChild(sumParent);
                for (int i = 0; i < childList.getLength(); i++) {
                    Node current = childList.item(i);
                    if (current.getNodeName().equals("id"))
                        sumID.setTextContent(current.getTextContent());
                    else if (current.getNodeName().equals("parent"))
                        innerItem.setTextContent(current.getTextContent());
                }
                newHead.appendChild(sumNode);
                break;
            case DOUBLES_MODE:
                // building node for doubles file
                Element doublNode = document.createElement("item");
                Element doublID = document.createElement("id");
                Element doublParent = document.createElement("parent");
                doublNode.appendChild(doublID);
                doublNode.appendChild(doublParent);
                for (int i = 0; i < childList.getLength(); i++) {
                    if (childList.item(i).getNodeName().equals("id"))
                        doublID.setTextContent(childList.item(i).getTextContent());
                    if (childList.item(i).getNodeName().equals("parent")) {
                        NodeList parentItemList = childList.item(i).getChildNodes();
                        for (int j = 0; j < parentItemList.getLength(); j++) {
                            if (parentItemList.item(j).getNodeName().equals("item")) {
                                Element innerParentItem = document.createElement("item");
                                innerParentItem.setTextContent(parentItemList.item(j).getTextContent());
                                doublParent.appendChild(innerParentItem);
                            }
                        }
                    }
                }
                newHead.appendChild(doublNode);
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
        window.putLog(String.format("[%6dkb] Modified %s  @%s", 1 + file.length() / 1024,
                new SimpleDateFormat("YYYY-MM-dd HH:mm").format(new Date(file.lastModified())),
                file.getPath().substring(workingDirectory.getParent().length())));
    }

    /**
     * @param file this file will be filled by template
     * @throws IOException IOException
     */
    private static void fillFileByDefault(File file) throws IOException {
        Date nowDate = new Date();
        String outputFileString = "<?xml version =\"1.0\" encoding=\"UTF-8\"?>\n<КоммерческаяИнформация ВерсияСхемы=\"2.03\" ДатаФормирования=\"" +
                new SimpleDateFormat("YYYY-MM-dd").format(nowDate) + "\"></КоммерческаяИнформация>";

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
