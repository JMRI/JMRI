// MatrixSignalMast.java
package jmri.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SignalMast implemented via a Binary Matrix (Truth Table) of Apects x Turnout objects.
 * <p>
 * A Signalmast that is built up from an array of 1 - 5 turnouts to control each aspect.
 * System name specifies the creation information:
 * <pre>
 * IF$xsm:basic:one-searchlight:($0001)-3t
 * </pre> The name is a colon-separated series of terms:
 * <ul>
 * <li>IF$xsm - defines signal masts of this type (x for matri<b>X</b>)
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map/mast model
 * <li>($0001) - small ordinal number for telling various matrix signal masts apart
 * <li>name ending in -nt for (binary) Turnout outputs or [to do:] -nd for direct DCC packets,
 * where n = the number of binary outputs, between 1 and 5</li>
 * </ul>
 *
 * @author	Bob Jacobsen Copyright (C) 2009, 2014
 * @author	Egbert Broerse Copyright (C) 2016
 */
public class MatrixSignalMast extends AbstractSignalMast {
    /**
     *  Number of columns in logix matrix, default to 5, set in Matrix Mast panel & on loading xml
     *  Used to set size of char[] bitString
    */
    protected int mastBitNum = 5;

    static String errorChars = "nnnnn";
    char[] errorBits = errorChars.toCharArray();

    static String emptyChars = "00000"; // default starting value
    char[] emptyBits = emptyChars.toCharArray();

    public MatrixSignalMast(String systemName, String userName) {
        super(systemName, userName);
        configureFromName(systemName);
    }

    public MatrixSignalMast(String systemName) {
        super(systemName);
        configureFromName(systemName);
    }

    protected String mastType = "IF$xsm";

    protected void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) {
            log.error("SignalMast system name needs at least three parts: " + systemName);
            throw new IllegalArgumentException("System name needs at least three parts: " + systemName);
        }
        if (!parts[0].equals(mastType)) {
            log.warn("SignalMast system name should start with " + mastType + " but is " + systemName);
        }
        String system = parts[1];
        String mast = parts[2];

        mast = mast.substring(0, mast.indexOf("("));
        String tmp = parts[2].substring(parts[2].indexOf("($") + 2, parts[2].indexOf(")"));
        try {
            int autoNumber = Integer.parseInt(tmp);
            if (autoNumber > lastRef) {
                lastRef = autoNumber;
            }
        } catch (NumberFormatException e) {
            log.warn("Auto generated SystemName " + systemName + " is not in the correct format");
        }

        configureSignalSystemDefinition(system); // (checks for system) in AbstractSignalMast
        configureAspectTable(system, mast); // (create -default- appmapping in var "map") in AbstractSignalMast
    }

    private HashMap<String, char[]> aspectToOutput = new HashMap<String, char[]>(16); // "Clear" - 01001 char[] pairs
    private char[] unLitBits;

    /**
     * Store bits in aspectToOutput hashmap
     * @param aspect String valid aspect to define
     * @param bitArray char[] of on/off outputs for the aspect, like "00010"
     * length of bitArray should match the number of outputs defined, so one digit per output
    */
    public void setBitsForAspect(String aspect, char[] bitArray) {
        if (aspectToOutput.containsKey(aspect)) {
            log.debug("Aspect " + aspect + " is already defined as " + aspectToOutput.get(aspect));
            aspectToOutput.remove(aspect);
        }
        aspectToOutput.put(aspect, bitArray); // store keypair aspectname - bitArray in hashmap
    }

    /**
     * Look up the pattern for an aspect
     * @param aspect String describing a (valid) signal mast aspect, like "Clear"
     * only called for an already existing mast
     * @return char[] of on/off outputs per aspect, like "00010"
     * length of array should match the number of outputs defined
     * when a mast is changed in the interface, extra 0's are added or superfluous elements deleted by the Add Mast panel
    */
    public char[] getBitsForAspect(String aspect) {
        if (!aspectToOutput.containsKey(aspect) || aspectToOutput.get(aspect) == null) {
            log.error("Trying to get aspect " + aspect + " but it has not been configured");
            return errorBits; // error flag
        }
        return aspectToOutput.get(aspect);
    }

    @Override
    public void setAspect(String aspect) {
        // check it's a valid choice
        if (!map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid Aspect: " + aspect + " on mast: " + getDisplayName());
            throw new IllegalArgumentException("attempting to set invalid Aspect: " + aspect + " on mast: " + getDisplayName());
        } else if (disabledAspects.contains(aspect)) {
            log.warn("attempting to set an Aspect that has been Disabled: " + aspect + " on mast: " + getDisplayName());
            throw new IllegalArgumentException("attempting to set an Aspect that has been Disabled: " + aspect + " on mast: " + getDisplayName());
        }
        if (getLit()) {
            // If the signalmast is lit, then send the commands to change the aspect.
            if (resetPreviousStates) {
                // Clear all the current states, this will result in the signalmast going blank (RED) for a very short time.
                // ToDo: check if decoder will accept direct DCC packets
                // or pick up drop down choice to choose between DCCPackets or Turnouts outputs
                // c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, aspectToOutput.get(aspect)), packetRepeatCount);
                if (aspectToOutput.containsKey("Stop")) {
                    updateOutputs(getBitsForAspect("Stop")); // show Red
                } else {
                    if (unLitBits != null) {
                        updateOutputs(unLitBits); // Dark (instead of Red), always available
                    }
                }
            }
            if (aspectToOutput.containsKey(aspect) && aspectToOutput.get(aspect) != errorBits) {
                // ToDo: pick up drop down choice for either DCC direct packets or Turnouts as outputs
                // c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, aspectToOutput.get(aspect)), packetRepeatCount);
                char[] bitArray = getBitsForAspect(aspect);
                // for  MatrixMast nest a loop, using setBitsForAspect(), provides extra check on value
                updateOutputs(bitArray);
                // Set the new Signal Mast state
            } else {
                log.error("Trying to set an aspect (" + aspect + ") on signal mast " + getDisplayName() + " which has not been configured");
            }
        } else if (log.isDebugEnabled()) {
            log.debug("Mast set to unlit, will not send aspect change to hardware");
        }
        super.setAspect(aspect);
    }

    public void setLit(boolean newLit) {
        if (!allowUnLit() || newLit == getLit()) {
            return;
        }
        if (newLit) {
            setAspect(getAspect());
            // if true, activate prior aspect
        } else {
            if (unLitBits != null) {
                updateOutputs(unLitBits); // directly set outputs
                //c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, unLitId), packetRepeatCount);
            }
        }
        super.setLit(newLit);
    }

    public void setUnLitBits(@Nonnull char[] bits) {
        unLitBits = bits;
    }

    /**
     *  Receive unLitBits from xml and store
     *  @param bitString String for 1-n 1/0 chararacters setting an unlit aspect
     */
    public void setUnLitBits(String bitString) {
        char[] unLitBits = bitString.toCharArray();
    }

    /**
     *  provide to panel for edit
     */
    public char[] getUnLitBits() {
        if (unLitBits != null) {
            return unLitBits;
        } else {
            return emptyBits;
        }
    }

    /**
     *  Hand unLitBits to xml
     *  @return String for 1-n 1/0 chararacters setting an unlit aspect
     */
    public String getUnLitChars() {
        if (unLitBits != null) {
            return String.valueOf(unLitBits);
        } else {
            log.error("Returning 0 values because unLitBits is empty");
            return emptyChars.substring(0, (mastBitNum)); // should only be called when Unlit = true
        }
    }

    public Turnout getOutputBean(int colnum) { // as bean
        String key = "output" + Integer.toString(colnum);
        if (colnum > 0 && colnum <= outputsToBeans.size()) {
            return outputsToBeans.get(key).getBean();
        }
        log.error("Trying to read bean for output " + colnum + " which has not been configured");
        return null;
    }

    /**
     *  Fetch output from outputsToBeans hashmap
     *  used in Add Mast panel line 427
     *  @param colnum int index (1 up to 5) for the column of the desired output
     *  @return NamedBeanHandle to the configured turnout output
     */
    public NamedBeanHandle<Turnout> getOutputHandle (int colnum) {
        String key = "output" + Integer.toString(colnum);
        if (colnum > 0 && colnum <= outputsToBeans.size()) {
            return outputsToBeans.get(key);
        }
        log.error("Trying to read output NamedBeanHandle " + key + " which has not been configured");
        return null;
    }

    /**
     *  Fetch output from outputsToBeans hashmap and provide to xml
     *  @see configurexml.MatrixSignalMastXml#store()
     *  @param colnum int index (1 up to 5) for the column of the desired output
     *  @return String with the desplay name of the configured turnout output
     */
    public String getOutputName(int colnum) {
        String key = "output" + Integer.toString(colnum);
        if (colnum > 0 && colnum <= outputsToBeans.size()) {
                return outputsToBeans.get(key).getName();
        }
        log.error("Trying to read name of output " + colnum + " which has not been configured");
        return "";
    }

    /**
     *  Receive aspect name from xml and store matching setting in outputsToBeans hashmap
     *  @see configurexml.MatrixSignalMastXml#load()
     *  @param aspect String describing (valid) signal mast aspect, like "Clear"
     *  @param bitString String of 1/0 digits representing on/off outputs per aspect, like "00010"
     */
    public void setBitstring(String aspect, String bitString) {
        if (aspectToOutput.containsKey(aspect)) {
            log.debug("Aspect " + aspect + " is already defined so will override");
            aspectToOutput.remove(aspect);
        }
        char[] bitArray = bitString.toCharArray(); // for faster lookup, stored as char[] array
        aspectToOutput.put(aspect, bitArray);
    }

    /**
     *  Receive aspect name from xml and store matching setting in outputsToBeans hashmap
     *  @param aspect String describing (valid) signal mast aspect, like "Clear"
     *  @param bitArray char[] of 1/0 digits representing on/off outputs per aspect, like {0,0,0,1,0}
     */
    public void setBitstring(String aspect, char[] bitArray) {
        if (aspectToOutput.containsKey(aspect)) {
            log.debug("Aspect " + aspect + " is already defined so will override");
            aspectToOutput.remove(aspect);
        }
        // is supplied as char array, no conversion needed
        aspectToOutput.put(aspect, bitArray);
    }

    /**
     *  Provide one series of on/off digits from aspectToOutput hashmap to xml
     *  @return bitString String of 1 (= on) and 0 (= off) chars
     *  @param aspect String describing valid signal mast aspect, like "Clear"
     */
    public String getBitstring(String aspect) {
        if (aspectToOutput.containsKey(aspect)) { // hashtable
            String bitString = new String(aspectToOutput.get(aspect)); // convert char[] to string
            return bitString;
            }
        return "";
    }

    /**
     *  Provide the names of the on/off turnout outputs from outputsToBeans hashmap to xml
     *  @return outputlist List&lt;String&gt; of display names for the outputs in order 1 to (max) 5
     */
    public List<String> getOutputs() { // provide to xml
        // to do: use for loop
        ArrayList<String> outputlist = new ArrayList<String>(); // (5) or (mastBitNum) ?
        //list = outputsToBeans.keySet();
        outputlist.add(outputsToBeans.get("output1").getName()); // convert NBH to name (String)
        if (outputsToBeans.containsKey("output2")) {
            // outputsToBeans hashmap
            outputlist.add(outputsToBeans.get("output2").getName());
        }
        if (outputsToBeans.containsKey("output3")) {
            outputlist.add(outputsToBeans.get("output3").getName());
        }
        if (outputsToBeans.containsKey("output4")) {
            outputlist.add(outputsToBeans.get("output4").getName());
        }
        if (outputsToBeans.containsKey("output5")) {
            outputlist.add(outputsToBeans.get("output5").getName());
        }
        return outputlist;
    }

    protected HashMap<String, NamedBeanHandle<Turnout>> outputsToBeans = new HashMap<String, NamedBeanHandle<Turnout>>(5); // output# - bean pairs

/*    //looks a lot like the next method, removed
    NamedBeanHandle<Turnout> TurnoutNameToHandle (String turnoutName) {
        if (turnoutName != null && !turnoutName.equals("")) {
            Turnout turn = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
            NamedBeanHandle<Turnout> namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turn);
            return namedTurnout;
        }
        log.error("Trying to create an empty output");
        return null;
    }*/

    /**
     * Receive properties from xml, convert name to NamedBeanHandle, store in hashmap outputsToBeans
     * @param colname String describing the name of the corresponding output, like "output1"
     * @param turnoutname String for the display name of the output, like "LT1"
    */
    public void setOutput(@Nonnull String colname, @Nonnull String turnoutname) {
        if (colname == null || turnoutname == null){
            log.error("Trying to store a null output. Fix output configuration for mast");
        } else {
            Turnout turn = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutname);
            NamedBeanHandle<Turnout> namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutname, turn);
            if (outputsToBeans.containsKey(colname)) {
                log.debug("Output " + colname + " is already defined so will override");
                outputsToBeans.remove(colname);
            }
            outputsToBeans.put(colname, namedTurnout);
        }
    }

    /**
     *  Send hardware instruction
     *  @param bits char[] of on/off outputs per aspect, like "00010"
     *  length of array should match the number of outputs defined
     */
    public void updateOutputs (char[] bits) {
        if (bits == null){
            log.debug("Empty char[] received");
        } else {
            for (int i = 0; i < outputsToBeans.size(); i++) {
                //log.debug("Setting bits[1] = " + bits[i] + " for output #" + i);
                if (getOutputBean(i + 1) != null) {
                    getOutputBean(i + 1).setBinaryOutput(true); // prevent feedback etc.
                }
                if (bits[i] == '1' && getOutputBean(i + 1) != null && getOutputBean(i + 1).getCommandedState() == Turnout.THROWN) {
                    // no need to set a state already set
                    getOutputBean(i + 1).setCommandedState(Turnout.CLOSED);
                } else if (bits[i] == '0' && getOutputBean(i + 1) != null && getOutputBean(i + 1).getCommandedState() == Turnout.CLOSED) {
                    getOutputBean(i + 1).setCommandedState(Turnout.THROWN);
                } else if (bits[i] == 'n' || bits[i] == 'u') {
                    // let pass, extra chars up to 5 are not defined
                } else {
                    // invalid char
                    log.debug("Invalid element " + bits[i] + " cannot be converted to state for output #" + i);
                }
            }
        }
    }

    boolean resetPreviousStates = false;

    /**
     * If the signal mast driver requires the previous state to be cleared down
     * before the next state is set.
     */
    public void resetPreviousStates(boolean boo) {
        resetPreviousStates = boo;
    }

    public boolean resetPreviousStates() {
        return resetPreviousStates;
    }

/*    Turnout getTurnoutBean(int i) { // as bean
        String key = "output" + Integer.toString(i);
        if (i < 1 || i > outputsToBeans.size() ) {
            return null;
        }
        if (outputsToBeans.containsKey(key) && outputsToBeans.get(key) != null){
            return outputsToBeans.get(key).getBean();
        }
        return null;
    }*/

/*    public String getTurnoutName(int i) {
        String key = "output" + Integer.toString(i);
        if (i < 1 || i > outputsToBeans.size() ) {
            return null;
        }
        if (outputsToBeans.containsKey(key) && outputsToBeans.get(key) != null) {
            return outputsToBeans.get(key).getName();
        }
        return null;
    }*/

    boolean isTurnoutUsed(Turnout t) {
        for (int i = 1; i <= outputsToBeans.size(); i++) {
            if (t.equals(getOutputBean(i))) {
                return true;
            }
        }
        return false;
    }

    public static int getLastRef() {
        return lastRef;
    }

    static int lastRef = 0;

    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { //NOI18N
            if (evt.getOldValue() instanceof Turnout) {
                if (isTurnoutUsed((Turnout) evt.getOldValue())) {
                    java.beans.PropertyChangeEvent e = new java.beans.PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new java.beans.PropertyVetoException(Bundle.getMessage("InUseTurnoutSignalMastVeto", getDisplayName()), e);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { //NOI18N
            //Do nothing at this stage
        }
    }

    /** Store number of outputs from integer
    * @param number int for the number of outputs defined for this mast
    * @see #mastBitNum
    */
    public void setBitNum(int number) {
            mastBitNum = number;
    }

    /** Store number of outputs from integer
     * @param bits char[] for outputs defined for this mast
     * @see #mastBitNum
     */
    public void setBitNum(char[] bits) {
        mastBitNum = bits.length;
    }

    public int getBitNum() {
        return mastBitNum;
    }

    public void setAspectDisabled(String aspect) {
        if (aspect == null || aspect.equals("")) {
            return;
        }
        if (!map.checkAspect(aspect)) {
            log.warn("attempting to disable an aspect: " + aspect + " that is not on the mast " + getDisplayName());
            return;
        }
        if (!disabledAspects.contains(aspect)) {
            disabledAspects.add(aspect);
            firePropertyChange("aspectDisabled", null, aspect);
        }
    }

    public void dispose() {
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(MatrixSignalMast.class.getName());
}

/* @(#)MatrixSignalMast.java */
