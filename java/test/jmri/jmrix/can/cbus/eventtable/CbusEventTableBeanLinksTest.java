package jmri.jmrix.can.cbus.eventtable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.event.TableModelListener;

import jmri.Light;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.*;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The CBUS event table follows Sensors, Turnouts and Lights being added,
 * removed and renamed, one bean at a time.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusEventTableBeanLinksTest {

    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis;
    private CbusTurnoutManager tm;
    private CbusSensorManager sm;
    private CbusLightManager lm;
    private CbusEventTableDataModel model;
    private final AtomicInteger tableEvents = new AtomicInteger();
    private final TableModelListener counter = e -> tableEvents.incrementAndGet();

    /** Let the GUI updates already queued run. */
    private static void flush() {
        ThreadingUtil.runOnGUI(() -> { });
    }

    /**
     * For each row, the beans for the event's on and off columns, as sets,
     * so that equal links compare equal whatever order they were made in.
     */
    private List<List<Set<NamedBean>>> links() {
        List<List<Set<NamedBean>>> all = new ArrayList<>();
        for (CbusTableEvent ev : model._mainArray) {
            List<Set<NamedBean>> row = new ArrayList<>();
            for (CbusTableEvent.EvState state : new CbusTableEvent.EvState[]{
                CbusTableEvent.EvState.ON, CbusTableEvent.EvState.OFF}) {
                CbusEventBeanData d = ev.getBeans(state);
                row.add(new HashSet<>(d.getActionA()));
                row.add(new HashSet<>(d.getActionB()));
            }
            all.add(row);
        }
        return all;
    }

    private CbusTableEvent row(int nn, int en) {
        int r = model.getEventTableRow(nn, en);
        assertTrue(r >= 0, "row for " + nn + " " + en);
        return model._mainArray.get(r);
    }

    @Test
    public void testAddingABeanUpdatesOnlyItsRow() {
        for (int i = 1; i <= 20; i++) {
            sm.provideSensor("MS+N1E" + i);
        }
        flush();
        tableEvents.set(0);
        int rows = model.getRowCount();
        for (int i = 21; i <= 70; i++) {
            sm.provideSensor("MS+N1E" + i);
        }
        flush();
        assertEquals(rows + 50, model.getRowCount(), "a row for each new event");
        // each new bean: its row inserted, and its on and off cells updated
        // once for each of its two events, so at most 5 table events
        assertTrue(tableEvents.get() <= 50 * 5,
            "table events for 50 new sensors: " + tableEvents.get());
        assertTrue(row(1, 70).hasBean(sm.getBySystemName("MS+N1E70")));
    }

    @Test
    public void testPerBeanLinksEqualAFullUpdate() {
        sm.provideSensor("MS+N1E1");
        sm.provideSensor("MS+5");
        sm.provideSensor("MS+N1E2;-N1E3");
        Sensor inverted = sm.provideSensor("MS+N1E4");
        inverted.setInverted(true);
        tm.provideTurnout("MT+N1E1");
        tm.provideTurnout("MT+N2E7;+N2E8");
        lm.provideLight("ML+N1E2");
        lm.provideLight("ML-N3E9");
        Turnout gone = tm.provideTurnout("MT+N4E4");
        tm.deregister(gone);
        Light renamed = lm.provideLight("ML+N5E5");
        renamed.setUserName("Platform lamp");
        flush();

        List<List<Set<NamedBean>>> perBean = links();
        model.ta.updatejmricols();
        flush();
        assertEquals(links(), perBean, "the same links as a full update");
        assertFalse(row(4, 4).hasBean(gone), "removed bean unlinked");
    }

    @Test
    public void testRemovedBeanUnlinked() {
        Sensor s = sm.provideSensor("MS+N1E9");
        Sensor other = sm.provideSensor("MS+N1E9;+N1E10");
        flush();
        assertTrue(row(1, 9).hasBean(s));
        sm.deregister(s);
        flush();
        assertFalse(row(1, 9).hasBean(s));
        assertTrue(row(1, 9).hasBean(other), "other bean kept");
    }

    @Test
    public void testSilencedBeansLinkedWhenResumed() {
        sm.setPropertyChangesSilenced(jmri.Manager.PROPERTY_BEANS, true);
        Sensor s = sm.provideSensor("MS+N1E30");
        flush();
        assertEquals(-1, model.getEventTableRow(1, 30), "not followed while silenced");
        sm.setPropertyChangesSilenced(jmri.Manager.PROPERTY_BEANS, false);
        flush();
        assertTrue(row(1, 30).hasBean(s), "linked when resumed");
    }

    @Test
    public void testRenameRedrawsOnlyItsRow() {
        for (int i = 1; i <= 40; i++) {
            sm.provideSensor("MS+N1E" + i);
        }
        Sensor s = sm.provideSensor("MS+N1E50");
        flush();
        tableEvents.set(0);
        s.setUserName("Up home signal");
        flush();
        assertEquals(2, tableEvents.get(), "its on and off cells redrawn");
        assertTrue(model.getValueAt(model.getEventTableRow(1, 50), CbusEventTableDataModel.STLR_ON_COLUMN)
            .toString().contains("Up home signal"));

        // with no name left there is no bean to find, so every row is
        // redrawn, in one table event
        tableEvents.set(0);
        s.setUserName(null);
        flush();
        assertEquals(1, tableEvents.get());
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.resetInstanceManager();
        tcis = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);

        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        memo.configureManagers();

        tm = (CbusTurnoutManager) memo.get(jmri.TurnoutManager.class);
        sm = (CbusSensorManager) memo.get(jmri.SensorManager.class);
        lm = (CbusLightManager) memo.get(jmri.LightManager.class);

        model = memo.get(CbusConfigurationManager.class)
            .provide(CbusEventTableDataModel.class);
        model.addTableModelListener(counter);
    }

    @AfterEach
    public void tearDown() {
        model.removeTableModelListener(counter);
        model.skipSaveOnDispose();
        model.dispose();
        memo.dispose();
        memo = null;
        tcis.terminateThreads();
        tcis = null;
        lm.dispose();
        tm.dispose();
        sm.dispose();

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
