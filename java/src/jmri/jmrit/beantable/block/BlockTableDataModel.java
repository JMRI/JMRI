package jmri.jmrit.beantable.block;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.beans.PropertyChangeEvent;

import java.io.File;
import java.io.IOException;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import jmri.*;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.beantable.*;
import jmri.jmrit.beantable.beanedit.BlockEditAction;
import jmri.util.gui.GuiLafPreferencesManager;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 * Data model for a Block Table.
 * Code originally within BlockTableAction.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2008
 * @author Egbert Broerse Copyright (C) 2017
 * @author Steve Young Copyright (C) 2021
 */
public class BlockTableDataModel extends BeanTableDataModel<Block> {

    public static final int EDITCOL = BeanTableDataModel.NUMCOLUMN;
    public static final int DIRECTIONCOL = EDITCOL + 1;
    public static final int LENGTHCOL = DIRECTIONCOL + 1;
    public static final int CURVECOL = LENGTHCOL + 1;
    public static final int STATECOL = CURVECOL + 1;
    public static final int SENSORCOL = STATECOL + 1;
    public static final int REPORTERCOL = SENSORCOL + 1;
    public static final int CURRENTREPCOL = REPORTERCOL + 1;
    public static final int PERMISCOL = CURRENTREPCOL + 1;
    public static final int SPEEDCOL = PERMISCOL + 1;
    public static final int GHOSTCOL = SPEEDCOL + 1;
    public static final int COLUMNCOUNT = GHOSTCOL + 1;

    private final boolean _graphicState =
        InstanceManager.getDefault(GuiLafPreferencesManager.class).isGraphicTableState();

    private Vector<String> speedList = new Vector<>();

    private String defaultBlockSpeedText;

    public BlockTableDataModel(Manager<Block> mgr){
        super();
        setManager(mgr); // for consistency with other BeanTableModels, default BlockManager always used.

        defaultBlockSpeedText = Bundle.getMessage("UseGlobal", "Global") + " " +
            InstanceManager.getDefault(BlockManager.class).getDefaultSpeed(); // first entry in drop down list
        speedList.add(defaultBlockSpeedText);
        Vector<String> speedMap = InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames();
        for (int i = 0; i < speedMap.size(); i++) {
            if (!speedList.contains(speedMap.get(i))) {
                speedList.add(speedMap.get(i));
            }
        }

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
        return COLUMNCOUNT;
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
                return metricUi ?  b.getLengthCm() : b.getLengthIn();
            case PERMISCOL:
                return b.getPermissiveWorking();
            case GHOSTCOL:
                return b.getIsGhost();
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
                return b.describeState(b.getState());
            case SENSORCOL:
                return b.getSensor();
            case REPORTERCOL:
                return b.getReporter();
            case CURRENTREPCOL:
                return b.isReportingCurrent();
            case EDITCOL:
                return Bundle.getMessage("ButtonEdit");
            default:
                return super.getValueAt(row, col);
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
            case GHOSTCOL:
                b.setIsGhost((boolean) value);
                break;
            case SPEEDCOL:
                @SuppressWarnings("unchecked")
                String speed = (String) ((JComboBox<String>) value).getSelectedItem();
                try {
                    b.setBlockSpeed(speed);
                } catch (JmriException ex) {
                    JmriJOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                    return;
                }
                if (!speedList.contains(speed) && !speed.contains("Global")) { // NOI18N
                    speedList.add(speed);
                }
                break;
            case REPORTERCOL:
                if ( value==null || value instanceof Reporter) {
                    b.setReporter((Reporter)value);
                }
                break;
            case SENSORCOL:
                b.setSensor(value instanceof Sensor ? ((Sensor)value).getDisplayName() : "");
                break;
            case CURRENTREPCOL:
                b.setReportingCurrent((Boolean) value);
                break;
            case EDITCOL:
                javax.swing.SwingUtilities.invokeLater( () -> editButton(b) );
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
            case GHOSTCOL:
                return Bundle.getMessage("BlockGhostColName");
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
                return String.class;
            case LENGTHCOL:
                return Float.class;
            case STATECOL: // may use an image to show block state
                if (_graphicState) {
                    return JLabel.class;
                } else {
                    return String.class;
                }
            case SPEEDCOL:
            case CURVECOL:
                return JComboBox.class;
            case REPORTERCOL:
                return Reporter.class;
            case SENSORCOL:
                return Sensor.class;
            case CURRENTREPCOL:
            case PERMISCOL:
            case GHOSTCOL:
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
            case GHOSTCOL:
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
            case GHOSTCOL:
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
    public JTable makeJTable(@Nonnull String name, @Nonnull TableModel model,
        @CheckForNull RowSorter<? extends TableModel> sorter) {
        if (!(model instanceof BlockTableDataModel)){
            throw new IllegalArgumentException("Model is not a BlockTableDataModel");
        }
        return configureJTable(name, new BlockTableJTable((BlockTableDataModel)model), sorter);
    }

    @Override
    public void configureTable(JTable table) {
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
        if ( BlockManager.PROPERTY_DEFAULT_BLOCK_SPEED_CHANGE.equals(e.getPropertyName()) ) {
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

    private void updateSpeedList() {
        speedList.remove(defaultBlockSpeedText);
        defaultBlockSpeedText = (Bundle.getMessage("UseGlobal", "Global")
            + " " + InstanceManager.getDefault(BlockManager.class).getDefaultSpeed());
        speedList.add(0, defaultBlockSpeedText);
        fireTableDataChanged();
    }

    public void setDefaultSpeeds(JFrame frame) {
        JComboBox<String> blockSpeedCombo = new JComboBox<>(speedList);
        JComboBoxUtil.setupComboBoxMaxRows(blockSpeedCombo);

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

        int retval = JmriJOptionPane.showConfirmDialog(
                frame,
                speedspanel,
                title,
                JmriJOptionPane.OK_CANCEL_OPTION,
                JmriJOptionPane.INFORMATION_MESSAGE);
        log.debug("Retval = {}", retval);
        if (retval != JmriJOptionPane.OK_OPTION) { // OK button not clicked
            return;
        }

        String speedValue = (String) blockSpeedCombo.getSelectedItem();
        //We will allow the turnout manager to handle checking if the values have changed
        try {
            InstanceManager.getDefault(BlockManager.class).setDefaultSpeed(speedValue);
        } catch (IllegalArgumentException ex) {
            JmriJOptionPane.showMessageDialog(frame, ex.getLocalizedMessage()+ "\n" + speedValue);
        }
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
        log.debug("Block configStateColumn (I am {})", this);
        if (_graphicState) {
            // have the state column hold a JPanel (icon)
            table.setDefaultRenderer(JLabel.class, new ImageIconRenderer());
        } // else, classic text style state indication, do nothing extra
    }

    // state column may be image so have the tooltip as text version of Block state.
    // length column tooltip confirms inches or cm.
    @Override
    public String getCellToolTip(JTable table, int modelRow, int modelCol) {
        switch (modelCol) {
            case BlockTableDataModel.STATECOL:
                Block b = (Block) getValueAt(modelRow, 0);
                return b.describeState(b.getState());
            case BlockTableDataModel.LENGTHCOL:
                return ( metricUi ? Bundle.getMessage("LengthCentimeters"): Bundle.getMessage("LengthInches"));
            default:
                return super.getCellToolTip(table, modelRow, modelCol);
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
            // TODO Undetected ?
            // TODO adjust table row height for Block icons
            //  if (iconHeight > 0) { // if necessary, increase row height;
            //table.setRowHeight(row, Math.max(table.getRowHeight(), iconHeight - 5));
            //                     }
            if (b.getState()==Block.UNOCCUPIED && offIcon != null) {
                label = new JLabel(offIcon);
                label.setVerticalAlignment(SwingConstants.BOTTOM);
            } else if (b.getState()==Block.OCCUPIED && onIcon != null) {
                label = new JLabel(onIcon);
                label.setVerticalAlignment(SwingConstants.BOTTOM);
            } else if (b.getState()==Block.INCONSISTENT) {
                label = new JLabel("X", SwingConstants.CENTER); // centered text alignment
                label.setForeground(Color.red);
                iconHeight = 0;
            } else { // Unknown Undetected Other
                label = new JLabel("?", SwingConstants.CENTER); // centered text alignment
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockTableDataModel.class);

}
