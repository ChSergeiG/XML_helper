package su.mpstsl.xml;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainWindow extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {

    private static final int POS_X = 100;
    private static final int POS_Y = 10;
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 700;
    private static final String WINDOW_NAME = "XML helper v 0.28.1";

    private final JButton jbFiles = new JButton("Указать файлы");
    private final JButton jbExcFile = new JButton("Указать файл остатков");
    private final JButton jbParse = new JButton("Преобразовать");
    private final JButton jbClear = new JButton("Очистить");
    private final JButton jbClose = new JButton("Закрыть");

    private final JMenuItem jmiQuit = new JMenuItem("Выход");
    private final JMenuItem jmiAbout = new JMenuItem("Об апплете");
    private final JTextArea jtaLog = new JTextArea();

    private File workingDirectory = null;
    private File remainderFile = null;

    /**
     * Main window constructor
     */
    private MainWindow() {
        setLocation(POS_X, POS_Y);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setTitle(WINDOW_NAME);

        JMenuBar jmTopBar = new JMenuBar();
        JMenu jmFile = new JMenu("Файл");
        JMenu jmSettings = new JMenu("Настройки");

        jmTopBar.add(jmFile);
        jmFile.add(jmiQuit);
        jmTopBar.add(jmSettings);
        jmSettings.add(jmiAbout);

        JPanel jpCentralPanel = new JPanel();
        jpCentralPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane jspTextArea = new JScrollPane(jtaLog, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        jspTextArea.setPreferredSize(new Dimension(WINDOW_WIDTH - 50, WINDOW_HEIGHT - 150));
        jpCentralPanel.add(jspTextArea);
        jtaLog.setEditable(false);
        jtaLog.setFont(new Font("Consolas", Font.PLAIN, 12));

        JPanel jpBottomPanel = new JPanel();
        jpBottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jpBottomPanel.add(jbFiles);
        jpBottomPanel.add(jbExcFile);
        jpBottomPanel.add(jbParse);
        jpBottomPanel.add(jbClear);
        jpBottomPanel.add(jbClose);
        jbParse.setEnabled(false);

        addListeners();

        setJMenuBar(jmTopBar);
        add(jpCentralPanel, BorderLayout.CENTER);
        add(jpBottomPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setVisible(true);
    }

    private void addListeners() {
        jmiQuit.addActionListener(this);
        jmiAbout.addActionListener(this);
        jbFiles.addActionListener(this);
        jbExcFile.addActionListener(this);
        jbParse.addActionListener(this);
        jbClear.addActionListener(this);
        jbClose.addActionListener(this);
    }

    /**
     * Standart entry point
     *
     * @param args command line parameters.
     */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }

    void putLog(String message) {
        jtaLog.append(message + "\n");
    }

    /**
     * Active elements actions
     *
     * @param e event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(jbFiles)) {
            chooseFilesDirectory();
        } else if (source.equals(jbExcFile)) {
            chooseRemaindersFile();
        } else if (source.equals(jbParse)) {
           parse();
        } else if (source.equals(jbClear)) {
            clearLog();
        } else if (source.equals(jbClose) || source.equals(jmiQuit)) {
            System.exit(0);
        } else if (source.equals(jmiAbout)) {
            showInfo();
        } else
            uncaughtException(Thread.currentThread(),
                    new RuntimeException("Неверное действие (" + e.getActionCommand() + ")"));
    }

    /**
     * Method to invoke info window
     */
    private void showInfo() {
        JOptionPane.showMessageDialog(
                this,
                "Пока что никакой\nинформации тут нет.\nДа и вряд ли будет.",
                "^^(,oO,)^^",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Clear log window
     */
    private void clearLog() {
        jtaLog.setText("");
    }

    /**
     * Method to do parse
     */
    private void parse() {
        if (workingDirectory != null) {
            Parser.initConstants(this, workingDirectory, remainderFile);
            Parser.parseIt();
            workingDirectory = null;
            remainderFile = null;
            jbParse.setEnabled(false);
        }
    }

    /**
     * Method to choose remainders xml file
     */
    private void chooseRemaindersFile() {
        JFileChooser setEF = new JFileChooser(System.getProperty("user.home"));
        setEF.setDialogType(JFileChooser.OPEN_DIALOG);
        setEF.showDialog(this, "Выбрать файл");
        remainderFile = setEF.getSelectedFile();
        putLog("Файл остатков: " + remainderFile.getPath());
        jbParse.setEnabled(workingDirectory != null && remainderFile != null);
    }

    /**
     * Method to select directory with source xml tree
     */
    private void chooseFilesDirectory() {
        JFileChooser setWD = new JFileChooser(System.getProperty("user.home"));
        setWD.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        setWD.setDialogType(JFileChooser.OPEN_DIALOG);
        setWD.showDialog(this, "Выбрать папку");
        workingDirectory = setWD.getSelectedFile();
        putLog("Директория с файлами: " + workingDirectory.getPath());
        jbParse.setEnabled(workingDirectory != null && remainderFile != null);
    }

    /**
     * Unexpected situation. What to do?
     *
     * @param t thread, generated exception
     * @param e exception
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String message = "В потоке '" + t.getName() + "' произошло что-то неожиданное.\n\tА именно: " + e.getMessage();
        JOptionPane.showMessageDialog(null, message, "Exception", JOptionPane.ERROR_MESSAGE);
    }
}
