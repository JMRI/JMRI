package jmri.jmrix.tmcc;

import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.implementation.AbstractConsistManager;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consist Manager for use with the TmccConsist class for the
 * consists it builds.
 *
 * @author Dean Cording Copyright (C) 2023
 * with edits/additions by
 * @author Timothy Jump Copyright (C) 2025
 */
public class TmccConsistManager extends AbstractConsistManager {

    protected TmccSystemConnectionMemo adapterMemo;

    /**
     * Constructor - call the constructor for the superclass, and initialize the
     * consist reader thread, which retrieves consist information from the
     * command station.
     *
     * @param memo the associated connection memo
     */
    public TmccConsistManager(TmccSystemConnectionMemo memo) {
        super();
        adapterMemo = memo;
    }

    public void setSendTopic(@Nonnull String sendTopicPrefix) {
        this.sendTopicPrefix = sendTopicPrefix;
    }

    @Nonnull
    public String sendTopicPrefix = "cab/{0}/consist";


    /**
     * This implementation does support advanced consists, so return true.
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
     * Add a new TMCC Consist with the given address to consistTable/consistList.
     */
    @Override
    public Consist addConsist(LocoAddress address) {
        if (! (address instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("address is not a DccLocoAddress object");
        }
        if (consistTable.containsKey(address)) { // no duplicates allowed across all connections
            return consistTable.get(address);
        }
        TmccConsist consist;
        consist = new TmccConsist((DccLocoAddress) address, adapterMemo,
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
     * Consist is activated on the controller for the specified LocoAddress
     * This is used by TmccThrottle to either publish an existing consist or clear
     * an old one upon opening the new throttle.
     * @param address Consist address to be activated
     */
    public void activateConsist(LocoAddress address) {

        ((TmccConsist)addConsist(address)).activate();
    }


    /**
     * If a consist exists with the given address, the consist is deactivated on the controller,
     * otherwise it does nothing.
     * This is used by a throttle in case it is controlling a consist.
     * @param address Consist address to be deactivated
     */
    public void deactivateConsist(LocoAddress address) {

        if (!consistTable.containsKey(address))
            return;

        ((TmccConsist)consistTable.get(address)).deactivate();
    }

    private final static Logger log = LoggerFactory.getLogger(TmccConsistManager.class);

}
