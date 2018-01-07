package jmri.util.swing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeSupport;
import java.util.concurrent.Semaphore;

import javax.swing.SwingUtilities;

import jmri.util.JUnitUtil;
import jmri.util.MockPropertyChangeListener;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Balazs Racz Copyright (C) 2017
 */
public class DedupingPropertyChangeListenerTest {
    PropertyChangeSupport source = new PropertyChangeSupport(this);
    MockPropertyChangeListener l = new MockPropertyChangeListener();

    Semaphore semaphore = new Semaphore(1);

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    /// Blocks the calling thread until the swing thread drains its entire queue.
    private void waitForEventThread() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    /// Blocks the swing execution thread, ensuring that no new work enqueued can be executed
    /// until releaseEventThread is called.
    private void blockEventThread() throws Exception {
        semaphore.acquire();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                } finally {
                    semaphore.release();
                }
            }
        });
    }

    /// Undoes the effect of blockEventThread and waits for all queued callbacks to execute.
    private void releaseEventThread() throws Exception {
        semaphore.release();
        waitForEventThread();
    }

    @Test
    public void testSimplePropertyChange() throws Exception {
        source.addPropertyChangeListener(new DedupingPropertyChangeListener(l));
        source.firePropertyChange("test", null, 42);
        waitForEventThread();
        verify(l.m).onChange("test", 42);
    }

    @Test
    public void testMultipleSequentialPropertyChange() throws Exception {
        source.addPropertyChangeListener(new DedupingPropertyChangeListener(l));
        source.firePropertyChange("test", null, 42);
        waitForEventThread();
        source.firePropertyChange("test", null, 43);
        waitForEventThread();
        source.firePropertyChange("test", null, 44);
        waitForEventThread();
        verify(l.m).onChange("test", 42);
        verify(l.m).onChange("test", 43);
        verify(l.m).onChange("test", 44);
        verifyNoMoreInteractions(l.m);
    }

    @Test
    public void testMultipleIdenticalPropertyChange() throws Exception {
        source.addPropertyChangeListener(new DedupingPropertyChangeListener(l));
        source.firePropertyChange("test", null, 42);
        waitForEventThread();
        source.firePropertyChange("test", null, 42);
        waitForEventThread();
        source.firePropertyChange("test", null, 42);
        waitForEventThread();
        verify(l.m, times(3)).onChange("test", 42);
        verifyNoMoreInteractions(l.m);
    }

    @Test
    public void testCollapseIdenticalPropertyChange() throws Exception {
        source.addPropertyChangeListener(new DedupingPropertyChangeListener(l));
        blockEventThread();
        source.firePropertyChange("test", null, 42);
        source.firePropertyChange("test", null, 44);
        source.firePropertyChange("test", null, 43);
        releaseEventThread();
        verify(l.m, times(1)).onChange("test", 43);
        verifyNoMoreInteractions(l.m);
    }

    @Test
    public void testNoCollapseDifferentPropertyChange() throws Exception {
        source.addPropertyChangeListener(new DedupingPropertyChangeListener(l));
        blockEventThread();
        source.firePropertyChange("testx", null, 42);
        source.firePropertyChange("testy", null, 44);
        source.firePropertyChange("testx", null, 43);
        releaseEventThread();
        verify(l.m, times(1)).onChange("testx", 43);
        verify(l.m, times(1)).onChange("testy", 44);
        verifyNoMoreInteractions(l.m);
    }

}
