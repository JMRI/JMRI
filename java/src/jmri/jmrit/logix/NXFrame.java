package jmri.jmrit.logix;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import jmri.JmriException;
import jmri.jmrit.roster.RosterSpeedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for defining and launching an entry/exit warrant. An NX warrant is a
 * warrant that can be defined on the run without a pre-recorded learn mode
 * session using a set script for ramping startup and stop throttle settings.
 * <p>
 * The route can be defined in a form or by mouse clicking on the OBlock
 * IndicatorTrack icons.
 * <br>
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Pete Cressman Copyright (C) 2009, 2010, 2015
 */
public class NXFrame extends WarrantRoute {

    private float _scale = 87.1f;
    private float _maxThrottle = 0.75f;
    private float _startDist;   // mm start distance to portal
    private float _stopDist;    // mm stop distance from portal

    private final JTextField _maxThrottleBox = new JTextField(6);
    private final JTextField _maxSpeedBox = new JTextField(6);
    private JButton _speedUnits;
    private final JTextField _originDist = new JTextField(6);
    private JButton _originUnits;
    private final JTextField _destDist = new JTextField(6);
    private JButton _destUnits;
    private JSpinner _timeIncre = new JSpinner(new SpinnerNumberModel(750, 200, 9000, 1));
    private JTextField _rampIncre = new JTextField(6);
    private final JRadioButton _forward = new JRadioButton();
    private final JRadioButton _reverse = new JRadioButton();
    private final JCheckBox _noRamp = new JCheckBox();
    private final JCheckBox _noSound = new JCheckBox();
    private final JCheckBox _stageEStop = new JCheckBox();
    private final JCheckBox _shareRouteBox = new JCheckBox();
    private final JCheckBox _haltStartBox = new JCheckBox();
    private final JRadioButton _runAuto = new JRadioButton(Bundle.getMessage("RunAuto"));
    private final JRadioButton _runManual = new JRadioButton(Bundle.getMessage("RunManual"));

    private JPanel _routePanel = new JPanel();
    private JPanel _autoRunPanel;
    private final JPanel __trainHolder = new JPanel();
    private JPanel _switchPanel;
    private JPanel _trainPanel;
    
    protected NXFrame() {
        super();
        init();
    }
    
    private void init() {
        if (log.isDebugEnabled()) log.debug("newInstance");
        WarrantFrame f = WarrantTableAction.getWarrantFrame();
        if (f != null) {    // only edit one warrant at a time.
            WarrantTableAction.closeWarrantFrame(f);
        }
        makeMenus();

        _routePanel = new JPanel();
        _routePanel.setLayout(new BoxLayout(_routePanel, BoxLayout.PAGE_AXIS));
        _routePanel.add(Box.createVerticalGlue());
        _routePanel.add(makeBlockPanels(true));
 
        _forward.setSelected(true);
        _stageEStop.setSelected(false);
        _haltStartBox.setSelected(false);
        _runAuto.setSelected(true);

        _autoRunPanel = makeAutoRunPanel();
        _switchPanel = makeSwitchPanel();
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(_routePanel);
        getContentPane().add(mainPanel);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeFrame();
            }
        });
        setAlwaysOnTop(true);
        setVisible(true);
        pack();
    }

    protected boolean isRouteSeaching() {
        return _routePanel.isVisible();
    }

    private void setPanel() {
        __trainHolder.add(_trainPanel);
    }
    private void setPanel(JPanel p) {
        JPanel con = (JPanel)getContentPane().getComponent(0);
        con.removeAll();
        con.add(p);
        con.add(_switchPanel);
        pack();
    }
    
    private JPanel makeSwitchPanel() {
        ButtonGroup bg = new ButtonGroup();
        bg.add(_runAuto);
        bg.add(_runManual);
        _runAuto.addActionListener((ActionEvent event) -> {
            setPanel();
            setPanel(_autoRunPanel);
        });
        _runManual.addActionListener((ActionEvent event) -> {
            setPanel(_trainPanel);
            _stageEStop.setSelected(false);
            _shareRouteBox.setSelected(false);
            _haltStartBox.setSelected(false);
        });
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.LINE_AXIS));
        pp.add(Box.createHorizontalGlue());
        pp.add(_runAuto);
        pp.add(Box.createHorizontalStrut(STRUT_SIZE));
        pp.add(_runManual);
        pp.add(Box.createHorizontalGlue());
        
        JPanel p = new JPanel();
        p.add(Box.createGlue());
        JButton button = new JButton(Bundle.getMessage("ButtonRoute"));
        button.addActionListener((ActionEvent e) -> {
            clearTempWarrant();
            JPanel con = (JPanel)getContentPane().getComponent(0);
            con.removeAll();
            con.add(_routePanel);
            pack();
        });
        p.add(button);
        p.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        button = new JButton(Bundle.getMessage("ButtonRunNX"));
        button.addActionListener((ActionEvent e) -> {
            clearTempWarrant();
            makeAndRunWarrant();
        });
        p.add(button);
        p.add(Box.createHorizontalStrut(2 * STRUT_SIZE));
        button = new JButton(Bundle.getMessage("ButtonCancel"));
        button.addActionListener((ActionEvent e) -> {
            closeFrame();
        });
        p.add(button);
        p.add(Box.createGlue());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(pp);
        panel.add(p);
        return panel;
    }

    private JButton getButton(String text) {
        JButton button = new JButton();
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        button.setText(text);
        button.setSelected(true);
        int bWidth = new JTextField(2).getPreferredSize().width;
        int bHeight = new JTextField(2).getPreferredSize().height;
        button.setMaximumSize(new Dimension(bWidth, bHeight));
        return button;
    }

    private void maxThrottleEventAction() {
        boolean isForward = _forward.isSelected();
        RosterSpeedProfile profile = _speedUtil.getSpeedProfile();
        if (profile != null) {
            NumberFormat formatter = NumberFormat.getNumberInstance(); 
            float num = 0;
            try {
                num =  formatter.parse(_maxThrottleBox.getText()).floatValue();
            } catch (java.text.ParseException pe) {
                _maxThrottleBox.setText("");
                return;
            }
            float speed = profile.getSpeed(num, isForward);
            if (_speedUnits.getText().equals("Mph")) {
                _maxSpeedBox.setText(formatter.format(speed * _scale * .0022369363f));                        
            } else {
                _maxSpeedBox.setText(formatter.format(speed * _scale * .0036f));                                               
            }
            return;
        }
        _maxSpeedBox.setText(Bundle.getMessage("NoData"));        
    }

    private JPanel makeAutoRunPanel() {
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));

        _speedUnits = getButton("Mph");
        _maxThrottleBox.addActionListener((ActionEvent evt)-> {
            maxThrottleEventAction();
        });

        
        _maxSpeedBox.addActionListener((ActionEvent evt)-> {
            boolean isForward = _forward.isSelected();
            RosterSpeedProfile profile = _speedUtil.getSpeedProfile();
            if (profile != null) {
                NumberFormat formatter = NumberFormat.getNumberInstance(); 
                float num = 0;
                try {
                    num =  formatter.parse(_maxSpeedBox.getText()).floatValue();
                } catch (java.text.ParseException pe) {
                    _maxSpeedBox.setText("");
                    return;
                }
                if (_speedUnits.getText().equals("Mph")) {
                    num = num * 447.04f / _scale;                        
                } else {
                    num = num * 277.7778f / _scale;                        
                }
                float throttle = profile.getThrottleSetting(num, isForward);
                if (throttle > 0.0f) {
                    _maxThrottleBox.setText(formatter.format(throttle));                    
                    return;
                }
            }
            _maxSpeedBox.setText(Bundle.getMessage("NoData"));
        });
        _speedUnits.addActionListener((ActionEvent evt)-> {
            NumberFormat formatter = NumberFormat.getNumberInstance(); 
            float num = 0;
            try {
                num =  formatter.parse(_maxSpeedBox.getText()).floatValue();
            } catch (java.text.ParseException pe) {
                return;
            }
            if (_speedUnits.getText().equals("Mph")) {
                _speedUnits.setText("Kmph");
                num = Math.round(num * 160.9344f);
                _maxSpeedBox.setText(formatter.format(num / 100));
            } else {
                num = Math.round(num * 62.137119f);
                _speedUnits.setText("Mph");
                _maxSpeedBox.setText(formatter.format(num / 100));
            }
        });
        p1.add(makeTextBoxPanel(false, _maxThrottleBox, "MaxSpeed", null));
        p1.add(makeTextAndButtonPanel(_maxSpeedBox, _speedUnits, "scaleSpeed", "ToolTipScaleSpeed"));

        _originUnits = getButton("In");
        _destUnits = getButton("In");
        
        _originUnits.addActionListener((ActionEvent evt)-> {
            NumberFormat formatter = NumberFormat.getNumberInstance(); 
            float num = 0;
            try {
                num =  formatter.parse(_originDist.getText()).floatValue();
            } catch (java.text.ParseException pe) {
                // errors reported later
            }
            if (_originUnits.getText().equals("In")) {
                _originUnits.setText("Cm");
                num = Math.round(num * 254f);
                _originDist.setText(formatter.format(num / 100));
            } else {
                num = Math.round(num * 100f / 2.54f);
                _originUnits.setText("In");
                _originDist.setText(formatter.format(num / 100));
            }
        });
        _destUnits.setActionCommand("In");
        _destUnits.addActionListener((ActionEvent evt)-> {
            NumberFormat formatter = NumberFormat.getNumberInstance(); 
            float num = 0;
            try {
                num =  formatter.parse(_destDist.getText()).floatValue();
            } catch (java.text.ParseException pe) {
                // errors reported later
            }
            if (_destUnits.getText().equals("In")) {
                _destUnits.setText("Cm");
                _destDist.setText(formatter.format(num * 2.54f));
            } else {
                _destUnits.setText("In");
                _destDist.setText(formatter.format(num / 2.54f));
            }
        });

        p1.add(makeTextAndButtonPanel(_originDist, _originUnits, "startDistance", "ToolTipStartDistance"));
        p1.add(makeTextAndButtonPanel(_destDist, _destUnits, "stopDistance", "ToolTipStopDistance"));
        p1.add(WarrantPreferencesPanel.timeIncrementPanel(false, _timeIncre));
        p1.add(WarrantPreferencesPanel.throttleIncrementPanel(false, _rampIncre));
        _rampIncre.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = _rampIncre.getText();
                boolean showdialog = false;
                try {
                    float incr = NumberFormat.getNumberInstance().parse(text).floatValue();
                    showdialog = (incr < 0.5f || incr > 25f);
                } catch (java.text.ParseException pe) {
                    showdialog = true;
                }
                if (showdialog) {
                    JOptionPane.showMessageDialog(null, Bundle.getMessage("rampIncrWarning", text),
                            Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        ButtonGroup bg = new ButtonGroup();
        bg.add(_forward);
        bg.add(_reverse);
        JPanel pp = new JPanel();
        pp.setLayout(new BoxLayout(pp, BoxLayout.LINE_AXIS));
        pp.add(Box.createHorizontalGlue());
        pp.add(makeTextBoxPanel(false, _forward, "forward", null));
        pp.add(makeTextBoxPanel(false, _reverse, "reverse", null));
        pp.add(Box.createHorizontalGlue());
        p1.add(pp);

        __trainHolder.setLayout(new BoxLayout(__trainHolder, BoxLayout.PAGE_AXIS));
        _trainPanel = makeTrainIdPanel(null);
        __trainHolder.add(_trainPanel);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.PAGE_AXIS));      
        p2.add(__trainHolder);
        p2.add(makeTextBoxPanel(_noRamp, "NoRamping", "ToolTipNoRamping"));
        p2.add(makeTextBoxPanel(_noSound, "NoSound", "ToolTipNoSound"));
        p2.add(makeTextBoxPanel(_stageEStop, "StageEStop", null));
        p2.add(makeTextBoxPanel(_haltStartBox, "HaltAtStart", null));
        p2.add(makeTextBoxPanel(_shareRouteBox, "ShareRoute", "ToolTipShareRoute"));

        JPanel autoRunPanel = new JPanel();
        autoRunPanel.setLayout(new BoxLayout(autoRunPanel, BoxLayout.PAGE_AXIS));
        JPanel ppp = new JPanel();
        ppp.setLayout(new BoxLayout(ppp, BoxLayout.LINE_AXIS));
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        ppp.add(p1);
        ppp.add(Box.createHorizontalGlue());
        ppp.add(p2);
        ppp.add(Box.createHorizontalStrut(STRUT_SIZE));
        autoRunPanel.add(ppp);

        _forward.addActionListener((ActionEvent evt)-> {
            maxThrottleEventAction();
        });
        _reverse.addActionListener((ActionEvent evt)-> {
            maxThrottleEventAction();
        });

        return autoRunPanel;
    }
    
    private void updateAutoRunPanel() {
        _startDist = getPathLength(_orders.get(0)) / 2;
        _stopDist = getPathLength(_orders.get(_orders.size()-1)) / 2;
        NumberFormat formatter = NumberFormat.getNumberInstance(); 
        if (_originUnits.getText().equals("In")) {
            float num = Math.round(_startDist * 100 / 25.4f);
            _originDist.setText(formatter.format(num / 100f));
        } else {
            float num = Math.round(_startDist * 100);
            _originDist.setText(formatter.format(num / 1000f));
        }
        if (_destUnits.getText().equals("In")) {
            float num = Math.round(_stopDist * 100 / 25.4f);
            _destDist.setText(formatter.format(num / 100f));
        } else {
            float num = Math.round(_stopDist * 100);
            _destDist.setText(formatter.format(num / 1000f));
        }
        _autoRunPanel.repaint();
    }

    private void makeMenus() {
        setTitle(Bundle.getMessage("AutoWarrant"));
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.logix.NXWarrant", true);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        String property = e.getPropertyName();
//        if (log.isDebugEnabled()) log.debug("propertyChange \""+property+
//                                            "\" old= "+e.getOldValue()+" new= "+e.getNewValue()+
//                                            " source= "+e.getSource().getClass().getName());
        if (property.equals("DnDrop")) {
            doAction(e.getSource());
        }
    }

    /**
     * Called by {@link jmri.jmrit.logix.RouteFinder#run()}. If all goes well,
     * WarrantTableFrame.runTrain(warrant) will run the warrant
     *
     * @param orders list of block orders
     */
    @Override
    public void selectedRoute(ArrayList<BlockOrder> orders) {
        JPanel con = (JPanel)getContentPane().getComponent(0);
        con.removeAll();
        if (_runAuto.isSelected()) {
            con.add(_autoRunPanel);            
        } else {
            con.add(_trainPanel);
        }
        con.add(_switchPanel);
        updateAutoRunPanel();
        pack();
    }

    private void makeAndRunWarrant() {
        if (log.isDebugEnabled()) {
            log.debug("NXFrame selectedRoute()");
        }
        String msg = getBoxData();
        if (msg == null) {
            msg = checkLocoAddress();
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        // There is a dccAddress so a throttle can be acquired
        String s = ("" + Math.random()).substring(2);
        Warrant warrant = new Warrant("IW" + s, "NX(" + getAddress() + ")");
        warrant.setBlockOrders(_orders);
        warrant.setTrainName(getTrainName());
        warrant.setNoRamp(_noRamp.isSelected());
        _speedUtil.setIsForward(_forward.isSelected());
        // position distance from start of path
        _speedUtil.setDistanceTravelled(getPathLength(_orders.get(0)) - _startDist);
        warrant.setSpeedUtil(_speedUtil);   // transfer SpeedUtil to warrant
        if (log.isDebugEnabled()) log.debug("Warrant {). Route and loco set.", warrant.getDisplayName());
        int mode;
        if (!_runManual.isSelected()) {
            mode = Warrant.MODE_RUN;
            warrant.setShareRoute(_shareRouteBox.isSelected());
            msg = makeCommands(warrant);
        } else {
            mode = Warrant.MODE_MANUAL;
        }
        WarrantTableFrame tableFrame = WarrantTableFrame.getDefault();
        if (msg == null) {
            tableFrame.getModel().addNXWarrant(warrant);   //need to catch propertyChange at start
            if (log.isDebugEnabled()) {
                log.debug("NXWarrant added to table");
            }
            msg = tableFrame.runTrain(warrant, mode);
            tableFrame.scrollTable();
        }
        if (msg != null) {
            if (log.isDebugEnabled()) {
                log.debug("WarrantTableFrame run warrant. msg= " + msg + " Remove warrant " + warrant.getDisplayName());
            }
            tableFrame.getModel().removeWarrant(warrant, false);
        }

        if (msg == null && mode == Warrant.MODE_RUN) {
            if (_haltStartBox.isSelected()) {
                class Halter implements Runnable {

                    Warrant war;

                    Halter(Warrant w) {
                        war = w;
                    }

                    @Override
                    public void run() {
                        int limit = 0;
                        try {
                            // wait until _engineer is assigned so HALT can take effect
                            while (!war.controlRunTrain(Warrant.HALT) && limit < 3000) {
                                Thread.sleep(200);
                                limit += 200;
                            }
                        } catch (InterruptedException e) {
                            war.controlRunTrain(Warrant.HALT);
                        }
                    }
                }
                Halter h = new Halter(warrant);
                new Thread(h).start();
            }
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
        } else {
            closeFrame();
            if (log.isDebugEnabled()) {
                log.debug("Close Frame.");
            }
        }
    }

    protected void closeFrame() {
        clearTempWarrant();
        WarrantTableAction.closeNXFrame(this);
    }

    // for the convenience of testing
    protected void setScale(float s) {
        _scale = s;
    }

    // for testing
    protected void setMaxSpeed(float s) {
        _maxThrottle = s;
        _maxThrottleBox.setText(NumberFormat.getNumberInstance().format(s));
    }
    
    private String getBoxData() {
        String text = null;
        float maxSpeed;
        float oDist;
        float dDist;
        NumberFormat formatter = NumberFormat.getNumberInstance(); 
        try {
            text = _maxThrottleBox.getText();
            maxSpeed = formatter.parse(text).floatValue();
        } catch (java.text.ParseException pe) {
            if (text==null) {
                text = "\"\"";
            }
            return Bundle.getMessage("badSpeed", text);
        }
        try {
            text = _originDist.getText();
            oDist = formatter.parse(text).floatValue();
            text = _destDist.getText();
            dDist = formatter.parse(text).floatValue();
        } catch (java.text.ParseException pe) {
            return Bundle.getMessage("MustBeFloat", text);
        }

        try {
            _startDist = checkDistance(_originUnits.getText().equals("In"), oDist, _orders.get(0));
        } catch (JmriException je) {
            displayDistance(_destUnits.getText().equals("In"), oDist, _originDist, _orders.get(0));
            return je.getMessage();
        }

        try {
            _stopDist = checkDistance(_destUnits.getText().equals("In"), dDist, _orders.get(_orders.size()-1));
        } catch (JmriException je) {
            displayDistance(_destUnits.getText().equals("In"), dDist, _destDist, _orders.get(_orders.size()-1));
            return je.getMessage();
        }

        if (maxSpeed > 1.0f || maxSpeed < 0.008f) {
            return Bundle.getMessage("badSpeed", maxSpeed);
        }
        _maxThrottle = maxSpeed;

        setAddress();

        int time = (Integer)_timeIncre.getValue();
        _speedUtil.setRampTimeIncrement(time);

        try {
            text = _rampIncre.getText();
            float incre = NumberFormat.getNumberInstance().parse(text).floatValue();
            if (incre < 0.5f || incre > 25f) {
                return Bundle.getMessage("rampIncrWarning", text);                
            } else {
                _speedUtil.setRampThrottleIncrement(incre/100);
            }
        } catch (java.text.ParseException pe) {
            return Bundle.getMessage("MustBeFloat", text);
        }
        
        _speedUtil.resetSpeedProfile();
        return null;
    }

    private float checkDistance(boolean isInches, float distance, BlockOrder bo) throws JmriException {
        float pathLen = getPathLength(bo);
        if (pathLen <= 0) {
            throw new JmriException(Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName()));                        
        }
        if (isInches){
            distance *= 25.4f;
            if (distance < 0 || distance > pathLen) {
                pathLen /= 25.4;
                distance /= 25.4;
                throw new JmriException(Bundle.getMessage(
                        "BadLengthIn", bo.getPathName(), bo.getBlock().getDisplayName(), pathLen, distance));                                        
            }
        } else {
            distance *= 10f;
            if (distance < 0 || distance > pathLen) {
                pathLen /= 10;
                distance /= 10;
                throw new JmriException(Bundle.getMessage(
                        "BadLengthCm", bo.getPathName(), bo.getBlock().getDisplayName(), pathLen, distance));                                        
            }
        }
        return distance;
    }

    private void displayDistance(boolean isInches, float distance, JTextField textBox, BlockOrder bo) {
        float pathLen = getPathLength(bo);
        if (pathLen <= 0) {
            return;                        
        }
        NumberFormat formatter = NumberFormat.getNumberInstance();
        if (distance < 0f) {
            textBox.setText(formatter.format(0f));
        } else {
            if (isInches) {
                float num = Math.round(pathLen * 100 / 25.4f);
                if (distance*25.4f > pathLen) {
                    textBox.setText(formatter.format(num / 100));
                }
            } else {
                float num = Math.round(pathLen * 100);
                if (distance*10 > pathLen) {
                    textBox.setText(formatter.format(num / 1000));
                }
            }
        }
    }

    private float getPathLength(BlockOrder bo) {
        float len = bo.getPath().getLengthMm();
        if (len <= 0) {
            len = bo.getTempPathLen();
            if ( len <= 0) {
                String sLen = JOptionPane.showInputDialog(this, 
                        Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName())
                        + Bundle.getMessage("getPathLength", bo.getPathName(), bo.getBlock().getDisplayName()),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                try {
                    len = NumberFormat.getNumberInstance().parse(sLen).floatValue();                    
                } catch (java.text.ParseException  pe) {
                    len = -1.0f;
                } catch (java.lang.NullPointerException  npe) {
                    len = -1.0f;
                }
                bo.setTempPathLen(len);
            }
        }
       return len;
    }
    private float adjustdistance(float fromSpeed, float toSpeed, float distance, BlockOrder bo) throws JmriException {
        float pathLen = getPathLength(bo);
        if (pathLen <= 0) {
            throw new JmriException(Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName()));
        }
        int timeIncrement = _speedUtil.getRampTimeIncrement();
        float minDist = _speedUtil.getDistanceOfSpeedChange(fromSpeed, toSpeed, timeIncrement) +.1f;
        if (distance < minDist) {
            distance = minDist;
        } else if (distance > pathLen - minDist) {
            distance = pathLen - minDist;
        }
        return distance;
    }
    /*
     * Return length of warrant route in mm.  Assume start and end is in the middle of first
     * and last blocks.  Use a default length for blocks with unspecified length.
     */
    private float getTotalLength() throws JmriException {
        float totalLen = 0.0f;
        List<BlockOrder> orders = getOrders();
        float throttleIncrement = _speedUtil.getRampThrottleIncrement();
        try {
            _startDist = adjustdistance(0f, throttleIncrement, _startDist, orders.get(0));
            totalLen = _startDist;
            for (int i = 1; i < orders.size() - 1; i++) {
                BlockOrder bo = orders.get(i);
                float pathLen = getPathLength(bo);
                if (pathLen <= 0) {
                    throw new JmriException(Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName()));
                }
                totalLen += pathLen;
            }
            _stopDist = adjustdistance(throttleIncrement, 0f, _stopDist, orders.get(0));
            totalLen += _stopDist;
        } catch (JmriException je) {
            throw je;
        }
        return totalLen;
    }

    private String makeCommands(Warrant w) {

        int nextIdx = 0;        // block index - increment after getting a block order
        List<BlockOrder> orders = getOrders();
        BlockOrder bo = orders.get(nextIdx++);
        String blockName = bo.getBlock().getDisplayName();
        boolean hasProfileSpeeds = _speedUtil.profileHasSpeedInfo();

        int cmdNum;
        w.addThrottleCommand(new ThrottleSetting(0, "F0", "true", blockName));
        if (_forward.isSelected()) {
            w.addThrottleCommand(new ThrottleSetting(100, "Forward", "true", blockName));
            if (!_noSound.isSelected()) {
                w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
                w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
                w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
                w.addThrottleCommand(new ThrottleSetting(2500, "F2", "false", blockName));
                cmdNum = 7;
            } else {
                cmdNum = 3;
            }
        } else {
            w.addThrottleCommand(new ThrottleSetting(100, "Forward", "false", blockName));
            if (!_noSound.isSelected()) {
                w.addThrottleCommand(new ThrottleSetting(1000, "F3", "true", blockName));
                w.addThrottleCommand(new ThrottleSetting(500, "F3", "false", blockName));
                w.addThrottleCommand(new ThrottleSetting(500, "F3", "true", blockName));
                w.addThrottleCommand(new ThrottleSetting(500, "F1", "true", blockName));
                cmdNum = 6;
            } else {
                cmdNum = 2;
            }
        }

        float totalLen;
        try {
            totalLen = getTotalLength();
        } catch (JmriException je) {
            return je.getMessage();
        }

        RampData upRamp;
        RampData downRamp;
        ListIterator<Float> downIter;
        float intervalDist;
        do {
            upRamp = _speedUtil.getRampForSpeedChange(0f, _maxThrottle);
            downRamp = _speedUtil.getRampForSpeedChange(_maxThrottle, 0f);
            downIter = downRamp.speedIterator(false);
            float prevSetting = downIter.previous().floatValue();   // top value is _maxThrottle 
            _maxThrottle -= prevSetting  - downIter.previous().floatValue();    // last throttle increment
            // distance attaining final speed
            intervalDist = _speedUtil.getDistanceOfSpeedChange(_maxThrottle, prevSetting, downRamp.getRampTimeIncrement());
            log.debug("Route length= {}, upRampLength= {}, dnRampLength= {}",
                    totalLen, upRamp.getRampLength(), downRamp.getRampLength());
        } while ((upRamp.getRampLength() + intervalDist + downRamp.getRampLength()) > totalLen);
        _maxThrottle = downRamp.getMaxSpeed();

        if (log.isDebugEnabled()) {
            if (hasProfileSpeeds) {
                log.debug("maxThrottle= {} ({} meters per sec), scale= {}", 
                        _maxThrottle, _speedUtil.getTrackSpeed(_maxThrottle), _scale);                
            } else {
                log.debug("maxThrottle= {} scale= {} no SpeedProfile data", _maxThrottle, _scale);                                
            }
        }
        float blockLen = _startDist;    // length of path in current block

        // start train
        float speedTime = 0;    // ms time to complete speed step from last block
        float noopTime = 0;     // ms time for entry into next block
        ListIterator<Float> iter = upRamp.speedIterator(true);
        float curThrottle = iter.next();  // throttle setting
        float nextThrottle = 0f;
        float curDistance = 0;  // current distance traveled mm
        float blkDistance = 0;  // distance traveled in current block mm
        float upRampLength = upRamp.getRampLength();
        float remRamp = upRampLength;
        float remTotal = totalLen;
        float dnRampLength = downRamp.getRampLength();
        int timeInterval = downRamp.getRampTimeIncrement();
        boolean rampsShareBlock = false;

        if (log.isDebugEnabled()) {
            log.debug("Start in block \"{}\" startDist= {} stopDist= {}", blockName, _startDist, _stopDist);
        }
        while (iter.hasNext()) {       // ramp up loop

            while (blkDistance < blockLen && iter.hasNext()) {
                nextThrottle = iter.next().floatValue();
                float dist = _speedUtil.getDistanceOfSpeedChange(curThrottle, nextThrottle, timeInterval);
                if (blkDistance + dist <= blockLen) {
                    blkDistance += dist;
                    remRamp -= dist;
                    curThrottle = nextThrottle;
                    w.addThrottleCommand(new ThrottleSetting((int) speedTime, "Speed",
                            Float.toString(curThrottle), blockName, 
                            (hasProfileSpeeds ? _speedUtil.getTrackSpeed(curThrottle) : 0.0f)));
                    if (log.isDebugEnabled()) {
                        log.debug("{}. Ramp Up in block \"{}\" to speed {} in {}ms to distance= {}mm, remRamp= {}",
                                ++cmdNum, blockName, curThrottle, (int) speedTime, blkDistance, remRamp);
                    }
                    speedTime = timeInterval;
                } else {
                    iter.previous();
                    break;
                }
            }
            curDistance += blkDistance;

            if (blkDistance >= blockLen) {
                // Possible case where blkDistance can exceed the length of a block that was just entered.
                // Skip over block and move to next block and adjust the distance times into that block
                noopTime = _speedUtil.getTimeForDistance(curThrottle, blockLen);   // noop distance to run through block 
                speedTime = _speedUtil.getTimeForDistance(curThrottle, blkDistance - blockLen);
                curDistance += blockLen;
            } else {
                // typical case where next speed change broke out of above loop
                noopTime = _speedUtil.getTimeForDistance(curThrottle, (blockLen - blkDistance));   // time to next block
                if (noopTime > timeInterval) {  // after last speed change
                    speedTime = 0;  // irrelevant, loop will end
                    if (!iter.hasNext()) {
                        noopTime += timeInterval;   // add time to complete last speed change
                    }
                } else {
                    speedTime = timeInterval - noopTime;   // time to next speed change
                }
                curDistance += blockLen - blkDistance;  // noop distance
            }

            // break out here if done or deceleration is to be started in this block
            if (!iter.hasNext() || remTotal - blockLen <= dnRampLength) {
                break;
            }

            remTotal -= blockLen;
            if (log.isDebugEnabled()) {
                log.debug("Leave RampUp block \"{}\"  blkDistance= {}, blockLen= {} remRamp= {} curDistance= {} remTotal={}",
                        blockName, blkDistance, blockLen, remRamp, curDistance, remTotal);
            }
            if (nextIdx < orders.size()) {
                bo = orders.get(nextIdx++);
                blockLen = getPathLength(bo);
                if (blockLen <= 0) {
                    return Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName());
                 }
                blockName = bo.getBlock().getDisplayName();
                w.addThrottleCommand(new ThrottleSetting((int) noopTime, "NoOp", "Enter Block", blockName,
                        (hasProfileSpeeds ? _speedUtil.getTrackSpeed(curThrottle) : 0.0f)));
                if (log.isDebugEnabled()) {
                    log.debug("{}. Enter RampUp block \"{}\" noopTime= {}, speedTime= {} blockLen= {}, remTotal= {}",
                        cmdNum++, blockName, noopTime, speedTime, blockLen, remTotal);
                }
            }
            blkDistance = _speedUtil.getDistanceTraveled(curThrottle, Warrant.Normal, speedTime);
        }
        if (log.isDebugEnabled()) {
            log.debug("Ramp Up done at block \"{}\" curThrottle={} blkDistance={} curDistance={} remTotal= {} remRamp={}", 
                    blockName, curThrottle, blkDistance, curDistance, remTotal, remRamp);
        }

        if (remTotal - blockLen > dnRampLength) {    // At maxThrottle, remainder of block at max speed
            if (nextIdx < orders.size()) {    // not the last block
                remTotal -= blockLen;
                bo = orders.get(nextIdx++);
                blockLen = getPathLength(bo);
                if (blockLen <= 0) {
                    return Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName());
                 }
                blockName = bo.getBlock().getDisplayName();
                w.addThrottleCommand(new ThrottleSetting((int) noopTime, "NoOp", "Enter Block", blockName,
                        (hasProfileSpeeds ? _speedUtil.getTrackSpeed(curThrottle) : 0.0f)));
                if (log.isDebugEnabled()) {
                    log.debug("{}. Enter block \"{}\" noopTime= {}, blockLen= {}, curDistance={}",
                            cmdNum++, blockName, noopTime, blockLen, curDistance);
                }
                blkDistance = 0;
            }

            // run through mid route at max speed
            while (nextIdx < orders.size() && remTotal - blockLen > dnRampLength) {
                remTotal -= blockLen;
                // constant speed, get time to next block
                noopTime = _speedUtil.getTimeForDistance(curThrottle, blockLen);   // time to next block
                curDistance += blockLen;
                if (log.isDebugEnabled()) {
                    log.debug("Leave MidRoute block \"{}\" noopTime= {} blockLen= {} curDistance={} remTotal= {}",
                            blockName, noopTime, blockLen, curDistance, remTotal);
                }
                bo = orders.get(nextIdx++);
                blockLen = getPathLength(bo);
                if (blockLen <= 0) {
                    return Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName());
                 }
                blockName = bo.getBlock().getDisplayName();
                if (nextIdx == orders.size()) {
                    blockLen = _stopDist;
                }
                w.addThrottleCommand(new ThrottleSetting((int) noopTime, "NoOp", "Enter Block", blockName,
                        (hasProfileSpeeds ? _speedUtil.getTrackSpeed(curThrottle) : 0.0f)));
                if (log.isDebugEnabled()) {
                    log.debug("{}. Enter MidRoute block \"{}\" noopTime= {}, blockLen= {}, curDistance={}",
                            cmdNum++, blockName, noopTime, blockLen, curDistance);
                }
             }
            blkDistance = 0;
       } else {
            // else Start ramp down in current block
            rampsShareBlock = true;
        }

        // Ramp down.
        remRamp = dnRampLength;
        iter = downRamp.speedIterator(false);
        iter.previous();   // discard, equals curThrottle
        float remMaxSpeedDist;
        if (!rampsShareBlock) {
            remMaxSpeedDist = remTotal - dnRampLength;
        } else {
            remMaxSpeedDist = totalLen - upRampLength - dnRampLength;
        }
        // distance in block where down ramp is started
        blkDistance += remMaxSpeedDist;
        // time to start down ramp
        speedTime = _speedUtil.getTimeForDistance(curThrottle, remMaxSpeedDist) + timeInterval;

        if (log.isDebugEnabled()) {
            log.debug("Begin Ramp Down at block \"{}\" blockLen={}, at distance= {} curDistance = {} remTotal= {} curThrottle= {} ({})",
                    blockName, blockLen, blkDistance, curDistance, remTotal, curThrottle, remMaxSpeedDist);
        }

        while (iter.hasPrevious()) {
            boolean atLastBlock = false;
            if (nextIdx == orders.size()) { // at last block
                atLastBlock = true;
                if (_stageEStop.isSelected()) {
                    w.addThrottleCommand(new ThrottleSetting(50, "Speed", "-0.5", blockName,
                            (hasProfileSpeeds ? _speedUtil.getTrackSpeed(curThrottle) : 0.0f)));
                    curThrottle = -0.5f;
                    if (log.isDebugEnabled()) {
                        log.debug("{}. At block \"{}\" EStop set speed= {}", cmdNum++, blockName, -0.5);
                    }
                    break;
                }
            }

            do /*while (blkDistance < blockLen && iter.hasPrevious())*/ {
                boolean hasPrevious = false;
                if (iter.hasPrevious()) {
                    nextThrottle = iter.previous();
                    hasPrevious = true;
                }
                float dist = _speedUtil.getDistanceOfSpeedChange(curThrottle, nextThrottle, timeInterval);
                blkDistance += dist;
                remRamp -= dist;                
                curThrottle = nextThrottle;
                if (curThrottle <= 0f && !atLastBlock) {
                    log.warn("Set curThrottle = {} in block \"{}\" (NOT the last block)!", curThrottle, blockName);
                    break;
                }
                if (hasPrevious) {
                    w.addThrottleCommand(new ThrottleSetting((int) speedTime, "Speed", Float.toString(curThrottle), blockName,
                            (hasProfileSpeeds ? _speedUtil.getTrackSpeed(curThrottle) : 0.0f)));
                    if (log.isDebugEnabled()) {
                        log.debug("{}. Ramp Down in block \"{}\" to curThrottle {} in {}ms to distance= {}mm, remRamp= {}",
                                ++cmdNum, blockName, curThrottle, (int) speedTime, blkDistance, remRamp);
                    }
                } else {
                    if (curThrottle > 0f) {
                        log.warn("No speed setting after command {} in block \"{}\". curThrottle= {} blkDistance= {}mm",
                                cmdNum, blockName, curThrottle, blkDistance);
                    }
                    break;
                }
                speedTime = timeInterval;
            } while (blkDistance < blockLen);
            curDistance += blkDistance;

            if (log.isDebugEnabled()) {
                log.debug("Leave RampDown block \"{}\"  blkDistance= {}, blockLen= {} remRamp= {} curDistance= {} remTotal= {}",
                        blockName, blkDistance, blockLen, remRamp, curDistance, remTotal);
            }
            if (blkDistance >= blockLen) {
                // typical case where next speed change broke out of above loop
                speedTime = _speedUtil.getTimeForDistance(curThrottle, blkDistance - blockLen); // time to run in next block
                if (speedTime > timeInterval) {
                    noopTime = 0;
                } else {
                    noopTime = timeInterval - speedTime;
                }
            } else {
                speedTime = timeInterval - noopTime;
            }

            remTotal -= blockLen;
            if (!atLastBlock) {
                curDistance += blockLen - blkDistance;  // noop distance
                bo = orders.get(nextIdx++);
                if (nextIdx == orders.size()) {
                    blockLen = _stopDist;
                } else {
                    blockLen = getPathLength(bo);
                    if (blockLen <= 0) {
                        return Bundle.getMessage("zeroPathLength", bo.getPathName(), bo.getBlock().getDisplayName());
                     }
                }
                blockName = bo.getBlock().getDisplayName();
                w.addThrottleCommand(new ThrottleSetting((int) noopTime, "NoOp", "Enter Block", blockName,
                        (hasProfileSpeeds ? _speedUtil.getTrackSpeed(curThrottle) : 0.0f)));
                if (log.isDebugEnabled()) {
                    log.debug("{}. Enter block \"{}\" noopTime= {}ms, blockLen= {}, curDistance={}",
                            cmdNum++, blockName, noopTime, blockLen, curDistance);
                }
                blkDistance = _speedUtil.getDistanceTraveled(curThrottle, Warrant.Normal, speedTime);
            } else {
                blkDistance = 0f;
            }
        }

        // Ramp down finished
        log.debug("Ramp down done at block \"{}\",  remRamp= {}, curDistance= {} remRamp= {}",
                blockName, remRamp, curDistance, remTotal);
        if (!_noSound.isSelected()) {
            w.addThrottleCommand(new ThrottleSetting(500, "F1", "false", blockName));
            w.addThrottleCommand(new ThrottleSetting(1000, "F2", "true", blockName));
            w.addThrottleCommand(new ThrottleSetting(3000, "F2", "false", blockName));
        }
        w.addThrottleCommand(new ThrottleSetting(1000, "F0", "false", blockName));
        /*      if (_addTracker.isSelected()) {
            WarrantTableFrame._defaultAddTracker = true;
            w.addThrottleCommand(new ThrottleSetting(10, "START TRACKER", "", blockName));
        } else {
            WarrantTableFrame._defaultAddTracker = false;
        }*/
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(NXFrame.class);
}
