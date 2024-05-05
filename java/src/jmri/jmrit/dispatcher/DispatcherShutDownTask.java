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
    public Boolean call() {
        DispatcherFrame df = InstanceManager.getDefault(DispatcherFrame.class);
        var atList = Collections.unmodifiableCollection(df.getActiveTrainsList());
        for (ActiveTrain at: atList) {
            if (at.getAutoActiveTrain() != null) {
                at.getAutoActiveTrain().initiateWorking();
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
    }

    private static final Logger log = LoggerFactory.getLogger(DispatcherShutDownTask.class);

}
