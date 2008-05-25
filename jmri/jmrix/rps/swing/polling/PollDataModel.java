// PollDataModel.java

package jmri.jmrix.rps.swing.polling;

import jmri.jmrix.rps.Engine;

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
 * @version	$Revision: 1.2 $
 */
public class PollDataModel extends AbstractTableModel {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.rps.swing.polling.PollingBundle");

    static final int IDCOL   = 0;
    static final int ADDRCOL = 1;
    static final int LONGCOL = 2;
    static final int POLLCOL = 3;
    static final int TYPECOL = 4;
    
    static final int LAST = 4;
    jmri.ModifiedFlag modifiedFlag;
    
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
