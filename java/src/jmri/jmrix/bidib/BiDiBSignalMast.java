package jmri.jmrix.bidib;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.*;

import jmri.implementation.AbstractSignalMast;
import jmri.SystemConnectionMemo;
import jmri.InstanceManager;
import jmri.SignalMast;
import org.bidib.jbidibc.messages.enums.LcOutputType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a SignalMast that use <B>BiDiB Accessories</B>
 * to set aspects.
 * <p>
 * This implementation writes out to BiDiB when it's commanded to
 * change appearance, and updates its internal state when it receives a status feedback from BiDiB.
 * <p>
 * System name specifies the creation information:
 * <pre>
 * BF$bsm:basic:one-searchlight(123)
 * </pre> The name is a colon-separated series of terms:
 * <ul>
 * <li>B - system prefix
 * <li>F$bsm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>(node:123) - BiDiB Accessory address
 * </ul>
 * <p>
 * To keep the state consistent, {@link #setAspect} does not immediately
 * change the local aspect.  Instead, it produces the relevant BiDiB message on the
 * network, waiting for that to return and do the local state change, notification, etc.
 * <p>
 * Based upon {@link jmri.implementation.DccSignalMast} by Kevin Dickerson
 *
 * @author Bob Jacobsen    Copyright (c) 2017, 2018
 * @author Eckart Meyer    Copyright (c) 2020-2023
 */
public class BiDiBSignalMast extends AbstractSignalMast implements BiDiBNamedBeanInterface {
    
    BiDiBTrafficController tc;
    //MessageListener messageListener = null;
    BiDiBOutputMessageHandler messageHandler = null;
    BiDiBAddress addr;
    private char typeLetter;
    int commandedAspect = -1;

    public BiDiBSignalMast(String sys, String user) {
        super(sys, user);
        configureFromName(sys);
    }

    public BiDiBSignalMast(String sys) {
        super(sys);
        configureFromName(sys);
    }

    public BiDiBSignalMast(String sys, String user, String mastSubType) {
        super(sys, user);
        mastType = mastSubType;
        configureFromName(sys);
    }

    //protected String mastType = "F$bsm";
    protected String mastType = getNamePrefix();
    
    static public String getNamePrefix() {
        return "F$bsm";
    }


    private void configureFromName(String systemName) {
        // split out the basic information
        BiDiBSystemConnectionMemo memo = null;
        String[] parts = systemName.split(":", 3); //the last part contains a BiDiB address and therfor may contain a colon itself
        if (parts.length < 3) {
            log.error("SignalMast system name needs at least three parts: {}", systemName);
            throw new IllegalArgumentException("System name needs at least three parts: " + systemName);
        }
        if (!parts[0].endsWith(mastType)) {
            log.warn("First part of SignalMast system name is incorrect {} : {}", systemName, mastType);
        } else {
            String systemPrefix = parts[0].substring(0, parts[0].indexOf("$") - 1);
            java.util.List<SystemConnectionMemo> memoList = jmri.InstanceManager.getList(SystemConnectionMemo.class);

            for (SystemConnectionMemo m : memoList) {
                if (m.getSystemPrefix().equals(systemPrefix)) {
                    if (m instanceof jmri.jmrix.bidib.BiDiBSystemConnectionMemo) {
                        tc = ((BiDiBSystemConnectionMemo) m).getBiDiBTrafficController();
                        memo = (BiDiBSystemConnectionMemo)m;
                    } else {
                        log.error("Can't create mast \"{}\" because system \"{}}\" is not BiDiBSystemConnectionMemo but rather {}",
                                systemName, systemPrefix, m.getClass());
                    }
                    break;
                }
            }

            if (tc == null) {
                log.error("No BiDiB connection found for system prefix \"{}\", so mast \"{}\" will not function",
                            systemPrefix, systemName);
            }
        }
        String system = parts[1];
        String mast = parts[2];

        mast = mast.substring(0, mast.indexOf("("));
        log.trace("In configureFromName setMastType to {}", mast);
        setMastType(mast);
        
        if (memo != null) {
            char accessoryTypeLetter = 'T';
            String accessorySystemName = memo.getSystemPrefix() + accessoryTypeLetter + parts[2].substring(parts[2].indexOf("(") + 1, parts[2].indexOf(")"));
            addr = new BiDiBAddress(accessorySystemName, accessoryTypeLetter, memo);
            log.info("New SIGNALMAST created: {} {} -> {}", systemName, accessorySystemName, addr);
            typeLetter = accessoryTypeLetter;
//            if (!addr.isValid()) {
//                log.warn("BiDiB signal mast accessory address SystemName {} is not in the correct format", systemName);
//            }
        }
        configureSignalSystemDefinition(system);
        configureAspectTable(system, mast);
        
        createSignalMastListener();
        
        messageHandler.sendQueryConfig();
    }


    protected HashMap<String, Integer> appearanceToOutput = new HashMap<>();

    public void setOutputForAppearance(String appearance, int number) {
        log.debug("setOutputForAppearance: {} -> {}", appearance, number);
        if (appearanceToOutput.containsKey(appearance)) {
            log.debug("Appearance {} is already defined as {}", appearance, appearanceToOutput.get(appearance));
            appearanceToOutput.remove(appearance);
        }
        appearanceToOutput.put(appearance, number);
    }

    public int getOutputForAppearance(String appearance) {
        if (!appearanceToOutput.containsKey(appearance)) {
            log.error("Trying to get appearance {} but it has not been configured", appearance);
            return -1;
        }
        return appearanceToOutput.get(appearance);
    }

    /*
     0.  "Stop"
     1.  "Take Siding"
     2.  "Stop-Orders"
     3.  "Stop-Proceed"
     4.  "Restricting"
     5.  "Permissive"
     6.  "Slow-Approach"
     7.  "Slow"
     8.  "Slow-Medium"
     9.  "Slow-Limited"
     10. "Slow-Clear"
     11. "Medium-Approach"
     12. "Medium-Slow"
     13. "Medium"
     14. "Medium-Ltd"
     15. "Medium-Clr"
     16. "Limited-Approach"
     17. "Limited-Slow"
     18. "Limited-Med"
     19. "Limited"
     20. "Limited-Clear"
     21. "Approach"
     22. "Advance-Appr"
     23. "Appr-Slow"
     24. "Adv-Appr-Slow"
     25. "Appr-Medium"
     26. "Adv-Appr-Med"
     27. "Appr-Limited"
     28. "Adv-Appr-Ltd"
     29. "Clear"
     30. "Cab-Speed"
     31. "Dark" */

    @Override
    public void setAspect(@Nonnull String aspect) {
        if (appearanceToOutput.containsKey(aspect) && appearanceToOutput.get(aspect) != -1) {
            sendMessage(appearanceToOutput.get(aspect));
        } else {
            log.warn("Trying to set aspect ({}) that has not been configured on mast {}", aspect, getDisplayName());
        }
        //super.setAspect(aspect);
        String oldAspect = this.aspect;
        this.aspect = null; //means UNKNOWN - there is no INCONSISTENT
        //this.speed = (String) getSignalSystem().getProperty(aspect, "speed"); // NOI18N
        firePropertyChange("Aspect", oldAspect, aspect); // NOI18N
    }

    @Override
    public void setLit(boolean newLit) {
        if (!allowUnLit() || newLit == getLit()) {
            return;
        }
        if (newLit) {
            String a = getAspect();
            if (a != null) {
                setAspect(a);
            }
            super.setLit(newLit);
        } else {
            sendMessage(unLitId);
        }
        //super.setLit(newLit);
    }

    /**
     * Request the state of the accessory from the layout.
     * The listener gets the answer.
     */
    public void queryAccessory() {
        messageHandler.sendQuery();
    }

    /**
     * Send a accessory message to BiDiB
     * 
     * @param aspect to send
     */
    protected void sendMessage(int aspect) {
        // TODO: check FEATURE_GEN_SWITCH_ACK
        log.debug("Signal Mast set aspect: {}, addr: {}", aspect, addr);
        //newKnownAspect(INCONSISTENT);
        if (addr.isValid()) {
            int state;
            if (addr.isPortAddr()) {
                state = (aspect == 0) ? 0 : 1;
                switch (messageHandler.getLcType()) {
                    case LIGHTPORT:
                        state = (aspect == 0) ? 2 : 3; //use Dim function - we can't configure this so far...
                        break;
                    case SERVOPORT:
                    case ANALOGPORT:
                    case BACKLIGHTPORT:
                        state = (aspect == 0) ? 0 : 255;
                        break;
                    case MOTORPORT:
                        state = (aspect == 0) ? 0 : 126;
                        break;
                    case INPUTPORT:
                        log.warn("output to INPUT port is not possible, addr: {}", addr);
                        return;
                    default:
                        break;
                }
            }
            else {
                state = aspect;
            }
            messageHandler.sendOutput(state);
            commandedAspect = aspect;
        }
    }
        
    int unLitId = 31;

    public void setUnlitId(int i) {
        unLitId = i;
    }

    public int getUnlitId() {
        return unLitId;
    }

    public String getAccessoryAddress() {
        if (addr.isValid()) {
            return addr.getAddrString();
        }
        return "";
    }
    
    public BiDiBTrafficController getTrafficController() {
        return tc;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BiDiBAddress getAddr() {
        return addr;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeNew() {
        //create a new BiDiBAddress
        addr = new BiDiBAddress(getSystemName(), typeLetter, tc.getSystemConnectionMemo());
        if (addr.isValid()) {
            log.info("new signal mast address created: {} -> {}", getSystemName(), addr);
            log.debug("current aspect is {}, commanded aspect {}", getAspect(), commandedAspect);
            if (addr.isPortAddr()) {
                messageHandler.sendQueryConfig();
                messageHandler.waitQueryConfig();
            }
            sendMessage(commandedAspect);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void nodeLost() {
        super.setLit(false);
    }
    
    /**
     * {@inheritDoc}
     * 
     * Remove the Message Listener for this signal mast
     */
    @Override
    public void dispose() {
        if (messageHandler != null) {
            tc.removeMessageListener(messageHandler);        
            messageHandler = null;
        }
        super.dispose();
    }

    public static String isAccessoryAddressUsed(BiDiBAddress address) {
        for (SignalMast mast : InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBeanSet()) {
            if (mast instanceof BiDiBSignalMast) {
                if (((BiDiBSignalMast) mast).getAddr().isAddressEqual(address)) {
                    return ((BiDiBSignalMast) mast).getDisplayName();
                }
            }
        }
        return null;
    }
    
    private void newKnownAspect(int aspectNum) {
        //log.debug("Available aspects: {}, addr: {}", appearanceToOutput.size(), addr);
        for(Map.Entry<String, Integer> entry : appearanceToOutput.entrySet()) {
            //log.debug("  check for aspect {}", entry.getValue());
            if (entry.getValue() == aspectNum) {
                //log.debug("set aspect {} ({})", entry.getKey(), aspectNum);
                if (aspectNum == unLitId) {
                    //log.debug(" setLit");
                    super.setLit(false);
                }
                else {
                    log.debug(" setAspect to {}", entry.getKey());
                    super.setAspect(entry.getKey());
                    super.setLit(true);
                }
            }
        }
    }
    
    private void createSignalMastListener() {
        //messageHandler = new BiDiBOutputMessageHandler("SIGNALMAST", addr, tc){
        messageHandler = new BiDiBOutputMessageHandler(this, "SIGNALMAST", tc){
            @Override
            public void newOutputState(int state) {
                int newAspect;
                if (addr.isPortAddr()) {
                    // since we do not know what other states than 0 (or 2 for LIGHTPORTS) mean (which aspect),
                    // we simply use what was commanded. Does not really work for spontaneous messages.
                    // So, preferrably use accessory addresses which supports the various aspects.
                    if (messageHandler.getLcType() == LcOutputType.LIGHTPORT) {
                        newAspect = (state == 2 || state == 0) ? 0 : commandedAspect;
                    }
                    else {
                        newAspect = (state == 0) ? 0 : commandedAspect;
                    }
                }
                else {
                    // for others (accessories), the state is the new aspect number
                    newAspect = state;
                }
                log.debug("SIGNALMAST new aspect: {}", newAspect);
                newKnownAspect(newAspect);
            }
            @Override
            public void outputWait(int time) {
                log.debug("SIGNALMAST wait: {}", time);
                //newKnownAspect(commandedAspect);
            }
            @Override
            public void errorState(int err) {
                log.warn("SIGNALMAST error: {} addr: {}", err, addr);
                //newKnownAspect(getUnlitId());
                //super.setAspect(aspect);
                String oldAspect = aspect;
                aspect = null; //means UNKNOWN - there is no INCONSISTENT
                //this.speed = (String) getSignalSystem().getProperty(aspect, "speed"); // NOI18N
                firePropertyChange("Aspect", oldAspect, aspect); // NOI18N
            }
        };
        tc.addMessageListener(messageHandler);
    }
    
    private final static Logger log = LoggerFactory.getLogger(BiDiBSignalMast.class);

}


