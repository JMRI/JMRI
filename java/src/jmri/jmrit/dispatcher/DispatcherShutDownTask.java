package jmri.jmrit.dispatcher;

import java.util.Collections;
import jmri.InstanceManager;
import jmri.implementation.AbstractShutDownTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatcherShutDownTask extends AbstractShutDownTask {

    public DispatcherShutDownTask(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            DispatcherFrame df = InstanceManager.getDefault(DispatcherFrame.class);
            var atList = Collections.unmodifiableCollection(df.getActiveTrainsList());
            for (ActiveTrain at : atList) {
                if (at.getAutoActiveTrain() != null) {
                    at.getAutoActiveTrain().initiateWorking();
                }
            }
        } catch (Exception ex) {
            log.error("Failed to stop all autotrains", ex);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(DispatcherShutDownTask.class);

}
