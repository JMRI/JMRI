package jmri.jmrit.signalling;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.ResourceBundle;
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
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.swing.RowSorterUtil;
import jmri.util.JmriJFrame;
import jmri.util.SystemNameComparator;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for the Signal Mast Table - Edit Logic Pane.
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 * @author	Egbert Broerse Copyright (C) 2017
 */
public class SignallingSourcePanel extends jmri.util.swing.JmriPanel implements PropertyChangeListener {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");

    SignalMastLogic sml;
    SignalMast sourceMast;
    JLabel fixedSourceMastLabel = new JLabel();

    JButton discoverPairs = new JButton(rb.getString("ButtonDiscover"));

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
        sml = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sourceMast);
        this.sourceMast = sourceMast;
        fixedSourceMastLabel = new JLabel(Bundle.getMessage("SourceMast") + ": " + sourceMast.getDisplayName());
        if (sml != null) {
            _signalMastList = sml.getDestinationList();
        }

        jmri.InstanceManager.getDefault(LayoutBlockManager.class).addPropertyChangeListener(this);
        jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).addPropertyChangeListener(this);

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
        sorter.setComparator(SignalMastAspectModel.SYSNAME_COLUMN, new SystemNameComparator());
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

        JButton addLogic = new JButton(rb.getString("AddLogic"));
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
        jmri.InstanceManager.getDefault(LayoutBlockManager.class).removePropertyChangeListener(this);
        jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).removePropertyChangeListener(this);
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
        if (!jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
            int response = JOptionPane.showConfirmDialog(null, rb.getString("EnableLayoutBlockRouting"));
            if (response == 0) {
                jmri.InstanceManager.getDefault(LayoutBlockManager.class).enableAdvancedRouting(true);
                JOptionPane.showMessageDialog(null, rb.getString("LayoutBlockRoutingEnabledShort"));
            }
        }

        ArrayList<LayoutEditor> layout = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
        if (layout.size() > 0) {
            signalMastLogicFrame = new JmriJFrame(rb.getString("DiscoverMastsTitle"), false, false);
            signalMastLogicFrame.setPreferredSize(null);
            JPanel panel1 = new JPanel();
            sourceLabel = new JLabel(rb.getString("DiscoveringMasts"));
            sourceLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
            panel1.add(sourceLabel);
            signalMastLogicFrame.add(sourceLabel);
            signalMastLogicFrame.pack();
            signalMastLogicFrame.setVisible(true);

            jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).addPropertyChangeListener(this);
            for (int i = 0; i < layout.size(); i++) {
                try {
                    jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).discoverSignallingDest(sourceMast, layout.get(i));
                    sourceLabel.setText(rb.getString("DiscoveringMasts") + " (" + i + "/" + layout.size() + ")"); // indicate progress
                } catch (jmri.JmriException ex) {
                    signalMastLogicFrame.setVisible(false);
                    JOptionPane.showMessageDialog(null, ex.toString());
                }
            }
            jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).removePropertyChangeListener(this);
        } else {
            // don't take the trouble of searching
            JOptionPane.showMessageDialog(null, rb.getString("GenSkipped"));
        }
    }

    /**
     * Listen for property changes in the SML that's being configured.
     * @param e The button event
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("autoSignalMastGenerateComplete")) {
            signalMastLogicFrame.setVisible(false);
            signalMastLogicFrame.dispose();

            if (sml == null) {
                updateDetails();
            }
            JOptionPane.showMessageDialog(null, rb.getString("GenComplete"));
        }
        if (e.getPropertyName().equals("advancedRoutingEnabled")) {
            boolean newValue = (Boolean) e.getNewValue();
            discoverPairs.setEnabled(newValue);
        }
        log.debug("SSP 173 Event: {}; Source: {}", e.getPropertyName(), e.toString()); // doesn't get notified, newDestination
        if (e.getPropertyName().equals("length")) { // redraw the Pairs table
            updateDetails();
        }
    }

    private ArrayList<SignalMast> _signalMastList;

    /**
     * Refresh the list of destination Signal Masts available for edit in the current SML.
     */
    private void updateDetails() {
        SignalMastLogic old = sml;
        sml = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sourceMast);
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
                    log.warn("Unexpected column in getPreferredWidth: " + col);
                    return new JTextField(8).getPreferredSize().width;
            }
        }

        @Override
        public String getColumnName(int col) {
            if (col == USERNAME_COLUMN) {
                return Bundle.getMessage("ColumnUserName");
            }
            if (col == SYSNAME_COLUMN) {
                return Bundle.getMessage("DestMast");
            }
            if (col == ACTIVE_COLUMN) {
                return Bundle.getMessage("SensorStateActive"); // "Active"
            }
            if (col == ENABLE_COLUMN) {
                return Bundle.getMessage("ColumnHeadEnabled");
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
            if (e.getPropertyName().equals("length")) {
                // should pick up adding a new destination mast, but doesn't refresh table by itself
                _signalMastList = sml.getDestinationList();
                int length = (Integer) e.getNewValue();
                if (length == 0) {
                    sml.removePropertyChangeListener(this);
                    sml = null;
                }
                fireTableDataChanged();
            } else if (e.getPropertyName().equals("updatedDestination")) {
                // a new NamedBean is available in the manager
                _signalMastList = sml.getDestinationList();
                fireTableDataChanged();
            } else if ((e.getPropertyName().equals("state")) || (e.getPropertyName().equals("Enabled"))) {
                fireTableDataChanged();
                fireTableRowsUpdated(0, _signalMastList.size()-1);
            }
            log.debug("SSP 310 Event: {}", e.getPropertyName());
        }

        /**
         * Display buttons in 2 columns of the manual control signal masts table.
         * @param table The control signal mast table to be configured
         */
        protected void configEditColumn(JTable table) {
            // have the Delete column hold a button
            setColumnToHoldButton(table, EDIT_COLUMN,
                    new JButton(Bundle.getMessage("ButtonEdit")));
            setColumnToHoldButton(table, DEL_COLUMN,
                    new JButton(Bundle.getMessage("ButtonDelete")));
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
                    log.debug("SML Edit existing logic started");
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
            jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).removeSignalMastLogic(sml, _signalMastList.get(r));
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
                log.debug("row is greater than turnout list size");
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
                    return Bundle.getMessage("ButtonEdit");
                case DEL_COLUMN:
                    return Bundle.getMessage("ButtonDelete");
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

    private final static Logger log = LoggerFactory.getLogger(SignallingSourcePanel.class.getName());

}
