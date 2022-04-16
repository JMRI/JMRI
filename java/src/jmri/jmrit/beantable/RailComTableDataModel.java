package jmri.jmrit.beantable;

import java.text.DateFormat;
import java.util.Date;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * TableDataModel for the RailCom Table.
 *
 * Split from {@link RailComTableAction}
 *
 * @author  Bob Jacobsen Copyright (C) 2003
 * @author  Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 * @author Steve Young Copyright (C) 2021
 */
public class RailComTableDataModel extends BeanTableDataModel<IdTag> {

    /**
     * Create a new Memory Table Data Model.
     * @param mgr Memory manager to use in the model, default MemoryManager always used.
     */
    public RailComTableDataModel(Manager<IdTag> mgr){
        super();
        setManager(mgr);
    }
    
    static public final int VALUECOL = 0;
    public static final int WHERECOL = VALUECOL + 1;
    public static final int WHENCOL = WHERECOL + 1;
    public static final int CLEARCOL = WHENCOL + 1;
    public static final int SPEEDCOL = CLEARCOL + 1;
    public static final int LOADCOL = SPEEDCOL + 1;
    public static final int TEMPCOL = LOADCOL + 1;
    public static final int FUELCOL = TEMPCOL + 1;
    public static final int WATERCOL = FUELCOL + 1;
    public static final int LOCATIONCOL = WATERCOL + 1;
    public static final int ROUTINGCOL = LOCATIONCOL + 1;
    static public final int DELETECOL = ROUTINGCOL + 1;

    static public final int NUMCOLUMN = DELETECOL + 1;

    @Override
    public String getValue(String name) {
        RailCom tag = (RailCom) getManager().getBySystemName(name);
        if (tag == null) {
            return "?";
        }
        return tag.getTagID();
    }
    
    private RailComManager manager;
    
    @Override
    public final void setManager(Manager<IdTag> mgr){
        if (mgr instanceof RailComManager){
            manager = (RailComManager)mgr;
        }
    }

    @Override
    public RailComManager getManager() {
        return ( manager!=null ? manager : InstanceManager.getDefault(RailComManager.class));
    }

    @Override
    public RailCom getBySystemName(@Nonnull String name) {
        return (RailCom) getManager().getBySystemName(name);
    }

    @Override
    public RailCom getByUserName(@Nonnull String name) {
        return (RailCom) getManager().getByUserName(name);
    }

    @Override
    public void clickOn(IdTag t) {
        // don't do anything on click; not used in this class, because
        // we override setValueAt
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == CLEARCOL) {
            RailCom t = getBySystemName(sysNameList.get(row));
            if (log.isDebugEnabled()) {
                log.debug("Clear where & when last seen for {}", t.getSystemName());
            }
            t.setWhereLastSeen(null);
            fireTableRowsUpdated(row, row);
        } else if (col == DELETECOL) {
            // button fired, delete Bean
            deleteBean(row, col);
        }
    }

    @Override
    public int getColumnCount() {
        return NUMCOLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case VALUECOL:
                return Bundle.getMessage("ColumnAddress");
            case WHERECOL:
                return Bundle.getMessage("ColumnIdWhere");
            case WHENCOL:
                return Bundle.getMessage("ColumnIdWhen");
            case SPEEDCOL:
                return Bundle.getMessage("ColumnSpeed");
            case LOADCOL:
                return Bundle.getMessage("ColumnLoad");
            case TEMPCOL:
                return Bundle.getMessage("ColumnTemp");
            case FUELCOL:
                return Bundle.getMessage("ColumnFuelLevel");
            case WATERCOL:
                return Bundle.getMessage("ColumnWaterLevel");
            case LOCATIONCOL:
                return Bundle.getMessage("ColumnLocation");
            case ROUTINGCOL:
                return Bundle.getMessage("ColumnRouting");
            case DELETECOL:
                return "";
            case CLEARCOL:
                return "";
            default:
                return super.getColumnName(col);
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case DELETECOL:
            case CLEARCOL:
                return JButton.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case DELETECOL:
            case CLEARCOL:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        RailCom t = getBySystemName(sysNameList.get(row));
        if (t == null) {
            return null;
        }
        switch (col) {
            case VALUECOL:
                return t.getLocoAddress().toString();
            case WHERECOL:
                Reporter r = t.getWhereLastSeen();
                return (r != null ? r.getSystemName() : null);
            case WHENCOL:
                Date d;
                return (((d = t.getWhenLastSeen()) != null)
                        ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(d) : null);
            case CLEARCOL:
                return Bundle.getMessage("ButtonClear");
            case SPEEDCOL:
                return (t.getActualSpeed()!=-1 ? t.getActualSpeed() : null);
            case LOADCOL:
                return (t.getActualLoad()!=-1 ? t.getActualLoad() : null);
            case TEMPCOL:
                return (t.getActualTemperature()!=-1 ? t.getActualTemperature() : null);
            case FUELCOL:
                return (t.getFuelLevel()!=-1 ? t.getFuelLevel() : null);
            case WATERCOL:
                return (t.getWaterLevel()!=-1 ? t.getWaterLevel() : null);
            case LOCATIONCOL:
                return (t.getLocation()!=-1 ? t.getLocation() : null);
            case ROUTINGCOL:
                return (t.getRoutingNo()!=-1 ? t.getRoutingNo() : null);
            case DELETECOL:  //
                return Bundle.getMessage("ButtonDelete");
            default:
                return super.getValueAt(row, col);
        }
    }

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case WHERECOL:
            case WHENCOL:
                return new JTextField(12).getPreferredSize().width;
            case VALUECOL:
                return new JTextField(10).getPreferredSize().width;
            case CLEARCOL:
            case DELETECOL:
                return new JButton(Bundle.getMessage("ButtonClear")).getPreferredSize().width + 4;
            default:
                return new JTextField(5).getPreferredSize().width;
        }
    }

    @Override
    public void configValueColumn(JTable table) {
        // value column isn't button, so config is null
    }

    @Override
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        return true;
        // return (e.getPropertyName().indexOf("alue")>=0);
    }

    @Override
    public JButton configureButton() {
        log.error("configureButton should not have been called");
        return null;
    }

    @Override
    protected String getMasterClassName() {
        return RailComTableAction.class.getName();
    }

    @Override
    protected String getBeanType() {
        return "ID Tag";
    }
    
    private static final Logger log = LoggerFactory.getLogger(RailComTableDataModel.class);

}
