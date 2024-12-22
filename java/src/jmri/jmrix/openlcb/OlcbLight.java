package jmri.jmrix.openlcb;

import jmri.Light;
import jmri.LightControl;
import jmri.implementation.AbstractLight;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.openlcb.OlcbInterface;
import org.openlcb.implementations.BitProducerConsumer;
import org.openlcb.implementations.VersionedValueListener;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *
 * @author jcollell
 */
public final class OlcbLight extends AbstractLight {
    
    private static final int PC_DEFAULT_FLAGS = BitProducerConsumer.DEFAULT_FLAGS &
            (~BitProducerConsumer.LISTEN_INVALID_STATE);
    static final boolean DEFAULT_IS_AUTHORITATIVE = true;
    static final boolean DEFAULT_LISTEN = true;
    private boolean _finishedLoad = false;
    
    OlcbAddress addrOn;    // go to On state
    OlcbAddress addrOff;  // go to Off state
    private final OlcbInterface iface;
    private final CanSystemConnectionMemo memo;
    
    VersionedValueListener<Boolean> lightListener;
    BitProducerConsumer pc;
    
    public OlcbLight(String prefix, String address, CanSystemConnectionMemo memo) {
        super(prefix + "L" + address);
        this.memo = memo;
        if (memo != null) { // greatly simplify testing
            this.iface = memo.get(OlcbInterface.class);
        } else {
            this.iface = null;
        }
        init(address);
    }

    /**
     * Common initialization for both constructors.
     * <p>
     *
     */
    private void init(String address) {
        // build local addresses
        OlcbAddress a = new OlcbAddress(address, memo);
        OlcbAddress[] v = a.split(memo);
        if (v == null) {
            log.error("Did not find usable system name: {}", address);
            return;
        }
        if (v.length == 2) {
            addrOn = v[0];
            addrOff = v[1];
        } else {
            log.error("Can't parse OpenLCB Light system name: {}", address);
        }
    }
    
    
    /**
     * Helper function that will be invoked after construction once the properties have been
     * loaded. Used specifically for preventing double initialization when loading lights from
     * XML.
     */
    void finishLoad() {
        int flags = PC_DEFAULT_FLAGS;
        flags = OlcbUtils.overridePCFlagsFromProperties(this, flags);
        pc = new BitProducerConsumer(iface, addrOn.toEventID(),
                addrOff.toEventID(), flags);
        lightListener = new VersionedValueListener<Boolean>(pc.getValue()) {
            @Override
            public void update(Boolean value) {
                setState(value ? Light.ON : Light.OFF);
            }
        };
        // A Light Control will have failed to set its state during xml load
        // as the LightListener is not present, so we re-activate any Light Controls
        activateLight();
    }

    @Override
    @CheckReturnValue
    @Nonnull
    public String getRecommendedToolTip() {
        return addrOn.toDottedString()+";"+addrOff.toDottedString();
    }
    
    /**
     * Activate a light activating all its LightControl objects.
     */
    @Override
    public void activateLight() {
        // during xml load any Light Controls may attempt to set the Light before the
        // lightListener has been set
        if (lightListener==null){
            return;
        }
        lightControlList.stream().forEach(LightControl::activateLightControl);
        mActive = true; // set flag for control listeners
        _finishedLoad = true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setState(int newState) {
        if (_finishedLoad){
            super.setState(newState);
        }
        else {
            log.debug("Light {} status being set while still Activating",this);
        }
    }
    
    /**
     * Set the current state of this Light This routine requests the hardware to
     * change to newState.
     * @param oldState old state
     * @param newState new state
     */
    @Override
    protected void doNewState(int oldState, int newState) {
        switch (newState) {
            case Light.ON:
                lightListener.setFromOwnerWithForceNotify(true);
                break;
            case Light.OFF:
                lightListener.setFromOwnerWithForceNotify(false);
                break;
            case Light.UNKNOWN:
                if (pc != null) {
                    pc.resetToDefault();
                }   break;
            default:
                break;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setProperty(@Nonnull String key, Object value) {
        Object old = getProperty(key);
        super.setProperty(key, value);
        if (value.equals(old)) return;
        if (pc == null) return;
        finishLoad();
    }
    
    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (lightListener != null) lightListener.release();
        if (pc != null) pc.release();
        super.dispose();
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbLight.class);

}
