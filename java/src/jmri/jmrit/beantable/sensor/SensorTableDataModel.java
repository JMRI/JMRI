package jmri.jmrit.beantable.sensor;

import apps.gui.GuiLafPreferencesManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
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

    static public final int INVERTCOL = NUMCOLUMN;
    static public final int EDITCOL = INVERTCOL + 1;
    static public final int USEGLOBALDELAY = EDITCOL + 1;
    static public final int ACTIVEDELAY = USEGLOBALDELAY + 1;
    static public final int INACTIVEDELAY = ACTIVEDELAY + 1;
    static public final int PULLUPCOL = INACTIVEDELAY + 1;
    static public final int FORGETCOL = PULLUPCOL + 1;
    static public final int QUERYCOL = FORGETCOL + 1;

    SensorManager senManager = null;
    // for icon state col
    protected boolean _graphicState = false; // updated from prefs

    public SensorTableDataModel() {
        super();
    }

    public SensorTableDataModel(SensorManager manager) {
        super();
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
        // load graphic state column display preference
        _graphicState = InstanceManager.getDefault(GuiLafPreferencesManager.class).isGraphicTableState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue(String name) {
        Sensor sen = senManager.getBySystemName(name);
        if (sen == null) {
            return "Failed to get sensor " + name;
        }
        int val = sen.getKnownState();
        switch (val) {
            case Sensor.ACTIVE:
                return Bundle.getMessage("SensorStateActive");
            case Sensor.INACTIVE:
                return Bundle.getMessage("SensorStateInactive");
            case Sensor.UNKNOWN:
                return Bundle.getMessage("BeanStateUnknown");
            case Sensor.INCONSISTENT:
                return Bundle.getMessage("BeanStateInconsistent");
            default:
                return "Unexpected value: " + val;
        }
    }

    protected void setManager(SensorManager manager) {
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
    protected Sensor getBySystemName(String name) {
        return senManager.getBySystemName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Sensor getByUserName(String name) {
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
            int state = t.getKnownState();
            if (state == Sensor.INACTIVE) {
                t.setKnownState(Sensor.ACTIVE);
            } else {
                t.setKnownState(Sensor.INACTIVE);
            }
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
                return Boolean.class;
            case EDITCOL:
                return JButton.class;
            case USEGLOBALDELAY:
                return Boolean.class;
            case ACTIVEDELAY:
                return String.class;
            case INACTIVEDELAY:
                return String.class;
            case PULLUPCOL:
                return JComboBox.class;
            case FORGETCOL:
                return JButton.class;
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
                return new JTextField(8).getPreferredSize().width;
            case EDITCOL:
                return new JTextField(7).getPreferredSize().width;
            case FORGETCOL:
                return new JButton(Bundle.getMessage("StateForgetButton"))
                        .getPreferredSize().width;
            case QUERYCOL:
                return new JButton(Bundle.getMessage("StateQueryButton"))
                        .getPreferredSize().width;
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
        Sensor sen = senManager.getBySystemName(name);
        if (sen == null) {
            return false;
        }
        if (col == INVERTCOL) {
            return sen.canInvert();
        }
        if (col == EDITCOL) {
            return true;
        }
        if (col == USEGLOBALDELAY) {
            return true;
        }
        //Need to do something here to make it disable
        if (col == ACTIVEDELAY || col == INACTIVEDELAY) {
            return !sen.getUseDefaultTimerSettings();
        }
        if (col == PULLUPCOL) {
            return (senManager.isPullResistanceConfigurable());
        }
        if (col == FORGETCOL) {
            return true;
        }
        if (col == QUERYCOL) {
            return true;
        }
        return super.isCellEditable(row, col);
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
                c.addActionListener((ActionEvent e) -> {
                    comboBoxAction(e);
                });
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
                s.setInverted(((Boolean) value));
                break;
            case USEGLOBALDELAY:
                s.setUseDefaultTimerSettings(((Boolean) value));
                break;
            case ACTIVEDELAY:
                try {
                    Long activeDeBounce = Long.parseLong((String) value);
                    if (activeDeBounce < 0 || activeDeBounce > Sensor.MAX_DEBOUNCE) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceActOutOfRange") + "\n\"" + (String) value + "\"", "Input Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        s.setSensorDebounceGoingActiveTimer(Long.parseLong((String) value));
                    }
                } catch (NumberFormatException exActiveDeBounce) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceActError") + "\n\"" + Long.toString(Sensor.MAX_DEBOUNCE) + "\"", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
                break;
            case INACTIVEDELAY:
                try {
                    Long inactiveDeBounce = Long.parseLong((String) value);
                    if (inactiveDeBounce < 0 || inactiveDeBounce > Sensor.MAX_DEBOUNCE) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceInActOutOfRange") + "\n\"" + Long.toString(Sensor.MAX_DEBOUNCE) + "\"", "Input Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        s.setSensorDebounceGoingInActiveTimer(Long.parseLong((String) value));
                    }
                } catch (NumberFormatException exActiveDeBounce) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("SensorDebounceInActError") + "\n\"" + (String) value + "\"", "Input Error", JOptionPane.ERROR_MESSAGE);
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
    protected String getBeanType() {
        return Bundle.getMessage("BeanNameSensor");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        if ((e.getPropertyName().contains("inverted")) || (e.getPropertyName().contains("GlobalTimer"))
                || (e.getPropertyName().contains("ActiveTimer")) || (e.getPropertyName().contains("InActiveTimer"))) {
            return true;
        } else {
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
    class ImageIconRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

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
            return updateLabel((String) value, row);
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
            return updateLabel((String) value, row);
        }

        public JLabel updateLabel(String value, int row) {
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
        this.table = table;
        showDebounce(false);
        showPullUp(false);
        this.table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        this.table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        showStateForgetAndQuery(false);
        super.configureTable(table);
    }

    protected JTable table;

    void editButton(Sensor s) {
        jmri.jmrit.beantable.beanedit.SensorEditAction beanEdit = new jmri.jmrit.beantable.beanedit.SensorEditAction();
        beanEdit.setBean(s);
        beanEdit.actionPerformed(null);
    }

    public void showDebounce(boolean show) {
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = columnModel.getColumnByModelIndex(USEGLOBALDELAY);
        columnModel.setColumnVisible(column, show);
        column = columnModel.getColumnByModelIndex(ACTIVEDELAY);
        columnModel.setColumnVisible(column, show);
        column = columnModel.getColumnByModelIndex(INACTIVEDELAY);
        columnModel.setColumnVisible(column, show);
    }

    public void showPullUp(boolean show) {
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = columnModel.getColumnByModelIndex(PULLUPCOL);
        columnModel.setColumnVisible(column, show);
    }

    public void showStateForgetAndQuery(boolean show) {
        XTableColumnModel columnModel = (XTableColumnModel) table.getColumnModel();
        TableColumn column = columnModel.getColumnByModelIndex(FORGETCOL);
        columnModel.setColumnVisible(column, show);
        column = columnModel.getColumnByModelIndex(QUERYCOL);
        columnModel.setColumnVisible(column, show);
    }

    protected String getClassName() {
        return jmri.jmrit.beantable.SensorTableAction.class.getName();
    }

//    public static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    public String getClassDescription() {
        return Bundle.getMessage("TitleSensorTable");
    }

    public void comboBoxAction(ActionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("Combobox change");
        }
        if (table != null && table.getCellEditor() != null) {
            table.getCellEditor().stopCellEditing();
        }
    }

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
