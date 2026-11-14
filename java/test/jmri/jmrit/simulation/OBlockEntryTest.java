package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Objects;

import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;
import jmri.jmrit.logix.PortalManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link OBlockEntry}: OBlocks with Portals, as on a Control Panel
 * Editor panel. These check the simulator's route finding only.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class OBlockEntryTest {

    private TrackModels models;
    private Turnout to1;

    /** Copy the layout's configuration, as a placement does. */
    private void copy() {
        models.use(TrackSnapshot.take(java.util.List.of())).dispose();
    }

    @Test
    public void testOBlockPortals() {
        OBlockManager obm = InstanceManager.getDefault(OBlockManager.class);
        PortalManager pm = InstanceManager.getDefault(PortalManager.class);
        OBlock a = Objects.requireNonNull(obm.createNewOBlock("OB1", "A"), "OBlock A created");
        OBlock x = Objects.requireNonNull(obm.createNewOBlock("OB2", "X"), "OBlock X created");
        OBlock c = Objects.requireNonNull(obm.createNewOBlock("OB3", "C"), "OBlock C created");
        OBlock d = Objects.requireNonNull(obm.createNewOBlock("OB4", "D"), "OBlock D created");
        x.setLength(600);
        a.setLength(300);
        c.setLength(300);
        d.setLength(300);
        Portal ax = pm.createNewPortal("A-X");
        ax.setFromBlock(a, false);
        ax.setToBlock(x, false);
        Portal xc = pm.createNewPortal("X-C");
        xc.setFromBlock(x, false);
        xc.setToBlock(c, false);
        Portal xd = pm.createNewPortal("X-D");
        xd.setFromBlock(x, false);
        xd.setToBlock(d, false);
        OPath main = new OPath("main", x, ax, xc, List.of(new BeanSetting(to1, "IT1", Turnout.CLOSED)));
        main.setLength(610);
        x.addPath(main);
        x.addPath(new OPath("branch", x, ax, xd, List.of(new BeanSetting(to1, "IT1", Turnout.THROWN))));
        c.addPath(new OPath("c", c, xc, null, null));

        copy();
        TrackEntry into = models.entryInto(x, a);
        to1.setCommandedState(Turnout.CLOSED);
        Passage p = into.follow();
        assertEquals(c, Objects.requireNonNull(p.next, "on from X").getBlock());
        assertEquals(610.0, p.length, 1e-9, "the OPath's own length");
        to1.setCommandedState(Turnout.THROWN);
        p = into.follow();
        assertEquals(d, Objects.requireNonNull(p.next, "on from X").getBlock());
        assertEquals(600.0, p.length, 1e-9, "the OBlock's length");
        p = models.entryInto(x, c).follow();
        assertEquals(Passage.Stop.BLOCKED, p.stop, "from C with the turnout thrown");
        to1.setCommandedState(Turnout.CLOSED);
        p = models.entryInto(c, x).follow();
        assertEquals(Passage.Stop.END_OF_TRACK, p.stop, "C's path ends in C");
        TrackEntry back = Objects.requireNonNull(p.back, "back out of C");
        assertEquals(x, Objects.requireNonNull(back.follow().next, "back out of C into X").getBlock(),
                "back out of C into X");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        models = new TrackModels();
        to1 = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
    }

    @AfterEach
    public void tearDown() {
        models.use(TrackSnapshot.empty()).dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
