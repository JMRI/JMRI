package jmri.jmrix.loconet.logixng;

import jmri.jmrit.logixng.actions.*;

import java.util.*;

import jmri.*;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrix.loconet.*;

/**
 * Sets the speed to zero if the loco hasn't been used in a while.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class SetSpeedZero extends AbstractDigitalAction {

    private static final int NUM_LOCO_SLOTS_TO_CHECK = 119;

    private LocoNetSystemConnectionMemo _memo;

    public SetSpeedZero(String sys, String user, LocoNetSystemConnectionMemo memo) {
        super(sys, user);
        _memo = memo;
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        SetSpeedZero copy = new SetSpeedZero(sysName, userName, _memo);
        copy.setComment(getComment());
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return CategoryLocoNet.LOCONET;
    }

    public void setMemo(LocoNetSystemConnectionMemo memo) {
        assertListenersAreNotRegistered(log, "setMemo");
        _memo = memo;
    }

    public LocoNetSystemConnectionMemo getMemo() {
        return _memo;
    }

    private void setSlotSpeedZero(LocoNetSlot slot) {
        boolean inUse =
                slot.slotStatus() == LnConstants.LOCO_IN_USE
                || slot.slotStatus() == LnConstants.LOCO_COMMON;

        boolean upLinkConsist =
                slot.consistStatus() == LnConstants.CONSIST_MID
                || slot.consistStatus() == LnConstants.CONSIST_SUB;

        if (inUse && !upLinkConsist && slot.speed() != 0) {
            LocoNetMessage msg = new LocoNetMessage(4);
            msg.setOpCode(LnConstants.OPC_LOCO_SPD);
            msg.setElement(1, slot.getSlot());
            msg.setElement(2, 0);
            _memo.getLnTrafficController().sendLocoNetMessage(msg);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() {
        for (int i=1; i <= NUM_LOCO_SLOTS_TO_CHECK; i++) {
            setSlotSpeedZero(_memo.getSlotManager().slot(i));
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "SetSpeedZero_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "SetSpeedZero_Long",
                _memo != null ? _memo.getUserName() : Bundle.getMessage("MemoNotSet"));
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetSpeedZero.class);

}
