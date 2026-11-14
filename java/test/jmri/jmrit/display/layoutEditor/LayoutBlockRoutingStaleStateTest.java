package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jmri.Block;
import jmri.InstanceManager;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrit.display.EditorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Check that a routing update carrying a block status that is already out of
 * date, because the block changed again while the update was on its way, is
 * not passed on round a loop of blocks without end.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutBlockRoutingStaleStateTest {

    @Test
    public void testOutOfDateStatusNotPassedOn() throws Exception {
        LayoutBlockManager lbm = LayoutBlockRoutingChecker.loadPanel("Jlang-Panel-4-19-6.xml");
        LayoutBlockRoutingChecker checker = new LayoutBlockRoutingChecker(lbm);
        LayoutBlock[] loop = checker.findLoop();
        assertNotNull(loop, "panel has a loop of blocks");
        LayoutBlock block = loop[1];

        // the status this block had a moment ago, not its status now
        int now = block.getBlockStatus();
        int earlier = (now == LayoutBlock.OCCUPIED) ? LayoutBlock.EMPTY : LayoutBlock.OCCUPIED;
        Constructor<?> packet = Class.forName(LayoutBlock.class.getName() + "$RoutingPacket")
                .getDeclaredConstructor(int.class, Block.class, int.class, int.class, float.class, int.class, Integer.class);
        packet.setAccessible(true);
        Object update = packet.newInstance(LayoutBlock.UPDATE, block.getBlock(), -1, -1, -1f, earlier, 1);
        Method fire = AbstractNamedBean.class.getDeclaredMethod("firePropertyChange", String.class, Object.class, Object.class);
        fire.setAccessible(true);

        AtomicInteger passedOn = new AtomicInteger();
        for (LayoutBlock lb : lbm.getNamedBeanSet()) {
            if (lb != block) {
                lb.addPropertyChangeListener(e -> {
                    if (LayoutBlock.PROPERTY_ROUTING.equals(e.getPropertyName())) {
                        passedOn.incrementAndGet();
                    }
                });
            }
        }

        // the block's neighbours receive the out of date status
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        ThreadingUtil.runOnLayout(() -> {
            try {
                fire.invoke(block, LayoutBlock.PROPERTY_ROUTING, null, update);
            } catch (InvocationTargetException e) {
                thrown.set(e.getCause());
            } catch (IllegalAccessException e) {
                thrown.set(e);
            }
        });
        assertNull(thrown.get(), "routing update handled");
        assertEquals(0, passedOn.get(), "out of date status not passed on by the neighbours");
        assertEquals(now, block.getBlockStatus(), "block status unchanged");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initMemoryManager();
    }

    @AfterEach
    public void tearDown() {
        for (LayoutEditor panel : InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class)) {
            JUnitUtil.dispose(panel);
        }
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
