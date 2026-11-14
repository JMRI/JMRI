package jmri.jmrix.marklin;

import jmri.util.JUnitUtil;

import java.util.ArrayList;
import java.util.List;

import jmri.jmrix.marklin.mcan.cdb.CdBConfigCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2SystemCommands;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for the jmri.jmrix.marklin.MarklinTrafficController class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class MarklinTrafficControllerTest extends jmri.jmrix.AbstractMRTrafficControllerTest {

    @Test
    public void testCdBFrameDispatchedOnceAndStillReachesOrdinaryListeners() {
        MarklinTrafficController controller = (MarklinTrafficController) tc;
        List<String> calls = new ArrayList<>();
        MarklinListener first = recorder("first", calls);
        MarklinListener second = recorder("second", calls);
        controller.addMarklinListener(first);
        controller.addMarklinListener(second);
        controller.getCdBFilter().addCdBConfigListener((frame, raw) -> calls.add("cdb"));

        MarklinReply cdb = new MarklinReply(CdBConfigCommands.buildRead(0x22, 3, 1, 2));
        controller.notifyReply(cdb, recorder("dest", calls));
        assertEquals(List.of("cdb", "first", "second", "dest"), calls);

        calls.clear();
        controller.removeMarklinListener(first);
        controller.removeMarklinListener(second);
        controller.notifyReply(new MarklinReply(cdb), null);
        assertEquals(List.of("cdb"), calls, "CdB delivery must not depend on ordinary listeners");
    }

    @Test
    public void testNativeFrameSkipsCdBListeners() {
        MarklinTrafficController controller = (MarklinTrafficController) tc;
        List<String> calls = new ArrayList<>();
        controller.addMarklinListener(recorder("first", calls));
        controller.getCdBFilter().addCdBConfigListener((frame, raw) -> calls.add("cdb"));

        controller.notifyReply(new MarklinReply(Cs2SystemCommands.buildSystemGo()), recorder("dest", calls));
        assertEquals(List.of("first", "dest"), calls);
    }

    private static MarklinListener recorder(String name, List<String> calls) {
        return new MarklinListener() {
            @Override public void message(MarklinMessage m) { }
            @Override public void reply(MarklinReply r) { calls.add(name); }
        };
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficController();
    }
    
    @Override
    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        JUnitUtil.tearDown();
    }

}
