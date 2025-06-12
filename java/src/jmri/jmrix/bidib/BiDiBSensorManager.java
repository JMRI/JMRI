package jmri.jmrix.bidib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import jmri.JmriException;
import jmri.Sensor;

import org.bidib.jbidibc.messages.BidibLibrary;
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.message.FeedbackGetRangeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement SensorManager for BiDiB systems.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Eckart Meyer Copyright (C) 2019-2023
 */
public class BiDiBSensorManager extends jmri.managers.AbstractSensorManager {

    // Whether we accumulate partially loaded turnouts in pendingTurnouts.
    private boolean isLoading = false;
    // Turnouts that are being loaded from XML.
    private final ArrayList<BiDiBSensor> pendingSensors = new ArrayList<>();
    private final Map<Node, Integer> pendingNodeMinAddr = new HashMap<>();
    private final Map<Node, Integer> pendingNodeMaxAddr = new HashMap<>();

    public BiDiBSensorManager(BiDiBSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BiDiBSystemConnectionMemo getMemo() {
        return (BiDiBSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    // BiDiB-specific methods
    /**
     * {@inheritDoc}
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        log.trace("createNewSensor {} - {}", systemName, userName);
        //String addr = systemName.substring(getSystemPrefix().length() + 1);
        // first, check validity
        try {
            validateSystemNameFormat(systemName);
        } catch (IllegalArgumentException e) {
            log.error("Illegal address", e);
            throw e;
        }
        // OK, make
        BiDiBSensor s = new BiDiBSensor(systemName, this);
        s.setUserName(userName);

        synchronized (pendingSensors) {
            if (isLoading) {
                pendingSensors.add(s);
                if (s.getAddr().isFeedbackAddr()) {
                    // try to build minimum/maximum address to use bulk query later
                    BiDiBAddress a = s.getAddr();
                    Node node = a.getNode();
                    if (!pendingNodeMinAddr.containsKey(node)  ||  a.getAddr() < pendingNodeMinAddr.get(node)) {
                        pendingNodeMinAddr.put(node, a.getAddr());
                    }
                    if (!pendingNodeMaxAddr.containsKey(node)  ||  a.getAddr() > pendingNodeMaxAddr.get(node)) {
                        pendingNodeMaxAddr.put(node, a.getAddr());
                    }
                }
            } else {
                s.finishLoad();
            }
        }

        return s;
    }

    /**
     * This function is invoked before an XML load is started. We defer initialization of the
     * newly created turnouts until finishLoad because the feedback type might be changing as we
     * are parsing the XML.
     */
    public void startLoad() {
        log.debug("Sensor manager : start load");
        synchronized (pendingSensors) {
            isLoading = true;
        }
    }

    /**
     * This function is invoked after the XML load is complete and all Sensors are instantiated
     * and their type is read in. We use this hook to finalize the construction of the
     * objects whose instantiation was deferred until the feedback type was known.
     */
    public void finishLoad() {
        log.info("Sensor manager : finish load");
        synchronized (pendingSensors) {
            pendingSensors.forEach((s) -> {
                if (!s.getAddr().isFeedbackAddr()) {
                    // sensor is not a feedback (BiDiB BM), may be a port input
                    s.finishLoad();
                }
            });
            // now request feedbacks as bulk request from each node
            pendingNodeMinAddr.forEach((node, min) -> {
                    updateNodeFeedbacks(node, min, pendingNodeMaxAddr.get(node));
            });
            pendingNodeMinAddr.clear();
            pendingNodeMaxAddr.clear();
            pendingSensors.clear();
            isLoading = false;
        }
    }
    
    public void updateNodeFeedbacks(Node node) {
        updateNodeFeedbacks(node, 0, 128);
    }
    
    public void updateNodeFeedbacks(Node node, int min, int max) {
        BiDiBTrafficController tc = getMemo().getBiDiBTrafficController();
        int bmSize = tc.getNodeFeature(node, BidibLibrary.FEATURE_BM_SIZE);
        if (bmSize > 0) {
            int first = (min / 8) * 8;
            //int max = pendingNodeMaxAddr.get(node);
            if (max > (bmSize - 1)) {
                max = (bmSize - 1);
            }
            int end = ((max + 8) / 8) * 8; //exclusive end address
            log.debug("sensor finish load: node: {}, requesting feedback from {} to {}", node, first, end);
            tc.sendBiDiBMessage(new FeedbackGetRangeMessage(first, end), node);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        log.trace("createSystemName from {} - {}", curAddress, prefix);
        try {
            int i = 1;
            int curNum = Integer.parseInt(curAddress);
            for (Sensor s : getNamedBeanSet()) {
                //log.trace("turnout: {}/{} {}", i, curNum, s.getSystemName());
                if (i++ == curNum) {
                    return s.getSystemName();
                }
            }
        } catch (java.lang.NumberFormatException ex) {
            throw new JmriException("Hardware Address passed "+curAddress+" should be a number");
        }
//        // first, check validity
//        try {
//            validateAddressFormat(curAddress);
//        } catch (IllegalArgumentException e) {
//            throw new JmriException(e.toString());
//        }
//        // getSystemPrefix() unsigned int with "+" as service to user
//        String newAddress = CbusAddress.validateSysName(curAddress);
//        return prefix + typeLetter() + newAddress;
        return prefix + typeLetter() + curAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        log.trace("validateSystemNameFormat: name: {}, typeLetter: {}", name, typeLetter());
        validateSystemNamePrefix(name, locale);
        //validateAddressFormat(name.substring(getSystemNamePrefix().length()));
        if (!BiDiBAddress.isValidSystemNameFormat(name, typeLetter(), getMemo())) {
            throw new jmri.NamedBean.BadSystemNameException(Locale.getDefault(), "InvalidSystemName",name);
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        log.trace("validSystemNameFormat: systemNname: {}", systemName);
        
        if (systemName.length() <= getSystemPrefix().length()) {
            return NameValidity.INVALID;
        }
        
//        try {
//            validateAddressFormat(addr);
//        } catch (IllegalArgumentException e) {
//            return NameValidity.INVALID;
//        }
        return NameValidity.VALID;
    }

    /**
     * Work out the details for BiDiB hardware address validation. Logging of
     * handled cases no higher than WARN.
     *
     * @param address the hardware address to check
     * @throws IllegalArgumentException when delimiter is not found
     */
    //TODO!
//    void validateAddressFormat(String address) throws IllegalArgumentException {
//        String newAddress = CbusAddress.validateSysName(address);
//        log.debug("validated system name {}", newAddress);
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    /*
     * {@inheritDoc} Send a query message to get all sensors.
     */
/* NOT USED
    @Override
    public void updateAll() {
        BiDiBTrafficController tc = getMemo().getBiDiBTrafficController();
        BidibRequestFactory rf = tc.getBidib().getRootNode().getRequestFactory();
        tc.getNodeList().forEach( (uid, node) -> {
            int bmSize = tc.getNodeFeature(node, BidibLibrary.FEATURE_BM_SIZE);
            if (NodeUtils.hasFeedbackFunctions(node.getUniqueId())  &&  bmSize > 0 ) {
                log.info("Requesting feedback status on node {}", node);
//                tc.sendBiDiBMessage(new FeedbackGetRangeMessage(0, 128), node);
                tc.sendBiDiBMessage(new FeedbackGetRangeMessage(0, bmSize), node);
            }
            Feature f = tc.findNodeFeature(node, BidibLibrary.FEATURE_CTRL_INPUT_COUNT);
            if (NodeUtils.hasSwitchFunctions(node.getUniqueId())  &&  (f == null  ||  f.getValue() > 0) ) {
                log.info("Requesting input port status on node {}", node);
                if (node.getProtocolVersion().isHigherThan(ProtocolVersion.VERSION_0_6)) {
                    // fast bulk query of all ports (new in bidib protocol version 0.7)
                    BidibCommandMessage m = (BidibCommandMessage)rf.createPortQueryAll(1 << BidibLibrary.BIDIB_PORTTYPE_INPUT, 0x0000, 0xFFFF);
                    tc.sendBiDiBMessage(m, node);
                }
                else {
                    // old version - request every single sensor
                    getNamedBeanSet().forEach((nb) -> {
                        if (nb instanceof BiDiBSensor) {
                            BiDiBAddress addr = new BiDiBAddress(((BiDiBSensor) nb).getSystemName(), typeLetter(), getMemo());
                            if (addr.isValid()  &&  addr.isPortAddr()  &&  addr.getNode().equals(node)) {
                                BidibCommandMessage m = (BidibCommandMessage)rf.createLcPortQuery(tc.getPortModel(node), LcOutputType.INPUTPORT, addr.getAddr());
                                log.trace("...from port {}", addr.getAddr());
                                tc.sendBiDiBMessage(m, node);
                            }
                        }
                    });
                }
            }
        });
//        getNamedBeanSet().forEach((nb) -> {
//            if (nb instanceof CbusSensor) {
//                nb.requestUpdateFromLayout();
//            }
//        });
    }
*/
    

    private final static Logger log = LoggerFactory.getLogger(BiDiBSensorManager.class);

}
