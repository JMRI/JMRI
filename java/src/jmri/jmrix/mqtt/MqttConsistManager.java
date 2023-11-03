/**
 * Consist Manager for use with the MqttConsist class for the
 * consists it builds.
 *
 * @author Dean Cording Copyright (C) 2023
 */
package jmri.jmrix.mqtt;

import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.implementation.AbstractConsistManager;
import javax.annotation.Nonnull;

public class MqttConsistManager extends AbstractConsistManager {

    protected MqttSystemConnectionMemo adapterMemo;

    /**
     * Constructor - call the constructor for the superclass, and initialize the
     * consist reader thread, which retrieves consist information from the
     * command station.
     *
     * @param memo the associated connection memo
     */
    public MqttConsistManager(MqttSystemConnectionMemo memo) {
        super();
        adapterMemo = memo;
    }

    public void setSendTopic(@Nonnull String sendTopicPrefix) {
        this.sendTopicPrefix = sendTopicPrefix;
    }

    @Nonnull
    public String sendTopicPrefix = "cab/{0}/consist";


    /**
     * This implementation does not support advanced consists, so return true.
     *
     */
    @Override
    public boolean isCommandStationConsistPossible() {
        return true;
    }

    /**
     * Does a CS consist require a separate consist address? CS consist
     * addresses are assigned by the user, so return true.
     *
     */
    @Override
    public boolean csConsistNeedsSeperateAddress() {
        return true;
    }

    /**
     * Add a new MQTT Consist with the given address to
     * consistTable/consistList.
     */
    @Override
    public Consist addConsist(LocoAddress address) {
        if (! (address instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("address is not a DccLocoAddress object");
        }
        if (consistTable.containsKey(address)) { // no duplicates allowed across all connections
            return consistTable.get(address);
        }
        MqttConsist consist;
        consist = new MqttConsist((DccLocoAddress) address, adapterMemo,
                sendTopicPrefix);
        consistTable.put(address, consist);
        notifyConsistListChanged();
        return consist;
    }

    /* Request an update from the layout, loading
     * Consists from the command station.
     */
    @Override
    public void requestUpdateFromLayout() {
    }

    @Override
    protected boolean shouldRequestUpdateFromLayout() {
        return false;
    }

    /**
     * If a consist exists with the given address, the consist is activated on the controller,
     * otherwise it does nothing.
     * This is used by a throttle in case it is controlling a consist.
     * @param address Consist address to be activated
     */
    public void activateConsist(LocoAddress address) {

        if (!consistTable.containsKey(address)) return;

        ((MqttConsist)consistTable.get(address)).activate();

    }

    /**
     * If a consist exists with the given address, the consist is deactivated on the controller,
     * otherwise it does nothing.
     * This is used by a throttle in case it is controlling a consist.
     * @param address Consist address to be deactivated
     */
    public void deactivateConsist(LocoAddress address) {

        if (!consistTable.containsKey(address)) return;

        ((MqttConsist)consistTable.get(address)).deactivate();

    }



//    private final static Logger log = LoggerFactory.getLogger(MqttConsistManager.class);

}
