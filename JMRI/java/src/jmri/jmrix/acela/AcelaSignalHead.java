package jmri.jmrix.acela;

import jmri.implementation.DefaultSignalHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSignalHead for Acela signals based upon
 * Grapevine example by Bob Jacobsen.
 *
 * @author Bob Coleman Copyright (C) 2009
 */
public class AcelaSignalHead extends DefaultSignalHead {

    AcelaSystemConnectionMemo _memo = null;

    /**
     * Create a SignalHead object, with only a system name.
     * <p>
     * 'systemName' should have been previously validated
     */
    public AcelaSignalHead(String systemName, AcelaSystemConnectionMemo memo) {
        super(systemName);
        _memo = memo;
        // Save system Name
        tSystemName = systemName;

        // Extract the Bit from the name
        int num = AcelaAddress.getBitFromSystemName(systemName, _memo.getSystemPrefix());
        addr = num;

        AcelaNode tNode = AcelaAddress.getNodeFromSystemName(tSystemName, _memo);
        if (tNode == null) {
            // node does not exist, ignore call
            log.error("Can't find new Acela Signal with name '{}'", tSystemName);
            return;
        }
        tNode.setOutputSpecial(addr, 1);
        tNode.setOutputSignalHeadType(addr, AcelaNode.UKNOWN);
    }

    /**
     * Create a SignalHead object, with both system and user names.
     * <p>
     * 'systemName' should have been previously validated
     */
    public AcelaSignalHead(String systemName, String userName, AcelaSystemConnectionMemo memo) {
        super(systemName, userName);
        _memo = memo;
        // Save system Name
        tSystemName = systemName;

        // Extract the Bit from the name
        int num = AcelaAddress.getBitFromSystemName(systemName, _memo.getSystemPrefix());
        addr = num;

        AcelaNode tNode = AcelaAddress.getNodeFromSystemName(tSystemName, _memo);
        if (tNode == null) {
            // node does not exist, ignore call
            log.error("Can't find new Acela Signal with name '{}'", tSystemName);
            return;
        }
        tNode.setOutputSpecial(addr, 1);
        tNode.setOutputSignalHeadType(addr, AcelaNode.UKNOWN);
    }

    /**
     * Handle a request to change state on layout
     */
    @Override
    protected void updateOutput() {
        AcelaNode tNode = AcelaAddress.getNodeFromSystemName(tSystemName,_memo);
        if (tNode == null) {
            // node does not exist, ignore call
            log.error("Can't resolve Acela Signal with name '{}'. command ingnored", tSystemName);
            return;
        }

        // sort out states
        int cmd;
        if (mLit) {
            switch (mAppearance) {
                case RED:
                    cmd = 1;
                    break;
                case FLASHRED:
                    cmd = 2;
                    break;
                case YELLOW:
                    cmd = 3;
                    break;
                case FLASHYELLOW:
                    cmd = 4;
                    break;
                case GREEN:
                    cmd = 5;
                    break;
                case FLASHGREEN:
                    cmd = 6;
                    break;
                case DARK:
                    cmd = 7;
                    break;
                default:
                    log.warn("Unexpected new appearance: {}", mAppearance);
                    cmd = 2;
                    break;  // flash red for error
            }
        } else {
            cmd = 7; // set dark if not lit
        }
        tNode.setOutputSpecial(addr, cmd);
        tNode.setOutputBit(addr, true);

    }

    // flashing is done on the cards, so we don't have to
    // do it manually
    @Override
    public void startFlash() {
    }

    @Override
    public void stopFlash() {
    }

    // data members
    String tSystemName; // System Name of this signal head
    int addr;         // output address

    private final static Logger log = LoggerFactory.getLogger(AcelaSignalHead.class);

}
