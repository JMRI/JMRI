package rand;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.implementation.DccSignalHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Manages resends of signal requests.
 * The NCE light-it boards have an issue where they go dumb for
 * a short period of time after handling any message. This means
 * normal DCC repeats don't work. It also means the light-it
 * can miss a message because a previous messsage for another device
 * made it go dumb.
 */

public class RefreshSignals extends Thread implements PropertyChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(RefreshSignals.class);


    private static final ConcurrentLinkedQueue<SignalHead> resendQueue = new ConcurrentLinkedQueue<>();
    private static final int resendTime = getIntProperty("rand.resendTime", 50);
    private static final int refreshTime = getIntProperty("rand.refreshTime", 10 * 1000);
    private static final int repeatCount = getIntProperty("rand.repeatCount", 3);

    @Override
    public void run() {
        try {
            logger.info("rand.resendTime=" + resendTime);
            logger.info("rand.refreshTime=" + refreshTime);
            logger.info("rand.repeatCount=" + repeatCount);
            SignalHeadManager manager = InstanceManager.getDefault(SignalHeadManager.class);
            List<SignalHead> signalHeads = manager.getNamedBeanSet().stream()
                    .filter(h -> h instanceof DccSignalHead)
                    .peek(h -> h.addPropertyChangeListener(this))
                    .collect(Collectors.toList());
            logger.info(String.format("Refreshing %d signals.", signalHeads.size()));
            int current = 0;

            long fullRefreshTime = System.currentTimeMillis();

            for (;;) {
                // do things slowly so that new commands can be sent in a timely manner.
                try {
                    //noinspection BusyWait
                    Thread.sleep(resendTime);
                } catch (InterruptedException e) {
                    break;
                }

                // resend any recent messages.
                SignalHead signalHead = resendQueue.poll();
                if (signalHead == null) {
                    // If nothing needs a resend, refresh everything once a minute.
                    long now = System.currentTimeMillis();
                    if (now > fullRefreshTime) {
                        if (current >= signalHeads.size()) {
                            fullRefreshTime = now + refreshTime;
                            current = 0;
                        }
                        signalHead = signalHeads.get(current++);
                        if (((DccSignalHead)signalHead).getDccSignalHeadPacketSendCount() > 1) {
                            logger.info("Refreshing " + signalHead.getUserName());
                        }
                    }
                }
                if (signalHead != null) {
                    signalHead.updateOutput();
                }
            }
        } catch (Exception e) {
            logger.error("Refresh exiting: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private static int getIntProperty(String propertyName, int defaultValue) {
        String text = System.getProperty(propertyName);
        int refreshTime;
        if (text != null) {
            refreshTime = Integer.parseInt(text);
        }
        else {
            refreshTime = defaultValue;
        }
        return refreshTime;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        for (int i = 0; i < repeatCount; i++) {
            DccSignalHead source = (DccSignalHead) evt.getSource();
            if (source.getDccSignalHeadPacketSendCount() > 1) {
                logger.info(String.format("Queueing %s", source.getUserName()));
            }
            resendQueue.offer(source);
        }
    }

}
