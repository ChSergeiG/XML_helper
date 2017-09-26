package su.mpstsl.xml;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class TopMenu extends JMenuBar implements ActionListener {
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
        JMenu jmSettings = new JMenu("Settings");
        add(jmSettings);
        JMenuItem jmiAbout = new JMenuItem("About");
        jmSettings.add(jmiAbout);
        jmiQuit.addActionListener(this);
        jmiAbout.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        switch (e.getActionCommand()) {
            case "Quit":
                System.exit(0);
            case "About":
                break;
            default:
                break;
        }
    }
}
