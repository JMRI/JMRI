package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jmri.*;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Checks the "beans" events and the sorted bean set kept by a proxy manager.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class ProxyManagerBeanEventsTest {

    private int recomputes;
    private ProxySensorManager proxy;
    private InternalSensorManager internal;

    @Test
    public void testBeansListenersCalledOncePerNewBean() {
        AtomicInteger named = new AtomicInteger();
        AtomicInteger unnamed = new AtomicInteger();
        proxy.addPropertyChangeListener("beans", e -> named.incrementAndGet());
        proxy.addPropertyChangeListener(e -> {
            if ("beans".equals(e.getPropertyName())) {
                unnamed.incrementAndGet();
            }
        });

        internal.provideSensor("IS2");
        assertEquals(1, named.get(), "named beans listener calls for one new bean");
        assertEquals(1, unnamed.get(), "unnamed listener beans calls for one new bean");

        // manager added after the listeners
        InternalSensorManager late = new InternalSensorManager(new InternalSystemConnectionMemo("J", "Juliet"));
        proxy.addManager(late);
        named.set(0);
        unnamed.set(0);
        late.provideSensor("JS5");
        assertEquals(1, named.get(), "named beans listener calls for one new bean on a late manager");
        assertEquals(1, unnamed.get(), "unnamed listener beans calls for one new bean on a late manager");
    }

    @Test
    public void testRemovedListenersNotLeftOnLateManager() {
        AtomicInteger hits = new AtomicInteger();
        List<PropertyChangeListener> ls = new ArrayList<>();
        for (int k = 0; k < 3; k++) {
            PropertyChangeListener l = e -> hits.incrementAndGet();
            ls.add(l);
            proxy.addPropertyChangeListener("beans", l);
        }
        InternalSensorManager late = new InternalSensorManager(new InternalSystemConnectionMemo("J", "Juliet"));
        proxy.addManager(late);
        // the listeners plus the proxy itself
        assertEquals(4, late.getPropertyChangeListeners("beans").length, "beans listeners on late manager");

        late.provideSensor("JS1");
        assertEquals(3, hits.get(), "each listener called once");

        ls.forEach(l -> proxy.removePropertyChangeListener("beans", l));
        assertEquals(0, proxy.getPropertyChangeListeners("beans").length, "listeners left on proxy");
        assertEquals(1, late.getPropertyChangeListeners("beans").length, "listeners left on late manager");

        hits.set(0);
        late.provideSensor("JS9");
        assertEquals(0, hits.get(), "removed listeners called");
    }

    @Test
    public void testNamedBeanSetUpdatedWithoutRebuilding() {
        Sensor s1 = internal.provideSensor("IS1");
        assertEquals(1, proxy.getNamedBeanSet().size());

        recomputes = 0;
        for (int i = 2; i <= 20; i++) {
            internal.provideSensor("IS" + i);
        }
        Sensor s5 = internal.getBySystemName("IS5");
        internal.deregister(s5);
        assertEquals(0, recomputes, "whole set rebuilt for single changes");

        assertEquals(19, proxy.getNamedBeanSet().size());
        assertEquals(s1, proxy.getNamedBeanSet().first());
        assertEquals("IS20", proxy.getNamedBeanSet().last().getSystemName());
        assertFalse(proxy.getNamedBeanSet().contains(s5), "removed bean still in set");

        // silenced and then released, as when loading a file
        internal.setPropertyChangesSilenced("beans", true);
        internal.provideSensor("IS30");
        internal.setPropertyChangesSilenced("beans", false);
        assertEquals(20, proxy.getNamedBeanSet().size());
        assertEquals("IS30", proxy.getNamedBeanSet().last().getSystemName());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        proxy = new ProxySensorManager() {
            @Override
            protected void recomputeNamedBeanSet() {
                recomputes++;
                super.recomputeNamedBeanSet();
            }
        };
        internal = new InternalSensorManager(new InternalSystemConnectionMemo("I", "India"));
        proxy.addManager(internal);
    }

    @AfterEach
    public void tearDown() {
        proxy.dispose();
        JUnitUtil.tearDown();
    }

}
