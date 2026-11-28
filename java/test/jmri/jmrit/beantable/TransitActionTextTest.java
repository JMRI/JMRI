package jmri.jmrit.beantable;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.TransitSectionAction;
import jmri.util.FileUtil;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for TransitActionText.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TransitActionTextTest {

    private static TransitSectionAction act(int when, int what, int dataWhen, int d1, int d2,
            String sWhen, String sWhat, String sWhat2) {
        return new TransitSectionAction(when, what, dataWhen, d1, d2, sWhen, sWhat, sWhat2);
    }

    @Test
    public void testEveryWhenIsWorded() {
        for (int when = 1; when <= TransitSectionAction.NUM_WHENS; when++) {
            String text = TransitActionText.whenText(act(when, TransitSectionAction.PAUSE, 0, 1, 0, "X", "", ""));
            assertFalse(text.isEmpty(), "when " + when);
        }
    }

    @Test
    public void testEveryWhatIsWorded() {
        for (int what = 1; what <= TransitSectionAction.NUM_WHATS; what++) {
            String text = TransitActionText.whatText(act(TransitSectionAction.ENTRY, what, 0, 1, 0, "", "X", "Y"));
            assertFalse(text.isEmpty(), "what " + what);
            assertNotEquals("WHAT", text, "what " + what);
        }
    }

    @Test
    public void testWordingWithValues() {
        assertEquals("On Entry to this Section", TransitActionText.whenText(
                act(TransitSectionAction.ENTRY, TransitSectionAction.PAUSE, 0, 1, 0, "", "", "")));
        assertEquals("\"500\" mSec after entering Block \"B1\"", TransitActionText.whenText(
                act(TransitSectionAction.BLOCKENTRY, TransitSectionAction.PAUSE, 500, 1, 0, "B1", "", "")));
        assertEquals("When Sensor \"S1\" becomes ACTIVE", TransitActionText.whenText(
                act(TransitSectionAction.SENSORACTIVE, TransitSectionAction.PAUSE, 0, 1, 0, "S1", "", "")));
        assertEquals("Set Sensor \"S2\" to INACTIVE", TransitActionText.whatText(
                act(TransitSectionAction.ENTRY, TransitSectionAction.SETSENSORINACTIVE, 0, 0, 0, "", "S2", "")));
        assertEquals("Load TrainInfo \"T.xml\" with Current Number.", TransitActionText.whatText(
                act(TransitSectionAction.ENTRY, TransitSectionAction.LOADTRAININFO, 0, 0,
                        TransitSectionAction.LOCOADDRESSTYPECURRENT, "", "T.xml", "")));
    }

    @Test
    public void testUnknownCodesAreNotFatal() {
        assertEquals("", TransitActionText.whenText(act(99, TransitSectionAction.PAUSE, 0, 0, 0, "", "", "")));
        JUnitAppender.assertWarnMessage("Unhandled transit section action When code: 99");
        assertEquals("WHAT", TransitActionText.whatText(act(TransitSectionAction.ENTRY, 99, 0, 0, 0, "", "", "")));
        JUnitAppender.assertWarnMessage("Unhandled transit section action What code: 99");
    }

    @Test
    public void testNoProblemsWhenNamesExist() throws IOException {
        InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        InstanceManager.getDefault(jmri.BlockManager.class).provideBlock("IB1");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(
                new jmri.implementation.VirtualSignalHead("IH1"));
        File dir = new File(FileUtil.getUserFilesPath(), "dispatcher/traininfo");
        FileUtil.createDirectory(dir);
        Files.write(new File(dir, "T.xml").toPath(), "<x/>".getBytes());

        assertTrue(TransitActionText.problems(act(TransitSectionAction.SENSORACTIVE,
                TransitSectionAction.SETSENSORACTIVE, 0, 0, 0, "IS1", "IS1", "")).isEmpty());
        assertTrue(TransitActionText.problems(act(TransitSectionAction.BLOCKEXIT,
                TransitSectionAction.HOLDSIGNAL, 0, 0, 0, "IB1", "IH1", "")).isEmpty());
        assertTrue(TransitActionText.problems(act(TransitSectionAction.ENTRY,
                TransitSectionAction.LOADTRAININFO, 0, 0, TransitSectionAction.LOCOADDRESSTYPENUMBER,
                "", "T.xml", "3")).isEmpty());
        assertTrue(TransitActionText.problems(act(TransitSectionAction.ENTRY,
                TransitSectionAction.TOMANUALMODE, 0, 0, 0, "", "", "")).isEmpty(), "no done Sensor is fine");
    }

    @Test
    public void testProblemsWhenNamesAreMissing() {
        List<String> p = TransitActionText.problems(act(TransitSectionAction.SENSORINACTIVE,
                TransitSectionAction.SETSENSORACTIVE, 0, 0, 0, "Gone1", "Gone2", ""));
        assertEquals(List.of("Sensor \"Gone1\" not found", "Sensor \"Gone2\" not found"), p);

        assertEquals(List.of("Block \"NoBlock\" not found"), TransitActionText.problems(act(
                TransitSectionAction.BLOCKENTRY, TransitSectionAction.PAUSE, 0, 1, 0, "NoBlock", "", "")));
        assertEquals(List.of("Signal Head or Mast \"NoSignal\" not found"), TransitActionText.problems(act(
                TransitSectionAction.ENTRY, TransitSectionAction.RELEASESIGNAL, 0, 0, 0, "", "NoSignal", "")));
        assertEquals(List.of("Sensor \"NoDone\" not found"), TransitActionText.problems(act(
                TransitSectionAction.ENTRY, TransitSectionAction.TOMANUALMODE, 0, 0, 0, "", "NoDone", "")));
        assertEquals(List.of("TrainInfo file \"None.xml\" not found", "Roster entry \"NoLoco\" not found"),
                TransitActionText.problems(act(TransitSectionAction.ENTRY, TransitSectionAction.LOADTRAININFO,
                        0, 0, TransitSectionAction.LOCOADDRESSTYPEROSTER, "", "None.xml", "NoLoco")));
    }

    @Test
    public void testBlockNotInItsSection() {
        jmri.Block inside = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("IB1", "Inside");
        InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("IB2", "Outside");
        jmri.Section s = InstanceManager.getDefault(jmri.SectionManager.class).createNewSection("Sec");
        s.addBlock(inside);
        jmri.TransitSection ts = new jmri.TransitSection(s, 1, jmri.Section.FORWARD);
        assertTrue(TransitActionText.problems(ts, act(TransitSectionAction.BLOCKENTRY,
                TransitSectionAction.PAUSE, 0, 1, 0, "Inside", "", "")).isEmpty());
        assertEquals(List.of("Block \"Outside\" is not in Section \"Sec\""), TransitActionText.problems(ts,
                act(TransitSectionAction.BLOCKEXIT, TransitSectionAction.PAUSE, 0, 1, 0, "Outside", "", "")));
    }

    @Test
    public void testDelayStartActionNeedsADelayStart() {
        jmri.Section s = InstanceManager.getDefault(jmri.SectionManager.class).createNewSection("Sec");
        jmri.TransitSection ts = new jmri.TransitSection(s, 1, jmri.Section.FORWARD);
        TransitSectionAction horn = act(TransitSectionAction.PRESTARTACTION, TransitSectionAction.SOUNDHORN,
                1000, 500, 0, "", "", "");
        ts.addAction(horn);
        assertEquals(List.of("No Delay Start action in this Section to start it"), TransitActionText.problems(ts, horn));
        ts.addAction(act(TransitSectionAction.PRESTARTDELAY, TransitSectionAction.PRESTARTRESUME,
                5000, 0, 0, "", "", ""));
        assertTrue(TransitActionText.problems(ts, horn).isEmpty());
    }

    @Test
    public void testCopyKeepsWhenAndWhat() {
        TransitSectionAction a = act(TransitSectionAction.BLOCKENTRY, TransitSectionAction.LOADTRAININFO,
                300, 3, TransitSectionAction.LOCOADDRESSTYPEROSTER, "IB1", "T.xml", "Loco");
        TransitSectionAction c = TransitActionText.copy(a);
        assertNotSame(a, c);
        assertEquals(TransitActionText.whenText(a), TransitActionText.whenText(c));
        assertEquals(TransitActionText.whatText(a), TransitActionText.whatText(c));
        assertEquals(3, c.getDataWhat1());
    }

    @Test
    public void testReplaceName() {
        TransitSectionAction a = act(TransitSectionAction.SENSORACTIVE, TransitSectionAction.SETSENSORACTIVE,
                0, 0, 0, "S1", "S1", "");
        assertTrue(TransitActionText.replaceName(a, "S1", "S9"));
        assertEquals("S9", a.getStringWhen());
        assertEquals("S9", a.getStringWhat());

        TransitSectionAction partial = act(TransitSectionAction.SENSORACTIVE, TransitSectionAction.PAUSE,
                0, 1, 0, "S10", "", "");
        assertFalse(TransitActionText.replaceName(partial, "S1", "S9"), "only whole names");
        assertEquals("S10", partial.getStringWhen());

        TransitSectionAction roster = act(TransitSectionAction.ENTRY, TransitSectionAction.LOADTRAININFO,
                0, 0, TransitSectionAction.LOCOADDRESSTYPEROSTER, "", "T.xml", "Loco");
        assertTrue(TransitActionText.replaceName(roster, "Loco", "Loco2"));
        assertEquals("Loco2", roster.getStringWhat2());

        TransitSectionAction pause = act(TransitSectionAction.ENTRY, TransitSectionAction.PAUSE,
                0, 1, 0, "", "S1", "");
        assertFalse(TransitActionText.replaceName(pause, "S1", "S9"), "a Pause names nothing");
    }

    @Test
    public void testNamesShownAsDisplayNames() {
        jmri.Sensor platform = InstanceManager.getDefault(SensorManager.class).provideSensor("IS5");
        platform.setUserName("Platform 1");
        InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock("IB7", "Station");
        assertEquals("When Sensor \"Platform 1\" becomes ACTIVE", TransitActionText.whenText(
                act(TransitSectionAction.SENSORACTIVE, TransitSectionAction.PAUSE, 0, 1, 0, "IS5", "", "")),
                "a Sensor stored by its system name is shown by its user name");
        assertEquals("On Entry to Block \"Station\"", TransitActionText.whenText(
                act(TransitSectionAction.BLOCKENTRY, TransitSectionAction.PAUSE, 0, 1, 0, "IB7", "", "")));
        assertEquals("Set Sensor \"Platform 1\" to ACTIVE", TransitActionText.whatText(
                act(TransitSectionAction.ENTRY, TransitSectionAction.SETSENSORACTIVE, 0, 0, 0, "", "IS5", "")));
        assertEquals("Set Sensor \"Gone\" to ACTIVE", TransitActionText.whatText(
                act(TransitSectionAction.ENTRY, TransitSectionAction.SETSENSORACTIVE, 0, 0, 0, "", "Gone", "")),
                "a name not found is shown as stored");
    }

    @Test
    public void testReplaceNameByAnotherNameOfTheSameBean() {
        jmri.Sensor platform = InstanceManager.getDefault(SensorManager.class).provideSensor("IS5");
        platform.setUserName("Platform 1");
        TransitSectionAction a = act(TransitSectionAction.SENSORACTIVE, TransitSectionAction.SETSENSORACTIVE,
                0, 0, 0, "IS5", "Platform 1", "");
        assertTrue(TransitActionText.replaceName(a, "Platform 1", "IS9"), "the user name finds a stored system name");
        assertEquals("IS9", a.getStringWhen());
        assertEquals("IS9", a.getStringWhat());
        assertFalse(TransitActionText.replaceName(a, "Platform 1", "IS8"), "no longer uses it");
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSectionManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
