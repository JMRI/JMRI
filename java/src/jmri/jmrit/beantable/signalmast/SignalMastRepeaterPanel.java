package jmri.jmrit.beantable.signalmast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SortOrder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.implementation.SignalMastRepeater;
import jmri.managers.DefaultSignalMastManager;
import jmri.swing.NamedBeanComboBox;
import jmri.swing.RowSorterUtil;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Frame for Signal Mast Add / Edit Panel
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SignalMastRepeaterPanel extends JmriPanel {

    final DefaultSignalMastManager dsmm;
    private ArrayList<SignalMastRepeater> _signalMastRepeaterList;
    private SignalMastRepeaterModel _RepeaterModel;
    private NamedBeanComboBox<SignalMast> _MasterBox;
    private NamedBeanComboBox<SignalMast> _SlaveBox;
    private JButton _addRepeater;

    public SignalMastRepeaterPanel() {
        super();
        dsmm = (DefaultSignalMastManager) InstanceManager.getDefault(SignalMastManager.class);
        init();
    }

    final void init() {

        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel sourcePanel = new JPanel();

        header.add(sourcePanel);
        add(header, BorderLayout.NORTH);

        _RepeaterModel = new SignalMastRepeaterModel();
        JTable repeaterTable = new JTable(_RepeaterModel);

        TableRowSorter<SignalMastRepeaterModel> sorter = new TableRowSorter<>(_RepeaterModel); // leave default sorting
        RowSorterUtil.setSortOrder(sorter, SignalMastRepeaterModel.DIR_COLUMN, SortOrder.ASCENDING);
        repeaterTable.setRowSorter(sorter);

        repeaterTable.setRowSelectionAllowed(false);
        repeaterTable.setPreferredScrollableViewportSize(new java.awt.Dimension(526, 120));
        _RepeaterModel.configureTable(repeaterTable);
        
        JScrollPane signalAppearanceScrollPane = new JScrollPane(repeaterTable);
        _RepeaterModel.fireTableDataChanged();
        add(signalAppearanceScrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        updateDetails();

        _MasterBox = new NamedBeanComboBox<>(dsmm);
        _MasterBox.addActionListener( e -> setSlaveBoxLists());
        JComboBoxUtil.setupComboBoxMaxRows(_MasterBox);

        _SlaveBox = new NamedBeanComboBox<>(dsmm);
        JComboBoxUtil.setupComboBoxMaxRows(_SlaveBox);
        _SlaveBox.setEnabled(false);
        footer.add(new JLabel(Bundle.getMessage("Master") + " : "));
        footer.add(_MasterBox);
        footer.add(new JLabel(Bundle.getMessage("Slave") + " : "));
        footer.add(_SlaveBox);
        _addRepeater = new JButton(Bundle.getMessage("ButtonAddText"));
        _addRepeater.setEnabled(false);
        _addRepeater.addActionListener((ActionEvent e) -> {
            SignalMastRepeater rp = new SignalMastRepeater(_MasterBox.getSelectedItem(), _SlaveBox.getSelectedItem());
            try {
                dsmm.addRepeater(rp);
            } catch (JmriException ex) {
                String error = java.text.MessageFormat.format(Bundle.getMessage("MessageAddFailed"),
                    new Object[]{_MasterBox.getSelectedItemDisplayName(), _SlaveBox.getSelectedItemDisplayName()});
                log.error("Failed to add Repeater. {} {}", error, ex.getMessage());
                JmriJOptionPane.showMessageDialog(this, error,
                    Bundle.getMessage("TitleAddFailed"), JmriJOptionPane.ERROR_MESSAGE);
            }
        });
        footer.add(_addRepeater);

        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(Bundle.getMessage("AddRepeater"));
        footer.setBorder(border);

        add(footer, BorderLayout.SOUTH);
    }

    void setSlaveBoxLists() {
        SignalMast masterMast = _MasterBox.getSelectedItem();
        if (masterMast == null) {
            _SlaveBox.setEnabled(false);
            _addRepeater.setEnabled(false);
            return;
        }
        java.util.Iterator<SignalMast> iter
                = dsmm.getNamedBeanSet().iterator();

        // don't return an element if there are not sensors to include
        if (!iter.hasNext()) {
            return;
        }
        Set<SignalMast> excludedSignalMasts = new HashSet<>();
        while (iter.hasNext()) {
            SignalMast s = iter.next();
            if ( ( s.getAppearanceMap() != masterMast.getAppearanceMap() )
                    || ( s == masterMast ) ) {
                excludedSignalMasts.add(s);
            }
        }
        _SlaveBox.setExcludedItems(excludedSignalMasts);
        if (excludedSignalMasts.size() == dsmm.getNamedBeanSet().size()) {
            _SlaveBox.setEnabled(false);
            _addRepeater.setEnabled(false);
        } else {
            _SlaveBox.setEnabled(true);
            _addRepeater.setEnabled(true);
        }
    }

    private void updateDetails() {
        _signalMastRepeaterList = new ArrayList<>(dsmm.getRepeaterList());
        _RepeaterModel.fireTableDataChanged();//updateSignalMastLogic(old, sml);
    }

    @Override
    public void dispose() {
        _RepeaterModel.dispose();
        super.dispose();
    }

    private class SignalMastRepeaterModel extends AbstractTableModel implements PropertyChangeListener {

        SignalMastRepeaterModel() {
            super();
            init();
        }

        final void init(){
            dsmm.addPropertyChangeListener(SignalMastRepeaterModel.this);
        }

        @Override
        public Class<?> getColumnClass(int c) {
            switch (c) {
                case DIR_COLUMN:
                case DEL_COLUMN:
                    return JButton.class;
                case ENABLE_COLUMN:
                    return Boolean.class;
                default:
                    return String.class;
            }
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
                case ENABLE_COLUMN:
                case DIR_COLUMN:
                    return new JTextField(5).getPreferredSize().width;
                case SLAVE_COLUMN:
                    return new JTextField(15).getPreferredSize().width;
                case MASTER_COLUMN:
                    return new JTextField(15).getPreferredSize().width;
                case DEL_COLUMN: // not actually used due to the configureTable, setColumnToHoldButton, configureButton
                    return new JTextField(22).getPreferredSize().width;
                default:
                    log.warn("Unexpected column in getPreferredWidth: {}", col);
                    return new JTextField(8).getPreferredSize().width;
            }
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case MASTER_COLUMN:
                    return Bundle.getMessage("ColumnMaster");
                case DIR_COLUMN:
                    return Bundle.getMessage("ColumnDir");
                case SLAVE_COLUMN:
                    return Bundle.getMessage("ColumnSlave");
                case ENABLE_COLUMN:
                    return Bundle.getMessage("ColumnHeadEnabled");
                case DEL_COLUMN:
                default:
                    return "";
            }
        }

        private void dispose() {
            dsmm.removePropertyChangeListener(SignalMastRepeaterModel.this);
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if ( SignalMastManager.PROPERTY_REPEATER_LENGTH.equals(e.getPropertyName())) {
                updateDetails();
            }
        }

        protected void configEditColumn(JTable table) {
            // have the delete column hold a button
            /*AbstractTableAction.Bundle.getMessage("EditDelete")*/

            JButton b = new JButton("< >");
            b.putClientProperty("JComponent.sizeVariant", "small");
            b.putClientProperty("JButton.buttonType", "square");

            setColumnToHoldButton(table, DIR_COLUMN,
                    b);
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

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            switch (c) {
                case DEL_COLUMN:
                case ENABLE_COLUMN:
                case DIR_COLUMN:
                    return true;
                default:
                    return false;
            }
        }

        protected void deleteRepeater(int r) {
            dsmm.removeRepeater(_signalMastRepeaterList.get(r));
        }

        public static final int MASTER_COLUMN = 0;
        public static final int DIR_COLUMN = 1;
        public static final int SLAVE_COLUMN = 2;
        public static final int ENABLE_COLUMN = 3;
        public static final int DEL_COLUMN = 4;

        @Override
        public int getRowCount() {
            return ( _signalMastRepeaterList == null ? 0 : _signalMastRepeaterList.size() );
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (r >= _signalMastRepeaterList.size()) {
                log.debug("row is greater than turnout list size");
                return null;
            }
            switch (c) {
                case MASTER_COLUMN:
                    return _signalMastRepeaterList.get(r).getMasterMastName();
                case DIR_COLUMN:  // slot number
                    return getValueAtDirectionCol(r);
                case SLAVE_COLUMN:
                    return _signalMastRepeaterList.get(r).getSlaveMastName();
                case ENABLE_COLUMN:
                    return _signalMastRepeaterList.get(r).getEnabled();
                case DEL_COLUMN:
                    return Bundle.getMessage("ButtonDelete");
                default:
                    return null;
            }
        }

        private String getValueAtDirectionCol(int r) {
            switch (_signalMastRepeaterList.get(r).getDirection()) {
                case SignalMastRepeater.MASTERTOSLAVE:
                    return " > ";
                case SignalMastRepeater.SLAVETOMASTER:
                    return " < ";
                case SignalMastRepeater.BOTHWAY:
                default:
                    return "< >";
            }
        }

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                                justification="better to keep cases in column order rather than to combine")
        @Override
        public void setValueAt(Object type, int r, int c) {
            switch (c) {
                case DIR_COLUMN:
                    setValueAtDirectionCol(r);
                    _RepeaterModel.fireTableDataChanged();
                    break;
                case DEL_COLUMN:
                    deleteRepeater(r);
                    break;
                case ENABLE_COLUMN:
                    boolean b = ((Boolean) type);
                    _signalMastRepeaterList.get(r).setEnabled(b);
                    break;
                default:
                    break;
            }
        }

        private void setValueAtDirectionCol(int r) {
            switch (_signalMastRepeaterList.get(r).getDirection()) {
                case SignalMastRepeater.BOTHWAY:
                    _signalMastRepeaterList.get(r).setDirection(SignalMastRepeater.MASTERTOSLAVE);
                    break;
                case SignalMastRepeater.MASTERTOSLAVE:
                    _signalMastRepeaterList.get(r).setDirection(SignalMastRepeater.SLAVETOMASTER);
                    break;
                case SignalMastRepeater.SLAVETOMASTER:
                default:
                    _signalMastRepeaterList.get(r).setDirection(SignalMastRepeater.BOTHWAY);
                    break;
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalMastRepeaterPanel.class);

}
