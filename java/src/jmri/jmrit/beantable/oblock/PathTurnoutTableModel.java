package jmri.jmrit.beantable.oblock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.Block;
import jmri.jmrit.beantable.RowComboBoxPanel;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.signalling.SignallingPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define Path-Turnout combos for OBlocks.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Pete Cressman (C) 2010
 */
public class PathTurnoutTableModel extends AbstractTableModel implements PropertyChangeListener {

    public static final int TURNOUT_NAME_COL = 0;
    public static final int STATE_COL = 1;
    public static final int DELETE_COL = 2;
    public static final int NUMCOLS = 3;

    //static final String closed = InstanceManager.turnoutManagerInstance().getClosedText();
    //static final String thrown = InstanceManager.turnoutManagerInstance().getThrownText();
    //static final String[] turnoutStates = {closed, thrown};//, unknown, inconsistent};
    private static final String SET_CLOSED = jmri.InstanceManager.turnoutManagerInstance().getClosedText();
    private static final String SET_THROWN = jmri.InstanceManager.turnoutManagerInstance().getThrownText();

    private final String[] tempRow = new String[NUMCOLS];
    TableFrames.PathTurnoutFrame _parent;
    private OPath _path;

    public PathTurnoutTableModel() {
        super();
    }

    public PathTurnoutTableModel(OPath path, TableFrames.PathTurnoutFrame parent) {
        super();
        _path = path;
        _path.getBlock().addPropertyChangeListener(this);
        _parent = parent; // is used to change the title, or dispose when item is deleted
    }

    public void removeListener() {
        Block block = _path.getBlock();
        if (block == null) {
            return;
        }
        try {
            _path.getBlock().removePropertyChangeListener(this);
        } catch (NullPointerException npe) { // OK when block is removed
        }
    }


    void initTempRow() {
        for (int i = 0; i < NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[DELETE_COL] = Bundle.getMessage("ButtonClear");
    }

    @Override
    public int getColumnCount() {
        return NUMCOLS;
    }

    @Override
    public int getRowCount() {
        return _path.getSettings().size() + 1;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case TURNOUT_NAME_COL:
                return Bundle.getMessage("LabelItemName");
            case STATE_COL:
                return Bundle.getMessage("ColumnState"); // state
            default:
                // fall through
                break;
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (_path.getSettings().size() == rowIndex) { // this must be tempRow
            return tempRow[columnIndex];
        }
        // some error checking
        if (rowIndex >= _path.getSettings().size()) {
            log.debug("row greater than bean list size");
            return "Error bean list";
        }
        BeanSetting bs = _path.getSettings().get(rowIndex);
        // some error checking
        if (bs == null) {
            log.debug("bean is null");
            return "Error no bean";
        }
        switch (columnIndex) {
            case TURNOUT_NAME_COL:
                return bs.getBeanName();
            case STATE_COL:
                switch (bs.getSetting()) {
                    case Turnout.CLOSED:
                        return SET_CLOSED;
                    case Turnout.THROWN:
                        return SET_THROWN;
                    default:
                        return "";

                }
            case DELETE_COL:
                return Bundle.getMessage("ButtonDelete");
            default:
                // fall through
                break;
        }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (_path.getSettings().size() == row) {
            switch (col) {
                case TURNOUT_NAME_COL:
                    tempRow[TURNOUT_NAME_COL] = (String) value;
                    if (tempRow[STATE_COL] == null) {
                        return;
                    }
                    break;
                case STATE_COL:
                    tempRow[STATE_COL] = (String) value;
                    if (tempRow[TURNOUT_NAME_COL] == null) {
                        return;
                    }
                    break;
                case DELETE_COL:
                    initTempRow();
                    fireTableRowsUpdated(row, row);
                    return;
                default:
                    // fall through
                    break;
            }
            Turnout t = InstanceManager.turnoutManagerInstance().getTurnout(tempRow[TURNOUT_NAME_COL]);
            if (t != null) {
                int s = Turnout.UNKNOWN;
                if (tempRow[STATE_COL].equals(SET_CLOSED)) {
                    s = Turnout.CLOSED;
                } else if (tempRow[STATE_COL].equals(SET_THROWN)) {
                    s = Turnout.THROWN;
                } else {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("TurnoutMustBeSet", SET_CLOSED, SET_THROWN),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                BeanSetting bs = new BeanSetting(t, tempRow[TURNOUT_NAME_COL], s);
                _path.addSetting(bs);
                fireTableRowsUpdated(row, row);
            } else {
                JOptionPane.showMessageDialog(null, Bundle.getMessage("NoSuchTurnout", tempRow[TURNOUT_NAME_COL]),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            initTempRow();
            return;
        }

        BeanSetting bs = _path.getSettings().get(row);

        switch (col) {
            case TURNOUT_NAME_COL:
                Turnout t = InstanceManager.turnoutManagerInstance().getTurnout((String) value);
                if (t != null) {
                    if (!t.equals(bs.getBean())) {
                        _path.removeSetting(bs);
                        _path.addSetting(new BeanSetting(t, (String) value, bs.getSetting()));
                    }
                } else {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("NoSuchTurnout", (String) value),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                fireTableDataChanged();
                break;
            case STATE_COL:
                String setting = (String) value;
                if (setting.equals(SET_CLOSED)) {
                    //bs.setSetting(Turnout.CLOSED);  - This was the form before BeanSetting was returned to Immutable
                    _path.getSettings().set(row, new BeanSetting(bs.getBean(), bs.getBeanName(), Turnout.CLOSED));
                } else if (setting.equals(SET_THROWN)) {
                    //bs.setSetting(Turnout.THROWN); 
                    _path.getSettings().set(row, new BeanSetting(bs.getBean(), bs.getBeanName(), Turnout.THROWN));
                } else {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("TurnoutMustBeSet", SET_CLOSED, SET_THROWN),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                fireTableRowsUpdated(row, row);
                break;
            case DELETE_COL:
                if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("DeleteTurnoutConfirm"),
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                        == JOptionPane.YES_OPTION) {
                    _path.removeSetting(bs);
                    fireTableDataChanged();
                }
                break;
            default:
                log.warn("Unhandled col: {}", col);
                break;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == DELETE_COL) {
            return JButton.class;
        } else if (col == STATE_COL) {
            return StateComboBoxPanel.class;
        }
        return String.class;
    }

    /**
     * Provide a table cell renderer looking like a JComboBox as an
     * editor/renderer for the manual tables on all except the Masts tab.
     * <p>
     * This is a lightweight version of the
     * {@link jmri.jmrit.beantable.RowComboBoxPanel} RowComboBox cell editor
     * class, some of the hashtables not needed here since we only need
     * identical options for all rows in a column.
     *
     * see jmri.jmrit.signalling.SignallingPanel.SignalMastModel.AspectComboBoxPanel for a full application with
     * row specific comboBox choices.
     */
    public class StateComboBoxPanel extends RowComboBoxPanel {

        @Override
        protected final void eventEditorMousePressed() {
            this.editor.add(getEditorBox(table.convertRowIndexToModel(this.currentRow))); // add editorBox to JPanel
            this.editor.revalidate();
            SwingUtilities.invokeLater(this.comboBoxFocusRequester);
            log.debug("eventEditorMousePressed in row: {})", this.currentRow);  // NOI18N
        }

        /**
         * Call the method in the surrounding method for the
         * SignalHeadTable.
         *
         * @param row the user clicked on in the table
         * @return an appropriate combobox for this signal head
         */
        @Override
        protected JComboBox<String> getEditorBox(int row) {
            return getStateEditorBox(row);
        }

    }
    // end of methods to display STATE_COLUMN ComboBox

    /**
     * Provide a static JComboBox element to display inside the JPanel
     * CellEditor. When not yet present, create, store and return a new one.
     *
     * @param row Index number (in TableDataModel)
     * @return A combobox containing the valid aspect names for this mast
     */
    JComboBox<String> getStateEditorBox(int row) {
        // create dummy comboBox, override in extended classes for each bean
        JComboBox<String> editCombo = new JComboBox<>();
        editCombo.addItem(SET_THROWN);
        editCombo.addItem(SET_CLOSED);
        return editCombo;
    }

    /**
     * Customize the Turnout column to show an appropriate ComboBox of
     * available options.
     *
     * @param table a JTable of beans
     */
    protected void configTurnoutStateColumn(JTable table) {
        // have the state column hold a JPanel with a JComboBox for States
        table.setDefaultEditor(StateComboBoxPanel.class, new StateComboBoxPanel());
        table.setDefaultRenderer(StateComboBoxPanel.class, new StateComboBoxPanel()); // use same class as renderer
        // Set more things?
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case TURNOUT_NAME_COL:
                return new JTextField(20).getPreferredSize().width;
            case STATE_COL:
                return new JTextField(10).getPreferredSize().width;
            case DELETE_COL:
                return new JButton("DELETE").getPreferredSize().width;
            default:
                // fall through
                break;
        }
        return 5;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (_path.getBlock().equals(e.getSource())) {
            String property = e.getPropertyName();
            if (property.equals("pathCount")) {
                fireTableDataChanged();
                if (_path.equals(e.getOldValue())) {    // path was deleted
                    removeListener();
                    _parent.dispose();
                }
            } else if (property.equals("pathName")) {
                String title = Bundle.getMessage("TitlePathTurnoutTable", _path.getBlock().getDisplayName(), e.getOldValue());
                if (_parent.getTitle().equals(title)) {
                    title = Bundle.getMessage("TitlePathTurnoutTable", _path.getBlock().getDisplayName(), e.getNewValue());
                    _parent.setTitle(title);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PathTurnoutTableModel.class);

}
