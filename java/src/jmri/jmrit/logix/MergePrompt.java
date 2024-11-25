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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import jmri.InstanceManager;
import jmri.jmrit.beantable.EnablingCheckboxRenderer;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedStep;
import jmri.util.table.ButtonEditor;

/**
 * Prompts user to select SpeedProfile to write to Roster
 *
 * @author Pete Cressman Copyright (C) 2017
 */
public class MergePrompt extends JDialog {

    private final Map<String, Boolean> _candidates;   // merge candidate choices
//    HashMap<String, RosterSpeedProfile> _mergeProfiles;  // candidate's speedprofile
    private final Map<String, Map<Integer, Boolean>> _anomalyMap;
    private JPanel _viewPanel;
    private static final int STRUT = 20;

    MergePrompt(String name, Map<String, Boolean> cand, Map<String, Map<Integer, Boolean>> anomalies) {
        super();
        _candidates = cand;
        _anomalyMap = anomalies;
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
        JPanel description = new JPanel();
        JLabel label = new JLabel(Bundle.getMessage("MergePrompt"));
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
        button.addActionListener((ActionEvent evt) -> dispose());
        panel.add(button);
        panel.add(Box.createHorizontalStrut(STRUT));
        button = new JButton(Bundle.getMessage("ButtonCloseView"));
        button.addActionListener((ActionEvent evt) -> {
            if (_viewPanel != null) {
                getContentPane().remove(_viewPanel);
            }
            pack();
        });
        panel.add(button);

        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(tablewidth, tablewidth));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(description);
        mainPanel.add(pane);
        if (_anomalyMap != null && !_anomalyMap.isEmpty()) {
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
    }

    private void noMerge() {
        for (Map.Entry<String, Boolean> ent : _candidates.entrySet()) {
            _candidates.put(ent.getKey(), false);
        }
    }

    static JPanel makeEditInfoPanel(RosterEntry entry) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel(Bundle.getMessage("viewTitle", entry.getId()));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        label = new JLabel(Bundle.getMessage("deletePrompt1"));
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setForeground(java.awt.Color.RED);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        label = new JLabel(Bundle.getMessage("deletePrompt2"));
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        label = new JLabel(Bundle.getMessage("deletePrompt3"));
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        return panel;
    }

    static JPanel makeAnomalyPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JLabel l = new JLabel(Bundle.getMessage("anomalyPrompt"));
        l.setForeground(java.awt.Color.RED);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(l);
        return panel;
    }

    static JPanel makeSpeedProfilePanel(String title, RosterSpeedProfile profile, 
                boolean edit, Map<Integer, Boolean> anomalies) {
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
     * @return Map of Key and direction of possible errors (anomalies)
     */
    public static Map<Integer, Boolean> validateSpeedProfile(RosterSpeedProfile speedProfile) {
        // do forward speeds, then reverse
        HashMap<Integer, Boolean> anomalies = new HashMap<>();
        if (speedProfile == null) {
            return anomalies;
        }
        TreeMap<Integer, SpeedStep> rosterTree = speedProfile.getProfileSpeeds();
        float lastForward = 0;
        Integer lastKey = 0;
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
        lastKey = 0;
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

    private class MergeTableModel extends javax.swing.table.AbstractTableModel {

        static final int MERGE_COL = 0;
        static final int ID_COL = 1;
        static final int VIEW_COL = 2;
        static final int NUMCOLS = 3;

        final ArrayList<Map.Entry<String, Boolean>> candidateArray = new ArrayList<>();

        MergeTableModel(Map<String, Boolean> map) {
            Iterator<Map.Entry<String, Boolean>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                candidateArray.add(iter.next());
            }
        }

        boolean hasAnomaly(int row) {
            Map.Entry<String, Boolean> entry = candidateArray.get(row);
            Map<Integer, Boolean> anomaly = _anomalyMap.get(entry.getKey());
            return(anomaly != null && !anomaly.isEmpty());
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
            return col != ID_COL;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Map.Entry<String, Boolean> entry = candidateArray.get(row);
            switch (col) {
                case MERGE_COL:
                    return entry.getValue();
                case ID_COL:
                    String id = entry.getKey();
                    if (id == null || id.isEmpty() ||
                            (id.charAt(0) == '$' && id.charAt(id.length()-1) == '$')) {
                        id = Bundle.getMessage("noSuchAddress");
                    }
                    return id;
                case VIEW_COL:
                    return Bundle.getMessage("View");
                default:
                    break;
            }
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            Map.Entry<String, Boolean> entry = candidateArray.get(row);
            switch (col) {
                case MERGE_COL:
                    String id = entry.getKey(); 
                    if (Roster.getDefault().getEntryForId(id) == null) {
                        _candidates.put(entry.getKey(), false);
                    } else {
                        _candidates.put(entry.getKey(), (Boolean) value);
                    }
                    break;
                case ID_COL:
                    break;
                case VIEW_COL:
                    showProfiles(entry.getKey());
                    break;
                default:
                    break;
            }
        }

        private void showProfiles(String id) {
            if (_viewPanel != null) {
                getContentPane().remove(_viewPanel);
            }
            invalidate();
            _viewPanel = makeViewPanel(id);
            if (_viewPanel == null) {
                return;
            }
            getContentPane().add(_viewPanel);
            pack();
            setVisible(true);
        }

        private JPanel makeViewPanel(String id) {
            RosterEntry entry = Roster.getDefault().getEntryForId(id);
            if (entry == null) {
                return null;
            }
            JPanel viewPanel = new JPanel();
            viewPanel.setLayout(new BoxLayout(viewPanel, BoxLayout.PAGE_AXIS));
            viewPanel.add(Box.createGlue());
            JPanel panel = new JPanel();
            panel.add(MergePrompt.makeEditInfoPanel(entry));
            viewPanel.add(panel);

            JPanel spPanel = new JPanel();
            spPanel.setLayout(new BoxLayout(spPanel, BoxLayout.LINE_AXIS));
            spPanel.add(Box.createGlue());

            RosterSpeedProfile speedProfile = entry.getSpeedProfile();
            if (speedProfile != null ){
                spPanel.add(makeSpeedProfilePanel("rosterSpeedProfile", speedProfile,  false, null));
                spPanel.add(Box.createGlue());
            }

            WarrantManager manager = InstanceManager.getDefault(WarrantManager.class);
            RosterSpeedProfile mergeProfile =  manager.getMergeProfile(id);
            Map<Integer, Boolean> anomaly = MergePrompt.validateSpeedProfile(mergeProfile);
            spPanel.add(makeSpeedProfilePanel("mergedSpeedProfile", mergeProfile, true, anomaly));
            spPanel.add(Box.createGlue());

            viewPanel.add(spPanel);
            return viewPanel;
        }

    }

    private static class ButtonCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component b = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            JLabel l = (JLabel)b;
            l.setHorizontalAlignment(SwingConstants.CENTER);
            MergeTableModel tableModel = (MergeTableModel) table.getModel();
            if (tableModel.hasAnomaly(row)) {
                l.setBackground(java.awt.Color.RED);
            } else {
                l.setBackground(table.getBackground());
            }
            return b;
        }
    }

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MergePrompt.class);

}
