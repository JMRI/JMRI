package jmri.jmrit.beantable.sensor;

import jmri.util.gui.GuiLafPreferencesManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.util.swing.XTableColumnModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data model for a SensorTable.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2009
 * @author Egbert Broerse Copyright (C) 2017
 */
public class SensorTableDataModel extends BeanTableDataModel<Sensor> {

    static public final int INVERTCOL = BeanTableDataModel.NUMCOLUMN;
    static public final int EDITCOL = INVERTCOL + 1;
    static public final int USEGLOBALDELAY = EDITCOL + 1;
    static public final int ACTIVEDELAY = USEGLOBALDELAY + 1;
    static public final int INACTIVEDELAY = ACTIVEDELAY + 1;
    static public final int PULLUPCOL = INACTIVEDELAY + 1;
    static public final int FORGETCOL = PULLUPCOL + 1;
    static public final int QUERYCOL = FORGETCOL + 1;

    private Manager<Sensor> senManager = null;
    protected boolean _graphicState = false; // icon state col updated from prefs

    /**
     * Create a new Sensor Table Data Model.
     * The default Manager for the bean type will be a Proxy Manager.
     */
    public SensorTableDataModel() {
        super();
        _graphicState = InstanceManager.getDefault(GuiLafPreferencesManager.class).isGraphicTableState();
    }

    /**
     * Create a new Sensor Table Data Model.
     * The default Manager for the bean type will be a Proxy Manager unless
     * one is specified here.
     * @param manager Bean Manager.
     */
    public SensorTableDataModel(Manager<Sensor> manager) {
        super();
        setManager(manager); // updates name list
        // load graphic state column display preference
        _graphicState = InstanceManager.getDefault(GuiLafPreferencesManager.class).isGraphicTableState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue(String name) {
        Sensor sen = getManager().getBySystemName(name);
        if (sen == null) {
            return "Failed to get sensor " + name;
        }
        return sen.describeState(sen.getKnownState());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void setManager(@Nonnull Manager<Sensor> manager) {
        if (!(manager instanceof SensorManager)) {
            return;
        }
        getManager().removePropertyChangeListener(this);
        if (sysNameList != null) {
            for (int i = 0; i < sysNameList.size(); i++) {
                // if object has been deleted, it's not here; ignore it
                NamedBean b = getBySystemName(sysNameList.get(i));
                if (b != null) {
                    b.removePropertyChangeListener(this);
                }
            }
        }
        senManager = manager;
        getManager().addPropertyChangeListener(this);
        updateNameList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Manager<Sensor> getManager() {
        if (senManager == null) {
            senManager = InstanceManager.sensorManagerInstance();
        }
        return senManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Sensor getBySystemName(@Nonnull String name) {
        return getManager().getBySystemName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Sensor getByUserName(@Nonnull String name) {
        return InstanceManager.getDefault(SensorManager.class).getByUserName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getMasterClassName() {
        return getClassName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void clickOn(Sensor t) {
        try {
            t.setKnownState(t.getKnownState() == Sensor.INACTIVE ? Sensor.ACTIVE : Sensor.INACTIVE );
        } catch (JmriException e) {
            log.warn("Error setting state", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return QUERYCOL + getPropertyColumnCount() + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case INVERTCOL:
                return Bundle.getMessage("Inverted");
            case EDITCOL:
                return "";
            case USEGLOBALDELAY:
                return Bundle.getMessage("SensorUseGlobalDebounce");
            case ACTIVEDELAY:
                return Bundle.getMessage("SensorActiveDebounce");
            case INACTIVEDELAY:
                return Bundle.getMessage("SensorInActiveDebounce");
            case PULLUPCOL:
                return Bundle.getMessage("SensorPullUp");
            case FORGETCOL:
                return Bundle.getMessage("StateForgetHeader");
            case QUERYCOL:
                return Bundle.getMessage("StateQueryHeader");
            default:
                return super.getColumnName(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case INVERTCOL:
            case USEGLOBALDELAY:
                return Boolean.class;
            case ACTIVEDELAY:
            case INACTIVEDELAY:
                return Long.class; // if long.class (lowercase) is returned here, cell is NOT editable.
            case PULLUPCOL:
                return JComboBox.class;
            case EDITCOL:
            case FORGETCOL:
            case QUERYCOL:
                return JButton.class;
            case VALUECOL:
                if (_graphicState) {
                    return JLabel.class; // use an image to show sensor state
                } else {
                    return super.getColumnClass(col);
                }
            default:
                return super.getColumnClass(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case INVERTCOL:
                return new JTextField(4).getPreferredSize().width;
            case USEGLOBALDELAY:
            case ACTIVEDELAY:
            case INACTIVEDELAY:
            case PULLUPCOL:
                return new JTextField(8).getPreferredSize().width;
            case EDITCOL:
                return new JButton(Bundle.getMessage("ButtonEdit")).getPreferredSize().width+4;
            case FORGETCOL:
                return new JButton(Bundle.getMessage("StateForgetButton"))
                        .getPreferredSize().width+4;
            case QUERYCOL:
                return new JButton(Bundle.getMessage("StateQueryButton"))
                        .getPreferredSize().width+4;
            default:
                return super.getPreferredWidth(col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        String name = sysNameList.get(row);
        Sensor sen = getManager().getBySystemName(name);
        if (sen == null) {
            return false;
        }
        switch (col) {
            case EDITCOL:
            case USEGLOBALDELAY:
            case FORGETCOL:
            case QUERYCOL:
                return true;
            case INVERTCOL:
                return sen.canInvert();
            case ACTIVEDELAY:
            case INACTIVEDELAY:
                return !sen.getUseDefaultTimerSettings();
            case PULLUPCOL:
                return (((SensorManager) getManager()).isPullResistanceConfigurable());
            default:
                return super.isCellEditable(row, col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int row, int col) {
        if (row >= sysNameList.size()) {
            log.debug("row is greater than name list");
            return "";
        }
        String name = sysNameList.get(row);
        Sensor s = senManager.getBySystemName(name);
        if (s == null) {
            log.debug("error null sensor!");
            return "error";
        }
        switch (col) {
            case INVERTCOL:
                return s.getInverted();
            case USEGLOBALDELAY:
                return s.getUseDefaultTimerSettings();
            case ACTIVEDELAY:
                return s.getSensorDebounceGoingActiveTimer();
            case INACTIVEDELAY:
                return s.getSensorDebounceGoingInActiveTimer();
            case EDITCOL:
                return Bundle.getMessage("ButtonEdit");
            case PULLUPCOL:
                PullResistanceComboBox c = new PullResistanceComboBox(s.getAvailablePullValues());
                c.setSelectedItem(s.getPullResistance());
                return c;
            case FORGETCOL:
                return Bundle.getMessage("StateForgetButton");
            case QUERYCOL:
                return Bundle.getMessage("StateQueryButton");
            default:
                return super.getValueAt(row, col);
        }
    }
    
    /**
     * Small class to ensure type-safety of references otherwise lost to type erasure
     */
    static private class PullResistanceComboBox extends JComboBox<Sensor.PullResistance> {
        public PullResistanceComboBox(Sensor.PullResistance[] values) { super(values); }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (row >= sysNameList.size()) {
            log.debug("row is greater than name list");
            return;
        }
        String name = sysNameList.get(row);
        Sensor s = senManager.getBySystemName(name);
        if (s == null) {
            log.debug("error null sensor!");
            return;
        }
        switch (col) {
            case INVERTCOL:
                s.setInverted(((boolean) value));
                break;
            case USEGLOBALDELAY:
                s.setUseDefaultTimerSettings(((boolean) value));
                break;
            case ACTIVEDELAY:
                try {
                    long activeDeBounce = (long) value;
                    if (activeDeBounce < 0 || activeDeBounce > Sensor.MAX_DEBOUNCE) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceActOutOfRange")
                            + "\n\"" + Sensor.MAX_DEBOUNCE + "\"", Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        s.setSensorDebounceGoingActiveTimer(activeDeBounce);
                    }
                } catch (NumberFormatException exActiveDeBounce) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceActError")
                        + "\n\"" + value  + "\"" + exActiveDeBounce.getLocalizedMessage(), Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case INACTIVEDELAY:
                try {
                    long inactiveDeBounce = (long) value;
                    if (inactiveDeBounce < 0 || inactiveDeBounce > Sensor.MAX_DEBOUNCE) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceInActOutOfRange") 
                            + "\n\"" + Sensor.MAX_DEBOUNCE + "\"", Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                    } else {
                        s.setSensorDebounceGoingInActiveTimer(inactiveDeBounce);
                    }
                } catch (NumberFormatException exActiveDeBounce) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceInActError")
                        + "\n\"" + value + "\"" + exActiveDeBounce.getLocalizedMessage(), Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                }
                break;
            case EDITCOL:
                javax.swing.SwingUtilities.invokeLater(() -> {
                    editButton(s);
                });
                break;
            case PULLUPCOL:
                PullResistanceComboBox cb = (PullResistanceComboBox) value;
                s.setPullResistance((Sensor.PullResistance) cb.getSelectedItem());
                break;
            case FORGETCOL:
                try {
                    s.setKnownState(Sensor.UNKNOWN);
                } catch (JmriException e) {
                    log.warn("Failed to set state to UNKNOWN: ", e);
                }
                break;
            case QUERYCOL:
                try {
                    s.setKnownState(Sensor.UNKNOWN);
                } catch (JmriException e) {
                    log.warn("Failed to set state to UNKNOWN: ", e);
                }
                s.requestUpdateFromLayout();
                break;
            case VALUECOL:
                if (_graphicState) { // respond to clicking on ImageIconRenderer CellEditor
                    clickOn(s);
                    fireTableRowsUpdated(row, row);
                } else {
                    super.setValueAt(value, row, col);
                }
                break;
            default:
                super.setValueAt(value, row, col);
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean matchPropertyName(PropertyChangeEvent e) {
        switch (e.getPropertyName()) {
            case "inverted":
            case "GlobalTimer":
            case "ActiveTimer":
            case "InActiveTimer":
                return true;
            default:
                return super.matchPropertyName(e);
        }
    }

    /**
     * Customize the sensor table Value (State) column to show an appropriate
     * graphic for the sensor state if _graphicState = true, or (default) just
     * show the localized state text when the TableDataModel is being called
     * from ListedTableAction.
     *
     * @param table a JTable of Sensors
     */
    @Override
    protected void configValueColumn(JTable table) {
        // have the value column hold a JPanel (icon)
        //setColumnToHoldButton(table, VALUECOL, new JLabel("1234")); // for small round icon, but cannot be converted to JButton
        // add extras, override BeanTableDataModel
        log.debug("Sensor configValueColumn (I am {})", this);
        if (_graphicState) { // load icons, only once
            table.setDefaultEditor(JLabel.class, new ImageIconRenderer()); // editor
            table.setDefaultRenderer(JLabel.class, new ImageIconRenderer()); // item class copied from SwitchboardEditor panel
        } else {
            super.configValueColumn(table); // classic text style state indication
        }
    }

    /**
     * Visualize state in table as a graphic, customized for Sensors (2 states).
     * Renderer and Editor are identical, as the cell contents are not actually
     * edited, only used to toggle state using {@link #clickOn}.
     */
    static class ImageIconRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

        protected JLabel label;
        protected String rootPath = "resources/icons/misc/switchboard/"; // also used in display.switchboardEditor
        protected char beanTypeChar = 'S'; // for Sensor
        protected String onIconPath = rootPath + beanTypeChar + "-on-s.png";
        protected String offIconPath = rootPath + beanTypeChar + "-off-s.png";
        protected BufferedImage onImage;
        protected BufferedImage offImage;
        protected ImageIcon onIcon;
        protected ImageIcon offIcon;
        protected int iconHeight = -1;

        /**
         * {@inheritDoc}
         */
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            log.debug("Renderer Item = {}, State = {}", row, value);
            if (iconHeight < 0) { // load resources only first time, either for renderer or editor
                loadIcons();
                log.debug("icons loaded");
            }
            return updateLabel((String) value, row, table);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected,
                int row, int column) {
            log.debug("Renderer Item = {}, State = {}", row, value);
            if (iconHeight < 0) { // load resources only first time, either for renderer or editor
                loadIcons();
                log.debug("icons loaded");
            }
            return updateLabel((String) value, row, table);
        }

        public JLabel updateLabel(String value, int row, JTable table) {
            if (iconHeight > 0) { // if necessary, increase row height;
                table.setRowHeight(row, Math.max(table.getRowHeight(), iconHeight - 5)); // adjust table row height for Sensor icon
            }
            if (value.equals(Bundle.getMessage("SensorStateInactive")) && offIcon != null) {
                label = new JLabel(offIcon);
                label.setVerticalAlignment(JLabel.BOTTOM);
                log.debug("offIcon set");
            } else if (value.equals(Bundle.getMessage("SensorStateActive")) && onIcon != null) {
                label = new JLabel(onIcon);
                label.setVerticalAlignment(JLabel.BOTTOM);
                log.debug("onIcon set");
            } else if (value.equals(Bundle.getMessage("BeanStateInconsistent"))) {
                label = new JLabel("X", JLabel.CENTER); // centered text alignment
                label.setForeground(Color.red);
                log.debug("Sensor state inconsistent");
                iconHeight = 0;
            } else if (value.equals(Bundle.getMessage("BeanStateUnknown"))) {
                label = new JLabel("?", JLabel.CENTER); // centered text alignment
                log.debug("Sensor state unknown");
                iconHeight = 0;
            } else { // failed to load icon
                label = new JLabel(value, JLabel.CENTER); // centered text alignment
                log.warn("Error reading icons for SensorTable");
                iconHeight = 0;
            }
            label.setToolTipText(value);
            label.addMouseListener(new MouseAdapter() {
                @Override
                public final void mousePressed(MouseEvent evt) {
                    log.debug("Clicked on icon in row {}", row);
                    stopCellEditing();
                }
            });
            return label;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getCellEditorValue() {
            log.debug("getCellEditorValue, me = {})", this);
            return this.toString();
        }

        /**
         * Read and buffer graphics. Only called once for this table.
         *
         * @see #getTableCellEditorComponent(JTable, Object, boolean, int, int)
         */
        protected void loadIcons() {
            try {
                onImage = ImageIO.read(new File(onIconPath));
                offImage = ImageIO.read(new File(offIconPath));
            } catch (IOException ex) {
                log.error("error reading image from {} or {}", onIconPath, offIconPath, ex);
            }
            log.debug("Success reading images");
            int imageWidth = onImage.getWidth();
            int imageHeight = onImage.getHeight();
            // scale icons 50% to fit in table rows
            Image smallOnImage = onImage.getScaledInstance(imageWidth / 2, imageHeight / 2, Image.SCALE_DEFAULT);
            Image smallOffImage = offImage.getScaledInstance(imageWidth / 2, imageHeight / 2, Image.SCALE_DEFAULT);
            onIcon = new ImageIcon(smallOnImage);
            offIcon = new ImageIcon(smallOffImage);
            iconHeight = onIcon.getIconHeight();
        }

    } // end of ImageIconRenderer class

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureTable(JTable table) {
        super.configureTable(table);
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        columnModel.getColumnByModelIndex(FORGETCOL).setHeaderValue(null);
        columnModel.getColumnByModelIndex(QUERYCOL).setHeaderValue(null);
    }

    void editButton(Sensor s) {
        jmri.jmrit.beantable.beanedit.SensorEditAction beanEdit = new jmri.jmrit.beantable.beanedit.SensorEditAction();
        beanEdit.setBean(s);
        beanEdit.actionPerformed(null);
    }

    /**
     * Show or hide the Debounce columns.
     * USEGLOBALDELAY, ACTIVEDELAY, INACTIVEDELAY
     * @param show true to display, false to hide.
     * @param table the JTable to set column visibility on.
     */
    public void showDebounce(boolean show, JTable table) {
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = columnModel.getColumnByModelIndex(USEGLOBALDELAY);
        columnModel.setColumnVisible(column, show);
        column = columnModel.getColumnByModelIndex(ACTIVEDELAY);
        columnModel.setColumnVisible(column, show);
        column = columnModel.getColumnByModelIndex(INACTIVEDELAY);
        columnModel.setColumnVisible(column, show);
    }

    /**
     * Show or hide the Pullup column.
     * PULLUPCOL
     * @param show true to display, false to hide.
     * @param table the JTable to set column visibility on.
     */
    public void showPullUp(boolean show, JTable table) {
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = columnModel.getColumnByModelIndex(PULLUPCOL);
        columnModel.setColumnVisible(column, show);
    }

    /**
     * Show or hide the State - Forget and Query columns.FORGETCOL, QUERYCOL
     * @param show true to display, false to hide.
     * @param table the JTable to set column visibility on.
     */
    public void showStateForgetAndQuery(boolean show, JTable table) {
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = columnModel.getColumnByModelIndex(FORGETCOL);
        columnModel.setColumnVisible(column, show);
        column = columnModel.getColumnByModelIndex(QUERYCOL);
        columnModel.setColumnVisible(column, show);
    }

    protected String getClassName() {
        return jmri.jmrit.beantable.SensorTableAction.class.getName();
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleSensorTable");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setColumnIdentities(JTable table) {
        super.setColumnIdentities(table);
        Enumeration<TableColumn> columns;
        if (table.getColumnModel() instanceof XTableColumnModel) {
            columns = ((XTableColumnModel) table.getColumnModel()).getColumns(false);
        } else {
            columns = table.getColumnModel().getColumns();
        }
        while (columns.hasMoreElements()) {
            TableColumn column = columns.nextElement();
            switch (column.getModelIndex()) {
                case FORGETCOL:
                    column.setIdentifier("ForgetState");
                    break;
                case QUERYCOL:
                    column.setIdentifier("QueryState");
                    break;
                default:
                // use existing value
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SensorTableDataModel.class);
}
