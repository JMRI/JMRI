package jmri.jmrit.logix;

import jmri.Throttle;

/**
 * A JInternalFrame that contains buttons for each decoder function.
 *
 * @author Pete Cressman Copyright 2020
 */
public class LearnFunctionPanel extends jmri.jmrit.throttle.FunctionPanel {

    private final LearnThrottleFrame _throttleFrame;

    LearnFunctionPanel(LearnThrottleFrame learnFrame) {
        super();
        _throttleFrame = learnFrame;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        super.propertyChange(e);

        String functionName = e.getPropertyName();
        if (!functionName.startsWith("F")) {
            return;
        }
        boolean isSet = ((Boolean) e.getNewValue());
        
        for ( int i = 0; i< 29; i++ ) {
            if (functionName.equals(Throttle.getFunctionString(i))) {
                _throttleFrame.setFunctionState(functionName, isSet);
            } else if (functionName.equals(Throttle.getFunctionMomentaryString(i))) {
                functionName = "Lock" + Throttle.getFunctionString(i);
                _throttleFrame.setFunctionLock(functionName, isSet);
             }
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(LearnFunctionPanel.class);
}
