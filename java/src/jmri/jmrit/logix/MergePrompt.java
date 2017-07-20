package jmri.jmrit.logix;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import jmri.InstanceManager;
import jmri.jmrit.beantable.EnablingCheckboxRenderer;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedStep;
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
    HashMap<String, RosterSpeedProfile> _mergeProfiles;  // candidate's speedprofile
    HashMap<String, RosterSpeedProfile> _sessionProfiles;  // candidate's speedprofile
    JPanel _viewFrame;
    JTable _viewTable;
    static int STRUT = 20;

    MergePrompt(String name, HashMap<String, Boolean> cand) {
        super();
        _candidates = cand;
        WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
        _mergeProfiles = manager.getMergeProfiles();
        _sessionProfiles = manager.getSessionProfiles();
        setTitle(name);
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                noMerge();
                dispose();
            }
        });

        MergeTableModel model = new MergeTableModel(cand);
        JTable table = new JTable(model);

        table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
        table.getColumnModel().getColumn(MergeTableModel.VIEW_COL).setCellEditor(new ButtonEditor(new JButton()));
        table.getColumnModel().getColumn(MergeTableModel.VIEW_COL).setCellRenderer(new ButtonRenderer());

        int tablewidth = 0;
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            tablewidth += column.getPreferredWidth();
        }
        int rowHeight = new JButton("VIEW").getPreferredSize().height;
        table.setRowHeight(rowHeight);
//        java.awt.Font font = table.getFont();
        JPanel description = new JPanel();
        JLabel label = new JLabel(Bundle.getMessage("MergePrompt"));
//        description.setFont(font);
        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        description.add(label);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        JButton button = new JButton(Bundle.getMessage("ButtonNoMerge"));
        button.addActionListener((ActionEvent evt) -> {
            noMerge();
            dispose();
        });
        panel.add(button);
        panel.add(Box.createHorizontalStrut(STRUT));
        button = new JButton(Bundle.getMessage("ButtonMerge"));
        button.addActionListener((ActionEvent evt) -> {
            dispose();
        });
        panel.add(button);
        panel.add(Box.createHorizontalStrut(STRUT));
        button = new JButton(Bundle.getMessage("ButtonCloseView"));
        button.addActionListener((ActionEvent evt) -> {
            if (_viewFrame != null) {
                getContentPane().remove(_viewFrame);
            }
            pack();
        });
        panel.add(button);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(description);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(tablewidth, tablewidth));
        mainPanel.add(pane);
        mainPanel.add(panel);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
        p.add(Box.createHorizontalStrut(STRUT));
        p.add(Box.createHorizontalGlue());
        p.add(mainPanel);
        p.add(Box.createHorizontalGlue());
        p.add(Box.createHorizontalStrut(STRUT));
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        contentPane.add(p);
        setContentPane(contentPane);
        pack();
        Dimension screen = getToolkit().getScreenSize();
        setLocation(screen.width/3, screen.height/4);
        setAlwaysOnTop(true);
        setVisible(true);
    }
    
    private void noMerge() {
        for (Map.Entry<String, Boolean> ent : _candidates.entrySet()) {
            _candidates.put(ent.getKey(), Boolean.valueOf(false));
        }
    }

    void showProfiles(String id) {
        if (_viewFrame != null) {
            getContentPane().remove(_viewFrame);
//            _viewFrame.removeKeyListener(_viewFrame);
        }
        _viewFrame = new JPanel();
        _viewFrame.setLayout(new BoxLayout(_viewFrame, BoxLayout.PAGE_AXIS));
        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("viewTitle", id)));
        panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("deletePrompt", id)));
        _viewFrame.add(panel);
 
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
        int tablewidth = 0;
        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            int width = model.getPreferredWidth(i);
            column.setPreferredWidth(width);
            tablewidth += width;
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane pane = new JScrollPane(table);
        int barWidth = 5+pane.getVerticalScrollBar().getPreferredSize().width;
        tablewidth += barWidth;
        pane.setPreferredSize(new Dimension(tablewidth, tablewidth));
        panel.add(pane);
        spPanel.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(new JLabel(Bundle.getMessage("mergedSpeedProfile")));
        model = new SpeedTableModel(_mergeProfiles.get(id));
        _viewTable = new JTable(model);
        _viewTable.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent ke) {
                char ch = ke.getKeyChar(); 
                if (ch == KeyEvent.VK_DELETE || ch == KeyEvent.VK_X) {
                    deleteRow();
                }
            }
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {}
        });
        tablewidth = barWidth;
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width);
            tablewidth += width;
        }
        _viewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        pane = new JScrollPane(_viewTable);
        pane.setPreferredSize(new Dimension(tablewidth, tablewidth));
        panel.add(pane);
        spPanel.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(new JLabel(Bundle.getMessage("sessionSpeedProfile")));
        model = new SpeedTableModel(_sessionProfiles.get(id));
        table = new JTable(model);
        tablewidth = barWidth;
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width);
            tablewidth += width;
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(tablewidth, tablewidth));
        panel.add(pane);
        spPanel.add(panel);
        
        _viewFrame.add(spPanel);
        getContentPane().add(_viewFrame);
        pack();
    }

    private void deleteRow() {
        int row = _viewTable.getSelectedRow();
        if (row >= 0) {
            SpeedTableModel model = (SpeedTableModel)_viewTable.getModel();
            Map.Entry<Integer, SpeedStep> entry = model.speedArray.get(row);
            model.speedArray.remove(entry);
            model.profile.deleteStep(entry.getKey());
            model.fireTableDataChanged();
        }
    }

    /**
     * Check that non zero value are ascending for both forward and reverse speeds.
     * Omit anomalies.
     * @param speedProfile speedProfile
     * @param id roster id
     */
    static protected void validateSpeedProfile(RosterSpeedProfile speedProfile, String id) {
        // do forward speeds, then reverse
        TreeMap<Integer, SpeedStep> rosterTree = speedProfile.getProfileSpeeds();
        float lastForward = 0;
        Integer lastKey = Integer.valueOf(0);
        Iterator<Map.Entry<Integer, SpeedStep>> iter = rosterTree.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, SpeedStep> entry = iter.next(); 
            float forward = entry.getValue().getForwardSpeed();
            Integer key = entry.getKey();
            if (forward > 0.0f) {
                if (forward < lastForward) {  // anomaly - remove bad entry
                    while (iter.hasNext()) {
                        Map.Entry<Integer, SpeedStep> nextEntry = iter.next();
                        float nextForward = nextEntry.getValue().getForwardSpeed();
                        if (nextForward > 0.0f) {
                            if (nextForward > lastForward) {    // remove forward
                                log.info("Remove anomaly key={}, forward={} from profile {}", key, forward, id);
                                speedProfile.setForwardSpeed(key, 0.0f);
                                forward = nextForward;
                                key = nextEntry.getKey();
                            } else {    // remove lastForward
                                speedProfile.setForwardSpeed(lastKey, 0.0f);
                                log.info("Remove anomaly key={}, forward={} from profile {}", lastKey, lastForward, id);
                            }
                            break;
                        }
                    }
                }
                lastForward = forward;
                lastKey = key;
            }
        }

        rosterTree = speedProfile.getProfileSpeeds();
        float lastReverse = 0;
        lastKey = Integer.valueOf(0);
        iter = rosterTree.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, SpeedStep> entry = iter.next(); 
            float reverse = entry.getValue().getReverseSpeed();
            Integer key = entry.getKey();
            if (reverse > 0.0f) {
                if (reverse < lastReverse) {  // anomaly - remove bad entry
                    while (iter.hasNext()) {
                        Map.Entry<Integer, SpeedStep> nextEntry = iter.next();
                        float nextreverse = nextEntry.getValue().getReverseSpeed();
                        if (nextreverse > 0.0f) {
                            if (nextreverse > lastReverse) {    // remove reverse
                                log.info("Remove anomaly key={}, reverse={} from profile {}", key, reverse, id);
                                speedProfile.setReverseSpeed(key, 0.0f);
                                reverse = nextreverse;
                                key = nextEntry.getKey();
                            } else {    // remove lastReverse
                                speedProfile.setReverseSpeed(lastKey, 0.0f);
                                log.info("Remove anomaly key={}, reverse={} from profile{}", lastKey, lastReverse, id);
                            }
                            break;
                        }
                    }
                }
                lastReverse = reverse;
                lastKey = key;
            }           
        }
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
        RosterSpeedProfile profile;
        
        SpeedTableModel(RosterSpeedProfile sp) {
            profile = sp;
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
