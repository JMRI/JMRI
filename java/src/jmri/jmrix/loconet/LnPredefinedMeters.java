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
 */
public class LnPredefinedMeters implements LocoNetListener {

    private SlotManager sm = null;
    private LnTrafficController tc = null;
    private final MeterUpdateTask updateTask;

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
    }

    @Override
    public void message(LocoNetMessage msg) {
        try {
            if (msg.getOpCode() != LnConstants.OPC_EXP_RD_SL_DATA
                    || msg.getElement(1) != 21
                    || msg.getElement(2) != 1
                    || msg.getElement(3) != 0x79) {
                return;
            }

            float valAmps = msg.getElement(6)/10.0f;
            float valVolts = msg.getElement(4)*2.0f/10.0f;

            int srcDeviceType = msg.getElement(16);
            int srcSerNum = msg.getElement(18)+128*msg.getElement(19);

            String voltSysName = getSystemName(srcDeviceType, srcSerNum, "Voltage");

            String ampsSysName = getSystemName(srcDeviceType, srcSerNum, "InputCurrent");

            Meter m = InstanceManager.getDefault(MeterManager.class).getBySystemName(ampsSysName);
            if (m == null) {
                // ammeter not (yet) registered
                Meter newCurrentMeter = new DefaultMeter.DefaultCurrentMeter(
                    ampsSysName,
                    Meter.Unit.NoPrefix, 0, 12.7, 0.1, updateTask);
                newCurrentMeter.setCommandedAnalogValue(valAmps);
                InstanceManager.getDefault(MeterManager.class).register(newCurrentMeter);
                log.warn("Adding ammeter {} with value {}",
                        ampsSysName, valAmps);
            } else {
                m.setCommandedAnalogValue(valAmps);
                log.warn("Updating ammeter {} with value {}",
                        ampsSysName, valAmps);
            }

            m = InstanceManager.getDefault(MeterManager.class).getBySystemName(voltSysName);
            if (m == null) {
                // volt not (yet) registered
                Meter newVoltMeter = new DefaultMeter.DefaultVoltageMeter(
                    voltSysName,
                    Meter.Unit.NoPrefix, 0, 25.6, 0.2, updateTask);
                newVoltMeter.setCommandedAnalogValue(valVolts);
                InstanceManager.getDefault(MeterManager.class).register(newVoltMeter);
                log.warn("Adding voltmeter {} with value {}",
                        ampsSysName, valVolts);
            } else {
                m.setCommandedAnalogValue(valVolts);
                log.warn("Updating volt meter {} with value {}",
                        ampsSysName, valAmps);
            }
        } catch (JmriException e) {
            log.error("exception thrown by setCurrent or setVoltage", e);
        }
    }

    public void dispose() {
        for (Meter m: InstanceManager.getDefault(MeterManager.class).getNamedBeanSet()) {
            if (m.getSystemName().startsWith(sm.getSystemPrefix()+"V")) {
                InstanceManager.getDefault(MeterManager.class).deregister(m);
                updateTask.disable(m);
                updateTask.dispose(m);
            }
        }
    }

    public void requestUpdateFromLayout() {
        sm.sendReadSlot(249);
    }

    private final String getSystemName(int device, int sn, String typeString) {
        return sm.getSystemPrefix()+"V"+
                LnIPLImplementation.getDeviceName(0, device,0,0) +
                "(s/n"+sn+")"+typeString;
    }

    private final static Logger log = LoggerFactory.getLogger(LnPredefinedMeters.class);

}
