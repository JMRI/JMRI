package jmri.jmrit.speedometer;

import java.awt.FlowLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.Application;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.XmlFile;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.SensorIcon;
import jmri.util.FileUtil;
import jmri.util.IntlUtilities;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing access to a speedometer.
 * <p>
 * This contains very simple debouncing logic:
 * <ul>
 * <li>The clock starts when the "start" sensor makes the correct transition.
 * <li>When a "stop" sensor makes the correct transition, the speed is computed
 * and displayed.
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2004, 2007
 * @author Adapted for metric system - S.K. Bosch
 * @author Matthew Harris Copyright (c) 2011
 */
public class SpeedometerFrame extends jmri.util.JmriJFrame {

    final String blank = "       ";
    JTextField startSensor = new JTextField(5);
    javax.swing.ButtonGroup startGroup = new javax.swing.ButtonGroup();
    javax.swing.JRadioButton startOnEntry = new javax.swing.JRadioButton(Bundle.getMessage("RadioButtonEntry"));
    javax.swing.JRadioButton startOnExit = new javax.swing.JRadioButton(Bundle.getMessage("RadioButtonExit"));

    JTextField stopSensor1 = new JTextField(5);
    javax.swing.ButtonGroup stopGroup1 = new javax.swing.ButtonGroup();
    javax.swing.JRadioButton stopOnEntry1 = new javax.swing.JRadioButton(Bundle.getMessage("RadioButtonEntry"));
    javax.swing.JRadioButton stopOnExit1 = new javax.swing.JRadioButton(Bundle.getMessage("RadioButtonExit"));

    public JTextField stopSensor2 = new JTextField(5);
    javax.swing.ButtonGroup stopGroup2 = new javax.swing.ButtonGroup();
    javax.swing.JRadioButton stopOnEntry2 = new javax.swing.JRadioButton(Bundle.getMessage("RadioButtonEntry"));
    javax.swing.JRadioButton stopOnExit2 = new javax.swing.JRadioButton(Bundle.getMessage("RadioButtonExit"));

    JTextField distance1 = new JTextField(5);
    JTextField distance2 = new JTextField(5);

    JButton dimButton = new JButton("");   // content will be set to English during startup
    JButton startButton = new JButton(Bundle.getMessage("ButtonStart"));

    JLabel text1 = new JLabel(Bundle.getMessage("Distance1English"));
    JLabel text2 = new JLabel(Bundle.getMessage("Distance2English"));
    JLabel text3 = new JLabel(Bundle.getMessage("Speed1English"));
    JLabel text4 = new JLabel(Bundle.getMessage("Speed2English"));

    JButton clearButton = new JButton(Bundle.getMessage("ButtonClear"));

    JLabel result1 = new JLabel(blank);
    JLabel time1 = new JLabel(blank);
    JLabel result2 = new JLabel(blank);
    JLabel time2 = new JLabel(blank);

    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    SensorIcon startSensorIcon;
    SensorIcon stopSensorIcon1;
    SensorIcon stopSensorIcon2;

    /**
     *
     * @param d1 First timer distance in current units. Express with the decimal
     *           marker in the current Locale.
     * @param d2 Second timer distance in current units. Express with the
     *           decimal marker in the current Locale.
     */
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

        setInputBehavior(true, true, true);

        startGroup.add(startOnEntry);
        startGroup.add(startOnExit);
        stopGroup1.add(stopOnEntry1);
        stopGroup1.add(stopOnExit1);
        stopGroup2.add(stopOnEntry2);
        stopGroup2.add(stopOnExit2);

        // general GUI config
        setTitle(Bundle.getMessage("TitleSpeedometer"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // need a captive panel editor for
        // the sensor icons to work
        jmri.jmrit.display.panelEditor.PanelEditor editor = new jmri.jmrit.display.panelEditor.PanelEditor();
        editor.makePrivateWindow();
        editor.setVisible(false);

        // add items to GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());
        pane1.add(new JLabel(Bundle.getMessage("LabelSensor")));
        startSensor.setToolTipText(Bundle.getMessage("TooltipStartSensor"));
        pane1.add(startSensor);
        JLabel startSensorLabel = new JLabel(Bundle.getMessage("LabelStartSensor"));
        startSensorLabel.setLabelFor(startSensor);
        pane1.add(startSensorLabel);
        pane1.add(startOnEntry);
        pane1.add(startOnExit);
        startSensorIcon = new SensorIcon(editor);
        setupIconMap(startSensorIcon);
        startSensorIcon.setToolTipText(Bundle.getMessage("TooltipStartSensorIcon"));
        pane1.add(startSensorIcon);
        getContentPane().add(pane1);

        JPanel pane2 = new JPanel();
        pane2.setLayout(new FlowLayout());
        pane2.add(new JLabel(Bundle.getMessage("LabelSensor")));
        stopSensor1.setToolTipText(Bundle.getMessage("TooltipStopSensor1"));
        pane2.add(stopSensor1);
        JLabel stopSensor1Label = new JLabel(Bundle.getMessage("LabelStopSensor1"));
        stopSensor1Label.setLabelFor(stopSensor1);
        pane2.add(stopSensor1Label);
        pane2.add(stopOnEntry1);
        pane2.add(stopOnExit1);
        stopSensorIcon1 = new SensorIcon(editor);
        setupIconMap(stopSensorIcon1);
        stopSensorIcon1.setToolTipText(Bundle.getMessage("TooltipStartSensorIcon"));
        pane2.add(stopSensorIcon1);
        getContentPane().add(pane2);

        JPanel pane3 = new JPanel();
        pane3.setLayout(new FlowLayout());
        pane3.add(new JLabel(Bundle.getMessage("LabelSensor")));
        stopSensor2.setToolTipText(Bundle.getMessage("TooltipStopSensor2"));
        pane3.add(stopSensor2);
        JLabel stopSensor2Label = new JLabel(Bundle.getMessage("LabelStopSensor2"));
        stopSensor2Label.setLabelFor(stopSensor2);
        pane3.add(stopSensor2Label);
        pane3.add(stopOnEntry2);
        pane3.add(stopOnExit2);
        stopSensorIcon2 = new SensorIcon(editor);
        setupIconMap(stopSensorIcon2);
        stopSensorIcon2.setToolTipText(Bundle.getMessage("TooltipStartSensorIcon"));
        pane3.add(stopSensorIcon2);
        getContentPane().add(pane3);

        JPanel pane4 = new JPanel();
        pane4.setLayout(new FlowLayout());
        pane4.add(text1);
        text1.setLabelFor(distance1);
        pane4.add(distance1);
        getContentPane().add(pane4);

        JPanel pane5 = new JPanel();
        pane5.setLayout(new FlowLayout());
        pane5.add(text2);
        text2.setLabelFor(distance2);
        pane5.add(distance2);
        getContentPane().add(pane5);

        JPanel buttons = new JPanel();
        buttons.add(dimButton);
        dimButton.setToolTipText(Bundle.getMessage("TooltipSwitchUnits"));
        buttons.add(startButton);
        buttons.add(clearButton);
        buttons.add(saveButton);
        getContentPane().add(buttons);

        clearButton.setVisible(false);

        // see if there's a sensor manager, if not disable
        if (null == InstanceManager.getNullableDefault(SensorManager.class)) {
            startButton.setEnabled(false);
            startButton.setToolTipText(Bundle.getMessage("TooltipSensorsNotSupported"));
        }

        JPanel pane6 = new JPanel();
        pane6.setLayout(new FlowLayout());
        pane6.add(text3);
        pane6.add(result1);
        text3.setLabelFor(result1);
        JLabel time1Label = new JLabel(Bundle.getMessage("LabelTime"));
        pane6.add(time1Label);
        pane6.add(time1);
        time1Label.setLabelFor(time1);
        getContentPane().add(pane6);

        JPanel pane7 = new JPanel();
        pane7.setLayout(new FlowLayout());
        pane7.add(text4);
        pane7.add(result2);
        text4.setLabelFor(result2);
        JLabel time2Label = new JLabel(Bundle.getMessage("LabelTime"));
        pane7.add(time2Label);
        pane7.add(time2);
        time2Label.setLabelFor(time2);
        getContentPane().add(pane7);

        // set the units consistently
        dim();

        // add the actions to the config button
        dimButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dim();
            }
        });

        startButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setup();
            }
        });

        clearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                time1.setText(blank);
                time2.setText(blank);
                result1.setText(blank);
                result2.setText(blank);
            }
        });

        saveButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doStore();
            }
        });

        // start displaying the sensor status when the number is entered
        startSensor.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startSensorIcon.setSensor(startSensor.getText());
            }
        });
        stopSensor1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopSensorIcon1.setSensor(stopSensor1.getText());
            }
        });

        stopSensor2.addActionListener(new java.awt.event.ActionListener() {
            @Override
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
        if (dimButton.getText().equals(Bundle.getMessage("ButtonToMetric"))) {
            dimButton.setText(Bundle.getMessage("ButtonToEnglish"));
            dim = true;
            text1.setText(Bundle.getMessage("Distance1Metric"));
            text2.setText(Bundle.getMessage("Distance2Metric"));
            text3.setText(Bundle.getMessage("Speed1Metric"));
            text4.setText(Bundle.getMessage("Speed2Metric"));
        } else {
            dimButton.setText(Bundle.getMessage("ButtonToMetric"));
            dim = false;
            text1.setText(Bundle.getMessage("Distance1English"));
            text2.setText(Bundle.getMessage("Distance2English"));
            text3.setText(Bundle.getMessage("Speed1English"));
            text4.setText(Bundle.getMessage("Speed2English"));
        }
    }

    public void setup() {
        //startButton.setToolTipText("You can only configure this once");

        // Check inputs are valid and get the number of valid stop sensors
        int valid = verifyInputs(true);
        if (log.isDebugEnabled()) {
            log.debug("Number of valid stop sensors: " + valid);
        }
        enableConfiguration(valid == 0);
        if (valid == 0) {
            return;
        }

        // set start sensor
        Sensor s;
        s = InstanceManager.sensorManagerInstance().
                provideSensor(startSensor.getText());
        s.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                SpeedometerFrame.log.debug("start sensor fired");
                if (e.getPropertyName().equals("KnownState")) {
                    int now = ((Integer) e.getNewValue()).intValue();
                    if ((now == Sensor.ACTIVE && startOnEntry.isSelected())
                            || (now == Sensor.INACTIVE && startOnExit.isSelected())) {
                        startTime = System.currentTimeMillis();  // milliseconds
                        if (log.isDebugEnabled()) {
                            log.debug("set start " + startTime);
                        }
                    }
                }
            }
        });
        startSensorIcon.setSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(startSensor.getText(), s));

        // set stop sensor1
        s = InstanceManager.sensorManagerInstance().
                provideSensor(stopSensor1.getText());
        s.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                SpeedometerFrame.log.debug("stop sensor fired");
                if (e.getPropertyName().equals("KnownState")) {
                    int now = ((Integer) e.getNewValue()).intValue();
                    if ((now == Sensor.ACTIVE && stopOnEntry1.isSelected())
                            || (now == Sensor.INACTIVE && stopOnExit1.isSelected())) {
                        stopTime1 = System.currentTimeMillis();  // milliseconds
                        if (log.isDebugEnabled()) {
                            log.debug("set stop " + stopTime1);
                        }
                        // calculate and show speed
                        float secs = (stopTime1 - startTime) / 1000.f;
                        float feet = 0.0f;
                        try {
                            feet = IntlUtilities.floatValue(distance1.getText());
                        } catch (java.text.ParseException ex) {
                            log.error("invalid floating point number as input: " + distance1.getText());
                        }
                        float speed;
                        if (dim == false) {
                            speed = (feet / 5280.f) * (3600.f / secs);
                        } else {
                            speed = (feet / 100000.f) * (3600.f / secs);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("calc from " + secs + "," + feet + ":" + speed);
                        }
                        result1.setText(String.valueOf(speed).substring(0, 4));
                        String time = String.valueOf(secs);
                        int offset = time.indexOf(".");
                        if (offset == -1) {
                            offset = time.length();
                        }
                        offset = offset + 2;  // the decimal point, plus tenths digit
                        if (offset > time.length()) {
                            offset = time.length();
                        }
                        time1.setText(time.substring(0, offset));
                    }
                }
            }
        });
        stopSensorIcon1.setSensor(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(stopSensor1.getText(), s));

        if (valid == 1) {
            return;
        }

        // set stop sensor2
        s = InstanceManager.sensorManagerInstance().
                provideSensor(stopSensor2.getText());
        s.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            // handle change in stop sensor
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                SpeedometerFrame.log.debug("stop sensor fired");
                if (e.getPropertyName().equals("KnownState")) {
                    int now = ((Integer) e.getNewValue()).intValue();
                    if ((now == Sensor.ACTIVE && stopOnEntry2.isSelected())
                            || (now == Sensor.INACTIVE && stopOnExit2.isSelected())) {
                        stopTime2 = System.currentTimeMillis();  // milliseconds
                        if (log.isDebugEnabled()) {
                            log.debug("set stop " + stopTime2);
                        }
                        // calculate and show speed
                        float secs = (stopTime2 - startTime) / 1000.f;
                        float feet = 0.0f;
                        try {
                            feet = IntlUtilities.floatValue(distance2.getText());
                        } catch (java.text.ParseException ex) {
                            log.error("invalid floating point number as input: " + distance2.getText());
                        }
                        float speed;
                        if (dim == false) {
                            speed = (feet / 5280.f) * (3600.f / secs);
                        } else {
                            speed = (feet / 100000.f) * (3600.f / secs);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("calc from " + secs + "," + feet + ":" + speed);
                        }
                        result2.setText(String.valueOf(speed).substring(0, 4));
                        String time = String.valueOf(secs);
                        int offset = time.indexOf(".");
                        if (offset == -1) {
                            offset = time.length();
                        }
                        offset = offset + 2;  // the decimal point, plus tenths digit
                        if (offset > time.length()) {
                            offset = time.length();
                        }
                        time2.setText(time.substring(0, offset));
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
     * Verifies if correct inputs have been made and returns the number of valid
     * stop sensors.
     *
     * @param warn true if warning messages to be displayed
     * @return 0 if not verified; otherwise the number of valid stop sensors
     *         defined
     */
    private int verifyInputs(boolean warn) {

        // Initially, no stop sensors are valid
        int verify = 0;

        Sensor s;

        // Check the start sensor
        try {
            s = InstanceManager.sensorManagerInstance().
                    provideSensor(startSensor.getText());
            if (s == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            // couldn't locate the sensor, that's an error
            log.error("Start sensor invalid: " + startSensor.getText());
            if (warn) {
                JOptionPane.showMessageDialog(
                        this,
                        Bundle.getMessage("ErrorStartSensor"),
                        Bundle.getMessage("TitleError"),
                        JOptionPane.WARNING_MESSAGE);
            }
            return verify;
        }

        // Check stop sensor 1
        try {
            s = InstanceManager.sensorManagerInstance().
                    provideSensor(stopSensor1.getText());
            if (s == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            // couldn't locate the sensor, that's an error
            log.error("Stop 1 sensor invalid : " + stopSensor1.getText());
            if (warn) {
                JOptionPane.showMessageDialog(
                        this,
                        Bundle.getMessage("ErrorStopSensor1"),
                        Bundle.getMessage("TitleError"),
                        JOptionPane.WARNING_MESSAGE);
            }
            return verify;
        }

        // Check distance1 has been defined
        if (distance1.getText().equals("")) {
            log.error("Distance 1 has not been defined");
            if (warn) {
                JOptionPane.showMessageDialog(
                        this,
                        Bundle.getMessage("ErrorDistance1"),
                        Bundle.getMessage("TitleError"),
                        JOptionPane.WARNING_MESSAGE);
            }
            return verify;
        }

        // We've got this far, so at least start and one stop sensor is valid
        verify = 1;

        // Check stop sensor2 if either sensor 2 and/or distance 2 defined
        if (!stopSensor2.getText().equals("") || !distance2.getText().equals("")) {
            try {
                s = InstanceManager.sensorManagerInstance().
                        provideSensor(stopSensor2.getText());
                if (s == null) {
                    throw new Exception();
                }
            } catch (Exception e) {
                // couldn't locate the sensor, that's an error
                log.error("Stop 2 sensor invalid: " + stopSensor2.getText());
                if (warn) {
                    JOptionPane.showMessageDialog(
                            this,
                            Bundle.getMessage("ErrorStopSensor2"),
                            Bundle.getMessage("TitleError"),
                            JOptionPane.WARNING_MESSAGE);
                }
                return 0;
            }

            // Check distance2 has been defined
            if (distance2.getText().equals("")) {
                log.error("Distance 2 has not been defined");
                enableConfiguration(true);
                if (warn) {
                    JOptionPane.showMessageDialog(
                            this,
                            Bundle.getMessage("ErrorDistance2"),
                            Bundle.getMessage("TitleError"),
                            JOptionPane.WARNING_MESSAGE);
                }
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
        if (verify == 0) {
            if (JOptionPane.showConfirmDialog(
                    this,
                    Bundle.getMessage("QuestionNothingToStore"),
                    Bundle.getMessage("TitleStoreQuestion"),
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
                "http://jmri.org/xml/schema/speedometer-3-9-3.xsd",
                org.jdom2.Namespace.getNamespace("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance"));
        Document doc = new Document(root);

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl");
        m.put("href", SpeedometerXml.xsltLocation + "speedometer.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0, p);

        Element values;

        // Store configuration
        root.addContent(values = new Element("configuration"));
        values.addContent(new Element("useMetric").addContent(dim ? "yes" : "no"));

        // Store values
        if (verify > 0 || startSensor.getText().length() > 0) {
            // Create sensors element
            root.addContent(values = new Element("sensors"));

            // Store start sensor
            Element e = new Element("sensor");
            e.addContent(new Element("sensorName").addContent(startSensor.getText()));
            e.addContent(new Element("type").addContent("StartSensor"));
            e.addContent(new Element("trigger").addContent(startOnEntry.isSelected() ? "entry" : "exit"));
            values.addContent(e);

            // If valid, store stop sensor 1
            if (verify > 0) {
                e = new Element("sensor");
                e.addContent(new Element("sensorName").addContent(stopSensor1.getText()));
                e.addContent(new Element("type").addContent("StopSensor1"));
                e.addContent(new Element("trigger").addContent(stopOnEntry1.isSelected() ? "entry" : "exit"));
                try {
                    e.addContent(new Element("distance").addContent(String.valueOf(IntlUtilities.floatValue(distance1.getText()))));
                } catch (java.text.ParseException ex) {
                    log.error("Distance isn't a valid floating number: " + distance1.getText());
                }
                values.addContent(e);
            }

            // If valid, store stop sensor 2
            if (verify > 1) {
                e = new Element("sensor");
                e.addContent(new Element("sensorName").addContent(stopSensor2.getText()));
                e.addContent(new Element("type").addContent("StopSensor2"));
                e.addContent(new Element("trigger").addContent(stopOnEntry2.isSelected() ? "entry" : "exit"));
                try {
                    e.addContent(new Element("distance").addContent(String.valueOf(IntlUtilities.floatValue(distance2.getText()))));
                } catch (java.text.ParseException ex) {
                    log.error("Distance isn't a valid floating number: " + distance2.getText());
                }
                values.addContent(e);
            }
        }
        try {
            x.writeXML(file, doc);
        } catch (FileNotFoundException ex) {
            log.error("File not found when writing: " + ex);
        } catch (IOException ex) {
            log.error("IO Exception when writing: " + ex);
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
                List<Element> l = root.getChild("configuration").getChildren();
                if (log.isDebugEnabled()) {
                    log.debug("readFile sees " + l.size() + " configurations");
                }
                for (int i = 0; i < l.size(); i++) {
                    Element e = l.get(i);
                    if (log.isDebugEnabled()) {
                        log.debug("Configuration " + e.getName() + " value " + e.getValue());
                    }
                    if (e.getName().equals("useMetric")) {
                        setUnitsMetric(e.getValue().equals("yes") ? true : false);
                    }
                }
            }

            // Now read sensor information
            if (root.getChild("sensors") != null) {
                List<Element> l = root.getChild("sensors").getChildren("sensor");
                if (log.isDebugEnabled()) {
                    log.debug("readFile sees " + l.size() + " sensors");
                }
                for (int i = 0; i < l.size(); i++) {
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
                        distance1.setText(
                                IntlUtilities.valueOf(
                                        Float.parseFloat(
                                                e.getChild("distance").getText()
                                        )
                                )
                        );
                    } else if (sensorType.equals("StopSensor2")) {
                        stopSensor2.setText(e.getChild("sensorName").getText());
                        boolean trigger = e.getChild("trigger").getValue().equals("entry");
                        stopOnEntry2.setSelected(trigger);
                        stopOnExit2.setSelected(!trigger);
                        distance2.setText(
                                IntlUtilities.valueOf(
                                        Float.parseFloat(
                                                e.getChild("distance").getText()
                                        )
                                )
                        );
                    } else {
                        log.warn("Unknown sensor type: " + sensorType);
                    }
                }
            }

        } catch (JDOMException ex) {
            log.error("File invalid: " + ex);
        } catch (IOException ex) {
            log.error("Error reading file: " + ex);
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
            return getFileLocation() + getFileName();
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
            return Application.getApplicationName() + baseFileName;
        }

        /**
         * Absolute path to location of Speedometer files.
         *
         * @return path to location
         */
        public static String getFileLocation() {
            return fileLocation;
        }

        private static String fileLocation = FileUtil.getUserFilesPath();

    }

    private static final Logger log = LoggerFactory.getLogger(SpeedometerFrame.class);
}
