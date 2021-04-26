package jmri.jmrit.logixng.tools;

import jmri.*;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.entryexit.PointDetails;
import jmri.jmrit.entryexit.Source;
import jmri.util.JUnitAppender;

import org.junit.Test;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 This test tests expression dp
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportExpressionEntryExitTest extends ImportExpressionTestBase {

    MyDestinationPoints dp;
    ConditionalVariable cv;
    
    @Override
    public void setNamedBeanState(State state) throws JmriException {
        switch (state) {
            case ON:
                dp.setActiveEntryExit(true);
                break;
                
            case OFF:
                dp.setActiveEntryExit(false);
                break;
                
            case OTHER:
            default:
                dp.setState(NamedBean.UNKNOWN);
                break;
        }
    }

    @Override
    public void setConditionalVariableState(State state) {
        switch (state) {
            case ON:
                cv.setType(Conditional.Type.ENTRYEXIT_ACTIVE);
                break;
                
            case OFF:
            case OTHER:
            default:
                cv.setType(Conditional.Type.ENTRYEXIT_INACTIVE);
                break;
        }
    }

    @Override
    public ConditionalVariable newConditionalVariable() {
        InstanceManager.setDefault(EntryExitPairs.class, new MyEntryExitPairs());
        dp = (MyDestinationPoints) InstanceManager.getDefault(EntryExitPairs.class).getBySystemName("DP1");
        cv = new ConditionalVariable();
        cv.setName("DP1");
        return cv;
    }
    
    @Override
    public void tearDown() {
        JUnitAppender.assertErrorMessage("Signal not found at point");
        JUnitAppender.assertErrorMessage("Signal not found at point");
        super.tearDown();
    }
    
    
    private static class MyDestinationPoints extends DestinationPoints {
        MyDestinationPoints(PointDetails point, String id, Source src) {
            super(point, id, src);
        }
        
        @Override
        public void setActiveEntryExit(boolean boo) {
            super.setActiveEntryExit(boo);
        }
    }
    
    
    private static class MyPointDetails extends PointDetails {
        MyPointDetails() {
            super(null, null);
        }
        
        @Override
        public String getDisplayName() {
            return "DisplayName";
        }
    }
    
    
    private static class MyEntryExitPairs extends EntryExitPairs {
        PointDetails point = new MyPointDetails();
        Source src = new Source(point);
        MyDestinationPoints dp = new MyDestinationPoints(new MyPointDetails(), "DP1", src);
        
        @Override
        public DestinationPoints getBySystemName(String systemName) {
            return dp;
        }
    }
}
