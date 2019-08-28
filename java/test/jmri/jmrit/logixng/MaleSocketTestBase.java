package jmri.jmrit.logixng;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.Debugable.DebugConfig;
import jmri.util.JUnitAppender;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Base class for test classes that tests MaleSockets
 */
public abstract class MaleSocketTestBase {

    protected MaleSocket maleSocketA;
    protected MaleSocket maleSocketB;
    
    protected abstract String getNewSystemName();
    
    @Test
    public void testMaleSocket() throws JmriException {
        Assert.assertNotNull("exists", maleSocketA);
        Assert.assertNotNull("exists", maleSocketB);
        Assert.assertNotEquals("not equals", maleSocketA, maleSocketB);
        
        Assert.assertNotNull("get object based on template is not null",
                maleSocketA.getNewObjectBasedOnTemplate(getNewSystemName()));
        
        maleSocketA.setLock(Base.Lock.NONE);
        Assert.assertTrue("lock is correct",
                Base.Lock.NONE == maleSocketA.getLock());
        maleSocketA.setLock(Base.Lock.USER_LOCK);
        Assert.assertTrue("lock is correct",
                Base.Lock.USER_LOCK == maleSocketA.getLock());
        
        Assert.assertEquals("category is correct",
                maleSocketA.getObject().getCategory(), maleSocketA.getCategory());
        Assert.assertEquals("category is correct",
                maleSocketB.getObject().getCategory(), maleSocketB.getCategory());
        Assert.assertNotEquals("categories are different",
                maleSocketA.getCategory(), maleSocketB.getCategory());
        
        Assert.assertEquals("isExternal() is correct",
                maleSocketA.getObject().isExternal(), maleSocketA.isExternal());
        Assert.assertEquals("isExternal() is correct",
                maleSocketB.getObject().isExternal(), maleSocketB.isExternal());
        Assert.assertNotEquals("isExternal() are different",
                maleSocketA.isExternal(), maleSocketB.isExternal());
        
        Assert.assertEquals("getShortDescription() is correct",
                maleSocketA.getObject().getShortDescription(), maleSocketA.getShortDescription());
        Assert.assertEquals("getShortDescription() is correct",
                maleSocketB.getObject().getShortDescription(), maleSocketB.getShortDescription());
        Assert.assertNotEquals("getShortDescription() are different",
                maleSocketA.getShortDescription(), maleSocketB.getShortDescription());
        
        Assert.assertEquals("getLongDescription() is correct",
                maleSocketA.getObject().getLongDescription(), maleSocketA.getLongDescription());
        Assert.assertEquals("getLongDescription() is correct",
                maleSocketB.getObject().getLongDescription(), maleSocketB.getLongDescription());
        Assert.assertNotEquals("getLongDescription() are different",
                maleSocketA.getLongDescription(), maleSocketB.getLongDescription());
        
        Assert.assertEquals("getSystemName() is correct",
                maleSocketA.getObject().getSystemName(), maleSocketA.getSystemName());
        Assert.assertEquals("getSystemName() is correct",
                maleSocketB.getObject().getSystemName(), maleSocketB.getSystemName());
        Assert.assertNotEquals("getSystemName() are different",
                maleSocketA.getSystemName(), maleSocketB.getSystemName());
        
        maleSocketA.setUserName("Test username Abc");
        Assert.assertEquals("getUserName() is correct",
                maleSocketA.getObject().getUserName(), maleSocketA.getUserName());
        Assert.assertEquals("getUserName() is correct",
                maleSocketB.getObject().getUserName(), maleSocketB.getUserName());
        Assert.assertNotEquals("getUserName() are different",
                maleSocketA.getUserName(), maleSocketB.getUserName());
        
        maleSocketA.getObject().setUserName("Abc");
        Assert.assertEquals("getUserName() is correct",
                "Abc", maleSocketA.getUserName());
        
        maleSocketA.getObject().setUserName("Def");
        Assert.assertEquals("getUserName() is correct",
                "Def", maleSocketA.getUserName());
        
        Assert.assertEquals("getDisplayName() is correct",
                ((NamedBean)maleSocketA.getObject()).getDisplayName(), ((NamedBean)maleSocketA).getDisplayName());
        Assert.assertEquals("getDisplayName() is correct",
                ((NamedBean)maleSocketB.getObject()).getDisplayName(), ((NamedBean)maleSocketB).getDisplayName());
        Assert.assertNotEquals("getDisplayName() are different",
                ((NamedBean)maleSocketA).getDisplayName(), ((NamedBean)maleSocketB).getDisplayName());
        
        ((NamedBean)maleSocketA).setState(NamedBean.UNKNOWN);
        Assert.assertEquals("getState() is correct",
                NamedBean.UNKNOWN, ((NamedBean)maleSocketA).getState());
        JUnitAppender.assertWarnMessageStartsWith("Unexpected call to setState in ");
        JUnitAppender.assertWarnMessageStartsWith("Unexpected call to getState in ");
        
        ((NamedBean)maleSocketA).setState(NamedBean.INCONSISTENT);
        Assert.assertEquals("getState() is correct",
                NamedBean.INCONSISTENT, ((NamedBean)maleSocketA).getState());
        JUnitAppender.assertWarnMessageStartsWith("Unexpected call to setState in ");
        JUnitAppender.assertWarnMessageStartsWith("Unexpected call to getState in ");
        
        ((NamedBean)maleSocketA).setComment("Abc");
        Assert.assertEquals("getComment() is correct",
                "Abc", ((NamedBean)maleSocketA).getComment());
        
        ((NamedBean)maleSocketA).setComment("Def");
        Assert.assertEquals("getComment() is correct",
                "Def", ((NamedBean)maleSocketA).getComment());
    }
    
    @Test
    public void testProperty() {
        // Remove all properties to be sure we don't hit any problem later
        for (String property : ((NamedBean)maleSocketA).getPropertyKeys()) {
            ((NamedBean)maleSocketA).removeProperty(property);
        }
        
        // Test set property and read it
        ((NamedBean)maleSocketA).setProperty("Abc", "Something");
        Assert.assertEquals("getProperty() is correct",
                "Something", ((NamedBean)maleSocketA).getProperty("Abc"));
        // Test set property to something else and read it
        ((NamedBean)maleSocketA).setProperty("Abc", "Something else");
        Assert.assertEquals("getProperty() is correct",
                "Something else", ((NamedBean)maleSocketA).getProperty("Abc"));
        Assert.assertEquals("num properties", 1, ((NamedBean)maleSocketA).getPropertyKeys().size());
        // Test set property with another key and read it
        ((NamedBean)maleSocketA).setProperty("Def", "Something different");
        Assert.assertEquals("getProperty() is correct",
                "Something different", ((NamedBean)maleSocketA).getProperty("Def"));
        // Test that the previous key hasn't been changed
        Assert.assertEquals("getProperty() is correct",
                "Something else", ((NamedBean)maleSocketA).getProperty("Abc"));
        Assert.assertEquals("num properties", 2, ((NamedBean)maleSocketA).getPropertyKeys().size());
        // Test removing the property and read it
        ((NamedBean)maleSocketA).removeProperty("Abc");
        Assert.assertNull("getProperty() is null",
                ((NamedBean)maleSocketA).getProperty("Abc"));
        Assert.assertEquals("num properties", 1, ((NamedBean)maleSocketA).getPropertyKeys().size());
    }
    
    @Test
    public void testDebugConfig() {
        DebugConfig debugConfig = maleSocketA.createDebugConfig();
        Assert.assertNotNull("debugConfig is not null", debugConfig);
        maleSocketA.setDebugConfig(debugConfig);
        Assert.assertTrue("debugConfig correct",
                debugConfig == maleSocketA.getDebugConfig());
        maleSocketA.setDebugConfig(null);
        Assert.assertNull("debugConfig is null", maleSocketA.getDebugConfig());
    }
    
}
