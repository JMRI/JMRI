package jmri.jmrix.can.cbus.swing.configtool;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanFrame;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanSystemConnectionMemo;
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

    static int configtool_instance_num;
    final static int NRECORDERS = 6;
    private final CbusEventRecorder[] recorders = new CbusEventRecorder[NRECORDERS];
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
                    ((jmri.SensorManager) memo.get(jmri.SensorManager.class)).provideSensor(name);
                }
                catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this,
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
                    ((jmri.TurnoutManager) memo.get(jmri.TurnoutManager.class)).provideTurnout(name);
                }
                catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this,
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
                    ((jmri.LightManager) memo.get(jmri.LightManager.class)).provideLight(name);
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        (ex.getMessage()), Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        makeLight.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TitleAddX", Bundle.getMessage("BeanNameLight"))));
        add(makeLight);

    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        addTc(memo);
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

    private MakeNamedBean makeSensor;
    private MakeNamedBean makeTurnout;
    private MakeNamedBean makeLight;

    @Override
    public void reply(jmri.jmrix.can.CanReply m) {
        sendToListeners(m);
    }

    /** {@inheritDoc} */
    @Override
    public void message(jmri.jmrix.can.CanMessage m) {
        sendToListeners(m);
    }

    private void sendToListeners( AbstractMessage m ){
        if ( ((CanFrame)m).extendedOrRtr()) {
            return;
        }
        if ( ( _filterFrame!=null ) && ( _filterFrame.filter(m)) ) {
            return;
        }
        // forward to anybody waiting to capture
        makeSensor.processFrame(m);
        makeTurnout.processFrame(m);
        makeLight.processFrame(m);
        for (CbusEventRecorder recorder : recorders) {
            if (recorder.waiting()) {
                recorder.processFrame(m);
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
        removeTc(memo);
        clearFilterFrame();
        clearHighlightFrame();
        super.dispose();
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
            _console.displayPane.filterButton.doClick();
            return;
        }

        if (_filterFrame == null) {
            _filterFrame = new CbusFilterFrame(_console,this);
            _filterFrame.initComponents();
        } else {
            _filterFrame.setState(Frame.NORMAL);
        }
        _filterFrame.setVisible(true);
    }

    public void highlightButtonActionPerformed(ActionEvent e) {
        // log.debug("Cbus Console filter button action performed");
        if ( _console != null ) {
            _console.displayPane.highlightButton.doClick();
            return;
        }
        if (_highlightFrame == null) {
            _highlightFrame = new CbusEventHighlightFrame(null,this);
            _highlightFrame.initComponents();

        } else {
            _highlightFrame.setState(Frame.NORMAL);
        }
        _highlightFrame.setVisible(true);
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
    private class MakeNamedBean extends JPanel {

        private JTextField f1 = new JTextField(15);
        private JTextField f2 = new JTextField(15);
        private JTextField f3 = new JTextField(15);

        private final JButton bc;

        JToggleButton b1 = new JToggleButton(Bundle.getMessage("ButtonCaptureNext"));
        JToggleButton b2 = new JToggleButton(Bundle.getMessage("ButtonCaptureNext"));
        JToggleButton b3 = new JToggleButton(Bundle.getMessage("ButtonCaptureNext"));

        private final JToggleButton[] captureNextButtons = new JToggleButton[]{b1,b2,b3};
        private final JTextField[] fields = new JTextField[]{f1,f2,f3};

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

            initGui(name1, name2);
        }

        private void initGui(String name1, String name2){

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

        public void processFrame(AbstractMessage m){

            int high = (_highlightFrame != null) ? _highlightFrame.highlight(m) : -1;
            for (int i=0; i < 3; i++){

                if (captureNextButtons[i].isSelected()) {
                    fields[i].setText(CbusMessage.toAddress(m));
                    captureNextButtons[i].setSelected(false);
                    if ( high > -1 ) {
                        fields[i].setBackground(CbusEventHighlightFrame.highlightColors[high]);
                    } else {
                        fields[i].setBackground(Color.WHITE);
                    }
                }
            }
        }
    }

    /**
     * Class to handle recording and presenting one event.
     */
    private class CbusEventRecorder extends JPanel implements FocusListener {

        CbusEventRecorder() {
            super();

            init();
            event.setEditable(false);
            event.setDragEnabled(true);
            event.setBackground(Color.WHITE);
            capture.setSelected(true);

        }

        final void init() {
            event.addFocusListener(this);
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(event);
            add(capture);
        }

        JCheckBox capture = new JCheckBox(Bundle.getMessage("MsgCaptureNext"));
        JTextField event = new JTextField(15);

        boolean waiting() {
            return capture.isSelected();
        }

        public void processFrame(AbstractMessage m) {
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
