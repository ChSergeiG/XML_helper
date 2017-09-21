/**
 * Main window
 */
package su.mpstsl.xml;

import javax.swing.*;

public class MainWindow extends JFrame {

    private static final int POS_X = 400;
    private static final int POS_Y = 200;
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 440;
    private static final String WINDOW_NAME = "XML helper v 0.10";

    Workshop workshop;

    /**
     * Standart entry point
     *
     * @param args command line parameters.
     */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow();
            }
        });
    }

    /**
     * Main window constructor
     */
    private MainWindow() {
        setLocation(POS_X, POS_Y);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setTitle(WINDOW_NAME);
        setJMenuBar(new TopMenu(this));
        workshop = new Workshop(this);
        add(workshop);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }
}
