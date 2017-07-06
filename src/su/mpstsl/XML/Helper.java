package su.mpstsl.XML;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class Helper {
    String headName = "XML helper v 0.01";
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
        try {
            // Looking for all files in defined directory
            File[] files2w = wd.listFiles();
            File wd2 = wd.getParentFile();
            // Defining and creating two output files in ./../
            File outp_d = new File(wd2.getAbsolutePath()+"\\catalogue_products.xml");
            File outp_s = new File(wd2.getAbsolutePath()+"\\products_to_categories.xml");
            if (outp_d.exists()) outp_d.delete();
            if (outp_s.exists()) outp_s.delete();
            outp_d.createNewFile();
            outp_s.createNewFile();
            // Parsing files in work directory one-by-one and writing result in output
            for (File file2w : files2w) {






            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void transform_d (File fileToRead, File fileToWrite) {
        FileInputStream fis;
        FileOutputStream fos;
        try {
            fis = new FileInputStream(fileToRead);
            fos = new FileOutputStream(fileToWrite);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
    private void transform_s (File fileToRead, File fileToWrite) {}

    // File/quit
    private class MyQuitListener implements ActionListener {@Override public void actionPerformed (ActionEvent e) {System.exit(0);}}
    // Start button
    private class MyStartListener implements ActionListener {@Override public void actionPerformed(ActionEvent e) {if (wd != null) startParser();}}
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
