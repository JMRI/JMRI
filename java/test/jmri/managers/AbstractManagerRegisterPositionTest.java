package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.beans.IndexedPropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import jmri.Manager;
import jmri.Sensor;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Checks the positions sent to listeners when beans are registered and
 * deregistered, including bulk loading in name order.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AbstractManagerRegisterPositionTest {

    private InternalSensorManager m;
    private final List<Integer> added = new ArrayList<>();
    private final List<Integer> removed = new ArrayList<>();

    private final Manager.ManagerDataListener<Sensor> dataListener = new Manager.ManagerDataListener<Sensor>() {
        @Override
        public void contentsChanged(Manager.ManagerDataEvent<Sensor> e) {
        }

        @Override
        public void intervalAdded(Manager.ManagerDataEvent<Sensor> e) {
            assertEquals(e.getIndex0(), e.getIndex1());
            added.add(e.getIndex0());
        }

        @Override
        public void intervalRemoved(Manager.ManagerDataEvent<Sensor> e) {
            assertEquals(e.getIndex0(), e.getIndex1());
            removed.add(e.getIndex0());
        }
    };

    private int expectedPosition(Sensor s) {
        return m.getNamedBeanSet().headSet(s).size();
    }

    @Test
    public void testPositionsForDataListener() {
        m.addDataListener(dataListener);
        int n = 2000;
        for (int i = 0; i < n; i++) {
            m.provideSensor("JS" + (10000 + i));
        }
        assertEquals(n, added.size());
        for (int i = 0; i < n; i++) {
            assertEquals(i, added.get(i).intValue(), "position of bean added in order");
        }

        Sensor mid = m.provideSensor("JS10500x");
        assertEquals(expectedPosition(mid), added.get(n).intValue(), "position of bean added in the middle");
        Sensor first = m.provideSensor("JS1");
        assertEquals(0, added.get(n + 1).intValue(), "position of bean added at the start");

        int midPosition = expectedPosition(mid);
        m.deregister(mid);
        m.deregister(first);
        Sensor last = m.getNamedBeanSet().last();
        m.deregister(last);
        assertEquals(List.of(midPosition, 0, n - 1), removed, "positions of removed beans");
    }

    @Test
    public void testPositionsForBeansListener() {
        List<Integer> indexes = new ArrayList<>();
        m.addPropertyChangeListener(e -> {
            if (Manager.PROPERTY_BEANS.equals(e.getPropertyName())) {
                indexes.add(((IndexedPropertyChangeEvent) e).getIndex());
            }
        });
        Sensor s3 = m.provideSensor("JS3");
        m.provideSensor("JS1");
        m.provideSensor("JS2");
        m.provideSensor("JS4");
        m.deregister(s3);
        assertEquals(List.of(0, 0, 1, 3, 2), indexes);
    }

    @Test
    public void testPositionsForNamedBeansListener() {
        List<Integer> indexes = new ArrayList<>();
        m.addPropertyChangeListener(Manager.PROPERTY_BEANS,
                e -> indexes.add(((IndexedPropertyChangeEvent) e).getIndex()));
        m.provideSensor("JS2");
        m.provideSensor("JS1");
        Sensor s3 = m.provideSensor("JS3");
        m.deregister(s3);
        assertEquals(List.of(0, 0, 2, 2), indexes);
    }

    @Test
    public void testRegisterWithoutListeners() {
        for (int i = 0; i < 100; i++) {
            m.provideSensor("JS" + (1000 - i));
        }
        assertEquals(100, m.getObjectCount());
        assertEquals("JS901", m.getNamedBeanSet().first().getSystemName());
        m.deregister(m.getNamedBeanSet().first());
        assertEquals("JS902", m.getNamedBeanSet().first().getSystemName());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        m = new InternalSensorManager(new InternalSystemConnectionMemo("J", "Juliet"));
    }

    @AfterEach
    public void tearDown() {
        m.dispose();
        JUnitUtil.tearDown();
    }

}
