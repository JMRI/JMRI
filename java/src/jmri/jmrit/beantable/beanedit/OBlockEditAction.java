package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;

import javax.swing.*;

import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.beantable.block.BlockCurvatureJComboBox;
import jmri.jmrit.beantable.oblock.TableFrames;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.swing.NamedBeanComboBox;
import jmri.util.swing.JComboBoxUtil;

/**
 * Provides a tabbed beanedit panel for an OBlock object.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Egbert Broerse Copyright (C) 2020
 */
public class OBlockEditAction extends BeanEditAction<OBlock> {

    static final java.util.Vector<String> speedList = new java.util.Vector<>();
    private String tabName = Bundle.getMessage("BeanNameOBlock");
    NamedBeanComboBox<Reporter> reporterComboBox;
    JCheckBox useCurrent = new JCheckBox();
    TableFrames.BlockPathJPanel blockPathPanel;
    NamedBeanComboBox<Sensor> sensorComboBox;
    NamedBeanComboBox<Sensor> errorSensorComboBox;

    public OBlockEditAction(ActionEvent ae) {
        super();
        if (ae != null) {
            tabName = ae.getActionCommand();
        }
    }

    @Override
    public String helpTarget() {
        return "package.jmri.jmrit.beantable.OBlockTable";
    } // NOI18N

    public void setTablePanel(TableFrames.BlockPathJPanel panel){
        blockPathPanel = panel;
    }

    @Override
    protected void initPanels() {
        super.initPanels();
        sensor();
        paths();
        reporterDetails();
        physicalDetails();
    }

    @Override
    public OBlock getByUserName(String name) {
        return InstanceManager.getDefault(OBlockManager.class).getByUserName(name);
    }

    BeanItemPanel sensor() {
        BeanItemPanel basic = new BeanItemPanel();
        basic.setName(Bundle.getMessage("BeanNameSensors"));

        sensorComboBox = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance(), bean.getSensor(), DisplayOptions.DISPLAYNAME);
        sensorComboBox.setAllowNull(true);
        JComboBoxUtil.setupComboBoxMaxRows(sensorComboBox);
        basic.addItem(new BeanEditItem(sensorComboBox, Bundle.getMessage("BeanNameSensor"), Bundle.getMessage("BlockAssignSensorText")));

        errorSensorComboBox = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance(), bean.getSensor(), DisplayOptions.DISPLAYNAME);
        errorSensorComboBox.setAllowNull(true);
        JComboBoxUtil.setupComboBoxMaxRows(errorSensorComboBox);
        basic.addItem(new BeanEditItem(errorSensorComboBox, Bundle.getMessage("ErrorSensorCol"), Bundle.getMessage("BlockAssignErrorSensorText")));

        final SensorDebounceEditAction debounce = new SensorDebounceEditAction();
        debounce.sensorDebounce(basic);

        sensorComboBox.addActionListener(e -> {
            debounce.setBean(sensorComboBox.getSelectedItem());
            debounce.resetDebounceItems(e);
        });
        basic.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sensorComboBox.getSelectedItem() == null) {
                    bean.setSensor(null);
                } else {
                    bean.setSensor(sensorComboBox.getSelectedItem().getDisplayName());
                }
                if (errorSensorComboBox.getSelectedItem() == null) {
                    bean.setErrorSensor(null);
                } else {
                    bean.setErrorSensor(errorSensorComboBox.getSelectedItem().getDisplayName());
                }
            }
        });
        basic.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //From basic details
                sensorComboBox.setSelectedItem(bean.getSensor());
                errorSensorComboBox.setSelectedItem(bean.getErrorSensor());
                debounce.setBean(bean.getSensor());
                debounce.resetDebounceItems(e);
            }
        });

        bei.add(basic);
        return basic;
    }

    BeanItemPanel paths() {
        BeanItemPanel paths = new BeanItemPanel();
        String name = Bundle.getMessage("TitlePaths");
        paths.setName(name);

        paths.addItem(new BeanEditItem(blockPathPanel, Bundle.getMessage("PathTableLabel", bean.getDisplayName()), null));
        // includes Add Path button

        paths.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //clear the table?
            }
        });

        bei.add(paths);
        if (InstanceManager.getNullableDefault(OBlockManager.class) == null) {
            setEnabled(false);
        }
        if (name.equals(tabName)) {
            setSelectedComponent(paths);
        }
        return paths;
    }

    BeanItemPanel reporterDetails() {
        BeanItemPanel reporter = new BeanItemPanel();
        reporter.setName(Bundle.getMessage("BeanNameReporter"));

        reporterComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(ReporterManager.class), bean.getReporter(), DisplayOptions.DISPLAYNAME);
        reporterComboBox.setAllowNull(true);
        JComboBoxUtil.setupComboBoxMaxRows(reporterComboBox);

        reporter.addItem(new BeanEditItem(reporterComboBox, Bundle.getMessage("BeanNameReporter"), Bundle.getMessage("BlockReporterText")));

        reporterComboBox.addActionListener(e -> useCurrent.setEnabled(reporterComboBox.getSelectedItem() != null));

        reporter.addItem(new BeanEditItem(useCurrent, Bundle.getMessage("BlockReporterCurrent"), Bundle.getMessage("BlockUseCurrentText")));

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

    JSpinner lengthSpinner = new JSpinner(); // 2 digit decimal format field, initialized later as instance
    private final BlockCurvatureJComboBox curvatureField = new BlockCurvatureJComboBox();
    JCheckBox permissiveField = new JCheckBox();
    JComboBox<String> speedField;

    JRadioButton inch = new JRadioButton(Bundle.getMessage("LengthInches"));
    JRadioButton cm = new JRadioButton(Bundle.getMessage("LengthCentimeters"));

    String defaultBlockSpeedText;

    BeanItemPanel physicalDetails() {
        defaultBlockSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + InstanceManager.getDefault(BlockManager.class).getDefaultSpeed());
        speedList.add(defaultBlockSpeedText);
        java.util.Vector<String> _speedMap = InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames();
        for (String s : _speedMap) {
            if (!speedList.contains(s)) {
                speedList.add(s);
            }
        }
        BeanItemPanel basic = new BeanItemPanel();
        basic.setName(Bundle.getMessage("BlockPhysicalProperties"));

        basic.addItem(new BeanEditItem(null, null, Bundle.getMessage("BlockPropertiesText")));
        lengthSpinner.setModel(
                            new SpinnerNumberModel(Float.valueOf(0f), Float.valueOf(0f), Float.valueOf(1000f), Float.valueOf(0.01f)));
        lengthSpinner.setEditor(new JSpinner.NumberEditor(lengthSpinner, "###0.00"));
        lengthSpinner.setPreferredSize(new JTextField(8).getPreferredSize());
        lengthSpinner.setValue(0f); // reset from possible previous use
        basic.addItem(new BeanEditItem(lengthSpinner, Bundle.getMessage("BlockLengthColName"), Bundle.getMessage("BlockLengthText")));

        ButtonGroup rg = new ButtonGroup();
        rg.add(inch);
        rg.add(cm);

        JPanel p = new JPanel();
        p.add(inch);
        p.add(cm);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        inch.setSelected(true);

        inch.addActionListener(e -> {
            cm.setSelected(!inch.isSelected());
            updateLength();
        });
        cm.addActionListener(e -> {
            inch.setSelected(!cm.isSelected());
            updateLength();
        });

        speedField = new JComboBox<>(speedList);
        
        basic.addItem(new BeanEditItem(p, Bundle.getMessage("BlockLengthUnits"), Bundle.getMessage("BlockLengthUnitsText")));
        basic.addItem(new BeanEditItem(curvatureField, Bundle.getMessage("BlockCurveColName"), ""));
        basic.addItem(new BeanEditItem(speedField, Bundle.getMessage("BlockSpeedColName"), Bundle.getMessage("BlockMaxSpeedText")));
        basic.addItem(new BeanEditItem(permissiveField, Bundle.getMessage("BlockPermColName"), Bundle.getMessage("BlockPermissiveText")));

        permissiveField.setSelected(bean.getPermissiveWorking());

        basic.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bean.setCurvature(curvatureField.getCurvature());

                String speed = (String) speedField.getSelectedItem();
                try {
                    bean.setBlockSpeed(speed);
                } catch (JmriException ex) {
                    JOptionPane.showMessageDialog(f, ex.getMessage() + "\n" + speed);
                    return;
                }
                if (speed != null && !speedList.contains(speed) && !speed.contains("Global")) {
                    speedList.add(speed);
                }
                float len = (Float) lengthSpinner.getValue();
                bean.setLength( inch.isSelected() ? (len * 25.4f) : (len * 10.0f) );
                bean.setPermissiveWorking(permissiveField.isSelected());
            }
        });
        basic.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lengthSpinner.setValue(bean.getLengthMm());

                curvatureField.setCurvature(bean.getCurvature());

                String speed = bean.getBlockSpeed();
                if (!speedList.contains(speed)) {
                    speedList.add(speed);
                }

                speedField.setEditable(true);
                speedField.setSelectedItem(speed);
                updateLength();
                permissiveField.setSelected(bean.getPermissiveWorking());
            }
        });
        bei.add(basic);
        return basic;
    }

    private void updateLength() {
        lengthSpinner.setValue(inch.isSelected() ? bean.getLengthIn() : bean.getLengthCm());
    }

    // private final static Logger log = LoggerFactory.getLogger(OBlockEditAction.class);

}
