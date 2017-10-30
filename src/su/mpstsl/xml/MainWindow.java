/**
 * Main window
 */
package su.mpstsl.xml;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainWindow extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler {

    private static final int POS_X = 400;
    private static final int POS_Y = 200;
    private static final int WINDOW_WIDTH = 700;
    private static final int WINDOW_HEIGHT = 500;
    private static final String WINDOW_NAME = "XML helper v 0.20";

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
    private Parser parser;

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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow();
            }
        });
    }

    void putLog(String message) {
        jtaLog.append(message + "\n");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(jbFiles)) {
//            JFileChooser setWD = new JFileChooser(System.getProperty("user.home"));
//            setWD.setDialogType(JFileChooser.OPEN_DIALOG);
//            setWD.showDialog(this, "This one");
//            workingDirectory = setWD.getCurrentDirectory();
            workingDirectory = new File("C:\\Users\\chser\\IdeaProjects\\XML_helper\\targets\\in\\");

            putLog("Директория с файлами: " + workingDirectory.getPath());
            jbParse.setEnabled(!workingDirectory.equals(null) && !remainderFile.equals(null));
        } else if (source.equals(jbExcFile)) {
//            JFileChooser setEF = new JFileChooser(System.getProperty("user.home"));
//            setEF.setDialogType(JFileChooser.OPEN_DIALOG);
//            setEF.showDialog(this, "This one");
//            remainderFile = setEF.getSelectedFile();

            remainderFile = new File("C:\\Users\\chser\\IdeaProjects\\XML_helper\\targets\\in\\catalogue_products_short.xml");
            putLog("Файл остатков: " + remainderFile.getPath());
            jbParse.setEnabled(!workingDirectory.equals(null) && !remainderFile.equals(null));
        } else if (source.equals(jbParse)) {
            if (workingDirectory != null) {
                parser = new Parser(this, workingDirectory, remainderFile);
                parser.parseIt();
                workingDirectory = null;
                remainderFile = null;
                jbParse.setEnabled(false);
            }
        } else if (source.equals(jbClear)) {
            jtaLog.setText("");
        } else if (source.equals(jbClose)) {
            System.exit(0);
        } else if (source.equals(jmiQuit)) {
            System.exit(0);
        } else if (source.equals(jmiAbout)) {
            JOptionPane.showMessageDialog(this, "Пока что никакой\nинформации тут нет.\nДа и вряд ли будет.", "^^(,oO,)^^",
                    JOptionPane.INFORMATION_MESSAGE);
        } else
            uncaughtException(Thread.currentThread(),
                    new RuntimeException("Неверное действие (" + e.getActionCommand() + ")"));
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String message = "В потоке " + t.getName() + " произошло что-то неожиданное.\n\tА именно: " + e.getMessage();
        JOptionPane.showMessageDialog(null, message, "Exception", JOptionPane.ERROR_MESSAGE);
    }
}
