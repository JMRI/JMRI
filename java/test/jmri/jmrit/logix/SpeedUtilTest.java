package jmri.jmrit.logix;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.Assume;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SpeedUtilTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedUtil t = new SpeedUtil();
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @Test
    public void testMakeRamp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedUtil su = new SpeedUtil();
        assertThat(su).withFailMessage("exists").isNotNull();
        RampData ramp = su.getRampForSpeedChange(.1f, .8f);
        assertThat(ramp).withFailMessage("exists").isNotNull();
        assertThat(ramp.isUpRamp()).withFailMessage("upRamp").isTrue();
    }

    @Test
    public void testMomentumTimePerStep() {
        float steps126 = 1.0f / 126.0f;
        float steps28 = 1.0f / 28.0f;
        // NMRA example: CV#3 = 2 with 28 speed steps is 0.064 s per step
        assertThat(SpeedUtil.getMomentumTimePerStep(2, steps28)).isEqualTo(64);
        // no momentum
        assertThat(SpeedUtil.getMomentumTimePerStep(0, steps126)).isZero();
        // CV#4 = 20 with 126 steps: 20 * 896 / 126 = 142.2 ms
        assertThat(SpeedUtil.getMomentumTimePerStep(20, steps126)).isEqualTo(142);
        // CV#3 = 255 with 28 steps: 255 * 896 / 28 = 8160 ms
        assertThat(SpeedUtil.getMomentumTimePerStep(255, steps28)).isEqualTo(8160);
    }

    @Test
    public void testMomentumFromRosterCVs() throws IOException {
        // CV23 and CV24 adjust momentum for a decoder in a consist; a warrant
        // drives the locomotive on its own address, so they are not used
        RosterEntry re = makeRosterEntry("momentumTest",
                "<CVvalue name=\"3\" value=\"20\"/>"
                + "<CVvalue name=\"4\" value=\"40\"/>"
                + "<CVvalue name=\"23\" value=\"10\"/>"
                + "<CVvalue name=\"24\" value=\"138\"/>");
        SpeedUtil su = new SpeedUtil();
        assertThat(su.setRosterId(re.getId())).withFailMessage("roster entry found").isTrue();
        su.getMergeProfile();   // reads the momentum CVs
        float incr = su.getThrottleSpeedStepIncrement();

        float accelerate = su.getMomentumTime(0.0f, incr);
        float decelerate = su.getMomentumTime(incr, 0.0f);
        assertThat(accelerate).isEqualTo(SpeedUtil.getMomentumTimePerStep(20, incr), within(0.5f));
        assertThat(decelerate).isEqualTo(SpeedUtil.getMomentumTimePerStep(40, incr), within(0.5f));
    }

    @Test
    public void testNoMomentumCVs() throws IOException {
        RosterEntry re = makeRosterEntry("noMomentum", "<CVvalue name=\"1\" value=\"3\"/>");
        SpeedUtil su = new SpeedUtil();
        assertThat(su.setRosterId(re.getId())).withFailMessage("roster entry found").isTrue();
        su.getMergeProfile();
        float incr = su.getThrottleSpeedStepIncrement();
        // with no momentum CVs, getMomentumTime allows its minimum of 2 ms per step
        assertThat(su.getMomentumTime(0.0f, incr)).isEqualTo(2.0f, within(0.01f));
        assertThat(su.getMomentumTime(incr, 0.0f)).isEqualTo(2.0f, within(0.01f));
    }

    private static org.assertj.core.data.Offset<Float> within(float offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }

    // write a roster file with the given CV values and add its entry to the roster
    private RosterEntry makeRosterEntry(String id, String cvValues) throws IOException {
        String fileName = id + ".xml";
        File dir = new File(Roster.getDefault().getRosterFilesLocation());
        FileUtil.createDirectory(dir);
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<locomotive-config>\n"
                + "  <locomotive id=\"" + id + "\" fileName=\"" + fileName + "\" dccAddress=\"3\">\n"
                + "    <values>" + cvValues + "</values>\n"
                + "  </locomotive>\n"
                + "</locomotive-config>\n";
        Files.write(new File(dir, fileName).toPath(), xml.getBytes(StandardCharsets.UTF_8));
        RosterEntry re = new RosterEntry(fileName);
        re.setId(id);
        re.setDccAddress("3");
        Roster.getDefault().addEntry(re);
        return re;
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initWarrantManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(SpeedUtilTest.class);

}
