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
 * This class implements a SignalHead that maps the various appearances values to
 * aspect values in the <b>Extended Accessory Decoder Control Packet Format</b>
 * and outputs that packet to the DCC System via the generic CommandStation
 * interface
 * <p>
 * The mapping is as follows:
 * <p>
 * 0 = RED         <br>
 * 1 = YELLOW      <br>
 * 2 = GREEN       <br>
 * 3 = LUNAR       <br>
 * 4 = FLASHRED    <br>
 * 5 = FLASHYELLOW <br>
 * 6 = FLASHGREEN  <br>
 * 7 = FLASHLUNAR  <br>
 * 8 = DARK        <br>
 * <p>
 * The FLASH appearances are expected to be implemented in the decoder.
 *
 * @author Alex Shepherd Copyright (c) 2008
 */
public class DccSignalHead extends AbstractSignalHead {

    public DccSignalHead(String sys, String user) {
        super(sys, user);
        configureHead(sys);
    }

    public DccSignalHead(String sys) {
        super(sys);
        configureHead(sys);
    }

    private void configureHead(String sys) {
        setDefaultOutputs();
        // New method separates the system name and address using $
        if (sys.contains("$")) {
            dccSignalDecoderAddress = Integer.parseInt(sys.substring(sys.indexOf("$") + 1));
            String commandStationPrefix = sys.substring(0, sys.indexOf("$") - 1);
            java.util.List<jmri.CommandStation> connList = jmri.InstanceManager.getList(jmri.CommandStation.class);

            for (CommandStation station : connList) {
                if (station.getSystemPrefix().equals(commandStationPrefix)) {
                    c = station;
                    break;
                }
            }

            if (c == null) {
                c = InstanceManager.getNullableDefault(CommandStation.class);
                log.error("No match against the command station for {}, so will use the default", sys);
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
            log.error("SignalHead decoder address out of range: {}", dccSignalDecoderAddress);
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
            firePropertyChange("Appearance", oldAppearance, newAppearance);
        }
    }

    @Override
    public void setLit(boolean newLit) {
        boolean oldLit = mLit;
        mLit = newLit;
        if (oldLit != newLit) {
            updateOutput();
            // notify listeners, if any
            firePropertyChange("Lit", oldLit, newLit);
        }
    }

    /**
     * Set the held parameter.
     * <p>
     * Note that this does not directly affect the output on the layout; the
     * held parameter is a local variable which affects the aspect only via
     * higher-level logic.
     */
    @Override
    public void setHeld(boolean newHeld) {
        boolean oldHeld = mHeld;
        mHeld = newHeld;
        if (oldHeld != newHeld) {
            // notify listeners, if any
            firePropertyChange("Held", oldHeld, newHeld);
        }
    }

    protected void updateOutput() {
        if (c != null) {
            int aspect = getOutputForAppearance(SignalHead.DARK);

            if (getLit()) {
                Integer app = mAppearance;
                if (appearanceToOutput.containsKey(app)) {
                    aspect = appearanceToOutput.get(app);
                } else {
                    log.error("Unknown appearance {} displays DARK", mAppearance);
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
                 log.error("Unknown appearance {} displays DARK", mAppearance);
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
                c.sendPacket(sigPacket, packetSendCount);
            }
        }
    }

    private CommandStation c;

    private boolean useAddressOffSet = false;

    public void useAddressOffSet(boolean boo) {
        useAddressOffSet = boo;
    }

    public boolean useAddressOffSet() {
        return useAddressOffSet;
    }

    protected HashMap<Integer, Integer> appearanceToOutput = new HashMap<Integer, Integer>();

    public int getOutputForAppearance(int appearance) {
        Integer app = appearance;
        if (!appearanceToOutput.containsKey(app)) {
            log.error("Trying to get appearance {} but it has not been configured", appearance);
            return -1;
        }
        return appearanceToOutput.get(app);
    }

    public void setOutputForAppearance(int appearance, int number) {
        Integer app = appearance;
        if (appearanceToOutput.containsKey(app)) {
            log.debug("Appearance {} is already defined as {}", appearance, appearanceToOutput.get(app));
            appearanceToOutput.remove(app);
        }
        appearanceToOutput.put(app, number);
    }

    /**
     * Create hashmap of default appearance output values.
     */
    private void setDefaultOutputs() {
        appearanceToOutput.put(SignalHead.RED, getDefaultNumberForAppearance(SignalHead.RED));
        appearanceToOutput.put(SignalHead.YELLOW, getDefaultNumberForAppearance(SignalHead.YELLOW));
        appearanceToOutput.put(SignalHead.GREEN, getDefaultNumberForAppearance(SignalHead.GREEN));
        appearanceToOutput.put(SignalHead.LUNAR, getDefaultNumberForAppearance(SignalHead.LUNAR));
        appearanceToOutput.put(SignalHead.FLASHRED, getDefaultNumberForAppearance(SignalHead.FLASHRED));
        appearanceToOutput.put(SignalHead.FLASHYELLOW, getDefaultNumberForAppearance(SignalHead.FLASHYELLOW));
        appearanceToOutput.put(SignalHead.FLASHGREEN, getDefaultNumberForAppearance(SignalHead.FLASHGREEN));
        appearanceToOutput.put(SignalHead.FLASHLUNAR, getDefaultNumberForAppearance(SignalHead.FLASHLUNAR));
        appearanceToOutput.put(SignalHead.DARK, getDefaultNumberForAppearance(SignalHead.DARK));
    }

    /**
     * Replaced by {@link #getDefaultNumberForAppearance} for misspelling
     * @deprecated since 4.5.17
     */
    @Deprecated
    public static int getDefaultNumberForApperance(int i) {
        return getDefaultNumberForAppearance(i);
    }

    public static int getDefaultNumberForAppearance(int i) {
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

    private int packetSendCount = 3;
    /**
     * Set Number of times the packet should be sent to the track.
     * @param count - less than 1 is treated as 1
     */
    public void setDccSignalHeadPacketSendCount(int count) {
        if (count > 0) {
            packetSendCount = count;
        } else {
            packetSendCount = 1;
        }
    }

    /**
     * Get the number of times the packet should be sent to the track.
     *
     * @return the count
     */
    public int getDccSignalHeadPacketSendCount() {
        return packetSendCount;
    }

    private int dccSignalDecoderAddress;

    @Override
    boolean isTurnoutUsed(Turnout t) {
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(DccSignalHead.class);

}
