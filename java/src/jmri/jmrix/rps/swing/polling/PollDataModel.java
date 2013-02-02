// PollDataModel.java

package jmri.jmrix.rps.swing.polling;

import org.apache.log4j.Logger;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.MeasurementListener;

import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.table.*;


/**
 * Pane for user management of RPS alignment.
 
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision$
 */
public class PollDataModel extends AbstractTableModel
    implements MeasurementListener {

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.rps.swing.polling.PollingBundle");

    static final int NAMECOL   = 0;
    static final int IDCOL   = 1;
    static final int ADDRCOL = 2;
    static final int LONGCOL = 3;
    static final int POLLCOL = 4;
    static final int LASTXCOL = 5;
    static final int LASTYCOL = 6;
    static final int LASTZCOL = 7;
    static final int LASTTIME = 8;

    static final int LAST = 8;
    jmri.ModifiedFlag modifiedFlag;
    
    static final int TYPECOL = -1;
    
    public PollDataModel(jmri.ModifiedFlag flag) {
        super();
        this.modifiedFlag = flag;
        Distributor.instance().addMeasurementListener(this);
        fireTableDataChanged();
    }
    
    public int getColumnCount () {return LAST+1;}

    public int getRowCount () {
        return Engine.instance().getNumTransmitters();
    }

    public String getColumnName(int c) {
        switch (c) {
        case NAMECOL:
            return rb.getString("TitleName");
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
        case LASTTIME:
            return rb.getString("TitleTime");
        default:
            return "";
        }
    }

    public Class<?> getColumnClass(int c) {
        if (c == LONGCOL || c == POLLCOL)
            return Boolean.class;
        else if (c == ADDRCOL || c == LASTTIME)
            return Integer.class;
        else if (c == TYPECOL)
            return JComboBox.class;
        else if (c == LASTXCOL || c == LASTYCOL || c == LASTZCOL )
            return Double.class;
        else 
            return String.class;
    }

    public boolean isCellEditable(int r,int c) {
        if (c == IDCOL || c == POLLCOL || c == TYPECOL )
            return true;
        else 
            return false;
    }

    public Object getValueAt (int r,int c) {
        // r is row number, from 0
        Measurement m;
        if (Engine.instance() == null )
            log.warn("returning null because of missing Engine.instance()");
        if (Engine.instance().getTransmitter(r) == null) 
            log.warn("returning null because of missing Engine.instance().getTransmitter("+r+")");
        
        double val;
        switch (c) {
        case NAMECOL:
            return Engine.instance().getTransmitter(r).getRosterName();
        case IDCOL:
            return Engine.instance().getTransmitter(r).getID();
        case ADDRCOL:
            return Integer.valueOf(Engine.instance().getTransmitter(r).getAddress());
        case LONGCOL:
            return Boolean.valueOf(Engine.instance().getTransmitter(r).isLongAddress());
        case POLLCOL:
            return Boolean.valueOf(Engine.instance().getTransmitter(r).isPolled());
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
        case LASTTIME:
            m = Engine.instance().getTransmitter(r).getLastMeasurement();
            if (m == null) return null;
            int time = m.getReading().getTime();
            return Integer.valueOf(time);
        default:
            return null;
        }
    }

    public void setValueAt(Object value,int r,int c) {
        // r is row number, from 0
        switch (c) {
        case IDCOL:
            String s = ((String)value);
            Engine.instance().getTransmitter(r).setID(s);
            modifiedFlag.setModifiedFlag(true);
            return;
        case POLLCOL:
            boolean p = ((Boolean)value).booleanValue();
            Engine.instance().getTransmitter(r).setPolled(p);
            modifiedFlag.setModifiedFlag(true);
            return;
        case TYPECOL:
            log.error("Got "+((JComboBox)value).getSelectedItem()+" but did not act");
        }
    }
    
    // When a measurement happens, mark data as changed.
    // It would be better to just mark one line...
    public void notify(Measurement m) {
        fireTableDataChanged();
    }

    public void dispose() {
        Distributor.instance().removeMeasurementListener(this);
    }
    
    static Logger log = Logger.getLogger(PollDataModel.class.getName());

}
