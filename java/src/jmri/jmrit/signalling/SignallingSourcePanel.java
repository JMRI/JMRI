package jmri.jmrit.signalling;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import jmri.InstanceManager;
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.swing.RowSorterUtil;
import jmri.util.JmriJFrame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for the Signal Mast Table - Edit Logic Pane.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Egbert Broerse Copyright (C) 2017
 */
public class SignallingSourcePanel extends jmri.util.swing.JmriPanel implements PropertyChangeListener {

    SignalMastLogic sml;
    SignalMast sourceMast;
    JLabel fixedSourceMastLabel = new JLabel();

    JButton discoverPairs = new JButton(Bundle.getMessage("ButtonDiscover"));  // NOI18N

    SignalMastAspectModel _AppearanceModel;
    JScrollPane _SignalAppearanceScrollPane;

    /**
     * Create a Signalling Source configuration Pane showing a list of defined
     * destination masts and allowing creation of new source-destination pairs
     * as well as showing a button to start Autodetect configuration.
     * @param sourceMast The source mast for this SML Source Pairs pane
     */
    public SignallingSourcePanel(final SignalMast sourceMast) {
        super();
        sml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sourceMast);
        this.sourceMast = sourceMast;
        fixedSourceMastLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("SourceMast")) + " " + sourceMast.getDisplayName());  // NOI18N
        if (sml != null) {
            _signalMastList = sml.getDestinationList();
        }

        InstanceManager.getDefault(LayoutBlockManager.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel sourcePanel = new JPanel();
        sourcePanel.add(fixedSourceMastLabel);
        header.add(sourcePanel);
        add(header, BorderLayout.NORTH);

        _AppearanceModel = new SignalMastAspectModel();
        JTable table = new JTable(_AppearanceModel);
        TableRowSorter<SignalMastAspectModel> sorter = new TableRowSorter<>(_AppearanceModel);
        RowSorterUtil.setSortOrder(sorter, SignalMastAspectModel.SYSNAME_COLUMN, SortOrder.ASCENDING);
        table.setRowSorter(sorter);
        table.setRowSelectionAllowed(false);
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(600, 120));
        _AppearanceModel.configureTable(table);
        _SignalAppearanceScrollPane = new JScrollPane(table);
        _AppearanceModel.fireTableDataChanged();
        add(_SignalAppearanceScrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel();

        footer.add(discoverPairs);
        discoverPairs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                discoverPressed(e);
            }
        });

        JButton addLogic = new JButton(Bundle.getMessage("AddLogic"));  // NOI18N
        footer.add(addLogic);
        addLogic.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                class WindowMaker implements Runnable {

                    WindowMaker() {
                    }

                    @Override
                    public void run() {
                        SignallingAction sigLog = new SignallingAction(); // opens a frame, opens a panel in that frame
                        sigLog.setMast(sourceMast, null);
                        sigLog.actionPerformed(null);
                        // unable to receive changes directly from created panel, so listen to common SML ancestor
                    }
                }
                WindowMaker t = new WindowMaker();
                javax.swing.SwingUtilities.invokeLater(t);
            }
        });

        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    public void dispose() {
        InstanceManager.getDefault(LayoutBlockManager.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).removePropertyChangeListener(this);
        super.dispose();
    }

    JmriJFrame signalMastLogicFrame = null;
    JLabel sourceLabel = new JLabel();

    /**
     * Respond to the Discover button being pressed.
     * Check whether AdvancedRouting is turned on and any Layout Editor Panels
     * are present. For each LE Panel, call discoverSignallingDest()
     * {@link jmri.SignalMastLogicManager#discoverSignallingDest(SignalMast, LayoutEditor)}
     * @param e The button event
     */
    void discoverPressed(ActionEvent e) {
        if (!InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
            int response = JOptionPane.showConfirmDialog(null, Bundle.getMessage("EnableLayoutBlockRouting"));  // NOI18N
            if (response == 0) {
                InstanceManager.getDefault(LayoutBlockManager.class).enableAdvancedRouting(true);
                JOptionPane.showMessageDialog(null, Bundle.getMessage("LayoutBlockRoutingEnabledShort"));  // NOI18N
            }
        }

        List<LayoutEditor> layout = InstanceManager.getDefault(PanelMenu.class).getLayoutEditorPanelList();
        if (layout.size() > 0) {
            signalMastLogicFrame = new JmriJFrame(Bundle.getMessage("DiscoverMastsTitle"), false, false);  // NOI18N
            signalMastLogicFrame.setPreferredSize(null);
            JPanel panel1 = new JPanel();
            sourceLabel = new JLabel(Bundle.getMessage("DiscoveringMasts"));  // NOI18N
            sourceLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
            panel1.add(sourceLabel);
            signalMastLogicFrame.add(sourceLabel);
            signalMastLogicFrame.pack();
            signalMastLogicFrame.setVisible(true);

            for (int i = 0; i < layout.size(); i++) {
                try {
                    InstanceManager.getDefault(jmri.SignalMastLogicManager.class).discoverSignallingDest(sourceMast, layout.get(i));
                } catch (jmri.JmriException ex) {
                    signalMastLogicFrame.setVisible(false);
                    JOptionPane.showMessageDialog(null, ex.toString());
                }
            }
            signalMastLogicFrame.setVisible(false);
            signalMastLogicFrame.dispose();
            JOptionPane.showMessageDialog(null, Bundle.getMessage("GenComplete"));  // NOI18N
        } else {
            // don't take the trouble of searching
            JOptionPane.showMessageDialog(null, Bundle.getMessage("GenSkipped"));  // NOI18N
        }
    }

    /**
     * Listen for property changes in the SML that's being configured.
     * @param e The button event
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("autoSignalMastGenerateComplete")) {  // NOI18N
            if (sml == null) {
                updateDetails();
            }
            log.debug("Generate complete for a LE panel ({}): mast = {}", this.hashCode(), sourceMast);
        }
        if (e.getPropertyName().equals("advancedRoutingEnabled")) {  // NOI18N
            boolean newValue = (Boolean) e.getNewValue();
            discoverPairs.setEnabled(newValue);
        }
        log.debug("SSP 173 Event: {}; Source: {}", e.getPropertyName(), e.toString()); // doesn't get notified, newDestination  // NOI18N
        if (e.getPropertyName().equals("length")) { // redraw the Pairs table  // NOI18N
            updateDetails();
        }
    }

    private List<SignalMast> _signalMastList;

    /**
     * Refresh the list of destination Signal Masts available for edit in the current SML.
     */
    private void updateDetails() {
        SignalMastLogic old = sml;
        sml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sourceMast);
        if (sml != null) {
            _signalMastList = sml.getDestinationList();
            _AppearanceModel.updateSignalMastLogic(old, sml);
        }
    }

    /**
     * TableModel to store SML control Signal Masts and their Set To Aspect.
     */
    public class SignalMastAspectModel extends AbstractTableModel implements PropertyChangeListener {

        SignalMastAspectModel() {
            super();
            if (sml != null) {
                sml.addPropertyChangeListener(this); // pick up creation of a new pair in the sml
            }
        }

        void updateSignalMastLogic(SignalMastLogic smlOld, SignalMastLogic smlNew) {
            if (smlOld != null) {
                smlOld.removePropertyChangeListener(this);
            }
            if (smlNew != null) {
                smlNew.addPropertyChangeListener(this);
            }
            fireTableDataChanged();
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == ACTIVE_COLUMN) {
                return Boolean.class;
            }
            if (c == ENABLE_COLUMN) {
                return Boolean.class;
            }
            if (c == EDIT_COLUMN) {
                return JButton.class;
            }
            if (c == DEL_COLUMN) {
                return JButton.class;
            }
            return String.class;
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

            configEditColumn(table);
        }

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                                justification="better to keep cases in column order rather than to combine")
        public int getPreferredWidth(int col) {
            switch (col) {
                case SYSNAME_COLUMN:
                    return new JTextField(15).getPreferredSize().width;
                case ENABLE_COLUMN:
                case ACTIVE_COLUMN:
                    return new JTextField(5).getPreferredSize().width;
                case USERNAME_COLUMN:
                    return new JTextField(15).getPreferredSize().width;
                case EDIT_COLUMN: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                    return new JTextField(22).getPreferredSize().width;
                case DEL_COLUMN: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                    return new JTextField(22).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: " + col);  // NOI18N
                    return new JTextField(8).getPreferredSize().width;
            }
        }

        @Override
        public String getColumnName(int col) {
            if (col == USERNAME_COLUMN) {
                return Bundle.getMessage("ColumnUserName");  // NOI18N
            }
            if (col == SYSNAME_COLUMN) {
                return Bundle.getMessage("DestMast");  // NOI18N
            }
            if (col == ACTIVE_COLUMN) {
                return Bundle.getMessage("SensorStateActive"); // "Active"  // NOI18N
            }
            if (col == ENABLE_COLUMN) {
                return Bundle.getMessage("ColumnHeadEnabled");  // NOI18N
            }
            if (col == EDIT_COLUMN) {
                return ""; //no title above Edit buttons
            }
            if (col == DEL_COLUMN) {
                return ""; //no title above Delete buttons
            }
            return "";
        }

        /**
         * Remove references to and from this object, so that it can eventually be
         * garbage-collected.
         */
        public void dispose() {
            if (sml != null) {
                sml.removePropertyChangeListener(this);
            }
        }

        /**
         * Listen for changes to specific properties of the displayed Signal Masts.
         * @param e The ChangeEvent heard
         */
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {  // NOI18N
                // should pick up adding a new destination mast, but doesn't refresh table by itself
                _signalMastList = sml.getDestinationList();
                int length = (Integer) e.getNewValue();
                if (length == 0) {
                    sml.removePropertyChangeListener(this);
                    sml = null;
                }
                fireTableDataChanged();
            } else if (e.getPropertyName().equals("updatedDestination")) {  // NOI18N
                // a new NamedBean is available in the manager
                _signalMastList = sml.getDestinationList();
                fireTableDataChanged();
            } else if ((e.getPropertyName().equals("state")) || (e.getPropertyName().equals("Enabled"))) {  // NOI18N
                fireTableDataChanged();
                fireTableRowsUpdated(0, _signalMastList.size()-1);
            }
            log.debug("SSP 310 Event: {}", e.getPropertyName());  // NOI18N
        }

        /**
         * Display buttons in 2 columns of the manual control signal masts table.
         * @param table The control signal mast table to be configured
         */
        protected void configEditColumn(JTable table) {
            // have the Delete column hold a button
            setColumnToHoldButton(table, EDIT_COLUMN,
                    new JButton(Bundle.getMessage("ButtonEdit")));  // NOI18N
            setColumnToHoldButton(table, DEL_COLUMN,
                    new JButton(Bundle.getMessage("ButtonDelete")));  // NOI18N
        }

        /**
         * Helper function for {@link #configEditColumn(JTable)}.
         * @param table  The control signal mast table to be configured
         * @param column Index for the column to put the button in
         * @param sample JButton to put there
         */
        protected void setColumnToHoldButton(JTable table, int column, JButton sample) {
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

        /**
         * Get the number of columns in the signal masts table.
         * @return Fixed value of 6
         */
        @Override
        public int getColumnCount() {
            return 6;
        }

        /**
         * Query whether the cells in a table column should respond to clicks.
         * @param r Index for the cell row
         * @param c Index for the cell column
         */
        @Override
        public boolean isCellEditable(int r, int c) {
            if (c == EDIT_COLUMN) {
                return true;
            }
            if (c == DEL_COLUMN) {
                return true;
            }
            if (c == ENABLE_COLUMN) {
                return true;
            }
            return ((c == USERNAME_COLUMN));
        }

        /**
         * Respond to the Edit Logic button being clicked.
         * @param r Index for the cell row
         */
        protected void editPair(int r) {

            class WindowMaker implements Runnable {

                int row;

                WindowMaker(int r) {
                    row = r;
                }

                @Override
                public void run() {
                    log.debug("SML Edit existing logic started");  // NOI18N
                    SignallingAction sigLog = new SignallingAction();
                    sigLog.setMast(sourceMast, _signalMastList.get(row));
                    sigLog.actionPerformed(null);
                    // Note: we cannot tell if Edit pair was cancelled
                }
            }
            WindowMaker t = new WindowMaker(r);
            javax.swing.SwingUtilities.invokeLater(t);
        }

        protected void deletePair(int r) {
            InstanceManager.getDefault(jmri.SignalMastLogicManager.class).removeSignalMastLogic(sml, _signalMastList.get(r));
        }

        public static final int SYSNAME_COLUMN = 0;
        public static final int USERNAME_COLUMN = 1;
        public static final int ACTIVE_COLUMN = 2;
        public static final int ENABLE_COLUMN = 3;
        public static final int EDIT_COLUMN = 4;
        public static final int DEL_COLUMN = 5;

        public void setSetToState(String x) {
        }

        /**
         * Get the number of Included signal masts for this SML.
         */
        @Override
        public int getRowCount() {
            if (_signalMastList == null) {
                return 0;
            }
            return _signalMastList.size();
        }

        /**
         * Retrieve the contents to display in a cell in the table, in terms of model
         * @param r index for the cell row
         * @param c index for the cell column
         * @return The value (text) stored in the cell
         */
        @Override
        public Object getValueAt(int r, int c) {
            if (sml == null) {
                return null;
            }
            // some error checking
            if (r >= _signalMastList.size()) {
                log.debug("row is greater than turnout list size");  // NOI18N
                return null;
            }
            switch (c) {
                case USERNAME_COLUMN:
                    return _signalMastList.get(r).getUserName();
                case SYSNAME_COLUMN:  // slot number
                    return _signalMastList.get(r).getSystemName();
                case ACTIVE_COLUMN:
                    return sml.isActive(_signalMastList.get(r));
                case ENABLE_COLUMN:
                    return sml.isEnabled(_signalMastList.get(r));
                case EDIT_COLUMN:
                    return Bundle.getMessage("ButtonEdit");  // NOI18N
                case DEL_COLUMN:
                    return Bundle.getMessage("ButtonDelete");  // NOI18N
                default:
                    return null;
            }
        }

        /**
         * Process the contents from a table cell, in terms of model
         * @param type the object type of the cell contents
         * @param r index for the cell row
         * @param c index for the cell column
         */
        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c == EDIT_COLUMN) {
                editPair(r);
            } else if (c == DEL_COLUMN) {
                deletePair(r);
            } else if (c == ENABLE_COLUMN) {
                boolean b = ((Boolean) type).booleanValue();
                if (b) {
                    sml.setEnabled(_signalMastList.get(r));
                } else {
                    sml.setDisabled(_signalMastList.get(r));
                }

            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignallingSourcePanel.class);

}
