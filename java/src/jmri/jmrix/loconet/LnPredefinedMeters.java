package jmri.jmrix.loconet;

import jmri.*;
import jmri.implementation.DefaultMeter;
import jmri.implementation.MeterUpdateTask;
import jmri.jmrix.loconet.duplexgroup.swing.LnIPLImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to current and voltage meter from some LocoNet command stations
 *
 * @author Steve G           Copyright (C) 2019
 * @author Bob Jacobsen      Copyright (C) 2019
 * @author Egbert Boerse     Copyright (C) 2019
 * @author Daniel Bergqvist  Copyright (C) 2020
 * @author B. Milhaupt       Copyright (C) 2020
 */
public class LnPredefinedMeters implements LocoNetListener {

    private SlotManager sm = null;
    private LnTrafficController tc = null;
    private final MeterUpdateTask updateTask;
    private final LnMeterInitTask initializationTask;

    /**
     * Create a LnPredefinedMeters object
     *
     * @param scm  connection memo
     */
    public LnPredefinedMeters(LocoNetSystemConnectionMemo scm) {
        this.sm = scm.getSlotManager();
        this.tc = scm.getLnTrafficController();

        updateTask = new MeterUpdateTask(LnConstants.METER_INTERVAL_MS) {
            @Override
            public void requestUpdateFromLayout() {
                sm.sendReadSlot(249);
            }
        };

        tc.addLocoNetListener(~0, this);

        updateTask.initTimer();

        // a work-around to ensure that the LocoNet transmit path is established
        // before making an initial query-mode request
        initializationTask = new LnMeterInitTask(sm.tc, 85);
        initializationTask.initTimer();
        initializationTask.enable();
    }

    @Override
    public void message(LocoNetMessage msg) {
        if (msg.getNumDataElements() != 21
                || msg.getOpCode() != LnConstants.OPC_EXP_RD_SL_DATA
                || msg.getElement(1) != 21
                || msg.getElement(2) != 1
                || msg.getElement(3) != 0x79) {
            return;
        }
        int srcDeviceType = msg.getElement(16);
        if ((srcDeviceType == LnConstants.RE_IPL_DIGITRAX_HOST_BXP88)
            || (srcDeviceType == LnConstants.RE_IPL_DIGITRAX_HOST_LNWI)
            || (srcDeviceType == LnConstants.RE_IPL_DIGITRAX_HOST_BXPA1)) {
            // these devices support Query Mode but always return 0s for
            // voltage/current data
            return;
        }

        float valAmps = msg.getElement(6)/10.0f;
        float valVolts = msg.getElement(4)*2.0f/10.0f;

        int srcSerNum = msg.getElement(18)+128*msg.getElement(19);

        String voltSysName = createSystemName(srcDeviceType, srcSerNum, "Voltage"); // NOI18N
        Meter m = InstanceManager.getDefault(MeterManager.class).getBySystemName(voltSysName);
        updateAddMeter(m, voltSysName, valVolts, true);

        String ampsSysName = createSystemName(srcDeviceType, srcSerNum, "InputCurrent"); // NOI18N
        m = InstanceManager.getDefault(MeterManager.class).getBySystemName(ampsSysName);
        updateAddMeter(m, ampsSysName, valAmps, false);
    }

    public void dispose() {
        for (Meter m: InstanceManager.getDefault(MeterManager.class).getNamedBeanSet()) {
            if (m.getSystemName().startsWith(sm.getSystemPrefix()+"V")) { // NOI18N
                updateTask.disable(m);
                InstanceManager.getDefault(MeterManager.class).deregister(m);
                updateTask.dispose(m);
            }
        }
    }

    public void requestUpdateFromLayout() {
        log.debug("sending request for voltmeter/ammeter information");
        sm.sendReadSlot(249);
    }

    private final String createSystemName(int device, int sn, String typeString) {
        String devName = LnIPLImplementation.getDeviceName(0, device,0,0);
        if (devName == null) {
            devName="["+device+"]"; // NOI18N
        }
        return sm.getSystemPrefix()+"V"+ devName + "(s/n"+sn+")"+typeString; // NOI18N
    }

    private void updateAddMeter(Meter m, String sysName, float value, boolean typeVolt ) {
        if (m == null) {
            Meter newMeter;
            if (typeVolt) {
                // voltMeter not (yet) registered
                newMeter = new DefaultMeter.DefaultVoltageMeter(sysName,
                    Meter.Unit.NoPrefix, 0, 25.4, 0.2, updateTask);
            } else {
                            // ammeter not (yet) registered
                newMeter = new DefaultMeter.DefaultCurrentMeter(sysName,
                    Meter.Unit.NoPrefix, 0, 12.7, 0.1, updateTask);
            }
            try {
                newMeter.setCommandedAnalogValue(value);
            } catch (JmriException e) {
                log.debug("Exception setting {}Meter {} to value {}",
                        (typeVolt?"volt":"current"), // NOI18N
                        sysName, value, e);
            }
            InstanceManager.getDefault(MeterManager.class).register(newMeter);
            log.debug("Added new {}Meter {} with value {}",
                        (typeVolt?"volt":"current"), // NOI18N
                    sysName, value);
        } else {
            try {
                m.setCommandedAnalogValue(value);
            } catch (JmriException e) {
                log.debug("Exception setting {}Meter {} to value {}",
                        (typeVolt?"volt":"current"), // NOI18N
                        sysName, value, e);
            }
            log.debug("Updating currentMeter {} with value {}",
                    sysName, value);
       }
    }

    private final static Logger log = LoggerFactory.getLogger(LnPredefinedMeters.class);
}
