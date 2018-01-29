package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.ThrottleListener;
import jmri.DccThrottle;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LnThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private DccThrottle throttle;
    
    boolean failedThrottleRequest = false;
    
    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",tm);
    }

    @Test
    public void testCreateLnThrottleRunAndRelease() {
        ThrottleListener throtListen = new ThrottleListener() {
            @Override
            public void notifyThrottleFound(DccThrottle t) {
                throttle = t;
                log.error("created a throttle");
            }

            @Override
            public void notifyFailedThrottleRequest(jmri.LocoAddress address, String reason) {
                log.error("Throttle request failed for " + address + " because " + reason);
                failedThrottleRequest = true;
            }

            @Override
            public void notifyStealThrottleRequired(jmri.LocoAddress address){
                // this is an automatically stealing impelementation.
                InstanceManager.throttleManagerInstance().stealThrottleRequest(address, this, true);
            }
        };
        int oldOutBoundSize = lnis.outbound.size();
        log.warn("oldOutBoundSize = {}", oldOutBoundSize);        

        tm.requestThrottle(1203, throtListen);
        oldOutBoundSize = lnis.outbound.size();
        log.warn("oldOutBoundSize = {}", oldOutBoundSize);        

        Assert.assertEquals("address request message",
                "BF 09 33 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        oldOutBoundSize = lnis.outbound.size();
        log.warn("oldOutBoundSize = {}", oldOutBoundSize);        

        LocoNetMessage cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x00, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x53});
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("null move",
                "BA 11 11 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        oldOutBoundSize = lnis.outbound.size();
        log.warn("oldOutBoundSize = {}", oldOutBoundSize);        

        cmdStationReply = new LocoNetMessage(new int[] {
                0xe7, 0x0e, 0x11, 0x30, 0x33, 0x0, 0x0, 0x7, 0x0, 0x09, 0x0, 0x0, 0x0, 0x00});
        lnis.sendTestMessage(cmdStationReply);
        Assert.assertEquals("write Throttle ID",
                "EF 0E 11 30 33 00 00 07 00 09 00 71 02 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        
        oldOutBoundSize = lnis.outbound.size();
        log.warn("oldOutBoundSize = {}", oldOutBoundSize);        
        
        cmdStationReply = new LocoNetMessage(new int[] {
                0xb4, 0x6f, 0x7f, 0x5B});
        lnis.sendTestMessage(cmdStationReply);

        log.warn("oldOutBoundSize = {}", oldOutBoundSize);        
        Assert.assertEquals("write Throttle ID",
                "B4 6F 7F 5B",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        
        Assert.assertNotNull("have created a throttle", throttle);
    }
    
    LocoNetInterfaceScaffold lnis;
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnis = new LocoNetInterfaceScaffold();
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(LnCommandStationType.COMMAND_STATION_DCS100,false,false);
        memo.configureManagers();
        tm = new LnThrottleManager(memo);
    }
    
    @After
    public void tearDown() {
        ((LnThrottleManager)tm).dispose();
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LnThrottleManagerTest.class);

}
