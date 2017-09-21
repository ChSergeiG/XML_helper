package su.mpstsl.xml;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

class Workshop extends JPanel {

    File wd;
    JTextArea textArea;
    Parser parser;
    MainWindow window;

    Workshop(MainWindow window) {
        this.window = window;
        BorderLayout layout = new BorderLayout();
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textArea = new JTextArea(18,70);
        JScrollPane textAreaWithScroll = new JScrollPane (textArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textArea.setEditable(false);
        textArea.append("----------------------------------------------------------------------\n");
        textArea.setText("");
        textArea.setFont(new Font("Consolas",Font.PLAIN,14));
        JButton wFlsBut = new JButton("Files");
        JButton parseWD = new JButton("Parse");
        JButton clearTArea = new JButton("Clear");
        Box center = new Box(BoxLayout.Y_AXIS);
        add(BorderLayout.CENTER, center);
        center.add(textAreaWithScroll);
        //
        Box south = new Box(BoxLayout.X_AXIS);
        add(BorderLayout.SOUTH, south);
        south.add(wFlsBut);
        south.add(parseWD);
        south.add(clearTArea);

        wFlsBut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser setWD = new JFileChooser();
                setWD.setDialogType(JFileChooser.OPEN_DIALOG);
                setWD.showDialog(window, "This one");
                wd = setWD.getCurrentDirectory();
            }
        });
        parseWD.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (wd != null) {
                    parser = new Parser(window);
                    parser.startParser(wd);
                }
            }
        });
        clearTArea.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });
    }
}
