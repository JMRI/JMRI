// TurnoutSignalMast.java
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
 * appearance. System name specifies the creation information:
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
 * @author	Bob Jacobsen Copyright (C) 2009, 2014, 2016
 */
public class MatrixSignalMast extends AbstractSignalMast {

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

    protected HashMap<String, String> appearanceToOutput = new HashMap<String, String>();

    public void setOutputForAppearance(String appearance, String bitnum) { // copied from dccmast, store keypair appearance-bitnum
        if (appearanceToOutput.containsKey(appearance)) {
            log.debug("Appearance " + appearance + " is already defined as " + appearanceToOutput.get(appearance));
            appearanceToOutput.remove(appearance);
        }
        appearanceToOutput.put(appearance, bitnum); // stores
    }

    public String getOutputForAppearance(String appearance) { // copied from dccmast, gives "00010"
        if (!appearanceToOutput.containsKey(appearance)) {
            log.error("Trying to get appearance " + appearance + " but it has not been configured");
            return "nnnnn";
        }
        return appearanceToOutput.get(appearance);
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
                // c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, appearanceToOutput.get(aspect)), packetRepeatCount);
                this.setOutputs("00000");
            }
            if (appearanceToOutput.containsKey(aspect) && appearanceToOutput.get(aspect) != "nnnnn") {
                // ToDo: pick up drop down choice for DCC packets or Turnouts
                // c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, appearanceToOutput.get(aspect)), packetRepeatCount);
                String bitstring = appearanceToOutput.get(aspect);
                // for  MatrixMast nest a loop, using appearanceToOutput() EBR
                this.setOutputs(bitstring);
                //Set the new Signal Mast state
            } else {
                log.error("Trying to set a aspect ( " + aspect + ") on signal mast " + getDisplayName() + " which has not been configured");
            }
        } else if (log.isDebugEnabled()) {
            log.debug("Mast set to unlit, will not send aspect change to hardware");
        }
        super.setAspect(aspect);
    }

    MatrixAspect unLit = null;

    public void setUnLitBitstring() {
        unLit = new MatrixAspect("Dark");
    }

    public String getUnLitBitstring() {
        if (unLit != null) {
            return unLit.getBitstring();
        }
        return null;
    }

//    public Turnout getUnLitTurnout() {
//        if (unLit != null) {
//            return unLit.getTurnout();
//        }
//        return null;
//    }

//    public int getUnLitTurnoutState() {
//        if (unLit != null) {
//            return unLit.getTurnoutState();
//        }
//        return -1;
//    }

    @Override
    public void setLit(boolean newLit) {
        if (!allowUnLit() || newLit == getLit()) {
            return;
        }
        if (newLit) {
            //This will force the signalmast to send out the commands to set the aspect again.
            setAspect(getAspect()); // method defined above
        } else {
            setOutputs("00000");
        }
        super.setLit(newLit);
    }

    public Turnout getOutput(int colnum) {
        if (unLit != null) {
            return ("output" + colnum).getTurnout();
        }
        return null;
    }

    public String getTurnoutName(int colnum) {
        if (colnum > 0 && colnum <= 5) {
            return ("output" + colnum).getTurnoutName();
        }
        return "";
    }

    public void setBitstring(String appearance, String bitString) {
        if (outputs.containsKey(appearance)) {
            log.debug("Appearance " + appearance + " is already defined so will override");
            bitstrings.remove(appearance);
        }
        bitstrings.put(appearance, new MatrixAspect(bitString));
    }

    public String getBitstring(String aspect) {
        return "00100";
        //ToDo do someting real URGENT
    }


    public String getOutputs() {
        return "";
    }
        //ToDo do someting real URGENT

    HashMap<String, MatrixAspect> outputs = new HashMap<String, MatrixAspect>(); // was turnouts, for unLit?

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

    static class MatrixAspect implements java.io.Serializable {

        //NamedBeanHandle<Turnout> namedTurnout;
        //int state;

        MatrixAspect(String bits) {
            if (turnoutName != null && !turnoutName.equals("")) {
                //Turnout turn = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
                //namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turn);
                bitString = bits;
            }
        }

//        Turnout getTurnout() {
//            if (namedTurnout == null) {
//                return null;
//            }
//            return namedTurnout.getBean();
//        }

//        String getTurnoutName() {
//            if (namedTurnout == null) {
//                return null;
//            }
//            return namedTurnout.getName();
//        }

//        int getTurnoutState() {
//            return state;
//        }

        String getMatrixBits(String aspect) {
            return aspect.getBitstring();
        }
    }

    boolean isTurnoutUsed(Turnout t) {
        for (int i = 0; i<5; i++) {
            if (t.equals(getOutput(i))) {
                return true;
            }
        }
        return false;
    }

    public List<NamedBeanHandle<Turnout>> getHeadsUsed() { //used?
        return new ArrayList<NamedBeanHandle<Turnout>>();
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

    int BitNum = -1;
    public void setBitNum(int number) {
            BitNum = number;
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
