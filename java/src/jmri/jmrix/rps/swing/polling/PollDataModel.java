package jmri.jmrix.rps.swing.polling;

import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.MeasurementListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for user management of RPS alignment.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class PollDataModel extends AbstractTableModel implements MeasurementListener {

    static final int NAMECOL = 0;
    static final int IDCOL = 1;
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

    @Override
    public int getColumnCount() {
        return LAST + 1;
    }

    @Override
    public int getRowCount() {
        return Engine.instance().getNumTransmitters();
    }

    @Override
    public String getColumnName(int c) {
        switch (c) {
            case NAMECOL:
                return Bundle.getMessage("TitleName");
            case IDCOL:
                return Bundle.getMessage("TitleIdCol");
            case ADDRCOL:
                return Bundle.getMessage("TitleAddrCol");
            case LONGCOL:
                return Bundle.getMessage("TitleLongCol");
            case POLLCOL:
                return Bundle.getMessage("TitlePollCol");
            case TYPECOL:
                return Bundle.getMessage("TitleTypeCol");
            case LASTXCOL:
                return Bundle.getMessage("TitleXCol");
            case LASTYCOL:
                return Bundle.getMessage("TitleYCol");
            case LASTZCOL:
                return Bundle.getMessage("TitleZCol");
            case LASTTIME:
                return Bundle.getMessage("TitleTime");
            default:
                return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int c) {
        if (c == LONGCOL || c == POLLCOL) {
            return Boolean.class;
        } else if (c == ADDRCOL || c == LASTTIME) {
            return Integer.class;
        } else if (c == TYPECOL) {
            return JComboBox.class;
        } else if (c == LASTXCOL || c == LASTYCOL || c == LASTZCOL) {
            return Double.class;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        if (c == IDCOL || c == POLLCOL || c == TYPECOL) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object getValueAt(int r, int c) {
        // r is row number, from 0
        Measurement m;
        if (Engine.instance() == null) {
            log.warn("returning null because of missing Engine.instance()");
        }
        if (Engine.instance().getTransmitter(r) == null) {
            log.warn("returning null because of missing Engine.instance().getTransmitter({})", r);
        }

        double val;
        switch (c) {
            case NAMECOL:
                return Engine.instance().getTransmitter(r).getRosterName();
            case IDCOL:
                return Engine.instance().getTransmitter(r).getId();
            case ADDRCOL:
                return Integer.valueOf(Engine.instance().getTransmitter(r).getAddress());
            case LONGCOL:
                return Boolean.valueOf(Engine.instance().getTransmitter(r).isLongAddress());
            case POLLCOL:
                return Boolean.valueOf(Engine.instance().getTransmitter(r).isPolled());
            case TYPECOL:
                JComboBox<String> b = new JComboBox<String>(new String[]{"F2", "F3", "BSCI"});
                return b;
            case LASTXCOL:
                m = Engine.instance().getTransmitter(r).getLastMeasurement();
                if (m == null) {
                    return null;
                }
                val = m.getX();
                return Double.valueOf(val);
            case LASTYCOL:
                m = Engine.instance().getTransmitter(r).getLastMeasurement();
                if (m == null) {
                    return null;
                }
                val = m.getY();
                return Double.valueOf(val);
            case LASTZCOL:
                m = Engine.instance().getTransmitter(r).getLastMeasurement();
                if (m == null) {
                    return null;
                }
                val = m.getZ();
                return Double.valueOf(val);
            case LASTTIME:
                m = Engine.instance().getTransmitter(r).getLastMeasurement();
                if (m == null) {
                    return null;
                }
                int time = m.getReading().getTime();
                return Integer.valueOf(time);
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int r, int c) {
        // r is row number, from 0
        switch (c) {
            case IDCOL:
                String s = ((String) value);
                Engine.instance().getTransmitter(r).setId(s);
                modifiedFlag.setModifiedFlag(true);
                return;
            case POLLCOL:
                boolean p = ((Boolean) value).booleanValue();
                Engine.instance().getTransmitter(r).setPolled(p);
                modifiedFlag.setModifiedFlag(true);
                return;
            case TYPECOL:
                log.error("Got {} but did not act", ((JComboBox<?>) value).getSelectedItem());
                break;
            default:
                log.warn("Unhandled col {}", c);
                break;
        }
    }

    // When a measurement happens, mark data as changed.
    // It would be better to just mark one line...
    @Override
    public void notify(Measurement m) {
        fireTableDataChanged();
    }

    public void dispose() {
        Distributor.instance().removeMeasurementListener(this);
    }

    private final static Logger log = LoggerFactory.getLogger(PollDataModel.class);

}
