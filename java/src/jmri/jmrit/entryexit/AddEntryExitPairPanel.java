package jmri.jmrit.entryexit;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.util.JmriJFrame;
import jmri.util.swing.*;
import jmri.util.AlphanumComparator;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanel to create a new EntryExitPair.
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class AddEntryExitPairPanel extends jmri.util.swing.JmriPanel {

    JComboBox<String> selectPanel = new JComboBox<>();
    JComboBox<String> fromPoint = new JComboBox<>();
    JComboBox<String> toPoint = new JComboBox<>();

    String[] interlockTypes = {Bundle.getMessage("SetTurnoutsOnly"), Bundle.getMessage("SetTurnoutsAndSignalMasts"), Bundle.getMessage("FullInterlock")};  // NOI18N
    JComboBox<String> typeBox = new JComboBox<>(interlockTypes);

    List<LayoutEditor> panels;

    EntryExitPairs nxPairs = jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class);

    // signalling.EntryExitBundle via Bundle method

    public AddEntryExitPairPanel(LayoutEditor panel) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel top = new JPanel();
        top.setLayout(new GridLayout(6, 2));

        top.add(new JLabel(Bundle.getMessage("SelectPanel")));  // NOI18N
        top.add(selectPanel);
        selectPanel.removeAllItems();
        panels = InstanceManager.getDefault(PanelMenu.class).getLayoutEditorPanelList();
        for (int i = 0; i < panels.size(); i++) {
            selectPanel.addItem(panels.get(i).getLayoutName());
        }
        if (panel != null) {
            selectPanel.setSelectedItem(panel.getLayoutName());
        }

        top.add(new JLabel(Bundle.getMessage("FromLocation")));  // NOI18N
        top.add(fromPoint);
        ActionListener selectPanelListener = (ActionEvent e) -> {
            selectPointsFromPanel();
            nxModel.setPanel(panels.get(selectPanel.getSelectedIndex()));
        };
        selectPointsFromPanel();
        selectPanel.addActionListener(selectPanelListener);

        top.add(new JLabel(Bundle.getMessage("ToLocation")));  // NOI18N
        top.add(toPoint);

        JComboBoxUtil.setupComboBoxMaxRows(fromPoint);
        JComboBoxUtil.setupComboBoxMaxRows(toPoint);

        top.add(new JLabel(Bundle.getMessage("NXType")));  // NOI18N
        top.add(typeBox);
        add(top);

        //add(top);
        JPanel p = new JPanel();
        JButton ok = new JButton(Bundle.getMessage("AddPair"));  // NOI18N
        p.add(ok);
        ok.addActionListener((ActionEvent e) -> {
            addButton();
        });

        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JButton auto;
        p.add(auto = new JButton(Bundle.getMessage("AutoGenerate")));  // NOI18N
        auto.addActionListener((ActionEvent e) -> {
            autoDiscovery();
        });
        p.add(auto);
        add(p);
        nxModel = new TableModel(panel);
        nxDataTable = new JTable(nxModel);
        nxDataTable.setRowSorter(new TableRowSorter<>(nxModel));
        nxDataScroll = new JScrollPane(nxDataTable);
        nxModel.configureTable(nxDataTable);
        java.awt.Dimension dataTableSize = nxDataTable.getPreferredSize();
        // width is right, but if table is empty, it's not high
        // enough to reserve much space.
        dataTableSize.height = Math.max(dataTableSize.height, 400);
        nxDataScroll.getViewport().setPreferredSize(dataTableSize);
        add(nxDataScroll);
    }

    LayoutEditor panel;

    private void addButton() {
        ValidPoints from = getValidPointFromCombo(fromPoint);
        ValidPoints to = getValidPointFromCombo(toPoint);
        if (from == null || to == null) {
            return;
        }

        nxPairs.addNXDestination(from.getPoint(), to.getPoint(), panel);
        nxPairs.setEntryExitType(from.getPoint(), panel, to.getPoint(), typeBox.getSelectedIndex());
    }

    jmri.util.JmriJFrame entryExitFrame = null;
    JLabel sourceLabel = new JLabel();

    private void autoDiscovery() {
        if (!InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
            int response = JOptionPane.showConfirmDialog(null, Bundle.getMessage("EnableLayoutBlockRouting"));  // NOI18N
            if (response == 0) {
                InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).enableAdvancedRouting(true);
                JOptionPane.showMessageDialog(null, Bundle.getMessage("LayoutBlockRoutingEnabled"));  // NOI18N
            }
        }
        entryExitFrame = new jmri.util.JmriJFrame(Bundle.getMessage("DiscoverEntryExitPairs"), false, false);   // NOI18N
        entryExitFrame.setPreferredSize(null);
        JPanel panel1 = new JPanel();
        sourceLabel = new JLabel(Bundle.getMessage("DiscoveringEntryExitPairs"));  // NOI18N
        /*ImageIcon i;
         i = new ImageIcon(FileUtil.findURL("resources/icons/misc/gui3/process-working.gif"));
         JLabel label = new JLabel();
         label.setIcon(i);
         panel1.add(label);*/
        panel1.add(sourceLabel);

        entryExitFrame.add(panel1);
        entryExitFrame.pack();
        entryExitFrame.setVisible(true);
        int retval = JOptionPane.showOptionDialog(null, Bundle.getMessage("AutoGenEntryExitMessage"), Bundle.getMessage("AutoGenEntryExitTitle"),  // NOI18N
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (retval == 0) {
            final PropertyChangeListener propertyNXListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("autoGenerateComplete")) {  // NOI18N
                        if (entryExitFrame != null) {
                            entryExitFrame.setVisible(false);
                            entryExitFrame.dispose();
                        }
                        nxPairs.removePropertyChangeListener(this);
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("AutoGenComplete"));  // NOI18N
                    }
                }
            };
            try {
                nxPairs.addPropertyChangeListener(propertyNXListener);
                nxPairs.automaticallyDiscoverEntryExitPairs(panels.get(selectPanel.getSelectedIndex()), typeBox.getSelectedIndex());
            } catch (jmri.JmriException e) {
                nxPairs.removePropertyChangeListener(propertyNXListener);
                JOptionPane.showMessageDialog(null, e.toString());
                entryExitFrame.setVisible(false);
            }
        } else {
            entryExitFrame.setVisible(false);
        }
    }

    ValidPoints getValidPointFromCombo(JComboBox<String> box) {
        String item = (String) box.getSelectedItem();
        for (int i = 0; i < validPoints.size(); i++) {
            if (validPoints.get(i).getDescription().equals(item)) {
                return validPoints.get(i);
            }
        }
        return null;
    }

    List<ValidPoints> validPoints = new ArrayList<>();
    boolean doFromCombo;
    SortedSet<String> fromSet = new TreeSet<>();
    SortedSet<String> toSet = new TreeSet<>();

    private void selectPointsFromPanel() {
        if (selectPanel.getSelectedIndex() == -1) {
            return;
        }
        if (panel == panels.get(selectPanel.getSelectedIndex())) {
            return;
        }
        panel = panels.get(selectPanel.getSelectedIndex());
        fromSet.clear();
        toSet.clear();
        doFromCombo = true;
        selectPoints(panel);

        // Do other panels if any
        doFromCombo = false;
        panels = InstanceManager.getDefault(PanelMenu.class).getLayoutEditorPanelList();
        for (int i = 0; i < panels.size(); i++) {
            if (panels.get(i) != panel) {
                selectPoints(panels.get(i));
            }
        }

        // Update the combo boxes
        fromPoint.removeAllItems();
        fromSet.forEach((ent) -> {
            fromPoint.addItem(ent);
        });
        toPoint.removeAllItems();
        toSet.forEach((ent) -> {
            toPoint.addItem(ent);
        });
    }

    private void selectPoints(LayoutEditor panel) {
        for (PositionablePoint pp : panel.getPositionablePoints()) {
            addPointToCombo(pp.getWestBoundSignalMastName(), pp.getWestBoundSensorName());
            addPointToCombo(pp.getEastBoundSignalMastName(), pp.getEastBoundSensorName());
        }

        for (LayoutTurnout t : panel.getLayoutTurnouts()) {
            addPointToCombo(t.getSignalAMastName(), t.getSensorAName());
            addPointToCombo(t.getSignalBMastName(), t.getSensorBName());
            addPointToCombo(t.getSignalCMastName(), t.getSensorCName());
            addPointToCombo(t.getSignalDMastName(), t.getSensorDName());
        }

        for (LevelXing xing : panel.getLevelXings()) {
            addPointToCombo(xing.getSignalAMastName(), xing.getSensorAName());
            addPointToCombo(xing.getSignalBMastName(), xing.getSensorBName());
            addPointToCombo(xing.getSignalCMastName(), xing.getSensorCName());
            addPointToCombo(xing.getSignalDMastName(), xing.getSensorDName());
        }

        for (LayoutSlip slip : panel.getLayoutSlips()) {
            addPointToCombo(slip.getSignalAMastName(), slip.getSensorAName());
            addPointToCombo(slip.getSignalBMastName(), slip.getSensorBName());
            addPointToCombo(slip.getSignalCMastName(), slip.getSensorCName());
            addPointToCombo(slip.getSignalDMastName(), slip.getSensorDName());
        }
    }

    void addPointToCombo(String signalMastName, String sensorName) {
        if (sensorName != null && !sensorName.isEmpty()) {
            String description = sensorName;
            NamedBean source = InstanceManager.sensorManagerInstance().getSensor(sensorName);
            if (signalMastName != null && !signalMastName.isEmpty()) {
                description = sensorName + " (" + signalMastName + ")";
            }
            validPoints.add(new ValidPoints(source, description));
            if (doFromCombo) {
                fromSet.add(description);
            }
            toSet.add(description);
        }
    }

    JTable nxDataTable;
    JScrollPane nxDataScroll;

    TableModel nxModel;

    static final int FROMPOINTCOL = 0;
    static final int TOPOINTCOL = 1;
    static final int ACTIVECOL = 2;
    static final int CLEARCOL = 3;
    static final int BOTHWAYCOL = 4;
    static final int DELETECOL = 5;
    static final int TYPECOL = 6;
    static final int ENABLEDCOL = 7;

    static final int NUMCOL = ENABLEDCOL + 1;

    //Need to add a property change listener to catch when paths go active.
    class TableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

        //needs a method to for when panel changes
        //need a method to delete an item
        //Possibly also to set a route.
        //Add a propertychange listener to hear when the route goes active.
        TableModel(LayoutEditor panel) {
            setPanel(panel);
            nxPairs.addPropertyChangeListener(this);
            source = nxPairs.getNxSource(panel);
            dest = nxPairs.getNxDestination();
        }

        void setPanel(LayoutEditor panel) {
            if (this.panel == panel) {
                return;
            }
            this.panel = panel;
            rowCount = nxPairs.getNxPairNumbers(panel);
            updateNameList();
            fireTableDataChanged();
        }

        LayoutEditor panel;

        List<Object> source = null;
        List<Object> dest = null;

        void updateNameList() {
            source = nxPairs.getNxSource(panel);
            dest = nxPairs.getNxDestination();
        }

        int rowCount = 0;

        @Override
        public int getRowCount() {
            return rowCount;
        }

        public void configureTable(JTable table) {
            // allow reordering of the columns
            table.getTableHeader().setReorderingAllowed(true);

            // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            // resize columns as requested
            for (int i = 0; i < table.getColumnCount(); i++) {
                int width = getPreferredWidth(i);
                table.getColumnModel().getColumn(i).setPreferredWidth(width);
            }
            table.sizeColumnsToFit(-1);

            configDeleteColumn(table);

        }

        @Override
        public Object getValueAt(int row, int col) {
            // get roster entry for row
            if (panel == null) {
                log.debug("no panel selected!");  // NOI18N
                return Bundle.getMessage("ErrorTitle");  // NOI18N
            }
            switch (col) {
                case FROMPOINTCOL:
                    return nxPairs.getPointAsString((NamedBean) source.get(row), panel);
                case TOPOINTCOL:
                    return nxPairs.getPointAsString((NamedBean) dest.get(row), panel);
                case ACTIVECOL:
                    return isPairActive(row);
                case BOTHWAYCOL:
                    return !nxPairs.isUniDirection(source.get(row), panel, dest.get(row));
                case ENABLEDCOL:
                    return !nxPairs.isEnabled(source.get(row), panel, dest.get(row));
                case CLEARCOL:
                    return Bundle.getMessage("ButtonClear");  // NOI18N
                case DELETECOL:
                    return Bundle.getMessage("ButtonDelete");  // NOI18N
                case TYPECOL:
                    return NXTYPE_NAMES[nxPairs.getEntryExitType(source.get(row), panel, dest.get(row))];
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == DELETECOL) {
                // button fired, delete Bean
                deleteEntryExit(row, col);
            }
            if (col == CLEARCOL) {
                nxPairs.cancelInterlock(source.get(row), panel, dest.get(row));
            }
            if (col == BOTHWAYCOL) {
                boolean b = !((Boolean) value);
                nxPairs.setUniDirection(source.get(row), panel, dest.get(row), b);
            }
            if (col == ENABLEDCOL) {
                boolean b = !((Boolean) value);
                nxPairs.setEnabled(source.get(row), panel, dest.get(row), b);
            }
            if (col == TYPECOL) {
                String val = (String) value;
                if (val.equals(Bundle.getMessage("SetTurnoutsOnly"))) { // I18N matching needs if-else  // NOI18N
                    nxPairs.setEntryExitType(source.get(row), panel, dest.get(row), 0x00);
                } else if (val.equals(Bundle.getMessage("SetTurnoutsAndSignalMasts"))) {  // NOI18N
                    nxPairs.setEntryExitType(source.get(row), panel, dest.get(row), 0x01);
                } else if (val.equals(Bundle.getMessage("FullInterlock"))) {
                    nxPairs.setEntryExitType(source.get(row), panel, dest.get(row), 0x02);  // NOI18N
                }
            }
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case FROMPOINTCOL:
                case TOPOINTCOL:
                    return new JTextField(15).getPreferredSize().width;
                case ACTIVECOL:
                case BOTHWAYCOL:
                case ENABLEDCOL:
                    return new JTextField(5).getPreferredSize().width;
                case CLEARCOL:
                case DELETECOL:  //
                    return new JTextField(22).getPreferredSize().width;
                case TYPECOL:
                    return new JTextField(10).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: " + col);  // NOI18N
                    return new JTextField(8).getPreferredSize().width;
            }
        }

        protected void deleteEntryExit(int row, int col) {
            NamedBean nbSource = ((NamedBean) source.get(row));
            NamedBean nbDest = (NamedBean) dest.get(row);
            nxPairs.deleteNxPair(nbSource, nbDest, panel);
        }

        String isPairActive(int row) {
            if (nxPairs.isPathActive(source.get(row), dest.get(row), panel)) {
                return (Bundle.getMessage("ButtonYes")); // "Yes"  // NOI18N
            }
            return ("");
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case FROMPOINTCOL:
                    return Bundle.getMessage("ColumnFrom");  // NOI18N
                case TOPOINTCOL:
                    return Bundle.getMessage("ColumnTo");  // NOI18N
                case ACTIVECOL:
                    return Bundle.getMessage("SensorStateActive"); // "Active"  // NOI18N
                case DELETECOL:
                    return "";
                case CLEARCOL:
                    return "";
                case BOTHWAYCOL:
                    return Bundle.getMessage("ColumnBoth");  // NOI18N
                case TYPECOL:
                    return Bundle.getMessage("NXType");  // NOI18N
                case ENABLEDCOL:
                    return Bundle.getMessage("Disabled");  // NOI18N
                default:
                    return "<UNKNOWN>";  // NOI18N
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case FROMPOINTCOL:
                case TOPOINTCOL:
                case ACTIVECOL:
                    return String.class;
                case DELETECOL:
                case CLEARCOL:
                    return JButton.class;
                case BOTHWAYCOL:
                case ENABLEDCOL:
                    return Boolean.class;
                case TYPECOL:
                    return String.class;
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case BOTHWAYCOL:
                    Object obj = nxPairs.getEndPointLocation((NamedBean) dest.get(row), panel);
                    if (obj instanceof PositionablePoint) {
                        PositionablePoint point = (PositionablePoint) obj;
                        if (point.getType() == PositionablePoint.END_BUMPER) {
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("EndBumperPoint"));  // NOI18N
                            return false;
                        }
                    }
                    if (!nxPairs.canBeBiDirectional(source.get(row), panel, dest.get(row))) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("BothWayTurnoutOnly"));  // NOI18N
                        return false;
                    }
                    /*if(nxPairs.getEntryExitType(source.get(row), panel, dest.get(row))!=0x00){
                     JOptionPane.showMessageDialog(null, Bundle.getMessage("BothWayTurnoutOnly"));
                     return false;
                     }*/
                    return true;
                case DELETECOL:
                case CLEARCOL:
                case ENABLEDCOL:
                case TYPECOL:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public int getColumnCount() {
            return NUMCOL;
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length") || e.getPropertyName().equals("active")) {  // NOI18N
                rowCount = nxPairs.getNxPairNumbers(panel);
                updateNameList();
                fireTableDataChanged();
            }
        }
    }

    String[] NXTYPE_NAMES = {Bundle.getMessage("SetTurnoutsOnly"), Bundle.getMessage("SetTurnoutsAndSignalMasts"), Bundle.getMessage("FullInterlock")};  // NOI18N
    // Picked up in setValueAt() to read back from table

    protected void configDeleteColumn(JTable table) {
        // have the delete column hold a button
        setColumnToHoldButton(table, DELETECOL,
                new JButton(Bundle.getMessage("ButtonDelete")));  // NOI18N

        setColumnToHoldButton(table, CLEARCOL,
                new JButton(Bundle.getMessage("ButtonClear")));  // NOI18N

        JComboBox<String> typeCombo = new JComboBox<>(NXTYPE_NAMES);

        TableColumn col = table.getColumnModel().getColumn(TYPECOL);
        col.setCellEditor(new DefaultCellEditor(typeCombo));
    }

    /**
     * Service method to set up a column so that it will hold a button for it's
     * values.
     *
     * @param table  the table
     * @param column the column
     * @param sample Typical button, used for size
     */
    protected void setColumnToHoldButton(JTable table, int column, JButton sample) {
        //TableColumnModel tcm = table.getColumnModel();
        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        table.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        table.setDefaultEditor(JButton.class, buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(sample.getPreferredSize().height);
        table.getColumnModel().getColumn(column)
                .setPreferredWidth((sample.getPreferredSize().width) + 4);
    }

    static class ValidPoints {

        NamedBean bean;
        String description;

        ValidPoints(NamedBean bean, String description) {
            this.bean = bean;
            this.description = description;
        }

        NamedBean getPoint() {
            return bean;
        }

        String getDescription() {
            return description;
        }
    }

    // Variables for the Options menu item
    JmriJFrame optionsFrame = null;
    Container optionsPane = null;
    String[] clearOptions = {Bundle.getMessage("PromptUser"), Bundle.getMessage("ClearRoute"),  // NOI18N
             Bundle.getMessage("CancelRoute"), Bundle.getMessage("StackRoute")};  // NOI18N
    JComboBox<String> clearEntry = new JComboBox<>(clearOptions);
    JTextField durationSetting = new JTextField(10);
    String[] colorText = {"ColorClear", "Black", "DarkGray", "Gray",  // NOI18N
        "LightGray", "White", "Red", "Pink", "Orange",  // NOI18N
        "Yellow", "Green", "Blue", "Magenta", "Cyan"}; // NOI18N
    Color[] colorCode = {null, Color.black, Color.darkGray, Color.gray,
        Color.lightGray, Color.white, Color.red, Color.pink, Color.orange,
        Color.yellow, Color.green, Color.blue, Color.magenta, Color.cyan};
    int numColors = 14;  // number of entries in the above arrays
    JCheckBox dispatcherUse = new JCheckBox(Bundle.getMessage("DispatcherInt"));  // NOI18N

    JComboBox<String> settingTrackColorBox = new JComboBox<>();

    private void initializeColorCombo(JComboBox<String> colorCombo) {
        colorCombo.removeAllItems();
        for (int i = 0; i < numColors; i++) {
            colorCombo.addItem(Bundle.getMessage(colorText[i])); // I18N using Bundle.getMessage from higher level color list
        }
    }

    private void setColorCombo(JComboBox<String> colorCombo, Color color) {
        for (int i = 0; i < numColors; i++) {
            if (color == colorCode[i]) {
                colorCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private Color getSelectedColor(JComboBox<String> colorCombo) {
        return (colorCode[colorCombo.getSelectedIndex()]);
    }

    /**
     * Build the Options window
     * @param e the action event
     */
    protected void optionWindow(ActionEvent e) {
        if (optionsFrame == null) {
            optionsFrame = new JmriJFrame(Bundle.getMessage("OptionsTitle"), false, true);  // NOI18N
            //optionsFrame.addHelpMenu("package.jmri.jmrit.dispatcher.Options", true);
            optionsPane = optionsFrame.getContentPane();
            optionsPane.setLayout(new BoxLayout(optionsFrame.getContentPane(), BoxLayout.Y_AXIS));
            clearEntry.setSelectedIndex(nxPairs.getClearDownOption());
            JPanel p1 = new JPanel();
            //clearEntry.addActionListener(clearEntryListener);
            clearEntry.setToolTipText(Bundle.getMessage("ReselectionTip"));  // NOI18N
            p1.add(new JLabel(Bundle.getMessage("Reselection")));  // NOI18N
            p1.add(clearEntry);
            optionsPane.add(p1);
            JPanel p2 = new JPanel();
            initializeColorCombo(settingTrackColorBox);
            setColorCombo(settingTrackColorBox, nxPairs.getSettingRouteColor());
            ActionListener settingTrackColorListener = (ActionEvent e1) -> {
                if (getSelectedColor(settingTrackColorBox) != null) {
                    durationSetting.setEnabled(true);
                } else {
                    durationSetting.setEnabled(false);
                }
            };

            settingTrackColorBox.addActionListener(settingTrackColorListener);
            p2.add(new JLabel(Bundle.getMessage("RouteSetColour")));  // NOI18N
            p2.add(settingTrackColorBox);
            optionsPane.add(p2);
            durationSetting.setText("" + nxPairs.getSettingTimer());
            if (nxPairs.useDifferentColorWhenSetting()) {
                durationSetting.setEnabled(true);
            } else {
                durationSetting.setEnabled(false);
            }
            JPanel p3 = new JPanel();
            p3.add(new JLabel(Bundle.getMessage("SettingDuration")));  // NOI18N
            p3.add(durationSetting);
            optionsPane.add(p3);

            JPanel p4 = new JPanel();
            p4.add(dispatcherUse);
            dispatcherUse.setSelected(nxPairs.getDispatcherIntegration());
            optionsPane.add(p4);

            JButton ok = new JButton(Bundle.getMessage("ButtonOK"));  // NOI18N
            optionsPane.add(ok);
            ok.addActionListener((ActionEvent e1) -> {
                optionSaveButton();
            });
        }
        optionsFrame.pack();
        optionsFrame.setVisible(true);
    }

    /**
     * Save the option updates
     */
    void optionSaveButton() {
        int settingTimer = 2000; // in milliseconds
        try {
            settingTimer = Integer.parseInt(durationSetting.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ValueBeNumber"));  // NOI18N
            return;
        }
        nxPairs.setSettingTimer(settingTimer);
        nxPairs.setSettingRouteColor(getSelectedColor(settingTrackColorBox));
        nxPairs.setClearDownOption(clearEntry.getSelectedIndex());
        nxPairs.setDispatcherIntegration(dispatcherUse.isSelected());
        optionsFrame.setVisible(false);

    }

    private final static Logger log = LoggerFactory.getLogger(AddEntryExitPairPanel.class);
}
