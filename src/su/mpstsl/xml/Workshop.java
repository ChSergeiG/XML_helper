package su.mpstsl.xml;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

class Workshop extends JPanel implements ActionListener, Thread.UncaughtExceptionHandler {

    private File wd;
    JTextArea textArea = new JTextArea(20, 75);
    private Parser parser;

    private final JButton jbFiles = new JButton("Files");
    private final JButton jbParse = new JButton("Parse");
    private final JButton jbClear = new JButton("Clear");
    private final JButton jbClose = new JButton("Close");

    private MainWindow window;

    /**
     * Constructor for  general workspace
     *
     * @param window frame of main window to work with
     */
    Workshop(MainWindow window) {
        this.window = window;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane jspTextArea = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textArea.setEditable(false);
        textArea.append("---------------------------------------------------------------------------\n");
        textArea.setText("");
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        jbParse.setEnabled(false);
        Box bCenter = new Box(BoxLayout.Y_AXIS);
        add(BorderLayout.CENTER, bCenter);
        bCenter.add(jspTextArea);
        Box bSouth = new Box(BoxLayout.X_AXIS);
        add(BorderLayout.SOUTH, bSouth);
        bSouth.add(jbFiles);
        bSouth.add(jbParse);
        bSouth.add(jbClear);
        bSouth.add(jbClose);
        jbFiles.addActionListener(this);
        jbParse.addActionListener(this);
        jbClear.addActionListener(this);
        jbClose.addActionListener(this);
    }

    /**
     * Method to disable "parse" button
     */

    void setParseButtonDisabled() {
        jbParse.setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(jbFiles)) {
            JFileChooser setWD = new JFileChooser(System.getProperty("user.home"));
            setWD.setDialogType(JFileChooser.OPEN_DIALOG);
            setWD.showDialog(window, "This one");
            wd = setWD.getCurrentDirectory();
            textArea.append("Selected path: " + wd.getPath() + "\n");
            jbParse.setEnabled(true);
        } else if (source.equals(jbParse)) {
            if (wd != null) {
                parser = new Parser(window, wd);
                parser.parseIt();
            }
        } else if (source.equals(jbClear)) {
            textArea.setText("");
        } else if (source.equals(jbClose)) {
            System.exit(0);
        } else {
            uncaughtException(Thread.currentThread(),
                    new RuntimeException("Unexpected action (" + e.getActionCommand() + ")"));
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String message = "Unexpected situation in thread " + t.getName() + ".\n\t" + e.getMessage();
        JOptionPane.showMessageDialog(null, message, "Exception", JOptionPane.ERROR_MESSAGE);
    }
}
