package jmri.jmrix.bidib;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeListener;
import org.bidib.jbidibc.messages.AddressData;
import org.bidib.jbidibc.core.DefaultMessageListener;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.messages.enums.BoosterState;
import org.bidib.jbidibc.messages.enums.CommandStationState;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.enums.BoosterControl;
import org.bidib.jbidibc.messages.message.BoostOnMessage;
import org.bidib.jbidibc.messages.message.BoostOffMessage;
import org.bidib.jbidibc.messages.message.BoostQueryMessage;
import org.bidib.jbidibc.messages.message.CommandStationSetStateMessage;
import org.bidib.jbidibc.messages.utils.NodeUtils;
import org.bidib.jbidibc.messages.utils.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BiDiBPowerManager.java
 *
 * Description: PowerManager implementation for controlling layout power
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Eckart Meyer Copyright (C) 2019-2023
 *
 */
public class BiDiBPowerManager implements PowerManager {

    BiDiBTrafficController tc = null;
    String userName = "BiDiB";
    int power = UNKNOWN;
    MessageListener messageListener = null;

    public BiDiBPowerManager(BiDiBSystemConnectionMemo memo) {
        tc = memo.getBiDiBTrafficController();
        userName = memo.getUserName();
        createBoosterListener();
        // ask BiDiB for current booster status
        tc.sendBiDiBMessage(new BoostQueryMessage(), tc.getFirstBoosterNode());
    }

    @Override
    public String getUserName() {
        return userName;
    }


    @Override
    public void setPower(int v) throws JmriException {
        int old = power;
        power = UNKNOWN;
        checkTC();
        Node csnode = tc.getFirstCommandStationNode();
        if (v == ON) {
            // send TRACK_POWER_ON
            // At first MSG_BOOST_ON(0), then powering on the DCC generator: all booster will be switched on,
            // except those where the FEATURE_BST_INHIBIT_AUTOSTART is set.
            tc.sendBiDiBMessage(new BoostOnMessage(BoostOnMessage.BROADCAST_MESSAGE), tc.getRootNode());
            if (csnode != null) {
                tc.sendBiDiBMessage(new CommandStationSetStateMessage(CommandStationState.GO), tc.getFirstCommandStationNode());
            }

        } else if (v == OFF) {
            // send TRACK_POWER_OFF
            if (csnode != null) {
                tc.sendBiDiBMessage(new CommandStationSetStateMessage(CommandStationState.OFF), csnode);
            }
            tc.sendBiDiBMessage(new BoostOffMessage(BoostOffMessage.BROADCAST_MESSAGE), tc.getRootNode());
        }
        firePropertyChange(POWER, old, power);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPower() {
        return power;
    }

    /**
     * {@inheritDoc}
     * 
     * Remove the Message Listener for this power manager
     */
    @Override
    public void dispose() throws JmriException {
        if (messageListener != null) {
            tc.removeMessageListener(messageListener);        
            messageListener = null;
        }
        tc = null;
    }

    /**
     * @throws JmriException if we don't have a valid Traffic Controller
     */
    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use PowerManager after dispose");
        }
    }

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    private void createBoosterListener() {
        // to listen to messages related to track power.
        messageListener = new DefaultMessageListener() {
            @Override
            public void boosterState(byte[] address, int messageNum, BoosterState state, BoosterControl control) {//ByteUtils
                Node node = tc.getFirstBoosterNode();
                log.trace("booster state: msg addr: {}, booster: {}, state: {}, control: {}", ByteUtils.bytesToHex(address), node, state.getType(), control.getType());
                if (NodeUtils.isAddressEqual(node.getAddr(), address)) {
                    log.info("POWER booster state was signalled: {}, control: {}", state.getType(), control.getType());
                    int old = power;
                    power = BoosterState.isOnState(state) ? ON : OFF;
                    log.debug("change {} from {} to {}", POWER, old, power);
                    firePropertyChange(POWER, old, power);
                }
            }
            @Override
            public void speed(byte[] address, int messageNum, AddressData addressData, int speed) {
                //Node node = tc.getFirstCommandStationNode();
                //log.trace("speed: node UID: {}, node addr: {}, msg node addr: {}, address: {}, speed: {}", node.getUniqueId(), node.getAddr(), address, addressData, speed);
                //if (NodeUtils.isAddressEqual(node.getAddr(), address)) {
                    //log.debug("SPEED was signalled, node addr: {}, speed: {}, loco: {}", node.getAddr(), speed, addressData);
                //}
            }
        };
        tc.addMessageListener(messageListener);        
    }

    // Initialize logging information
    private final static Logger log = LoggerFactory.getLogger(BiDiBPowerManager.class);

}



