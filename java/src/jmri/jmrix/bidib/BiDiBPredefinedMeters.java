package jmri.jmrix.bidib;

import java.util.HashMap;
import java.util.Map;
import jmri.*;
import jmri.implementation.DefaultMeter;
import jmri.implementation.MeterUpdateTask;

import org.bidib.jbidibc.core.DefaultMessageListener;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.messages.CurrentValue;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.message.BoostQueryMessage;
import org.bidib.jbidibc.messages.utils.NodeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provide access to voltage and current readings
 *
 * @author Mark Underwood    Copyright (C) 2015
 * @author Paul Bender       Copyright (C) 2017
 * @author Daniel Bergqvist  Copyright (C) 2020
 * @author Eckart Meyer      Copyright (C) 2021
 */
public class BiDiBPredefinedMeters {

    private BiDiBTrafficController tc;
    private final BiDiBSystemConnectionMemo _memo;
    private final MeterUpdateTask updateTask;
    private final Map<Integer, Meter> currentMeters = new HashMap<>();
    private final Map<Integer, Meter> voltageMeters = new HashMap<>();

    private boolean enabled = false;  // disable by default; prevent polling when not being used.

    public BiDiBPredefinedMeters(BiDiBSystemConnectionMemo memo) {

        _memo = memo;
        tc = _memo.getBiDiBTrafficController();

        updateTask = new UpdateTask(-1);

//        // scan nodes list for booster nodes
        Map<Long, Node> nodes = tc.getNodeList();
        for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            if (NodeUtils.hasBoosterFunctions(node.getUniqueId())) {
                log.trace("Booster - node addr: {}, node uid: {}", node.getAddr(), node);
                String sysname = String.format("X%010x", node.getUniqueId() & 0xffffffffffL);
                Meter currentMeter = new DefaultMeter.DefaultCurrentMeter(
                        memo.getSystemPrefix() + InstanceManager.getDefault(MeterManager.class).typeLetter() + sysname + ":BoosterCurrent",
                        Meter.Unit.Milli, 0, 20224.0, 1, updateTask);
                currentMeters.put(NodeUtils.convertAddress(node.getAddr()), currentMeter);

                Meter voltageMeter = new DefaultMeter.DefaultVoltageMeter(
                        memo.getSystemPrefix() + InstanceManager.getDefault(MeterManager.class).typeLetter() + sysname + ":BoosterVoltage",
                        Meter.Unit.Milli, 0, 25000.0, 100, updateTask);
                voltageMeters.put(NodeUtils.convertAddress(node.getAddr()), voltageMeter);

                InstanceManager.getDefault(MeterManager.class).register(currentMeter);
                InstanceManager.getDefault(MeterManager.class).register(voltageMeter);

                log.debug("BiDiBPredefinedMeters constructor called");
            }
        }
    }

    public void setBiDiBTrafficController(BiDiBTrafficController controller) {
        tc = controller;
    }

    private void disposeMeter(Meter meter) {
        updateTask.disable(meter);
        InstanceManager.getDefault(MeterManager.class).deregister(meter);
        updateTask.dispose(meter);
    }

    public void dispose() {
        for(Map.Entry<Integer, Meter> entry : currentMeters.entrySet()) {
            disposeMeter(entry.getValue());
        }
        for(Map.Entry<Integer, Meter> entry : voltageMeters.entrySet()) {
            disposeMeter(entry.getValue());
        }
//        updateTask.disable(currentMeter);
//        updateTask.disable(voltageMeter);
//        InstanceManager.getDefault(MeterManager.class).deregister(currentMeter);
//        InstanceManager.getDefault(MeterManager.class).deregister(voltageMeter);
//        updateTask.dispose(currentMeter);
//        updateTask.dispose(voltageMeter);
    }


    private class UpdateTask extends MeterUpdateTask {

        MessageListener messageListener = null;

        public UpdateTask(int interval) {
            super(interval);
            createBoosterDiagListener();
        }

        @Override
        public void enable(){
            enabled = true;
            // TODO: set feature to enable booster diag messages - and switch it off by default somewhere
            tc.addMessageListener(messageListener);
            log.info("Enabled meter.");
            super.enable();
        }

        @Override
        public void disable(){
            if (!enabled) return;
            super.disable();
            enabled = false;
            // TODO: set feature to disable booster diag messages
            tc.removeMessageListener(messageListener);
            log.info("Disabled meter.");
        }

        private void setCurrent(byte[] address, double value) throws JmriException {
            Meter meter = currentMeters.get(NodeUtils.convertAddress(address));
            log.trace("setCurrent - addr: {}, Meter: {}, value: {}", address, meter, value);
            if (meter != null) {
                meter.setCommandedAnalogValue(value);
            }
        }

        private void setVoltage(byte[] address, double value) throws JmriException {
            Meter meter = voltageMeters.get(NodeUtils.convertAddress(address));
            log.trace("setVoltage - addr: {}, Meter: {}, value: {}", address, meter, value);
            if (meter != null) {
                meter.setCommandedAnalogValue(value);
            }
        }

        @Override
        public void requestUpdateFromLayout() {
            Map<Long, Node> nodes = tc.getNodeList();
            for(Map.Entry<Long, Node> entry : nodes.entrySet()) {
                Node node = entry.getValue();
                if (NodeUtils.hasBoosterFunctions(node.getUniqueId())) {
                    tc.sendBiDiBMessage(new BoostQueryMessage(), node);
                }
            }
        }

        private void createBoosterDiagListener() {
            // to listen messages related to track power.
            messageListener = new DefaultMessageListener() {
                @Override
                public void boosterDiag(byte[] address, int messageNum, CurrentValue current, int voltage, int temperature) {
                    log.info("METER booster diag was signalled: node addr: {}, current: {}, voltage: {}, temperature: {}",
                            address, current, voltage, temperature);
                    try {
                        setCurrent(address, current.getCurrent() * 1.0f);
                        setVoltage(address, voltage * 100.0f); //units of 100mV
                    } catch (JmriException e) {
                        log.error("exception thrown by setCurrent or setVoltage", e);
                    }
                }
            };
        }


    }

    private static final Logger log = LoggerFactory.getLogger(BiDiBPredefinedMeters.class);

}
