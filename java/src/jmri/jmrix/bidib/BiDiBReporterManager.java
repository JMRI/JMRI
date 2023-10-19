package jmri.jmrix.bidib;

import java.util.Locale;
import java.util.Map;

import jmri.Reporter;
import org.bidib.jbidibc.messages.BidibLibrary;

import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.message.FeedbackGetAddressRangeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BiDiBReporterManager implements the ReporterManager for BiDiB
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Eckart Meyer Copyright (C) 2019-2023
 */
public class BiDiBReporterManager extends jmri.managers.AbstractReporterManager {

    // ctor has to register for LocoNet events
    public BiDiBReporterManager(BiDiBSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BiDiBSystemConnectionMemo getMemo() {
        return (BiDiBSystemConnectionMemo) memo;
    }

//    @Override
//    public void dispose() {
//        super.dispose();
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reporter createNewReporter(String systemName, String userName) {
        Reporter r = new BiDiBReporter(systemName, this);
        r.setUserName(userName);
        register(r);
        return r;
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

    public void updateNode(Node node) {
        BiDiBTrafficController tc = getMemo().getBiDiBTrafficController();
        if ( tc.getNodeFeature(node, BidibLibrary.FEATURE_BM_ADDR_DETECT_ON) > 0) {
            log.trace("node can detect addresse: {}", node);
            log.info("Requesting all adresses");
            tc.sendBiDiBMessage(new FeedbackGetAddressRangeMessage(0, 128), node);
            log.info("Requesting adress of global detector");
            tc.sendBiDiBMessage(new FeedbackGetAddressRangeMessage(255, 0), node);
        }
    }
    
    /**
     * Get all loco addresses from railcom
     */
    public void updateAll() {
        BiDiBTrafficController tc = getMemo().getBiDiBTrafficController();
        Map<Long,Node> nodeList = tc.getNodeList();
        nodeList.forEach((uid, node) -> {
            updateNode(node);
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddReporterEntryToolTip");
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(BiDiBReporterManager.class);
}
