package jmri.jmrit.logix;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import jmri.jmrit.beantable.EnablingCheckboxRenderer;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedStep;
import jmri.util.JmriJFrame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prompts user to select SpeedProfile to write to Roster
 *
 * @author Pete Cressman Copyright (C) 2017
 */
public class MergePrompt extends JDialog {
    
    HashMap<String, Boolean> _candidates;   // merge candidate choices
    HashMap<String, RosterSpeedProfile> _profiles;  // candidate's speedprofile
    JmriJFrame _viewFrame;

    MergePrompt(String name, HashMap<String, Boolean> cand, HashMap<String, RosterSpeedProfile> prof) {
        super();
        _candidates = cand;
        _profiles = prof;
        setTitle(name);
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
            }
        });

        MergeTableModel model = new MergeTableModel(cand);
        JTable table = new JTable(model);

        table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
        table.getColumnModel().getColumn(MergeTableModel.VIEW_COL).setCellEditor(new ButtonEditor(new JButton()));
        table.getColumnModel().getColumn(MergeTableModel.VIEW_COL).setCellRenderer(new ButtonRenderer());

        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width);
        }
//        java.awt.Font font = table.getFont();
        JPanel description = new JPanel();
        JLabel label = new JLabel(Bundle.getMessage("MergePrompt"));
//        description.setFont(font);
        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        description.add(label);

        JPanel panel = new JPanel();
        JButton button = new JButton(Bundle.getMessage("ButtonDone"));
        button.addActionListener((ActionEvent evt) -> {
            dispose();
        });
        panel.add(button);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        contentPane.add(description, BorderLayout.NORTH);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane pane = new JScrollPane(table);
        contentPane.add(pane, BorderLayout.CENTER);
        contentPane.add(panel, BorderLayout.SOUTH);

        getContentPane().add(contentPane);
        pack();
        Dimension screen = getToolkit().getScreenSize();
        setLocation(screen.width/3, screen.height/4);
        setVisible(true);
    }

    static class MyBooleanRenderer extends javax.swing.table.DefaultTableCellRenderer {

        String _trueValue;
        String _falseValue;

        MyBooleanRenderer(String trueValue, String falseValue) {
            _trueValue = trueValue;
            _falseValue = falseValue;
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            JLabel val;
            if (value instanceof Boolean) {
                if (((Boolean) value).booleanValue()) {
                    val = new JLabel(_trueValue);
                } else {
                    val = new JLabel(_falseValue);
                }
            } else {
                val = new JLabel("");
            }
            val.setFont(table.getFont().deriveFont(java.awt.Font.PLAIN));
            return val;
        }
    }

    void showProfiles(String id) {
        if (_viewFrame != null) {
            _viewFrame.dispose();
        }
        _viewFrame = new JmriJFrame(Bundle.getMessage("viewTitle", id), false, true);
        JPanel mainPanel = new JPanel();
//        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.LINE_AXIS));

        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("viewTitle", id)));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(panel);
 
        JPanel spPanel = new JPanel();
        spPanel.setLayout(new BoxLayout(spPanel, BoxLayout.LINE_AXIS));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(new JLabel(Bundle.getMessage("rosterSpeedProfile")));
        RosterEntry re = Roster.getDefault().entryFromTitle(id);
        RosterSpeedProfile speedProfile = null;
        if (re!=null) {
            speedProfile = re.getSpeedProfile();
        }
        if (speedProfile == null) {
            speedProfile = new RosterSpeedProfile(null);            
        }
        SpeedTableModel model = new SpeedTableModel(speedProfile);
        JTable table = new JTable(model);
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width);
            if (i>0) {
                DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                renderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                column.setCellRenderer(renderer);                
            }
        }
        JScrollPane pane = new JScrollPane(table);
        panel.add(pane);
        spPanel.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(new JLabel(Bundle.getMessage("sessionSpeedProfile")));
        model = new SpeedTableModel(_profiles.get(id));
        table = new JTable(model);
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width);
            if (i>0) {
                DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                renderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                column.setCellRenderer(renderer);                
            }
        }
        pane = new JScrollPane(table);
        panel.add(pane);
        spPanel.add(panel);
        mainPanel.add(spPanel);

        _viewFrame.add(mainPanel);
        _viewFrame.setVisible(true);
        _viewFrame.pack();
    }

    class MergeTableModel extends javax.swing.table.AbstractTableModel {
        static final int MERGE_COL = 0;
        static final int ID_COL = 1;
        static final int VIEW_COL = 2;
        static final int NUMCOLS = 3;
 
        ArrayList<Map.Entry<String, Boolean>> candidates = new  ArrayList<Map.Entry<String, Boolean>>();
        
        MergeTableModel (HashMap<String, Boolean> map) {
            Iterator <java.util.Map.Entry <String, Boolean>> iter = map.entrySet().iterator(); 
            while (iter.hasNext()) {
                candidates.add(iter.next());
            }
        }

        @Override
        public int getColumnCount() {
            return NUMCOLS;
        }

        @Override
        public int getRowCount() {
            return candidates.size();
        }
        @Override
        public String getColumnName(int col) {
            switch (col) {
                case MERGE_COL:
                    return Bundle.getMessage("Merge");
                case ID_COL:
                    return Bundle.getMessage("TrainId");
                case VIEW_COL:
                    return Bundle.getMessage("SpeedProfiles");
                default:
                    // fall out
                    break;
            }
            return "";
        }
        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case MERGE_COL:
                    return Boolean.class;
                case ID_COL:
                    return String.class;
                case VIEW_COL:
                    return JButton.class;
                default:
                    // fall out
                    break;
            }
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case MERGE_COL:
                    return new JTextField(3).getPreferredSize().width;                
                case ID_COL:
                    return new JTextField(12).getPreferredSize().width;
                case VIEW_COL:
                    return new JTextField(8).getPreferredSize().width;
                default:
                    // fall out
                    break;
            }
            return new JTextField(12).getPreferredSize().width;
        }
        
        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == ID_COL) {
                return false;
            }
            return true;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Map.Entry<String, Boolean> entry = candidates.get(row);
            switch (col) {
                case MERGE_COL:
                    return entry.getValue();
                case ID_COL:
                    return entry.getKey();
                case VIEW_COL:
                    return Bundle.getMessage("View");
                default:
                    // fall out
                    break;
            }
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            Map.Entry<String, Boolean> entry = candidates.get(row);
            switch (col) {
                case MERGE_COL:
                    _candidates.put(entry.getKey(), (Boolean)value);
                    break;
                case ID_COL:
                    break;
                case VIEW_COL:
                    showProfiles(entry.getKey());
                    break;
                default:
                    // fall out
                    break;
            }
       }
    }
 
    class SpeedTableModel extends javax.swing.table.AbstractTableModel {
        static final int STEP_COL = 0;
        static final int THROTTLE_COL = 1;
        static final int FORWARD_SPEED_COL = 2;
        static final int REVERSE_SPEED_COL = 3;
        static final int NUMCOLS = 4;
        
        java.text.DecimalFormat threeDigit = new java.text.DecimalFormat("0.000");
        ArrayList<Map.Entry<Integer, SpeedStep>> speedArray = new  ArrayList<Map.Entry<Integer, SpeedStep>>();
        
        SpeedTableModel(RosterSpeedProfile sp) {
            TreeMap<Integer, SpeedStep> speeds = sp.getProfileSpeeds();
            Map.Entry<Integer, SpeedStep> entry = speeds.firstEntry();
            while (entry!=null) {
                speedArray.add(entry);
                entry = speeds.higherEntry(entry.getKey());
            }
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
        }
    }

    private final static Logger log = LoggerFactory.getLogger(MergePrompt.class.getName());

}
