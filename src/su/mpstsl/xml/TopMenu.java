package su.mpstsl.xml;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class TopMenu extends JMenuBar {
    /**
     * Constructor for top pull-down menu
     * @param window frame of main window
     */
    TopMenu(MainWindow window) {
        JMenu fileMenu = new JMenu("File");
        add(fileMenu);
        JMenuItem quit = new JMenuItem("Quit");
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(quit);
    }
}
