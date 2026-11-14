package jmri.jmrix.can.cbus.simulator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jmri.Block;
import jmri.DccThrottle;
import jmri.IdTag;
import jmri.JmriException;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.Sensor;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.SimulationTruth;
import jmri.jmrit.simulation.VirtualTrain;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Simulated trains' ID tags read by CBUS reporters on a CAN Simulation
 * connection, as a classic MERG RFID reader sends them. The frames are
 * checked against the CBUS specification's accessory data event (ACDAT,
 * opcode 0xF6: the reader's number, two bytes, then five data bytes), worked
 * out here, and the tags against the classic RFID naming, ten hex digits.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusSimulatedReportersTest {

    private static final int ACDAT = 0xF6; // from the CBUS specification, not JMRI's constants

    @Test
    public void testTrainsTagReadOnCbus() throws JmriException {
        Block[] b = SimulationTestUtil.line(500, "A", "B", "C");
        String[] names = {"A", "B", "C"};
        for (int i = 0; i < b.length; i++) {
            Sensor s = CbusSimulationTestUtil.sensor(memo, "+N1E" + (i + 1));
            String name = names[i];
            ThreadingUtil.runOnLayoutWithJmriException(() -> {
                s.setUserName(name);
                s.setKnownState(Sensor.INACTIVE);
            });
            b[i].setSensor(s.getSystemName());
        }
        Reporter[] r = new Reporter[1];
        ThreadingUtil.runOnLayout(() -> r[0] = memo.get(ReporterManager.class).provideReporter("MR258"));
        b[2].setReporter(r[0]);
        b[2].setReportingCurrent(true);

        List<int[]> frames = new CopyOnWriteArrayList<>();
        CanListener heard = new CanListener() {
            @Override
            public void message(CanMessage m) {
                // JMRI's own frames
            }

            @Override
            public void reply(CanReply f) {
                if (f.getElement(0) == ACDAT) {
                    int[] d = new int[f.getNumDataElements()];
                    for (int i = 0; i < d.length; i++) {
                        d[i] = f.getElement(i);
                    }
                    frames.add(d);
                }
            }
        };
        memo.getTrafficController().addCanListener(heard);
        VirtualTrain t = VirtualTrain.builder("T")
                .loco(jmri.jmrit.simulation.SimulatedLoco.builder(SimulationTestUtil.address(3)).length(100)
                        .fullSpeed(400).build())
                .length(100).idTag("0A1B2C3D4E").build();
        SimulationTestUtil.place(t, b[0], null, 150);
        DccThrottle th = CbusSimulationTestUtil.throttle(memo, SimulationTestUtil.address(3));
        ThreadingUtil.runOnLayout(() -> {
            th.setIsForward(true);
            th.setSpeedSetting(0.5f);
        });
        SimulationTestUtil.waitForFrontIn(t, b[2]);
        ThreadingUtil.runOnLayout(() -> th.setSpeedSetting(0f));
        SimulationTestUtil.waitForTruth(tr -> {
            SimulationTruth.TrainTruth tt = tr.getTrain(t);
            return tt != null && tt.getSpeedMmPerSecond() == 0;
        }, "stopped");
        JUnitUtil.waitFor(() -> r[0].getCurrentReport() != null, "C's reader read the tag");
        Assertions.assertEquals(1, frames.size(), "one read, by C's reader only (A and B have none)");
        Assertions.assertArrayEquals(new int[]{ACDAT, 0x01, 0x02, 0x0A, 0x1B, 0x2C, 0x3D, 0x4E}, frames.get(0),
                "reader 258 is 0x0102; the tag's five bytes, high first");
        Object report = r[0].getCurrentReport();
        Assertions.assertTrue(report instanceof IdTag, "an ID tag: " + report);
        Assertions.assertEquals("0A1B2C3D4E", ((IdTag) report).getTagID(), "named as classic RFID tags are");
        JUnitUtil.waitFor(() -> b[2].getValue() == report, "C's value is the tag it reports");
        memo.getTrafficController().removeCanListener(heard);
        ThreadingUtil.runOnLayout(() -> th.release(null));
    }

    @Test
    public void testTagACbusReaderCanNotReadIsNotSent() {
        Reporter[] r = new Reporter[1];
        ThreadingUtil.runOnLayout(() -> r[0] = memo.get(ReporterManager.class).provideReporter("MR258"));
        List<int[]> frames = new CopyOnWriteArrayList<>();
        CanListener heard = new CanListener() {
            @Override
            public void message(CanMessage m) {
                // JMRI's own frames
            }

            @Override
            public void reply(CanReply f) {
                if (f.getElement(0) == ACDAT) {
                    frames.add(new int[]{f.getElement(0)});
                }
            }
        };
        memo.getTrafficController().addCanListener(heard);
        CbusSimulatedReporterOutput out = memo.get(CbusSimulator.class).getSimulatedReporterOutput();
        Assertions.assertTrue(out.reports(r[0]));
        out.report(r[0], "TAG1", System.nanoTime());
        JUnitAppender.assertWarnMessage("Tag \"TAG1\" is not 10 hex digits, so a CBUS RFID reader can't read it.");
        out.report(r[0], "0A1B2C3D4E", System.nanoTime());
        JUnitUtil.waitFor(() -> r[0].getCurrentReport() != null, "the good tag read");
        Assertions.assertEquals(1, frames.size(), "only the tag a reader can read was sent");
        memo.getTrafficController().removeCanListener(heard);
    }

    private CanSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initIdTagManager();
        SimulationTestUtil.setUpSimulatedProfile();
        memo = CbusSimulationTestUtil.addCanSimulation("M");
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        CbusSimulationTestUtil.dispose(memo);
        memo = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
