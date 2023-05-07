package jmri.util;

import java.awt.Container;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.event.MouseInputAdapter;

import jmri.util.swing.JmriMouseAdapter;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2022
 */
public class MouseInputAdapterInstallerTest {

    // no testCtor as class only supplies static methods.

    @Test
    public void testMouseAdaptersAdded() {
        MIAListener m = new MIAListener();
        Container c = new Container();
        Container cInC = new Container();
        c.add(cInC);

        assertEquals(0, c.getListeners(MouseListener.class).length);
        assertEquals(0, c.getListeners(MouseMotionListener.class).length);
        assertEquals(0, cInC.getListeners(MouseListener.class).length);
        assertEquals(0, cInC.getListeners(MouseMotionListener.class).length);

        assertDoesNotThrow(() -> { 
            MouseInputAdapterInstaller.installMouseInputAdapterOnAllComponents(m, c);
        });

        assertEquals(1, c.getListeners(MouseListener.class).length);
        assertEquals(1, c.getListeners(MouseMotionListener.class).length);
        assertEquals(1, cInC.getListeners(MouseListener.class).length);
        assertEquals(1, cInC.getListeners(MouseMotionListener.class).length);

    }

    @Test
    public void testJmriMouseListenersAdded() {
        JMAListener m = new JMAListener();
        Container c = new Container();
        Container cInC = new Container();
        c.add(cInC);

        assertEquals(0, c.getListeners(MouseListener.class).length);
        assertEquals(0, c.getListeners(MouseMotionListener.class).length);
        assertEquals(0, cInC.getListeners(MouseListener.class).length);
        assertEquals(0, cInC.getListeners(MouseMotionListener.class).length);

        assertDoesNotThrow(() -> { 
            MouseInputAdapterInstaller.installMouseListenerOnAllComponents(m, c);
        });

        assertEquals(1, c.getListeners(MouseListener.class).length);
        assertEquals(0, c.getListeners(MouseMotionListener.class).length);
        assertEquals(1, cInC.getListeners(MouseListener.class).length);
        assertEquals(0, cInC.getListeners(MouseMotionListener.class).length);

    }

    @Test
    public void testInstallMouseMotionListeners() {
        MIAListener m = new MIAListener();
        Container c = new Container();
        Container cInC = new Container();
        c.add(cInC);

        assertEquals(0, c.getListeners(MouseListener.class).length);
        assertEquals(0, c.getListeners(MouseMotionListener.class).length);
        assertEquals(0, cInC.getListeners(MouseListener.class).length);
        assertEquals(0, cInC.getListeners(MouseMotionListener.class).length);

        assertDoesNotThrow(() -> { 
            MouseInputAdapterInstaller.installMouseMotionListenerOnAllComponents(m, c);
        });

        assertEquals(0, c.getListeners(MouseListener.class).length);
        assertEquals(1, c.getListeners(MouseMotionListener.class).length);
        assertEquals(0, cInC.getListeners(MouseListener.class).length);
        assertEquals(1, cInC.getListeners(MouseMotionListener.class).length);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static class MIAListener extends MouseInputAdapter {}

    private static class JMAListener extends JmriMouseAdapter {}

    // private final static Logger log = LoggerFactory.getLogger(MouseInputAdapterInstallerTest.class);

}
