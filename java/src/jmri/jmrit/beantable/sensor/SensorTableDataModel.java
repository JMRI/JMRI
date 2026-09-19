package jmri.jmrit.beantable.sensor;

import jmri.util.gui.GuiLafPreferencesManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
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
import jmri.managers.ProxySensorManager;
import jmri.jmrit.beantable.BeanTableDataModel;
import jmri.util.swing.XTableColumnModel;
import jmri.util.swing.JmriJOptionPane;

/**
 * Data model for a SensorTable.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2009
 * @author Egbert Broerse Copyright (C) 2017
 */
public class SensorTableDataModel extends BeanTableDataModel<Sensor> {

    public static final int INVERTCOL = BeanTableDataModel.NUMCOLUMN;
    public static final int EDITCOL = INVERTCOL + 1;
    public static final int USEGLOBALDELAY = EDITCOL + 1;
    public static final int ACTIVEDELAY = USEGLOBALDELAY + 1;
    public static final int INACTIVEDELAY = ACTIVEDELAY + 1;
    public static final int PULLUPCOL = INACTIVEDELAY + 1;
    public static final int FORGETCOL = PULLUPCOL + 1;
    public static final int QUERYCOL = FORGETCOL + 1;

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
                if ( getManager() instanceof ProxySensorManager ) {
                    return ((ProxySensorManager)getManager()).isPullResistanceConfigurable(name);
                }
                return (((SensorManager) getManager()).isPullResistanceConfigurable()); // proxymanager always false
                
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
                PullResistanceComboBox c = new PullResistanceComboBox(Sensor.PullResistance.values());
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
    private static class PullResistanceComboBox extends JComboBox<Sensor.PullResistance> {
        PullResistanceComboBox(Sensor.PullResistance[] values) { super(values); }
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
                        JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceActOutOfRange")
                            + "\n\"" + Sensor.MAX_DEBOUNCE + "\"", Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                    } else {
                        s.setSensorDebounceGoingActiveTimer(activeDeBounce);
                    }
                } catch (NumberFormatException exActiveDeBounce) {
                    JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceActError")
                        + "\n\"" + value  + "\"" + exActiveDeBounce.getLocalizedMessage(), Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                }
                break;
            case INACTIVEDELAY:
                try {
                    long inactiveDeBounce = (long) value;
                    if (inactiveDeBounce < 0 || inactiveDeBounce > Sensor.MAX_DEBOUNCE) {
                        JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceInActOutOfRange") 
                            + "\n\"" + Sensor.MAX_DEBOUNCE + "\"", Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                    } else {
                        s.setSensorDebounceGoingInActiveTimer(inactiveDeBounce);
                    }
                } catch (NumberFormatException exActiveDeBounce) {
                    JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceInActError")
                        + "\n\"" + value + "\"" + exActiveDeBounce.getLocalizedMessage(), Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
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
            case Sensor.PROPERTY_SENSOR_INVERTED:
            case Sensor.PROPERTY_GLOBAL_TIMER:
            case Sensor.PROPERTY_ACTIVE_TIMER:
            case Sensor.PROPERTY_INACTIVE_TIMER:
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
     * <p>
     * A single label is reused for every cell and the icons are loaded once
     * per JVM, so rendering a cell allocates nothing. The label ignores
     * revalidate/repaint requests and the row height is set once in
     * {@link #configureTable}: painting a cell must never schedule more
     * painting, or the table repaints itself forever.
     */
    static class ImageIconRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

        private static final String ROOT_PATH = "resources/icons/misc/switchboard/"; // also used in display.switchboardEditor
        private static final char BEAN_TYPE_CHAR = 'S'; // for Sensor
        private static final String ON_ICON_PATH = ROOT_PATH + BEAN_TYPE_CHAR + "-on-s.png";
        private static final String OFF_ICON_PATH = ROOT_PATH + BEAN_TYPE_CHAR + "-off-s.png";
        private static ImageIcon onIcon = null;
        private static ImageIcon offIcon = null;
        private static int iconHeight = -1;

        private final StateLabel label = new StateLabel();
        private final Color defaultForeground = label.getForeground();
        private final String activeText = Bundle.getMessage("SensorStateActive");
        private final String inactiveText = Bundle.getMessage("SensorStateInactive");
        private final String unknownText = Bundle.getMessage("BeanStateUnknown");
        private final String inconsistentText = Bundle.getMessage("BeanStateInconsistent");
        private int row = -1; // row of the cell last rendered or edited

        ImageIconRenderer() {
            label.setHorizontalAlignment(JLabel.CENTER);
            // must stay the only anonymous class in ImageIconRenderer, see jmri.ArchitectureTest
            label.addMouseListener(new MouseAdapter() {
                @Override
                public final void mousePressed(MouseEvent evt) {
                    log.debug("Clicked on icon in row {}", row);
                    stopCellEditing();
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            log.debug("Renderer Item = {}, State = {}", row, value);
            return updateLabel((String) value, row);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected,
                int row, int column) {
            log.debug("Editor Item = {}, State = {}", row, value);
            return updateLabel((String) value, row);
        }

        protected JLabel updateLabel(String value, int row) {
            this.row = row;
            if (iconHeight < 0) { // load resources only first time, either for renderer or editor
                loadIcons();
                log.debug("icons loaded");
            }
            label.setForeground(defaultForeground); // reset a foreground left over from an INCONSISTENT cell
            if (value.equals(inactiveText) && offIcon != null) {
                label.setIcon(offIcon);
                label.setText(null);
                label.setVerticalAlignment(JLabel.BOTTOM);
                log.debug("offIcon set");
            } else if (value.equals(activeText) && onIcon != null) {
                label.setIcon(onIcon);
                label.setText(null);
                label.setVerticalAlignment(JLabel.BOTTOM);
                log.debug("onIcon set");
            } else if (value.equals(inconsistentText)) {
                label.setIcon(null);
                label.setText("X");
                label.setForeground(Color.red);
                label.setVerticalAlignment(JLabel.CENTER);
                log.debug("Sensor state inconsistent");
            } else if (value.equals(unknownText)) {
                label.setIcon(null);
                label.setText("?");
                label.setVerticalAlignment(JLabel.CENTER);
                log.debug("Sensor state unknown");
            } else { // failed to load icon
                label.setIcon(null);
                label.setText(value);
                label.setVerticalAlignment(JLabel.CENTER);
                log.warn("Error reading icons for SensorTable");
            }
            label.setToolTipText(value);
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
         * Get the row height needed to fit the state icons, loading them if
         * required. Used by {@link #configureTable} to size the rows once,
         * outside of the render path.
         *
         * @return required row height in pixels, 0 if the icons could not be read
         */
        static int getIconRowHeight() {
            if (iconHeight < 0) {
                loadIcons();
            }
            return Math.max(iconHeight - 5, 0);
        }

        /**
         * Read and buffer graphics. Only called once per JVM, the icons are
         * shared by all instances of this renderer.
         */
        private static synchronized void loadIcons() {
            if (iconHeight >= 0) { // another instance already loaded the icons
                return;
            }
            BufferedImage onImage = null;
            BufferedImage offImage = null;
            try {
                onImage = ImageIO.read(new File(ON_ICON_PATH));
                offImage = ImageIO.read(new File(OFF_ICON_PATH));
            } catch (IOException ex) {
                log.error("error reading image from {} or {}", ON_ICON_PATH, OFF_ICON_PATH, ex);
            }
            if (onImage == null || offImage == null) { // ImageIO.read returns null for an unreadable file
                log.error("error reading image from {} or {}", ON_ICON_PATH, OFF_ICON_PATH);
                iconHeight = 0; // give up: render states as text, don't retry for every cell
                return;
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

        /**
         * Label that ignores revalidate and repaint requests.
         * <p>
         * The rendered component stays a child of the table's
         * CellRendererPane after painting, so a plain JLabel schedules a new
         * repaint of the whole table each time its icon or text changes
         * during a paint cycle. Same overrides as DefaultTableCellRenderer,
         * except firePropertyChange, which BasicLabelUI needs to keep its
         * view up to date.
         */
        private static class StateLabel extends JLabel {

            @Override
            public void revalidate() {
                // ignored, see class comment
            }

            @Override
            public void repaint() {
                // ignored, see class comment
            }

            @Override
            public void repaint(long tm) {
                // ignored, see class comment
            }

            @Override
            public void repaint(int x, int y, int width, int height) {
                // ignored, see class comment
            }

            @Override
            public void repaint(long tm, int x, int y, int width, int height) {
                // ignored, see class comment
            }

            @Override
            public void repaint(Rectangle r) {
                // ignored, see class comment
            }
        }

    } // end of ImageIconRenderer class

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureTable(JTable table) {
        super.configureTable(table);
        if (_graphicState) {
            // make the rows tall enough for the state icons, once and outside of the
            // renderer; must come after super.configureTable, which sets the row
            // height for the buttons
            table.setRowHeight(Math.max(table.getRowHeight(), ImageIconRenderer.getIconRowHeight()));
        }
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SensorTableDataModel.class);

}
