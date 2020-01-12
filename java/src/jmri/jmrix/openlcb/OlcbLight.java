/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.openlcb;

import jmri.Light;
import jmri.implementation.AbstractLight;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.BitProducerConsumer;
import org.openlcb.implementations.VersionedValueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jcollell
 */
public class OlcbLight extends AbstractLight {
    
    private static final int PC_DEFAULT_FLAGS = BitProducerConsumer.DEFAULT_FLAGS &
            (~BitProducerConsumer.LISTEN_INVALID_STATE);
    static final boolean DEFAULT_IS_AUTHORITATIVE = true;
    static final boolean DEFAULT_LISTEN = true;
    private boolean _finishedLoad = false;
    
    OlcbAddress addrOn;    // go to On state
    OlcbAddress addrOff;  // go to Off state
    OlcbInterface iface;
    
    VersionedValueListener<Boolean> lightListener;
    BitProducerConsumer pc;
    
    /**
     * Common initialization for both constructors.
     * <p>
     *
     */
    private void init(String address) {
        // build local addresses
        OlcbAddress a = new OlcbAddress(address);
        OlcbAddress[] v = a.split();
        if (v == null) {
            log.error("Did not find usable system name: " + address);
            return;
        }
        switch (v.length) {
            case 2:
                addrOn = v[0];
                addrOff = v[1];
                break;
            default:
                log.error("Can't parse OpenLCB Light system name: " + address);
                return;
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
        lightControlList.stream().forEach((lc) -> {
            lc.activateLightControl();
        });
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
    public void setProperty(String key, Object value) {
        Object old = getProperty(key);
        super.setProperty(key, value);
        if (old != null && value.equals(old)) return;
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
    
    private final static Logger log = LoggerFactory.getLogger(OlcbLight.class);

    public OlcbLight(String systemName) {
        super(systemName);
    }
    
    public OlcbLight(String prefix, String address, OlcbInterface iface) {
        super(prefix + "L" + address);
        this.iface = iface;
        init(address);
    }
}
