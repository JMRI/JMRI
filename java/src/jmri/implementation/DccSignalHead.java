package jmri.implementation;

import java.util.HashMap;
import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.NmraPacket;
import jmri.SignalHead;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a SignalHead the maps the various appearances values to
 * aspect values in the <B>Extended Accessory Decoder Control Packet Format</B>
 * and outputs that packet to the DCC System via the generic CommandStation
 * interface
 * <P>
 * The mapping is as follows:
 * <P>
 * 0 = RED         <BR>
 * 1 = YELLOW      <BR>
 * 2 = GREEN       <BR>
 * 3 = LUNAR       <BR>
 * 4 = FLASHRED    <BR>
 * 5 = FLASHYELLOW <BR>
 * 6 = FLASHGREEN  <BR>
 * 7 = FLASHLUNAR  <BR>
 * 8 = DARK        <BR>
 * <P>
 * The FLASH appearances are expected to be implemented in the decoder.
 *
 * @author Alex Shepherd Copyright (c) 2008
 */
public class DccSignalHead extends AbstractSignalHead {

    private final static Logger log = LoggerFactory.getLogger(DccSignalHead.class);

    public DccSignalHead(String sys, String user) {
        super(sys, user);
        configureHead(sys);

    }

    public DccSignalHead(String sys) {
        super(sys);
        configureHead(sys);
    }

    void configureHead(String sys) {
        //Set the default appearances
        appearanceToOutput.put(Integer.valueOf(SignalHead.RED), getDefaultNumberForApperance(SignalHead.RED));
        appearanceToOutput.put(Integer.valueOf(SignalHead.YELLOW), getDefaultNumberForApperance(SignalHead.YELLOW));
        appearanceToOutput.put(Integer.valueOf(SignalHead.GREEN), getDefaultNumberForApperance(SignalHead.GREEN));
        appearanceToOutput.put(Integer.valueOf(SignalHead.LUNAR), getDefaultNumberForApperance(SignalHead.LUNAR));
        appearanceToOutput.put(Integer.valueOf(SignalHead.FLASHRED), getDefaultNumberForApperance(SignalHead.FLASHRED));
        appearanceToOutput.put(Integer.valueOf(SignalHead.FLASHYELLOW), getDefaultNumberForApperance(SignalHead.FLASHYELLOW));
        appearanceToOutput.put(Integer.valueOf(SignalHead.FLASHGREEN), getDefaultNumberForApperance(SignalHead.FLASHGREEN));
        appearanceToOutput.put(Integer.valueOf(SignalHead.FLASHLUNAR), getDefaultNumberForApperance(SignalHead.FLASHLUNAR));
        appearanceToOutput.put(Integer.valueOf(SignalHead.DARK), getDefaultNumberForApperance(SignalHead.DARK));
        //New method seperates the system name and address using $
        if (sys.contains("$")) {
            dccSignalDecoderAddress = Integer.parseInt(sys.substring(sys.indexOf("$") + 1, sys.length()));
            String commandStationPrefix = sys.substring(0, sys.indexOf("$") - 1);
            java.util.List<jmri.CommandStation> connList = jmri.InstanceManager.getList(jmri.CommandStation.class);

            for (int x = 0; x < connList.size(); x++) {
                jmri.CommandStation station = connList.get(x);
                if (station.getSystemPrefix().equals(commandStationPrefix)) {
                    c = station;
                    break;
                }
            }

            if (c == null) {
                c = InstanceManager.getNullableDefault(CommandStation.class);
                log.error("No match against the command station for " + sys + ", so will use the default");
            }
        } else {
            c = InstanceManager.getNullableDefault(CommandStation.class);
            if ((sys.length() > 2) && ((sys.charAt(1) == 'H') || (sys.charAt(1) == 'h'))) {
                dccSignalDecoderAddress = Integer.parseInt(sys.substring(2, sys.length()));
            } else {
                dccSignalDecoderAddress = Integer.parseInt(sys);
            }
        }
        // validate the decoder address
        // now some systems don't support this whole range
        // also depending on how you view the NRMA spec, 1 - 2044 or 1 - 2048
        if (dccSignalDecoderAddress < NmraPacket.accIdLowLimit || dccSignalDecoderAddress > NmraPacket.accIdAltHighLimit) {
            log.error("SignalHead decoder address out of range: " + dccSignalDecoderAddress);
            throw new IllegalArgumentException("SignalHead decoder address out of range: " + dccSignalDecoderAddress);
        }
    }

    @Override
    public void setAppearance(int newAppearance) {
        int oldAppearance = mAppearance;
        mAppearance = newAppearance;

        if (oldAppearance != newAppearance) {
            updateOutput();

            // notify listeners, if any
            firePropertyChange("Appearance", Integer.valueOf(oldAppearance), Integer.valueOf(newAppearance));
        }
    }

    @Override
    public void setLit(boolean newLit) {
        boolean oldLit = mLit;
        mLit = newLit;
        if (oldLit != newLit) {
            updateOutput();
            // notify listeners, if any
            firePropertyChange("Lit", Boolean.valueOf(oldLit), Boolean.valueOf(newLit));
        }
    }

    /**
     * Set the held parameter.
     * <P>
     * Note that this does not directly effect the output on the layout; the
     * held parameter is a local variable which effects the aspect only via
     * higher-level logic
     */
    @Override
    public void setHeld(boolean newHeld) {
        boolean oldHeld = mHeld;
        mHeld = newHeld;
        if (oldHeld != newHeld) {
            // notify listeners, if any
            firePropertyChange("Held", Boolean.valueOf(oldHeld), Boolean.valueOf(newHeld));
        }
    }

    protected void updateOutput() {
        if (c != null) {
            int aspect = getOutputForAppearance(SignalHead.DARK);

            if (getLit()) {
                Integer app = Integer.valueOf(mAppearance);
                if (appearanceToOutput.containsKey(app)) {
                    aspect = appearanceToOutput.get(app);
                } else {
                    log.error("Unknown appearance " + mAppearance + " displays DARK");
                }
                /*        switch( mAppearance ){
                 case SignalHead.DARK:        aspect = 8 ; break;
                 case SignalHead.RED:         aspect = 0 ; break;
                 case SignalHead.YELLOW:      aspect = 1 ; break;
                 case SignalHead.GREEN:       aspect = 2 ; break;
                 case SignalHead.LUNAR:       aspect = 3 ; break;
                 case SignalHead.FLASHRED:    aspect = 4 ; break;
                 case SignalHead.FLASHYELLOW: aspect = 5 ; break;
                 case SignalHead.FLASHGREEN:  aspect = 6 ; break;
                 case SignalHead.FLASHLUNAR:  aspect = 7 ; break;
                 default :                    aspect = 8;
                 log.error("Unknown appearance " + mAppearance+" displays DARK");
                 break;
                 }*/
            }

            byte[] sigPacket;
            if (useAddressOffSet) {
                sigPacket = NmraPacket.accSignalDecoderPkt(dccSignalDecoderAddress, aspect);
            } else {
                sigPacket = NmraPacket.altAccSignalDecoderPkt(dccSignalDecoderAddress, aspect);
            }
            if (sigPacket != null) {
                c.sendPacket(sigPacket, 3);
            }
        }
    }

    CommandStation c;

    boolean useAddressOffSet = false;

    public void useAddressOffSet(boolean boo) {
        useAddressOffSet = boo;
    }

    public boolean useAddressOffSet() {
        return useAddressOffSet;
    }

    protected HashMap<Integer, Integer> appearanceToOutput = new HashMap<Integer, Integer>();

    public int getOutputForAppearance(int appearance) {
        Integer app = Integer.valueOf(appearance);
        if (!appearanceToOutput.containsKey(app)) {
            log.error("Trying to get appearance " + appearance + " but it has not been configured");
            return -1;
        }
        return appearanceToOutput.get(app);
    }

    public void setOutputForAppearance(int appearance, int number) {
        Integer app = Integer.valueOf(appearance);
        if (appearanceToOutput.containsKey(app)) {
            log.debug("Appearance " + appearance + " is already defined as " + appearanceToOutput.get(app));
            appearanceToOutput.remove(app);
        }
        appearanceToOutput.put(app, number);
    }

    public static int getDefaultNumberForApperance(int i) {
        switch (i) {
            case SignalHead.DARK:
                return 8;
            case SignalHead.RED:
                return 0;
            case SignalHead.YELLOW:
                return 1;
            case SignalHead.GREEN:
                return 2;
            case SignalHead.LUNAR:
                return 3;
            case SignalHead.FLASHRED:
                return 4;
            case SignalHead.FLASHYELLOW:
                return 5;
            case SignalHead.FLASHGREEN:
                return 6;
            case SignalHead.FLASHLUNAR:
                return 7;
            default:
                return 8;
        }
    }

    int dccSignalDecoderAddress;

    @Override
    boolean isTurnoutUsed(Turnout t) {
        return false;
    }
}
