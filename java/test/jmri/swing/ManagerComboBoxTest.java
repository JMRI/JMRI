package jmri.swing;

import javax.swing.JFrame;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.ProxyManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.jmrix.internal.InternalTurnoutManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JComboBoxOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ManagerComboBoxTest {

    private ManagerComboBox<Turnout> box;

    @Test
    public void testSetManagers_ManagersNoSelection() {
        assertNull( box.getSelectedItem(), "no selection");
        assertEquals( 0, box.getItemCount(), "no items");
        TurnoutManager defaultManager = InstanceManager.getDefault(TurnoutManager.class);
        assertInstanceOf(ProxyManager.class, defaultManager, "default is proxy");
        // checks for unchecked casts do not appear to be assertion aware
        @SuppressWarnings("unchecked")
        ProxyManager<Turnout> proxyManager = (ProxyManager<Turnout>) defaultManager;
        box.setManagers(proxyManager.getDisplayOrderManagerList());
        assertEquals( 0, box.getSelectedIndex(), "first item is selected");
        assertEquals( 2, box.getItemCount(), "all items in box");
    }

    @Test
    public void testSetManagers_NonproxyManager() {
        assertNull( box.getSelectedItem(), "no selection");
        assertEquals( 0, box.getItemCount(), "no items");
        TurnoutManager defaultManager = InstanceManager.getDefault(TurnoutManager.class);
        assertInstanceOf(ProxyManager.class, defaultManager);
        // checks for unchecked casts do not appear to be assertion aware
        @SuppressWarnings("unchecked")
        ProxyManager<Turnout> proxyManager = (ProxyManager<Turnout>) defaultManager;
        Manager<Turnout> manager = proxyManager.getDefaultManager();
        box.setManagers(manager);
        assertEquals( manager, box.getSelectedItem(), "manager is selected");
        assertEquals( 1, box.getItemCount(), "single item in box");
    }

    @Test
    public void testSetManagers_ProxyManager() {
        assertNull( box.getSelectedItem(), "no selection");
        assertEquals( 0, box.getItemCount(), "no items");
        TurnoutManager defaultManager = InstanceManager.getDefault(TurnoutManager.class);
        assertInstanceOf(ProxyManager.class, defaultManager, "default is proxy");
        // checks for unchecked casts do not appear to be assertion aware
        @SuppressWarnings("unchecked")
        ProxyManager<Turnout> proxyManager = (ProxyManager<Turnout>) defaultManager;
        box.setManagers(defaultManager);
        assertEquals( proxyManager.getDefaultManager(), box.getSelectedItem(), "default is selected");
        assertEquals( 2, box.getItemCount(), "all items in box");
    }

    @Test
    @DisabledIfHeadless("GUI Required")
    public void testRenderer() {
        box.setManagers(InstanceManager.getDefault(TurnoutManager.class));
        JFrame frame = new JFrame();
        frame.add(box);
        frame.pack();
        frame.setVisible(true);
        JComboBoxOperator cbo = new JComboBoxOperator(box);
        assertEquals( box.getSelectedItem(), cbo.getItemAt(0), "Rendered without Exception");
        frame.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        InstanceManager.setTurnoutManager(new InternalTurnoutManager(new InternalSystemConnectionMemo("J", "Juliet")));
        box = new ManagerComboBox<Turnout>();
    }

    @AfterEach
    public void tearDown() {
        box = null;
        JUnitUtil.tearDown();
    }
}
