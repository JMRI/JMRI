package jmri.jmrit.beantable.sensor;

import apps.gui.GuiLafPreferencesManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
public class SensorTableDataModel extends BeanTableDataModel {

    static public final int INVERTCOL = NUMCOLUMN;
    static public final int EDITCOL = INVERTCOL + 1;
    static public final int USEGLOBALDELAY = EDITCOL + 1;
    static public final int ACTIVEDELAY = USEGLOBALDELAY + 1;
    static public final int INACTIVEDELAY = ACTIVEDELAY + 1;
    static public final int PULLUPCOL = INACTIVEDELAY + 1;

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

    @Override
    protected Manager getManager() {
        if (senManager == null) {
            senManager = InstanceManager.sensorManagerInstance();
        }
        return senManager;
    }

    @Override
    protected NamedBean getBySystemName(String name) {
        return senManager.getBySystemName(name);
    }

    @Override
    protected NamedBean getByUserName(String name) {
        return InstanceManager.getDefault(SensorManager.class).getByUserName(name);
    }

    @Override
    protected String getMasterClassName() {
        return getClassName();
    }

    @Override
    protected void clickOn(NamedBean t) {
        try {
            int state = ((Sensor) t).getKnownState();
            if (state == Sensor.INACTIVE) {
                ((Sensor) t).setKnownState(Sensor.ACTIVE);
            } else {
                ((Sensor) t).setKnownState(Sensor.INACTIVE);
            }
        } catch (JmriException e) {
            log.warn("Error setting state: " + e);
        }
    }

    @Override
    public int getColumnCount() {
        return PULLUPCOL + 1;
    }

    @Override
    public String getColumnName(int col) {
        switch(col) {
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
           default:
              return super.getColumnName(col);
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch(col) {
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
           case VALUECOL:
               if (_graphicState) {
                    return JLabel.class; // use an image to show sensor state
               }
           default:
              return super.getColumnClass(col);
        }
    }

    @Override
    public int getPreferredWidth(int col) {
        if (col == INVERTCOL) {
            return new JTextField(4).getPreferredSize().width;
        }
        if (col == USEGLOBALDELAY || col == ACTIVEDELAY || col == INACTIVEDELAY) {
            return new JTextField(8).getPreferredSize().width;
        }
        if (col == EDITCOL) {
            return new JTextField(7).getPreferredSize().width;
        } else {
            return super.getPreferredWidth(col);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        String name = sysNameList.get(row);
        Sensor sen = senManager.getBySystemName(name);
        if (sen == null) return false;
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
            if (sen.getUseDefaultTimerSettings()) {
                return false;
            } else {
                return true;
            }
        }
        if(col == PULLUPCOL){
            return(senManager.isPullResistanceConfigurable());
        }
        return super.isCellEditable(row, col);
    }

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
        if (col == INVERTCOL) {
            boolean val = s.getInverted();
            return Boolean.valueOf(val);
        } else if (col == USEGLOBALDELAY) {
            boolean val = s.getUseDefaultTimerSettings();
            return Boolean.valueOf(val);
        } else if (col == ACTIVEDELAY) {
            return s.getSensorDebounceGoingActiveTimer();
        } else if (col == INACTIVEDELAY) {
            return s.getSensorDebounceGoingInActiveTimer();
        } else if (col == EDITCOL) {
            return Bundle.getMessage("ButtonEdit");
        } else if (col == PULLUPCOL) {
            JComboBox<Sensor.PullResistance> c = new JComboBox<Sensor.PullResistance>(Sensor.PullResistance.values());
            c.setSelectedItem(s.getPullResistance());
            c.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                   comboBoxAction(e);
                }
            });
            return c;
        } else {
            return super.getValueAt(row, col);
        }
    }

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
        if (col == INVERTCOL) {
            boolean b = ((Boolean) value).booleanValue();
            s.setInverted(b);
        } else if (col == USEGLOBALDELAY) {
            boolean b = ((Boolean) value).booleanValue();
            s.setUseDefaultTimerSettings(b);
        } else if (col == ACTIVEDELAY) {
            String val = (String) value;
            long goingActive = Long.valueOf(val);
            s.setSensorDebounceGoingActiveTimer(goingActive);
        } else if (col == INACTIVEDELAY) {
            String val = (String) value;
            long goingInActive = Long.valueOf(val);
            s.setSensorDebounceGoingInActiveTimer(goingInActive);
        } else if (col == EDITCOL) {
            class WindowMaker implements Runnable {

                Sensor s;

                WindowMaker(Sensor s) {
                    this.s = s;
                }

                @Override
                public void run() {
                    editButton(s);
                }
            }
            WindowMaker w = new WindowMaker(s);
            javax.swing.SwingUtilities.invokeLater(w);
        } else if (col == PULLUPCOL) {
            JComboBox<Sensor.PullResistance> cb = (JComboBox<Sensor.PullResistance>) value;
            s.setPullResistance((Sensor.PullResistance)cb.getSelectedItem());
        } else if (col == VALUECOL && _graphicState) { // respond to clicking on ImageIconRenderer CellEditor
            clickOn(s);
            fireTableRowsUpdated(row, row);
        } else {
            super.setValueAt(value, row, col);
        }
    }

    @Override
    protected String getBeanType() {
        return Bundle.getMessage("BeanNameSensor");
    }

    @Override
    protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
        if ((e.getPropertyName().indexOf("inverted") >= 0) || (e.getPropertyName().indexOf("GlobalTimer") >= 0)
                || (e.getPropertyName().indexOf("ActiveTimer") >= 0) || (e.getPropertyName().indexOf("InActiveTimer") >= 0)) {
            return true;
        } else {
            return super.matchPropertyName(e);
        }
    }

    /**
     * Customize the sensor table Value (State) column to show an appropriate graphic for the sensor state
     * if _graphicState = true, or (default) just show the localized state text
     * when the TableDataModel is being called from ListedTableAction.
     *
     * @param table a JTable of Sensors
     */
    @Override
    protected void configValueColumn(JTable table) {
        // have the value column hold a JPanel (icon)
        //setColumnToHoldButton(table, VALUECOL, new JLabel("1234")); // for small round icon, but cannot be converted to JButton
        // add extras, override BeanTableDataModel
        log.debug("Sensor configValueColumn (I am {})", super.toString());
        if (_graphicState) { // load icons, only once
            table.setDefaultEditor(JLabel.class, new ImageIconRenderer()); // editor
            table.setDefaultRenderer(JLabel.class, new ImageIconRenderer()); // item class copied from SwitchboardEditor panel
        } else {
            super.configValueColumn(table); // classic text style state indication
        }
    }

    /**
     * Visualize state in table as a graphic, customized for Sensors (2 states).
     * Renderer and Editor are identical, as the cell contents are not actually edited,
     * only used to toggle state using {@link #clickOn(NamedBean)}.
     * @see jmri.jmrit.beantable.BlockTableAction#createModel()
     * @see jmri.jmrit.beantable.LightTableAction#createModel()
     * @see jmri.jmrit.beantable.TurnoutTableAction#createModel()
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
            label.addMouseListener (new MouseAdapter ()
            {
                @Override
                public final void mousePressed (MouseEvent evt)
                {
                    log.debug("Clicked on icon in row {}", row);
                    stopCellEditing();
                }
            });
            return label;
        }

        @Override
        public Object getCellEditorValue() {
            log.debug("getCellEditorValue, me = {})", this.toString());
            return this.toString();
        }

        /**
         * Read and buffer graphics. Only called once for this table.
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

    @Override
    public void configureTable(JTable table) {
        this.table = table;
        showDebounce(false);
        showPullUp(false);
        this.table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        this.table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
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

    private final static Logger log = LoggerFactory.getLogger(SensorTableDataModel.class);
}
