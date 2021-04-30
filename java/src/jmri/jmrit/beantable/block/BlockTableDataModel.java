package jmri.jmrit.beantable.block;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.beans.PropertyChangeEvent;

import java.io.File;
import java.io.IOException;

import java.text.DecimalFormat;

import java.util.Arrays;
import java.util.Set;
import java.util.Vector;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jmri.*;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.beantable.*;
import jmri.jmrit.beantable.beanedit.BlockEditAction;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.swing.JComboBoxUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data model for a Block Table.
 * Code originally within BlockTableAction.
 * 
 * @author Bob Jacobsen Copyright (C) 2003, 2008
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class BlockTableDataModel extends BeanTableDataModel<Block> {
    
    static public final int EDITCOL = BeanTableDataModel.NUMCOLUMN;
    static public final int DIRECTIONCOL = EDITCOL + 1;
    static public final int LENGTHCOL = DIRECTIONCOL + 1;
    static public final int CURVECOL = LENGTHCOL + 1;
    static public final int STATECOL = CURVECOL + 1;
    static public final int SENSORCOL = STATECOL + 1;
    static public final int REPORTERCOL = SENSORCOL + 1;
    static public final int CURRENTREPCOL = REPORTERCOL + 1;
    static public final int PERMISCOL = CURRENTREPCOL + 1;
    static public final int SPEEDCOL = PERMISCOL + 1;

    private final boolean _graphicState = InstanceManager.getDefault(GuiLafPreferencesManager.class).isGraphicTableState();
    
    private final DecimalFormat twoDigit = new DecimalFormat("0.00");
    
    private Vector<String> speedList = new Vector<>();
    private String[] sensorList;
    private String[] reporterList;
    
    String defaultBlockSpeedText;
    
    public BlockTableDataModel(Manager<Block> mgr){
        super();
        setManager(mgr); // for consistency with other BeanTableModels, default BlockManager always used.
        
        defaultBlockSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + InstanceManager.getDefault(BlockManager.class).getDefaultSpeed()); // first entry in drop down list
        speedList.add(defaultBlockSpeedText);
        Vector<String> _speedMap = InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames();
        for (int i = 0; i < _speedMap.size(); i++) {
            if (!speedList.contains(_speedMap.get(i))) {
                speedList.add(_speedMap.get(i));
            }
        }
        
        updateSensorList();
        updateReporterList();
    }
    
    @Override
    public String getValue(String name) {
        if (name == null) {
            log.warn("requested getValue(null)");
            return "(no name)";
        }
        Block b = InstanceManager.getDefault(BlockManager.class).getBySystemName(name);
        if (b == null) {
            log.debug("requested getValue(\"{}\"), Block doesn't exist", name);
            return "(no Block)";
        }
        Object m = b.getValue();
        if (m != null) {
            if ( m instanceof Reportable) {
                return ((Reportable) m).toReportString();
            }
            else {
                return m.toString();
            }
        } else {
            return "";
        }
    }

    @Override
    public Manager<Block> getManager() {
        return InstanceManager.getDefault(BlockManager.class);
    }

    @Override
    public Block getBySystemName(@Nonnull String name) {
        return InstanceManager.getDefault(BlockManager.class).getBySystemName(name);
    }

    @Override
    public Block getByUserName(@Nonnull String name) {
        return InstanceManager.getDefault(BlockManager.class).getByUserName(name);
    }

    @Override
    protected String getMasterClassName() {
        return BlockTableAction.class.getName();
    }

    @Override
    public void clickOn(Block t) {
        // don't do anything on click; not used in this class, because
        // we override setValueAt
    }

    @Override
    public int getColumnCount() {
        return SPEEDCOL + 1;
    }

    @Override
    public Object getValueAt(int row, int col) {
        // some error checking
        if (row >= sysNameList.size()) {
            log.error("requested getValueAt(\"{}\"), row outside of range", row);
            return "Error table size";
        }
        Block b = getBySystemName(sysNameList.get(row));
        if (b == null) {
            log.error("requested getValueAt(\"{}\"), Block doesn't exist", row);
            return "(no Block)";
        }
        switch (col) {
            case DIRECTIONCOL:
                return Path.decodeDirection(b.getDirection());
            case CURVECOL:
                BlockCurvatureJComboBox box = new BlockCurvatureJComboBox(b.getCurvature());
                box.setJTableCellClientProperties();
                return box;
            case LENGTHCOL:
                return (twoDigit.format(metricUi ?  b.getLengthCm() : b.getLengthIn()));
            case PERMISCOL:
                return b.getPermissiveWorking();
            case SPEEDCOL:
                String speed = b.getBlockSpeed();
                if (!speedList.contains(speed)) {
                    speedList.add(speed);
                }
                JComboBox<String> c = new JComboBox<>(speedList);
                c.setEditable(true);
                c.setSelectedItem(speed);
                JComboBoxUtil.setupComboBoxMaxRows(c);
                return c;
            case STATECOL:
                return blockDescribeState(b.getState());
            case SENSORCOL:
                Sensor sensor = b.getSensor();
                JComboBox<String> cs = new JComboBox<>(sensorList);
                String name = "";
                if (sensor != null) {
                    name = sensor.getDisplayName();
                }
                cs.setSelectedItem(name);
                JComboBoxUtil.setupComboBoxMaxRows(cs);
                return cs;
            case REPORTERCOL:
                Reporter reporter = b.getReporter();
                JComboBox<String> rs = new JComboBox<>(reporterList);
                String repname = "";
                if (reporter != null) {
                    repname = reporter.getDisplayName();
                }
                rs.setSelectedItem(repname);
                JComboBoxUtil.setupComboBoxMaxRows(rs);
                return rs;
            case CURRENTREPCOL:
                return b.isReportingCurrent();
            case EDITCOL:
                return Bundle.getMessage("ButtonEdit");
            default:
                return super.getValueAt(row, col);
        }
    }
    
    // TODO : Add Block.UNDETECTED
    // TODO : Move to Block.describeState(int)
    private String blockDescribeState(int blockState){
        switch (blockState) {
            case (Block.OCCUPIED):
                return Bundle.getMessage("BlockOccupied");
            case (Block.UNOCCUPIED):
                return Bundle.getMessage("BlockUnOccupied");
            case (Block.UNKNOWN):
                return Bundle.getMessage("BlockUnknown");
            default:
                return Bundle.getMessage("BlockInconsistent");
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        // no setting of block state from table
        Block b = getBySystemName(sysNameList.get(row));
        switch (col) {
            case VALUECOL:
                b.setValue(value);
                break;
            case LENGTHCOL:
                float len = 0.0f;
                try {
                    len = jmri.util.IntlUtilities.floatValue(value.toString());
                } catch (java.text.ParseException ex2) {
                    log.error("Error parsing length value of \"{}\"", value);
                }   // block setLength() expecting value in mm, TODO: unit testing around this.
                b.setLength( metricUi ? len * 10.0f : len * 25.4f);
                break;
            case CURVECOL:
                b.setCurvature(BlockCurvatureJComboBox.getCurvatureFromObject(value));
                break;
            case PERMISCOL:
                b.setPermissiveWorking((Boolean) value);
                break;
            case SPEEDCOL:
                @SuppressWarnings("unchecked")
                String speed = (String) ((JComboBox<String>) value).getSelectedItem();
                try {
                    b.setBlockSpeed(speed);
                } catch (JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                    return;
                }
                if (!speedList.contains(speed) && !speed.contains("Global")) { // NOI18N
                    speedList.add(speed);
                }
                break;
            case REPORTERCOL:
                @SuppressWarnings("unchecked")
                String strReporter = (String) ((JComboBox<String>) value).getSelectedItem();
                Reporter r = InstanceManager.getDefault(ReporterManager.class).getReporter(strReporter);
                b.setReporter(r);
                break;
            case SENSORCOL:
                @SuppressWarnings("unchecked")
                String strSensor = (String) ((JComboBox<String>) value).getSelectedItem();
                b.setSensor(strSensor);
                break;
            case CURRENTREPCOL:
                b.setReportingCurrent((Boolean) value);
                break;
            case EDITCOL:
                javax.swing.SwingUtilities.invokeLater(() -> {
                    editButton(b);
                });
                break;
            default:
                super.setValueAt(value, row, col);
                break;
        }
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case DIRECTIONCOL:
                return Bundle.getMessage("BlockDirection");
            case VALUECOL:
                return Bundle.getMessage("BlockValue");
            case CURVECOL:
                return Bundle.getMessage("BlockCurveColName");
            case LENGTHCOL:
                return Bundle.getMessage("BlockLengthColName");
            case PERMISCOL:
                return Bundle.getMessage("BlockPermColName");
            case SPEEDCOL:
                return Bundle.getMessage("BlockSpeedColName");
            case STATECOL:
                return Bundle.getMessage("BlockState");
            case REPORTERCOL:
                return Bundle.getMessage("BlockReporter");
            case SENSORCOL:
                return Bundle.getMessage("BlockSensor");
            case CURRENTREPCOL:
                return Bundle.getMessage("BlockReporterCurrent");
            case EDITCOL:
                return Bundle.getMessage("ButtonEdit");
            default:
                return super.getColumnName(col);
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case DIRECTIONCOL:
            case VALUECOL: // not a button
            case LENGTHCOL:
                return String.class;
            case STATECOL: // may use an image to show block state
                if (_graphicState) {
                    return JLabel.class;
                } else {
                    return String.class;
                }
            case SPEEDCOL:
            case CURVECOL:
            case REPORTERCOL:
            case SENSORCOL:
                return JComboBox.class;
            case CURRENTREPCOL:
            case PERMISCOL:
                return Boolean.class;
            case EDITCOL:
                return JButton.class;
            default:
                return super.getColumnClass(col);
        }
    }

    @Override
    public int getPreferredWidth(int col) {
        switch (col) {
            case DIRECTIONCOL:
            case LENGTHCOL:
            case PERMISCOL:
            case SPEEDCOL:
            case CURRENTREPCOL:
            case EDITCOL:
                return new JTextField(7).getPreferredSize().width;
            case CURVECOL:
            case STATECOL:
            case REPORTERCOL:
            case SENSORCOL:
                return new JTextField(8).getPreferredSize().width;
            default:
                return super.getPreferredWidth(col);
        }
    }

    @Override
    public void configValueColumn(JTable table) {
        // value column isn't button, so config is null
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case CURVECOL:
            case LENGTHCOL:
            case PERMISCOL:
            case SPEEDCOL:
            case REPORTERCOL:
            case SENSORCOL:
            case CURRENTREPCOL:
            case EDITCOL:
                return true;
            case STATECOL:
                return false;
            default:
                return super.isCellEditable(row, col);
        }
    }

    @Override
    public void configureTable(JTable table) {
        InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
        InstanceManager.getDefault(ReporterManager.class).addPropertyChangeListener(this);
        configStateColumn(table);
        super.configureTable(table);
    }
    
    void editButton(Block b) {
        BlockEditAction beanEdit = new BlockEditAction();
        beanEdit.setBean(b);
        beanEdit.actionPerformed(null);
    }

    /**
     * returns true for all Block properties.
     * @param e property event that has changed.
     * @return true as all matched.
     */
    @Override
    protected boolean matchPropertyName(PropertyChangeEvent e) {
        return true;
    }

    @Override
    public JButton configureButton() {
        log.error("configureButton should not have been called");
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getSource() instanceof SensorManager) {
            if (e.getPropertyName().equals("length") || e.getPropertyName().equals("DisplayListName")) { // NOI18N
                updateSensorList();
            }
        }
        if (e.getSource() instanceof ReporterManager) {
            if (e.getPropertyName().equals("length") || e.getPropertyName().equals("DisplayListName")) { // NOI18N
                updateReporterList();
            }
        }
        if (e.getPropertyName().equals("DefaultBlockSpeedChange")) { // NOI18N
            updateSpeedList();
        } else {
            super.propertyChange(e);
        }
    }
    
    private boolean metricUi = InstanceManager.getDefault(UserPreferencesManager.class)
        .getSimplePreferenceState(BlockTableAction.BLOCK_METRIC_PREF);
    
    /**
     * Set and refresh the UI to use Metric or Imperial values.
     * @param boo true if metric, false for Imperial.
     */
    public void setMetric(boolean boo){
        metricUi = boo;
        fireTableDataChanged();
    }
    
    private void updateSensorList() {
        Set<Sensor> nameSet = InstanceManager.sensorManagerInstance().getNamedBeanSet();
        String[] displayList = new String[nameSet.size()];
        int i = 0;
        for (Sensor nBean : nameSet) {
            if (nBean != null) {
                displayList[i++] = nBean.getDisplayName();
            }
        }
        Arrays.sort(displayList);
        sensorList = new String[displayList.length + 1];
        sensorList[0] = "";
        i = 1;
        for (String name : displayList) {
            sensorList[i] = name;
            i++;
        }
    }

    private void updateReporterList() {
        Set<Reporter> nameSet = InstanceManager.getDefault(ReporterManager.class).getNamedBeanSet();
        String[] displayList = new String[nameSet.size()];
        int i = 0;
        for (Reporter nBean : nameSet) {
            if (nBean != null) {
                displayList[i++] = nBean.getDisplayName();
            }
        }
        Arrays.sort(displayList);
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
        defaultBlockSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + InstanceManager.getDefault(BlockManager.class).getDefaultSpeed());
        speedList.add(0, defaultBlockSpeedText);
        fireTableDataChanged();
    }
    
    public void setDefaultSpeeds(JFrame _who) {
        JComboBox<String> blockSpeedCombo = new JComboBox<>(speedList);
        
        blockSpeedCombo.setEditable(true);

        JPanel block = new JPanel();
        block.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BlockSpeedLabel"))));
        block.add(blockSpeedCombo);

        blockSpeedCombo.removeItem(defaultBlockSpeedText);

        blockSpeedCombo.setSelectedItem(InstanceManager.getDefault(BlockManager.class).getDefaultSpeed());

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
            InstanceManager.getDefault(BlockManager.class).setDefaultSpeed(speedValue);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(_who, ex.getMessage() + "\n" + speedValue);
        }
    }
    
    @Override
    synchronized public void dispose() {
        InstanceManager.getDefault(SensorManager.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(ReporterManager.class).removePropertyChangeListener(this);
        super.dispose();
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
    
    // state column may be image so have the tooltip as text version of Block state.
    // length column tooltip confirms inches or cm.
    @Override
    public String getCellToolTip(JTable table, int row, int col) {
        switch (col) {
            case BlockTableDataModel.STATECOL:
                Block b = (Block) getValueAt(row, 0);
                return blockDescribeState(b.getState());
            case BlockTableDataModel.LENGTHCOL:
                return ( metricUi ? Bundle.getMessage("LengthCentimeters"): Bundle.getMessage("LengthInches"));
            default:
                return super.getCellToolTip(table, row, col);
        }
    }

    /**
     * Visualize state in table as a graphic, customized for Blocks (2
     * states). Renderer and Editor are identical, as the cell contents
     * are not actually edited.
     *
     */
    static class ImageIconRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

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
            Block b = (Block) table.getModel().getValueAt(row, 0);
            return updateLabel(b);
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
            Block b = (Block) table.getModel().getValueAt(row, 0);
            return updateLabel(b);
        }

        public JLabel updateLabel(Block b) {
            //  if (iconHeight > 0) { // if necessary, increase row height;
            //table.setRowHeight(row, Math.max(table.getRowHeight(), iconHeight - 5)); // TODO adjust table row height for Block icons
            //                     }
            if (b.getState()==Block.UNOCCUPIED && offIcon != null) {
                label = new JLabel(offIcon);
                label.setVerticalAlignment(JLabel.BOTTOM);
            } else if (b.getState()==Block.OCCUPIED && onIcon != null) {
                label = new JLabel(onIcon);
                label.setVerticalAlignment(JLabel.BOTTOM);
            } else if (b.getState()==Block.INCONSISTENT) {
                label = new JLabel("X", JLabel.CENTER); // centered text alignment
                label.setForeground(Color.red);
                iconHeight = 0;
            } else { // Unknown Undetected Other
                label = new JLabel("?", JLabel.CENTER); // centered text alignment
                iconHeight = 0;
            }
            label.addMouseListener(new MouseAdapter() {
                @Override
                public final void mousePressed(MouseEvent evt) {
                    log.debug("Clicked on icon for block {}",b);
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

    private final static Logger log = LoggerFactory.getLogger(BlockTableDataModel.class);
    
}
