// AddSensorPanel.java
package jmri.jmrit.signalling;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.ResourceBundle;
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
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.util.JmriJFrame;
import jmri.util.com.sun.TableSorter;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanel to create a new JMRI devices HiJacked to serve other beantable tables.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version $Revision: 1.2 $
 */
public class AddEntryExitPairPanel extends jmri.util.swing.JmriPanel {

    /**
     *
     */
    private static final long serialVersionUID = 4871721972825766572L;
    JComboBox<String> selectPanel = new JComboBox<String>();
    JComboBox<String> fromPoint = new JComboBox<String>();
    JComboBox<String> toPoint = new JComboBox<String>();

    String[] interlockTypes = {"Set Turnouts Only", "Set Turnouts and Signal Masts", "Full Interlock"};
    JComboBox<String> typeBox = new JComboBox<String>(interlockTypes);

    ArrayList<LayoutEditor> panels;

    EntryExitPairs nxPairs = jmri.InstanceManager.getDefault(jmri.jmrit.signalling.EntryExitPairs.class);

    protected static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.EntryExitBundle");

    public AddEntryExitPairPanel(LayoutEditor panel) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel top = new JPanel();
        top.setLayout(new GridLayout(6, 2));

        top.add(new JLabel(rb.getString("SelectPanel")));
        top.add(selectPanel);
        selectPanel.removeAllItems();
        panels = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
        for (int i = 0; i < panels.size(); i++) {
            selectPanel.addItem(panels.get(i).getLayoutName());
        }
        if (panel != null) {
            selectPanel.setSelectedItem(panel.getLayoutName());
        }

        top.add(new JLabel(rb.getString("FromLocation")));
        top.add(fromPoint);
        ActionListener selectPanelListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectPointsFromPanel();
                nxModel.setPanel(panels.get(selectPanel.getSelectedIndex()));
            }
        };
        selectPointsFromPanel();
        selectPanel.addActionListener(selectPanelListener);

        top.add(new JLabel(rb.getString("ToLocation")));
        top.add(toPoint);
        top.add(new JLabel("NX Type"));
        top.add(typeBox);
        add(top);

        //add(top);
        JPanel p = new JPanel();
        JButton ok = new JButton(rb.getString("Add"));
        p.add(ok);
        ok.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addButton();
                    }
                });

        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JButton auto;
        p.add(auto = new JButton(rb.getString("AutoGenerate")));
        auto.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        autoDiscovery();
                    }
                });
        p.add(auto);
        add(p);
        nxModel = new TableModel(panel);
        nxSorter = new TableSorter(nxModel);
        nxDataTable = new JTable(nxSorter);
        nxSorter.setTableHeader(nxDataTable.getTableHeader());
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
            int response = JOptionPane.showConfirmDialog(null, rb.getString("EnableLayoutBlockRouting"));
            if (response == 0) {
                InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).enableAdvancedRouting(true);
                JOptionPane.showMessageDialog(null, rb.getString("LayoutBlockRoutingEnabled"));
            }
        }
        entryExitFrame = new jmri.util.JmriJFrame("Discover Entry Exit Pairs", false, false);
        entryExitFrame.setPreferredSize(null);
        JPanel panel1 = new JPanel();
        sourceLabel = new JLabel("Discovering Entry Exit Pairs");
        /*ImageIcon i;
         i = new ImageIcon(FileUtil.findURL("resources/icons/misc/gui3/process-working.gif"));
         JLabel label = new JLabel(); 
         label.setIcon(i); 
         panel1.add(label);*/
        panel1.add(sourceLabel);

        entryExitFrame.add(panel1);
        entryExitFrame.pack();
        entryExitFrame.setVisible(true);
        int retval = JOptionPane.showOptionDialog(null, rb.getString("AutoGenEntryExitMessage"), rb.getString("AutoGenEntryExitTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (retval == 0) {
            final PropertyChangeListener propertyNXListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("autoGenerateComplete")) {
                        if (entryExitFrame != null) {
                            entryExitFrame.setVisible(false);
                            entryExitFrame.dispose();
                        }
                        nxPairs.removePropertyChangeListener(this);
                        JOptionPane.showMessageDialog(null, "Generation of Entry Exit Pairs Completed");
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

    ArrayList<ValidPoints> validPoints = new ArrayList<ValidPoints>();

    private void selectPointsFromPanel() {
        if (selectPanel.getSelectedIndex() == -1) {
            return;
        }
        if (panel == panels.get(selectPanel.getSelectedIndex())) {
            return;
        }
        panel = panels.get(selectPanel.getSelectedIndex());
        fromPoint.removeAllItems();
        toPoint.removeAllItems();
        for (PositionablePoint pp : panel.pointList) {
            addPointToCombo(pp.getWestBoundSignalMastName(), pp.getWestBoundSensorName());
            addPointToCombo(pp.getEastBoundSignalMastName(), pp.getEastBoundSensorName());
        }

        for (LayoutTurnout t : panel.turnoutList) {
            addPointToCombo(t.getSignalAMastName(), t.getSensorAName());
            addPointToCombo(t.getSignalBMastName(), t.getSensorBName());
            addPointToCombo(t.getSignalCMastName(), t.getSensorCName());
            addPointToCombo(t.getSignalDMastName(), t.getSensorDName());
        }

        for (LevelXing xing : panel.xingList) {
            addPointToCombo(xing.getSignalAMastName(), xing.getSensorAName());
            addPointToCombo(xing.getSignalBMastName(), xing.getSensorBName());
            addPointToCombo(xing.getSignalCMastName(), xing.getSensorCName());
            addPointToCombo(xing.getSignalDMastName(), xing.getSensorDName());
        }
        for (LayoutSlip slip : panel.slipList) {
            addPointToCombo(slip.getSignalAMastName(), slip.getSensorAName());
            addPointToCombo(slip.getSignalBMastName(), slip.getSensorBName());
            addPointToCombo(slip.getSignalCMastName(), slip.getSensorCName());
            addPointToCombo(slip.getSignalDMastName(), slip.getSensorDName());
        }
    }

    void addPointToCombo(String signalMastName, String sensorName) {
        NamedBean source = null;
        if (sensorName != null && !sensorName.isEmpty()) {
            String description = sensorName;
            source = InstanceManager.sensorManagerInstance().getSensor(sensorName);
            if (signalMastName != null && !signalMastName.isEmpty()) {
                description = sensorName + " (" + signalMastName + ")";
            }
            validPoints.add(new ValidPoints(source, description));
            fromPoint.addItem(description);
            toPoint.addItem(description);
        }
    }

    TableSorter nxSorter;
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

        /**
         *
         */
        private static final long serialVersionUID = 3291217259103678604L;

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

        ArrayList<Object> source = null;
        ArrayList<Object> dest = null;

        void updateNameList() {
            source = nxPairs.getNxSource(panel);
            dest = nxPairs.getNxDestination();
        }

        int rowCount = 0;

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

        public Object getValueAt(int row, int col) {
            // get roster entry for row
            if (panel == null) {
                log.debug("no panel selected!");
                return "Error";
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
                    return Bundle.getMessage("ButtonClear");
                case DELETECOL:
                    return Bundle.getMessage("ButtonDelete");
                case TYPECOL:
                    return NXTYPE_NAMES[nxPairs.getEntryExitType(source.get(row), panel, dest.get(row))];
                default:
                    return "";
            }
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == DELETECOL) {
                // button fired, delete Bean
                deleteEntryExit(row, col);
            }
            if (col == CLEARCOL) {
                nxPairs.cancelInterlock(source.get(row), panel, dest.get(row));
            }
            if (col == BOTHWAYCOL) {
                boolean b = !((Boolean) value).booleanValue();
                nxPairs.setUniDirection(source.get(row), panel, dest.get(row), b);
            }
            if (col == ENABLEDCOL) {
                boolean b = !((Boolean) value).booleanValue();
                nxPairs.setEnabled(source.get(row), panel, dest.get(row), b);
            }
            if (col == TYPECOL) {
                String val = (String) value;
                if (val.equals("Turnout")) {
                    nxPairs.setEntryExitType(source.get(row), panel, dest.get(row), 0x00);
                } else if (val.equals("Signal Mast")) {
                    nxPairs.setEntryExitType(source.get(row), panel, dest.get(row), 0x01);
                } else if (val.equals("Full InterLock")) {
                    nxPairs.setEntryExitType(source.get(row), panel, dest.get(row), 0x02);
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
                    log.warn("Unexpected column in getPreferredWidth: " + col);
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
                return ("yes");
            }
            return ("");
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case FROMPOINTCOL:
                    return rb.getString("ColumnFrom");
                case TOPOINTCOL:
                    return rb.getString("ColumnTo");
                case ACTIVECOL:
                    return rb.getString("ColumnActive");
                case DELETECOL:
                    return "";
                case CLEARCOL:
                    return "";
                case BOTHWAYCOL:
                    return rb.getString("ColumnBoth");
                case TYPECOL:
                    return "NX Type";
                case ENABLEDCOL:
                    return "Disabled";
                default:
                    return "<UNKNOWN>";
            }
        }

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

        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case BOTHWAYCOL:
                    Object obj = nxPairs.getEndPointLocation((NamedBean) dest.get(row), panel);
                    if (obj instanceof PositionablePoint) {
                        PositionablePoint point = (PositionablePoint) obj;
                        if (point.getType() == PositionablePoint.END_BUMPER) {
                            JOptionPane.showMessageDialog(null, rb.getString("EndBumperPoint"));
                            return false;
                        }
                    }
                    if (!nxPairs.canBeBiDirectional(source.get(row), panel, dest.get(row))) {
                        JOptionPane.showMessageDialog(null, rb.getString("BothWayTurnoutOnly"));
                        return false;
                    }
                    /*if(nxPairs.getEntryExitType(source.get(row), panel, dest.get(row))!=0x00){
                     JOptionPane.showMessageDialog(null, rb.getString("BothWayTurnoutOnly"));
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

        public int getColumnCount() {
            return NUMCOL;
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length") || e.getPropertyName().equals("active")) {
                rowCount = nxPairs.getNxPairNumbers(panel);
                updateNameList();
                fireTableDataChanged();
            }
        }
    }

    String[] NXTYPE_NAMES = {"Turnout", "Signal Mast", "Full InterLock"};

    protected void configDeleteColumn(JTable table) {
        // have the delete column hold a button
        setColumnToHoldButton(table, DELETECOL,
                new JButton(rb.getString("ButtonDelete")));

        setColumnToHoldButton(table, CLEARCOL,
                new JButton(rb.getString("ButtonClear")));

        JComboBox<String> typeCombo = new JComboBox<String>(NXTYPE_NAMES);

        TableColumn col = table.getColumnModel().getColumn(TYPECOL);
        col.setCellEditor(new DefaultCellEditor(typeCombo));
    }

    /**
     * Service method to setup a column so that it will hold a button for it's
     * values
     *
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

    JmriJFrame optionsFrame = null;
    Container optionsPane = null;
    String[] clearOptions = {"Prompt User", "Clear Route", "Cancel Route"};
    JComboBox<String> clearEntry = new JComboBox<String>(clearOptions);
    JTextField durationSetting = new JTextField(10);
    String[] colorText = {"None", "Black", "DarkGray", "Gray",
        "LightGray", "White", "Red", "Pink", "Orange",
        "Yellow", "Green", "Blue", "Magenta", "Cyan"};
    Color[] colorCode = {null, Color.black, Color.darkGray, Color.gray,
        Color.lightGray, Color.white, Color.red, Color.pink, Color.orange,
        Color.yellow, Color.green, Color.blue, Color.magenta, Color.cyan};
    int numColors = 14;  // number of entries in the above arrays
    JCheckBox dispatcherUse = new JCheckBox(Bundle.getMessage("DispatcherInt"));

    JComboBox<String> settingTrackColorBox = new JComboBox<String>();

    private void initializeColorCombo(JComboBox<String> colorCombo) {
        colorCombo.removeAllItems();
        for (int i = 0; i < numColors; i++) {
            colorCombo.addItem(rb.getString(colorText[i]));
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
     * Utility methods for converting between string and color Note: These names
     * are only used internally, so don't need a resource bundle
     */
    protected void optionWindow(ActionEvent e) {
        if (optionsFrame == null) {
            optionsFrame = new JmriJFrame(Bundle.getMessage("OptionsTitle"), false, true);
            //optionsFrame.addHelpMenu("package.jmri.jmrit.dispatcher.Options", true);
            optionsPane = optionsFrame.getContentPane();
            optionsPane.setLayout(new BoxLayout(optionsFrame.getContentPane(), BoxLayout.Y_AXIS));
            clearEntry.setSelectedIndex(nxPairs.getClearDownOption());
            JPanel p1 = new JPanel();
            //clearEntry.addActionListener(clearEntryListener);
            clearEntry.setToolTipText("set the action for when the NX buttons are reselected");
            p1.add(new JLabel(Bundle.getMessage("Reselection")));
            p1.add(clearEntry);
            optionsPane.add(p1);
            JPanel p2 = new JPanel();
            initializeColorCombo(settingTrackColorBox);
            setColorCombo(settingTrackColorBox, nxPairs.getSettingRouteColor());
            ActionListener settingTrackColorListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {

                    if (getSelectedColor(settingTrackColorBox) != null) {
                        durationSetting.setEnabled(true);
                    } else {
                        durationSetting.setEnabled(false);
                    }
                }
            };

            settingTrackColorBox.addActionListener(settingTrackColorListener);
            p2.add(new JLabel(Bundle.getMessage("RouteSetColour")));
            p2.add(settingTrackColorBox);
            optionsPane.add(p2);
            durationSetting.setText("" + nxPairs.getSettingTimer());
            if (nxPairs.useDifferentColorWhenSetting()) {
                durationSetting.setEnabled(true);
            } else {
                durationSetting.setEnabled(false);
            }
            JPanel p3 = new JPanel();
            p3.add(new JLabel(Bundle.getMessage("SettingDuration")));
            p3.add(durationSetting);
            optionsPane.add(p3);

            JPanel p4 = new JPanel();
            p4.add(dispatcherUse);
            dispatcherUse.setSelected(nxPairs.getDispatcherIntegration());
            optionsPane.add(p4);

            JButton ok = new JButton(Bundle.getMessage("ButtonOkay"));
            optionsPane.add(ok);
            ok.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            optionSaveButton();
                        }
                    });
        }
        optionsFrame.pack();
        optionsFrame.setVisible(true);
    }

    void optionSaveButton() {
        int settingTimer = 2000;
        try {
            settingTimer = Integer.parseInt(durationSetting.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ValueBeNumber"));
            return;
        }
        nxPairs.setSettingTimer(settingTimer);
        nxPairs.setSettingRouteColor(getSelectedColor(settingTrackColorBox));
        nxPairs.setClearDownOption(clearEntry.getSelectedIndex());
        nxPairs.setDispatcherIntegration(dispatcherUse.isSelected());
        optionsFrame.setVisible(false);

    }

    private final static Logger log = LoggerFactory.getLogger(AddEntryExitPairPanel.class.getName());
}


/* @(#)AddNewHardwareDevicePanel.java */
