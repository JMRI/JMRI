package jmri.jmrit.etcs.dmi.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.jmrit.etcs.TrackCondition;
import jmri.jmrit.etcs.ResourceUtil;

import org.apiguardian.api.API;

/**
 * Class to demonstrate features of ERTMS DMI Panel B,
 * Speedometer Dial and Buttons underneath.
 * @author Steve Young Copyright (C) 2024
 */
@API(status=API.Status.EXPERIMENTAL)
public class DmiPanelB extends JPanel {

    private final JLabel b6Label;
    private final JLabel b7Label;
    private final JLabel b8Label;
    private final DmiSpeedoDialPanel p;
    private final DmiPanel mainPanel;

    private final UnderDialButton b3;
    private final UnderDialButton b4;
    private final UnderDialButton b5;

    private final List<TrackCondition> requireAllocationOrderAccouncements;
    private final List<UnderDialButton> underDialButtonList;

    public DmiPanelB(@Nonnull DmiPanel main){
        super();
        setLayout(null);

        setOpaque(true);
        mainPanel = main;
        p = new DmiSpeedoDialPanel();

        requireAllocationOrderAccouncements = new java.util.ArrayList<>();
        underDialButtonList = new java.util.ArrayList<>();

        b3 = new UnderDialButton(main);
        b4 = new UnderDialButton(main);
        b5 = new UnderDialButton(main);

        underDialButtonList.add(b3);
        underDialButtonList.add(b4);
        underDialButtonList.add(b5);

        JPanel b6 = new JPanel();
        JPanel b7 = new JPanel();
        JPanel b8 = new JPanel();

        p.setBounds(0, 0, 280, 300);
        b3.setBounds(122-36, 256, 36, 36);
        b4.setBounds(122, 256, 36, 36);
        b5.setBounds(122+36, 256, 36, 36);
        b6.setBounds(10, 256, 36, 36);
        b7.setBounds(254-18, 256, 36, 36);
        b8.setBounds(140-18, 216-18, 36, 36);

        setBg(b6);
        setBg(b7);
        setBg(b8);

        // b3, b4 and b5 are shared in that when b3 is occupied, b4 is used, then b5.
        // if further info needs to be displayed, wait until free slot.

        b6.setToolTipText("Release Speed");

        b6Label = new JLabel();
        b6Label.setForeground(DmiPanel.MEDIUM_GREY);
        b6Label.setFont(new Font(DmiPanel.FONT_NAME, Font.BOLD, 22));
        b6Label.setBounds(0, 0, 36, 36);
        b6Label.setVerticalAlignment(SwingConstants.CENTER);
        b6.add(b6Label);

        b7Label = new JLabel();
        b7.add(b7Label);

        add(p);
        add(b3);
        add(b4);
        add(b5);
        add(b6);
        add(b7);

        b8Label = new JLabel();
        b8.add(b8Label);

        add(b8);

        DmiPanelB.this.setMode(13); // Standby Mode
    }

    protected void addAnnouncement( TrackCondition tc ){
        log.debug("adding announcement {}", tc);
        requireAllocationOrderAccouncements.add(tc);
        updateDisplayOrderAccouncements();
    }

    protected void removeAnnouncement( TrackCondition tc ){
        log.debug("b4 remove {}", requireAllocationOrderAccouncements.size());
        requireAllocationOrderAccouncements.remove(tc);
        removeFromButton(tc);
        updateDisplayOrderAccouncements();
        log.debug("after remove {}", requireAllocationOrderAccouncements.size());
    }

    private void removeFromButton(TrackCondition tc) {
        underDialButtonList.forEach( udb -> {
            if ( tc.equals(udb.getTrackCondition())) {
                log.debug("removing track condition {}", tc);
                udb.setTrackCondition(null);
            } });
    }

    private void updateDisplayOrderAccouncements(){
        if ( !requireAllocationOrderAccouncements.isEmpty() ) {
            for ( UnderDialButton udb : underDialButtonList){
                log.debug("updateDisplayOrderAccouncements for {}", udb.getTrackCondition() );
                if ( udb.getTrackCondition() == null ) {
                    log.debug("setting tc to {}", requireAllocationOrderAccouncements.get(0));
                    udb.setTrackCondition(requireAllocationOrderAccouncements.remove(0));
                    return;
                }
            }
        }
    }

    private void setBg(JPanel p){
        p.setBackground(DmiPanel.BACKGROUND_COLOUR);
        p.setBorder(BorderFactory.createLineBorder(Color.black, 1));
    
    }

    protected void setMaxDialSpeed( int speed ) {
        p.setMaxDialSpeed( speed);
    }

    protected void setCentreCircleAndDialColor ( Color color ) {
        p.setCentreCircleAndDialColor(color);
    }

    protected void setActualSpeed( int speed ) {
        p.update(speed);
    }

    protected void setTargetAdviceSpeed(int newVal){
        p.setTargetAdviceSpeed(newVal);
    }

    protected void setCsgSections(List<DmiCircularSpeedGuideSection> list){
        p.setCsgSections(list);
    }

    protected void setDisplaySpeedUnit( String newVal ) {
        p.setDisplaySpeedUnit(newVal);
    }

    protected void setReleaseSpeed(int spd){
        b6Label.setText(spd<0 ? "" : String.valueOf(spd));
    }

    protected void setReleaseSpeedColour(Color newColour){
        b6Label.setForeground(newColour);
    }

    /**
     * Set Mode.
     * 0 - No Mode Displayed
     * 1 - Shunting
     * 4 - Trip
     * 6 - Post Trip
     * 7 - On Sight
     * 9 - Staff Responsible
     * 11 - Full Supervision Mode
     * 12 - Non-leading
     * 13 - Standby
     * 14 - Reversing
     * 16 - Unfitted
     * 18 - System Failure
     * 21 - Limited Supervision - Not ERTMS4
     * 23 - Automatic Driving ( From ERTMS4 )
     * 24 - Supervised Manoeuvre ( From ERTMS4 )
     * @param newMode The Mode to display in B6.
     */
    protected void setMode(int newMode){
        b7Label.setVisible( newMode != 0 );
        setCoasting(false);
        setSupervisedDirection(0);
        switch (newMode) {
            case 0:
                break;
            case 1:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_01.bmp"));
                b7Label.setToolTipText("Shunting");
                break;
            case 4:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_04.bmp"));
                b7Label.setToolTipText("Trip");
                break;
            case 6:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_06.bmp"));
                b7Label.setToolTipText("Post trip");
                break;
            case 7:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_07.bmp"));
                b7Label.setToolTipText("On Sight");
                break;
            case 9:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_09.bmp"));
                b7Label.setToolTipText("Staff Responsible");
                break;
            case 11:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_11.bmp"));
                b7Label.setToolTipText("Full Supervision Mode");
                break;
            case 12:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_12.bmp"));
                b7Label.setToolTipText("Non-leading");
                break;
            case 13:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_13.bmp"));
                b7Label.setToolTipText("Stand By");
                break;
            case 14:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_14.bmp"));
                b7Label.setToolTipText("Reversing");
                break;
            case 16:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_16.bmp"));
                b7Label.setToolTipText("Unfitted");
                break;
            case 18:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_18.bmp"));
                b7Label.setToolTipText("System failure");
                break;
            case 21:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_21.bmp"));
                b7Label.setToolTipText("Limited Supervision");
                break;
            case 23:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_23.bmp"));
                b7Label.setToolTipText("Automatic Driving");
                break;
            case 24:
                b7Label.setIcon(ResourceUtil.getImageIcon("MO_24.bmp"));
                b7Label.setToolTipText("Supervised Manoeuvre");
                break;
            default:
                throw new IllegalArgumentException("Could not set Mode " + newMode);
        }
    }

    protected void setCoasting(boolean visible){
        b8Label.setVisible(visible);
        if ( visible ) {
            b8Label.setIcon(ResourceUtil.getImageIcon("ATO_20.bmp"));
            var icon = ResourceUtil.getImageIcon("ATO_20.bmp");
            log.debug("icon found {}", icon);
            b8Label.setToolTipText("Coasting");
        } else {
            b8Label.setIcon(null);
            b8Label.setToolTipText(null);
        }
        b8Label.repaint();
    }

    // -1 reverse, 0 hidden, 1 forwards
    // supervised manoeuvre mode required
    protected void setSupervisedDirection(int newDirection) {
        int direction = newDirection;
        if ( newDirection !=0 && mainPanel.getMode() != 24 ) {
            log.warn("Supervised Manoeuvre Mode is required for Direction symbol");
            direction = 0;
        }
        switch (direction) {
            case -1:
                b8Label.setIcon(ResourceUtil.getImageIcon("SM02.bmp"));
                break;
            case 1:
                b8Label.setIcon(ResourceUtil.getImageIcon("SM01.bmp"));
                break;
            default:
            case 0:
                b8Label.setIcon(null);
                break;
        }
        b8Label.setVisible(direction != 0);
    }

    private static class UnderDialButton extends JButton {

        private final transient PropertyChangeListener pcl = (PropertyChangeEvent evt) ->  changeBorder(); 
        private boolean nextFlashState = true;
        private final DmiPanel mainPanel;

        UnderDialButton(DmiPanel main){
            super();
            mainPanel = main;
            setBorder(DmiPanel.BORDER_NORMAL);
            setFocusable(false);
            setBackground(DmiPanel.BACKGROUND_COLOUR);
            addActionListener(this::buttonClicked);
        }

        void buttonClicked(ActionEvent e){
            setEnabled(false);
            mainPanel.removeFlashListener(pcl, false);
            log.debug("button clicked: {}", e.getActionCommand());
            mainPanel.firePropertyChange(e.getActionCommand(), false, true);
            setBorder(DmiPanel.BORDER_NORMAL);
        }

        private TrackCondition tc = null;

        void setTrackCondition(@CheckForNull TrackCondition newTc){
            log.debug("button set track condition to {}", tc);
            tc = newTc;
            setEnabled(tc != null && tc.getIsOrder());
            resetImage();
            setActionCommand(newTc == null ? "": newTc.getAckString());
            log.debug("set {} actionCommand to {}", tc,  getActionCommand());
        }

        void resetImage(){
            if ( tc == null ) {
                this.setIcon(null);
                setBorder( DmiPanel.BORDER_NORMAL);
                mainPanel.removeFlashListener(pcl, false);
            } else {
                setIcon((tc.getLargeIcon(isEnabled())));
                if (isEnabled()){
                    mainPanel.addFlashListener(pcl, false);
                    nextFlashState = true;
                    changeBorder();
                }
            }
            this.repaint();
        }

        TrackCondition getTrackCondition(){
            return tc;
        }

        private void changeBorder(){
            setBorder( nextFlashState ? DmiPanel.BORDER_ACK : DmiPanel.BORDER_NORMAL);
            nextFlashState = !nextFlashState;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DmiPanelB.class);

}
