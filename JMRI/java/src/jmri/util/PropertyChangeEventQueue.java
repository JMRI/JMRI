package jmri.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import jmri.NamedBean;


/**
 * Gathers PropertyChangeEvents that might occur in overlapping threads and at
 * overlapping times, presenting them as requested.
 * <p>
 * Listeners are installed when the object is constructed. {@link #dispose()}
 * detaches those listeners, after which the object should not be used. It is
 * not an error to call {@link #dispose()} multiple times.
 * <p>
 * Although this could be more generic than NamedBean, there's no single
 * interface that specifies "can call addPropertyChangeListener(..)".
 *
 * @author Bob Jacobsen Copyright 2017
 */
@ThreadSafe
public class PropertyChangeEventQueue {

    /**
     * @param collection Set of NamedBeans whose events should be handled. Keeps
     *                   a copy of the contents, so future changes irrelevant.
     */
    public PropertyChangeEventQueue(@Nonnull Collection<NamedBean> collection) {
        this();
        for (NamedBean item : collection) {
            items.add(item);
            item.addPropertyChangeListener(listener);
        }
        if (log.isTraceEnabled()) {
            log.trace("Created {}", this.toString());
        }
    }

    /**
     * @param array Set of NamedBeans whose events should be handled Keeps a
     *              copy of the contents, so future changes irrelevant.
     */
    public PropertyChangeEventQueue(@Nonnull NamedBean[] array) {
        this(Arrays.asList(array));
    }

    // Empty object makes no sense
    private PropertyChangeEventQueue() {
    }

    final Collection<NamedBean> items = new ArrayList<>();
    final static int MAX_SIZE = 100;
    final BlockingQueue<PropertyChangeEvent> dq = new ArrayBlockingQueue<>(MAX_SIZE);
    final PropertyChangeListener listener = (PropertyChangeEvent e) -> {
        log.trace(" handling event {}", e);
        boolean success = dq.offer(e);
        if (!success) {
            log.error("Could not process event {} from {} in {}", e.getPropertyName(), e.getSource(), dq);
        }
    };

    /**
     * Dispose by dropping the listeners to all the specified
     * {@link NamedBean}s. The object should not be used again after calling
     * this. It is not an error to call this multiple times.
     */
    public void dispose() {
        log.trace("dispose() {}", items);
        items.forEach((bean) -> {
            bean.removePropertyChangeListener(listener);
        });
    }

    public PropertyChangeEvent take() throws InterruptedException {
        return dq.take();
    }

    public PropertyChangeEvent poll(long timeout, TimeUnit unit) throws InterruptedException {
        return dq.poll(timeout, unit);
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer("PropertyChangeEventQueue for");
        items.stream().forEachOrdered((bean) -> {
            b.append(" (\"");
            b.append(bean.getDisplayName());
            b.append("\")");
        });
        return new String(b);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PropertyChangeEventQueue.class);
}
