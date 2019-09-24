package jmri.jmrit.logixng.digital.implementation;

import jmri.jmrit.logixng.digital.implementation.DefaultMaleDigitalActionWithChangeSocket;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.MaleSocketTestBase;
import jmri.jmrit.logixng.digital.actions_with_change.AbstractDigitalActionWithChange;
import jmri.jmrit.logixng.digital.actions_with_change.OnChangeAction;
import jmri.jmrit.logixng.DigitalActionWithChangeManager;
import jmri.jmrit.logixng.DigitalActionWithChangeBean;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultMaleDigitalActionWithChangeSocketTest extends MaleSocketTestBase{

    @Override
    protected String getNewSystemName() {
        return InstanceManager.getDefault(DigitalActionWithChangeManager.class)
                .getNewSystemName();
    }
    
    @Test
    public void testCtor() {
        DigitalActionWithChangeBean action = new OnChangeAction("IQDC321", null, OnChangeAction.ChangeType.CHANGE);
        Assert.assertNotNull("exists", new DefaultMaleDigitalActionWithChangeSocket(action));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        DigitalActionWithChangeBean actionA = new OnChangeAction("IQDC321", null, OnChangeAction.ChangeType.CHANGE);
        Assert.assertNotNull("exists", actionA);
        DigitalActionWithChangeBean actionB = new MyDigitalActionWithChange("IQDC322");
        Assert.assertNotNull("exists", actionA);
        
        maleSocketA =
                InstanceManager.getDefault(DigitalActionWithChangeManager.class)
                        .registerAction(actionA);
        Assert.assertNotNull("exists", maleSocketA);
        
        maleSocketB =
                InstanceManager.getDefault(DigitalActionWithChangeManager.class)
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
    private class MyDigitalActionWithChange extends AbstractDigitalActionWithChange {
        
        MyDigitalActionWithChange(String sysName) {
            super(sysName);
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
        public Base getNewObjectBasedOnTemplate() {
            throw new UnsupportedOperationException("Not supported.");
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
            return Category.COMMON;
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
