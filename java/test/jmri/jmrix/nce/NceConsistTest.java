package jmri.jmrix.nce;

import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistPreferencesManager;
import jmri.util.junit.annotations.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * NceConsistTest.java
 *
 * Test for the jmri.jmrix.nce.NceConsist class
 *
 * @author Paul Bender Copyright (C) 2016,2017
 * @author Ken Cameron Copyright (C) 2023
 */

public class NceConsistTest extends jmri.implementation.AbstractConsistTestBase {

    // infrastructure objects, populated by setUp;
    NceInterfaceScaffold nnis;
    NceSystemConnectionMemo memo;


    @Test public void testCtor2() {
        // DccLocoAddress constructor test.
        NceConsist c = new NceConsist(new DccLocoAddress(3, false),memo){
           @Override
           void killConsist(int address, boolean isLong){
           }
        };
        // send a reply the memory read instruction trigged by the constructor above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        Assert.assertNotNull(c);
        c.dispose();
    }

    @Override
    @Test public void checkSizeLimitAdvanced(){
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        Assert.assertEquals("Advanced Consist Limit",6,c.sizeLimit());
    }

    @Override
    @Test public void checkContainsAdvanced(){
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        // nothing added, should be false for all.
        Assert.assertFalse("Advanced Consist Contains",c.contains(A));
        Assert.assertFalse("Advanced Consist Contains",c.contains(B));
        // add just A
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertFalse("Advanced Consist Contains",c.contains(B));
        // then add B
        c.restore(B,false);
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        Assert.assertTrue("Advanced Consist Contains",c.contains(A));
        Assert.assertTrue("Advanced Consist Contains",c.contains(B));
    }

    @Override
    @Test public void checkGetLocoDirectionAdvanced(){
        c.setConsistType(jmri.Consist.ADVANCED_CONSIST);
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(200,true);
        jmri.DccLocoAddress B = new jmri.DccLocoAddress(250,true);
        c.restore(A,true); // use restore here, as it does not send
                           // any data to the command station
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        c.restore(B,false); // revese direction.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
        Assert.assertTrue("Direction in Advanced Consist",c.getLocoDirection(A));
        Assert.assertFalse("Direction in Advanced Consist",c.getLocoDirection(B));
    }

    @Test public void checkSetKnownDirection(){
        NceConsist nc = (NceConsist) c;
        jmri.DccLocoAddress A = new jmri.DccLocoAddress(1000,true);
        nc.restore(A,true,jmri.Consist.POSITION_LEAD); // as read from command station memory
        Assert.assertTrue("Direction before",nc.getLocoDirection(A));
        // the NCE Consist Editor reports it sent a reverse command
        nc.setKnownDirection(A,false);
        Assert.assertFalse("Direction after",nc.getLocoDirection(A));
        Assert.assertEquals("Position kept",jmri.Consist.POSITION_LEAD,nc.getPosition(A));
    }

    // Command station memory replies, 16 bytes: a loco in the first slot.
    // 1234 and 2345 are long addresses (top two bits set), 77 is short.
    private static final String LOCO_1234 = "C4 D2 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String LOCO_2345 = "C9 29 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String LOCO_77 = "00 4D 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String PLACEHOLDER = "3F FF 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String MID_77 = LOCO_77;
    private static final String EMPTY = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

    private static final DccLocoAddress L1234 = new DccLocoAddress(1234, true);
    private static final DccLocoAddress L2345 = new DccLocoAddress(2345, true);
    private static final DccLocoAddress L77 = new DccLocoAddress(77, false);

    private NceConsist newTestConsist(int number) {
        return new NceConsist(number, memo) {
            @Override
            void killConsist(int address, boolean isLong) {
            }
        };
    }

    // a 16 byte binary reply from space separated hex bytes
    private NceReply binaryReply(String hex) {
        NceReply r = new NceReply(nnis);
        String[] bytes = hex.split(" ");
        for (int i = 0; i < bytes.length; i++) {
            r.setElement(i, Integer.parseInt(bytes[i], 16));
        }
        return r;
    }

    // wait for the next command station memory read, then answer it
    private void answerRead(int readsBefore, String reply) {
        JUnitUtil.waitFor(() -> nnis.outbound.size() > readsBefore, "memory read " + (readsBefore + 1) + " sent");
        nnis.sendTestReplyToLastSender(binaryReply(reply));
    }

    // a consist read from the command station with lead 1234, rear 2345 and,
    // if withMid, mid 77
    private NceConsist readConsist(int number, boolean withMid) {
        int n = nnis.outbound.size();
        NceConsist nc = newTestConsist(number);
        answerRead(n, LOCO_1234);
        answerRead(n + 1, LOCO_2345);
        answerRead(n + 2, withMid ? MID_77 : EMPTY);
        JUnitUtil.waitFor(nc::isValid, "first read finished");
        Assert.assertTrue("lead read", nc.contains(L1234));
        Assert.assertTrue("rear read", nc.contains(L2345));
        Assert.assertEquals("mid read", withMid, nc.contains(L77));
        return nc;
    }

    @Test
    public void testKilledOnCommandStationIsEmptied() {
        NceConsist nc = readConsist(5, true);
        int n = nnis.outbound.size();
        nc.checkConsist();
        answerRead(n, EMPTY); // the consist was killed: no lead
        JUnitUtil.waitFor(nc::isValid, "check finished");
        Assert.assertTrue("all locos dropped", nc.getConsistList().isEmpty());
        Assert.assertEquals("no rear or mid read once the lead is gone", n + 1, nnis.outbound.size());
        nc.dispose();
    }

    @Test
    public void testMidLocoRemovedOnCommandStation() {
        NceConsist nc = readConsist(6, true);
        int n = nnis.outbound.size();
        nc.checkConsist();
        answerRead(n, LOCO_1234);
        answerRead(n + 1, LOCO_2345);
        answerRead(n + 2, EMPTY);
        JUnitUtil.waitFor(nc::isValid, "check finished");
        Assert.assertFalse("mid dropped", nc.contains(L77));
        Assert.assertTrue("lead kept", nc.contains(L1234));
        Assert.assertTrue("rear kept", nc.contains(L2345));
        nc.dispose();
    }

    @Test
    public void testReplaceOnCommandStation() {
        NceConsist nc = readConsist(7, false);
        // part way through a Replace: the lead slot holds the placeholder
        int n = nnis.outbound.size();
        nc.checkConsist();
        answerRead(n, PLACEHOLDER);
        answerRead(n + 1, LOCO_2345);
        answerRead(n + 2, EMPTY);
        JUnitUtil.waitFor(nc::isValid, "check finished");
        Assert.assertFalse("old lead dropped: the command station no longer has it", nc.contains(L1234));
        Assert.assertFalse("placeholder not added", nc.contains(new DccLocoAddress(0x3FFF, true)));
        Assert.assertTrue("rear kept", nc.contains(L2345));
        // Replace done: 77 is now the lead
        n = nnis.outbound.size();
        nc.checkConsist();
        answerRead(n, LOCO_77);
        answerRead(n + 1, LOCO_2345);
        answerRead(n + 2, EMPTY);
        JUnitUtil.waitFor(nc::isValid, "check finished");
        Assert.assertTrue("new lead", nc.contains(L77));
        Assert.assertEquals("new lead position", jmri.Consist.POSITION_LEAD, nc.getPosition(L77));
        Assert.assertFalse("old lead dropped", nc.contains(L1234));
        Assert.assertTrue("rear kept", nc.contains(L2345));
        nc.dispose();
    }

    @Test
    public void testCheckAskedForDuringReadRunsAfterIt() {
        int n = nnis.outbound.size();
        NceConsist nc = newTestConsist(8);
        JUnitUtil.waitFor(() -> nnis.outbound.size() > n, "first read sent");
        // the consist file restores a loco while the first read is running
        nc.restore(L1234, true);
        Assert.assertEquals("no second read while the first runs", n + 1, nnis.outbound.size());
        nnis.sendTestReplyToLastSender(binaryReply(EMPTY)); // command station has no such consist
        // the check follows the first read, and finds the file's loco stale
        answerRead(n + 1, EMPTY);
        JUnitUtil.waitFor(() -> nc.isValid() && nc.getConsistList().isEmpty(), "stale file loco dropped");
        Assert.assertEquals("one check read only", n + 2, nnis.outbound.size());
        nc.dispose();
    }

    @Test
    public void testFileLocoKeptWhenOnCommandStation() {
        int n = nnis.outbound.size();
        NceConsist nc = newTestConsist(9);
        JUnitUtil.waitFor(() -> nnis.outbound.size() > n, "first read sent");
        nc.restore(L1234, false); // from the consist file, reversed
        nc.restore(L2345, true);
        nnis.sendTestReplyToLastSender(binaryReply(LOCO_1234));
        answerRead(n + 1, LOCO_2345);
        answerRead(n + 2, EMPTY);
        // then the check asked for by the file restores
        answerRead(n + 3, LOCO_1234);
        answerRead(n + 4, LOCO_2345);
        answerRead(n + 5, EMPTY);
        JUnitUtil.waitFor(() -> nc.isValid() && nnis.outbound.size() == n + 6, "check finished");
        Assert.assertTrue("lead kept", nc.contains(L1234));
        Assert.assertTrue("rear kept", nc.contains(L2345));
        Assert.assertFalse("file direction kept", nc.getLocoDirection(L1234));
        nc.dispose();
    }

    @Test
    public void testDisposeSendsOnlyTheKill() {
        // deleting a consist (e.g. from the JSON server) kills it on the
        // command station once; it must not then try to remove each loco
        // too -- refused for the lead and rear, and an extra command for a mid
        int n = nnis.outbound.size();
        int[] kills = {0};
        NceConsist nc = new NceConsist(10, memo) {
            @Override
            void killConsist(int address, boolean isLong) {
                kills[0]++;
                Assert.assertEquals("killed by the lead's address", 1234, address);
            }
        };
        answerRead(n, LOCO_1234);
        answerRead(n + 1, LOCO_2345);
        answerRead(n + 2, MID_77);
        JUnitUtil.waitFor(nc::isValid, "first read finished");
        Assert.assertEquals("three locos read", 3, nc.getConsistList().size());
        int before = nnis.outbound.size();
        nc.dispose();
        Assert.assertEquals("one kill", 1, kills[0]);
        Assert.assertEquals("no remove commands after the kill", before, nnis.outbound.size());
    }

    private static final DccLocoAddress L99 = new DccLocoAddress(99, false);

    @Test
    public void testRearTakenOutWithReplaceAndNothingAdded() {
        // NCE Consist Editor: Replace on the rear, then no new loco added.
        // The rear slot keeps the dummy loco ("NONE"); the old rear is gone.
        NceConsist nc = readConsist(15, false);
        int n = nnis.outbound.size();
        nc.checkConsist();
        answerRead(n, LOCO_1234);
        answerRead(n + 1, PLACEHOLDER);
        answerRead(n + 2, EMPTY);
        JUnitUtil.waitFor(nc::isValid, "check finished");
        Assert.assertTrue("lead kept", nc.contains(L1234));
        Assert.assertFalse("old rear dropped", nc.contains(L2345));
        Assert.assertEquals("only the lead left", 1, nc.getConsistList().size());
        nc.dispose();
    }

    @Test
    public void testCheckLeftAloneWhenJmriChangesTheConsistDuringIt() {
        NceConsist nc = readConsist(11, true);
        int n = nnis.outbound.size();
        nc.checkConsist();
        JUnitUtil.waitFor(() -> nnis.outbound.size() > n, "check read sent");
        NceListener reader = nnis.lastSender();
        nc.add(L99, true); // JMRI adds a loco while the check is reading
        // the lead read answers from before the add (here: an empty slot)
        jmri.util.ThreadingUtil.runOnLayout(() -> nnis.sendTestReply(binaryReply(EMPTY), reader));
        JUnitUtil.waitFor(nc::isValid, "check finished");
        Assert.assertTrue("lead kept", nc.contains(L1234));
        Assert.assertTrue("rear kept", nc.contains(L2345));
        Assert.assertTrue("mid kept", nc.contains(L77));
        Assert.assertTrue("added loco kept", nc.contains(L99));
        Assert.assertEquals("one read and the add command, no rear or mid read", n + 2, nnis.outbound.size());
        nc.dispose();
    }

    @Test
    public void testCheckIfQuietSkipsAConsistJmriJustChanged() {
        NceConsist nc = readConsist(12, false);
        int n = nnis.outbound.size();
        nc.add(L99, true);
        Assert.assertEquals("add command sent", n + 1, nnis.outbound.size());
        Assert.assertFalse("no check right after a change", nc.checkIfQuiet());
        Assert.assertEquals("no read sent", n + 1, nnis.outbound.size());
        nc.dispose();
    }

    @Test
    public void testLocoFoundAgainKeepsItsDirection() {
        // a loco JMRI added reversed drops out of a check (a change still on
        // its way to the command station), then is found again: still reversed
        NceConsist nc = readConsist(17, false);
        nc.add(L99, false);
        int n = nnis.outbound.size();
        nc.checkConsist();
        answerRead(n, LOCO_1234);
        answerRead(n + 1, LOCO_2345);
        answerRead(n + 2, EMPTY);
        JUnitUtil.waitFor(nc::isValid, "first check finished");
        Assert.assertFalse("dropped", nc.contains(L99));
        n = nnis.outbound.size();
        nc.checkConsist();
        answerRead(n, LOCO_1234);
        answerRead(n + 1, LOCO_2345);
        answerRead(n + 2, "00 63 00 00 00 00 00 00 00 00 00 00 00 00 00 00"); // mid 99
        JUnitUtil.waitFor(nc::isValid, "second check finished");
        Assert.assertTrue("found again", nc.contains(L99));
        Assert.assertFalse("still reversed", nc.getLocoDirection(L99));
        nc.dispose();
    }

    @Test
    public void testBlockStarts() {
        java.util.List<Integer> numbers = java.util.Arrays.asList(84, 85, 90, 92, 100);
        Assert.assertEquals("8 lead slots a block", java.util.Arrays.asList(84, 92, 100), NceConsistManager.blockStarts(numbers, 8));
        Assert.assertEquals("2 consists' mids a block", java.util.Arrays.asList(84, 90, 92, 100), NceConsistManager.blockStarts(numbers, 2));
    }

    @Test
    public void testDiffersFromCommandStation() {
        NceConsist nc = readConsist(16, true); // lead 1234, rear 2345, mid 77
        int lead = NceConsistManager.locoAt(binaryReply(LOCO_1234), 0);
        int rear = NceConsistManager.locoAt(binaryReply(LOCO_2345), 0);
        int mid = NceConsistManager.locoAt(binaryReply(MID_77), 0);
        int none = NceConsistManager.locoAt(binaryReply(EMPTY), 0);
        Assert.assertEquals("placeholder is no loco", none, NceConsistManager.locoAt(binaryReply(PLACEHOLDER), 0));
        Assert.assertFalse("same", NceConsistManager.differs(nc, lead, rear, new int[]{mid, none, none, none}));
        Assert.assertTrue("mid gone", NceConsistManager.differs(nc, lead, rear, new int[]{none, none, none, none}));
        Assert.assertTrue("rear gone", NceConsistManager.differs(nc, lead, none, new int[]{mid, none, none, none}));
        Assert.assertTrue("killed", NceConsistManager.differs(nc, none, none, null));
        Assert.assertTrue("another lead", NceConsistManager.differs(nc, mid, rear, new int[]{mid, none, none, none}));
        nc.dispose();
    }

    // run one round of the background check on its own thread
    private Thread scanRound(NceConsistManager cm) {
        Thread t = new Thread(() -> {
            try {
                cm.scanRound(0);
            } catch (InterruptedException e) {
                // test ended
            }
        }, "test scan round");
        t.start();
        return t;
    }

    @Test
    public void testScanFindsARearTakenOffOnACab() {
        NceConsistManager cm = new NceConsistManager(memo);
        int n = nnis.outbound.size();
        NceConsist nc = (NceConsist) cm.addConsist(new DccLocoAddress(13, false));
        answerRead(n, LOCO_1234);
        answerRead(n + 1, LOCO_2345);
        answerRead(n + 2, EMPTY);
        JUnitUtil.waitFor(nc::isValid, "first read finished");
        n = nnis.outbound.size();
        Thread t = scanRound(cm);
        answerRead(n, LOCO_1234);     // lead slots of 13..20
        answerRead(n + 1, EMPTY);     // rear slots: 13's rear is gone
        // it differs, so 13 gets a full check
        answerRead(n + 2, LOCO_1234);
        answerRead(n + 3, EMPTY);
        answerRead(n + 4, EMPTY);
        JUnitUtil.waitFor(() -> !t.isAlive(), "scan round finished");
        Assert.assertTrue("lead kept", nc.contains(L1234));
        Assert.assertFalse("rear dropped", nc.contains(L2345));
        Assert.assertEquals("2 block reads and one consist check", n + 5, nnis.outbound.size());
        nc.dispose();
    }

    @Test
    public void testScanAddsAConsistBuiltOnACab() {
        NceConsistManager cm = new NceConsistManager(memo);
        int n = nnis.outbound.size();
        NceConsist nc = (NceConsist) cm.addConsist(new DccLocoAddress(13, false));
        answerRead(n, LOCO_1234);
        answerRead(n + 1, EMPTY);     // no rear, so no mid read
        JUnitUtil.waitFor(nc::isValid, "first read finished");
        n = nnis.outbound.size();
        Thread t = scanRound(cm);
        answerRead(n, "C4 D2 C9 29 00 00 00 00 00 00 00 00 00 00 00 00"); // leads: 13 = 1234, 14 = 2345
        answerRead(n + 1, EMPTY);     // no rears
        // 13 matches; 14 is new: read it
        answerRead(n + 2, LOCO_2345);
        answerRead(n + 3, EMPTY);
        JUnitUtil.waitFor(() -> !t.isAlive(), "scan round finished");
        Assert.assertTrue("consist 14 added", cm.getConsistList().contains(new DccLocoAddress(14, false)));
        Assert.assertTrue("with its lead", cm.getConsist(new DccLocoAddress(14, false)).contains(L2345));
        Assert.assertEquals("13 unchanged", 1, nc.getConsistList().size());
        nc.dispose();
    }

    @Override
    @Test
    @Disabled("Remove requires response from command station")
    @ToDo("re-write parent class test here and include simulated command station response")
    public void checkRemoveWithGetRosterIDAdvanced(){
    }

    @Override
    @Test
    @Disabled("Remove requires response from command station")
    @ToDo("re-write parent class test here and include simulated command station response") 
    public void checkAddRemoveWithRosterUpdateAdvanced(){
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        InstanceManager.setDefault(ConsistPreferencesManager.class,new ConsistPreferencesManager());
        // prepare an interface
        nnis = new NceInterfaceScaffold();
        memo = new NceSystemConnectionMemo();
        memo.setNceTrafficController(nnis);
        nnis.csm = new NceCmdStationMemory();

        c = new NceConsist(3,memo){
           @Override
           void killConsist(int address, boolean isLong){
           }
        };
        // the constructor above starts a memory read on its own thread; wait
        // for it, so later tests count only their own reads
        JUnitUtil.waitFor(() -> nnis.outbound.size() == 1, "first read of consist 3 sent");
        // send a reply the memory read instruction trigged by the constructor above.
        nnis.sendTestReply(new NceReply(nnis,"00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"),null);
    }

    @AfterEach
    @Override
    public void tearDown() {
        c.dispose();
        c = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
