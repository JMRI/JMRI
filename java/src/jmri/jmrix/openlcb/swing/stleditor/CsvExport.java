package jmri.jmrix.openlcb.swing.stleditor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import jmri.util.FileUtil;
import jmri.jmrix.openlcb.swing.stleditor.StlEditorPane;
import jmri.jmrix.openlcb.swing.stleditor.StlEditorPane.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CsvExport {

    private static String FILE_DIRECTORY_PATH = FileUtil.getUserFilesPath() + "stl-exports" + FileUtil.SEPARATOR;

//             FileOutputStream out = new FileOutputStream(jmri.util.FileUtil.getUserFilesPath() + "STL Editor.properties");

    public CsvExport() {
    }

    static List<String> exportData(StlEditorPane pane) {
        List<String> messages = new ArrayList<>();
        FileUtil.createDirectory(FILE_DIRECTORY_PATH);

        try {
            CsvExport.exportGroupLogic(pane._groupList);
            CsvExport.exportInputs(pane._inputList);
            CsvExport.exportOutputs(pane._outputList);
            CsvExport.exportReceivers(pane._receiverList);
            CsvExport.exportTransmitters(pane._transmitterList);
        } catch (IOException ex) {
            messages.add(String.format("CSV Export error: %s", ex.getMessage()));
        }
        messages.add("Export completed");
        return messages;
    }

    static void exportGroupLogic(List groupList) throws IOException {
        var fileWriter = new FileWriter(FILE_DIRECTORY_PATH + "group_logic.csv");
        var bufferedWriter = new BufferedWriter(fileWriter);
        var csvFile = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT);

        csvFile.printRecord(Bundle.getMessage("GroupName"), Bundle.getMessage("ColumnLabel"),
                 Bundle.getMessage("ColumnOper"), Bundle.getMessage("ColumnName"), Bundle.getMessage("ColumnComment"));

        for (int i = 0; i < 16; i++) {
            var row = (GroupRow) groupList.get(i);
            var groupName = row.getName();
            csvFile.printRecord(groupName);
            var logicRow = row.getLogicList();
            for (LogicRow logic : logicRow) {
                var operName = logic.getOperName();
                csvFile.printRecord("", logic.getLabel(), operName, logic.getName(), logic.getComment());
            }
        }

        // Flush the write buffer and close the file
        csvFile.flush();
        csvFile.close();
    }

    static void exportInputs(List inputList) throws IOException {
        var fileWriter = new FileWriter(FILE_DIRECTORY_PATH + "inputs.csv");
        var bufferedWriter = new BufferedWriter(fileWriter);
        var csvFile = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT);

        csvFile.printRecord(Bundle.getMessage("ColumnInput"), Bundle.getMessage("ColumnName"),
                 Bundle.getMessage("ColumnTrue"), Bundle.getMessage("ColumnFalse"));

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                var variable = "I" + i + "." + j;
                var row = (InputRow) inputList.get((i * 8) + j);
            csvFile.printRecord(variable, row.getName(), row.getEventTrue(), row.getEventFalse());
            }
        }

        // Flush the write buffer and close the file
        csvFile.flush();
        csvFile.close();
    }

    static void exportOutputs(List outputList) throws IOException {
        var fileWriter = new FileWriter(FILE_DIRECTORY_PATH + "outputs.csv");
        var bufferedWriter = new BufferedWriter(fileWriter);
        var csvFile = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT);

        csvFile.printRecord(Bundle.getMessage("ColumnOutput"), Bundle.getMessage("ColumnName"),
                 Bundle.getMessage("ColumnTrue"), Bundle.getMessage("ColumnFalse"));

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                var variable = "Q" + i + "." + j;
                var row = (OutputRow) outputList.get((i * 8) + j);
            csvFile.printRecord(variable, row.getName(), row.getEventTrue(), row.getEventFalse());
            }
        }

        // Flush the write buffer and close the file
        csvFile.flush();
        csvFile.close();
    }

    static void exportReceivers(List receiverList) throws IOException {
        var fileWriter = new FileWriter(FILE_DIRECTORY_PATH + "receivers.csv");
        var bufferedWriter = new BufferedWriter(fileWriter);
        var csvFile = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT);

        csvFile.printRecord(Bundle.getMessage("ColumnCircuit"), Bundle.getMessage("ColumnName"),
                 Bundle.getMessage("ColumnEventID"));

        for (int i = 0; i < 16; i++) {
            var variable = "Y" + i;
            var row = (ReceiverRow) receiverList.get(i);
            csvFile.printRecord(variable, row.getName(), row.getEventId());
        }

        // Flush the write buffer and close the file
        csvFile.flush();
        csvFile.close();
    }

    static void exportTransmitters(List transmitterList) throws IOException {
        var fileWriter = new FileWriter(FILE_DIRECTORY_PATH + "transmitters.csv");
        var bufferedWriter = new BufferedWriter(fileWriter);
        var csvFile = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT);

        csvFile.printRecord(Bundle.getMessage("ColumnCircuit"), Bundle.getMessage("ColumnName"),
                 Bundle.getMessage("ColumnEventID"));

        for (int i = 0; i < 16; i++) {
            var variable = "Z" + i;
            var row = (TransmitterRow) transmitterList.get(i);
            csvFile.printRecord(variable, row.getName(), row.getEventId());
        }

        // Flush the write buffer and close the file
        csvFile.flush();
        csvFile.close();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CsvExport.class);
}
