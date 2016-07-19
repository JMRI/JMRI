package jmri.jmrit.signalling;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import jmri.SignalMast;
import jmri.SignalMastLogic;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JmriJFrame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for Signal Mast Add / Edit Panel
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 */
public class SignallingSourcePanel extends jmri.util.swing.JmriPanel implements PropertyChangeListener {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");

    SignalMastLogic sml;
    SignalMast sourceMast;
    JLabel fixedSourceMastLabel = new JLabel();

    JButton discoverPairs = new JButton(rb.getString("ButtonDiscover"));

    SignalMastAppearanceModel _AppearanceModel;
    JScrollPane _SignalAppearanceScrollPane;

    public SignallingSourcePanel(final SignalMast sourceMast) {
        super();
        sml = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sourceMast);
        this.sourceMast = sourceMast;
        fixedSourceMastLabel = new JLabel(sourceMast.getDisplayName());
        if (sml != null) {
            _signalMastList = sml.getDestinationList();
        }

        jmri.InstanceManager.getDefault(LayoutBlockManager.class).addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel sourcePanel = new JPanel();
        sourcePanel.add(fixedSourceMastLabel);
        header.add(sourcePanel);
        add(header, BorderLayout.NORTH);

        _AppearanceModel = new SignalMastAppearanceModel();
        JTable SignalAppearanceTable = jmri.util.JTableUtil.sortableDataModel(_AppearanceModel);

        try {
            jmri.util.com.sun.TableSorter tmodel = ((jmri.util.com.sun.TableSorter) SignalAppearanceTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(SignalMastAppearanceModel.SYSNAME_COLUMN, jmri.util.com.sun.TableSorter.ASCENDING);
        } catch (ClassCastException e3) {
        }  // if not a sortable table model

        SignalAppearanceTable.setRowSelectionAllowed(false);
        SignalAppearanceTable.setPreferredScrollableViewportSize(new java.awt.Dimension(600, 120));
        _AppearanceModel.configureTable(SignalAppearanceTable);
        _SignalAppearanceScrollPane = new JScrollPane(SignalAppearanceTable);
        _AppearanceModel.fireTableDataChanged();
        add(_SignalAppearanceScrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel();

        footer.add(discoverPairs);
        discoverPairs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                discoverPressed(e);
            }
        });

        JButton addLogic = new JButton(rb.getString("AddLogic"));
        footer.add(addLogic);
        addLogic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                class WindowMaker implements Runnable {

                    WindowMaker() {
                    }

                    public void run() {
                        SignallingAction sigLog = new SignallingAction();
                        sigLog.setMast(sourceMast, null);
                        sigLog.actionPerformed(null);
                    }
                }
                WindowMaker t = new WindowMaker();
                javax.swing.SwingUtilities.invokeLater(t);
            }
        });

        /*if(!jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled())
         discoverPairs.setEnabled(false);*/
        add(footer, BorderLayout.SOUTH);
    }

    JmriJFrame signalMastLogicFrame = null;
    JLabel sourceLabel = new JLabel();

    void discoverPressed(ActionEvent e) {
        if (!jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
            int response = JOptionPane.showConfirmDialog(null, rb.getString("EnableLayoutBlockRouting"));
            if (response == 0) {
                jmri.InstanceManager.getDefault(LayoutBlockManager.class).enableAdvancedRouting(true);
                JOptionPane.showMessageDialog(null, rb.getString("LayoutBlockRoutingEnabledShort"));
            }
        }

        signalMastLogicFrame = new JmriJFrame("Discover Signal Mast Pairs", false, false);
        signalMastLogicFrame.setPreferredSize(null);
        JPanel panel1 = new JPanel();
        sourceLabel = new JLabel("Discovering Signalmasts");
        panel1.add(sourceLabel);
        signalMastLogicFrame.add(sourceLabel);
        signalMastLogicFrame.pack();
        signalMastLogicFrame.setVisible(true);

        ArrayList<LayoutEditor> layout = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
        jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).addPropertyChangeListener(this);
        for (int i = 0; i < layout.size(); i++) {
            try {
                jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).discoverSignallingDest(sourceMast, layout.get(i));
            } catch (jmri.JmriException ex) {
                signalMastLogicFrame.setVisible(false);
                JOptionPane.showMessageDialog(null, ex.toString());
            }
        }
        jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).removePropertyChangeListener(this);
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("autoSignalMastGenerateComplete")) {
            signalMastLogicFrame.setVisible(false);
            signalMastLogicFrame.dispose();

            if (sml == null) {
                updateDetails();
            }
            JOptionPane.showMessageDialog(null, "Generation of Signalling Pairs Completed");
        }
        if (e.getPropertyName().equals("advancedRoutingEnabled")) {
            boolean newValue = (Boolean) e.getNewValue();
            discoverPairs.setEnabled(newValue);
        }
    }

    private ArrayList<SignalMast> _signalMastList;

    private void updateDetails() {
        SignalMastLogic old = sml;
        sml = jmri.InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sourceMast);
        if (sml != null) {
            _signalMastList = sml.getDestinationList();
            _AppearanceModel.updateSignalMastLogic(old, sml);
        }
    }

    public class SignalMastAppearanceModel extends AbstractTableModel implements PropertyChangeListener {

        /**
         *
         */
        private static final long serialVersionUID = 1161557965839973328L;

        SignalMastAppearanceModel() {
            super();
            if (sml != null) {
                sml.addPropertyChangeListener(this);
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
                return Bundle.getMessage("ColumnSystemName");
            }
            if (col == ACTIVE_COLUMN) {
                return rb.getString("ColumnActive");
            }
            if (col == ENABLE_COLUMN) {
                return rb.getString("ColumnEnabled");
            }
            if (col == EDIT_COLUMN) {
                return ""; //no title above Edit buttons
            }
            if (col == DEL_COLUMN) {
                return ""; //no title above Delete buttons
            }
            return "";
        }

        public void dispose() {
            if (sml != null) {
                sml.removePropertyChangeListener(this);
            }
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
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
                fireTableRowsUpdated(0, _signalMastList.size());
            }
        }

        protected void configEditColumn(JTable table) {
            // have the delete column hold a button
            /*AbstractTableAction.rb.getString("EditDelete")*/
            setColumnToHoldButton(table, EDIT_COLUMN,
                    new JButton(Bundle.getMessage("ButtonEdit")));
            setColumnToHoldButton(table, DEL_COLUMN,
                    new JButton(Bundle.getMessage("ButtonDelete")));
        }

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

        public int getColumnCount() {
            return 6;
        }

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

        protected void editPair(int r) {

            class WindowMaker implements Runnable {

                int row;

                WindowMaker(int r) {
                    row = r;
                }

                public void run() {
                    SignallingAction sigLog = new SignallingAction();
                    sigLog.setMast(sourceMast, _signalMastList.get(row));
                    sigLog.actionPerformed(null);
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

        public int getRowCount() {
            if (_signalMastList == null) {
                return 0;
            }
            return _signalMastList.size();
        }

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
