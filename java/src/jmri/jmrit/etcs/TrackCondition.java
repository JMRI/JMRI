package jmri.jmrit.etcs;

import java.awt.image.BufferedImage;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.swing.ImageIcon;

import jmri.jmrit.etcs.dmi.swing.DmiPanel;

import org.apiguardian.api.API;

/**
 * Class to represent DMI Track Points of Interest,
 * i.e. Announcements and Orders.
 * @author Steve Young Copyright (C) 2024
 */
@API(status=API.Status.EXPERIMENTAL)
public class TrackCondition {

    private final boolean isOrder;
    private int distanceFromStart;
    private final BufferedImage smallImage;
    private final String largeImageOrder;
    private final String largeImageNotOrder;
    protected final String descript;
    private int slotNum =0;
    private final String actionCommand;

    protected TrackCondition( int distance, boolean order,
            String ordSmlPath, String notOrdSmlPath,
            String ordLrgPath, String notOrdLrgPath,
            String description, String command ) {
        distanceFromStart = distance;
        isOrder = order;
        descript = description;
        largeImageOrder = ordLrgPath;
        largeImageNotOrder = notOrdLrgPath;
        String smlIconPath = ( order ? ordSmlPath : notOrdSmlPath );
        if ( ! smlIconPath.isBlank() ) {
            smlIconPath += ".bmp";
        }
        actionCommand = command;
        smallImage = ResourceUtil.getTransparentImage(smlIconPath);
    }

    /**
     * Get if the Track Condition is an order,
     * i.e. the Condition requires an acknowledgement from driver.
     * @return true if order, else false if informational.
     */
    public boolean getIsOrder(){
        return isOrder;
    }

    /**
     * Get the Distance to the start of the Track Condition.
     * @return the distance.
     */
    public int getDistanceFromStart() {
        return distanceFromStart;
    }

    protected void setDistanceFromStart(int distance) {
        distanceFromStart = distance;
    }

    /**
     * Get a small icon to display in the Planning area?
     * @return small icon.
     */
    public BufferedImage getSmlImage(){
        return smallImage;
    }

    /**
     * Get a larger image to display in a button.
     * @param isOrder true if requires acknowledgement, false if informational.
     * @return Large image.
     */
    @CheckForNull
    public ImageIcon getLargeIcon(boolean isOrder){
        var tst = getLargeImage(isOrder);
        if ( tst == null ) {
            return null;
        }
        return new ImageIcon(tst);
    }

    @CheckForNull
    private BufferedImage getLargeImage(boolean isOrder){
        String lrgIconPath = ( isOrder ? largeImageOrder : largeImageNotOrder );
        if (!lrgIconPath.isEmpty()) {
            lrgIconPath += ".bmp";
        }
        log.debug("getting image for {}", lrgIconPath);
        return ResourceUtil.getTransparentImage(lrgIconPath);
    }

    /**
     * Get Description of Track Condition.
     * @return if is driver action or informational, along with description.
     */
    public String getDescription(){
        return Bundle.getMessage(getIsOrder() ? "DriverAction" : "DriverInfo",descript);
    }

    /**
     * If this is an order, get the acknowledgement String for when
     * the driver clicks the button.
     * These can be listened for via adding a changeListener to DmiPanel.
     * @return the Acknowledgement String for the Condition.
     */
    public String getAckString(){
        return actionCommand;
    }

    /**
     * Get the Column Number for a Condition in the PASP Planning area.
     * @return column number, 0 if unset.
     */
    public int getColumnNum(){
        return slotNum;
    }

    /**
     * Set the Column Number for the PASP column.
     * @param newCol column number.
     */
    public void setColumnNum(int newCol) {
        slotNum = newCol;
    }

    @Override
    public boolean equals(Object o){
        return o instanceof TrackCondition &&
            ((TrackCondition)o).descript.equals(this.descript);
    }

    @Override
    public int hashCode() {
        return 29 + Objects.hashCode(descript);
    }

    @Override
    public String toString(){
        return this.getDescription();
    }

    /**
     * Get a new Level Crossing Track Condition.
     * No acknowledgement element.
     * @param distance distance until the Track Condition.
     * @return a Level Crossing Track Condition. 
     */
    public static TrackCondition levelCrossing(int distance ) {
        return new TrackCondition(distance, false,
            "", "","","LX_01",
            "Level Crossing, not protected", "");
    }

    /**
     * Get a new Radio Hole Track Condition.
     * No acknowledgement element.
     * @param distance distance until the Track Condition.
     * @return a Radio Hole Track Condition. 
     */
    public static TrackCondition radioHole(int distance ) {
        return new TrackCondition(distance, false,
            "", "PL_10","","TC_12",
            "Radio hole", "");
    }

    /**
     * Get a new Radio Hole Track Condition.
     * Always contains acknowledgement element.
     * @param distance distance until the Track Condition.
     * @return a Radio Hole Track Condition. 
     */
    public static TrackCondition soundHorn(int distance) {
        return new TrackCondition(distance, true,
            "PL_24", "","TC_35","",
            "Sound Horn", DmiPanel.PROP_CHANGE_SOUND_HORN_ACK);
    }

    /**
     * Get a new Radio Hole Track Condition.
     * No acknowledgement element.
     * No distance element as used for displaying symbol.
     * @return a Radio Hole Track Condition. 
     */
    public static TrackCondition pantographIsLowered() {
        return new TrackCondition(0, false,
            "", "","","TC_01",
            "Pantograph Lowered","");
    }

    /**
     * Get a new Lower Pantograph Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return a Lower Pantograph Track Condition. 
     */
    public static TrackCondition pantographLower(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_02", "PL_01","TC_03","TC_02",
            "Lower Pantograph", DmiPanel.PROP_CHANGE_LOWER_PANT_ACK);
    }

    /**
     * Get a new Raise Pantograph Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return a Raise Pantograph Track Condition. 
     */
    public static TrackCondition pantographRaise(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_04", "PL_03","TC_05","TC_04",
            "Raise Pantograph", DmiPanel.PROP_CHANGE_RAISE_PANT_ACK);
    }

    /**
     * Get a new Close Air Conditioning Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return a Close Air Conditioning Track Condition. 
     */
    public static TrackCondition airConClose(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_19", "PL_17","TC_21","TC_19",
            "Close air conditioning intake", DmiPanel.PROP_CHANGE_AIRCON_CLOSE_ACK);
    }

    /**
     * Get a new Open Air Conditioning Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return an Open Air Conditioning Track Condition. 
     */
    public static TrackCondition airConOpen(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_20", "PL_18","TC_22","TC_20",
            "Open air conditioning intake", DmiPanel.PROP_CHANGE_AIRCON_OPEN_ACK);
    }

    /**
     * Get a Start of Neutral Section Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return a start of Neutral Section Track Condition. 
     */
    public static TrackCondition neutralSection(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_06", "PL_05","TC_07","TC_06",
            "Neutral Section", DmiPanel.PROP_CHANGE_NEUTRAL_START_ACK);
    }

    /**
     * Get an End of Neutral Section Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return an end of Neutral Section Track Condition. 
     */
    public static TrackCondition neutralSectionEnd(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_08", "PL_07","TC_09","TC_08",
            "End of Neutral Section", DmiPanel.PROP_CHANGE_NEUTRAL_END_ACK);
    }

    /**
     * Get a Non Stopping Area Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     *              Always true when used in a TrackSection
     * @return a Non Stopping Area Track Condition. 
     */
    public static TrackCondition nonStoppingArea(int distance, boolean order ) {
        return new TrackCondition(distance, order,
            "PL_09", "PL_09","TC_11","TC_10",
            "Non stopping area", DmiPanel.PROP_CHANGE_NONSTOP_ACK);
    }

    /**
     * Get an Inhibit Magnetic Shoe Brake Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return an Inhibit Magnetic Shoe Brake Track Condition. 
     */
    public static TrackCondition inhibitMagShoeBrake(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_12", "PL_11","TC_14","TC_13",
            "Inhibition of Magnetic Shoe Brake", DmiPanel.PROP_CHANGE_INHIBIT_MAG_BRAKE_ACK);
    }

    /**
     * Get an Inhibit Eddy Current Brake Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return an Inhibit Eddy Current Brake Track Condition. 
     */
    public static TrackCondition inhibitEddyCurrentBrake(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_14", "PL_13","TC_16","TC_15",
            "Inhibition of eddy current Brake", DmiPanel.PROP_CHANGE_INHIBIT_EDDY_BRAKE_ACK);
    }

    /**
     * Get an Inhibit Regenerative Brake Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return an Inhibit Regenerative Brake Track Condition. 
     */
    public static TrackCondition inhibitRegenerativeBrake(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_16", "PL_15","TC_18", "TC_17",
            "Inhibition of regenerative Brake", DmiPanel.PROP_CHANGE_INHIBIT_REGEN_BRAKE_ACK);
    }

    /**
     * Get a No Traction Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return a No Traction Track Condition. 
     */
    public static TrackCondition tractionChange0(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_26", "PL_25","TC_24","TC_23",
            "No traction system", DmiPanel.PROP_CHANGE_TRACTION_0_ACK);
    }

    /**
     * Get a Traction Change to 25kV Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return a Traction Change to 25kV Track Condition. 
     */
    public static TrackCondition tractionChange25000(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_28", "PL_27","TC_26","TC_25",
            "Traction System : AC 25 kV 50 Hz", DmiPanel.PROP_CHANGE_TRACTION_25KV_ACK);
    }

    /**
     * Get a Traction Change to 25kV Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return a Traction Change to 25kV Track Condition. 
     */
    public static TrackCondition tractionChange15000(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_30", "PL_29","TC_28","TC_27",
            "Traction System : AC 15 kV 16.7 Hz", DmiPanel.PROP_CHANGE_TRACTION_15KV_ACK);
    }

    /**
     * Get a Traction Change to 3kV DC Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return a Traction Change to 3kV DC Track Condition. 
     */
    public static TrackCondition tractionChange3000(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_32", "PL_31","TC_30","TC_29",
            "Traction System : DC 3 kV", DmiPanel.PROP_CHANGE_TRACTION_3KV_ACK);
    }

    /**
     * Get a Traction Change to 1.5kV Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return a Traction Change to 1.5kV Track Condition. 
     */
    public static TrackCondition tractionChange1500(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_34", "PL_33","TC_32","TC_31",
            "Traction System : DC 1.5 kV", DmiPanel.PROP_CHANGE_TRACTION_1_5KV_ACK);
    }

    /**
     * Get a Traction Change to 600V or 750V Track Condition.
     * @param distance distance until the Track Condition.
     * @param order true if acknowledgement required, else false.
     * @return a Traction Change to 750V Track Condition. 
     */
    public static TrackCondition tractionChange750(int distance, boolean order) {
        return new TrackCondition(distance, order,
            "PL_36", "PL_35","TC_34","TC_33",
            "Traction System : DC 600/750 V", DmiPanel.PROP_CHANGE_TRACTION_750V_ACK);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackCondition.class);

}
