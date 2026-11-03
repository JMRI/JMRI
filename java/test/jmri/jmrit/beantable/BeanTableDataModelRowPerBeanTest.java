package jmri.jmrit.beantable;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.beantable.sensor.SensorTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bean tables show a bean added to or removed from the manager by inserting or
 * removing its row, not by rebuilding the table.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class BeanTableDataModelRowPerBeanTest {

    private SensorManager sm;
    private SensorTableDataModel model;
    private int inserted;
    private int deleted;
    private int rebuilt;

    private void count(TableModelEvent e) {
        if (e.getType() == TableModelEvent.INSERT) {
            inserted++;
        } else if (e.getType() == TableModelEvent.DELETE) {
            deleted++;
        } else if (e.getFirstRow() == 0 && e.getLastRow() == Integer.MAX_VALUE) {
            rebuilt++;
        }
    }

    private List<String> managerOrder() {
        List<String> names = new ArrayList<>();
        for (NamedBean b : sm.getNamedBeanSet()) {
            names.add(b.getSystemName());
        }
        return names;
    }

    @Test
    public void testAddedBeansInsertRows() {
        for (int i = 1; i <= 50; i++) {
            sm.provideSensor("IS" + i);
        }
        assertEquals(50, inserted, "a row inserted for each sensor");
        assertEquals(0, rebuilt, "never rebuilt");
        assertEquals(managerOrder(), model.sysNameList, "rows in the manager's order");
        assertEquals(50, model.getRowCount());
    }

    @Test
    public void testRowsInTheManagersOrder() {
        for (String n : new String[]{"IS10", "IS2", "ISa", "IS1", "IS100", "ISB", "IS20", "IS3"}) {
            sm.provideSensor(n);
        }
        List<String> rowByRow = new ArrayList<>(model.sysNameList);
        model.updateNameList();
        assertEquals(model.sysNameList, rowByRow, "the same list as a rebuild");
        assertEquals(managerOrder(), rowByRow);
    }

    @Test
    public void testRemovedBeanDeletesRowAndListener() {
        Sensor a = sm.provideSensor("IS1");
        Sensor b = sm.provideSensor("IS2");
        sm.deregister(a);
        assertEquals(1, deleted);
        assertEquals(0, rebuilt);
        assertEquals(List.of("IS2"), model.sysNameList);
        assertFalse(List.of(a.getPropertyChangeListeners()).contains(model), "listener removed");
        assertTrue(List.of(b.getPropertyChangeListeners()).contains(model), "listener kept");
    }

    @Test
    public void testNewBeanWatchedForChanges() throws JmriException {
        Sensor s = sm.provideSensor("IS7");
        List<TableModelEvent> updates = new ArrayList<>();
        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                updates.add(e);
            }
        });
        s.setState(Sensor.ACTIVE);
        assertFalse(updates.isEmpty(), "its row updated when it changes");
        assertEquals(0, updates.get(0).getFirstRow());
    }

    @Test
    public void testFilteredBeanNotShownNorRebuilt() {
        model.setFilter(b -> !b.getSystemName().endsWith("x"));
        rebuilt = 0;
        sm.provideSensor("IS1x");
        sm.provideSensor("IS2");
        assertEquals(List.of("IS2"), model.sysNameList);
        assertEquals(0, rebuilt);
    }

    @Test
    public void testLengthWithoutBeansStillRebuilds() {
        // a manager that is silenced sends "length" only; the table rebuilds as before
        SensorManager internal = (SensorManager) ((jmri.managers.AbstractProxyManager<?>) sm).getDefaultManager();
        internal.setPropertyChangesSilenced(jmri.Manager.PROPERTY_BEANS, true);
        sm.provideSensor("IS5");
        sm.provideSensor("IS6");
        internal.setPropertyChangesSilenced(jmri.Manager.PROPERTY_BEANS, false);
        assertEquals(2, rebuilt, "rebuilt on each length event");
        assertEquals(managerOrder(), model.sysNameList);
    }

    /** A table that builds its name list itself, counting each build. */
    @SuppressWarnings("serial") // not serialized
    private static class OwnListModel extends SensorTableDataModel {
        // counted from the constructor on, so not initialised here
        private int builds;

        OwnListModel(SensorManager manager) {
            super(manager);
        }

        @Override
        protected synchronized void updateNameList() {
            super.updateNameList();
            builds++;
        }

        synchronized int builds() {
            return builds;
        }
    }

    @Test
    public void testSubclassBuildingItsOwnListRebuilds() {
        OwnListModel custom = new OwnListModel(sm);
        int before = custom.builds();
        sm.provideSensor("IS9");
        assertEquals(before + 1, custom.builds(), "its own list rebuilt");
        assertEquals(List.of("IS9"), custom.sysNameList);
        custom.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        sm = InstanceManager.getDefault(SensorManager.class);
        model = new SensorTableDataModel(sm);
        model.addTableModelListener(this::count);
    }

    @AfterEach
    public void tearDown() {
        model.dispose();
        model = null;
        JUnitUtil.tearDown();
    }

}
