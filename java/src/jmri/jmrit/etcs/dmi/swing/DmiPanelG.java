package jmri.jmrit.etcs.dmi.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Timebase;
import jmri.jmrit.etcs.ResourceUtil;

/**
 * Class to demonstrate features of ERTMS DMI Panel G,
 * Automatic Train Operation and clock.
 * @author Steve Young Copyright (C) 2024
 */
public class DmiPanelG extends JPanel {

    private final DmiPanel main;
    private final JLabel timeLabel;
    private final JLabel g2g3g4LabelTop;
    private final JLabel g2g3g4LabelBottom;

    private final Timebase clock;

    private final transient java.beans.PropertyChangeListener clockListener;

    private final JButton g1Button;
    private final JButton g2Button;
    private final JButton g3Button;
    private final JLabel g4Label;
    private final JLabel g3LabelMins;
    private final JLabel g3LabelSecs;
    private final JButton g5Button;

    public DmiPanelG(@Nonnull DmiPanel mainPanel){
        super();
        setLayout(null);

        setBackground(DmiPanel.BACKGROUND_COLOUR);
        setBounds(334, 315, 246, 150);

        main = mainPanel;
        JToggleButton g12PositionButton = new JToggleButton();

        g12PositionButton.setBounds(63,100,120,50);
        g12PositionButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black, 1));
        g12PositionButton.setBackground(DmiPanel.BACKGROUND_COLOUR);

        g12PositionButton.setIcon(ResourceUtil.getImageIcon("DR_03.bmp"));
        add(g12PositionButton);

        g12PositionButton.setFocusable(false);

        // position G13
        timeLabel = new JLabel();
        timeLabel.setBounds(183, 100, 63, 50);
        timeLabel.setForeground(DmiPanel.GREY);
        timeLabel.setBackground(DmiPanel.BACKGROUND_COLOUR);
        timeLabel.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black, 1));
        timeLabel.setFont(new Font(DmiPanel.FONT_NAME, Font.PLAIN, 13));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(timeLabel);

        clock = InstanceManager.getDefault(jmri.Timebase.class);
        clockListener = (java.beans.PropertyChangeEvent e) -> update();
        clock.addMinuteChangeListener(clockListener);
        DmiPanelG.this.update();

        g2g3g4LabelTop = new JLabel();
        g2g3g4LabelTop.setBounds(49,4,147,25);
        g2g3g4LabelTop.setForeground(DmiPanel.GREY);
        g2g3g4LabelTop.setBackground(DmiPanel.BACKGROUND_COLOUR);
        g2g3g4LabelTop.setFont(new Font(DmiPanel.FONT_NAME, Font.PLAIN, 13));
        g2g3g4LabelTop.setHorizontalAlignment(SwingConstants.CENTER);
        add(g2g3g4LabelTop);

        g2g3g4LabelBottom = new JLabel();
        g2g3g4LabelBottom.setBounds(49,21,147,24);
        g2g3g4LabelBottom.setForeground(DmiPanel.GREY);
        g2g3g4LabelBottom.setBackground(DmiPanel.BACKGROUND_COLOUR);
        g2g3g4LabelBottom.setFont(new Font(DmiPanel.FONT_NAME, Font.PLAIN, 13));
        g2g3g4LabelBottom.setHorizontalAlignment(SwingConstants.CENTER);
        add(g2g3g4LabelBottom);

        g1Button = new JButton();
        g1Button.setBorder(DmiPanel.BORDER_NORMAL);
        g1Button.setFocusable(false);
        g1Button.setVisible(false);
        g1Button.setBackground(DmiPanel.BACKGROUND_COLOUR);
        g1Button.setContentAreaFilled(false); // Make the button transparent
        g1Button.addActionListener(this::gButtonPressed);
        g1Button.setName("g1Button");

        g2Button = new JButton(); // stopping accuracy
        g2Button.setBorder(DmiPanel.BORDER_NORMAL);
        g2Button.setFocusable(false);
        g2Button.setVisible(false);
        g2Button.setBackground(DmiPanel.BACKGROUND_COLOUR);
        g2Button.setContentAreaFilled(false); // Make the button transparent
        g2Button.addActionListener(this::gButtonPressed);
        g2Button.setName("g2Button");

        g3Button = new JButton();
        g3Button.setBorder(DmiPanel.BORDER_NORMAL);
        g3Button.setFocusable(false);
        g3Button.setVisible(false);
        g3Button.setBackground(DmiPanel.BACKGROUND_COLOUR);
        g3Button.setContentAreaFilled(false); // Make the button transparent
        g3Button.addActionListener(this::gButtonPressed);
        g3Button.setName("g3Button");

        g3LabelMins = new JLabel();
        g3LabelMins.setBounds(49+49,12,24,24);
        g3LabelMins.setForeground(DmiPanel.GREY);
        g3LabelMins.setBackground(DmiPanel.BACKGROUND_COLOUR);
        g3LabelMins.setFont(new Font(DmiPanel.FONT_NAME, Font.PLAIN, 17));
        g3LabelMins.setHorizontalAlignment(SwingConstants.RIGHT);
        add(g3LabelMins);

        g3LabelSecs = new JLabel();
        g3LabelSecs.setBounds(49+49+24,14,24,24);
        g3LabelSecs.setForeground(DmiPanel.GREY);
        g3LabelSecs.setBackground(DmiPanel.BACKGROUND_COLOUR);
        g3LabelSecs.setFont(new Font(DmiPanel.FONT_NAME, Font.PLAIN, 13));
        g3LabelSecs.setHorizontalAlignment(SwingConstants.LEFT);
        add(g3LabelSecs);

        g4Label = new JLabel();
        g4Label.setVisible(false);
        g4Label.setBackground(DmiPanel.BACKGROUND_COLOUR);

        g5Button = new JButton();
        g5Button.setBorder(DmiPanel.BORDER_NORMAL);
        g5Button.setFocusable(false);
        g5Button.setVisible(false);
        g5Button.setBackground(DmiPanel.BACKGROUND_COLOUR);
        g5Button.setContentAreaFilled(false); // Make the button transparent
        g5Button.addActionListener(this::gButtonPressed);
        g5Button.setName("g5Button");

        g1Button.setBounds(0,0,49,50);
        g2Button.setBounds(49,0,49,50);
        g3Button.setBounds(49+49,0,49,50);
        g4Label.setBounds(49+49+49,0,49,50);
        g5Button.setBounds(49+49+49+49,0,49,50);

        add(g1Button);
        add(g2Button);
        add(g3Button);
        add(g4Label);
        add(g5Button);
    }

    /**
     * Set Automatic Train Operation Mode.
     * @param mode the new ATO Mode.
     * 0: No ATO 
     * 1: ATO selected
     * 2: ATO Ready for Engagement
     * 3: ATO Engaged
     * 4: ATO Disengaging
     * 5: ATO failure
     */
    protected void setAtoMode(int mode){
        switch(mode){
            case 1:
                g1Button.setIcon(ResourceUtil.getImageIcon("ATO_01.bmp"));
                g1Button.setDisabledIcon(ResourceUtil.getImageIcon("ATO_01.bmp"));
                break;
            case 2:
                g1Button.setIcon(ResourceUtil.getImageIcon("ATO_02.bmp"));
                g1Button.setDisabledIcon(ResourceUtil.getImageIcon("ATO_02.bmp"));
                g1Button.setActionCommand(DmiPanel.PROP_CHANGE_ATO_DRIVER_REQUEST_START);
                break;
            case 3:
                g1Button.setIcon(ResourceUtil.getImageIcon("ATO_03.bmp"));
                g1Button.setDisabledIcon(ResourceUtil.getImageIcon("ATO_03.bmp"));
                g1Button.setActionCommand(DmiPanel.PROP_CHANGE_ATO_DRIVER_REQUEST_STOP);
                break;
            case 4:
                g1Button.setIcon(ResourceUtil.getImageIcon("ATO_04.bmp"));
                g1Button.setDisabledIcon(ResourceUtil.getImageIcon("ATO_04.bmp"));
                g1Button.setActionCommand(DmiPanel.PROP_CHANGE_ATO_DRIVER_REQUEST_STOP);
                break;
            case 5:
                g1Button.setIcon(ResourceUtil.getImageIcon("ATO_05.bmp"));
                g1Button.setDisabledIcon(ResourceUtil.getImageIcon("ATO_05.bmp"));
                break;
            case 0:
            default:
                g1Button.setIcon(null);
                g1Button.setDisabledIcon(null);
        }
        g1Button.setEnabled( mode > 1 && mode < 5);
        g1Button.setVisible(mode != 0);
    }

    /**
     * Set Stopping accuracy symbol visible.
     * Only valid in ATO Mode.
     * @param acc -2: Hidden, -1: Undershot 0: Accurate 1: Overshot
     */
    protected void setStoppingAccuracy(int acc){
        switch (acc){
            case -1:
                g2Button.setIcon(ResourceUtil.getImageIcon("ATO_07.bmp"));
                break;
            case  0:
                g2Button.setIcon(ResourceUtil.getImageIcon("ATO_08.bmp"));
                break;
            case  1:
                g2Button.setIcon(ResourceUtil.getImageIcon("ATO_06.bmp"));
                 break;
            case -2:
            default:
                
        }
        g2Button.setVisible(acc != -2);
    }

    protected void setStoppingPointLabel(String station, String eta){
        g2g3g4LabelTop.setText(station);
        g2g3g4LabelBottom.setText(eta);
        g2g3g4LabelTop.setVisible(!station.isBlank());
        g2g3g4LabelBottom.setVisible(!eta.isBlank());
    }

    protected void setDwellTime(int mins, int secs){
        g3LabelMins.setVisible(mins > 0);
        g3LabelSecs.setVisible(secs > -1);
        g3LabelMins.setText(String.valueOf(mins));
        g3LabelSecs.setText(( mins > 0 ? ":" : "")  + ( secs < 10 ? "0": "")+ secs);
    }

    protected void setDoorIcon(int mode){
        switch (mode){
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                g4Label.setIcon(ResourceUtil.getImageIcon("ATO_"+mode+".bmp"));
                break;
            case 0:
            default:
                g4Label.setIcon(null);
        }
        g4Label.setVisible(mode != 0);
    }

    protected void setSkipStoppingPoint(int mode){
        g5Button.setEnabled(false);
        switch (mode){
            case 17:
                g5Button.setActionCommand(DmiPanel.PROP_CHANGE_SKIP_STOPPING_POINT_INACTIVE_DRIVER);
                g5Button.setIcon(ResourceUtil.getImageIcon("ATO_17.bmp"));
                g5Button.setDisabledIcon(ResourceUtil.getImageIcon("ATO_"+mode+".bmp"));
                g5Button.setEnabled(true);
                break;
            case 18:
                g5Button.setIcon(ResourceUtil.getImageIcon("ATO_18.bmp"));
                g5Button.setDisabledIcon(ResourceUtil.getImageIcon("ATO_"+mode+".bmp"));
                break;
            case 19:
                g5Button.setActionCommand(DmiPanel.PROP_CHANGE_SKIP_STOPPING_POINT_REQUEST_DRIVER);
                g5Button.setIcon(ResourceUtil.getImageIcon("ATO_19.bmp"));
                g5Button.setDisabledIcon(ResourceUtil.getImageIcon("ATO_"+mode+".bmp"));
                g5Button.setEnabled(true);
                break;
            case 0:
            default:
                g5Button.setIcon(null);
                g5Button.setDisabledIcon(null);
        }
        g5Button.setVisible(mode != 0);
    }

    private void gButtonPressed(ActionEvent e){
        main.firePropertyChange(e.getActionCommand(), false, true);
    }

    // todo - display seconds, add routine to TimeBase ??
    @SuppressWarnings("deprecation") // Date.getHours, getMinutes, getSeconds
    public void update() {
        Date now = clock.getTime();
        int hours = now.getHours();
        int minutes = now.getMinutes();

        String time = ( hours > 9 ? "" : "0" ) + hours + ":" + ( minutes > 9 ? "" : "0" ) + minutes;
        timeLabel.setText(time);
    }

    public void dispose(){
        clock.removeMinuteChangeListener(clockListener);
    }

}
