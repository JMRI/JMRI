package jmri.jmrit.logix;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Allows user to decide if (and which) SpeedProfiles to write to the Roster at 
 * the end of a session.  Locos running warrants have had their speeds measured
 * and this new data may or may not be merged into any existing SpeedProfiles
 * in the Roster.
 * <p>
 *
 * @author Pete cressman Copyright (C) 2017
 */
public class SpeedProfilePanel extends JPanel {

    JTable _table;
    JScrollPane _scrollPane;
    static java.awt.Color myRed = new java.awt.Color(255, 120, 120);
    static String entryFlavorType =  DataFlavor.javaJVMLocalObjectMimeType + ";class=java.util.AbstractMap";
    DataFlavor _entryFlavor; 
    
    /**
     * @param speedProfile a RosterSpeedProfile
     * @param editable allow editing.
     * @param anomalies map of entries where speed decreases from previous speed
     */
    public SpeedProfilePanel(RosterSpeedProfile speedProfile, boolean editable, HashMap<Integer, Boolean> anomalies) {
        SpeedTableModel model = new SpeedTableModel(speedProfile, editable, anomalies);
        _table = new JTable(model);
        int tablewidth = 0;
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = _table.getColumnModel().getColumn(i);
            int width = model.getPreferredWidth(i);
            column.setPreferredWidth(width);
            tablewidth += width;
        }
        if (editable) {
            _table.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent ke) {
                    char ch = ke.getKeyChar(); 
                    if (ch == KeyEvent.VK_DELETE || ch == KeyEvent.VK_X) {
                        deleteRow();
                    } else if (ch == KeyEvent.VK_ENTER) {
                        int row = _table.getEditingRow();
                        if (row < 0) {
                            row = _table.getSelectedRow();
                        }
                        if (row >= 0) {
                            rePack(row);
                        }
                    }
                }
                @Override
                public void keyPressed(KeyEvent e) {}
                @Override
                public void keyReleased(KeyEvent e) {}
            });
            _table.getColumnModel().getColumn(SpeedTableModel.FORWARD_SPEED_COL).setCellRenderer(new ColorCellRenderer());
            _table.getColumnModel().getColumn(SpeedTableModel.REVERSE_SPEED_COL).setCellRenderer(new ColorCellRenderer());
        }
//        _table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
       _scrollPane = new JScrollPane(_table);
        int barWidth = 5+_scrollPane.getVerticalScrollBar().getPreferredSize().width;
        tablewidth += barWidth;
        _scrollPane.setPreferredSize(new Dimension(tablewidth, tablewidth));
        try {
            _entryFlavor = new DataFlavor(entryFlavorType);
            if (editable) {
                _table.setTransferHandler(new ImportEntryTranferHandler());                    
                _table.setDragEnabled(true);
            } else {
                _table.setTransferHandler(new ExportEntryTranferHandler());                    
                _table.setDragEnabled(true);
            }
        } catch (ClassNotFoundException cnfe) {
            log.error("SpeedProfilePanel unable to Drag and Drop" + cnfe);
        }
        add(_scrollPane);
        if (anomalies != null) {
            setAnomalies(anomalies);
        }
    }

    void setAnomalies(HashMap<Integer, Boolean> anomalies) {
        SpeedTableModel model = (SpeedTableModel)_table.getModel();
        model.setAnomaly(anomalies);
        if (anomalies != null && anomalies.size() > 0) {
            JScrollBar bar = _scrollPane.getVerticalScrollBar();
            bar.setValue(50);       // important to "prime" the setting for bar.getMaximum()
            int numRows = model.getRowCount();
            Integer key = 1000;
            for (int k : anomalies.keySet()) {
                if (k < key) {
                    key = k;
                }
            }
            TreeMap<Integer, SpeedStep> speeds = model.getProfileSpeeds();
            Map.Entry<Integer, SpeedStep> entry = speeds.higherEntry(key);
            if (entry == null) {
                entry = speeds.lowerEntry(key);
            }
            int row = model.getRow(entry);
            int pos = (int)(((float)row)*bar.getMaximum() / numRows + .5);
            bar.setValue(pos);
        }
    }

    private void deleteRow() {
        int row = _table.getSelectedRow();
        if (row >= 0) {
            SpeedTableModel model = (SpeedTableModel)_table.getModel();
            Map.Entry<Integer, SpeedStep> entry = model.speedArray.get(row);
            model.speedArray.remove(entry);
            model._profile.deleteStep(entry.getKey());
            model.fireTableDataChanged();
        }
    }

    public static class ColorCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            SpeedTableModel model = (SpeedTableModel) table.getModel();
            HashMap<Integer, Boolean> anomalies = model.getAnomalies();
  
            if (anomalies == null || anomalies.size() == 0) {
                c.setBackground(table.getBackground());                                
                return c;
            }
            Map.Entry<Integer, SpeedStep> entry = model.getRowEntry(row);
            Boolean direction = anomalies.get(entry.getKey());
            if (direction == null) {
                c.setBackground(table.getBackground());
                return c;
            }
            boolean dir =  direction.booleanValue();
            if ( dir && col == SpeedTableModel.FORWARD_SPEED_COL) {
                c.setBackground(myRed);                
            } else if (!dir && col == SpeedTableModel.REVERSE_SPEED_COL){
                c.setBackground(myRed);
            }
            return c;
        }
    }

    private void rePack(int row) {
        SpeedTableModel model = (SpeedTableModel)_table.getModel();
        Map.Entry<Integer, SpeedStep> entry = model.getRowEntry(row);
        setAnomalies(model.updateAnomaly(entry));
        model.fireTableDataChanged();
    }

    private void rePack(Integer key) {
        SpeedTableModel model = (SpeedTableModel)_table.getModel();
        setAnomalies(model.updateAnomaly(model.getKeyEntry(key)));
        model.fireTableDataChanged();
    }

    static class SpeedTableModel extends javax.swing.table.AbstractTableModel {
        static final int STEP_COL = 0;
        static final int THROTTLE_COL = 1;
        static final int FORWARD_SPEED_COL = 2;
        static final int REVERSE_SPEED_COL = 3;
        static final int NUMCOLS = 4;
        
        java.text.DecimalFormat threeDigit = new java.text.DecimalFormat("0.000");
        ArrayList<Map.Entry<Integer, SpeedStep>> speedArray = new  ArrayList<>();
        RosterSpeedProfile _profile;
        Boolean _editable;
        HashMap<Integer, Boolean> _anomaly;
        
        SpeedTableModel(RosterSpeedProfile sp, boolean editable, HashMap<Integer, Boolean> anomalies) {
            _profile = sp;
            _editable = editable; // allow mergeProfile editing
            _anomaly = anomalies;
            TreeMap<Integer, SpeedStep> speeds = sp.getProfileSpeeds();
            Map.Entry<Integer, SpeedStep> entry = speeds.firstEntry();
            while (entry!=null) {
                speedArray.add(entry);
                entry = speeds.higherEntry(entry.getKey());
            }
        }

        HashMap<Integer, Boolean> getAnomalies() {
            return _anomaly;
        }

        void setAnomaly(HashMap<Integer, Boolean> an) {
            _anomaly = an;
        }
        private HashMap<Integer, Boolean> updateAnomaly(Map.Entry<Integer, SpeedStep> entry) {
            SpeedStep ss = entry.getValue();
            _profile.setSpeed(entry.getKey(), ss.getForwardSpeed(), ss.getReverseSpeed());
            _anomaly = MergePrompt.validateSpeedProfile(_profile);
            log.debug("updateAnomaly size={}", _anomaly.size());
            return _anomaly;
        }

        Map.Entry<Integer, SpeedStep> getRowEntry(int row) {
            return speedArray.get(row); 
        }

        Map.Entry<Integer, SpeedStep> getKeyEntry(Integer key) {
            for (Map.Entry<Integer, SpeedStep> entry : speedArray) {
                if (entry.getKey().equals(key)) {
                    return entry;
                }
            }
            return null; 
        }

        TreeMap<Integer, SpeedStep> getProfileSpeeds() {
            return _profile.getProfileSpeeds();
        }
        
        void setSelectionData(Integer key) {
            
        }

        void addEntry( Map.Entry<Integer, SpeedStep> entry) {
            SpeedStep ss = entry.getValue();
            Integer key = entry.getKey();
            _profile.setSpeed(key, ss.getForwardSpeed(), ss.getReverseSpeed());
            for (int row = 0; row<speedArray.size(); row++) {
                int k = speedArray.get(row).getKey().intValue();
                if (key.intValue() < k) {
                    speedArray.add(row, entry);
                    log.debug("addEntry _profile size={}, speedArray size={}", _profile.getProfileSize(), speedArray.size());
                    return;
                }
            }
            speedArray.add(entry);
        }

        int getRow(Map.Entry<Integer, SpeedStep> entry) {
            return speedArray.indexOf(entry);
        }

        @Override
        public int getColumnCount() {
            return NUMCOLS;
        }

        @Override
        public int getRowCount() {
            return speedArray.size();
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case STEP_COL:
                    return Bundle.getMessage("step");
                case THROTTLE_COL:
                    return Bundle.getMessage("throttlesetting");
                case FORWARD_SPEED_COL:
                    return Bundle.getMessage("forward");
                case REVERSE_SPEED_COL:
                    return Bundle.getMessage("reverse");
                default:
                    // fall out
                    break;
            }
            return "";
        }
        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case STEP_COL:
                    return new JTextField(3).getPreferredSize().width;
                case THROTTLE_COL:
                    return new JTextField(5).getPreferredSize().width;
                case FORWARD_SPEED_COL:
                case REVERSE_SPEED_COL:
                    return new JTextField(8).getPreferredSize().width;
                default:
                    // fall out
                    break;
            }
            return new JTextField(8).getPreferredSize().width;
        }
        
        @Override
        public boolean isCellEditable(int row, int col) {
            if (_editable && (col == FORWARD_SPEED_COL || col == REVERSE_SPEED_COL)) {
                return true;
            }
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Map.Entry<Integer, SpeedStep> entry = speedArray.get(row);
            switch (col) {
                case STEP_COL:
                    return Math.round((float)(entry.getKey()*126)/1000);
                case THROTTLE_COL:
                    return (float)(entry.getKey())/1000;
                case FORWARD_SPEED_COL:
                    float speed = entry.getValue().getForwardSpeed();
                    return threeDigit.format(speed);
                case REVERSE_SPEED_COL:
                    speed = entry.getValue().getReverseSpeed();
                    return threeDigit.format(speed);
                default:
                    // fall out
                    break;
            }
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (!_editable) {
                return;
            }
            Map.Entry<Integer, SpeedStep> entry = speedArray.get(row);
            try {
            switch (col) {
                case FORWARD_SPEED_COL:
                    entry.getValue().setForwardSpeed(Float.parseFloat((String)value));
                    return;
                case REVERSE_SPEED_COL:
                    entry.getValue().setReverseSpeed(Float.parseFloat((String)value));
                    return;
                default:
                    // fall out
                    break;
            }
            } catch (NumberFormatException nfe) {
                log.error("SpeedTableModel ({}, {}) value={}", row, col, value);
            }
        }
    }

    class ExportEntryTranferHandler extends TransferHandler {

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        @Override
        public Transferable createTransferable(JComponent c) {
            JTable table = (JTable) c;
            int row = table.getSelectedRow();
            if (row < 0) {
                return null;
            }
            row = table.convertRowIndexToModel(row);
            SpeedTableModel model = (SpeedTableModel)table.getModel();
            return new EntrySelection(model.getRowEntry(row));
        }
    }

    class ImportEntryTranferHandler extends ExportEntryTranferHandler {

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            DataFlavor[] flavors =  support.getDataFlavors();
            if (flavors == null) {
                return false;
            }
            for (int k = 0; k < flavors.length; k++) {
                if (_entryFlavor.equals(flavors[k])) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            if (!support.isDrop()) {
                return false;            
            }
            TransferHandler.DropLocation loc = support.getDropLocation();
            if (!(loc instanceof JTable.DropLocation)) {
                return false;
            }
            Component comp = support.getComponent();
            if (!(comp instanceof JTable)) {
                return false;            
            }       
            JTable table = (JTable)comp;
            try {
                Transferable trans = support.getTransferable();
                Object obj = trans.getTransferData(_entryFlavor);
                if (!(obj instanceof Map.Entry)) {
                    return false;
                }
                @SuppressWarnings("unchecked")
                Map.Entry<Integer, SpeedStep> sourceEntry = (Map.Entry<Integer, SpeedStep>)obj;
                SpeedStep sss = sourceEntry.getValue();
                SpeedTableModel model = (SpeedTableModel)table.getModel();
                Integer key = sourceEntry.getKey();
                Map.Entry<Integer, SpeedStep> entry = model.getKeyEntry(key);
                if (entry != null ) {
                    SpeedStep ss = entry.getValue();
                    if (sss.getForwardSpeed() > 0f) {
                        if (ss.getForwardSpeed() <= 0f) {
                            ss.setForwardSpeed(sss.getForwardSpeed());
                        } else {
                            ss.setForwardSpeed((sss.getForwardSpeed() + ss.getForwardSpeed()) / 2);
                        }
                    }
                    if (sss.getReverseSpeed() > 0f) {
                        if (ss.getReverseSpeed() <= 0f) {
                            ss.setReverseSpeed(sss.getReverseSpeed());
                        } else {
                            ss.setReverseSpeed((sss.getReverseSpeed() + ss.getReverseSpeed()) / 2);
                        }
                    }
                } else {
                    model.addEntry(sourceEntry);
                }
                rePack(key);

                return true;
            } catch (UnsupportedFlavorException ufe) {
                log.warn("MergeTranferHandler.importData: " + ufe);
            } catch (IOException ioe) {
                log.warn("MergeTranferHandler.importData: " + ioe);
            }
            return false;
        }
    }

    class EntrySelection implements Transferable {
        Integer _key;
        SpeedStep _step;
        public EntrySelection(Map.Entry<Integer, SpeedStep> entry) {
            _key = entry.getKey();
            _step = new SpeedStep();
            SpeedStep step = entry.getValue();
            _step.setForwardSpeed(step.getForwardSpeed());
            _step.setReverseSpeed(step.getReverseSpeed());
        }
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {_entryFlavor, DataFlavor.stringFlavor};
        }
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            if (_entryFlavor.equals(flavor)) {
                return true;
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                return true;
            }
            return false;
        }
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (_entryFlavor.equals(flavor)) {
                SimpleEntry<Integer, SpeedStep> entry = new SimpleEntry<>(_key, _step);
                return entry;
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                StringBuilder  msg = new StringBuilder ();
                msg.append(_key.toString());
                msg.append(',');
                msg.append(_step.getForwardSpeed());
                msg.append(',');
                msg.append(_step.getReverseSpeed());
                return msg.toString();
            }
            log.warn("EntrySelection.getTransferData: " + flavor);
            throw(new UnsupportedFlavorException(flavor));
        }
    }
    private final static Logger log = LoggerFactory.getLogger(SpeedProfilePanel.class);
}
