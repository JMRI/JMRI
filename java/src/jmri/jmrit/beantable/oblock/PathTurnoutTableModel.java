package jmri.jmrit.beantable.oblock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.Block;
import jmri.jmrit.logix.OPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI to define OBlocks
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
    public static final int SETTINGCOLUMN = 1;
    public static final int DELETE_COL = 2;
    public static final int NUMCOLS = 3;

    static final String closed = InstanceManager.turnoutManagerInstance().getClosedText();
    static final String thrown = InstanceManager.turnoutManagerInstance().getThrownText();

    static final String[] turnoutStates = {closed, thrown};//, unknown, inconsistent};

    private String[] tempRow = new String[NUMCOLS];
    TableFrames.PathTurnoutFrame _parent;
    private OPath _path;

    public PathTurnoutTableModel() {
        super();
    }

    public PathTurnoutTableModel(OPath path, TableFrames.PathTurnoutFrame parent) {
        super();
        _path = path;
        _path.getBlock().addPropertyChangeListener(this);
        _parent = parent;
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
            case SETTINGCOLUMN:
                return Bundle.getMessage("ColumnSetting");
            default:
                // fall through
                break;
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (_path.getSettings().size() == rowIndex) {
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
            case SETTINGCOLUMN:
                switch (bs.getSetting()) {
                    case Turnout.CLOSED:
                        return closed;
                    case Turnout.THROWN:
                        return thrown;
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
                    if (tempRow[SETTINGCOLUMN] == null) {
                        return;
                    }
                    break;
                case SETTINGCOLUMN:
                    tempRow[SETTINGCOLUMN] = (String) value;
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
                if (tempRow[SETTINGCOLUMN].equals(closed)) {
                    s = Turnout.CLOSED;
                } else if (tempRow[SETTINGCOLUMN].equals(thrown)) {
                    s = Turnout.THROWN;
                } else {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("TurnoutMustBeSet", closed, thrown),
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
            case SETTINGCOLUMN:
                String setting = (String) value;
                if (setting.equals(closed)) {
                    //bs.setSetting(Turnout.CLOSED);  - This was the form before BeanSetting was returned to Immutable
                    _path.getSettings().set(row, new BeanSetting(bs.getBean(), bs.getBeanName(), Turnout.CLOSED));
                } else if (setting.equals(thrown)) {
                    //bs.setSetting(Turnout.THROWN); 
                    _path.getSettings().set(row, new BeanSetting(bs.getBean(), bs.getBeanName(), Turnout.THROWN));
                } else {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("TurnoutMustBeSet", closed, thrown),
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
        } else if (col == SETTINGCOLUMN) {
            return JComboBox.class;
        }
        return String.class;
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case TURNOUT_NAME_COL:
                return new JTextField(20).getPreferredSize().width;
            case SETTINGCOLUMN:
                return new JTextField(10).getPreferredSize().width;
            case DELETE_COL:
                return new JButton("DELETE").getPreferredSize().width;
            default:
                // fall through
                break;
        }
        return 5;
    }

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
