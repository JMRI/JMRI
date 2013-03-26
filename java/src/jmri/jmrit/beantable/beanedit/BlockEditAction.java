// StatusPanel.java

package jmri.jmrit.beantable.beanedit;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Block;
import jmri.util.swing.JmriBeanComboBox;
import jmri.Sensor;
import jmri.Reporter;

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.*;

/**
 * Provides an edit panel for a block object
 *
 * @author	Kevin Dickerson Copyright (C) 2011
 * @version	$Revision: 19923 $
 */
public class BlockEditAction extends BeanEditAction {
    
    private String noneText = Bundle.getMessage("BlockNone");
	private String gradualText = Bundle.getMessage("BlockGradual");
	private String tightText = Bundle.getMessage("BlockTight");
	private String severeText = Bundle.getMessage("BlockSevere");
	public String[] curveOptions = {noneText, gradualText, tightText, severeText};
    static final java.util.Vector<String> speedList = new java.util.Vector<String>();
    
    private DecimalFormat twoDigit = new DecimalFormat("0.00");
    
    public String helpTarget() { return "package.jmri.jmrit.beantable.BlockEdit"; } //IN18N
    
    @Override
    protected void initPanels(){
        super.initPanels();
        sensor();
        reporterDetails();
        properties();
    }
    
    public String getBeanType() { return Bundle.getMessage("BeanNameBlock"); }
    public NamedBean getBySystemName(String name) { return jmri.InstanceManager.getDefault(jmri.BlockManager.class).getBySystemName(name);}
    public NamedBean getByUserName(String name) { return jmri.InstanceManager.getDefault(jmri.BlockManager.class).getByUserName(name);}
    
    JTextField userNameField = new JTextField(20);
    JmriBeanComboBox reporterField;
    JCheckBox useCurrent = new JCheckBox();
    JTextArea commentField = new JTextArea(3,30);
    JScrollPane commentFieldScroller = new JScrollPane(commentField);
    
    BeanItemPanel reporterDetails(){
        BeanItemPanel reporter = new BeanItemPanel();
        reporter.setName(Bundle.getMessage("BeanNameReporter"));
        
        reporterField = new JmriBeanComboBox(InstanceManager.reporterManagerInstance(), ((Block)bean).getReporter(), JmriBeanComboBox.DISPLAYNAME);
        reporterField.setFirstItemBlank(true);

        reporter.addItem(new BeanEditItem(reporterField, Bundle.getMessage("BeanNameReporter"), Bundle.getMessage("BlockReporterText")));

        reporterField.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(reporterField.getSelectedBean()!=null)
                    useCurrent.setEnabled(true);
                else
                    useCurrent.setEnabled(false);
            }
        });
        

        reporter.addItem(new BeanEditItem(useCurrent, Bundle.getMessage("BlockReporterCurrent"), Bundle.getMessage("BlockUseCurrentText")));

        if(reporterField.getSelectedBean()==null){
            useCurrent.setEnabled(false);
        }
        
        reporter.setResetItem(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                reporterField.setSelectedBean(((Block)bean).getReporter());
                useCurrent.setSelected(((Block)bean).isReportingCurrent());
            }
        });
        
        reporter.setSaveItem(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                Block blk = (Block) bean;
                blk.setReporter((Reporter)reporterField.getSelectedBean());
                blk.setReportingCurrent(useCurrent.isSelected());
            }
        });
        bei.add(reporter);
        if (jmri.InstanceManager.getDefault(jmri.ReporterManager.class)==null) {
            setEnabled(false);
        }
        return reporter;
    }
    
    JTextField lengthField = new JTextField(20);
    JComboBox curvatureField = new JComboBox(curveOptions);
    JCheckBox permissiveField = new JCheckBox();
    JComboBox speedField;
    
    JRadioButton inch = new JRadioButton(Bundle.getMessage("LengthInches"));
    JRadioButton cm = new JRadioButton(Bundle.getMessage("LengthCentimeters"));

    String defaultBlockSpeedText;
    
    BeanItemPanel properties(){
        
        defaultBlockSpeedText = (Bundle.getMessage("UseGlobal") + " " + jmri.InstanceManager.getDefault(jmri.BlockManager.class).getDefaultSpeed());
        speedList.add(defaultBlockSpeedText);
        java.util.Vector<String> _speedMap = jmri.implementation.SignalSpeedMap.getMap().getValidSpeedNames();
        for(int i = 0; i<_speedMap.size(); i++){
            if (!speedList.contains(_speedMap.get(i))){
                speedList.add(_speedMap.get(i));
            }
        }
        BeanItemPanel basic = new BeanItemPanel();
        basic.setName(Bundle.getMessage("Properties"));

        basic.addItem(new BeanEditItem(null, null, Bundle.getMessage("BlockPropertiesText")));
        basic.addItem(new BeanEditItem(lengthField, Bundle.getMessage("BlockLengthColName"), Bundle.getMessage("BlockLengthText")));
        
        lengthField.addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }
            public void keyReleased(KeyEvent keyEvent) {
                String text = lengthField.getText();
                if (!validateNumericalInput(text)){
                    String msg = java.text.MessageFormat.format(Bundle.getMessage("ShouldBeNumber"), new Object[] { Bundle.getMessage("BlockLengthColName") });
                    jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).showInfoMessage(Bundle.getMessage("ErrorTitle"), msg, "Block Details", "length", false, false);
                }
            }
            public void keyTyped(KeyEvent keyEvent) {
            }
        });
        

        ButtonGroup rg = new ButtonGroup();
        rg.add(inch);
        rg.add(cm);
        
        JPanel p = new JPanel();
        p.add(inch);
        p.add(cm);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        inch.setSelected(true);
        
		inch.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
                    cm.setSelected(!inch.isSelected());
                    updateLength();
				}
			});
		cm.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
                    inch.setSelected(!cm.isSelected());
                    updateLength();
                }
			});
        
        basic.addItem(new BeanEditItem(p, Bundle.getMessage("BlockLengthUnits"), Bundle.getMessage("BlockLengthUnitsText")));
        basic.addItem(new BeanEditItem(curvatureField, Bundle.getMessage("BlockCurveColName"), ""));
        basic.addItem(new BeanEditItem(speedField = new JComboBox(speedList), Bundle.getMessage("BlockSpeedColName"), Bundle.getMessage("BlockMaxSpeedText")));
        basic.addItem(new BeanEditItem(permissiveField, Bundle.getMessage("BlockPermColName"), Bundle.getMessage("BlockPermissiveText")));

        permissiveField.setSelected(((Block)bean).getPermissiveWorking());

        basic.setSaveItem(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                Block blk = (Block) bean;
                String cName = (String)curvatureField.getSelectedItem();
                if (cName.equals(noneText)) blk.setCurvature(Block.NONE);
                else if (cName.equals(gradualText)) blk.setCurvature(Block.GRADUAL);
                else if (cName.equals(tightText)) blk.setCurvature(Block.TIGHT);
                else if (cName.equals(severeText)) blk.setCurvature(Block.SEVERE);
                
                String speed = (String)speedField.getSelectedItem();
                try {
                    blk.setBlockSpeed(speed);
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                    return;
                }
                if (!speedList.contains(speed) && !speed.contains(Bundle.getMessage("UseGlobal"))){
                    speedList.add(speed);
                }
                String length = lengthField.getText();
                float len = Float.valueOf(length).floatValue();
                if (inch.isSelected()) 
                    blk.setLength(len*25.4f);
                else
                    blk.setLength(len*10.0f);
                blk.setPermissiveWorking(permissiveField.isSelected());
            }
        });
        basic.setResetItem(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                Block blk = (Block) bean;
                lengthField.setText(""+((Block)bean).getLengthMm());

                if (blk.getCurvature()==Block.NONE) curvatureField.setSelectedItem(0);
                else if (blk.getCurvature()==Block.GRADUAL) curvatureField.setSelectedItem(gradualText);
                else if (blk.getCurvature()==Block.TIGHT) curvatureField.setSelectedItem(tightText);
                else if (blk.getCurvature()==Block.SEVERE) curvatureField.setSelectedItem(severeText);
                
                String speed = blk.getBlockSpeed();
                if(!speedList.contains(speed)){
                    speedList.add(speed);
                }

                speedField.setEditable(true);
                speedField.setSelectedItem(speed);
                double len = 0.0;
                if (inch.isSelected())
                    len = blk.getLengthIn();
                else 
                    len = blk.getLengthCm();
                lengthField.setText(twoDigit.format(len));
                permissiveField.setSelected(((Block)bean).getPermissiveWorking());
            }
        });
        bei.add(basic);
        return basic;
    }
    
    private void updateLength(){
        double len = 0.0;
        Block blk = (Block) bean;
        if (inch.isSelected())
            len = blk.getLengthIn();
        else 
            len = blk.getLengthCm();
        lengthField.setText(twoDigit.format(len));
    }
    
    JmriBeanComboBox sensorField;
    JTextField sensorDebounceInactiveField = new JTextField(5);
    JTextField sensorDebounceActiveField = new JTextField(5);
    JCheckBox sensorDebounceGlobalCheck = new JCheckBox();//"Use global Debounce"/*rb.getString("OccupancySensorUseGlobal")*/);
    
    BeanItemPanel sensor() {

        BeanItemPanel basic = new BeanItemPanel();
        basic.setName(Bundle.getMessage("BeanNameSensor"));

        sensorField = new JmriBeanComboBox(InstanceManager.sensorManagerInstance(), ((Block)bean).getSensor(), JmriBeanComboBox.DISPLAYNAME);
        sensorField.setFirstItemBlank(true);
        basic.addItem(new BeanEditItem(sensorField, Bundle.getMessage("BeanNameSensor"), Bundle.getMessage("BlockAssignSensorText")));
        
        sensorField.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(sensorField.getSelectedBean()==null){
                    sensorDebounceGlobalCheck.setEnabled(false);
                    sensorDebounceInactiveField.setEnabled(false);
                    sensorDebounceActiveField.setEnabled(false);
                } else {
                    sensorDebounceGlobalCheck.setEnabled(true);
                    sensorDebounceInactiveField.setEnabled(true);
                    sensorDebounceActiveField.setEnabled(true);
                }
            }
        });
        
        sensorDebounceGlobalCheck.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(sensorDebounceGlobalCheck.isSelected()){
                    sensorDebounceInactiveField.setEnabled(false);
                    sensorDebounceActiveField.setEnabled(false);
                } else {
                    sensorDebounceInactiveField.setEnabled(true);
                    sensorDebounceActiveField.setEnabled(true);
                }
            }
        });

        sensorDebounceInactiveField.addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }
            public void keyReleased(KeyEvent keyEvent) {
                String text = sensorDebounceInactiveField.getText();
                if (!validateNumericalInput(text)){
                    String msg = java.text.MessageFormat.format(Bundle.getMessage("ShouldBeNumber"), new Object[] { Bundle.getMessage("SensorInActiveDebounce") });
                    jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).showInfoMessage(Bundle.getMessage("ErrorTitle"), msg, "Block Details", "Inactive", false, false);
                }
            }
            public void keyTyped(KeyEvent keyEvent) {
            }
        });
        
        sensorDebounceActiveField.addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }
            public void keyReleased(KeyEvent keyEvent) {
                String text = sensorDebounceActiveField.getText();
                if (!validateNumericalInput(text)){
                    String msg = java.text.MessageFormat.format(Bundle.getMessage("ShouldBeNumber"), new Object[] { Bundle.getMessage("SensorActiveDebounce") });
                    jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).showInfoMessage(Bundle.getMessage("ErrorTitle"), msg, "Block Details", "Active", false, false);
                }
            }
            public void keyTyped(KeyEvent keyEvent) {
            }
        });
        
        basic.addItem(new BeanEditItem(null, null, Bundle.getMessage("SensorDebounceText")));
        basic.addItem(new BeanEditItem(sensorDebounceGlobalCheck,Bundle.getMessage("SensorDebounceUseGlobalText"), null));
        basic.addItem(new BeanEditItem(sensorDebounceInactiveField,Bundle.getMessage("SensorInActiveDebounce"), Bundle.getMessage("SensorInActiveDebounceText")));
        basic.addItem(new BeanEditItem(sensorDebounceActiveField,Bundle.getMessage("SensorActiveDebounce"), Bundle.getMessage("SensorActiveDebounceText")));
        
        basic.setSaveItem(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                Block blk = (Block) bean;
                jmri.jmrit.display.layoutEditor.LayoutBlock lBlk = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class).getLayoutBlock(blk);
                //If the block is related to a layoutblock then set the sensor details there and allow that to propergate the changes down.
                if(lBlk!=null)
                    lBlk.validateSensor(sensorField.getSelectedDisplayName(), null);
                else
                    blk.setSensor(sensorField.getSelectedDisplayName());
                if(sensorField.getSelectedBean()!=null){
                    Sensor sen = (Sensor) sensorField.getSelectedBean();
                    
                    String timeVal = sensorDebounceActiveField.getText();
                    int time = Integer.valueOf(timeVal).intValue();
                    sen.setSensorDebounceGoingActiveTimer(time);
                    
                    timeVal = sensorDebounceInactiveField.getText();
                    time = Integer.valueOf(timeVal).intValue();
                    sen.setSensorDebounceGoingInActiveTimer(time);
                    sen.useDefaultTimerSettings(sensorDebounceGlobalCheck.isSelected());
                }
            }
        });
        basic.setResetItem(new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                Block blk = (Block)bean;
                //From basic details
                sensorField.setSelectedBean(blk.getSensor());
                if(sensorField.getSelectedBean()==null){
                    sensorDebounceGlobalCheck.setEnabled(false);
                    sensorDebounceInactiveField.setEnabled(false);
                    sensorDebounceActiveField.setEnabled(false);
                } else {
                    Sensor sen = (Sensor) sensorField.getSelectedBean();
                    sensorDebounceGlobalCheck.setSelected(sen.useDefaultTimerSettings());
                    sensorDebounceActiveField.setText(""+sen.getSensorDebounceGoingActiveTimer());
                    sensorDebounceInactiveField.setText(""+sen.getSensorDebounceGoingInActiveTimer());
                }
            }
        });

        bei.add(basic);
        return basic;
    }

}