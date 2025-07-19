package jmri.jmrit.etcs.dmi.swing;

import java.awt.*;
import java.beans.PropertyChangeListener;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.Border;

import jmri.jmrit.etcs.*;
import jmri.util.ThreadingUtil;

import org.apiguardian.api.API;

/**
 * JPanel containing the ERTMS ETCS DMI.
 * @author Steve Young Copyright (C) 2024
 * @since 5.7.4
 */
@API(status=API.Status.EXPERIMENTAL)
public class DmiPanel extends JPanel {

    public static final Color WHITE = Color.WHITE;
    public static final Color BLACK = Color.BLACK;
    public static final Color GREY = new Color(195,195,195);
    public static final Color DARK_GREY = new Color (85,85,85);
    public static final Color MEDIUM_GREY = new Color(150,150,150);
    public static final Color DARK_BLUE = new Color(3,17,34);
    public static final Color ORANGE = new Color(234,145,0);
    public static final Color RED = new Color(191,0,2);
    public static final Color YELLOW = new Color(223,223,0);
    protected static final Color BACKGROUND_COLOUR = DARK_BLUE;

    protected static final Border BORDER_ACK =  BorderFactory.createLineBorder(DmiPanel.YELLOW , 2);
    protected static final Border BORDER_NORMAL =  BorderFactory.createLineBorder(DmiPanel.BACKGROUND_COLOUR , 2);

    protected static final String FONT_NAME = "Helvetica";

    public static final String PROP_CHANGE_CABMESSAGE_ACK = "MessageAcknowledged";
    public static final String PROP_CHANGE_LEVEL_NTC_TRANSITION_ACK = "LevelNTCAcknowledged";
    public static final String PROP_CHANGE_LEVEL_0_TRANSITION_ACK = "Level0Acknowledged";
    public static final String PROP_CHANGE_LEVEL_1_TRANSITION_ACK = "Level1Acknowledged";
    public static final String PROP_CHANGE_LEVEL_2_TRANSITION_ACK = "Level2Acknowledged";
    public static final String PROP_CHANGE_LEVEL_3_TRANSITION_ACK = "Level3Acknowledged";

    public static final int MODE_NONE = 0;
    public static final int MODE_SHUNTING = 1;
    public static final int MODE_TRIP = 4;
    public static final int MODE_POST_TRIP = 6;
    public static final int MODE_ON_SIGHT = 7;
    public static final int MODE_STAFF_RESPONSIBLE = 9;
    public static final int MODE_FULL_SUPERVISION = 11;
    public static final int MODE_NON_LEADING = 12;
    public static final int MODE_STANDBY = 13;
    public static final int MODE_REVERSING = 14;
    public static final int MODE_UNFITTED = 16;
    public static final int MODE_SYSTEM_FAILURE = 18;
    public static final int MODE_NATIONAL_SYSTEM = 20;
    public static final int MODE_LIMITED_SUPERVISION = 21;
    public static final int MODE_AUTOMATIC_DRIVING = 23;
    public static final int MODE_SUPERVISED_MANOEUVRE = 24;

    public static final String PROP_CHANGE_MODE_SHUNTING_ACK = "ModeShuntingAcknowledged";
    public static final String PROP_CHANGE_MODE_TRIP_ACK = "ModeTripAcknowledged";
    public static final String PROP_CHANGE_MODE_ON_SIGHT_ACK = "ModeOnSightAcknowledged";
    public static final String PROP_CHANGE_MODE_STAFF_RESPONSIBLE_ACK = "ModeStaffResponsibleAcknowledged";
    public static final String PROP_CHANGE_MODE_REVERSING_ACK = "ModeReversingAcknowledged";
    public static final String PROP_CHANGE_MODE_UNFITTED_ACK = "ModeUnfittedAcknowledged";
    public static final String PROP_CHANGE_MODE_NATIONAL_SYSTEM_ACK = "ModeNationalSystemAcknowledged";
    public static final String PROP_CHANGE_MODE_LIMITED_SUPERVISION_ACK = "ModeLimitedSupervisionAcknowledged";
    public static final String PROP_CHANGE_TRACK_AHEAD_FREE_TRUE = "DriverAdvisesTrackAheadFree";

    public static final String PROP_CHANGE_ATO_DRIVER_REQUEST_START = "AtoDriverStart";
    public static final String PROP_CHANGE_ATO_DRIVER_REQUEST_STOP = "AtoDriverStop";
    public static final String PROP_CHANGE_SKIP_STOPPING_POINT_INACTIVE_DRIVER = "SkipStoppingPointInactive";
    public static final String PROP_CHANGE_SKIP_STOPPING_POINT_REQUEST_DRIVER = "SkipStoppingPointRequestByDriver";

    public static final String PROP_CHANGE_TUNNEL_STOP_AREA_ACK = "TunnelStopAreaAcknowledged";
    public static final String PROP_CHANGE_SOUND_HORN_ACK = "SoundHornAcknowledged";
    public static final String PROP_CHANGE_LOWER_PANT_ACK = "LowerPantographAcknowledged";
    public static final String PROP_CHANGE_RAISE_PANT_ACK = "RaisePantographAcknowledged";
    public static final String PROP_CHANGE_AIRCON_OPEN_ACK = "AirConOpenAcknowledged";
    public static final String PROP_CHANGE_AIRCON_CLOSE_ACK = "AirConCloseAcknowledged";
    public static final String PROP_CHANGE_NEUTRAL_START_ACK = "NeutralSectionStartAcknowledged";
    public static final String PROP_CHANGE_NEUTRAL_END_ACK = "NeutralSectionEndAcknowledged";
    public static final String PROP_CHANGE_NONSTOP_ACK = "NonStoppingAreaAcknowledged";
    public static final String PROP_CHANGE_INHIBIT_MAG_BRAKE_ACK = "InhibitMagShoeBrakeAcknowledged";
    public static final String PROP_CHANGE_INHIBIT_EDDY_BRAKE_ACK = "InhibitEddyCurrentBrakeAcknowledged";
    public static final String PROP_CHANGE_INHIBIT_REGEN_BRAKE_ACK = "InhibitRegenerativeBrakeAcknowledged";
    public static final String PROP_CHANGE_TRACTION_0_ACK = "NoTractionAcknowledge";
    public static final String PROP_CHANGE_TRACTION_25KV_ACK = "TractionSystemAC25kVAcknowledge";
    public static final String PROP_CHANGE_TRACTION_15KV_ACK = "TractionSystemAC15kVAcknowledge";
    public static final String PROP_CHANGE_TRACTION_3KV_ACK = "TractionSystemDC3kVAcknowledge";
    public static final String PROP_CHANGE_TRACTION_1_5KV_ACK = "TractionSystemDC1.5kVAcknowledge";
    public static final String PROP_CHANGE_TRACTION_750V_ACK = "TractionSystemDC600750VAcknowledge";

    protected static final String PROPERTY_CENTRE_TEXT = DmiPanel.class.getName()+"centreText";

    private final DmiPanelA panelA;
    private final DmiPanelB panelB;
    private final DmiPanelC panelC;
    private final DmiPanelD panelD;
    private final DmiPanelE panelE;
    private final DmiPanelG panelG;
    private final DmiFlashTimer flashTimer;
    private int mode = 0; // unset

    /**
     * Create a new DmiPanel.
     */
    public DmiPanel(){
        super();
        setPreferredSize(new Dimension(640,480));
        setLayout(null); // Set the layout manager to null

        flashTimer = new DmiFlashTimer(this);

        panelA = getPanelA();
        panelB = getPanelB();
        panelC = getPanelC();
        panelD = getPanelD();
        panelE = getPanelE();
        panelG = getPanelG();

        add(panelA);
        add(panelB);
        add(panelC);
        add(panelD);
        add(panelE);
        add(getPanelF());
        add(panelG);
        add(getPanelY());
        add(getPanelZ());

    }

    // distance countdown bar
    private DmiPanelA getPanelA() {
        return new DmiPanelA(this);
    }

    // speedometer
    private DmiPanelB getPanelB() {
        return new DmiPanelB(this);
    }

    // larger icons under speedometer
    private DmiPanelC getPanelC() {
        return new DmiPanelC(this);
    }

    // planning area
    private DmiPanelD getPanelD() {
        return new DmiPanelD(this);
    }

    // messages
    private DmiPanelE getPanelE() {
        return new DmiPanelE(this);
    }

    // right hand side buttons
    private DmiPanelF getPanelF() {
        return new DmiPanelF(this);
    }

    // ATO and clock
    private DmiPanelG getPanelG() {
        return new DmiPanelG(this);
    }

    // top panel bar spacer
    private JPanel getPanelY() {
        JPanel p = new JPanel();
        p.setBackground(BACKGROUND_COLOUR);
        p.setBounds(0, 0, 640, 15);
        return p;
    }

    // bottom panel bar spacer
    private JPanel getPanelZ() {
        JPanel p = new JPanel();
        p.setBackground(BACKGROUND_COLOUR);
        p.setLayout(null);
        p.setBounds(0, 465, 640, 15);
        JLabel jmriLabel = new JLabel("JMRI " + jmri.Version.getCanonicalVersion());
        jmriLabel.setFont(new Font(DmiPanel.FONT_NAME, Font.PLAIN, 9));
        jmriLabel.setForeground(BLACK);
        jmriLabel.setLayout(null);
        jmriLabel.setBounds(590, 2, 110 , 15);
        p.add(jmriLabel);
        return p;
    }

    /**
     * Set the Maximum Speed on the Speed Dial.
     * @param speed 140, 180, 250 or 400
     */
    public void setMaxDialSpeed( int speed ) {
        ThreadingUtil.runOnGUI( () -> panelB.setMaxDialSpeed(speed) );
    }

    /**
     * Set the speed value to be displayed by the dial and in centre of dial.
     * @param speed no unit specified.
     */
    public void setActualSpeed( float speed ) {
        ThreadingUtil.runOnGUI( () -> panelB.setActualSpeed(speed) );
    }

    /**
     * Set the Centre Speedometer Circle and Dial Colour.
     * Default is DmiPanel.GREY
     * @param colour the colour to use.
     */
    public void setCentreCircleAndDialColor ( Color colour ) {
        ThreadingUtil.runOnGUI( () -> panelB.setCentreCircleAndDialColor(colour) );
    }

    /**
     * Set a list of Circular Speed Guide sections to display.
     * @param list the list to display.
     */
    public void setCsgSections(java.util.List<DmiCircularSpeedGuideSection> list){
        ThreadingUtil.runOnGUI( () -> panelB.setCsgSections(list) );
    }

    /**
     * Set a speed unit to be displayed in the dial.
     * @param newVal the speed unit, for display purpose only.
     */
    public void setDisplaySpeedUnit( String newVal ) {
        ThreadingUtil.runOnGUI( () -> panelB.setDisplaySpeedUnit(newVal) );
    }

    /**
     * Set the ATO Target Advice Speed.
     * @param newVal Target speed.
     * Negative values hide the advice.
     */
    public void setTargetAdviceSpeed(int newVal) {
        ThreadingUtil.runOnGUI(() -> panelB.setTargetAdviceSpeed(newVal) );
    }

    /**
     * Set distance to the next Advice Change.
     * @param distance to next advice.
     * Negative values hide the advice.
     */
    public void setNextAdviceChange(int distance) {
        ThreadingUtil.runOnGUI( () -> panelD.setNextAdviceChange(distance) );
    }

    /**
     * Set the release speed.
     * A negative value hides the speed.
     * @param speed to display.
     */
    public void setReleaseSpeed(int speed) {
        ThreadingUtil.runOnGUI(() -> panelB.setReleaseSpeed(speed) );
    }

    /**
     * Set the text colour of the Release Speed.
     * @param newColour the colour to use.
     */
    public void setReleaseSpeedColour(Color newColour) {
        ThreadingUtil.runOnGUI(() -> panelB.setReleaseSpeedColour( newColour ));
    }

    /**
     * Set Level Transition Announcement Notification.
     * Note that some valid options for ERTMS3.6 are invalid for ERTMS4 ,
     * e.g. 2, false.
     * @param newLevel
     * -2 : No notification displayed.
     * -1 : NTC
     * 0 : Level 0
     * 1 : Level 1 Intermittent
     * 2 : Level 2
     * 3 : Level 3
     * @param ackRequired true if acknowledgement required by driver, else false.
     */
    public void setLevelTransition(int newLevel, boolean ackRequired) {
        ThreadingUtil.runOnGUI( () -> panelC.setLevelTransition(newLevel, ackRequired) );
    }

    /**
     * Display Level Symbol.
     * @param level
     * -2 : No notification displayed.
     * -1 : NTC
     * 0 : Level 0
     * 1 : Level 1 Intermittent
     * 2 : Level 2
     * 3 : Level 3 ( ERTMS &lt; 4 )
     */
    public void setLevel(int level){
        ThreadingUtil.runOnGUI( () -> panelC.setLevel(level) );
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
     * 21 - Limited Supervision
     * 23 - Automatic Driving ( From ERTMS4 )
     * 24 - Supervised Manoeuvre ( From ERTMS4 )
     * @param newMode the mode to display.
     */
    public void setMode(int newMode){
        mode = newMode;
        ThreadingUtil.runOnGUI(() -> {
            panelB.setMode(newMode);
            panelD.repaint(); // Panel D also has conditionals depending on mode.
        });
    }

    /**
     * Set the display to acknowledge the transition to a new Mode.
     * @param newMode the new Mode to request acknowledgement for.
     */
    public void setModeAcknowledge(int newMode){
        ThreadingUtil.runOnGUI(() -> panelC.setModeAcknowledge(newMode) );
    }

    /**
     * Get the displayed operating mode.
     * @return the current mode.
     */
    protected int getMode(){
        return mode;
    }

    /**
     * Add a TrackCondition Announcement to under the Dial.
     * @param tc the Announcement to add.
     */
    public void addAnnouncement( TrackCondition tc ) {
        ThreadingUtil.runOnGUI( () -> panelB.addAnnouncement(tc) );
    }

    /**
     * Remove an Announcement from under the Dial.
     * @param tc the Announcement to remove.
     */
    public void removeAnnouncement ( TrackCondition tc ) {
        ThreadingUtil.runOnGUI( () -> panelB.removeAnnouncement(tc) );
    }

    /**
     * Set a Limited Supervision Speed.
     * A negative value hides the icon.
     * @param spd the Limited Supervision Speed.
     */
    public void setLimitedSupervisionSpeed(float spd) {
        ThreadingUtil.runOnGUI( () -> panelA.setLimitedSupervisionSpeed(spd) );
    }

    /**
     * Set the distance to target bar.
     * A negative value hides the field.
     * Values displayed to nearest 10m.
     * @param distance the distance to set.
     */
    public void setDistanceToTarget(float distance){
        ThreadingUtil.runOnGUI( () -> panelA.setDistanceToTarget(distance) );
    }

    /**
     * Set the adhesion Factor symbol displayed.
     * @param newVal true to display, else false.
     */
    public void setAdhesionFactorOn(boolean newVal){
        ThreadingUtil.runOnGUI( () -> panelA.setAdhesionFactorOn(newVal) );
    }

    /**
     * Set if Intervention Symbol is displayed.
     * @param newVal true to display, false to hide.
     */
    public void setIntervetionSymbol(boolean newVal){
        ThreadingUtil.runOnGUI( () -> panelC.setIntervetionSymbol(newVal) );
    }

    /**
     * Set the Reversing Permitted symbol visible.
     * @param newVal true to display, false to hide.
     */
    public void setReversingPermittedSymbol(boolean newVal){
        ThreadingUtil.runOnGUI( () -> panelC.setReversingPermittedSymbol(newVal) );
    }

    /**
     * Set the Indication marker.
     * Negative values not displayed.
     * @param distance the distance at which to display the marker.
     * @param whichSpeedChange the order of the speed change in the Movement Authority.
     */
    public void setIndicationMarker(int distance, int whichSpeedChange ) {
        ThreadingUtil.runOnGUI( () -> panelD.setIndicationMarkerLine(distance, whichSpeedChange) );
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
        ThreadingUtil.runOnGUI( () -> panelG.setAtoMode(mode) );
    }

    /**
     * Set the Coasting Symbol visible.
     * Only valid to display if in ATO mode
     * @param visible true to display, else false.
     */
    public void setCoasting(boolean visible){
        ThreadingUtil.runOnGUI( () -> panelB.setCoasting(visible) );
    }

    /**
     * Set Stopping accuracy symbol visible.
     * Only valid in ATO Mode.
     * @param acc -2: Hidden, -1: Undershot 0: Accurate 1: Overshot
     */
    public void setStoppingAccuracy(int acc){
        ThreadingUtil.runOnGUI( () -> panelG.setStoppingAccuracy(acc) );
    }

    /**
     * Set stopping point text.
     * Only valid in ATO mode.
     * @param station the next station.
     * @param eta ETA of next station.
     */
    public void setStoppingPointLabel(String station, String eta){
        ThreadingUtil.runOnGUI( () -> panelG.setStoppingPointLabel(station, eta) );
    }

    /**
     * Set remaining Station Dwell time.
     * @param mins minutes remaining.
     * @param secs seconds remaining.
     */
    public void setDwellTime(int mins, int secs){
        ThreadingUtil.runOnGUI( () -> panelG.setDwellTime(mins, secs) );
    }

    /**
     * Set Door Icon.
     * @param mode the icon code to display.
     * 0: Unset
     * 10: Request driver to open both sides doors
     * 11: Request driver to open left doors
     * 12: Request driver to open right doors
     * 13: Doors are open
     * 14: Request driver to close doors
     * 15: Doors are being closed by ATO
     * 16: Doors are closed
     */
    public void setDoorIcon(int mode){
        ThreadingUtil.runOnGUI( () -> panelG.setDoorIcon(mode) );
    }

    /**
     * Set Skip Stopping Point Icon.
     * @param mode the icon code to display.
     * 0: Unset
     * 17: Skip Stopping Point Inactive
     * 18: Skip Stopping Point requested by ATO-TS
     * 19: Skip Stopping Point requested by driver
     */
    public void setSkipStoppingPoint(int mode){
        ThreadingUtil.runOnGUI( () -> panelG.setSkipStoppingPoint(mode) );
    }

    /**
     * Set the Direction Symbol and visibility.
     * @param newDirection -1: Reverse, 0 Hidden, 1 Forwards.
     */
    public void setSupervisedDirection(int newDirection) {
        ThreadingUtil.runOnGUI( () -> panelB.setSupervisedDirection(newDirection) );
    }

    /**
     * No value displayed if distance &lt; 1
     * @param distance in m to stopping area.
     * 
     */
    public void setTunnelStoppingDistance(int distance) {
        ThreadingUtil.runOnGUI( () -> panelC.setTunnelStoppingDistance(distance) );
    }

    /**
     * Set Tunnel Stopping Icon Visible.
     * @param visible true if visible, false hidden.
     * @param ack true if Acknowledgement Required.
     */
    public void setTunnelStoppingIconVisible(boolean visible, boolean ack){
        ThreadingUtil.runOnGUI( () -> panelC.setTunnelStoppingIconVisible(visible, ack) );
    }

    /**
     * Set the Safe Radio Connection Symbol.
     * @param newVal -1 default, not displayed.
     *                0 Connection Lost
     *                1 Connection OK
     */
    public void setSafeRadioConnection(int newVal) {
        ThreadingUtil.runOnGUI( () -> panelE.setSafeRadioConnection(newVal) );
    }

    /**
     * Set the Track Ahead Free? Question visible.
     * @param newVal true to display, false to hide.
     */
    public void setTrackAheadFreeQuestionVisible(boolean newVal) {
        ThreadingUtil.runOnGUI( () -> panelD.setTrackAheadFreeQuestionVisible(newVal) );
    }

    /**
     * Set the Scale on the Planning Area.
     * 0 : 0 - 1000
     * 1 : 0 - 2000
     * 2 : 0 - 4000
     * 3 : 0 - 8000
     * 4 : 0 - 16000
     * 5 : 0 - 32000
     * @param scale the scale to use.
     */
    public void setScale(int scale){
        ThreadingUtil.runOnGUI( () -> panelD.setScale(scale) );
    }

    /**
     * Reset the Movement Authorities to the supplied List.
     * Existing MAs will be discarded.
     * @param a List of MAs.
     */
    public void resetMovementAuthorities(@Nonnull java.util.List<MovementAuthority> a) {
        ThreadingUtil.runOnGUI( () -> panelD.resetMovementAuthorities(a) );
    }

    /**
     * Get a List of current Movement Authorities.
     * @return List of MAs.
     */
    public java.util.List<MovementAuthority> getMovementAuthorities() {
        return panelD.getMovementAuthorities();
    }

    /**
     * Extend the Movement Authority.
     * @param dma the Movement Authority to add to existing List.
     */
    public void extendMovementAuthorities(@Nonnull MovementAuthority dma){
        ThreadingUtil.runOnGUI( () -> panelD.extendMovementAuthorities(dma) );
    }

    /**
     * Get the next Planning Track Announcement with the Movement Authority.
     * @param mustBeStation true if only station data is required.
     * @return the next Announcement, may be null if none within the Movement Authority.
     */
    @CheckForNull
    public TrackCondition getNextAnnouncement(boolean mustBeStation){
        java.util.List<MovementAuthority> mas = panelD.getMovementAuthorities();
        for ( MovementAuthority ma : mas ) {
            java.util.List<TrackSection> tsList = ma.getTrackSections();
            for ( TrackSection ts : tsList ){
                java.util.List<TrackCondition> anList = ts.getAnnouncements();
                for ( TrackCondition tc : anList ){
                     if ( !mustBeStation || tc instanceof StationTrackCondition ) {
                        return tc;
                    }
                }
            }
        }
        return null;
    } 

    /**
     * Advance the train.
     * Updates planning panel.
     * Updates distance to target.
     * @param distance to advance.
     */
    public void advance(int distance){
        ThreadingUtil.runOnGUI( () -> {
            panelD.advance(distance);
            panelA.advance(distance);
        });
    }

    /**
     * Send a CabMessage to the Driver.
     * @param msg the CabMessage to send.
     */
    public void messageDriver(CabMessage msg) {
        ThreadingUtil.runOnGUI( () -> panelE.addMessage(msg) );
    }

    /**
     * Remove a previously sent CabMessage from the display.
     * @param messageId the messageId of the CabMessage to remove.
     */
    public void removeMessage(String messageId) {
        ThreadingUtil.runOnGUI( () -> panelE.removeMessage(messageId) );
    }

    /**
     * Play one of the DMI UI Sounds.
     * <p>
     * 1 - S1_toofast.wav - 2 secs, plays once.
     * <p>
     * 2 - S2_warning.wav - 3 secs, loops until stopped.
     * <p>
     * 3 - S_info.wav - 1 sec, plays once.
     * <p>
     * 4 - click.wav - 1 sec, plays once.
     * @param sound which Sound, 
     */
    public void playDmiSound(int sound) throws IllegalArgumentException {
        ResourceUtil.playDmiSound(sound);
    }

    /**
     * Stop playing a DMI Sound.
     * @param sound the sound to Stop, normally 2 which plays in a loop.
     */
    public void stopDmiSound(int sound) {
        ResourceUtil.stopDmiSound(sound);
    }

    /**
     * Add a listener to synchronise panel flashing.
     * @param pcl the listener to add.
     * @param fast true if fast flashing, false for slow.
     */
    protected void addFlashListener( PropertyChangeListener pcl, boolean fast ) {
        flashTimer.addFlashListener(pcl, fast);
    }

    /**
     * Remove a listener from panel Flash timer notifications.
     * @param pcl the listener to remove.
     * @param fast true if fast listener, false if slow.
     */
    protected void removeFlashListener ( PropertyChangeListener pcl, boolean fast ) {
        flashTimer.removeFlashListener(pcl, fast);
    }

    /**
     * Fire a Property Change from this panel.
     * Old value fired as empty String.
     * @param property the property name.
     * @param newVal the new value.
     */
    protected void firePropertyChange(String property, String newVal) {
        this.firePropertyChange(property, "", newVal);
    }

    @Override
    public void setVisible(boolean newVal){
        ThreadingUtil.runOnGUI( () -> super.setVisible(newVal) );
    }

    /**
     * Dispose of any Listeners, e.g. Fast Clock.
     */
    public void dispose(){
        panelG.dispose();
        panelC.dispose();
        flashTimer.dispose();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DmiPanel.class);

}
