package jmri.swing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JComboBoxOperator;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.ProxyManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.jmrix.internal.InternalTurnoutManager;
import jmri.util.JUnitUtil;

public class ManagerComboBoxTest {

    private ManagerComboBox<Turnout> box;

    @Test
    public void testSetManagers_ManagersNoSelection() {
        assertNull("no selection", box.getSelectedItem());
        assertEquals("no items", 0, box.getItemCount());
        TurnoutManager defaultManager = InstanceManager.getDefault(TurnoutManager.class);
        assertTrue("default is proxy", defaultManager instanceof ProxyManager);
        // checks for unchecked casts do not appear to be assertion aware
        @SuppressWarnings("unchecked")
        ProxyManager<Turnout> proxyManager = (ProxyManager<Turnout>) defaultManager;
        box.setManagers(proxyManager.getDisplayOrderManagerList());
        assertEquals("first item is selected", 0, box.getSelectedIndex());
        assertEquals("all items in box", 2, box.getItemCount());
    }

    @Test
    public void testSetManagers_NonproxyManager() {
        assertNull("no selection", box.getSelectedItem());
        assertEquals("no items", 0, box.getItemCount());
        TurnoutManager defaultManager = InstanceManager.getDefault(TurnoutManager.class);
        assertTrue("default is proxy", defaultManager instanceof ProxyManager);
        // checks for unchecked casts do not appear to be assertion aware
        @SuppressWarnings("unchecked")
        ProxyManager<Turnout> proxyManager = (ProxyManager<Turnout>) defaultManager;
        Manager<Turnout> manager = proxyManager.getDefaultManager();
        box.setManagers(manager);
        assertEquals("manager is selected", manager, box.getSelectedItem());
        assertEquals("single item in box", 1, box.getItemCount());
    }

    @Test
    public void testSetManagers_ProxyManager() {
        assertNull("no selection", box.getSelectedItem());
        assertEquals("no items", 0, box.getItemCount());
        TurnoutManager defaultManager = InstanceManager.getDefault(TurnoutManager.class);
        assertTrue("default is proxy", defaultManager instanceof ProxyManager);
        // checks for unchecked casts do not appear to be assertion aware
        @SuppressWarnings("unchecked")
        ProxyManager<Turnout> proxyManager = (ProxyManager<Turnout>) defaultManager;
        box.setManagers(defaultManager);
        assertEquals("default is selected", proxyManager.getDefaultManager(), box.getSelectedItem());
        assertEquals("all items in box", 2, box.getItemCount());
    }

    @Test
    public void testRenderer() {
        assumeFalse("GUI required", GraphicsEnvironment.isHeadless());
        box.setManagers(InstanceManager.getDefault(TurnoutManager.class));
        JFrame frame = new JFrame();
        frame.add(box);
        frame.pack();
        frame.setVisible(true);
        JComboBoxOperator cbo = new JComboBoxOperator(box);
        assertEquals("Rendered without Exception", box.getSelectedItem(), cbo.getItemAt(0));
        frame.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        InstanceManager.setTurnoutManager(new InternalTurnoutManager(new InternalSystemConnectionMemo("J", "Juliet")));
        box = new ManagerComboBox<Turnout>();
    }

    @After
    public void tearDown() {
        box = null;
        JUnitUtil.tearDown();
    }
}
