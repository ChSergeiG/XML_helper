package su.mpstsl.xml;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

class Parser {

    private ArrayList<TableEntry> entries;
    private MainWindow window;
    private JTextArea outputArea;

    Parser(MainWindow window) {
        this.window = window;
        entries = new ArrayList<>();
        outputArea = window.workshop.textArea;
    }

    void startParser(File wd) {
        File[] inputFiles = wd.listFiles();
        int numberOfInputFiles = inputFiles.length;
        outputArea.append("Files to parse:\n");
        for (File file : inputFiles)
            outputArea.append(file.getAbsolutePath() + "\n");

        outputArea.append("-------------------------------------\n");
        File outputDetailedFile = new File(wd.getParent() + "\\catalogue_products.xml");
        File outputSummaryFile = new File(wd.getParent() + "\\products_to_categories.xml");
        outputArea.append("Output files:\n" + outputDetailedFile.getAbsolutePath() + "\n" +
                outputSummaryFile.getAbsolutePath() + "\n" + "-------------------------------------\n");


    }
}
