package su.mpstsl.xml;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

class Workshop extends JPanel implements ActionListener {

    private File wd;
    JTextArea textArea;
    private Parser parser;
    private JButton btnParse;
    private MainWindow window;

    /**
     * Constructor for  general workspace
     *
     * @param window frame of main window to work with
     */
    Workshop(MainWindow window) {
        this.window = window;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textArea = new JTextArea(20, 75);
        JScrollPane scrlTextAres = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textArea.setEditable(false);
        textArea.append("---------------------------------------------------------------------------\n");
        textArea.setText("");
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JButton btnFiles = new JButton("Files");
        btnParse = new JButton("Parse");
        JButton btnClearTextArea = new JButton("Clear");
        JButton btnClose = new JButton("Close");
        btnParse.setEnabled(false);
        Box center = new Box(BoxLayout.Y_AXIS);
        add(BorderLayout.CENTER, center);
        center.add(scrlTextAres);
        Box south = new Box(BoxLayout.X_AXIS);
        add(BorderLayout.SOUTH, south);
        south.add(btnFiles);
        south.add(btnParse);
        south.add(btnClearTextArea);
        south.add(btnClose);
        btnFiles.addActionListener(this);
        btnParse.addActionListener(this);
        btnClearTextArea.addActionListener(this);
        btnClose.addActionListener(this);
    }

    /**
     * Method to disable "parse" button
     */

    void setParseButtonDisabled() {
        btnParse.setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Files":
                JFileChooser setWD = new JFileChooser(System.getProperty("user.home"));
                setWD.setDialogType(JFileChooser.OPEN_DIALOG);
                setWD.showDialog(window, "This one");
                wd = setWD.getCurrentDirectory();
                textArea.append("Selected path: " + wd.getPath() + "\n");
                btnParse.setEnabled(true);
                break;
            case "Parse":
                if (wd != null) {
                    parser = new Parser(window, wd);
                    parser.parseIt();
                }
                break;
            case "Clear":
                textArea.setText("");
                break;
            case "Close":
                System.exit(0);
                break;
            default:
                break;
        }
    }
}
