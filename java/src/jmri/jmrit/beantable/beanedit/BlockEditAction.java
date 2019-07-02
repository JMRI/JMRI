package jmri.jmrit.beantable.beanedit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.Sensor;
import jmri.NamedBean.DisplayOptions;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.swing.NamedBeanComboBox;

/**
 * Provides an edit panel for a Block object.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class BlockEditAction extends BeanEditAction<Block> {

    private String noneText = Bundle.getMessage("BlockNone");
    private String gradualText = Bundle.getMessage("BlockGradual");
    private String tightText = Bundle.getMessage("BlockTight");
    private String severeText = Bundle.getMessage("BlockSevere");
    public String[] curveOptions = {noneText, gradualText, tightText, severeText};
    static final java.util.Vector<String> speedList = new java.util.Vector<String>();

    @Override
    public String helpTarget() {
        return "package.jmri.jmrit.beantable.BlockEdit";
    } //IN18N

    @Override
    protected void initPanels() {
        super.initPanels();
        sensor();
        reporterDetails();
        physicalDetails();
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameBlock");
    }

    @Override
    public Block getByUserName(String name) {
        return InstanceManager.getDefault(BlockManager.class).getByUserName(name);
    }

    JTextField userNameField = new JTextField(20);
    NamedBeanComboBox<Reporter> reporterComboBox;
    JCheckBox useCurrent = new JCheckBox();
    JTextArea commentField = new JTextArea(3, 30);
    JScrollPane commentFieldScroller = new JScrollPane(commentField);

    BeanItemPanel reporterDetails() {
        BeanItemPanel reporter = new BeanItemPanel();
        reporter.setName(Bundle.getMessage("BeanNameReporter"));

        reporterComboBox = new NamedBeanComboBox<>(InstanceManager.getDefault(ReporterManager.class), bean.getReporter(), DisplayOptions.DISPLAYNAME);
        reporterComboBox.setAllowNull(true);

        reporter.addItem(new BeanEditItem(reporterComboBox, Bundle.getMessage("BeanNameReporter"), Bundle.getMessage("BlockReporterText")));

        reporterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (reporterComboBox.getSelectedItem() != null) {
                    useCurrent.setEnabled(true);
                } else {
                    useCurrent.setEnabled(false);
                }
            }
        });

        reporter.addItem(new BeanEditItem(useCurrent, Bundle.getMessage("BlockReporterCurrent"), Bundle.getMessage("BlockUseCurrentText")));

        if (reporterComboBox.getSelectedItem() == null) {
            useCurrent.setEnabled(false);
        }

        reporter.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reporterComboBox.setSelectedItem(bean.getReporter());
                useCurrent.setSelected(bean.isReportingCurrent());
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
    JComboBox<String> curvatureField = new JComboBox<String>(curveOptions);
    JCheckBox permissiveField = new JCheckBox();
    JComboBox<String> speedField;

    JRadioButton inch = new JRadioButton(Bundle.getMessage("LengthInches"));
    JRadioButton cm = new JRadioButton(Bundle.getMessage("LengthCentimeters"));

    String defaultBlockSpeedText;

    BeanItemPanel physicalDetails() {

        defaultBlockSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + InstanceManager.getDefault(BlockManager.class).getDefaultSpeed());
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
        lengthSpinner.setModel(
                            new SpinnerNumberModel(Float.valueOf(0f), Float.valueOf(0f), Float.valueOf(1000f), Float.valueOf(0.01f)));
        lengthSpinner.setEditor(new JSpinner.NumberEditor(lengthSpinner, "###0.00"));
        lengthSpinner.setPreferredSize(new JTextField(8).getPreferredSize());
        lengthSpinner.setValue(Float.valueOf(0f)); // reset from possible previous use
        basic.addItem(new BeanEditItem(lengthSpinner, Bundle.getMessage("BlockLengthColName"), Bundle.getMessage("BlockLengthText")));

        ButtonGroup rg = new ButtonGroup();
        rg.add(inch);
        rg.add(cm);

        JPanel p = new JPanel();
        p.add(inch);
        p.add(cm);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        inch.setSelected(true);

        inch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cm.setSelected(!inch.isSelected());
                updateLength();
            }
        });
        cm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inch.setSelected(!cm.isSelected());
                updateLength();
            }
        });

        basic.addItem(new BeanEditItem(p, Bundle.getMessage("BlockLengthUnits"), Bundle.getMessage("BlockLengthUnitsText")));
        basic.addItem(new BeanEditItem(curvatureField, Bundle.getMessage("BlockCurveColName"), ""));
        basic.addItem(new BeanEditItem(speedField = new JComboBox<String>(speedList), Bundle.getMessage("BlockSpeedColName"), Bundle.getMessage("BlockMaxSpeedText")));
        basic.addItem(new BeanEditItem(permissiveField, Bundle.getMessage("BlockPermColName"), Bundle.getMessage("BlockPermissiveText")));

        permissiveField.setSelected(bean.getPermissiveWorking());

        basic.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cName = (String) curvatureField.getSelectedItem();
                if (cName.equals(noneText)) {
                    bean.setCurvature(Block.NONE);
                } else if (cName.equals(gradualText)) {
                    bean.setCurvature(Block.GRADUAL);
                } else if (cName.equals(tightText)) {
                    bean.setCurvature(Block.TIGHT);
                } else if (cName.equals(severeText)) {
                    bean.setCurvature(Block.SEVERE);
                }

                String speed = (String) speedField.getSelectedItem();
                try {
                    bean.setBlockSpeed(speed);
                } catch (JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                    return;
                }
                if (!speedList.contains(speed) && !speed.contains("Global")) {
                    speedList.add(speed);
                }
                float len = 0.0f;
                len = (Float) lengthSpinner.getValue();
                if (inch.isSelected()) {
                    bean.setLength(len * 25.4f);
                } else {
                    bean.setLength(len * 10.0f);
                }
                bean.setPermissiveWorking(permissiveField.isSelected());
            }
        });
        basic.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lengthSpinner.setValue(bean.getLengthMm());

                if (bean.getCurvature() == Block.NONE) {
                    curvatureField.setSelectedItem(0);
                } else if (bean.getCurvature() == Block.GRADUAL) {
                    curvatureField.setSelectedItem(gradualText);
                } else if (bean.getCurvature() == Block.TIGHT) {
                    curvatureField.setSelectedItem(tightText);
                } else if (bean.getCurvature() == Block.SEVERE) {
                    curvatureField.setSelectedItem(severeText);
                }

                String speed = bean.getBlockSpeed();
                if (!speedList.contains(speed)) {
                    speedList.add(speed);
                }

                speedField.setEditable(true);
                speedField.setSelectedItem(speed);
                float len = 0.0f;
                if (inch.isSelected()) {
                    len = bean.getLengthIn();
                } else {
                    len = bean.getLengthCm();
                }
                lengthSpinner.setValue(len);
                permissiveField.setSelected(bean.getPermissiveWorking());
            }
        });
        bei.add(basic);
        return basic;
    }

    private void updateLength() {
        float len = 0.0f;
        if (inch.isSelected()) {
            len = bean.getLengthIn();
        } else {
            len = bean.getLengthCm();
        }
        lengthSpinner.setValue(len);
    }

    NamedBeanComboBox<Sensor> sensorComboBox;

    BeanItemPanel sensor() {

        BeanItemPanel basic = new BeanItemPanel();
        basic.setName(Bundle.getMessage("BeanNameSensor"));

        sensorComboBox = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance(), bean.getSensor(), DisplayOptions.DISPLAYNAME);
        sensorComboBox.setAllowNull(true);
        basic.addItem(new BeanEditItem(sensorComboBox, Bundle.getMessage("BeanNameSensor"), Bundle.getMessage("BlockAssignSensorText")));

      final SensorDebounceEditAction debounce = new SensorDebounceEditAction();
        //debounce.setBean(bean);
        debounce.sensorDebounce(basic);

        sensorComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                debounce.setBean(sensorComboBox.getSelectedItem());
                debounce.resetDebounceItems(e);
            }
        });

        basic.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LayoutBlock lBlk = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(bean);
                //If the block is related to a layoutblock then set the sensor details there and allow that to propagate the changes down.
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
