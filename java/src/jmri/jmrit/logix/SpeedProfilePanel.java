package jmri.jmrit.logix;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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

    HashMap<String, Boolean> _mergeCandidates;
    HashMap<String, RosterSpeedProfile> _mergeProfiles;
    HashMap<String, HashMap<Integer, Boolean>> _anomalies;
    JTable _table;
    static java.awt.Color myRed = new java.awt.Color(255, 120, 120);
    
    /**
     * @param speedProfile a RosterSpeedProfile
     * @param anomalies Map of keys where profile is not monotonic increasing.
     */
    public SpeedProfilePanel(RosterSpeedProfile speedProfile, HashMap<Integer, Boolean> anomalies) {
        SpeedTableModel model = new SpeedTableModel(speedProfile, anomalies);
        _table = new JTable(model);
        int tablewidth = 0;
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = _table.getColumnModel().getColumn(i);
            int width = model.getPreferredWidth(i);
            column.setPreferredWidth(width);
            tablewidth += width;
        }
        if (anomalies != null) {
            _table.addKeyListener(new KeyListener() {
                public void keyTyped(KeyEvent ke) {
                    char ch = ke.getKeyChar(); 
                    if (ch == KeyEvent.VK_DELETE || ch == KeyEvent.VK_X) {
                        deleteRow();
                    }
                }
                public void keyPressed(KeyEvent e) {}
                public void keyReleased(KeyEvent e) {}
            });
            _table.getColumnModel().getColumn(SpeedTableModel.FORWARD_SPEED_COL).setCellRenderer(new ColorCellRenderer());
            _table.getColumnModel().getColumn(SpeedTableModel.REVERSE_SPEED_COL).setCellRenderer(new ColorCellRenderer());
        }
        _table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane pane = new JScrollPane(_table);
        int barWidth = 5+pane.getVerticalScrollBar().getPreferredSize().width;
        tablewidth += barWidth;
        pane.setPreferredSize(new Dimension(tablewidth, tablewidth));
        if (anomalies != null) {
            JScrollBar bar = pane.getVerticalScrollBar();
            bar.setValue(50);       // important to "prime" the setting for bar.getMaximum()
            int numRows = model.getRowCount();
            Integer key = 1000;
            Iterator<Integer> iter = anomalies.keySet().iterator();
            while (iter.hasNext()) {
                Integer k = iter.next();
                if (k < key) {
                    key = k;
                }
            }
            TreeMap<Integer, SpeedStep> speeds = speedProfile.getProfileSpeeds();
            Map.Entry<Integer, SpeedStep> entry = speeds.higherEntry(key);
            if (entry == null) {
                entry = speeds.lowerEntry(key);
            }
            int row = model.getRow(entry);
            int pos = (int)(((float)row)*bar.getMaximum() / numRows + .5);
            bar.setValue(pos);
        }
        add(pane);
    }

    private void deleteRow() {
        int row = _table.getSelectedRow();
        if (row >= 0) {
            SpeedTableModel model = (SpeedTableModel)_table.getModel();
            Map.Entry<Integer, SpeedStep> entry = model.speedArray.get(row);
            model.speedArray.remove(entry);
            model.profile.deleteStep(entry.getKey());
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
            Map.Entry<Integer, SpeedStep> entry = model.getEntry(row);
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


    static class SpeedTableModel extends javax.swing.table.AbstractTableModel {
        static final int STEP_COL = 0;
        static final int THROTTLE_COL = 1;
        static final int FORWARD_SPEED_COL = 2;
        static final int REVERSE_SPEED_COL = 3;
        static final int NUMCOLS = 4;
        
        java.text.DecimalFormat threeDigit = new java.text.DecimalFormat("0.000");
        ArrayList<Map.Entry<Integer, SpeedStep>> speedArray = new  ArrayList<Map.Entry<Integer, SpeedStep>>();
        RosterSpeedProfile profile;
        Boolean _editable;
        HashMap<Integer, Boolean> _anomaly;
        
        SpeedTableModel(RosterSpeedProfile sp, HashMap<Integer, Boolean> anomaly) {
            profile = sp;
            _editable = (anomaly != null && anomaly.size() > 0);
            _anomaly = anomaly;
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

        Map.Entry<Integer, SpeedStep> getEntry(int row) {
            return speedArray.get(row); 
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

    private final static Logger log = LoggerFactory.getLogger(SpeedProfilePanel.class);
}
