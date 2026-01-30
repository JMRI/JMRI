package rand;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkedSignal {
    private static final Logger logger = LoggerFactory.getLogger(LinkedSignal.class);

    /**
     * The appearance of the signalHead with the source name will
     * be copied to the signalHead with the first target name. The
     * remaining target signals will be RED.
     * @param sourceName the name of the source signal head
     * @param targetNames the names of the target signal heads.
     */
    public LinkedSignal(String sourceName, String... targetNames) {
        SignalHead sourceSignalHead = InstanceManager.getDefault(SignalHeadManager.class)
                .getByUserName(sourceName);
        if (sourceSignalHead == null) {
            logger.error("could not find " + sourceName);
            return;
        }
        SignalHead[] targetSignalHead;
        targetSignalHead = new SignalHead[targetNames.length];
        for (int i = 0; i < targetNames.length; i++) {
            targetSignalHead[i] = InstanceManager.getDefault(SignalHeadManager.class)
                    .getByUserName(targetNames[i]);
            if (targetSignalHead[i] == null) {
                logger.error("could not find " + targetNames[i]);
                return;
            }
        }
        for (SignalHead signalHead : targetSignalHead)
            signalHead.setAppearance(SignalHead.RED);

        sourceSignalHead.addPropertyChangeListener(evt -> {
            int value = sourceSignalHead.getAppearance();
            targetSignalHead[0].setAppearance(value);
            for (int i = 1; i < targetSignalHead.length; i++)
                targetSignalHead[i].setAppearance(SignalHead.RED);
        });
    }
}
