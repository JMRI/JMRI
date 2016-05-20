// MatrixSignalMast.java
package jmri.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SignalMast implemented via a Binary Matrix of Apects x Turnout objects.
 * <p>
 * A Signalmast that is built up from an array of 1 - 5 turnouts to control a specific
 * aspect. System name specifies the creation information:
 * <pre>
 * IF$xsm:basic:one-searchlight:(IT1)(IT2) // update when released for MatrixSignalMast object
 * </pre> The name is a colon-separated series of terms:
 * <ul>
 * <li>IF$xsm - defines signal masts of this type (x for logic matriX)
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>(IT1)(IT2)(ITn) - colon-separated list of names for Turnouts as (binary) outputs
 * <li>name ending in _t (for Turnout outputs) or [option/to do] _d for direct DCC packets</li>
 * </ul>
 *
 * @author	Bob Jacobsen Copyright (C) 2009, 2014
 * @author	Egbert Broerse Copyright (C) 2016
 */
public class MatrixSignalMast extends AbstractSignalMast {
    int BitNum = 5; // number of columns in logix matrix, default to 5, set in Matrix Mast panel

    public MatrixSignalMast(String systemName, String userName) {
        super(systemName, userName);
        configureFromName(systemName);
    }

    public MatrixSignalMast(String systemName) {
        super(systemName);
        configureFromName(systemName);
    }

    void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) {
            log.error("SignalMast system name needs at least three parts: " + systemName);
            throw new IllegalArgumentException("System name needs at least three parts: " + systemName);
        }
        if (!parts[0].equals("IF$xsm")) {
            log.warn("SignalMast system name should start with IF$xsm but is " + systemName);
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

        configureSignalSystemDefinition(system); //  (checks for system) AbstractSignalMast
        configureAspectTable(system, mast); //  (create -default- appmapping in var "map") AbstractSignalMast
    }

    protected HashMap<String, char[]> aspectToOutput = new HashMap<String, char[]>(16); // "Clear" - 01001 char[] pairs

    public void setBitsForAspect(String aspect, char[] bitstring) {
        if (aspectToOutput.containsKey(aspect)) {
            log.debug("Aspect " + aspect + " is already defined as " + aspectToOutput.get(aspect));
            aspectToOutput.remove(aspect);
        }
        aspectToOutput.put(aspect, bitstring); // store keypair aspectname-bitstring in hashmap
    }

    String errorChars = "nnnnn";
    char[] errorBits = errorChars.toCharArray();

    /**    only called for an already existing mast
     * returns "00010" as char[]
     * length of aray should initially match the number of outputs defined, when a mast is changed in the interface,
     * extra 0's are added or last elements deleted in the Add Mast panel
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
            //If the signalmast is lit, then send the commands to change the aspect.
            if (resetPreviousStates) {
                // Clear all the current states, this will result in the signalmast going blank (RED) for a very short time.
                // ToDo: pick up drop down choice for DCCPackets or Turnouts outputs
                // c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, aspectToOutput.get(aspect)), packetRepeatCount);
                this.setBitsForAspect("Stop", unLitBits); // or Dark?
            }
            if (aspectToOutput.containsKey(aspect) && aspectToOutput.get(aspect) != errorBits) {
                // ToDo: pick up drop down choice for either DCC direct packets or Turnouts as outputs
                // c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, aspectToOutput.get(aspect)), packetRepeatCount);
                char[] bitArray = getBitsForAspect(aspect);
                // for  MatrixMast nest a loop, using setBitsForAspect(), provides extra check on value
                updateOutputs(bitArray); // NPE ERROR HERE??? try without this.
                //Set the new Signal Mast state
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
            // activate prior aspect
        } else {
            updateOutputs(unLitBits); // directly set outputs
            //c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, unLitId), packetRepeatCount);
        }
        super.setLit(newLit);
    }

    String UnLitChars = "00000"; // default starting value
    char[] unLitBits = UnLitChars.toCharArray();

    public void setUnlitBits(char[] bits) {
        unLitBits = bits;
    }

    public char[] getUnlitBits() {
        return unLitBits;
    }

    public String getUnlitChars() {
        return String.valueOf(unLitBits);
    }

    public Turnout getOutput(int colnum) { // as bean
        if (colnum > 0 && colnum <= outputsToBeans.size()) {
            return outputsToBeans.get("output" + colnum).getBean();
        } else {
            log.error("Trying to read output " + colnum + " which has not been configured");
        }
        return null;
    }

    // used in add mast panel #400
    public NamedBeanHandle<Turnout> getOutputHandle (int i) {
        return outputsToBeans.get("output" + i);
    }

    public String getOutputName(int colnum) { // provide to xml
        if (colnum > 0 && colnum <= outputsToBeans.size()) {
                return outputsToBeans.get("output" + colnum).getName();
        }
        log.error("Trying to read output " + colnum + " which has not been configured");
        return "";
    }

    public void setBitstring(String aspect, String bitString) {
        if (aspectToOutput.containsKey(aspect)) {
            log.debug("Aspect " + aspect + " is already defined so will override");
            aspectToOutput.remove(aspect);
        }
        char[] bitArray = bitString.toCharArray(); // for faster lookup, stored as char[] array
        aspectToOutput.put(aspect, bitArray);
    }

    public void setBitstring(String aspect, char[] bitArray) {
        if (aspectToOutput.containsKey(aspect)) {
            log.debug("Aspect " + aspect + " is already defined so will override");
            aspectToOutput.remove(aspect);
        }
        // is supplied as char array, no conversion needed
        aspectToOutput.put(aspect, bitArray);
    }

    // used?
    public List<String> getBitstrings() {
        // provide to xml as normal Strings
        ArrayList<String> bitlist = new ArrayList<String>(16);
        if (aspectToOutput != null) { // hashtable
            Set<String> keys = aspectToOutput.keySet(); // not a list
            for(String key: keys){
                String bits = new String(aspectToOutput.get(key)); // convert char[] to string
                bitlist.add(bits);
            }
        }
        // use hashmap  aspectToOutput directly and provide to xml
        return bitlist;
        // to do: pick key + char[] array from aspectToOutput
    }

    public String getBitstring(String aspect) {
        // provide to xml as normal String
        if (aspectToOutput != null) { // hashtable
            String bitString = new String(aspectToOutput.get(aspect)); // convert char[] to string
            return bitString;
            }
        return "";
    }

    public List<String> getOutputs() { // provide to xml
        // to do: use hashmap directly & use for loop
        ArrayList<String> outputlist = new ArrayList<String>(); // (5) or (bitNum) ?
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
        //use hashmap  aspectToOutput directly and provide to xml
        return outputlist;
        //hashmap > xml
    }

    HashMap<String, NamedBeanHandle<Turnout>> outputsToBeans = new HashMap<String, NamedBeanHandle<Turnout>>(5); // output# - bean pairs

    //looks a lot like the next method, remove?
    NamedBeanHandle<Turnout> TurnoutNameToHandle (String turnoutName) {
        if (turnoutName != null && !turnoutName.equals("")) {
            Turnout turn = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
            NamedBeanHandle<Turnout> namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turn);
            return namedTurnout;
        }
        log.error("Trying to create an empty output");
        return null;
    }

    public void setOutput(String colname, String turnoutname) { // receive properties from xml, convert name to NamedBeanHandle
        Turnout turn = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutname);
        NamedBeanHandle<Turnout> namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutname, turn);
        if (outputsToBeans.containsKey(colname)) {
            log.debug("Output " + colname + " is already defined so will override");
            outputsToBeans.remove(colname);
        }
        outputsToBeans.put(colname, namedTurnout);
    }

    public void updateOutputs (char[] bits) {
        // hardware instruction
        if (bits == null){
            log.debug("Empty char[] received");
        }
        for (int i = 0; i < 2; i++) {
            if (bits[i] == '1') {
                //outputsToBeans.get("output" + i).getBean().setCommandedState(Turnout.CLOSED);
            } else if (bits[i] == '0') {
                //outputsToBeans.get("output" + i).getBean().setCommandedState(Turnout.THROWN);
            } else if (bits[i] == 'n') {
                // let pass, extra chars up to 5 are not defined
            } else {
                // invalid char
                log.debug("Invalid element " + bits[i] + " cannot be converted to state for output #" + i);
            }
        }
    }

    boolean resetPreviousStates = false; // to do, add to panel

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

    Turnout getTurnoutBean(int i) {
        if (i < 0 || i > outputsToBeans.size() ) {
            return null;
        }
        outputsToBeans.get("output" + i).getBean();
        return null;
    }

/*    public String getTurnoutName(int i) {
        if (i < 0 || i > outputsToBeans.size() ) {
            return null;
        }
        outputsToBeans.get("output" + i).getName();
        return null;
    }*/

    boolean isTurnoutUsed(Turnout t) {
        for (int i = 1; i <= outputsToBeans.size(); i++) {
            if (t.equals(getOutput(i))) {
                return true;
            }
        }
        return false;
    }

    public static int getLastRef() {
        return lastRef;
    }

    static int lastRef = 0; // used with this type?

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

    public void setBitNum(int number) {
            BitNum = number;
    }

    public void setBitNum(char[] bits) {
        BitNum = bits.length;
    }

    public int getBitNum() {
        return BitNum;
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
