package su.mpstsl.XML;

import javax.swing.*;
import java.awt.*;

public class Helper {
    String headName = "XML helper v 0.01";
    JFrame frame = new JFrame(headName);
    BorderLayout layout;
    JPanel mainPanel;
    JMenu filemenu;
    JMenuBar menuBar;

    private void SetUpGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        layout = new BorderLayout();
        mainPanel = new JPanel(layout);
        menuBar = new JMenuBar();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        menuBar.add(filemenu);












        frame.setJMenuBar(menuBar);
        frame.setBounds(50, 50, 300, 300);
        frame.pack();
        frame.setVisible(true);



    }
    public static void main(String[] args) {
        Helper hlp = new Helper();
        hlp.SetUpGUI();
    }
}
