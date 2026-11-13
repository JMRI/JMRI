package jmri.jmrix.can.cbus;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;

/**
 * Delivers the CBUS frames of one traffic controller to the sensors, turnouts
 * and lights that can act on them.
 * <p>
 * Each of these beans listens for the frames carrying its own events. If every
 * bean were a listener of the traffic controller, every frame sent or received
 * would be offered to every bean on the connection, so the cost of each frame
 * would grow with the size of the layout. Instead, one dispatcher per traffic
 * controller listens, and keeps the beans in an index. A frame is offered only
 * to the beans filed under its key: its length and its bytes from element 3
 * on. Every way a bean recognises a frame, {@link CbusAddress#match} for
 * events and response events and {@link CbusAddress#matchRequest} for
 * requests, needs the frame's length and those bytes to equal the bean's, so
 * no bean that could act on a frame is left out. Each bean still makes its own
 * exact check, in its {@code reply} and {@code message} methods, which are
 * unchanged.
 * <p>
 * The traffic controller leaves the sender of an outgoing frame out of that
 * frame's notification. The beans send through {@link #send}, which notes
 * which bean sent each frame until the frame is notified, so a bean still never
 * hears its own frames.
 * <p>
 * There is one dispatcher for each traffic controller. It adds itself to the
 * traffic controller's listeners when its first bean is added, and removes
 * itself when its last bean is removed. Beans are added and removed on any
 * thread; frames are delivered on the thread the traffic controller notifies
 * them on, as before.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class CbusBeanDispatcher implements CanListener {

    /**
     * The key of an address that can match no frame.
     */
    static final long NO_KEY = -1;

    // Weak keys, so a dispatcher goes when its traffic controller goes. The
    // dispatcher holds its traffic controller only weakly, so the entry can
    // be cleared.
    private static final Map<TrafficController, CbusBeanDispatcher> DISPATCHERS
            = Collections.synchronizedMap(new WeakHashMap<>());

    private final WeakReference<TrafficController> tc;

    // key -> the beans filed under it, in the order they were added
    private final Map<Long, List<CanListener>> byKey = new ConcurrentHashMap<>();

    // bean -> its keys; guarded by this
    private final Map<CanListener, long[]> keysOf = new HashMap<>();

    // outgoing frame (by identity) -> the bean that sent it, until notified
    private final Map<CanMessage, CanListener> sentBy
            = Collections.synchronizedMap(new IdentityHashMap<>());

    private CbusBeanDispatcher(TrafficController tc) {
        this.tc = new WeakReference<>(tc);
    }

    /**
     * Start delivering a bean the frames of its addresses.
     *
     * @param tc        the traffic controller the bean uses; nothing is done
     *                  if null
     * @param bean      the bean
     * @param addresses the bean's addresses; null or invalid addresses are
     *                  ignored
     */
    static void add(@CheckForNull TrafficController tc, @Nonnull CanListener bean,
            @Nonnull CbusAddress... addresses) {
        if (tc != null) {
            DISPATCHERS.computeIfAbsent(tc, CbusBeanDispatcher::new).addBean(tc, bean, addresses);
        }
    }

    /**
     * Stop delivering frames to a bean.
     *
     * @param tc   the traffic controller the bean uses; nothing is done if
     *             null
     * @param bean the bean
     */
    static void remove(@CheckForNull TrafficController tc, @Nonnull CanListener bean) {
        if (tc != null) {
            CbusBeanDispatcher d = DISPATCHERS.get(tc);
            if (d != null) {
                d.removeBean(tc, bean);
            }
        }
    }

    /**
     * Send a frame for a bean, leaving the bean out of the frame's
     * notification as the traffic controller does for the sender.
     *
     * @param tc     the traffic controller
     * @param m      the frame
     * @param sender the bean sending it
     */
    static void send(@Nonnull TrafficController tc, @Nonnull CanMessage m, @Nonnull CanListener sender) {
        CbusBeanDispatcher d = DISPATCHERS.get(tc);
        if (d != null) {
            d.noteSender(m, sender);
        }
        tc.sendCanMessage(m, sender);
    }

    /**
     * The key a frame of these bytes is filed under: the length and the bytes
     * from element 3 on.
     *
     * @param frame the frame's bytes, or null
     * @return the key, or {@link #NO_KEY} if null
     */
    static long key(@CheckForNull int[] frame) {
        if (frame == null) {
            return NO_KEY;
        }
        long k = frame.length & 0xff;
        for (int i = 3; i < Math.min(frame.length, 8); i++) {
            k = (k << 8) | (frame[i] & 0xff);
        }
        return k;
    }

    /**
     * The key of a frame, as {@link #key(int[])}.
     *
     * @param m the frame
     * @return the key
     */
    static long key(@Nonnull AbstractMessage m) {
        int n = m.getNumDataElements();
        long k = n & 0xff;
        for (int i = 3; i < Math.min(n, 8); i++) {
            k = (k << 8) | (m.getElement(i) & 0xff);
        }
        return k;
    }

    private synchronized void addBean(TrafficController t, CanListener bean, CbusAddress[] addresses) {
        if (keysOf.containsKey(bean)) {
            return;
        }
        long[] keys = new long[addresses.length];
        int n = 0;
        for (CbusAddress a : addresses) {
            long k = (a == null) ? NO_KEY : a.dispatchKey();
            boolean seen = false;
            for (int i = 0; i < n; i++) {
                seen |= keys[i] == k;
            }
            if (k != NO_KEY && !seen) {
                keys[n++] = k;
            }
        }
        if (n == 0) {
            return; // can match nothing
        }
        keys = java.util.Arrays.copyOf(keys, n);
        for (long k : keys) {
            byKey.computeIfAbsent(k, x -> new CopyOnWriteArrayList<>()).add(bean);
        }
        keysOf.put(bean, keys);
        if (keysOf.size() == 1) {
            t.addCanListener(this);
        }
    }

    private synchronized void removeBean(TrafficController t, CanListener bean) {
        long[] keys = keysOf.remove(bean);
        if (keys == null) {
            return;
        }
        for (long k : keys) {
            List<CanListener> beans = byKey.get(k);
            if (beans != null) {
                beans.remove(bean);
                if (beans.isEmpty()) {
                    byKey.remove(k);
                }
            }
        }
        if (keysOf.isEmpty()) {
            t.removeCanListener(this);
            sentBy.clear();
        }
    }

    private synchronized void noteSender(CanMessage m, CanListener sender) {
        // only a bean of this dispatcher can be skipped, and only then is the
        // dispatcher listening, so that the note is removed when notified
        if (keysOf.containsKey(sender)) {
            sentBy.put(m, sender);
        }
    }

    /**
     * Offer an outgoing frame to the beans filed under its key, except the
     * bean that sent it.
     *
     * @param m the frame
     */
    @Override
    public void message(CanMessage m) {
        CanListener sender = sentBy.remove(m);
        if (m.extendedOrRtr()) {
            return;
        }
        List<CanListener> beans = byKey.get(key(m));
        if (beans == null) {
            return;
        }
        for (CanListener bean : beans) {
            if (bean != sender) {
                try {
                    bean.message(m);
                } catch (RuntimeException e) {
                    log.warn("During message dispatch to {}", bean, e);
                }
            }
        }
    }

    /**
     * Offer an incoming frame to the beans filed under its key.
     *
     * @param r the frame
     */
    @Override
    public void reply(CanReply r) {
        if (r.extendedOrRtr()) {
            return;
        }
        List<CanListener> beans = byKey.get(key(r));
        if (beans == null) {
            return;
        }
        for (CanListener bean : beans) {
            try {
                bean.reply(r);
            } catch (RuntimeException e) {
                log.warn("During reply dispatch to {}", bean, e);
            }
        }
    }

    /**
     * @return the number of beans frames are delivered to
     */
    synchronized int numBeans() {
        return keysOf.size();
    }

    /**
     * @param tc a traffic controller
     * @return its dispatcher, or null if it has never had a bean
     */
    @CheckForNull
    static CbusBeanDispatcher forTrafficController(@Nonnull TrafficController tc) {
        return DISPATCHERS.get(tc);
    }

    @Override
    public String toString() {
        return "CbusBeanDispatcher for " + tc.get();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusBeanDispatcher.class);

}
