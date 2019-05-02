package jmri.util.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.beans.PropertyChangeSupport;
import java.util.concurrent.Semaphore;

import javax.swing.SwingUtilities;

import jmri.util.JUnitUtil;
import jmri.util.PropertyChangeListenerScaffold;

/**
 * @author Balazs Racz Copyright (C) 2017
 */
public class DedupingPropertyChangeListenerTest {
    PropertyChangeSupport source = new PropertyChangeSupport(this);
    PropertyChangeListenerScaffold l; 

    Semaphore semaphore = new Semaphore(1);

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        l = new PropertyChangeListenerScaffold();
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
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged();});
        Assert.assertEquals("called once",1,l.getCallCount());
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
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged();});
        Assert.assertEquals("called three times",3,l.getCallCount());
    }

    @Test
    public void testMultipleIdenticalPropertyChange() throws Exception {
        source.addPropertyChangeListener(new DedupingPropertyChangeListener(l));
        source.firePropertyChange("test", null, 42);
        waitForEventThread();
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged();});
        l.resetPropertyChanged();
        source.firePropertyChange("test", null, 42);
        waitForEventThread();
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged();});
        l.resetPropertyChanged();
        source.firePropertyChange("test", null, 42);
        waitForEventThread();
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged();});
    }

    @Test
    public void testCollapseIdenticalPropertyChange() throws Exception {
        source.addPropertyChangeListener(new DedupingPropertyChangeListener(l));
        blockEventThread();
        source.firePropertyChange("test", null, 42);
        source.firePropertyChange("test", null, 44);
        source.firePropertyChange("test", null, 43);
        releaseEventThread();
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged();});
        Assert.assertEquals("called once",1,l.getCallCount());
    }

    @Test
    public void testNoCollapseDifferentPropertyChange() throws Exception {
        source.addPropertyChangeListener(new DedupingPropertyChangeListener(l));
        blockEventThread();
        source.firePropertyChange("testx", null, 42);
        source.firePropertyChange("testy", null, 44);
        source.firePropertyChange("testx", null, 43);
        releaseEventThread();
        JUnitUtil.waitFor( () -> { return l.getPropertyChanged();});
        Assert.assertEquals("called twice",2,l.getCallCount());
    }

}
