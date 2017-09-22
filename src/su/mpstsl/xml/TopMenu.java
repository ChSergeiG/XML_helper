package su.mpstsl.xml;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class TopMenu extends JMenuBar {
    /**
     * Constructor for top pull-down menu
     *
     * @param window frame of main window
     */
    TopMenu(MainWindow window) {
        JMenu jmFile = new JMenu("File");
        add(jmFile);
        JMenuItem jmiQuit = new JMenuItem("Quit");
        jmFile.add(jmiQuit);
        JMenu jmInfo = new JMenu("Info");
        add(jmInfo);
        JMenuItem jmiAbout = new JMenuItem("About");
        jmInfo.add(jmiAbout);
        jmiQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }
}
