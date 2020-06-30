package jmri.jmrit.logixng.digital.implementation;

import java.util.Locale;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.DigitalBooleanActionBean;
import jmri.jmrit.logixng.DigitalBooleanActionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.MaleSocketTestBase;
import jmri.jmrit.logixng.digital.boolean_actions.AbstractDigitalBooleanAction;
import jmri.jmrit.logixng.digital.boolean_actions.OnChange;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultMaleDigitalBooleanActionSocketTest extends MaleSocketTestBase{

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(DigitalBooleanActionManager.class)
                .getAutoSystemName();
    }
    
    @Test
    public void testCtor() {
        DigitalBooleanActionBean action = new OnChange("IQDB321", null, OnChange.ChangeType.CHANGE);
        Assert.assertNotNull("exists", new DefaultMaleDigitalBooleanActionSocket(action));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        DigitalBooleanActionBean actionA = new OnChange("IQDB321", null, OnChange.ChangeType.CHANGE);
        Assert.assertNotNull("exists", actionA);
        DigitalBooleanActionBean actionB = new MyDigitalBooleanAction("IQDB322");
        Assert.assertNotNull("exists", actionA);
        
        maleSocketA =
                InstanceManager.getDefault(DigitalBooleanActionManager.class)
                        .registerAction(actionA);
        Assert.assertNotNull("exists", maleSocketA);
        
        maleSocketB =
                InstanceManager.getDefault(DigitalBooleanActionManager.class)
                        .registerAction(actionB);
        Assert.assertNotNull("exists", maleSocketA);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    /**
     * This action is different from MyStringAction and is used to test the
     * male socket.
     */
    private class MyDigitalBooleanAction extends AbstractDigitalBooleanAction {
        
        MyDigitalBooleanAction(String sysName) {
            super(sysName, null);
        }

        @Override
        protected void registerListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected void unregisterListenersForThisClass() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected void disposeMe() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getShortDescription(Locale locale) {
            return "My short description";
        }

        @Override
        public String getLongDescription(Locale locale) {
            return "My long description";
        }

        @Override
        public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getChildCount() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Category getCategory() {
            return Category.OTHER;
        }

        @Override
        public boolean isExternal() {
            return false;
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void execute(boolean hasChangedToTrue) {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
    
}
