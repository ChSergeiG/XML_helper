package su.mpstsl.XML;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Helper {
    String headName = "XML helper v 0.02";
    JFrame frame = new JFrame(headName);
    BorderLayout layout;
    JPanel mainPanel;
    JMenu fileMenu;
    JMenuBar menuBar;
    File wd;

    private void setUpGUI() {
        // GUI setup
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        layout = new BorderLayout();
        mainPanel = new JPanel(layout);
        menuBar = new JMenuBar();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        Box center = new Box(BoxLayout.Y_AXIS);
        Box south = new Box(BoxLayout.X_AXIS);
        mainPanel.add(BorderLayout.CENTER,center);
        mainPanel.add(BorderLayout.SOUTH,south);
        fileMenu = new JMenu("File");
        JMenuItem quit = new JMenuItem("Quit");
        JButton wFlsBut = new JButton("Files");
        JButton parseWD = new JButton("Parse");
        quit.addActionListener(new MyQuitListener());
        wFlsBut.addActionListener(new MyWdirListener());
        parseWD.addActionListener(new MyStartListener());
        menuBar.add(fileMenu);
        fileMenu.add(quit);
        south.add(wFlsBut);
        south.add(parseWD);
        frame.getContentPane().add(mainPanel);
        frame.setJMenuBar(menuBar);
        frame.setBounds(50, 50, 300, 300);
        frame.pack();
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        Helper hlp = new Helper();
        hlp.setUpGUI();
    }
    private void startParser() {
        File[] inputFiles = wd.listFiles();
        File outputDetailedFile = new File(wd.getParent() + "\\catalogue_products.xml");
        File outputSummaryFile = new File(wd.getParent()+ "\\products_to_categories.xml");
        int numberOfInputFiles = inputFiles.length;
        Document[] inputDocuments = new Document[numberOfInputFiles];
        NodeList[] inputNodeLists = new NodeList[numberOfInputFiles];
        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        try {
            if (outputDetailedFile.exists()) outputDetailedFile.delete(); outputDetailedFile.createNewFile();
            if (outputSummaryFile.exists()) outputSummaryFile.delete(); outputSummaryFile.createNewFile();
            fillFileByDefault(outputDetailedFile);
            fillFileByDefault(outputSummaryFile);

            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            for (int i = 0; i < numberOfInputFiles; i++) {
                inputDocuments[i] = db.parse(inputFiles[i]);
                inputNodeLists[i] = inputDocuments[i].getElementsByTagName("offer");
            }
            Document outputDetailedDocument = db.parse(outputDetailedFile);
            Document outputSummaryDocument = db.parse(outputSummaryFile);
            NodeList outputDetailedNodeList = outputDetailedDocument.getElementsByTagName("offer");
            NodeList outputSummaryNodeList = outputSummaryDocument.getElementsByTagName("offer");

            for (int i = 0; i < numberOfInputFiles; i++) {
                for (int j = 0; j < inputNodeLists[i].getLength(); j++) {

                }
            }







        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static Node getChildrenByNodeName(Node node, String nodeName) {
//        for (Node childNode = node.getFirstChild(); childNode != null;) {
//            Node nextChild = childNode.getNextSibling();
//            if (childNode.getNodeName().equalsIgnoreCase(nodeName)) {
//                return childNode;
//            }
//            childNode = nextChild;
//        }
//        return null;
//    }

    private static void fillFileByDefault(File f) {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
        Date nowDate = new Date();
        String outputFilesHeader = "<?xml version =\"1.0\" encoding=\"UTF-8\"?>\n<КоммерческаяИнформация ВерсияСхемы=\"2.03\" ДатаФормирования=\"";
        String outputFileDate = sdf.format(nowDate);
        String outputFilesFooter = "\">\n\n</КоммерческаяИнформация>";
        try {
            FileWriter outputFW  = new FileWriter(f);
            outputFW.write(outputFilesHeader+outputFileDate+outputFilesFooter);
            outputFW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // File/quit
    private class MyQuitListener implements ActionListener {
        @Override
        public void actionPerformed (ActionEvent e) {
            System.exit(0);
        }
    }
    // Start button
    private class MyStartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (wd != null) startParser();
        }
    }
    // Change working directory button
    private class MyWdirListener implements  ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser setWD = new JFileChooser();
            setWD.setDialogType(JFileChooser.FILES_AND_DIRECTORIES);
            setWD.showDialog(frame,"This one");
            wd = setWD.getCurrentDirectory();
        }
    }
}
