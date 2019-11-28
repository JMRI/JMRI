package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.IdTag;
import jmri.Manager;
import jmri.NamedBean;
import jmri.RailCom;
import jmri.RailComManager;
import jmri.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a RailCommTable GUI.
 *
 * @author  Bob Jacobsen Copyright (C) 2003
 * @author  Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class RailComTableAction extends AbstractTableAction<IdTag> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public RailComTableAction(String actionName) {
        super(actionName);

        includeAddButton = false;
    }

    @Nonnull
    protected RailComManager tagManager = InstanceManager.getDefault(jmri.RailComManager.class);

    public RailComTableAction() {
        this("Rail Com Table");
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of RailComm objects
     */
    @Override
    protected void createModel() {
        m = new BeanTableDataModel<IdTag>() {

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
                RailCom tag = (RailCom) tagManager.getBySystemName(name);
                if (tag == null) {
                    return "?";
                }
                return tag.getTagID();
            }

            @Override
            public RailComManager getManager() {
                return tagManager;
            }

            @Override
            public RailCom getBySystemName(String name) {
                return (RailCom) tagManager.getBySystemName(name);
            }

            @Override
            public RailCom getByUserName(String name) {
                return (RailCom) tagManager.getByUserName(name);
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
                        log.debug("Clear where & when last seen for " + t.getSystemName());
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
                        Reporter r;
                        return (((r = t.getWhereLastSeen()) != null) ? r.getSystemName() : null);
                    case WHENCOL:
                        Date d;
                        return (((d = t.getWhenLastSeen()) != null)
                                ? DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(d) : null);
                    case CLEARCOL:
                        return Bundle.getMessage("ButtonClear");
                    case SPEEDCOL:
                        if (t.getActualSpeed() == -1) {
                            return null;
                        }
                        return t.getActualSpeed();
                    case LOADCOL:
                        if (t.getActualLoad() == -1) {
                            return null;
                        }
                        return t.getActualLoad();
                    case TEMPCOL:
                        if (t.getActualTemperature() == -1) {
                            return null;
                        }
                        return t.getActualTemperature();
                    case FUELCOL:
                        if (t.getFuelLevel() == -1) {
                            return null;
                        }
                        return t.getFuelLevel();
                    case WATERCOL:
                        if (t.getWaterLevel() == -1) {
                            return null;
                        }
                        return t.getWaterLevel();
                    case LOCATIONCOL:
                        if (t.getLocation() == -1) {
                            return null;
                        }
                        return t.getLocation();
                    case ROUTINGCOL:
                        if (t.getRoutingNo() == -1) {
                            return null;
                        }
                        return t.getRoutingNo();
                    case DELETECOL:  //
                        return Bundle.getMessage("ButtonDelete");
                    default:
                        // fall through
                        break;
                }
                return null;
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
                return getClassName();
            }

            @Override
            protected String getBeanType() {
                return "ID Tag";
            }
        };
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleRailComTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.RailComTable";
    }

    @Override
    protected void addPressed(ActionEvent e) {
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleRailComTable");
    }

    @Override
    public void addToFrame(BeanTableFrame f) {
        log.debug("Added CheckBox in addToFrame method");
    }

    @Override
    public void addToPanel(AbstractTableTabAction<IdTag> f) {
        log.debug("Added CheckBox in addToPanel method");
    }

    @Override
    protected String getClassName() {
        return RailComTableAction.class.getName();
    }
    private static final Logger log = LoggerFactory.getLogger(RailComTableAction.class);
}
