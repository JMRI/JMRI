package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.beantable.BlockTableAction;
import jmri.jmrit.beantable.block.BlockCurvatureJComboBox;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.swing.NamedBeanComboBox;
import jmri.util.swing.JComboBoxUtil;

/**
 * Provides an edit panel for a Block object.
 * Note that LayoutBlockEditAction extends this class, so please check
 * this still functions as expected when making changes here.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class BlockEditAction extends BeanEditAction<Block> {

    static final java.util.Vector<String> speedList = new java.util.Vector<>();

    @Override
    public String helpTarget() {
        return "package.jmri.jmrit.beantable.BlockEdit";
    } // NOI18N

    @Override
    protected void initPanels() {
        super.initPanels();
        sensor();
        reporterDetails();
        physicalDetails();
    }

    @Override
    public Block getByUserName(String name) {
        return InstanceManager.getDefault(BlockManager.class).getByUserName(name);
    }

    private NamedBeanComboBox<Reporter> reporterComboBox;
    private final JCheckBox useCurrent = new JCheckBox();

    private final JSpinner lengthSpinner = new JSpinner(); // 2 digit decimal format field, initialized later as instance
    private final BlockCurvatureJComboBox curvatureField = new BlockCurvatureJComboBox();
    private final JCheckBox permissiveField = new JCheckBox();
    private final JCheckBox ghostField = new JCheckBox();
    private JComboBox<String> speedField;

    private final JRadioButton inch = new JRadioButton(Bundle.getMessage("LengthInches"));
    private final JRadioButton cm = new JRadioButton(Bundle.getMessage("LengthCentimeters"));

    protected boolean metricUi = InstanceManager.getDefault(UserPreferencesManager.class)
        .getSimplePreferenceState(BlockTableAction.BLOCK_METRIC_PREF);

    BeanItemPanel reporterDetails() {
        BeanItemPanel reporter = new BeanItemPanel();
        reporter.setName(Bundle.getMessage("BeanNameReporter"));

        reporterComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(
            ReporterManager.class), bean.getReporter(), DisplayOptions.DISPLAYNAME);
        reporterComboBox.setAllowNull(true);
        JComboBoxUtil.setupComboBoxMaxRows(reporterComboBox);

        reporter.addItem(new BeanEditItem(reporterComboBox,
            Bundle.getMessage("BeanNameReporter"), Bundle.getMessage("BlockReporterText")));

        reporterComboBox.addActionListener( e ->
            useCurrent.setEnabled(reporterComboBox.getSelectedItem() != null));

        reporter.addItem(new BeanEditItem(useCurrent,
            Bundle.getMessage("BlockReporterCurrent"), Bundle.getMessage("BlockUseCurrentText")));

        reporter.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reporterComboBox.setSelectedItem(bean.getReporter());
                useCurrent.setSelected(bean.isReportingCurrent());
                useCurrent.setEnabled(bean.getReporter()!=null);
            }
        });

        reporter.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bean.setReporter(reporterComboBox.getSelectedItem());
                bean.setReportingCurrent(useCurrent.isSelected());
            }
        });
        bei.add(reporter);
        if (InstanceManager.getNullableDefault(ReporterManager.class) == null) {
            setEnabled(false);
        }
        return reporter;
    }

    BeanItemPanel physicalDetails() {

        String defaultBlockSpeedText = Bundle.getMessage("UseGlobal", "Global")
            + " " + InstanceManager.getDefault(BlockManager.class).getDefaultSpeed();
        speedList.add(defaultBlockSpeedText);
        java.util.Vector<String> _speedMap = InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames();
        for (int i = 0; i < _speedMap.size(); i++) {
            if (!speedList.contains(_speedMap.get(i))) {
                speedList.add(_speedMap.get(i));
            }
        }
        BeanItemPanel basic = new BeanItemPanel();
        basic.setName(Bundle.getMessage("BlockPhysicalProperties"));

        basic.addItem(new BeanEditItem(null, null, Bundle.getMessage("BlockPropertiesText")));
        lengthSpinner.setModel( new SpinnerNumberModel(
            Float.valueOf(0f), Float.valueOf(0f), Float.valueOf(1000f), Float.valueOf(0.01f)));
        lengthSpinner.setEditor(new JSpinner.NumberEditor(lengthSpinner, "###0.00"));
        lengthSpinner.setPreferredSize(new JTextField(8).getPreferredSize());

        ButtonGroup rg = new ButtonGroup();
        rg.add(inch);
        rg.add(cm);

        JPanel p = new JPanel();
        p.add(inch);
        p.add(cm);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        inch.setSelected(!metricUi);
        cm.setSelected(metricUi);
        inch.addActionListener( e -> updateLength());
        cm.addActionListener( e -> updateLength());

        basic.addItem(new BeanEditItem(lengthSpinner,
            Bundle.getMessage("BlockLengthColName"), Bundle.getMessage("BlockLengthText") ));
        basic.addItem(new BeanEditItem(p,
            Bundle.getMessage("BlockLengthUnits"), Bundle.getMessage("BlockLengthUnitsText")));
        addRouteLengths(basic);
        basic.addItem(new BeanEditItem(curvatureField, Bundle.getMessage("BlockCurveColName"), ""));
        speedField = new JComboBox<>(speedList);
        JComboBoxUtil.setupComboBoxMaxRows(speedField);
        basic.addItem(new BeanEditItem(speedField,
            Bundle.getMessage("BlockSpeedColName"), Bundle.getMessage("BlockMaxSpeedText")));
        basic.addItem(new BeanEditItem(permissiveField,
            Bundle.getMessage("BlockPermColName"), Bundle.getMessage("BlockPermissiveText")));

        permissiveField.setSelected(bean.getPermissiveWorking());

        basic.addItem(new BeanEditItem(ghostField,
            Bundle.getMessage("BlockGhostColName"), Bundle.getMessage("BlockGhostText")));
        ghostField.setSelected(bean.getIsGhost());
        addBraking(basic);

        basic.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bean.setCurvature(curvatureField.getCurvature());
                String speed = (String) speedField.getSelectedItem();
                try {
                    bean.setBlockSpeed(speed);
                } catch (JmriException ex) {
                    jmri.util.swing.JmriJOptionPane.showMessageDialog(f, ex.getMessage() + "\n" + speed);
                    return;
                }
                if (!speedList.contains(speed) && !speed.contains("Global")) {
                    speedList.add(speed);
                }
                float len = (Float) lengthSpinner.getValue();
                bean.setLength( metricUi ? len * 10.0f : len * 25.4f);
                bean.setPermissiveWorking(permissiveField.isSelected());
                bean.setIsGhost(ghostField.isSelected());
                saveRoutes();
            }
        });
        basic.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                curvatureField.setCurvature(bean.getCurvature());
                String speed = bean.getBlockSpeed();
                if (!speedList.contains(speed)) {
                    speedList.add(speed);
                }
                speedField.setEditable(true);
                speedField.setSelectedItem(speed);
                updateLength();
                permissiveField.setSelected(bean.getPermissiveWorking());
                ghostField.setSelected(bean.getIsGhost());
                resetRoutes();
            }
        });
        bei.add(basic);
        return basic;
    }

    private void updateLength() {
        metricUi = cm.isSelected();
        lengthSpinner.setValue(metricUi ?  bean.getLengthCm() : bean.getLengthIn());
        routesModel.fireTableStructureChanged(); // the length column's units
    }

    private final RoutesModel routesModel = new RoutesModel();
    final JTable routesTable = new JTable(routesModel);
    private final JPanel brakingPanel = new JPanel();
    final java.util.Map<Block, JCheckBox> brakingBoxes = new java.util.LinkedHashMap<>();

    /**
     * The length of each way through the Block, where it differs from the
     * Block's own, on the Physical Details tab.
     *
     * @param panel the Physical Details tab
     */
    private void addRouteLengths(BeanItemPanel panel) {
        routesTable.setName("BlockRouteLengths"); // NOI18N
        routesTable.setRowSelectionAllowed(false);
        routesTable.setDefaultEditor(Object.class, new LengthEditor());
        routesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        JScrollPane scroll = new JScrollPane(routesTable);
        scroll.setPreferredSize(new java.awt.Dimension(360, 110));
        panel.addItem(new BeanEditItem(scroll,
            Bundle.getMessage("BlockRouteLengths"), Bundle.getMessage("BlockRouteLengthsText")));
    }

    /**
     * The neighbours towards which the Block is a braking section, on the
     * Physical Details tab.
     *
     * @param panel the Physical Details tab
     */
    private void addBraking(BeanItemPanel panel) {
        brakingPanel.setLayout(new BoxLayout(brakingPanel, BoxLayout.Y_AXIS));
        panel.addItem(new BeanEditItem(brakingPanel,
            Bundle.getMessage("BlockBraking"), Bundle.getMessage("BlockBrakingText")));
    }

    private void resetRoutes() {
        if (routesTable.isEditing()) {
            routesTable.getCellEditor().cancelCellEditing();
        }
        routesModel.load(bean);
        brakingBoxes.clear();
        brakingPanel.removeAll();
        for (Path p : bean.getPaths()) {
            Block towards = p.getBlock();
            if (towards != null && towards != bean && !brakingBoxes.containsKey(towards)) {
                JCheckBox box = new JCheckBox(Bundle.getMessage("BlockBrakingTowards", towards.getDisplayName()));
                box.setSelected(bean.isBrakingTowards(towards));
                brakingBoxes.put(towards, box);
                brakingPanel.add(box);
            }
        }
        if (brakingBoxes.isEmpty()) {
            brakingPanel.add(new JLabel(Bundle.getMessage("BlockNoNeighbours")));
        }
        brakingPanel.revalidate();
    }

    private void saveRoutes() {
        if (routesTable.isEditing() && !routesTable.getCellEditor().stopCellEditing()) {
            routesTable.getCellEditor().cancelCellEditing(); // not a length: the one before is kept
        }
        for (int row = 0; row < routesModel.getRowCount(); row++) {
            Block.WayThrough way = routesModel.ways.get(row);
            Float mm = routesModel.lengths.get(row);
            bean.setRouteLengthMm(way.getOneEnd(), way.getOtherEnd(), mm == null ? 0f : mm);
        }
        brakingBoxes.forEach((towards, box) -> bean.setBrakingTowards(towards, box.isSelected()));
    }

    /**
     * @param text a length as typed, in the user's locale
     * @return the length, or null if blank
     * @throws java.text.ParseException if it is not a length of 0 or more
     */
    @CheckForNull
    static Float parseLength(@Nonnull String text) throws java.text.ParseException {
        String t = text.trim();
        if (t.isEmpty()) {
            return null;
        }
        float v = jmri.util.IntlUtilities.floatValue(t);
        if (v < 0 || Float.isNaN(v) || Float.isInfinite(v)) {
            throw new java.text.ParseException(t, 0);
        }
        return v;
    }

    /**
     * Edits a length, refusing (with a red border) anything that isn't a
     * length or blank.
     */
    private static final class LengthEditor extends DefaultCellEditor {

        LengthEditor() {
            super(new JTextField());
            ((JTextField) getComponent()).setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                int row, int column) {
            JTextField field = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
            field.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            return field;
        }

        @Override
        public boolean stopCellEditing() {
            JTextField field = (JTextField) getComponent();
            try {
                parseLength(field.getText());
            } catch (java.text.ParseException e) {
                field.setBorder(new javax.swing.border.LineBorder(java.awt.Color.red));
                return false;
            }
            return super.stopCellEditing();
        }
    }

    /**
     * The ways through the Block and their lengths, held in mm and shown in
     * the units chosen for the Block's length. A blank length means the
     * Block's own.
     */
    private final class RoutesModel extends javax.swing.table.AbstractTableModel {

        private final java.util.List<Block.WayThrough> ways = new java.util.ArrayList<>();
        private final java.util.List<Float> lengths = new java.util.ArrayList<>();

        void load(Block block) {
            ways.clear();
            lengths.clear();
            for (Block.WayThrough w : block.getWaysThrough()) {
                ways.add(w);
                lengths.add(block.hasRouteLength(w.getOneEnd(), w.getOtherEnd())
                    ? block.getRouteLengthMm(w.getOneEnd(), w.getOtherEnd()) : null);
            }
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return ways.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int col) {
            return col == 0 ? Bundle.getMessage("BlockRouteBetween")
                : Bundle.getMessage("BlockRouteLength",
                    Bundle.getMessage(metricUi ? "BlockRouteUnitCm" : "BlockRouteUnitInches"));
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 1;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return ways.get(row).toString();
            }
            Float mm = lengths.get(row);
            if (mm == null) {
                return "";
            }
            java.text.NumberFormat f = java.text.NumberFormat.getNumberInstance();
            f.setMinimumFractionDigits(2);
            f.setMaximumFractionDigits(2);
            f.setGroupingUsed(false);
            return f.format(metricUi ? mm / 10.0f : mm / 25.4f);
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            Float v;
            try {
                v = parseLength(value == null ? "" : value.toString());
            } catch (java.text.ParseException e) {
                return; // the editor refuses anything else
            }
            lengths.set(row, (v == null || v == 0f) ? null : (metricUi ? v * 10.0f : v * 25.4f));
            fireTableCellUpdated(row, col);
        }
    }

    private NamedBeanComboBox<Sensor> sensorComboBox;

    BeanItemPanel sensor() {

        BeanItemPanel basic = new BeanItemPanel();
        basic.setName(Bundle.getMessage("BeanNameSensor"));

        sensorComboBox = new NamedBeanComboBox<>(
            InstanceManager.sensorManagerInstance(), bean.getSensor(), DisplayOptions.DISPLAYNAME);
        sensorComboBox.setAllowNull(true);
        JComboBoxUtil.setupComboBoxMaxRows(sensorComboBox);
        basic.addItem(new BeanEditItem(sensorComboBox,
            Bundle.getMessage("BeanNameSensor"), Bundle.getMessage("BlockAssignSensorText")));

        final SensorDebounceEditAction debounce = new SensorDebounceEditAction();
        //debounce.setBean(bean);
        debounce.sensorDebounce(basic);

        sensorComboBox.addActionListener((ActionEvent e) -> {
            debounce.setBean(sensorComboBox.getSelectedItem());
            debounce.resetDebounceItems(e);
        });

        basic.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LayoutBlock lBlk = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(bean);
                // If the block is related to a layoutblock then set the
                // sensor details there and allow that to propagate the changes down.
                if (lBlk != null) {
                    lBlk.validateSensor(sensorComboBox.getSelectedItemDisplayName(), null);
                } else {
                    bean.setSensor(sensorComboBox.getSelectedItemDisplayName());
                }
                debounce.saveDebounceItems(e);
            }
        });
        basic.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //From basic details
                sensorComboBox.setSelectedItem(bean.getSensor());
                debounce.setBean(bean.getSensor());
                debounce.resetDebounceItems(e);
            }
        });

        bei.add(basic);
        return basic;
    }

    // private static final Logger log = LoggerFactory.getLogger(BlockEditAction.class);

}
