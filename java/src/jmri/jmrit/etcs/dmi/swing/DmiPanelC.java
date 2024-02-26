package jmri.jmrit.etcs.dmi.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.jmrit.etcs.ResourceUtil;

/**
 * Class to display features of ERTMS DMI Panel C,
 * Large Buttons underneath Speedometer.
 * @author Steve Young Copyright (C) 2024
 */
public class DmiPanelC extends JPanel {

    private final JButton c1Button;
    private final JButton c2Button;
    private final JLabel c3Label;
    private final JPanel c1Panel;
    private final JLabel c6Label;
    private final JLabel c8Label;
    private final JLabel c9Label;
    private final DmiPanel mainPanel;

    private final transient PropertyChangeListener pclC1 = (PropertyChangeEvent evt) ->  changeC1Border(); 
    private final transient PropertyChangeListener pclC2 = (PropertyChangeEvent evt) ->  changeC2Border(); 
    private boolean nextc1FlashState = true;
    private boolean nextc2FlashState = true;

    public DmiPanelC(@Nonnull DmiPanel panel){
        super();
        setLayout(null);

        setBackground(DmiPanel.BACKGROUND_COLOUR);
        setBounds(0, 315, 334, 50);

        mainPanel = panel;

        c1Panel = new JPanel();
        c1Panel.setLayout(new GridLayout(1, 1)); // Using GridLayout with 1 row and 1 column

        JPanel c2Panel = new JPanel(); // tunnel stopping area
        JPanel c3Panel = new JPanel();
        JPanel c4Panel = new JPanel();
        JPanel c5Panel = new JPanel();
        JPanel c6Panel = new JPanel();
        JPanel c7Panel = new JPanel();
        JPanel c8Panel = new JPanel();
        JPanel c9Panel = new JPanel();

        c8Panel.setBounds(0, 0, 54, 25);
        c9Panel.setBounds(0, 25, 54, 25);
        c2Panel.setBounds(54, 0, 37, 50);
        c3Panel.setBounds(54+37, 0, 37, 50);
        c4Panel.setBounds(54+37+37, 0, 37, 50);
        c1Panel.setBounds(54+37+37+37, 0, 58, 50);
        c5Panel.setBounds(54+37+37+37+58, 0, 37, 50);
        c6Panel.setBounds(54+37+37+37+58+37, 0, 37, 50);
        c7Panel.setBounds(54+37+37+37+58+37+37, 0, 37, 50);

        c6Label = new JLabel();
        c8Label = new JLabel();
        c9Label = new JLabel();

        c6Panel.add(c6Label);
        c8Panel.add(c8Label);
        c9Panel.add(c9Label);

        setBg(c1Panel);
        setBg(c2Panel);
        setBg(c3Panel);
        setBg(c4Panel);
        setBg(c5Panel);
        setBg(c6Panel);
        setBg(c7Panel);
        setBg(c8Panel);
        setBg(c9Panel);

        c1Panel.setBackground(DmiPanel.BACKGROUND_COLOUR);

        c1Button = new JButton(); // Level Transition
        c1Button.setBorder(DmiPanel.BORDER_NORMAL);
        c1Button.setFocusable(false);
        c1Button.setVisible(false);
        c1Button.setBackground(DmiPanel.BACKGROUND_COLOUR);
        c1Button.setContentAreaFilled(false); // Make the button transparent
        c1Button.addActionListener(this::acknowledgeLevelPressed);
        c1Button.setName("levelTransitionNotificationButton");

        c2Button = new JButton();
        c2Button.setBorder(DmiPanel.BORDER_NORMAL);
        c2Button.setFocusable(false);
        c2Button.setVisible(false);
        c2Button.setBackground(DmiPanel.BACKGROUND_COLOUR);
        c2Button.setContentAreaFilled(false); // Make the button transparent
        c2Button.addActionListener(this::acknowledgeTunnelStopPressed);
        c2Button.setName("TunnelStopNotificationButton");
        // Tunnel Stopping Area Announce
        c2Button.setIcon(ResourceUtil.getImageIcon("TC_37.bmp"));

        // Tunnel Stopping Area
        c2Button.setDisabledIcon(ResourceUtil.getImageIcon("TC_36.bmp"));

        c3Label = new JLabel();
        c3Label.setBounds(54+37, 0, 37+37, 50);
        c3Label.setForeground(DmiPanel.GREY);
        c3Label.setVerticalAlignment(SwingConstants.CENTER);
        c3Label.setHorizontalAlignment(SwingConstants.CENTER);
        c3Label.setFont(new Font(DmiPanel.FONT_NAME, Font.BOLD, 18));
        add(c3Label);

        c1Panel.add(c1Button);
        c2Panel.add(c2Button);
        // c3Panel.add(c3Label);

        add(c8Panel);
        add(c9Panel);
        add(c2Panel);
        add(c3Panel);
        add(c4Panel);
        add(c1Panel);
        add(c5Panel);
        add(c6Panel);
        add(c7Panel);

        DmiPanelC.this.setLevel(-2);
        DmiPanelC.this.setTunnelStoppingDistance(0);
        DmiPanelC.this.setLevelTransition(-2, false);
    }

    /**
     * Set ERTMS Level Transition symbol / button active.
     * Note that some valid options for ERTMS3.6 are invalid for ERTMS4 ,
     * e.g. 2, false.
     * 
     * @param newLevel the level to set.
     * @param ackRequired true if acknowledgement required by driver, false if automatic.
     */
    protected void setLevelTransition(int newLevel, boolean ackRequired) {
        mainPanel.removeFlashListener(pclC1, false);
        c1Button.setBorder(DmiPanel.BORDER_NORMAL);
        setC1LevelTransitionIcon(newLevel, ackRequired);
        if ( ackRequired ) {
            startC1Flash();
        }
        
    }

    protected void setModeAcknowledge(int newMode){
        c1Button.setVisible(newMode != DmiPanel.MODE_NONE);
        mainPanel.removeFlashListener(pclC1, false);
        if (newMode == DmiPanel.MODE_NONE) {
            return;
        }
        startC1Flash();
        c1Button.setEnabled(true);
        c1Button.setIcon(getC1ModeAcknowledgeIcon(newMode));
        c1Button.setSelectedIcon(getC1ModeAcknowledgeIcon(newMode));
        c1Button.setActionCommand(getC1ActionEventText(newMode));
    }

    private static Icon getC1ModeAcknowledgeIcon(int newMode){
        switch (newMode) {
            case DmiPanel.MODE_SHUNTING:
                return ResourceUtil.getImageIcon( "MO_02.bmp");
            case DmiPanel.MODE_TRIP:
                return ResourceUtil.getImageIcon( "MO_05.bmp");
            case DmiPanel.MODE_ON_SIGHT:
                return ResourceUtil.getImageIcon( "MO_08.bmp");
            case DmiPanel.MODE_STAFF_RESPONSIBLE:
                return ResourceUtil.getImageIcon( "MO_10.bmp");
            case DmiPanel.MODE_REVERSING:
                return ResourceUtil.getImageIcon( "MO_15.bmp");
            case DmiPanel.MODE_UNFITTED:
                return ResourceUtil.getImageIcon( "MO_17.bmp");
            case DmiPanel.MODE_NATIONAL_SYSTEM:
                return ResourceUtil.getImageIcon( "MO_20.bmp");
            case DmiPanel.MODE_LIMITED_SUPERVISION:
                return ResourceUtil.getImageIcon( "MO_22.bmp");
            default:
                log.warn("No Icon for C1 SetLevel with Confirmation {}", newMode );
                return null;
        }
    }

    private String getC1ActionEventText(int mode) {
        switch (mode) {
            case DmiPanel.MODE_SHUNTING:
                return DmiPanel.PROP_CHANGE_MODE_SHUNTING_ACK;
            case DmiPanel.MODE_TRIP:
                return DmiPanel.PROP_CHANGE_MODE_TRIP_ACK;
            case DmiPanel.MODE_ON_SIGHT:
                return DmiPanel.PROP_CHANGE_MODE_ON_SIGHT_ACK;
            case DmiPanel.MODE_STAFF_RESPONSIBLE:
                return DmiPanel.PROP_CHANGE_MODE_STAFF_RESPONSIBLE_ACK;
            case DmiPanel.MODE_REVERSING:
                return DmiPanel.PROP_CHANGE_MODE_REVERSING_ACK;
            case DmiPanel.MODE_UNFITTED:
                return DmiPanel.PROP_CHANGE_MODE_UNFITTED_ACK;
            case DmiPanel.MODE_NATIONAL_SYSTEM:
                return DmiPanel.PROP_CHANGE_MODE_NATIONAL_SYSTEM_ACK;
            case DmiPanel.MODE_LIMITED_SUPERVISION:
                return DmiPanel.PROP_CHANGE_MODE_LIMITED_SUPERVISION_ACK;
            default:
                log.warn("No ActionText for C1 SetLevel with Confirmation {}", mode );
                return null;
        }
    }

    private void startC1Flash(){
        nextc1FlashState = true;
        changeC1Border();
        mainPanel.addFlashListener(pclC1, false);
    }

    private void setC1LevelTransitionIcon(int newLevel, boolean ackRequired) {
        if ( ackRequired ) {
            switch (newLevel) {
                case -1: // ntc
                    c1Button.setIcon(ResourceUtil.getImageIcon( "LE_09.bmp"));
                    c1Button.setActionCommand(DmiPanel.PROP_CHANGE_LEVEL_NTC_TRANSITION_ACK);
                    break;
                case 0:
                    c1Button.setIcon(ResourceUtil.getImageIcon("LE_07.bmp"));
                    c1Button.setActionCommand(DmiPanel.PROP_CHANGE_LEVEL_0_TRANSITION_ACK);
                    break;
                case 1:
                    c1Button.setIcon(ResourceUtil.getImageIcon("LE_11.bmp"));
                    c1Button.setActionCommand(DmiPanel.PROP_CHANGE_LEVEL_1_TRANSITION_ACK);
                    break;
                case 2:
                    c1Button.setIcon(ResourceUtil.getImageIcon("LE_13.bmp"));
                    c1Button.setActionCommand(DmiPanel.PROP_CHANGE_LEVEL_2_TRANSITION_ACK);
                    break;
                case 3:
                    c1Button.setIcon(ResourceUtil.getImageIcon("LE_15.bmp"));
                    c1Button.setActionCommand(DmiPanel.PROP_CHANGE_LEVEL_3_TRANSITION_ACK);
                    break;
                default:
                    c1Button.setIcon(null);
            }
        } else { // ack not required
            // icons are set for both states to ensure displayed.
            // some icons are not in ERTMS4 so disabled Icon used for both.
            switch (newLevel) {
                case -1: // ntc
                    c1Button.setIcon(ResourceUtil.getImageIcon("LE_08.bmp"));
                    c1Button.setDisabledIcon(ResourceUtil.getImageIcon("LE_08.bmp"));
                    break;
                case 0:
                    c1Button.setIcon(ResourceUtil.getImageIcon("LE_06.bmp"));
                    c1Button.setDisabledIcon(ResourceUtil.getImageIcon("LE_06.bmp"));
                    break;
                case 1:
                    c1Button.setIcon(ResourceUtil.getImageIcon("LE_10.bmp"));
                    c1Button.setDisabledIcon(ResourceUtil.getImageIcon("LE_10.bmp"));
                    break;
                case 2:
                    c1Button.setIcon(ResourceUtil.getImageIcon("LE_12.bmp"));
                    c1Button.setDisabledIcon(ResourceUtil.getImageIcon("LE_12.bmp"));
                    break;
                case 3:
                    c1Button.setIcon(ResourceUtil.getImageIcon("LE_14.bmp"));
                    c1Button.setDisabledIcon(ResourceUtil.getImageIcon("LE_14.bmp"));
                    break;
                default:
                    c1Button.setIcon(null);
                    c1Button.setDisabledIcon(null);
            }
        }
        c1Button.setEnabled(ackRequired);
        c1Button.setVisible(newLevel > -2);
    }

    protected void setTunnelStoppingIconVisible(boolean visible, boolean ackReqd){
        c2Button.setEnabled(ackReqd);
        c2Button.setBorder( ackReqd ? DmiPanel.BORDER_ACK : DmiPanel.BORDER_NORMAL);
        c2Button.setVisible(visible);
        if ( visible && ackReqd ){
            mainPanel.addFlashListener(pclC2, false);
        } else {
            mainPanel.removeFlashListener(pclC2, false);
        }
    }

    /**
     * No value displayed if distance &lt; 1
     * @param distance in m to stopping area.
     * 
     */
    protected void setTunnelStoppingDistance(int distance){
        c3Label.setVisible(distance>0);
        c3Label.setText(String.valueOf(distance));
    }

    private void setBg(JComponent p){
        p.setBackground(DmiPanel.BACKGROUND_COLOUR);
        p.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black, 1));
    }

    protected void setC8Label(Icon ico) {
        c8Label.setIcon(ico);
    }

    protected void setLevel(int level){
        switch (level) {
            case -1: // NTC
                setC8Label(ResourceUtil.getImageIcon("LE_02.bmp"));
                break;
            case 0:
                setC8Label(ResourceUtil.getImageIcon("LE_01.bmp"));
                break;
            case 1:
                setC8Label(ResourceUtil.getImageIcon("LE_03.bmp"));
                break;
            case 2:
                setC8Label(ResourceUtil.getImageIcon("LE_04.bmp"));
                break;
            case 3:
                setC8Label(ResourceUtil.getImageIcon("LE_05.bmp"));
                break;
            default:
                setC8Label(null);
                break;
        }
    }

    protected void setIntervetionSymbol( boolean newVal) {
        c9Label.setIcon(newVal ? ResourceUtil.getImageIcon("ST_01.bmp") : null);
    }

    protected void setReversingPermittedSymbol( boolean newVal) {
        c6Label.setIcon(newVal ? ResourceUtil.getImageIcon("ST_06.bmp") : null);
    }

    private void changeC1Border(){
        c1Button.setBorder( nextc1FlashState ? DmiPanel.BORDER_ACK : DmiPanel.BORDER_NORMAL);
        nextc1FlashState = !nextc1FlashState;
    }

    private void changeC2Border(){
        c2Button.setBorder( nextc2FlashState ? DmiPanel.BORDER_ACK : DmiPanel.BORDER_NORMAL);
        nextc2FlashState = !nextc2FlashState;
    }

    private void acknowledgeLevelPressed(ActionEvent e){
        log.debug("confirmation of level change {}", e);
        mainPanel.removeFlashListener(pclC1, false);
        nextc1FlashState = true;
        c1Button.setBorder(DmiPanel.BORDER_NORMAL);
        setModeAcknowledge(DmiPanel.MODE_NONE);
        if ( e.getActionCommand().startsWith("Level") ) {
            int newLevel = jmri.util.StringUtil.getFirstIntFromString(e.getActionCommand());
            setC1LevelTransitionIcon(newLevel, false);
        }
        mainPanel.firePropertyChange(e.getActionCommand(), false, true);
    }

    private void acknowledgeTunnelStopPressed(ActionEvent e){
        log.debug("confirmation of tunnel stopping area pressed {}", e);
        mainPanel.removeFlashListener(pclC2, false);
        c2Button.setBorder(DmiPanel.BORDER_NORMAL);
        nextc1FlashState = true;
        c2Button.setEnabled(false);
        mainPanel.firePropertyChange(DmiPanel.PROP_CHANGE_TUNNEL_STOP_AREA_ACK, false, true);
        
    }

    protected void dispose(){
        mainPanel.removeFlashListener(pclC1, false);
        mainPanel.removeFlashListener(pclC2, false);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DmiPanelC.class);

}
