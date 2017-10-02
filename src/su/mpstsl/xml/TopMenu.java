package su.mpstsl.xml;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class TopMenu extends JMenuBar implements ActionListener, Thread.UncaughtExceptionHandler {


    private final JMenuItem jmiQuit = new JMenuItem("Quit");
    private final JMenuItem jmiAbout = new JMenuItem("About");
    private final MainWindow window;

    /**
     * Constructor for top pull-down menu
     *
     * @param window frame of main window
     */
    TopMenu(MainWindow window) {
        this.window = window;
        JMenu jmFile = new JMenu("File");
        add(jmFile);
        jmFile.add(jmiQuit);
        JMenu jmSettings = new JMenu("Settings");
        add(jmSettings);
        jmSettings.add(jmiAbout);
        jmiQuit.addActionListener(this);
        jmiAbout.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        Object source = e.getSource();
        if (source.equals(jmiQuit)) {
            System.exit(0);
        } else if (source.equals(jmiAbout)) {
            JOptionPane.showMessageDialog(window, "No info", "No title -_-",
                    JOptionPane.INFORMATION_MESSAGE);
        } else
            uncaughtException(Thread.currentThread(), new RuntimeException("Unexpected action"));
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String message = "Unexpected situation in thread " + t.getName() + ".\n\t" + e.getMessage();
        JOptionPane.showMessageDialog(null, message, "Exception", JOptionPane.ERROR_MESSAGE);
    }
}
