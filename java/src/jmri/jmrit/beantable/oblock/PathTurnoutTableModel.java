package jmri.jmrit.beantable.oblock;

/**
 * GUI to define OBlocks 
 *<P> 
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author	Pete Cressman (C) 2010
 * @version     $Revision$
 */

import org.apache.log4j.Logger;
import java.util.ResourceBundle;

import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import javax.swing.table.AbstractTableModel;

import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.Turnout;

import jmri.jmrit.logix.OPath;

public class PathTurnoutTableModel extends AbstractTableModel {

    public static final int TURNOUT_NAME_COL = 0;
    public static final int SETTINGCOLUMN = 1;
    public static final int DELETE_COL = 2;
    public static final int NUMCOLS = 3;

    static final String unknown = jmri.jmrit.beantable.AbstractTableAction.rbean.getString("BeanStateUnknown");
    static final String inconsistent = jmri.jmrit.beantable.AbstractTableAction.rbean.getString("BeanStateInconsistent");
    static final String closed = InstanceManager.turnoutManagerInstance().getClosedText();
    static final String thrown = InstanceManager.turnoutManagerInstance().getThrownText();
    
    static final String[] turnoutStates = {closed, thrown, unknown, inconsistent};
    
	public static final ResourceBundle rbo = ResourceBundle.getBundle("jmri.jmrit.beantable.OBlockTableBundle");
    
    private String[] tempRow= new String[NUMCOLS];
    private OPath _path;

    public PathTurnoutTableModel() {
        super();
    }

    public PathTurnoutTableModel(OPath path) {
        super();
        _path = path;
    }

    public void init() {
        initTempRow();
    }

    void initTempRow() {
        for (int i=0; i<NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[DELETE_COL] = rbo.getString("ButtonClear");
    }
    public int getColumnCount () {
        return NUMCOLS;
    }

    public int getRowCount() {
        return _path.getSettings().size() + 1;
    }

    public String getColumnName(int col) {
        switch (col) {
            case TURNOUT_NAME_COL: return rbo.getString("LabelItemName");
            case SETTINGCOLUMN: return rbo.getString("ColumnSetting");
        }
        return "";
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (_path.getSettings().size() == rowIndex) {
            return tempRow[columnIndex];
        }
        // some error checking
        if (rowIndex >= _path.getSettings().size()){
            log.debug("row greater than bean list size");
            return "Error bean list";
        }
        BeanSetting bs = _path.getSettings().get(rowIndex);
        // some error checking
        if (bs == null){
            log.debug("bean is null");
            return "Error no bean";
        }
        switch(columnIndex) {
            case TURNOUT_NAME_COL:
                return bs.getBeanName();
            case SETTINGCOLUMN:
                switch (bs.getSetting()) {
                    case Turnout.CLOSED:
                        return closed;
                    case Turnout.THROWN:
                        return thrown;
                    case Turnout.UNKNOWN:
                        return unknown;
                    case Turnout.INCONSISTENT:
                        return inconsistent;
                }
                return  unknown;
            case DELETE_COL:
                return rbo.getString("ButtonDelete");
        }
        return "";
    }

    public void setValueAt(Object value, int row, int col) {
        if (_path.getSettings().size() == row) {
            switch(col) {
                case TURNOUT_NAME_COL:
                    String name = (String)value;
                    Turnout t = InstanceManager.turnoutManagerInstance().getTurnout(name);
                    if (t != null) {
                        int s = Turnout.UNKNOWN;
                        if (tempRow[SETTINGCOLUMN] == null) {
                            s = Turnout.UNKNOWN;
                        } else if (tempRow[SETTINGCOLUMN].equals(closed)) {
                            s = Turnout.CLOSED;
                        } else if (tempRow[SETTINGCOLUMN].equals(thrown)) {
                            s = Turnout.THROWN; 
                        } else if (tempRow[SETTINGCOLUMN].equals(unknown)) {
                            s = Turnout.UNKNOWN;
                        } else if (tempRow[SETTINGCOLUMN].equals(inconsistent)) {
                            s = Turnout.INCONSISTENT; 
                        }
                        BeanSetting bs = new BeanSetting(t, name, s);
                        _path.addSetting(bs);
                        fireTableRowsUpdated(row,row);
                    } else {
                        JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                                rbo.getString("NoSuchTurnout"), name),
                                rbo.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    break;           
                case SETTINGCOLUMN:
                    tempRow[col] = (String)value;
                    break;
                case DELETE_COL:
                	initTempRow();
                	fireTableRowsUpdated(row,row);
                	break;
            }
            return;
        }

        BeanSetting bs = _path.getSettings().get(row);

        switch(col) {
            case TURNOUT_NAME_COL:
                Turnout t = InstanceManager.turnoutManagerInstance().getTurnout((String)value);
                if (t!=null) {
                     if (!t.equals(bs.getBean())) {
                         _path.removeSetting(bs);
                         _path.addSetting(new BeanSetting(t, (String)value, bs.getSetting()));
                     }
                } else {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rbo.getString("NoSuchTurnout"), (String)value),
                            rbo.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                fireTableDataChanged();
                break;
            case SETTINGCOLUMN:
                String setting = (String)value;
                if (setting.equals(closed)) {
                    //bs.setSetting(Turnout.CLOSED);  - This was the form before BeanSetting was returned to Immutable
                    _path.getSettings().set(row, new BeanSetting(bs.getBean(), bs.getBeanName(), Turnout.CLOSED));
                } else if (setting.equals(thrown)) {
                    //bs.setSetting(Turnout.THROWN); 
                    _path.getSettings().set(row, new BeanSetting(bs.getBean(), bs.getBeanName(), Turnout.THROWN));
                } else if (setting.equals(unknown)) {
                    //bs.setSetting(Turnout.UNKNOWN);
                    _path.getSettings().set(row, new BeanSetting(bs.getBean(), bs.getBeanName(), Turnout.UNKNOWN));
                } else if (setting.equals(inconsistent)) {
                    //bs.setSetting(Turnout.INCONSISTENT);
                    _path.getSettings().set(row, new BeanSetting(bs.getBean(), bs.getBeanName(), Turnout.INCONSISTENT));
                }
                fireTableRowsUpdated(row,row);
                break;
            case DELETE_COL:
                if (JOptionPane.showConfirmDialog(null, rbo.getString("DeleteTurnoutConfirm"),
                                                  rbo.getString("WarningTitle"),
                                                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                                    ==  JOptionPane.YES_OPTION) {
                    _path.removeSetting(bs);
                    fireTableDataChanged();
                }
        }
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public Class<?> getColumnClass(int col) {
        if (col==DELETE_COL) {
            return JButton.class;
        } else if (col==SETTINGCOLUMN) {
            return JComboBox.class;
        }
        return String.class;
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case TURNOUT_NAME_COL: return new JTextField(20).getPreferredSize().width;
            case SETTINGCOLUMN: return new JTextField(10).getPreferredSize().width;
            case DELETE_COL: return new JButton("DELETE").getPreferredSize().width;
        }
        return 5;
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (_path.getBlock().equals(e.getSource()) && e.getPropertyName().equals("pathCount")) {
            fireTableDataChanged();
        }
    }

    static Logger log = Logger.getLogger(PathTurnoutTableModel.class.getName());
}

