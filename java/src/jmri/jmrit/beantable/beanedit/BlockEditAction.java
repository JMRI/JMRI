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
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Reporter;
import jmri.implementation.SignalSpeedMap;
import jmri.util.swing.JmriBeanComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an edit panel for a Block object
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class BlockEditAction extends BeanEditAction {

    private String noneText = Bundle.getMessage("BlockNone");
    private String gradualText = Bundle.getMessage("BlockGradual");
    private String tightText = Bundle.getMessage("BlockTight");
    private String severeText = Bundle.getMessage("BlockSevere");
    public String[] curveOptions = {noneText, gradualText, tightText, severeText};
    static final java.util.Vector<String> speedList = new java.util.Vector<String>();
    private final static Logger log = LoggerFactory.getLogger(BlockEditAction.class);

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
    public NamedBean getByUserName(String name) {
        return jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName(name);
    }

    JTextField userNameField = new JTextField(20);
    JmriBeanComboBox reporterComboBox;
    JCheckBox useCurrent = new JCheckBox();
    JTextArea commentField = new JTextArea(3, 30);
    JScrollPane commentFieldScroller = new JScrollPane(commentField);

    BeanItemPanel reporterDetails() {
        BeanItemPanel reporter = new BeanItemPanel();
        reporter.setName(Bundle.getMessage("BeanNameReporter"));

        reporterComboBox = new JmriBeanComboBox(InstanceManager.getDefault(jmri.ReporterManager.class), ((Block) bean).getReporter(), JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
        reporterComboBox.setFirstItemBlank(true);

        reporter.addItem(new BeanEditItem(reporterComboBox, Bundle.getMessage("BeanNameReporter"), Bundle.getMessage("BlockReporterText")));

        reporterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (reporterComboBox.getSelectedBean() != null) {
                    useCurrent.setEnabled(true);
                } else {
                    useCurrent.setEnabled(false);
                }
            }
        });

        reporter.addItem(new BeanEditItem(useCurrent, Bundle.getMessage("BlockReporterCurrent"), Bundle.getMessage("BlockUseCurrentText")));

        if (reporterComboBox.getSelectedBean() == null) {
            useCurrent.setEnabled(false);
        }

        reporter.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reporterComboBox.setSelectedBean(((Block) bean).getReporter());
                useCurrent.setSelected(((Block) bean).isReportingCurrent());
            }
        });

        reporter.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Block blk = (Block) bean;
                blk.setReporter((Reporter) reporterComboBox.getSelectedBean());
                blk.setReportingCurrent(useCurrent.isSelected());
            }
        });
        bei.add(reporter);
        if (jmri.InstanceManager.getNullableDefault(jmri.ReporterManager.class) == null) {
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

        defaultBlockSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + jmri.InstanceManager.getDefault(jmri.BlockManager.class).getDefaultSpeed());
        speedList.add(defaultBlockSpeedText);
        java.util.Vector<String> _speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames();
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

        permissiveField.setSelected(((Block) bean).getPermissiveWorking());

        basic.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Block blk = (Block) bean;
                String cName = (String) curvatureField.getSelectedItem();
                if (cName.equals(noneText)) {
                    blk.setCurvature(Block.NONE);
                } else if (cName.equals(gradualText)) {
                    blk.setCurvature(Block.GRADUAL);
                } else if (cName.equals(tightText)) {
                    blk.setCurvature(Block.TIGHT);
                } else if (cName.equals(severeText)) {
                    blk.setCurvature(Block.SEVERE);
                }

                String speed = (String) speedField.getSelectedItem();
                try {
                    blk.setBlockSpeed(speed);
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                    return;
                }
                if (!speedList.contains(speed) && !speed.contains("Global")) {
                    speedList.add(speed);
                }
                float len = 0.0f;
                len = (Float) lengthSpinner.getValue();
                if (inch.isSelected()) {
                    blk.setLength(len * 25.4f);
                } else {
                    blk.setLength(len * 10.0f);
                }
                blk.setPermissiveWorking(permissiveField.isSelected());
            }
        });
        basic.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Block blk = (Block) bean;
                lengthSpinner.setValue(((Block) bean).getLengthMm());

                if (blk.getCurvature() == Block.NONE) {
                    curvatureField.setSelectedItem(0);
                } else if (blk.getCurvature() == Block.GRADUAL) {
                    curvatureField.setSelectedItem(gradualText);
                } else if (blk.getCurvature() == Block.TIGHT) {
                    curvatureField.setSelectedItem(tightText);
                } else if (blk.getCurvature() == Block.SEVERE) {
                    curvatureField.setSelectedItem(severeText);
                }

                String speed = blk.getBlockSpeed();
                if (!speedList.contains(speed)) {
                    speedList.add(speed);
                }

                speedField.setEditable(true);
                speedField.setSelectedItem(speed);
                float len = 0.0f;
                if (inch.isSelected()) {
                    len = blk.getLengthIn();
                } else {
                    len = blk.getLengthCm();
                }
                lengthSpinner.setValue(len);
                permissiveField.setSelected(((Block) bean).getPermissiveWorking());
            }
        });
        bei.add(basic);
        return basic;
    }

    private void updateLength() {
        float len = 0.0f;
        Block blk = (Block) bean;
        if (inch.isSelected()) {
            len = blk.getLengthIn();
        } else {
            len = blk.getLengthCm();
        }
        lengthSpinner.setValue(len);
    }

    JmriBeanComboBox sensorComboBox;

    BeanItemPanel sensor() {

        BeanItemPanel basic = new BeanItemPanel();
        basic.setName(Bundle.getMessage("BeanNameSensor"));

        sensorComboBox = new JmriBeanComboBox(InstanceManager.sensorManagerInstance(), ((Block) bean).getSensor(), JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
        sensorComboBox.setFirstItemBlank(true);
        basic.addItem(new BeanEditItem(sensorComboBox, Bundle.getMessage("BeanNameSensor"), Bundle.getMessage("BlockAssignSensorText")));

      final SensorDebounceEditAction debounce = new SensorDebounceEditAction();
        //debounce.setBean(bean);
        debounce.sensorDebounce(basic);

        sensorComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                debounce.setBean(sensorComboBox.getSelectedBean());
                debounce.resetDebounceItems(e);
            }
        });

        basic.setSaveItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Block blk = (Block) bean;
                jmri.jmrit.display.layoutEditor.LayoutBlock lBlk = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getLayoutBlock(blk);
                //If the block is related to a layoutblock then set the sensor details there and allow that to propagate the changes down.
                if (lBlk != null) {
                    lBlk.validateSensor(sensorComboBox.getSelectedDisplayName(), null);
                } else {
                    blk.setSensor(sensorComboBox.getSelectedDisplayName());
                }
                debounce.saveDebounceItems(e);
            }
        });
        basic.setResetItem(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Block blk = (Block) bean;
                //From basic details
                sensorComboBox.setSelectedBean(blk.getSensor());
                debounce.setBean(blk.getSensor());
                debounce.resetDebounceItems(e);
            }
        });

        bei.add(basic);
        return basic;
    }

}
