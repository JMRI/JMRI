package jmri.jmrit.beantable;

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
import java.text.DecimalFormat;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Reporter;
import jmri.Sensor;
import jmri.implementation.SignalSpeedMap;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a BlockTable GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2008
 * @author Egbert Broerse Copyright (C) 2017
 */
public class BlockTableAction extends AbstractTableAction<Block> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName the Action title
     */
    public BlockTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary Block manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.BlockManager.class) == null) {
            setEnabled(false);
        }
        inchBox.setSelected(true);
        centimeterBox.setSelected(false);

        if (jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).getSimplePreferenceState(getClassName() + ":LengthUnitMetric")) {
            inchBox.setSelected(false);
            centimeterBox.setSelected(true);
        }

        defaultBlockSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + jmri.InstanceManager.getDefault(jmri.BlockManager.class).getDefaultSpeed()); // first entry in drop down list
        speedList.add(defaultBlockSpeedText);
        java.util.Vector<String> _speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames();
        for (int i = 0; i < _speedMap.size(); i++) {
            if (!speedList.contains(_speedMap.get(i))) {
                speedList.add(_speedMap.get(i));
            }
        }
        updateSensorList();
        updateReporterList();
    }

    public BlockTableAction() {
        this(Bundle.getMessage("TitleBlockTable"));
    }

    private String noneText = Bundle.getMessage("BlockNone");
    private String gradualText = Bundle.getMessage("BlockGradual");
    private String tightText = Bundle.getMessage("BlockTight");
    private String severeText = Bundle.getMessage("BlockSevere");
    private String[] curveOptions = {noneText, gradualText, tightText, severeText};
    private java.util.Vector<String> speedList = new java.util.Vector<String>();
    private String[] sensorList;
    private String[] reporterList;
    private DecimalFormat twoDigit = new DecimalFormat("0.00");
    String defaultBlockSpeedText;
    // for icon state col
    protected boolean _graphicState = false; // updated from prefs

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Block objects.
     */
    @Override
    protected void createModel() {
        // load graphic state column display preference
        _graphicState = InstanceManager.getDefault(GuiLafPreferencesManager.class).isGraphicTableState();

        m = new BeanTableDataModel<Block>() {
            static public final int EDITCOL = NUMCOLUMN;
            static public final int DIRECTIONCOL = EDITCOL + 1;
            static public final int LENGTHCOL = DIRECTIONCOL + 1;
            static public final int CURVECOL = LENGTHCOL + 1;
            static public final int STATECOL = CURVECOL + 1;
            static public final int SENSORCOL = STATECOL + 1;
            static public final int REPORTERCOL = SENSORCOL + 1;
            static public final int CURRENTREPCOL = REPORTERCOL + 1;
            static public final int PERMISCOL = CURRENTREPCOL + 1;
            static public final int SPEEDCOL = PERMISCOL + 1;

            @Override
            public String getValue(String name) {
                if (name == null) {
                    log.warn("requested getValue(null)");
                    return "(no name)";
                }
                Block b = InstanceManager.getDefault(jmri.BlockManager.class).getBySystemName(name);
                if (b == null) {
                    log.debug("requested getValue(\"" + name + "\"), Block doesn't exist");
                    return "(no Block)";
                }
                Object m = b.getValue();
                if (m != null) {
                    return m.toString();
                } else {
                    return "";
                }
            }

            @Override
            public Manager<Block> getManager() {
                return InstanceManager.getDefault(jmri.BlockManager.class);
            }

            @Override
            public Block getBySystemName(String name) {
                return InstanceManager.getDefault(jmri.BlockManager.class).getBySystemName(name);
            }

            @Override
            public Block getByUserName(String name) {
                return InstanceManager.getDefault(jmri.BlockManager.class).getByUserName(name);
            }

            @Override
            protected String getMasterClassName() {
                return getClassName();
            }

            @Override
            public void clickOn(Block t) {
                // don't do anything on click; not used in this class, because
                // we override setValueAt
            }

            //Permissive and speed columns are temp disabled
            @Override
            public int getColumnCount() {
                return SPEEDCOL + 1;
            }

            @Override
            public Object getValueAt(int row, int col) {
                // some error checking
                if (row >= sysNameList.size()) {
                    log.debug("requested getValueAt(\"" + row + "\"), row outside of range");
                    return "Error table size";
                }
                Block b = getBySystemName(sysNameList.get(row));
                if (b == null) {
                    log.debug("requested getValueAt(\"" + row + "\"), Block doesn't exist");
                    return "(no Block)";
                }
                if (col == DIRECTIONCOL) {
                    return jmri.Path.decodeDirection(b.getDirection());
                } else if (col == CURVECOL) {
                    JComboBox<String> c = new JComboBox<>(curveOptions);
                    if (b.getCurvature() == Block.NONE) {
                        c.setSelectedItem(0);
                    } else if (b.getCurvature() == Block.GRADUAL) {
                        c.setSelectedItem(gradualText);
                    } else if (b.getCurvature() == Block.TIGHT) {
                        c.setSelectedItem(tightText);
                    } else if (b.getCurvature() == Block.SEVERE) {
                        c.setSelectedItem(severeText);
                    }
                    return c;
                } else if (col == LENGTHCOL) {
                    double len = 0.0;
                    if (inchBox.isSelected()) {
                        len = b.getLengthIn();
                    } else {
                        len = b.getLengthCm();
                    }
                    return (twoDigit.format(len));
                } else if (col == PERMISCOL) {
                    boolean val = b.getPermissiveWorking();
                    return Boolean.valueOf(val);
                } else if (col == SPEEDCOL) {
                    String speed = b.getBlockSpeed();
                    if (!speedList.contains(speed)) {
                        speedList.add(speed);
                    }
                    JComboBox<String> c = new JComboBox<>(speedList);
                    c.setEditable(true);
                    c.setSelectedItem(speed);
                    return c;
                } else if (col == STATECOL) {
                    switch (b.getState()) {
                        case (Block.OCCUPIED):
                            return Bundle.getMessage("BlockOccupied");
                        case (Block.UNOCCUPIED):
                            return Bundle.getMessage("BlockUnOccupied");
                        case (Block.UNKNOWN):
                            return Bundle.getMessage("BlockUnknown");
                        default:
                            return Bundle.getMessage("BlockInconsistent");
                    }
                } else if (col == SENSORCOL) {
                    Sensor sensor = b.getSensor();
                    JComboBox<String> c = new JComboBox<>(sensorList);
                    String name = "";
                    if (sensor != null) {
                        name = sensor.getDisplayName();
                    }
                    c.setSelectedItem(name);
                    return c;
                } else if (col == REPORTERCOL) {
                    Reporter reporter = b.getReporter();
                    JComboBox<String> rs = new JComboBox<>(reporterList);
                    String name = "";
                    if (reporter != null) {
                        name = reporter.getDisplayName();
                    }
                    rs.setSelectedItem(name);
                    return rs;
                } else if (col == CURRENTREPCOL) {
                    return Boolean.valueOf(b.isReportingCurrent());
                } else if (col == EDITCOL) {  //
                    return Bundle.getMessage("ButtonEdit");
                } else {
                    return super.getValueAt(row, col);
                }
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                // no setting of block state from table
                Block b = getBySystemName(sysNameList.get(row));
                if (col == VALUECOL) {
                    b.setValue(value);
                    fireTableRowsUpdated(row, row);
                } else if (col == LENGTHCOL) {
                    float len = 0.0f;
                    try {
                        len = jmri.util.IntlUtilities.floatValue(value.toString());
                    } catch (java.text.ParseException ex2) {
                        log.error("Error parsing length value of \"{}\"", value);
                    }
                    if (inchBox.isSelected()) {
                        b.setLength(len * 25.4f);
                    } else {
                        b.setLength(len * 10.0f);
                    }
                    fireTableRowsUpdated(row, row);
                } else if (col == CURVECOL) {

                    @SuppressWarnings("unchecked")
                    String cName = (String) ((JComboBox<String>) value).getSelectedItem();
                    if (cName.equals(noneText)) {
                        b.setCurvature(Block.NONE);
                    } else if (cName.equals(gradualText)) {
                        b.setCurvature(Block.GRADUAL);
                    } else if (cName.equals(tightText)) {
                        b.setCurvature(Block.TIGHT);
                    } else if (cName.equals(severeText)) {
                        b.setCurvature(Block.SEVERE);
                    }
                    fireTableRowsUpdated(row, row);
                } else if (col == PERMISCOL) {
                    boolean boo = ((Boolean) value).booleanValue();
                    b.setPermissiveWorking(boo);
                    fireTableRowsUpdated(row, row);
                } else if (col == SPEEDCOL) {
                    @SuppressWarnings("unchecked")
                    String speed = (String) ((JComboBox<String>) value).getSelectedItem();
                    try {
                        b.setBlockSpeed(speed);
                    } catch (jmri.JmriException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                        return;
                    }
                    if (!speedList.contains(speed) && !speed.contains("Global")) { // NOI18N
                        speedList.add(speed);
                    }
                    fireTableRowsUpdated(row, row);
                } else if (col == REPORTERCOL) {
                    @SuppressWarnings("unchecked")
                    String strReporter = (String) ((JComboBox<String>) value).getSelectedItem();
                    Reporter r = jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getReporter(strReporter);
                    b.setReporter(r);
                    fireTableRowsUpdated(row, row);
                } else if (col == SENSORCOL) {
                    @SuppressWarnings("unchecked")
                    String strSensor = (String) ((JComboBox<String>) value).getSelectedItem();
                    b.setSensor(strSensor);
                    fireTableRowsUpdated(row, row);
                } else if (col == CURRENTREPCOL) {
                    boolean boo = ((Boolean) value);
                    b.setReportingCurrent(boo);
                    fireTableRowsUpdated(row, row);
                } else if (col == EDITCOL) {
                    class WindowMaker implements Runnable {

                        Block b;

                        WindowMaker(Block b) {
                            this.b = b;
                        }

                        @Override
                        public void run() {
                            editButton(b); // don't really want to stop Route w/o user action
                        }
                    }
                    WindowMaker t = new WindowMaker(b);
                    javax.swing.SwingUtilities.invokeLater(t);
                    //editButton(b);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            @Override
            public String getColumnName(int col) {
                if (col == DIRECTIONCOL) {
                    return Bundle.getMessage("BlockDirection");
                }
                if (col == VALUECOL) {
                    return Bundle.getMessage("BlockValue");
                }
                if (col == CURVECOL) {
                    return Bundle.getMessage("BlockCurveColName");
                }
                if (col == LENGTHCOL) {
                    return Bundle.getMessage("BlockLengthColName");
                }
                if (col == PERMISCOL) {
                    return Bundle.getMessage("BlockPermColName");
                }
                if (col == SPEEDCOL) {
                    return Bundle.getMessage("BlockSpeedColName");
                }
                if (col == STATECOL) {
                    return Bundle.getMessage("BlockState");
                }
                if (col == REPORTERCOL) {
                    return Bundle.getMessage("BlockReporter");
                }
                if (col == SENSORCOL) {
                    return Bundle.getMessage("BlockSensor");
                }
                if (col == CURRENTREPCOL) {
                    return Bundle.getMessage("BlockReporterCurrent");
                }
                if (col == EDITCOL) {
                    return Bundle.getMessage("ButtonEdit");
                }
                return super.getColumnName(col);
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == DIRECTIONCOL) {
                    return String.class;
                }
                if (col == VALUECOL) {
                    return String.class;  // not a button
                }
                if (col == CURVECOL) {
                    return JComboBox.class;
                }
                if (col == LENGTHCOL) {
                    return String.class;
                }
                if (col == PERMISCOL) {
                    return Boolean.class;
                }
                if (col == SPEEDCOL) {
                    return JComboBox.class;
                }
                if (col == STATECOL) {
                    if (_graphicState) {
                        return JLabel.class; // use an image to show block state
                    } else {
                        return String.class;
                    }
                }
                if (col == REPORTERCOL) {
                    return JComboBox.class;
                }
                if (col == SENSORCOL) {
                    return JComboBox.class;
                }
                if (col == CURRENTREPCOL) {
                    return Boolean.class;
                }
                if (col == EDITCOL) {
                    return JButton.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

            @Override
            public int getPreferredWidth(int col) {
                if (col == DIRECTIONCOL) {
                    return new JTextField(7).getPreferredSize().width;
                }
                if (col == CURVECOL) {
                    return new JTextField(8).getPreferredSize().width;
                }
                if (col == LENGTHCOL) {
                    return new JTextField(7).getPreferredSize().width;
                }
                if (col == PERMISCOL) {
                    return new JTextField(7).getPreferredSize().width;
                }
                if (col == SPEEDCOL) {
                    return new JTextField(7).getPreferredSize().width;
                }
                if (col == STATECOL) {
                    return new JTextField(8).getPreferredSize().width;
                }
                if (col == REPORTERCOL) {
                    return new JTextField(8).getPreferredSize().width;
                }
                if (col == SENSORCOL) {
                    return new JTextField(8).getPreferredSize().width;
                }
                if (col == CURRENTREPCOL) {
                    return new JTextField(7).getPreferredSize().width;
                }
                if (col == EDITCOL) {
                    return new JTextField(7).getPreferredSize().width;
                } else {
                    return super.getPreferredWidth(col);
                }
            }

            @Override
            public void configValueColumn(JTable table) {
                // value column isn't button, so config is null
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == CURVECOL) {
                    return true;
                } else if (col == LENGTHCOL) {
                    return true;
                } else if (col == PERMISCOL) {
                    return true;
                } else if (col == SPEEDCOL) {
                    return true;
                } else if (col == STATECOL) {
                    return false;
                } else if (col == REPORTERCOL) {
                    return true;
                } else if (col == SENSORCOL) {
                    return true;
                } else if (col == CURRENTREPCOL) {
                    return true;
                } else if (col == EDITCOL) {
                    return true;
                } else {
                    return super.isCellEditable(row, col);
                }
            }

            @Override
            public void configureTable(JTable table) {
                table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
                table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
                jmri.InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
                jmri.InstanceManager.getDefault(jmri.ReporterManager.class).addPropertyChangeListener(this);
                configStateColumn(table);
                super.configureTable(table);
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
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getSource() instanceof jmri.SensorManager) {
                    if (e.getPropertyName().equals("length") || e.getPropertyName().equals("DisplayListName")) {
                        updateSensorList();
                    }
                }
                if (e.getSource() instanceof jmri.ReporterManager) {
                    if (e.getPropertyName().equals("length") || e.getPropertyName().equals("DisplayListName")) {
                        updateReporterList();
                    }
                }
                if (e.getPropertyName().equals("DefaultBlockSpeedChange")) {
                    updateSpeedList();
                } else {
                    super.propertyChange(e);
                }
            }

            @Override
            protected String getBeanType() {
                return Bundle.getMessage("BeanNameBlock");
            }

            @Override
            synchronized public void dispose() {
                super.dispose();
                jmri.InstanceManager.sensorManagerInstance().removePropertyChangeListener(this);
                jmri.InstanceManager.getDefault(jmri.ReporterManager.class).removePropertyChangeListener(this);
            }

            /**
             * Customize the block table State column to show an appropriate
             * graphic for the block occupancy state if _graphicState = true, or
             * (default) just show the localized state text when the
             * TableDataModel is being called from ListedTableAction.
             *
             * @param table a JTable of Blocks
             */
            protected void configStateColumn(JTable table) {
                // have the state column hold a JPanel (icon)
                //setColumnToHoldButton(table, VALUECOL, new JLabel("1234")); // for small round icon, but cannot be converted to JButton
                // add extras, override BeanTableDataModel
                log.debug("Block configStateColumn (I am {})", super.toString());
                if (_graphicState) { // load icons, only once
                    //table.setDefaultEditor(JLabel.class, new ImageIconRenderer()); // there's no editor for state column in BlockTable
                    table.setDefaultRenderer(JLabel.class, new ImageIconRenderer()); // item class copied from SwitchboardEditor panel
                    // else, classic text style state indication, do nothing extra
                }
            }

            /**
             * Visualize state in table as a graphic, customized for Blocks (2
             * states). Renderer and Editor are identical, as the cell contents
             * are not actually edited.
             *
             * @see jmri.jmrit.beantable.TurnoutTableAction#createModel()
             * @see jmri.jmrit.beantable.LightTableAction#createModel()
             */
            class ImageIconRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

                protected JLabel label;
                protected String rootPath = "resources/icons/misc/switchboard/"; // also used in display.switchboardEditor
                protected char beanTypeChar = 'S'; // reuse Sensor icon for block state
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
//                     if (iconHeight > 0) { // if necessary, increase row height;
                    //table.setRowHeight(row, Math.max(table.getRowHeight(), iconHeight - 5)); // TODO adjust table row height for Block icons
//                     }
                    if (value.equals(Bundle.getMessage("BlockUnOccupied")) && offIcon != null) {
                        label = new JLabel(offIcon);
                        label.setVerticalAlignment(JLabel.BOTTOM);
                        log.debug("offIcon set");
                    } else if (value.equals(Bundle.getMessage("BlockOccupied")) && onIcon != null) {
                        label = new JLabel(onIcon);
                        label.setVerticalAlignment(JLabel.BOTTOM);
                        log.debug("onIcon set");
                    } else if (value.equals(Bundle.getMessage("BlockInconsistent"))) {
                        label = new JLabel("X", JLabel.CENTER); // centered text alignment
                        label.setForeground(Color.red);
                        log.debug("Block state inconsistent");
                        iconHeight = 0;
                    } else if (value.equals(Bundle.getMessage("BlockUnknown"))) {
                        label = new JLabel("?", JLabel.CENTER); // centered text alignment
                        log.debug("Block state in transition");
                        iconHeight = 0;
                    } else { // failed to load icon
                        label = new JLabel(value, JLabel.CENTER); // centered text alignment
                        log.warn("Error reading icons for BlockTable");
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

                @Override
                public Object getCellEditorValue() {
                    log.debug("getCellEditorValue, me = {})", this.toString());
                    return this.toString();
                }

                /**
                 * Read and buffer graphics. Only called once for this table.
                 *
                 * @see #getTableCellEditorComponent(JTable, Object, boolean,
                 * int, int)
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

        }; // end of custom data model
    }

    void editButton(Block b) {
        jmri.jmrit.beantable.beanedit.BlockEditAction beanEdit = new jmri.jmrit.beantable.beanedit.BlockEditAction();
        beanEdit.setBean(b);
        beanEdit.actionPerformed(null);
    }

    private void updateSensorList() {
        Set<Sensor> nameSet = jmri.InstanceManager.sensorManagerInstance().getNamedBeanSet();
        String[] displayList = new String[nameSet.size()];
        int i = 0;
        for (Sensor nBean : nameSet) {
            if (nBean != null) {
                displayList[i++] = nBean.getDisplayName();
            }
        }
        java.util.Arrays.sort(displayList);
        sensorList = new String[displayList.length + 1];
        sensorList[0] = "";
        i = 1;
        for (String name : displayList) {
            sensorList[i] = name;
            i++;
        }
    }

    private void updateReporterList() {
        Set<Reporter> nameSet = jmri.InstanceManager.getDefault(jmri.ReporterManager.class).getNamedBeanSet();
        String[] displayList = new String[nameSet.size()];
        int i = 0;
        for (Reporter nBean : nameSet) {
            if (nBean != null) {
                displayList[i++] = nBean.getDisplayName();
            }
        }
        java.util.Arrays.sort(displayList);
        reporterList = new String[displayList.length + 1];
        reporterList[0] = "";
        i = 1;
        for (String name : displayList) {
            reporterList[i] = name;
            i++;
        }
    }

    private void updateSpeedList() {
        speedList.remove(defaultBlockSpeedText);
        defaultBlockSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + jmri.InstanceManager.getDefault(jmri.BlockManager.class).getDefaultSpeed());
        speedList.add(0, defaultBlockSpeedText);
        m.fireTableDataChanged();
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleBlockTable"));
    }

    JRadioButton inchBox = new JRadioButton(Bundle.getMessage("LengthInches"));
    JRadioButton centimeterBox = new JRadioButton(Bundle.getMessage("LengthCentimeters"));

    /**
     * Add the radiobuttons (only 1 may be selected) TODO change names from -box
     * to radio- add radio buttons to a ButtongGroup delete extra
     * inchBoxChanged() and centimeterBoxChanged() methods
     */
    @Override
    public void addToFrame(BeanTableFrame f) {
        //final BeanTableFrame finalF = f; // needed for anonymous ActionListener class
        f.addToBottomBox(inchBox, this.getClass().getName());
        inchBox.setToolTipText(Bundle.getMessage("InchBoxToolTip"));
        inchBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inchBoxChanged();
            }
        });
        f.addToBottomBox(centimeterBox, this.getClass().getName());
        centimeterBox.setToolTipText(Bundle.getMessage("CentimeterBoxToolTip"));
        centimeterBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                centimeterBoxChanged();
            }
        });
    }

    /**
     * Insert 2 table specific menus. Account for the Window and Help menus,
     * which are already added to the menu bar as part of the creation of the
     * JFrame, by adding the menus 2 places earlier unless the table is part of
     * the ListedTableFrame, that adds the Help menu later on.
     *
     * @param f the JFrame of this table
     */
    @Override
    public void setMenuBar(BeanTableFrame f) {
        final jmri.util.JmriJFrame finalF = f; // needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        int pos = menuBar.getMenuCount() - 1; // count the number of menus to insert the TableMenus before 'Window' and 'Help'
        int offset = 1;
        log.debug("setMenuBar number of menu items = " + pos);
        for (int i = 0; i <= pos; i++) {
            if (menuBar.getComponent(i) instanceof JMenu) {
                if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
                    offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
                }
            }
        }

        JMenu pathMenu = new JMenu(Bundle.getMessage("MenuPaths"));
        JMenuItem item = new JMenuItem(Bundle.getMessage("MenuItemDeletePaths"));
        pathMenu.add(item);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePaths(finalF);
            }
        });
        menuBar.add(pathMenu, pos + offset);

        JMenu speedMenu = new JMenu(Bundle.getMessage("SpeedsMenu"));
        item = new JMenuItem(Bundle.getMessage("SpeedsMenuItemDefaults"));
        speedMenu.add(item);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDefaultSpeeds(finalF);
            }
        });
        menuBar.add(speedMenu, pos + offset + 1); // put it to the right of the Paths menu
    }

    protected void setDefaultSpeeds(JFrame _who) {
        JComboBox<String> blockSpeedCombo = new JComboBox<>(speedList);
        blockSpeedCombo.setEditable(true);

        JPanel block = new JPanel();
        block.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BlockSpeedLabel"))));
        block.add(blockSpeedCombo);

        blockSpeedCombo.removeItem(defaultBlockSpeedText);

        blockSpeedCombo.setSelectedItem(InstanceManager.getDefault(jmri.BlockManager.class).getDefaultSpeed());

        // block of options above row of buttons; gleaned from Maintenance.makeDialog()
        // can be accessed by Jemmy in GUI test
        String title = Bundle.getMessage("BlockSpeedLabel");
        // build JPanel for comboboxes
        JPanel speedspanel = new JPanel();
        speedspanel.setLayout(new BoxLayout(speedspanel, BoxLayout.PAGE_AXIS));
        speedspanel.add(new JLabel(Bundle.getMessage("BlockSpeedSelectDialog")));
        //default LEFT_ALIGNMENT
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        speedspanel.add(block);

        int retval = JOptionPane.showConfirmDialog(
                _who,
                speedspanel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        log.debug("Retval = {}", retval);
        if (retval != JOptionPane.OK_OPTION) { // OK button not clicked
            return;
        }

        String speedValue = (String) blockSpeedCombo.getSelectedItem();
        //We will allow the turnout manager to handle checking if the values have changed
        try {
            InstanceManager.getDefault(jmri.BlockManager.class).setDefaultSpeed(speedValue);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speedValue);
        }
    }

    private void inchBoxChanged() {
        centimeterBox.setSelected(!inchBox.isSelected());
        m.fireTableDataChanged();  // update view
    }

    private void centimeterBoxChanged() {
        inchBox.setSelected(!centimeterBox.isSelected());
        m.fireTableDataChanged();  // update view
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.BlockTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(20);
    JTextField userName = new JTextField(20);
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));

    JComboBox<String> cur = new JComboBox<String>(curveOptions);
    JTextField lengthField = new JTextField(7);
    JTextField blockSpeed = new JTextField(7);
    JCheckBox checkPerm = new JCheckBox(Bundle.getMessage("BlockPermColName"));

    SpinnerNumberModel numberToAddSpinnerNumberModel = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAddSpinner = new JSpinner(numberToAddSpinnerNumberModel);
    JCheckBox addRangeCheckBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JCheckBox _autoSystemNameCheckBox = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    JLabel statusBar = new JLabel(Bundle.getMessage("AddBeanStatusEnter"), JLabel.LEADING);
    jmri.UserPreferencesManager pref;

    @Override
    protected void addPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddBlock"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.BlockAddEdit", true); //NOI18N
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            ActionListener oklistener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            };
            ActionListener cancellistener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            };
            addFrame.add(new AddNewBeanPanel(sysName, userName, numberToAddSpinner, addRangeCheckBox, _autoSystemNameCheckBox, "ButtonCreate", oklistener, cancellistener, statusBar));
            sysName.setToolTipText(Bundle.getMessage("SysNameToolTip", "B")); // override tooltip with bean specific letter
        }
        sysName.setBackground(Color.white);
        // reset statusBar text
        statusBar.setText(Bundle.getMessage("AddBeanStatusEnter"));
        statusBar.setForeground(Color.gray);
        if (pref.getSimplePreferenceState(systemNameAuto)) {
            _autoSystemNameCheckBox.setSelected(true);
        }
        addRangeCheckBox.setSelected(false);
        addFrame.pack();
        addFrame.setVisible(true);
    }

    JComboBox<String> speeds = new JComboBox<String>();

/*    JPanel additionalAddOption() {

        GridLayout additionLayout = new GridLayout(0, 2);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(additionLayout);
        mainPanel.add(new JLabel(Bundle.getMessage("BlockLengthColName")));
        mainPanel.add(lengthField);

        mainPanel.add(new JLabel(Bundle.getMessage("BlockCurveColName")));
        mainPanel.add(cur);

        mainPanel.add(new JLabel("  "));
        mainPanel.add(checkPerm);

        speeds = new JComboBox<String>();
        speeds.setEditable(true);
        for (int i = 0; i < speedList.size(); i++) {
            speeds.addItem(speedList.get(i));
        }

        mainPanel.add(new JLabel(Bundle.getMessage("BlockSpeed")));
        mainPanel.add(speeds);

        //return displayList;
        lengthField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                String text = lengthField.getText();
                if (!validateNumericalInput(text)) {
                    String msg = Bundle.getMessage("ShouldBeNumber", new Object[]{Bundle.getMessage("BlockLengthColName")});
                    jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).showWarningMessage(Bundle.getMessage("ErrorTitle"), msg, getClassName(), "length", false, false);
                }
            }

            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }
        });

        return mainPanel;
    }*/

    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";

    boolean validateNumericalInput(String text) {
        if (text.length() != 0) {
            try {
                Integer.parseInt(text);
            } catch (java.lang.NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    /**
     * Respond to Create new item pressed on Add Block pane.
     *
     * @param e the click event
     */
    void okPressed(ActionEvent e) {

        int numberOfBlocks = 1;

        if (addRangeCheckBox.isSelected()) {
            numberOfBlocks = (Integer) numberToAddSpinner.getValue();
        }
        if (numberOfBlocks >= 65) { // limited by JSpinnerModel to 100
            if (JOptionPane.showConfirmDialog(addFrame,
                    Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("Blocks"), numberOfBlocks),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }
        String user = NamedBean.normalizeUserName(userName.getText());
        if (user == null || user.isEmpty()) {
            user = null;
        }
        String uName = user; // keep result separate to prevent recursive manipulation
        String system = "";

        if (!_autoSystemNameCheckBox.isSelected()) {
            system = InstanceManager.getDefault(jmri.BlockManager.class).makeSystemName(sysName.getText());
        }
        String sName = system; // keep result separate to prevent recursive manipulation
        // initial check for empty entry using the raw name
        if (sName.length() < 3 && !_autoSystemNameCheckBox.isSelected()) {  // Using 3 to catch a plain IB
            statusBar.setText(Bundle.getMessage("WarningSysNameEmpty"));
            statusBar.setForeground(Color.red);
            sysName.setBackground(Color.red);
            return;
        } else {
            sysName.setBackground(Color.white);
        }

        // Add some entry pattern checking, before assembling sName and handing it to the blockManager
        String statusMessage = Bundle.getMessage("ItemCreateFeedback", Bundle.getMessage("BeanNameBlock"));

        for (int x = 0; x < numberOfBlocks; x++) {
            if (x != 0) { // start at 2nd Block
                if (!_autoSystemNameCheckBox.isSelected()) {
                    // Find first block with unused system name
                    while (true) {
                        system = nextName(system);
                        // log.warn("Trying " + system);
                        Block blk = InstanceManager.getDefault(jmri.BlockManager.class).getBySystemName(system);
                        if (blk == null) {
                            sName = system;
                            break;
                        }
                    }
                }
                if (user != null) {
                    // Find first block with unused user name
                    while (true) {
                        user = nextName(user);
                        //log.warn("Trying " + user);
                        Block blk = InstanceManager.getDefault(jmri.BlockManager.class).getByUserName(user);
                        if (blk == null) {
                            uName = user;
                            break;
                        }
                    }
                }
            }
            Block blk;
            String xName = "";
            try {
                if (_autoSystemNameCheckBox.isSelected()) {
                    blk = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock(uName);
                    if (blk == null) {
                        xName = uName;
                        throw new java.lang.IllegalArgumentException();
                    }
                } else {
                    blk = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock(sName, uName);
                    if (blk == null) {
                        xName = sName;
                        throw new java.lang.IllegalArgumentException();
                    }
                }
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(xName);
                statusBar.setText(Bundle.getMessage("ErrorAddFailedCheck"));
                statusBar.setForeground(Color.red);
                return; // without creating
            }
            if (lengthField.getText().length() != 0) {
                blk.setLength(Integer.parseInt(lengthField.getText()));
            }
            /*if (blockSpeed.getText().length()!=0)
             blk.setSpeedLimit(Integer.parseInt(blockSpeed.getText()));*/
            try {
                blk.setBlockSpeed((String) speeds.getSelectedItem());
            } catch (jmri.JmriException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + (String) speeds.getSelectedItem());
            }
            if (checkPerm.isSelected()) {
                blk.setPermissiveWorking(true);
            }
            String cName = (String) cur.getSelectedItem();
            if (cName.equals(noneText)) {
                blk.setCurvature(Block.NONE);
            } else if (cName.equals(gradualText)) {
                blk.setCurvature(Block.GRADUAL);
            } else if (cName.equals(tightText)) {
                blk.setCurvature(Block.TIGHT);
            } else if (cName.equals(severeText)) {
                blk.setCurvature(Block.SEVERE);
            }
            // add first and last names to statusMessage user feedback string
            if (x == 0 || x == numberOfBlocks - 1) {
                statusMessage = statusMessage + " " + sName + " (" + user + ")";
            }
            if (x == numberOfBlocks - 2) {
                statusMessage = statusMessage + " " + Bundle.getMessage("ItemCreateUpTo") + " ";
            }
            // only mention first and last of addRangeCheckBox added
        } // end of for loop creating addRangeCheckBox of Blocks

        // provide feedback to user
        statusBar.setText(statusMessage);
        statusBar.setForeground(Color.gray);

        pref.setSimplePreferenceState(systemNameAuto, _autoSystemNameCheckBox.isSelected());
    }

    void handleCreateException(String sysName) {
        JOptionPane.showMessageDialog(addFrame,
                Bundle.getMessage("ErrorBlockAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }
    //private boolean noWarn = false;

    void deletePaths(jmri.util.JmriJFrame f) {
        // Set option to prevent the path information from being saved.

        Object[] options = {Bundle.getMessage("ButtonRemove"),
            Bundle.getMessage("ButtonKeep")};

        int retval = JOptionPane.showOptionDialog(f,
                Bundle.getMessage("BlockPathMessage"),
                Bundle.getMessage("BlockPathSaveTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (retval != 0) {
            InstanceManager.getDefault(jmri.BlockManager.class).setSavedPathInfo(true);
            log.info("Requested to save path information via Block Menu.");
        } else {
            InstanceManager.getDefault(jmri.BlockManager.class).setSavedPathInfo(false);
            log.info("Requested not to save path information via Block Menu.");
        }
    }

    @Override
    public void dispose() {
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setSimplePreferenceState(getClassName() + ":LengthUnitMetric", centimeterBox.isSelected());
        super.dispose();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleBlockTable");
    }

    @Override
    protected String getClassName() {
        return BlockTableAction.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(BlockTableAction.class);

}
