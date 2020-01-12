package jmri.jmrix.can.cbus.swing.configtool;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;
import jmri.InstanceManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.swing.console.CbusConsolePane;
import jmri.jmrix.can.cbus.swing.CbusEventHighlightFrame;
import jmri.jmrix.can.cbus.swing.CbusFilterFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Pane for user creation of Sensor, Turnouts and Lights that are linked to CBUS
 * events.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @since 2.3.1
 */
public class ConfigToolPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    protected static int configtool_instance_num;
    final static int NRECORDERS = 6;
    CbusEventRecorder[] recorders = new CbusEventRecorder[NRECORDERS];
    private CbusFilterFrame _filterFrame;
    private CbusEventHighlightFrame _highlightFrame;
    private final CbusConsolePane _console;
    protected JButton filterButton;
    protected JButton highlightButton;
    private JButton resetCaptureButton;

    public static void incrementInstance() {
        configtool_instance_num++;
    }

    public static int getConfigToolInstanceNum() {
        log.debug("instance num {}",configtool_instance_num);
        return configtool_instance_num;
    }
    
    public ConfigToolPane() {
        super();
        _filterFrame = null;
        _highlightFrame = null;
        _console = null;
    }

    public ConfigToolPane(CbusConsolePane console, CbusFilterFrame filterFrame, CbusEventHighlightFrame highlightFrame) {
        super();
        _filterFrame = filterFrame;
        _highlightFrame = highlightFrame;
        _console = console;
    }

    public void init() {
        // log.debug("ConfigToolPane init");
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add event displays
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        
        filterButton = new JButton(Bundle.getMessage("ButtonFilter"));
        filterButton.setVisible(true);
        filterButton.setToolTipText(Bundle.getMessage("TooltipFilter"));
        buttons.add(filterButton);
        
        filterButton.addActionListener(this::filterButtonActionPerformed);
        
       
        highlightButton = new JButton(Bundle.getMessage("ButtonHighlight"));
        highlightButton.setVisible(true);
        highlightButton.setToolTipText(Bundle.getMessage("TooltipHighlighter"));
        buttons.add(highlightButton);
        
        highlightButton.addActionListener(this::highlightButtonActionPerformed);
        
        resetCaptureButton = new JButton(Bundle.getMessage("ButtonResetCapture"));
        resetCaptureButton.setVisible(true);
        resetCaptureButton.setHorizontalAlignment(SwingConstants.RIGHT);
        buttons.add(resetCaptureButton);
        
        resetCaptureButton.addActionListener(this::resetCaptureButtonActionPerformed);
        
        p1.add(buttons);
        
        for (int i = 0; i < recorders.length; i++) {
            recorders[i] = new CbusEventRecorder();
            p1.add(recorders[i]);
        }
        p1.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutEvents")));
        add(p1);

        // add sensor
        makeSensor = new MakeNamedBean("LabelEventActive", "LabelEventInactive") {
            @Override
            void create(String name) {
                try {
                    if (memo != null) {
                        ((jmri.SensorManager) memo.get(jmri.SensorManager.class)).provideSensor(prefix + "S" + name);
                    } else {
                        InstanceManager.sensorManagerInstance().provideSensor(prefix + "S" + name); // S for Sensor
                    }
                }
                catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null, 
                        (ex.getMessage()), Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        makeSensor.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TitleAddX", Bundle.getMessage("BeanNameSensor"))));
        add(makeSensor);

        // add turnout
        makeTurnout = new MakeNamedBean("LabelEventThrown", "LabelEventClosed") {
            @Override
            void create(String name) {
                try {
                    if (memo != null) {
                        ((jmri.TurnoutManager) memo.get(jmri.TurnoutManager.class)).provideTurnout(name); 
                        // provideTurnout auto adds the conn. prefix + T
                    } else {
                        InstanceManager.turnoutManagerInstance().provideTurnout(prefix + "T" + name); // T for Turnout
                    }
                }
                catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null, 
                        (ex.getMessage()), Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        makeTurnout.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TitleAddX", Bundle.getMessage("BeanNameTurnout"))));
        add(makeTurnout);
        
        // add light
        makeLight = new MakeNamedBean("LabelEventLightOn", "LabelEventLightOff") {
            @Override
            void create(String name) {
                try {
                    if (memo != null) {
                        ((jmri.LightManager) memo.get(jmri.LightManager.class)).provideLight(prefix + "L" + name);
                    } else {
                        InstanceManager.lightManagerInstance().provideLight(prefix + "L" + name); // L for Light
                    }
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, 
                        (ex.getMessage()), Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        makeLight.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TitleAddX", Bundle.getMessage("BeanNameLight"))));
        add(makeLight);        
        
    }

    protected TrafficController tc;
    protected String prefix = "M";

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        // log.debug("ConfigToolPane initComponents");
        super.initComponents(memo);
        prefix = memo.getSystemPrefix();
        tc = memo.getTrafficController();
        addTc(tc);
        incrementInstance();
        init();
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            StringBuilder title = new StringBuilder(20);
            title.append(memo.getUserName());
            title.append(" ");
            title.append(Bundle.getMessage("CapConfigTitle"));
            if (getConfigToolInstanceNum() > 1) {
                title.append(" ");
                title.append( getConfigToolInstanceNum() );
            }
            return title.toString();
        }
        return Bundle.getMessage("CapConfigTitle");
    }

    MakeNamedBean makeSensor;
    MakeNamedBean makeTurnout;
    MakeNamedBean makeLight;

    @Override
    public void reply(jmri.jmrix.can.CanReply m) {
        if ( m.isExtended() || m.isRtr() ) {
            return;
        }
        if ( ( _filterFrame!=null ) && ( _filterFrame.filter(m) ) ) {
            return;
        }
        // forward to anybody waiting to capture
        makeSensor.reply(m);
        makeTurnout.reply(m);
        makeLight.reply(m);
        for (CbusEventRecorder recorder : recorders) {
            if (recorder.waiting()) {
                recorder.reply(m);
                break;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void message(jmri.jmrix.can.CanMessage m) {
        if ( m.isExtended() || m.isRtr() ) {
            return;
        }
        if ( ( _filterFrame!=null ) && ( _filterFrame.filter(m)) ) {
            return;
        }
        // forward to anybody waiting to capture
        makeSensor.message(m);
        makeTurnout.message(m);
        makeLight.message(m);
        for (CbusEventRecorder recorder : recorders) {
            if (recorder.waiting()) {
                recorder.message(m);
                break;
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.can.cbus.swing.configtool.ConfigToolFrame";
    }

    @Override
    public void dispose() {
        clearTrafficController();
        clearFilterFrame();
        clearHighlightFrame();
        super.dispose();
    }

    private void clearTrafficController(){
        if(tc!=null) {
           tc.removeCanListener(this);
        }
        tc = null;
    }

    private void clearFilterFrame(){
        if (_filterFrame != null) {
            _filterFrame.dispose();
        }
        _filterFrame=null;
    }

    private void clearHighlightFrame(){
        if (_highlightFrame != null) {
            _highlightFrame.dispose();
        }
        _highlightFrame=null;
    }


    public void resetCaptureButtonActionPerformed(ActionEvent e) {
        for (CbusEventRecorder recorder : recorders) {
            recorder.capture.setSelected(true);
        }
    }

    public void filterButtonActionPerformed(ActionEvent e) {
        // log.debug("Cbus Console filter button action performed");
        
        
        if ( _console != null ) {
            _console.filterButton.doClick();
            return;
        }
        
        
        if (_filterFrame == null) {
            _filterFrame = new CbusFilterFrame(_console,this);
            try {
                _filterFrame.initComponents();
            } catch (Exception ex) {
                log.error("Exception: " + ex.toString());
            }
            _filterFrame.setVisible(true);
        } else {
            _filterFrame.setState(Frame.NORMAL);
            _filterFrame.setVisible(true);
        }
    }

    public void highlightButtonActionPerformed(ActionEvent e) {
        // log.debug("Cbus Console filter button action performed");
        if ( _console != null ) {
            _console.highlightButton.doClick();
            return;
        }
        if (_highlightFrame == null) {
            _highlightFrame = new CbusEventHighlightFrame(null,this);
            try {
                _highlightFrame.initComponents();
            } catch (Exception ex) {
                log.error("Exception: " + ex.toString());
            }
            _highlightFrame.setVisible(true);
        } else {
            _highlightFrame.setState(Frame.NORMAL);
            _highlightFrame.setVisible(true);
        }
    }

    public void setHighlighter( CbusEventHighlightFrame highlightFrame){
        _highlightFrame = highlightFrame;
    }

    public void setFilter( CbusFilterFrame filterFrame){
        _filterFrame = filterFrame;
    }

    /**
     * Class to build one NamedBean
     */
    class MakeNamedBean extends JPanel implements CanListener {

        JTextField f1 = new JTextField(15);
        JTextField f2 = new JTextField(15);
        JTextField f3 = new JTextField(15);
        
        JButton bc;

        JToggleButton b1 = new JToggleButton(Bundle.getMessage("ButtonCaptureNext"));
        JToggleButton b2 = new JToggleButton(Bundle.getMessage("ButtonCaptureNext"));
        JToggleButton b3 = new JToggleButton(Bundle.getMessage("ButtonCaptureNext"));
        

        /**
         * Create CBUS NamedBean using a JPanel for user interaction.
         *
         * @param name1 string for Label 1 in configuration pane
         * @param name2 string for Label 2 in configuration pane
         */
        MakeNamedBean(String name1, String name2) {
            // actions
            bc = new JButton(Bundle.getMessage("ButtonCreate"));
            bc.addActionListener((ActionEvent e) -> {
                if (f2.getText().isEmpty()) {
                    create(f1.getText());
                } else {
                    create(f1.getText() + ";" + f2.getText());
                }
            });

            // GUI
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = 1;
            c.gridheight = 1;

            c.gridx = 0;
            c.gridy = 0;
            c.anchor = GridBagConstraints.EAST;
            add(new JLabel(Bundle.getMessage(name1)), c);

            c.gridx = 1;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            add(f1, c);

            c.gridx = 2;
            c.gridy = 0;
            c.anchor = GridBagConstraints.WEST;
            add(b1, c);
            b1.setToolTipText(Bundle.getMessage("CaptureNextTooltip"));

            c.gridx = 0;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            add(new JLabel(Bundle.getMessage(name2)), c);

            c.gridx = 1;
            c.gridy = 1;
            c.anchor = GridBagConstraints.EAST;
            add(f2, c);

            c.gridx = 2;
            c.gridy = 1;
            c.anchor = GridBagConstraints.WEST;
            add(b2, c);
            b2.setToolTipText(Bundle.getMessage("CaptureNextTooltip"));

            c.gridx = 1;
            c.gridy = 2;
            c.anchor = GridBagConstraints.WEST;
            add(bc, c);
            bc.setToolTipText(Bundle.getMessage("CreateTooltip"));
        }

        void create(String name) {
        }

        /** {@inheritDoc} */
        @Override
        public void reply(jmri.jmrix.can.CanReply m) {
            int high = (_highlightFrame != null) ? _highlightFrame.highlight(m) : -1;
            if (b1.isSelected()) {
                f1.setText(CbusMessage.toAddress(m));
                b1.setSelected(false);
                if ( high > -1 ) {
                    f1.setBackground(CbusEventHighlightFrame.highlightColors[high]);
                } else {
                    f1.setBackground(Color.WHITE);
                }
            }
            if (b2.isSelected()) {
                f2.setText(CbusMessage.toAddress(m));
                b2.setSelected(false);
                if ( high > -1 ) {
                    f2.setBackground(CbusEventHighlightFrame.highlightColors[high]);
                } else {
                    f2.setBackground(Color.WHITE);
                }
            }
            if (b3.isSelected()) {
                f3.setText(CbusMessage.toAddress(m));
                b3.setSelected(false);
                if ( high > -1 ) {
                    f3.setBackground(CbusEventHighlightFrame.highlightColors[high]);
                } else {
                    f3.setBackground(Color.WHITE);
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public void message(jmri.jmrix.can.CanMessage m) {
            int high = (_highlightFrame != null) ? _highlightFrame.highlight(m) : -1;
            if (b1.isSelected()) {
                f1.setText(CbusMessage.toAddress(m));
                b1.setSelected(false);
                if ( high > -1 ) {
                    f1.setBackground(CbusEventHighlightFrame.highlightColors[high]);
                } else {
                    f1.setBackground(Color.WHITE);
                }
            }
            if (b2.isSelected()) {
                f2.setText(CbusMessage.toAddress(m));
                b2.setSelected(false);
                if ( high > -1 ) {
                    f2.setBackground(CbusEventHighlightFrame.highlightColors[high]);
                } else {
                    f2.setBackground(Color.WHITE);
                }
            }
            if (b3.isSelected()) {
                f3.setText(CbusMessage.toAddress(m));
                b3.setSelected(false);
                if ( high > -1 ) {
                    f3.setBackground(CbusEventHighlightFrame.highlightColors[high]);
                } else {
                    f3.setBackground(Color.WHITE);
                }
            }
        }
    }

    /**
     * Class to handle recording and presenting one event.
     */
    class CbusEventRecorder extends JPanel implements CanListener, FocusListener {

        CbusEventRecorder() {
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(event);
            add(capture);

            event.setEditable(false);
            event.setDragEnabled(true);
            event.setBackground(Color.WHITE);
            capture.setSelected(true);
            
        }
        
        final void init() {
            event.addFocusListener(this);
        }

        JCheckBox capture = new JCheckBox(Bundle.getMessage("MsgCaptureNext"));
        JTextField event = new JTextField(15);

        boolean waiting() {
            return capture.isSelected();
        }

        /** {@inheritDoc} */
        @Override
        public void reply(jmri.jmrix.can.CanReply m) {
            if (capture.isSelected()) {
                event.setText(CbusMessage.toAddress(m));
                capture.setSelected(false);
                int high = (_highlightFrame != null) ? _highlightFrame.highlight(m) : -1;
                if ( high > -1 ) {
                    event.setBackground(CbusEventHighlightFrame.highlightColors[high]);
                } else {
                    event.setBackground(Color.WHITE);
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public void message(jmri.jmrix.can.CanMessage m) {
            if (capture.isSelected()) {
                event.setText(CbusMessage.toAddress(m));
                capture.setSelected(false);
                int high = (_highlightFrame != null) ? _highlightFrame.highlight(m) : -1;
                if ( high > -1 ) {
                    event.setBackground(CbusEventHighlightFrame.highlightColors[high]);
                } else {
                    event.setBackground(Color.WHITE);
                }
            }
        }

        @Override
        public void focusGained(FocusEvent fe) {
            JTextField txt = (JTextField)fe.getComponent();
            txt.selectAll();
        }

        @Override
        public void focusLost(FocusEvent e) {
        }

    }

    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {
        public Default() {
            super(Bundle.getMessage("CapConfigTitle"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    ConfigToolPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ConfigToolPane.class);

}
