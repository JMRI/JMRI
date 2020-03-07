package jmri.implementation;

import java.util.HashMap;
import javax.annotation.Nonnull;
import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.NmraPacket;
import jmri.SignalMast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a SignalMast that uses <b>Extended Accessory Decoder
 * Control Packet Format</b>
 * and outputs that packet to the DCC System via the generic CommandStation
 * interface.
 * <p>
 * This implementation writes out to the physical signal when it's commanded to
 * change appearance, and updates its internal state when it hears commands from
 * other places.
 * <p>
 * System name specifies the creation information:
 * <pre>
 * IF$dsm:basic:one-searchlight(123)
 * </pre> The name is a colon-separated series of terms:
 * <ul>
 *   <li>IF$dsm - defines signal masts of this type
 *   <li>basic - name of the signaling system
 *   <li>one-searchlight - name of the particular aspect map
 *   <li>(123) - DCC address for the decoder
 * </ul>
 * <p>
 * Based upon {@link jmri.implementation.DccSignalHead} by Alex Shepherd
 *
 * @author Kevin Dickerson Copyright (c) 2012
 */
public class DccSignalMast extends AbstractSignalMast {

    public DccSignalMast(String sys, String user) {
        super(sys, user);
        configureFromName(sys);
    }

    public DccSignalMast(String sys) {
        super(sys);
        configureFromName(sys);
    }

    public DccSignalMast(String sys, String user, String mastSubType) {
        super(sys, user);
        mastType = mastSubType;
        configureFromName(sys);
    }

    private String mastType = "F$dsm";

    protected void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) {
            log.error("SignalMast system name needs at least three parts: {}", systemName);
            throw new IllegalArgumentException("System name needs at least three parts: " + systemName);
        }
        if (!parts[0].endsWith(mastType)) {
            log.warn("First part of SignalMast system name is incorrect {} : {}", systemName, mastType);
        } else {
            String commandStationPrefix = parts[0].substring(0, parts[0].indexOf("$") - 1);
            java.util.List<jmri.CommandStation> connList = jmri.InstanceManager.getList(jmri.CommandStation.class);

            for (jmri.CommandStation station : connList) {
                log.trace(" check against {} with letter {}", station, station.getSystemPrefix());
                if (station.getSystemPrefix().equals(commandStationPrefix)) {
                    c = station;
                    break;
                }
            }

            if (c == null) {
                c = InstanceManager.getNullableDefault(CommandStation.class);
                log.error("No match against the command station for \"{}\", so will use the default {}", commandStationPrefix, c);
            }
        }
        String system = parts[1];
        String mast = parts[2];

        mast = mast.substring(0, mast.indexOf("("));
        log.trace("In configureFromName setMastType to {}", mast);
        setMastType(mast);
        
        String tmp = parts[2].substring(parts[2].indexOf("(") + 1, parts[2].indexOf(")"));
        try {
            dccSignalDecoderAddress = Integer.parseInt(tmp);
        } catch (NumberFormatException e) {
            log.warn("DCC accessory address SystemName {} is not in the correct format", systemName);
        }
        configureSignalSystemDefinition(system);
        configureAspectTable(system, mast);
    }

    protected HashMap<String, Integer> appearanceToOutput = new HashMap<String, Integer>();

    public void setOutputForAppearance(String appearance, int number) {
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
    protected int packetSendCount = 3;  // default 3

    @Override
    public void setAspect(@Nonnull String aspect) {
        if (appearanceToOutput.containsKey(aspect) && appearanceToOutput.get(aspect) != -1) {
            c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, appearanceToOutput.get(aspect)), packetSendCount);
        } else {
            log.warn("Trying to set aspect ({}) that has not been configured on mast {}", aspect, getDisplayName());
        }
        super.setAspect(aspect);
    }

    @Override
    public void setLit(boolean newLit) {
        if (!allowUnLit() || newLit == getLit()) {
            return;
        }
        if (newLit) {
            setAspect(getAspect());
        } else {
            c.sendPacket(NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, unLitId), packetSendCount);
        }
        super.setLit(newLit);
    }

    int unLitId = 31;

    public void setUnlitId(int i) {
        unLitId = i;
    }

    public int getUnlitId() {
        return unLitId;
    }

    public int getDccSignalMastAddress() {
        return dccSignalDecoderAddress;
    }

    public CommandStation getCommandStation() {
        return c;
    }

    protected CommandStation c;

    protected int dccSignalDecoderAddress;

    public static String isDCCAddressUsed(int addr) {
        for (SignalMast mast : InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBeanSet()) {
            if (mast instanceof jmri.implementation.DccSignalMast) {
                if (((DccSignalMast) mast).getDccSignalMastAddress() == addr) {
                    return ((DccSignalMast) mast).getDisplayName();
                }
            }
        }
        return null;
    }

    /**
     * Set Number of times the packet should be sent.
     * @param count - less than 1 is treated as 1.
     */
    public void setDccSignalMastPacketSendCount(int count) {
        if (count >= 0) {
            packetSendCount = count;
        } else {
            packetSendCount = 1;
        }
    }

    /**
     * Get the number of times the packet should be sent to the track.
     *
     * @return the count.
     */
    public int getDccSignalMastPacketSendCount() {
        return packetSendCount;
    }

    private final static Logger log = LoggerFactory.getLogger(DccSignalMast.class);

}
