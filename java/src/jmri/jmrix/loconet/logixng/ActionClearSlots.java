package jmri.jmrix.loconet.logixng;

import jmri.jmrit.logixng.actions.*;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrix.loconet.*;

/**
 * Sets all engine slots to status common
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ActionClearSlots extends AbstractDigitalAction {

    private static final int NUM_LOCO_SLOTS_TO_CLEAR = 119;
    
    private LocoNetSystemConnectionMemo _memo;
    
    public ActionClearSlots(String sys, String user, LocoNetSystemConnectionMemo memo) {
        super(sys, user);
        _memo = memo;
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionUpdateSlots copy = new ActionUpdateSlots(sysName, userName, _memo);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return CategoryLocoNet.LOCONET;
    }

    public void setMemo(LocoNetSystemConnectionMemo memo) {
        assertListenersAreNotRegistered(log, "setMemo");
        _memo = memo;
    }
    
    public LocoNetSystemConnectionMemo getMemo() {
        return _memo;
    }
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        for (int i=1; i <= NUM_LOCO_SLOTS_TO_CLEAR; i++) {
            LocoNetSlot slot = _memo.getSlotManager().slot(i);
            if ((slot.slotStatus() & LnConstants.LOCOSTAT_MASK) != LnConstants.LOCO_FREE) {
//                _memo.getLnTrafficController().sendLocoNetMessage(slot.writeStatus(LnConstants.LOCO_FREE));
                _memo.getLnTrafficController().sendLocoNetMessage(slot.releaseSlot());
            }
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
        return Bundle.getMessage(locale, "ActionClearSlots_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionClearSlots_Long",
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


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionClearSlots.class);

}
