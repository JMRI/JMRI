package jmri.jmrit.dispatcher;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jmri.Block;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Section;
import jmri.Transit;
import jmri.implementation.DefaultSection;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for how an Auto Active Train finds the next Block in a Section.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class AutoActiveTrainNextBlockTest {

    private ActiveTrain at;
    private AutoActiveTrain aat;
    private Block b1;
    private Block b2;
    private Block b3;

    /**
     * A Section that, part way through a walk of its Blocks, is walked from
     * the start again, as when another thread walks it at the same time.
     */
    private static class SharedSection extends DefaultSection {

        private int nextCalls = 0;
        private final int interruptAt;

        SharedSection(String systemName, int interruptAt) {
            super(systemName);
            this.interruptAt = interruptAt;
        }

        @Override
        public Block getNextBlock() {
            if (++nextCalls == interruptAt) {
                getEntryBlock(); // another walk starts
            }
            return super.getNextBlock();
        }
    }

    private AllocatedSection allocated(Section s) {
        AllocatedSection as = mock(AllocatedSection.class);
        when(as.getSection()).thenReturn(s);
        when(as.getDirection()).thenReturn(s.getState());
        return as;
    }

    private Section section(Section s, int direction) {
        s.addBlock(b1);
        s.addBlock(b2);
        s.addBlock(b3);
        s.setState(direction);
        return s;
    }

    private Block nextBlock(Block b, AllocatedSection as) throws ReflectiveOperationException {
        Method m = AutoActiveTrain.class.getDeclaredMethod("getNextBlock", Block.class, AllocatedSection.class);
        m.setAccessible(true);
        return (Block) m.invoke(aat, b, as);
    }

    private void setField(String name, Object value) throws ReflectiveOperationException {
        Field f = AutoActiveTrain.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(aat, value);
    }

    @Test
    public void testNextBlockWhileSectionWalkedElsewhere() throws ReflectiveOperationException {
        Section s = section(new SharedSection("TS1", 2), Section.FORWARD);
        assertSame(b3, nextBlock(b2, allocated(s)), "next Block after B2");
    }

    @Test
    public void testNextBlockReverse() throws ReflectiveOperationException {
        Section s = section(new SharedSection("TS1", 2), Section.REVERSE);
        assertSame(b1, nextBlock(b2, allocated(s)), "next Block after B2 in reverse");
        assertNull(nextBlock(b1, allocated(s)), "no Block after B1 in reverse");
    }

    @Test
    public void testReverseAtEndWithTrainFillingSection() throws ReflectiveOperationException {
        // the train fills the Section, the reversed head is at the far end
        b1.setState(Block.OCCUPIED);
        b2.setState(Block.OCCUPIED);
        b3.setState(Block.OCCUPIED);
        Section s = section(new DefaultSection("TS1"), Section.REVERSE);
        setField("_currentBlock", b3);
        setField("_currentAllocatedSection", allocated(s));
        at.setReverseDelayRestart(ActiveTrain.SENSORDELAY);

        aat.executeStopTasks(AutoActiveTrain.END_REVERSAL);

        assertSame(b1, aat.getCurrentBlock(), "head of the train after reversing");
        assertNull(aat.getNextBlock(), "no further Block in the Section");
    }

    @BeforeEach
    public void setUp() throws ReflectiveOperationException {
        JUnitUtil.setUp();
        DispatcherFrame dispatcher = mock(DispatcherFrame.class);
        InstanceManager.setDefault(DispatcherFrame.class, dispatcher);

        Transit transit = new jmri.implementation.DefaultTransit("TT1");
        at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        at.setDispatcher(dispatcher);
        aat = new AutoActiveTrain(at);
        b1 = new Block("IB1", "B1");
        b2 = new Block("IB2", "B2");
        b3 = new Block("IB3", "B3");

        AutoActiveTrain.AutoEngineer engineer = aat.new AutoEngineer(mock(DccThrottle.class), null);
        setField("_autoEngineer", engineer);
        setField("_dispatcher", dispatcher);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
