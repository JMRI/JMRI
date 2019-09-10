package jmri.jmrit.logixng;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmrit.logixng.implementation.AbstractBase;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test AbstractAnalogExpression
 * 
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractBaseTestBase {

    public final String TREE_INDENT = "   ";
    protected Base _base;
    
    /**
     * Returns the LogixNG for _base.
     * @return the LogixNG for _base or null if _base doesn't have any LogixNG
     */
    public abstract ConditionalNG getConditionalNG();
    
    @Test
    public void testGetConditionalNG() {
//        if (getConditionalNG() == null) {
//            log.warn("Method getConditionalNG() returns null for class {}", this.getClass().getName());
//        }
        Assert.assertTrue("ConditionalNG is equal", getConditionalNG() == _base.getConditionalNG());
    }
    
    /**
     * Returns the LogixNG for _base.
     * @return the LogixNG for _base or null if _base doesn't have any LogixNG
     */
    public abstract LogixNG getLogixNG();
    
    @Test
    public void testGetLogixNG() {
//        if (getLogixNG() == null) {
//            log.warn("Method getLogixNG() returns null for class {}", this.getClass().getName());
//        }
        Assert.assertTrue("LogixNG is equal", getLogixNG() == _base.getLogixNG());
    }
    
    /**
     * Returns the expected result of _base.printTree(writer, TREE_INDENT)
     * @return the expected printed tree
     */
    public abstract String getExpectedPrintedTree();
    
    @Test
    public void testGetPrintTree() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        _base.printTree(Locale.ENGLISH, printWriter, TREE_INDENT);
        Assert.assertEquals("Tree is equal", getExpectedPrintedTree(), stringWriter.toString());
    }
    
    /**
     * Returns the expected result of _base.getRoot().printTree(writer, TREE_INDENT)
     * @return the expected printed tree
     */
    public abstract String getExpectedPrintedTreeFromRoot();
    
    @Test
    public void testGetPrintTreeFromRoot() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        _base.getRoot().printTree(Locale.ENGLISH, printWriter, TREE_INDENT);
        Assert.assertEquals("Tree is equal", getExpectedPrintedTreeFromRoot(), stringWriter.toString());
    }
    
    @Test
    public void testLock() {
        _base.setLock(Base.Lock.NONE);
        Assert.assertTrue("Enum matches", Base.Lock.NONE == _base.getLock());
        Assert.assertTrue("String matches", "No lock".equals(_base.getLock().toString()));
        Assert.assertTrue("isChangeableByUser", _base.getLock().isChangeableByUser());
        _base.setLock(Base.Lock.USER_LOCK);
        Assert.assertTrue("Enum matches", Base.Lock.USER_LOCK == _base.getLock());
        Assert.assertTrue("String matches", "User lock".equals(_base.getLock().toString()));
        Assert.assertTrue("isChangeableByUser", _base.getLock().isChangeableByUser());
        _base.setLock(Base.Lock.HARD_LOCK);
        Assert.assertTrue("Enum matches", Base.Lock.HARD_LOCK == _base.getLock());
        Assert.assertTrue("String matches", "Hard lock".equals(_base.getLock().toString()));
        Assert.assertFalse("isChangeableByUser", _base.getLock().isChangeableByUser());
        _base.setLock(Base.Lock.TEMPLATE_LOCK);
        Assert.assertTrue("Enum matches", Base.Lock.TEMPLATE_LOCK == _base.getLock());
        Assert.assertTrue("String matches", "Template lock".equals(_base.getLock().toString()));
        Assert.assertFalse("isChangeableByUser", _base.getLock().isChangeableByUser());
    }
    
    @Test
    public void testConstants() {
        Assert.assertTrue("String matches", "ChildCount".equals(Base.PROPERTY_CHILD_COUNT));
        Assert.assertTrue("String matches", "SocketConnected".equals(Base.PROPERTY_SOCKET_CONNECTED));
        Assert.assertEquals("integer matches", 0x02, Base.SOCKET_CONNECTED);
        Assert.assertEquals("integer matches", 0x04, Base.SOCKET_DISCONNECTED);
    }
    
    @Test
    public void testNames() {
        Assert.assertNotNull("system name not null", _base.getSystemName());
        Assert.assertFalse("system name is not empty string", _base.getSystemName().isEmpty());
        
        _base.setUserName("One user name");
        Assert.assertTrue("User name matches", "One user name".equals(_base.getUserName()));
        _base.setUserName("Another user name");
        Assert.assertTrue("User name matches", "Another user name".equals(_base.getUserName()));
        _base.setUserName(null);
        Assert.assertNull("User name matches", _base.getUserName());
        _base.setUserName("One user name");
        Assert.assertTrue("User name matches", "One user name".equals(_base.getUserName()));
    }
    
    @Test
    public void testParent() {
        MyBase a = new MyBase();
        _base.setParent(null);
        Assert.assertNull("Parent matches", _base.getParent());
        _base.setParent(a);
        Assert.assertTrue("Parent matches", a == _base.getParent());
        _base.setParent(null);
        Assert.assertNull("Parent matches", _base.getParent());
    }
    
    @Test
    public void testIsEnabled() {
        MyBase a = new MyBase();
        Assert.assertTrue("isEnabled() returns true by default", a.isEnabled());
    }
    
    @Test
    public void testDispose() {
        _base.dispose();
    }
    
    
    private class MyBase extends AbstractBase {

        private MyBase() {
            super("IQ1");
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
        public void setState(int s) throws JmriException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getBeanType() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getShortDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getLongDescription(Locale locale) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Base getNewObjectBasedOnTemplate(String sys) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Base getParent() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setParent(Base parent) {
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
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean isExternal() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Lock getLock() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setLock(Lock lock) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setup() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }

//    private final static Logger log = LoggerFactory.getLogger(AbstractBaseTestBase.class);

}
