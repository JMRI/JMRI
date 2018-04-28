package jmri.jmrit.beantable.signalmast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
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
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import jmri.NamedBean;
import jmri.SignalMast;
import jmri.implementation.SignalMastRepeater;
import jmri.managers.DefaultSignalMastManager;
import jmri.swing.RowSorterUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriBeanComboBox;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for Signal Mast Add / Edit Panel
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SignalMastRepeaterPanel extends jmri.util.swing.JmriPanel implements PropertyChangeListener {

    DefaultSignalMastManager dsmm;

    SignalMastRepeaterModel _RepeaterModel;
    JScrollPane _SignalAppearanceScrollPane;
    JmriBeanComboBox _MasterBox;
    JmriBeanComboBox _SlaveBox;
    JButton _addRepeater;

    public SignalMastRepeaterPanel() {
        super();
        dsmm = (DefaultSignalMastManager) jmri.InstanceManager.getDefault(jmri.SignalMastManager.class);
        dsmm.addPropertyChangeListener(this);

        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel sourcePanel = new JPanel();

        header.add(sourcePanel);
        add(header, BorderLayout.NORTH);

        _RepeaterModel = new SignalMastRepeaterModel();
        JTable _RepeaterTable = new JTable(_RepeaterModel);

        TableRowSorter<SignalMastRepeaterModel> sorter = new TableRowSorter<>(_RepeaterModel); // leave default sorting
        RowSorterUtil.setSortOrder(sorter, SignalMastRepeaterModel.DIR_COLUMN, SortOrder.ASCENDING);
        _RepeaterTable.setRowSorter(sorter);

        _RepeaterTable.setRowSelectionAllowed(false);
        _RepeaterTable.setPreferredScrollableViewportSize(new java.awt.Dimension(526, 120));
        _RepeaterModel.configureTable(_RepeaterTable);
        _SignalAppearanceScrollPane = new JScrollPane(_RepeaterTable);
        _RepeaterModel.fireTableDataChanged();
        add(_SignalAppearanceScrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        updateDetails();

        _MasterBox = new JmriBeanComboBox(dsmm);
        _MasterBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSlaveBoxLists();
            }
        });

        _SlaveBox = new JmriBeanComboBox(dsmm);
        _SlaveBox.setEnabled(false);
        footer.add(new JLabel(Bundle.getMessage("Master") + " : "));
        footer.add(_MasterBox);
        footer.add(new JLabel(Bundle.getMessage("Slave") + " : "));
        footer.add(_SlaveBox);
        _addRepeater = new JButton(Bundle.getMessage("ButtonAdd"));
        _addRepeater.setEnabled(false);
        _addRepeater.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SignalMastRepeater rp = new SignalMastRepeater((SignalMast) _MasterBox.getSelectedBean(), (SignalMast) _SlaveBox.getSelectedBean());
                try {
                    dsmm.addRepeater(rp);
                } catch (jmri.JmriException ex) {
                    log.error(ex.toString());
                    /**/
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(Bundle.getMessage("MessageAddFailed"),
                            new Object[]{_MasterBox.getSelectedDisplayName(), _SlaveBox.getSelectedDisplayName()}),
                            Bundle.getMessage("TitleAddFailed"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        footer.add(_addRepeater);

        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(Bundle.getMessage("AddRepeater"));
        footer.setBorder(border);

        add(footer, BorderLayout.SOUTH);
    }

    void setSlaveBoxLists() {
        SignalMast masterMast = (SignalMast) _MasterBox.getSelectedBean();
        if (masterMast == null) {
            _SlaveBox.setEnabled(false);
            _addRepeater.setEnabled(false);
            return;
        }
        java.util.Iterator<String> iter
                = dsmm.getSystemNameList().iterator();

        // don't return an element if there are not sensors to include
        if (!iter.hasNext()) {
            return;
        }
        ArrayList<NamedBean> excludeList = new ArrayList<>();
        while (iter.hasNext()) {
            String mname = iter.next();
            if (mname != null) {
                SignalMast s = dsmm.getBySystemName(mname);
                if (s.getAppearanceMap() != masterMast.getAppearanceMap()) {
                    excludeList.add(s);
                } else if (s == masterMast) {
                    excludeList.add(s);
                }
            }
        }
        _SlaveBox.excludeItems(excludeList);
        if (excludeList.size() == dsmm.getSystemNameList().size()) {
            _SlaveBox.setEnabled(false);
            _addRepeater.setEnabled(false);
        } else {
            _SlaveBox.setEnabled(true);
            _addRepeater.setEnabled(true);
        }
    }

    JmriJFrame signalMastLogicFrame = null;
    JLabel sourceLabel = new JLabel();

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {

    }

    private ArrayList<SignalMastRepeater> _signalMastRepeaterList;

    private void updateDetails() {
        _signalMastRepeaterList = new ArrayList<SignalMastRepeater>(dsmm.getRepeaterList());
        _RepeaterModel.fireTableDataChanged();//updateSignalMastLogic(old, sml);
    }

    public class SignalMastRepeaterModel extends AbstractTableModel implements PropertyChangeListener {

        SignalMastRepeaterModel() {
            super();
            dsmm.addPropertyChangeListener(this);
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == DIR_COLUMN) {
                return JButton.class;
            }
            if (c == ENABLE_COLUMN) {
                return Boolean.class;
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
                    log.warn("Unexpected column in getPreferredWidth: " + col);
                    return new JTextField(8).getPreferredSize().width;
            }
        }

        @Override
        public String getColumnName(int col) {
            if (col == MASTER_COLUMN) {
                return Bundle.getMessage("ColumnMaster");
            }
            if (col == DIR_COLUMN) {
                return Bundle.getMessage("ColumnDir");
            }
            if (col == SLAVE_COLUMN) {
                return Bundle.getMessage("ColumnSlave");
            }
            if (col == ENABLE_COLUMN) {
                return Bundle.getMessage("ColumnHeadEnabled");
            }
            if (col == DEL_COLUMN) {
                return "";
            }
            return "";
        }

        public void dispose() {

        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("repeaterlength")) {
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
            if (c == DEL_COLUMN) {
                return true;
            }
            if (c == ENABLE_COLUMN) {
                return true;
            }
            if (c == DIR_COLUMN) {
                return true;
            }
            return (false);
        }

        protected void deleteRepeater(int r) {
            dsmm.removeRepeater(_signalMastRepeaterList.get(r));
        }

        public static final int MASTER_COLUMN = 0;
        public static final int DIR_COLUMN = 1;
        public static final int SLAVE_COLUMN = 2;
        public static final int ENABLE_COLUMN = 3;
        public static final int DEL_COLUMN = 4;

        public void setSetToState(String x) {
        }

        @Override
        public int getRowCount() {
            if (_signalMastRepeaterList == null) {
                return 0;
            }
            return _signalMastRepeaterList.size();
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
                    switch (_signalMastRepeaterList.get(r).getDirection()) {
                        case SignalMastRepeater.BOTHWAY:
                            return "< >";
                        case SignalMastRepeater.MASTERTOSLAVE:
                            return " > ";
                        case SignalMastRepeater.SLAVETOMASTER:
                            return " < ";
                        default:
                            return "< >";
                    }
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

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                                justification="better to keep cases in column order rather than to combine")
        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c == DIR_COLUMN) {
                switch (_signalMastRepeaterList.get(r).getDirection()) {
                    case SignalMastRepeater.BOTHWAY:
                        _signalMastRepeaterList.get(r).setDirection(SignalMastRepeater.MASTERTOSLAVE);
                        break;
                    case SignalMastRepeater.MASTERTOSLAVE:
                        _signalMastRepeaterList.get(r).setDirection(SignalMastRepeater.SLAVETOMASTER);
                        break;
                    case SignalMastRepeater.SLAVETOMASTER:
                        _signalMastRepeaterList.get(r).setDirection(SignalMastRepeater.BOTHWAY);
                        break;
                    default:
                        _signalMastRepeaterList.get(r).setDirection(SignalMastRepeater.BOTHWAY);
                        break;
                }
                _RepeaterModel.fireTableDataChanged();
            } else if (c == DEL_COLUMN) {
                deleteRepeater(r);
            } else if (c == ENABLE_COLUMN) {
                boolean b = ((Boolean) type).booleanValue();
                _signalMastRepeaterList.get(r).setEnabled(b);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignalMastRepeaterPanel.class);

}
