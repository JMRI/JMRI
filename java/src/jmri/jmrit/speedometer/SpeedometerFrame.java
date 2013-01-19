// SpeedometerFrame.java

package jmri.jmrit.speedometer;

import java.io.FileNotFoundException;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.SensorIcon;
import jmri.NamedBeanHandle;
import java.awt.FlowLayout;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.Application;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.ProcessingInstruction;

/**
 * Frame providing access to a speedometer.
 * <P>
 * This contains very simple debouncing logic:
 * <UL>
 * <LI>The clock starts when the "start" sensor makes
 * the correct transition.
 * <LI>When a "stop" sensor makes the correct transition,
 * the speed is computed and displayed.
 * </UL>
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2004, 2007
 * @author      Adapted for metric system - S.K. Bosch
 * @author      Matthew Harris  Copyright (c) 2011
 * @version	$Revision$
 */
public class SpeedometerFrame extends jmri.util.JmriJFrame {

    private static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.speedometer.SpeedometerBundle");

    final String blank = "       ";
    JTextField startSensor = new JTextField(5);
    javax.swing.ButtonGroup startGroup 		= new javax.swing.ButtonGroup();
    javax.swing.JRadioButton startOnEntry  	= new javax.swing.JRadioButton(rb.getString("RadioButtonEntry"));
    javax.swing.JRadioButton startOnExit    = new javax.swing.JRadioButton(rb.getString("RadioButtonExit"));

    JTextField stopSensor1 = new JTextField(5);
    javax.swing.ButtonGroup stopGroup1 		= new javax.swing.ButtonGroup();
    javax.swing.JRadioButton stopOnEntry1  	= new javax.swing.JRadioButton(rb.getString("RadioButtonEntry"));
    javax.swing.JRadioButton stopOnExit1    = new javax.swing.JRadioButton(rb.getString("RadioButtonExit"));

    public JTextField stopSensor2 = new JTextField(5);
    javax.swing.ButtonGroup stopGroup2 		= new javax.swing.ButtonGroup();
    javax.swing.JRadioButton stopOnEntry2  	= new javax.swing.JRadioButton(rb.getString("RadioButtonEntry"));
    javax.swing.JRadioButton stopOnExit2    = new javax.swing.JRadioButton(rb.getString("RadioButtonExit"));

    JTextField distance1 = new JTextField(5);
    JTextField distance2 = new JTextField(5);

    JButton dimButton = new JButton("");   // content will be set to English during startup
    JButton startButton = new JButton(rb.getString("ButtonStart"));

    JLabel text1 = new JLabel(rb.getString("Distance1English"));
    JLabel text2 = new JLabel(rb.getString("Distance2English"));
    JLabel text3 = new JLabel(rb.getString("Speed1English"));
    JLabel text4 = new JLabel(rb.getString("Speed2English"));

    JButton clearButton = new JButton(rb.getString("ButtonClear"));

    JLabel result1 = new JLabel(blank);
    JLabel time1 = new JLabel(blank);
    JLabel result2 = new JLabel(blank);
    JLabel time2 = new JLabel(blank);

    JButton saveButton = new JButton(rb.getString("ButtonSave"));

    SensorIcon startSensorIcon;
    SensorIcon stopSensorIcon1;
    SensorIcon stopSensorIcon2;

    public void setInputs(String start, String stop1, String stop2, String d1, String d2) {
        startSensor.setText(start);
        stopSensor1.setText(stop1);
        stopSensor2.setText(stop2);
        distance1.setText(d1);
        distance2.setText(d2);
    }

    public final void setInputBehavior(boolean startOnEntry, boolean stopOnEntry1, boolean stopOnEntry2) {
        this.startOnEntry.setSelected(startOnEntry);
        this.startOnExit.setSelected(!startOnEntry);
        this.stopOnEntry1.setSelected(stopOnEntry1);
        this.stopOnExit1.setSelected(!stopOnEntry1);
        this.stopOnEntry2.setSelected(stopOnEntry2);
        this.stopOnExit2.setSelected(!stopOnEntry2);
    }

    public final void setUnitsMetric(boolean metric) {
        if (dim != metric) {
            dim();
        }
    }
    
    public SpeedometerFrame() {
        super(false, false);
        //Install the named bean handle if not installed, which can happen if opened from DP3
        if (jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class)==null){
            jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
        }
        
        setInputBehavior(true,true,true);

        startGroup.add(startOnEntry);
        startGroup.add(startOnExit);
        stopGroup1.add(stopOnEntry1);
        stopGroup1.add(stopOnExit1);
        stopGroup2.add(stopOnEntry2);
        stopGroup2.add(stopOnExit2);

        // general GUI config
        setTitle(rb.getString("TitleSpeedometer"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add items to GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel(rb.getString("LabelSensor")));
        startSensor.setToolTipText(rb.getString("TooltipStartSensor"));
        pane1.add(startSensor);
        pane1.add(new JLabel(rb.getString("LabelStartSensor")));
        pane1.add(startOnEntry);
        pane1.add(startOnExit);
        startSensorIcon = new SensorIcon(new jmri.jmrit.display.panelEditor.PanelEditor());
        setupIconMap(startSensorIcon);
        startSensorIcon.setToolTipText(rb.getString("TooltipStartSensorIcon"));
        pane1.add(startSensorIcon);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        pane2.add(new JLabel(rb.getString("LabelSensor")));
        stopSensor1.setToolTipText(rb.getString("TooltipStopSensor1"));
        pane2.add(stopSensor1);
        pane2.add(new JLabel(rb.getString("LabelStopSensor1")));
        pane2.add(stopOnEntry1);
        pane2.add(stopOnExit1);
        stopSensorIcon1 = new SensorIcon(new jmri.jmrit.display.panelEditor.PanelEditor());
        setupIconMap(stopSensorIcon1);
        stopSensorIcon1.setToolTipText(rb.getString("TooltipStartSensorIcon"));
        pane2.add(stopSensorIcon1);
        getContentPane().add(pane2);

        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
        pane3.add(new JLabel(rb.getString("LabelSensor")));
        stopSensor2.setToolTipText(rb.getString("TooltipStopSensor2"));
        pane3.add(stopSensor2);
        pane3.add(new JLabel(rb.getString("LabelStopSensor2")));
        pane3.add(stopOnEntry2);
        pane3.add(stopOnExit2);
        stopSensorIcon2 = new SensorIcon(new jmri.jmrit.display.panelEditor.PanelEditor());
        setupIconMap(stopSensorIcon2);
        stopSensorIcon2.setToolTipText(rb.getString("TooltipStartSensorIcon"));
        pane3.add(stopSensorIcon2);
        getContentPane().add(pane3);

        JPanel pane4 = new JPanel();
        pane4.setLayout(new FlowLayout());
        pane4.add(text1);
        pane4.add(distance1);
        getContentPane().add(pane4);

        JPanel pane5 = new JPanel();
        pane5.setLayout(new FlowLayout());
        pane5.add(text2);
        pane5.add(distance2);
        getContentPane().add(pane5);

        JPanel buttons = new JPanel();
        buttons.add(dimButton);
        dimButton.setToolTipText(rb.getString("TooltipSwitchUnits"));
        buttons.add(startButton);
        buttons.add(clearButton);
        buttons.add(saveButton);
        getContentPane().add(buttons);

        clearButton.setVisible(false);

        // see if there's a sensor manager, if not disable
        if (null == InstanceManager.sensorManagerInstance()) {
           startButton.setEnabled(false);
           startButton.setToolTipText(rb.getString("TooltipSensorsNotSupported"));
        }

        JPanel pane6 = new JPanel();
        pane6.setLayout(new FlowLayout());
        pane6.add(text3);
        pane6.add(result1);
        pane6.add(new JLabel(rb.getString("LabelTime")));
        pane6.add(time1);
        getContentPane().add(pane6);

        JPanel pane7 = new JPanel();
        pane7.setLayout(new FlowLayout());
        pane7.add(text4);
        pane7.add(result2);
        pane7.add(new JLabel(rb.getString("LabelTime")));
        pane7.add(time2);
        getContentPane().add(pane7);

        // set the units consistently
        dim();

        // add the actions to the config button
        dimButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    dim();
                }
            });

        startButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setup();
                }
            });

        clearButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    time1.setText(blank);
                    time2.setText(blank);
                    result1.setText(blank);
                    result2.setText(blank);
                }
            });

        saveButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doStore();
                }
            });

        // start displaying the sensor status when the number is entered
        startSensor.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    startSensorIcon.setSensor(startSensor.getText());
                }
            });
        stopSensor1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    stopSensorIcon1.setSensor(stopSensor1.getText());
                }
            });

        stopSensor2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    stopSensorIcon2.setSensor(stopSensor2.getText());
                }
            });

        // add help menu to window
    	addHelpMenu("package.jmri.jmrit.speedometer.SpeedometerFrame", true);

        // and get ready to display
        pack();

        // finally, load any previously saved defaults
        doLoad();
    }

    long startTime = 0;
    long stopTime1 = 0;
    long stopTime2 = 0;

    /**
     * "Distance Is Metric": If true, metric distances are being used.
     */
    boolean dim;

    // establish whether English or Metric representation is wanted
    final void dim() {
        dimButton.setEnabled(true);
        if (dimButton.getText().equals (rb.getString("ButtonToMetric"))) {
          dimButton.setText(rb.getString("ButtonToEnglish"));
          dim = true;
          text1.setText(rb.getString("Distance1Metric"));
          text2.setText(rb.getString("Distance2Metric"));
          text3.setText(rb.getString("Speed1Metric"));
          text4.setText(rb.getString("Speed2Metric"));
          }
        else {
          dimButton.setText(rb.getString("ButtonToMetric"));
          dim = false;
          text1.setText(rb.getString("Distance1English"));
          text2.setText(rb.getString("Distance2English"));
          text3.setText(rb.getString("Speed1English"));
          text4.setText(rb.getString("Speed2English"));
          }
       }

    public void setup() {
        //startButton.setToolTipText("You can only configure this once");

        // Check inputs are valid and get the number of valid stop sensors
        int valid = verifyInputs(true);
        if (log.isDebugEnabled())
            log.debug("Number of valid stop sensors: " + valid);
        enableConfiguration(valid==0);
        if (valid==0) return;

        // set start sensor
        Sensor s;
        s = InstanceManager.sensorManagerInstance().
                provideSensor(startSensor.getText());
        s.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    SpeedometerFrame.log.debug("start sensor fired");
                    if (e.getPropertyName().equals("KnownState")) {
                        int now = ((Integer) e.getNewValue()).intValue();
                        if ( (now==Sensor.ACTIVE && startOnEntry.isSelected())
                             || (now==Sensor.INACTIVE && startOnExit.isSelected()) ) {
                            startTime = System.currentTimeMillis();  // milliseconds
                            if (log.isDebugEnabled()) log.debug("set start "+startTime);
                        }
                    }
                }
            });
        startSensorIcon.setSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(startSensor.getText(),s));

        // set stop sensor1
        s = InstanceManager.sensorManagerInstance().
                provideSensor(stopSensor1.getText());
        s.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    SpeedometerFrame.log.debug("stop sensor fired");
                    if (e.getPropertyName().equals("KnownState")) {
                        int now = ((Integer) e.getNewValue()).intValue();
                        if ( (now==Sensor.ACTIVE && stopOnEntry1.isSelected())
                             || (now==Sensor.INACTIVE && stopOnExit1.isSelected()) ) {
                            stopTime1 = System.currentTimeMillis();  // milliseconds
                            if (log.isDebugEnabled()) log.debug("set stop "+stopTime1);
                            // calculate and show speed
                            float secs = (stopTime1-startTime)/1000.f;
                            float feet = Float.valueOf(distance1.getText()).floatValue();
                            float speed;
                            if (dim == false) {
                              speed = (feet/5280.f)*(3600.f/secs);
                              }
                            else {
                              speed = (feet/100000.f)*(3600.f/secs);
                              }
                            if (log.isDebugEnabled()) log.debug("calc from "+secs+","+feet+":"+speed);
                            result1.setText(String.valueOf(speed).substring(0,4));
                            String time = String.valueOf(secs);
                            int offset = time.indexOf(".");
                            if (offset==-1) offset=time.length();
                            offset=offset+2;  // the decimal point, plus tenths digit
                            if (offset>time.length()) offset=time.length();
                            time1.setText(time.substring(0,offset));
                        }
                    }
                }
            });
        stopSensorIcon1.setSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(stopSensor1.getText(),s));

        if (valid==1) return;

        // set stop sensor2
        s = InstanceManager.sensorManagerInstance().
                provideSensor(stopSensor2.getText());
        s.addPropertyChangeListener(new java.beans.PropertyChangeListener(){
                // handle change in stop sensor
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    SpeedometerFrame.log.debug("stop sensor fired");
                    if (e.getPropertyName().equals("KnownState")) {
                        int now = ((Integer) e.getNewValue()).intValue();
                        if ( (now==Sensor.ACTIVE && stopOnEntry2.isSelected())
                             || (now==Sensor.INACTIVE && stopOnExit2.isSelected()) ) {
                            stopTime2 = System.currentTimeMillis();  // milliseconds
                            if (log.isDebugEnabled()) log.debug("set stop "+stopTime2);
                            // calculate and show speed
                            float secs = (stopTime2-startTime)/1000.f;
                            float feet = Float.valueOf(distance2.getText()).floatValue();

                            float speed;
                            if (dim == false) {
                              speed = (feet/5280.f)*(3600.f/secs);
                              }
                            else {
                              speed = (feet/100000.f)*(3600.f/secs);
                              }
                            if (log.isDebugEnabled()) log.debug("calc from "+secs+","+feet+":"+speed);
                            result2.setText(String.valueOf(speed).substring(0,4));
                            String time = String.valueOf(secs);
                            int offset = time.indexOf(".");
                            if (offset==-1) offset=time.length();
                            offset=offset+2;  // the decimal point, plus tenths digit
                            if (offset>time.length()) offset=time.length();
                            time2.setText(time.substring(0,offset));
                        }
                    }
                }
            });
        NamedBeanHandle<Sensor> namedSensor2 = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(stopSensor2.getText(), s);
        stopSensorIcon2.setSensor(namedSensor2);
    }

    private void enableConfiguration(boolean enable) {
        // Buttons first
        startButton.setEnabled(enable);
        startButton.setVisible(enable);
        clearButton.setEnabled(!enable);
        clearButton.setVisible(!enable);
        saveButton.setEnabled(enable);

        // Now Start sensor
        startSensor.setEnabled(enable);
        startOnEntry.setEnabled(enable);
        startOnExit.setEnabled(enable);

        // Now Stop sensor 1
        stopSensor1.setEnabled(enable);
        stopOnEntry1.setEnabled(enable);
        stopOnExit1.setEnabled(enable);

        // Now Stop sensor 2
        stopSensor2.setEnabled(enable);
        stopOnEntry2.setEnabled(enable);
        stopOnExit2.setEnabled(enable);

        // Finally, distances
        distance1.setEnabled(enable);
        distance2.setEnabled(enable);
        dimButton.setEnabled(enable);
    }

    /**
     * Verifies if correct inputs have been made and returns the number of
     * valid stop sensors.
     * @param warn true if warning messages to be displayed
     * @return 0 if not verified; otherwise the number of valid stop sensors defined
     */
    private int verifyInputs(boolean warn) {

        // Initially, no stop sensors are valid
        int verify = 0;

        Sensor s;

        // Check the start sensor
        try {
            s = InstanceManager.sensorManagerInstance().
                    provideSensor(startSensor.getText());
            if (s==null) throw new Exception();
        }
        catch (Exception e) {
            // couldn't locate the sensor, that's an error
            log.error("Start sensor invalid: "+startSensor.getText());
            if (warn) JOptionPane.showMessageDialog(
                    this,
                    rb.getString("ErrorStartSensor"),
                    rb.getString("TitleError"),
                    JOptionPane.WARNING_MESSAGE);
            return verify;
        }

        // Check stop sensor 1
        try {
            s = InstanceManager.sensorManagerInstance().
                    provideSensor(stopSensor1.getText());
            if (s==null) throw new Exception();
        }
        catch (Exception e) {
            // couldn't locate the sensor, that's an error
            log.error("Stop 1 sensor invalid : "+stopSensor1.getText());
            if (warn) JOptionPane.showMessageDialog(
                    this,
                    rb.getString("ErrorStopSensor1"),
                    rb.getString("TitleError"),
                    JOptionPane.WARNING_MESSAGE);
            return verify;
        }

        // Check distance1 has been defined
        if (distance1.getText().equals("")) {
            log.error("Distance 1 has not been defined");
            if (warn) JOptionPane.showMessageDialog(
                    this,
                    rb.getString("ErrorDistance1"),
                    rb.getString("TitleError"),
                    JOptionPane.WARNING_MESSAGE);
            return verify;
        }

        // We've got this far, so at least start and one stop sensor is valid
        verify = 1;

        // Check stop sensor2 if either sensor 2 and/or distance 2 defined
        if (!stopSensor2.getText().equals("") || !distance2.getText().equals("")) {
            try {
                s = InstanceManager.sensorManagerInstance().
                        provideSensor(stopSensor2.getText());
                if (s==null) throw new Exception();
            }
            catch (Exception e) {
                // couldn't locate the sensor, that's an error
                log.error("Stop 2 sensor invalid: "+stopSensor2.getText());
                if (warn) JOptionPane.showMessageDialog(
                        this,
                        rb.getString("ErrorStopSensor2"),
                        rb.getString("TitleError"),
                        JOptionPane.WARNING_MESSAGE);
                return 0;
            }

            // Check distance2 has been defined
            if (distance2.getText().equals("")) {
                log.error("Distance 2 has not been defined");
                enableConfiguration(true);
                if (warn) JOptionPane.showMessageDialog(
                        this,
                        rb.getString("ErrorDistance2"),
                        rb.getString("TitleError"),
                        JOptionPane.WARNING_MESSAGE);
                return 0;
            }

            // We've got this far, so stop sensor 2 is valid
            verify = 2;
        }
        return verify;
    }

    private void doStore() {
        log.debug("Check if there's anything to store");
        int verify = verifyInputs(false);
        if (verify==0) {
            if (JOptionPane.showConfirmDialog(
                    this,
                        rb.getString("QuestionNothingToStore"),
                        rb.getString("TitleStoreQuestion"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
                return;
            }
        }
        log.debug("Start storing speedometer settings...");

        SpeedometerXml x = new SpeedometerXml();

        x.makeBackupFile(SpeedometerXml.getDefaultFileName());
        
        File file = x.getFile(true);

        // Create root element
        Element root = new Element("speedometer-config");
        root.setAttribute("noNamespaceSchemaLocation",
                "http://jmri.org/xml/schema/speedometer.xsd",
                org.jdom.Namespace.getNamespace("xsi",
                "http://www.w3.org/2001/XMLSchema-instance"));
        Document doc = new Document(root);

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl");
        m.put("href", SpeedometerXml.xsltLocation+"speedometer.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0,p);

        Element values;

        // Store configuration
        root.addContent(values = new Element("configuration"));
        values.addContent(new Element("useMetric").addContent(dim?"yes":"no"));

        // Store values
        if (verify>0 || startSensor.getText().length()>0) {
            // Create sensors element
            root.addContent(values = new Element("sensors"));

            // Store start sensor
            Element e = new Element("sensor");
            e.addContent(new Element("sensorName").addContent(startSensor.getText()));
            e.addContent(new Element("type").addContent("StartSensor"));
            e.addContent(new Element("trigger").addContent(startOnEntry.isSelected()?"entry":"exit"));
            values.addContent(e);

            // If valid, store stop sensor 1
            if (verify > 0) {
                e = new Element("sensor");
                e.addContent(new Element("sensorName").addContent(stopSensor1.getText()));
                e.addContent(new Element("type").addContent("StopSensor1"));
                e.addContent(new Element("trigger").addContent(stopOnEntry1.isSelected()?"entry":"exit"));
                e.addContent(new Element("distance").addContent(distance1.getText()));
                values.addContent(e);
            }

            // If valid, store stop sensor 2
            if (verify > 1) {
                e = new Element("sensor");
                e.addContent(new Element("sensorName").addContent(stopSensor2.getText()));
                e.addContent(new Element("type").addContent("StopSensor2"));
                e.addContent(new Element("trigger").addContent(stopOnEntry2.isSelected()?"entry":"exit"));
                e.addContent(new Element("distance").addContent(distance2.getText()));
                values.addContent(e);
            }
        }
        try {
            x.writeXML(file, doc);
        } catch (FileNotFoundException ex) {
            log.error("File not found when writing: "+ex);
        } catch (IOException ex) {
            log.error("IO Exception when writing: "+ex);
        }

        log.debug("...done");
    }

    private void doLoad() {

        log.debug("Check if there's anything to load");
        SpeedometerXml x = new SpeedometerXml();
        File file = x.getFile(false);

        if (file == null) {
            log.debug("Nothing to load");
            return;
        }

        log.debug("Start loading speedometer settings...");

        // Find root
        Element root;
        try {
            root = x.rootFromFile(file);
            if (root == null) {
                log.debug("File could not be read");
                return;
            }

            // First read configuration
            if (root.getChild("configuration") != null) {
                @SuppressWarnings("unchecked")
                List<Element> l = root.getChild("configuration").getChildren();
                if (log.isDebugEnabled()) log.debug("readFile sees " + l.size() + " configurations");
                for (int i=0; i<l.size(); i++) {
                    Element e = l.get(i);
                    if (log.isDebugEnabled()) log.debug("Configuration " + e.getName() + " value " + e.getValue());
                    if (e.getName().equals("useMetric")) {
                        setUnitsMetric(e.getValue().equals("yes")?true:false);
                    }
                }
            }

            // Now read sensor information
            if (root.getChild("sensors") != null) {
                @SuppressWarnings("unchecked")
                List<Element> l = root.getChild("sensors").getChildren("sensor");
                if (log.isDebugEnabled()) log.debug("readFile sees " + l.size() + " sensors");
                for (int i=0; i<l.size(); i++) {
                    Element e = l.get(i);
                    String sensorType = e.getChild("type").getText();
                    if (sensorType.equals("StartSensor")) {
                        startSensor.setText(e.getChild("sensorName").getText());
                        boolean trigger = e.getChild("trigger").getValue().equals("entry");
                        startOnEntry.setSelected(trigger);
                        startOnExit.setSelected(!trigger);
                    } else if (sensorType.equals("StopSensor1")) {
                        stopSensor1.setText(e.getChild("sensorName").getText());
                        boolean trigger = e.getChild("trigger").getValue().equals("entry");
                        stopOnEntry1.setSelected(trigger);
                        stopOnExit1.setSelected(!trigger);
                        distance1.setText(e.getChild("distance").getText());
                    } else if (sensorType.equals("StopSensor2")) {
                        stopSensor2.setText(e.getChild("sensorName").getText());
                        boolean trigger = e.getChild("trigger").getValue().equals("entry");
                        stopOnEntry2.setSelected(trigger);
                        stopOnExit2.setSelected(!trigger);
                        distance2.setText(e.getChild("distance").getText());
                    } else {
                        log.warn("Unknown sensor type: " + sensorType);
                    }
                }
            }

        } catch (JDOMException ex) {
            log.error("File invalid: "+ex);
        } catch (IOException ex) {
            log.error("Error reading file: "+ex);
        }

        log.debug("...done");
    }

    private void setupIconMap(SensorIcon sensor) {
        sensor.setIcon("SensorStateActive",
                new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-occupied.gif",
                    "resources/icons/smallschematics/tracksegments/circuit-occupied.gif"));
        sensor.setIcon("SensorStateInactive",
                new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-empty.gif",
                    "resources/icons/smallschematics/tracksegments/circuit-empty.gif"));
        sensor.setIcon("BeanStateInconsistent",
                new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                    "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        sensor.setIcon("BeanStateUnknown",
                new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                    "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
    }

    private static class SpeedometerXml extends XmlFile {

        public static String getDefaultFileName() {
            return getFileLocation()+getFileName();
        }

        public File getFile(boolean store) {
            File file = findFile(getDefaultFileName());
            if (file == null && store) {
                file = new File(getDefaultFileName());
            }
            return file;
        }

        private static String baseFileName = "Speedometer.xml";

        public static String getFileName() {
            return Application.getApplicationName()+baseFileName;
        }

        /**
         * Absolute path to location of Speedometer files.
         * @return path to location
         */
        public static String getFileLocation() {
            return fileLocation;
        }

        private static String fileLocation = FileUtil.getUserFilesPath();

    }

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpeedometerFrame.class.getName());
}


