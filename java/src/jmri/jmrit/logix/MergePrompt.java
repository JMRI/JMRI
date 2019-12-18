package jmri.jmrit.logix;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.beantable.EnablingCheckboxRenderer;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedStep;
import jmri.util.JmriJFrame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Prompts user to select SpeedProfile to write to Roster
 *
 * @author Pete Cressman Copyright (C) 2017
 */
public class MergePrompt extends JDialog {

    HashMap<String, Boolean> _candidates;   // merge candidate choices
    HashMap<String, RosterSpeedProfile> _mergeProfiles;  // candidate's speedprofile
    HashMap<String, RosterSpeedProfile> _sessionProfiles;  // candidate's speedprofile
    HashMap<String, HashMap<Integer, Boolean>> _anomalyMap;
    JPanel _viewFrame;
    JTable _mergeTable;
    JmriJFrame _anomolyFrame;
    static int STRUT = 20;

    MergePrompt(String name, HashMap<String, Boolean> cand, HashMap<String, HashMap<Integer, Boolean>> anomalies) {
        super();
        _candidates = cand;
        _anomalyMap = anomalies;
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
        table.getColumnModel().getColumn(MergeTableModel.VIEW_COL).setCellRenderer(new ButtonCellRenderer());

        int tablewidth = 0;
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            table.getColumnModel().getColumn(i).setPreferredWidth(width);
            tablewidth += width;
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

//        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(tablewidth, tablewidth));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(description);
        mainPanel.add(pane);
        if (_anomalyMap != null && _anomalyMap.size() > 0) {
            mainPanel.add(makeAnomalyPanel());
        }
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
        setLocation(screen.width / 3, screen.height / 4);
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
        }
        _viewFrame = new JPanel();
        _viewFrame.setLayout(new BoxLayout(_viewFrame, BoxLayout.PAGE_AXIS));
        _viewFrame.add(Box.createGlue());
        JPanel panel = new JPanel();
        panel.add(MergePrompt.makeEditInfoPanel(id));
        _viewFrame.add(panel);

        HashMap<Integer, Boolean> anomalies = _anomalyMap.get(id);

        JPanel spPanel = new JPanel();
        spPanel.setLayout(new BoxLayout(spPanel, BoxLayout.LINE_AXIS));
        spPanel.add(Box.createGlue());

        RosterEntry re = Roster.getDefault().entryFromTitle(id);
        RosterSpeedProfile speedProfile = null;
        if (re != null) {
            speedProfile = re.getSpeedProfile();
        }
        if (speedProfile == null) {
            speedProfile = new RosterSpeedProfile(null);
        }
        spPanel.add(makeSpeedProfilePanel("rosterSpeedProfile", speedProfile,  false, null));
        spPanel.add(Box.createGlue());

        spPanel.add(makeSpeedProfilePanel("mergedSpeedProfile", _mergeProfiles.get(id), true, anomalies));
        spPanel.add(Box.createGlue());

        spPanel.add(makeSpeedProfilePanel("sessionSpeedProfile", _sessionProfiles.get(id), false, null));
        spPanel.add(Box.createGlue());

        _viewFrame.add(spPanel);
        getContentPane().add(_viewFrame);
        pack();
    }

    static JPanel makeEditInfoPanel(String id) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(Bundle.getMessage("viewTitle", id));
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(label);
        label = new JLabel(Bundle.getMessage("deletePrompt1"));
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setForeground(java.awt.Color.RED);
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(label);
        label = new JLabel(Bundle.getMessage("deletePrompt2"));
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(label);
        label = new JLabel(Bundle.getMessage("deletePrompt3"));
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(label);
        return panel;
    }

    static JPanel makeAnomalyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JLabel l = new JLabel(Bundle.getMessage("anomalyPrompt"));
        l.setForeground(java.awt.Color.RED);
        l.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(l);
        return panel;
    }

    static JPanel makeSpeedProfilePanel(String title, RosterSpeedProfile profile, 
                boolean edit, HashMap<Integer, Boolean> anomalies) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(new JLabel(Bundle.getMessage(title)));
        SpeedProfilePanel speedPanel = new SpeedProfilePanel(profile, edit, anomalies);
        panel.add(speedPanel);
        return panel;
    }
    /**
     * Check that non zero value are ascending for both forward and reverse
     * speeds. Omit anomalies.
     *
     * @param speedProfile speedProfile
     * @return HashMap of Key and direction of possible errors (anomalies)
     */
    static public HashMap<Integer, Boolean> validateSpeedProfile(RosterSpeedProfile speedProfile) {
        // do forward speeds, then reverse
        HashMap<Integer, Boolean> anomalies = new HashMap<>();
        TreeMap<Integer, SpeedStep> rosterTree = speedProfile.getProfileSpeeds();
        float lastForward = 0;
        Integer lastKey = Integer.valueOf(0);
        Iterator<Map.Entry<Integer, SpeedStep>> iter = rosterTree.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, SpeedStep> entry = iter.next();
            float forward = entry.getValue().getForwardSpeed();
            Integer key = entry.getKey();
            if (forward > 0.0f) {
                if (forward < lastForward) {  // anomaly found
                    while (iter.hasNext()) {
                        Map.Entry<Integer, SpeedStep> nextEntry = iter.next();
                        float nextForward = nextEntry.getValue().getForwardSpeed();
                        if (nextForward > 0.0f) {
                            if (nextForward > lastForward) {    // remove forward
                                anomalies.put(key, true);
                                forward = nextForward;
                                key = nextEntry.getKey();
                            } else {    // remove lastForward
                                anomalies.put(lastKey, true);
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
                if (reverse < lastReverse) {  // anomaly found
                    while (iter.hasNext()) {
                        Map.Entry<Integer, SpeedStep> nextEntry = iter.next();
                        float nextreverse = nextEntry.getValue().getReverseSpeed();
                        if (nextreverse > 0.0f) {
                            if (nextreverse > lastReverse) {    // remove reverse
                                anomalies.put(key, false);
                                reverse = nextreverse;
                                key = nextEntry.getKey();
                            } else {    // remove lastReverse
                                anomalies.put(lastKey, false);
                            }
                            break;
                        }
                    }
                }
                lastReverse = reverse;
                lastKey = key;
            }
        }
        return anomalies;
    }

    class MergeTableModel extends javax.swing.table.AbstractTableModel {

        static final int MERGE_COL = 0;
        static final int ID_COL = 1;
        static final int VIEW_COL = 2;
        static final int NUMCOLS = 3;

        ArrayList<Map.Entry<String, Boolean>> candidateArray = new ArrayList<>();

        MergeTableModel(HashMap<String, Boolean> map) {
            Iterator<java.util.Map.Entry<String, Boolean>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                candidateArray.add(iter.next());
            }
        }

        boolean hasAnomaly(int row) {
            Map.Entry<String, Boolean> entry = candidateArray.get(row);
            HashMap<Integer, Boolean> anomaly = _anomalyMap.get(entry.getKey());
            if (anomaly != null && anomaly.size() > 0) {
                return true;
            }
            return false;
        }

        @Override
        public int getColumnCount() {
            return NUMCOLS;
        }

        @Override
        public int getRowCount() {
            return candidateArray.size();
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
                    return new JTextField(16).getPreferredSize().width;
                case VIEW_COL:
                    return new JTextField(7).getPreferredSize().width;
                default:
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
            Map.Entry<String, Boolean> entry = candidateArray.get(row);
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
            Map.Entry<String, Boolean> entry = candidateArray.get(row);
            switch (col) {
                case MERGE_COL:
                    _candidates.put(entry.getKey(), (Boolean) value);
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

    public static class ButtonCellRenderer extends ButtonRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component b = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            MergeTableModel tableModel = (MergeTableModel) table.getModel();
            if (tableModel.hasAnomaly(row)) {
                b.setBackground(java.awt.Color.RED);
            } else {
                b.setBackground(table.getBackground());
            }
            return b;
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(MergePrompt.class);
}
