package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;

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
            }
        });
        bei.add(basic);
        return basic;
    }

    private void updateLength() {
        metricUi = cm.isSelected();
        lengthSpinner.setValue(metricUi ?  bean.getLengthCm() : bean.getLengthIn());
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

    // private final static Logger log = LoggerFactory.getLogger(BlockEditAction.class);

}
