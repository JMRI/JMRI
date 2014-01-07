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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.Block;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Reporter;
import jmri.Sensor;
import jmri.jmrit.beantable.AbstractTableAction;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

    /**
     * Duplicates the JTable model for BlockTableAction and adds a column
     * for the occupancy sensor.  Configured for use within an internal frame.
     */
public class OBlockTableModel extends jmri.jmrit.picker.PickListModel {

    static public final int SYSNAMECOL  = 0;
    static public final int USERNAMECOL = 1;
    static public final int COMMENTCOL = 2;
    static public final int SENSORCOL = 3;
    static public final int EDIT_COL = 4;			// Path button
    static public final int DELETE_COL = 5;
    static public final int LENGTHCOL = 6;
    static public final int UNITSCOL = 7;
    static public final int REPORTERCOL = 8;
    static public final int REPORT_CURRENTCOL = 9;
    static public final int PERMISSIONCOL = 10;
    static public final int SPEEDCOL = 11;
    static public final int ERR_SENSORCOL = 12;
    static public final int CURVECOL = 13;
    static public final int NUMCOLS = 14;

    static public final String noneText = AbstractTableAction.rb.getString("BlockNone");
    static public final String gradualText = AbstractTableAction.rb.getString("BlockGradual");
    static public final String tightText = AbstractTableAction.rb.getString("BlockTight");
    static public final String severeText = AbstractTableAction.rb.getString("BlockSevere");
    static final String[] curveOptions = {noneText, gradualText, tightText, severeText};
    

	static final ResourceBundle rbo = ResourceBundle.getBundle("jmri.jmrit.beantable.OBlockTableBundle");

    java.text.DecimalFormat twoDigit = new java.text.DecimalFormat("0.00");

    OBlockManager manager;
    private final String[] tempRow= new String[NUMCOLS];
    TableFrames _parent;

    public OBlockTableModel(TableFrames parent) {
        super();
        _parent = parent;
        initTempRow();
    }
    
    void addHeaderListener(JTable table) {
    	addMouseListenerToHeader(table);
    }

    void initTempRow() {
        for (int i=0; i<NUMCOLS; i++) {
            tempRow[i] = null;
        }
        tempRow[LENGTHCOL] = twoDigit.format(0.0);
        tempRow[UNITSCOL] = rbo.getString("in");
        tempRow[CURVECOL] = noneText;
        tempRow[REPORT_CURRENTCOL] = rbo.getString("Current");
        tempRow[PERMISSIONCOL] = rbo.getString("Permissive");
        tempRow[SPEEDCOL] = rbo.getString("Normal");
        tempRow[DELETE_COL] = rbo.getString("ButtonClear");
    }

    @Override
    public Manager getManager() {
        manager = InstanceManager.oBlockManagerInstance();
        return manager;
    }
    @Override
    public NamedBean getBySystemName(String name) {
        return manager.getBySystemName(name);
    }
    // Method name not appropriate (initial use was for Icon Editors)
    @Override
    public NamedBean addBean(String name) {
        return manager.getOBlock(name);
    }
    @Override
    public NamedBean addBean(String sysName, String userName) {
        return manager.createNewOBlock(sysName, userName);
    }
    @Override
    public boolean canAddBean() {
        return true;
    }

    @Override
    public int getColumnCount () {
        return NUMCOLS;
    }
    @Override
    public int getRowCount () {
        return super.getRowCount() + 1;
    }
    
    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public NamedBean getBeanAt(int index) {
    	if (index >=_pickList.size()) {
    		return null;
    	}
       return _pickList.get(index);
    }

    @Override
     public Object getValueAt(int row, int col) 
    {
        OBlock b = (OBlock)getBeanAt(row);
        switch (col) {
        	case SYSNAMECOL:
                if (b != null) {
                	return b.getSystemName();
                } else {
                	return tempRow[col];
                }
        	case USERNAMECOL:
                if (b != null) {
            		return b.getUserName();
                } else {
                	return tempRow[col];
                }
            case COMMENTCOL:
                if (b != null) {
                    return b.getComment();
                } else {
                	return tempRow[col];
                }
            case SENSORCOL:
                if (b != null) {
                    Sensor s = b.getSensor();
                    if (s==null) {
                         return "";
                    }
                    return s.getDisplayName();
                } else {
                	return tempRow[col];
                }
            case LENGTHCOL:
                if (b != null) {
                    if (b.isMetric()) {
                        return (twoDigit.format(b.getLengthCm()));
                    } else {
                        return (twoDigit.format(b.getLengthIn()));
                    }
                } else {
                	return tempRow[col];
                }
            case UNITSCOL:
                if (b != null) {
                    return b.isMetric();
                } else {
                    return Boolean.valueOf(tempRow[UNITSCOL].equals(rbo.getString("in")));
                }
            case CURVECOL:
                if (b != null) {
                    String c = "";
                    if (b.getCurvature()==Block.NONE) c = noneText;
                    else if (b.getCurvature()==Block.GRADUAL) c = gradualText;
                    else if (b.getCurvature()==Block.TIGHT) c = tightText;
                    else if (b.getCurvature()==Block.SEVERE) c = severeText;
                    return c;
                } else {
                	return tempRow[col];
                }
            case ERR_SENSORCOL:
                if (b != null) {
                	Sensor s = b.getErrorSensor();
                    if (s==null) { return "";}
                    return s.getDisplayName();
                } else {
                	return tempRow[col];
                }
            case REPORTERCOL:
                if (b != null) {
                    Reporter r = b.getReporter();
                    if (r==null) {return "";}
                    return r.getDisplayName();
                } else {
                	return tempRow[col];
                }
            case REPORT_CURRENTCOL:
                if (b != null) {
                	if (b.getReporter()!=null) {
                        return b.isReportingCurrent();                		
                	}
                	else {
                		return "";
                	}
                } else {
                    return Boolean.valueOf(tempRow[REPORT_CURRENTCOL].equals(rbo.getString("Current")));
                }
            case PERMISSIONCOL:
                if (b != null) {
                    return b.getPermissiveWorking();
                } else {
                    return Boolean.valueOf(tempRow[PERMISSIONCOL].equals(rbo.getString("Permissive")));
                }
            case SPEEDCOL:
                if (b != null) {
                	return b.getBlockSpeed();
                } else {
                	return tempRow[col];
                }
            case EDIT_COL:
                if (b != null) {
                    return rbo.getString("ButtonEditPath");
                } else {
                	return "";
                }
            case DELETE_COL:
                if (b != null) {
                    return AbstractTableAction.rb.getString("ButtonDelete");
                } else {
                	return rbo.getString("ButtonClear");
                }
        }
        return super.getValueAt(row, col);
    }    		

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (log.isDebugEnabled()) log.debug("setValueAt: row= "+row+", col= "+col+", value= "+(String)value);
        if (super.getRowCount() == row) 
        {
            if (col==SYSNAMECOL) {
                OBlock block = manager.createNewOBlock((String)value, tempRow[USERNAMECOL]);
                if (block==null) {
                    block = manager.getOBlock(tempRow[USERNAMECOL]);
                    String name = "blank";     // zero length string error
                    if (block!=null) {
                        name = block.getDisplayName();
                    }
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                        rbo.getString("CreateDuplBlockErr"), name),
                        AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (tempRow[SENSORCOL] != null) {
                    if (!sensorExists(tempRow[SENSORCOL])) {
                        JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rbo.getString("NoSuchSensorErr"), tempRow[SENSORCOL]),
                            AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    }
                }
                block.setComment(tempRow[COMMENTCOL]);
                float len = Float.valueOf(tempRow[LENGTHCOL]).floatValue();
                if (tempRow[UNITSCOL].equals(rbo.getString("cm"))) {
                    block.setLength(len*10.0f);
                    block.setMetricUnits(true);
                } else {
                    block.setLength(len*25.4f);
                    block.setMetricUnits(false);
                }
                if (tempRow[CURVECOL].equals(noneText)) {
                	block.setCurvature(Block.NONE);
                } else if (tempRow[CURVECOL].equals(gradualText)) {
                	block.setCurvature(Block.GRADUAL);
                } else if (tempRow[CURVECOL].equals(tightText)) {
                	block.setCurvature(Block.TIGHT);
                } else if (tempRow[CURVECOL].equals(severeText)) {
                	block.setCurvature(Block.SEVERE);
                }
                block.setPermissiveWorking(tempRow[PERMISSIONCOL].equals(rbo.getString("Permissive")));
                try {
                    block.setBlockSpeed(tempRow[SPEEDCOL]);
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + tempRow[SPEEDCOL]);
                    return;
                }
                if (tempRow[ERR_SENSORCOL] != null) {
                    if (tempRow[ERR_SENSORCOL].trim().length()>0) {
                        if (!sensorExists(tempRow[ERR_SENSORCOL])) {
                            JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                                rbo.getString("NoSuchSensorErr"), tempRow[ERR_SENSORCOL]),
                                AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
                if (tempRow[REPORTERCOL] != null) {
                    Reporter rep = null;
                    try {
                        rep = InstanceManager.reporterManagerInstance().getReporter(tempRow[REPORTERCOL]);
                        if (rep!=null) {
                        	block.setReporter(rep);
                            block.setReportingCurrent(tempRow[REPORT_CURRENTCOL].equals(rbo.getString("Current")));
                        }
                    } catch (Exception ex) {
                        log.error("No Reporter named \""+tempRow[REPORTERCOL]+"\" found. threw exception: "+ ex);
                    }
                    if (rep==null) {
                        JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rbo.getString("NoSuchReporterErr"), tempRow[REPORTERCOL]),
                            AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    }
                    block.setReporter(rep);                	
                }
                
                initTempRow();
                fireTableDataChanged();
          	
            }else {
            	switch (col) {
            	case DELETE_COL:			// clear
            		initTempRow();
            		fireTableRowsUpdated(row,row);
            		return;
            	case UNITSCOL:
                    if (((Boolean)value).booleanValue()) {//toggle
                        tempRow[UNITSCOL] = rbo.getString("in");
                    } else {
                        tempRow[UNITSCOL] = rbo.getString("cm");
                    }
                    return;
            	case REPORT_CURRENTCOL:
                    if (((Boolean)value).booleanValue()) {//toggle
                        tempRow[REPORT_CURRENTCOL] = rbo.getString("Current");
                    } else {
                        tempRow[REPORT_CURRENTCOL] = rbo.getString("Last");
                    }
                    return;
            	case PERMISSIONCOL:
                    if (((Boolean)value).booleanValue()) {//toggle
                        tempRow[PERMISSIONCOL] = rbo.getString("Permissive");
                    } else {
                        tempRow[PERMISSIONCOL] = rbo.getString("Absolute");
                    }
                    return;
            	}
            	tempRow[col] = (String)value;
            	return;
            }
        }
        OBlock block = (OBlock)getBeanAt(row);
        switch (col) {
            case USERNAMECOL:
                OBlock b = manager.getOBlock((String)value);
                if (b != null) {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                        rbo.getString("CreateDuplBlockErr"), block.getDisplayName()),
                        AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                    return;
                }
                block.setUserName((String)value);
                fireTableRowsUpdated(row,row);
                return;
            case COMMENTCOL:
                block.setComment((String)value);
                fireTableRowsUpdated(row,row);
                return;
            case SENSORCOL:
                if (!block.setSensor((String)value)) {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rbo.getString("NoSuchSensorErr"), (String)value),
                            AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);                	
                }
                fireTableRowsUpdated(row,row);
                return;
            case LENGTHCOL:
                float len = Float.valueOf((String)value).floatValue();
                if (block.isMetric()) {
                    block.setLength(len*10.0f);
                } else {
                    block.setLength(len*25.4f);
                }
                fireTableRowsUpdated(row,row);
                return;
            case UNITSCOL:
                block.setMetricUnits(((Boolean)value).booleanValue());
                fireTableRowsUpdated(row,row);
                return;
            case CURVECOL:
                String cName = (String)value;
                if (cName.equals(noneText)) {
                	block.setCurvature(Block.NONE);
                } else if (cName.equals(gradualText)) {
                	block.setCurvature(Block.GRADUAL);
                } else if (cName.equals(tightText)) {
                	block.setCurvature(Block.TIGHT);
                } else if (cName.equals(severeText)) {
                	block.setCurvature(Block.SEVERE);
                }
                fireTableRowsUpdated(row,row);
                return;
            case ERR_SENSORCOL:
            	boolean err = false;
                try {
                    if (((String)value).trim().length()==0) {
                        block.setErrorSensor(null);
                        err = true;
                     } else {
                        err = block.setErrorSensor((String)value);
                        fireTableRowsUpdated(row,row);
                    }
                } catch (Exception ex) {
                    log.error("getSensor("+(String)value+") threw exception: "+ ex);
                }
                if (err) {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                            rbo.getString("NoSuchSensorErr"), (String)value),
                            AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);                	
                }
                fireTableRowsUpdated(row,row);
                return;
            case REPORTERCOL:
                Reporter rep = null;
                try {
                    rep = InstanceManager.reporterManagerInstance().getReporter((String)value);
                    if (rep!=null) {
                        block.setReporter(rep);
                        fireTableRowsUpdated(row,row);
                   }
                } catch (Exception ex) {
                    log.error("No Reporter named \""+(String)value+"\" found. threw exception: "+ ex);
                }
                if (rep==null) {
                    JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(
                        rbo.getString("NoSuchReporterErr"), tempRow[REPORTERCOL]),
                        AbstractTableAction.rb.getString("ErrorTitle"), JOptionPane.WARNING_MESSAGE);
                }
                block.setReporter(rep);                	
                fireTableRowsUpdated(row,row);
                return;
            case REPORT_CURRENTCOL:
            	if (block.getReporter()!=null) {
            		block.setReportingCurrent(((Boolean) value).booleanValue());
                    fireTableRowsUpdated(row,row);
            	}
                return;
            case PERMISSIONCOL:
                block.setPermissiveWorking(((Boolean) value).booleanValue());
                fireTableRowsUpdated(row,row);
                return;
            case SPEEDCOL:
                try {
                	block.setBlockSpeed((String)value);
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + (String)value);
                    return;
                }
                fireTableRowsUpdated(row,row);
                return;            	
            case EDIT_COL:
                _parent.openBlockPathFrame(block.getSystemName());
                return;
            case DELETE_COL:
                deleteBean(block);
                block = null;
                return;
        }
        super.setValueAt(value, row, col);					
    }
    
    private boolean sensorExists(String name) {
        Sensor sensor = InstanceManager.sensorManagerInstance().getByUserName(name);
        if (sensor==null) {
        	sensor = InstanceManager.sensorManagerInstance().getBySystemName(name);
        }
        return (sensor!=null);   	
    }
    
    public String getColumnName(int col) {
        switch (col) {
            case COMMENTCOL: return AbstractTableAction.rb.getString("Comment");
            case SENSORCOL: return AbstractTableAction.rbean.getString("BeanNameSensor");
            case CURVECOL: return AbstractTableAction.rb.getString("BlockCurveColName");
            case LENGTHCOL: return AbstractTableAction.rb.getString("BlockLengthColName");
            case UNITSCOL: return "";
            case ERR_SENSORCOL: return rbo.getString("ErrorSensorCol");
            case REPORTERCOL: return rbo.getString("ReporterCol");
            case REPORT_CURRENTCOL: return rbo.getString("RepCurrentCol");
            case PERMISSIONCOL: return rbo.getString("PermissionCol");
            case SPEEDCOL: return rbo.getString("SpeedCol");
            case EDIT_COL: return "";
            case DELETE_COL: return "";
        }
        return super.getColumnName(col);
    }

    boolean noWarnDelete = false;

    void deleteBean(OBlock bean) {
        int count = bean.getNumPropertyChangeListeners()-2; // one is this table, other is manager
        if (log.isDebugEnabled()) {
            log.debug("Delete with "+count+" remaining listenner");
            //java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(bean);
            PropertyChangeListener[] listener=((jmri.implementation.AbstractNamedBean)bean).getPropertyChangeListeners();
            for (int i=0; i<listener.length; i++) {
                log.debug(i+") "+listener[i].getClass().getName());
            }
        }
        if (!noWarnDelete) {
            String msg;
            if (count>0) { // warn of listeners attached before delete
                msg = java.text.MessageFormat.format(
                        AbstractTableAction.rb.getString("DeletePrompt"), bean.getSystemName())+"\n"+
                      java.text.MessageFormat.format(AbstractTableAction.rb.getString("ReminderInUse"),
                        count);
            } else {
                msg = java.text.MessageFormat.format(
                        AbstractTableAction.rb.getString("DeletePrompt"),
                        new Object[]{bean.getSystemName()});
            }

            // verify deletion
            int val = JOptionPane.showOptionDialog(null, 
                    msg, AbstractTableAction.rb.getString("WarningTitle"), 
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new Object[]{AbstractTableAction.rb.getString("ButtonYes"),
                                 AbstractTableAction.rb.getString("ButtonYesPlus"),
                                 AbstractTableAction.rb.getString("ButtonNo")},
                    AbstractTableAction.rb.getString("ButtonNo"));
            if (val == 2) return;  // return without deleting
            if (val == 1) { // suppress future warnings
                noWarnDelete = true;
            }
        }
        // finally OK, do the actual delete
        bean.dispose();
    }

    public Class<?> getColumnClass(int col) {
    	switch (col) {
    	case CURVECOL:
    	case SPEEDCOL:
            return JComboBox.class;
    	case DELETE_COL:
    	case EDIT_COL:
            return JButton.class;
    	case UNITSCOL:
    	case REPORT_CURRENTCOL:
    	case PERMISSIONCOL:
            return Boolean.class;
    	}
        return String.class;
    }

    public int getPreferredWidth(int col) {
        switch (col) {
            case SYSNAMECOL: return new JTextField(18).getPreferredSize().width;
            case USERNAMECOL: return new JTextField(18).getPreferredSize().width;
            case COMMENTCOL: return new JTextField(10).getPreferredSize().width;
            case SENSORCOL: return new JTextField(15).getPreferredSize().width;
            case CURVECOL: return new JTextField(6).getPreferredSize().width;
            case LENGTHCOL: return new JTextField(5).getPreferredSize().width;
            case UNITSCOL: return new JTextField(2).getPreferredSize().width;
            case ERR_SENSORCOL: return new JTextField(15).getPreferredSize().width;
            case REPORTERCOL: return new JTextField(15).getPreferredSize().width;
            case REPORT_CURRENTCOL: return new JTextField(6).getPreferredSize().width;
            case PERMISSIONCOL: return new JTextField(6).getPreferredSize().width;
            case SPEEDCOL: return new JTextField(8).getPreferredSize().width;
            case EDIT_COL: return new JButton("DELETE").getPreferredSize().width;
            case DELETE_COL: return new JButton("DELETE").getPreferredSize().width;
        }
        return 5;
    }

    public boolean isCellEditable(int row, int col) {
        if (super.getRowCount() == row) return true;
        if (col==SYSNAMECOL) return false;
        else return true;
    }

    public void propertyChange(PropertyChangeEvent e) {
        super.propertyChange(e);
        String property = e.getPropertyName();
        if (log.isDebugEnabled()) log.debug("PropertyChange = "+property);
        _parent.getPortalModel().propertyChange(e);
        _parent.getXRefModel().propertyChange(e);

        if (property.equals("length") || property.equals("UserName")
                            || property.equals("portalCount")) {
            _parent.updateOpenMenu();
        }
    }

    static Logger log = LoggerFactory.getLogger(OBlockTableModel.class.getName());
}
