// MatrixSignalMast.java
package jmri.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * <li>(IT1)(IT2)(ITn) - colon-separated list of names for Turnouts
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

    protected HashMap<String, char[]> aspectToOutput = new HashMap<String, char[]>(16); // Clear - 01001 pairs

    public void setBitsForAspect(String aspect, char[] bitstring) {
        if (aspectToOutput.containsKey(aspect)) {
            log.debug("Aspect " + aspect + " is already defined as " + aspectToOutput.get(aspect));
            aspectToOutput.remove(aspect);
        }
        aspectToOutput.put(aspect, bitstring); // store keypair aspectname-bitstring in hashmap
    }

    String errorChars = "n";
    char[] errorBits = errorChars.toCharArray();

    public char[] getBitsForAspect(String aspect) { // gives "00010"
        if (!aspectToOutput.containsKey(aspect)) {
            log.error("Trying to get aspect " + aspect + " but it has not been configured");
            // if (bitNum < 5) {
            // to do
            // }
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
        if (getLit()) { //If the signalmast is lit, then send the commands to change the aspect.
            if (resetPreviousStates) {
                // Clear all the current states, this will result in the signalmast going blank (RED) for a very short time.
                // ToDo: pick up drop down choice for DCCPackets or Turnouts outputs
                // c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, aspectToOutput.get(aspect)), packetRepeatCount);
                this.setBitsForAspect("Dark", unLitBits);
            }
            if (aspectToOutput.containsKey(aspect) && aspectToOutput.get(aspect) != errorBits) {
                // ToDo: pick up drop down choice for either DCC direct packets or Turnouts as outputs
                // c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, aspectToOutput.get(aspect)), packetRepeatCount);
                char[] bitArray = getBitsForAspect(aspect);
                // for  MatrixMast nest a loop, using setBitsForAspect(), provides extra check on value
                this.UpdateOutputs(bitArray);
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
            UpdateOutputs(unLitBits); // directly set outputs
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

    public Turnout getOutput(int colnum) { // bean
        if (colnum > 0 && colnum <= 5) {
            if (colnum == 1) {
                return output1.getBean();
            } else if (colnum == 2) {
                return output2.getBean();
            } else if (colnum == 3) {
                return output3.getBean();
            } else if (colnum == 4) {
                return output4.getBean();
            } else if (colnum == 5) {
                return output5.getBean();
            } else {
                log.error("Trying to read output " + colnum + " which has not been configured");
            }
        }
        return null;
    }

    public NamedBeanHandle<Turnout> getOutput1 () {
        return output1;
    }
    public NamedBeanHandle<Turnout> getOutput2 () {
        return output2;
    }
    public NamedBeanHandle<Turnout> getOutput3 () {
        return output3;
    }
    public NamedBeanHandle<Turnout> getOutput4 () {
        return output4;
    }
    public NamedBeanHandle<Turnout> getOutput5 () {
        return output5;
    }

    public void setOutput1 (NamedBeanHandle<Turnout> turn) {
        this.output1 = turn;
    }
    public void setOutput2 (NamedBeanHandle<Turnout> turn) {
        this.output2 = turn;
    }
    public void setOutput3 (NamedBeanHandle<Turnout> turn) {
        this.output3 = turn;
    }
    public void setOutput4 (NamedBeanHandle<Turnout> turn) {
        this.output4 = turn;
    }
    public void setOutput5 (NamedBeanHandle<Turnout> turn) {
        this.output5 = turn;
    }

    public String getOutputName(int colnum) {
        if (colnum > 0 && colnum <= 5) {
            if (colnum == 1) {
                return output1.getName(); // bean or object? getTurnout(output1).getBean()
            } else if (colnum == 2) {
                return output2.getName();
            } else if (colnum == 3) {
                return output3.getName();
            } else if (colnum == 4) {
                return output4.getName();
            } else if (colnum == 5) {
                return output5.getName();
            }
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
        // already supplied as char array
        aspectToOutput.put(aspect, bitArray);
    }

    public List<String> getBitstrings() {
        // provide to xml
        ArrayList<String> bitlist = new ArrayList<String>(16);
        for (int i = 1; i <= aspectToOutput.size(); i++) { // number of aspects in hashmap
            // aspect =
            String bits = new String(aspectToOutput.get(aspect)); // convert char[] to string
            bitlist.add(bits);
        }
        //use hashmap  aspectToOutput directly and provide to xml
        return bitlist;
        // to do: pick key + char[] array from aspectToOutput
    }

    public List<String> getOutputs() {
        // to do: use hashmap directly
        ArrayList<String> outputlist = new ArrayList<String>(5);
        //list = outputsToBeans.keySet();
        outputlist.add(output1.getName()); // convert bean to name
        if (outputsToBeans.containsKey("output2")) { // outputsToBeans hashmap
            outputlist.add(output2.getName());
        }
        if (outputsToBeans.containsKey("output3")) {
            outputlist.add(output3.getName());
        }
        if (outputsToBeans.containsKey("output4")) {
            outputlist.add(output4.getName());
        }
        if (outputsToBeans.containsKey("output5")) {
            outputlist.add(output5.getName());
        }
        //use hashmap  aspectToOutput directly and provide to xml
        return outputlist;
        //hashmap > xml
    }

    NamedBeanHandle<Turnout> output1;
    NamedBeanHandle<Turnout> output2;
    NamedBeanHandle<Turnout> output3;
    NamedBeanHandle<Turnout> output4;
    NamedBeanHandle<Turnout> output5;
    HashMap<String, NamedBeanHandle<Turnout>> outputsToBeans = new HashMap<String, NamedBeanHandle<Turnout>>(5); // output# - bean pairs

    public void setOutput(String colname, String turnoutname) { // receive properties from xml, convert name to bean!
        Turnout turn = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutname);
        NamedBeanHandle<Turnout> namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutname, turn);
        if (outputsToBeans.containsKey(colname)) {
            log.debug("Output " + colname + " is already defined so will override");
            outputsToBeans.remove(colname);
        }
        outputsToBeans.put(colname, namedTurnout);
    }

    public void UpdateOutputs (char[] bits) {
        // hardware instruction
        for (int i = 1; i <= bits.length; i++) {
            if (bits[i] == '1') {
                output1.getBean().setCommandedState(Turnout.CLOSED);
            } else if (bits[i] == '0') {
                output1.getBean().setCommandedState(Turnout.THROWN);
                // to do: repeat for remaining < bitNum outputs
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

    Turnout getTurnout(int i) {
        if (i < 0 || i >= 5 ) {
            return null;
        }
        return output1.getBean();
        // to do: loop with i in var name
    }

    String getTurnoutName( int i) {
        if (i< 0 || i >= 5 ) {
            return null;
        }
        return output1.getName();         // to do: loop with i in var name
    }

/*    char[] getMatrixBits(String aspect) { // search/replace in AddMast and replace with getBitsForAspect()
            return aspect.getBitstring();
        }*/

    boolean isTurnoutUsed(Turnout t) {
        for (int i = 1; i<=5; i++) {
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
