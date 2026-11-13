package jmri.implementation;

import jmri.AddressedProgrammerManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.mockito.Mockito;

/**
 * Test simple functioning of DccConsist
 *
 * @author Paul Copyright (C) 2011, 2016
 */
public class DccConsistTest extends AbstractConsistTestBase {

    @Test
    public void testCtor2() {
        // integer constructor test.
        DccConsist c = new DccConsist(12);
        Assert.assertNotNull(c);
    }

    @Test
    public void testCtor3() {
        // integer constructor test.
        DccConsist c = new DccConsist(new DccLocoAddress(12, true));
        Assert.assertNotNull(c);
    }

    /**
     * @param status what the programmer reports for each CV19 write
     * @return replies the consist sent its listeners: address, then status
     */
    private java.util.List<Object> addWithCv19Reply(int status, boolean directionNormal) throws jmri.ProgrammerException {
        jmri.AddressedProgrammer programmer = Mockito.mock(jmri.AddressedProgrammer.class);
        Mockito.doAnswer(inv -> {
            ((jmri.ProgListener) inv.getArgument(2)).programmingOpReply(0, status);
            return null;
        }).when(programmer).writeCV(Mockito.anyString(), Mockito.anyInt(), Mockito.any(jmri.ProgListener.class));
        AddressedProgrammerManager pm = Mockito.mock(AddressedProgrammerManager.class);
        Mockito.when(pm.getAddressedProgrammer(Mockito.anyBoolean(), Mockito.anyInt())).thenReturn(programmer);
        DccConsist dc = new DccConsist(new DccLocoAddress(12, true), pm);
        java.util.List<Object> replies = new java.util.ArrayList<>();
        dc.addConsistListener((address, s) -> {
            replies.add(address);
            replies.add(s);
        });
        dc.add(new DccLocoAddress(1234, true), directionNormal);
        return replies;
    }

    @Test
    public void testFailedCv19WriteReportedAsError() throws jmri.ProgrammerException {
        java.util.List<Object> replies = addWithCv19Reply(jmri.ProgListener.NoLocoDetected, true);
        Assert.assertEquals(2, replies.size());
        Assert.assertEquals("for the loco being added", new DccLocoAddress(1234, true), replies.get(0));
        Assert.assertEquals("the write failed: not reported as a success",
                jmri.ConsistListener.CONSIST_ERROR, replies.get(1));
    }

    @Test
    public void testCv19WriteReportedAsSuccess() throws jmri.ProgrammerException {
        java.util.List<Object> replies = addWithCv19Reply(jmri.ProgListener.OK, true);
        Assert.assertEquals(java.util.List.of(new DccLocoAddress(1234, true), jmri.ConsistListener.OPERATION_SUCCESS),
                replies);
        replies = addWithCv19Reply(jmri.ProgListener.OK, false); // facing the other way
        Assert.assertEquals(java.util.List.of(new DccLocoAddress(1234, true), jmri.ConsistListener.OPERATION_SUCCESS),
                replies);
    }

    @Test
    public void testProgrammingOpReplyPassesOnTheStatus() {
        java.util.List<Integer> statuses = new java.util.ArrayList<>();
        DccConsist dc = (DccConsist) c;
        dc.addConsistListener((address, s) -> statuses.add(s));
        dc.programmingOpReply(0, jmri.ProgListener.OK);
        dc.programmingOpReply(0, jmri.ProgListener.ProgrammerBusy);
        Assert.assertEquals(java.util.List.of(jmri.ConsistListener.OPERATION_SUCCESS, jmri.ConsistListener.CONSIST_ERROR),
                statuses);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ConsistPreferencesManager.class, new ConsistPreferencesManager());
        JUnitUtil.initDebugProgrammerManager();
        c = new DccConsist(new DccLocoAddress(12, true), InstanceManager.getDefault(AddressedProgrammerManager.class));
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
        c = null;
    }

}
