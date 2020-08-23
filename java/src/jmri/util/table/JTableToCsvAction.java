package jmri.util.table;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import jmri.util.FileUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save a JTable or AbstractTableModel to CSV file after prompting for filename.
 * <p>
 * First line contains Column Headings.
 * Save order can replicate current JTable sort, filters, 
 * visible columns and column order.
 * Entire Table Model can be saved by not specifying a JTable.
 * Can exclude specific columns ( e.g. JButtons ) from the save.
 *
 * @author Steve Young Copyright (C) 2020
 * @since 4.19.5
 */ 
public class JTableToCsvAction extends AbstractAction {

    private final JFileChooser _fileChooser;
    private File _saveFile;
    private final JTable _table;
    private final TableModel _model;
    private final String _defaultFileName;
    private String _saveFileName;
    private final int[] _excludedCols;
    private List<Integer> modelColList;
    private List<Integer> modelRowList;
    
    /**
     * Create a new Save to CSV Action.
     * 
     * @param actionName Action Name
     * @param jtable to save the view, else null for whole table.
     * @param model Table Model to use.
     * @param defaultFileName File Name to use as default.
     * @param excludedCols int Array of Table Model columns to exclude.
     */
    public JTableToCsvAction(String actionName, JTable jtable, @Nonnull TableModel model, 
        @Nonnull String defaultFileName, @Nonnull int[] excludedCols ){
        super(actionName);
        _table = jtable;
        _model = model;
        _defaultFileName = defaultFileName;
        _excludedCols = excludedCols;
        _fileChooser = new JFileChooser(FileUtil.getUserFilesPath());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        
        _fileChooser.setFileFilter(new FileNameExtensionFilter(Bundle.getMessage("CSVFileLabel"), "csv"));
        _fileChooser.setSelectedFile(new File(_defaultFileName));
        
        // handle selection or cancel
        if (_fileChooser.showSaveDialog(_table) == JFileChooser.APPROVE_OPTION) {
            _saveFileName = _fileChooser.getSelectedFile().getPath();
            if (!_saveFileName.regionMatches(true,_saveFileName.length()-4,".csv",0,4)) { // 
                _saveFileName += ".csv";
            }
            _saveFile = new File(_saveFileName);
            if (continueIfExisting()) {
                saveToCSV();
            }
        }
    }
    
    private boolean continueIfExisting(){
        return !(_saveFile.isFile() && ( JOptionPane.showConfirmDialog(_table,
            Bundle.getMessage("ConfirmOverwriteFile"),
            Bundle.getMessage("ConfirmQuestion"), JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE)
            != JOptionPane.YES_OPTION));
    }

    private void saveToCSV() {
        
        createColumnsAndRows();
        
        try (CSVPrinter p = new CSVPrinter(new OutputStreamWriter(
            new FileOutputStream(_saveFileName), StandardCharsets.UTF_8), CSVFormat.DEFAULT)) {
            
            addToFile(p);
            
            p.flush();
            p.close();
        } catch (IOException e) {
            log.error("Error Saving Table to CSV File {}",e.toString());
        }
    }
    
    private void addToFile(CSVPrinter p) throws IOException {
    
        // Save table per row. _saveFileName
        // print header labels
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < modelColList.size(); i++) {
            headers.add(_model.getColumnName(modelColList.get(i)));
        }
        p.printRecord(headers);

        // print rows
        for (int i = 0; i < modelRowList.size(); i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < modelColList.size(); j++) {
                String val = String.valueOf( _model.getValueAt(modelRowList.get(i), modelColList.get(j)));
                if (val.equals("null")) {
                    val = "";
                }
                row.add(val);
            }
            p.printRecord(row);
        }
    
    }
    
    private void createColumnsAndRows(){
    
        modelColList = new ArrayList<>();
        modelRowList = new ArrayList<>();
        
        if ( _table != null ) {
            for (int i = 0; i < _table.getColumnCount(); i++) {
                addColtoListIfNotExcluded(modelColList,_table.convertColumnIndexToModel(i),_excludedCols);
            }
            for (int i = 0; i < _table.getRowCount(); i++) {
                modelRowList.add(_table.convertRowIndexToModel(i));
            }
        } else {
            for (int i = 0; i < _model.getColumnCount(); i++) {
                addColtoListIfNotExcluded(modelColList,i,_excludedCols);
            }
            for (int i = 0; i < _model.getRowCount(); i++) {
                modelRowList.add(i);
            }
        }
    
    }
    
    private void addColtoListIfNotExcluded(@Nonnull List<Integer> list, 
        int modelCol, @Nonnull int[] colsToNotSave){
        if (! Arrays.stream(colsToNotSave).anyMatch(j -> j == modelCol)){
            list.add(modelCol);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    private final static Logger log = LoggerFactory.getLogger(JTableToCsvAction.class);
    
}
