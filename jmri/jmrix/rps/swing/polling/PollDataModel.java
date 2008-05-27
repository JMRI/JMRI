// PollDataModel.java

package jmri.jmrix.rps.swing.polling;

import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;

import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.Border;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Pane for user management of RPS alignment.
 
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision: 1.3 $
 */
public class PollDataModel extends AbstractTableModel {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.rps.swing.polling.PollingBundle");

    static final int IDCOL   = 0;
    static final int ADDRCOL = 1;
    static final int LONGCOL = 2;
    static final int POLLCOL = 3;
    static final int LASTXCOL = 4;
    static final int LASTYCOL = 5;
    static final int LASTZCOL = 6;

    static final int LAST = 6;
    jmri.ModifiedFlag modifiedFlag;
    
    static final int TYPECOL = -1;
    
    public PollDataModel(jmri.ModifiedFlag flag) {
        super();
        this.modifiedFlag = flag;
    }
    
    public int getColumnCount () {return LAST+1;}

    public int getRowCount () {
        return Engine.instance().getNumTransmitters();
    }

    public String getColumnName(int c) {
        switch (c) {
        case IDCOL:
            return rb.getString("TitleIdCol");
        case ADDRCOL:
            return rb.getString("TitleAddrCol");
        case LONGCOL:
            return rb.getString("TitleLongCol");
        case POLLCOL:
            return rb.getString("TitlePollCol");
        case TYPECOL:
            return rb.getString("TitleTypeCol");
        case LASTXCOL:
            return rb.getString("TitleXCol");
        case LASTYCOL:
            return rb.getString("TitleYCol");
        case LASTZCOL:
            return rb.getString("TitleZCol");
        default:
            return "";
        }
    }

    public Class getColumnClass(int c) {
        if (c == LONGCOL || c == POLLCOL)
            return Boolean.class;
        else if (c == ADDRCOL)
            return Integer.class;
        else if (c == TYPECOL)
            return JComboBox.class;
        else if (c == LASTXCOL || c == LASTYCOL || c == LASTZCOL )
            return Double.class;
        else 
            return String.class;
    }

    public boolean isCellEditable(int r,int c) {
        if (c == POLLCOL || c == TYPECOL )
            return true;
        else 
            return false;
    }

    public Object getValueAt (int r,int c) {
        // r is row number, from 0
        Measurement m;
        double val;
        switch (c) {
        case IDCOL:
            return Engine.instance().getTransmitter(r).getID();
        case ADDRCOL:
            return new Integer(Engine.instance().getTransmitter(r).getAddress());
        case LONGCOL:
            return new Boolean(Engine.instance().getTransmitter(r).isLongAddress());
        case POLLCOL:
            return new Boolean(Engine.instance().getTransmitter(r).isPolled());
        case TYPECOL:
            JComboBox b = new JComboBox(new String[]{"F2", "F3", "BSCI"});
            return b;
        case LASTXCOL:
            m = Engine.instance().getTransmitter(r).getLastMeasurement();
            if (m == null) return null;
            val = m.getX();
            return new Double(val);
        case LASTYCOL:
            m = Engine.instance().getTransmitter(r).getLastMeasurement();
            if (m == null) return null;
            val = m.getY();
            return new Double(val);
        case LASTZCOL:
            m = Engine.instance().getTransmitter(r).getLastMeasurement();
            if (m == null) return null;
            val = m.getZ();
            return new Double(val);
        default:
            return null;
        }
    }

    public void setValueAt(Object value,int r,int c) {
        // r is row number, from 0
        switch (c) {
        case POLLCOL:
            boolean p = ((Boolean)value).booleanValue();
            Engine.instance().getTransmitter(r).setPolled(p);
            modifiedFlag.setModifiedFlag(true);
            return;
        case TYPECOL:
            System.out.println("Got "+((JComboBox)value).getSelectedItem());
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PollDataModel.class.getName());

}
