package jmri.jmrit.logixng;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.CheckForNull;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.implementation.AbstractBase;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
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
    protected MaleSocket _baseMaleSocket;
    protected Category _category;
    protected boolean _isExternal;
    
    
    /**
     * Returns the LogixNG for _base.
     * @return the LogixNG for _base or null if _base doesn't have any LogixNG
     */
    public abstract ConditionalNG getConditionalNG();
    
    /**
     * Returns a MaleSocket that can be connected to _base.getChild(0).
     * If _base cannot have any children, this method returns null.
     * @return a male socket or null
     */
    @CheckForNull
    public abstract MaleSocket getConnectableChild();
    
    @Test
    public void testGetConditionalNG() {
        if (getConditionalNG() == null) {
            log.warn("Method getConditionalNG() returns null for class {}", this.getClass().getName());
            log.error("Method getConditionalNG() returns null for class {}", this.getClass().getName());
        }
        Assert.assertTrue("ConditionalNG is equal", getConditionalNG() == _base.getConditionalNG());
        
        _base.getConditionalNG().setEnabled(false);
        _base.setParent(null);
        Assert.assertNull("ConditionalNG is null", _base.getConditionalNG());
    }
    
    /**
     * Returns the LogixNG for _base.
     * @return the LogixNG for _base or null if _base doesn't have any LogixNG
     */
    public abstract LogixNG getLogixNG();
    
    @Test
    public void testGetLogixNG() {
        if (getLogixNG() == null) {
            log.warn("Method getLogixNG() returns null for class {}", this.getClass().getName());
        }
        Assert.assertTrue("LogixNG is equal", getLogixNG() == _base.getLogixNG());
        
        _base.getConditionalNG().setEnabled(false);
        _base.setParent(null);
        Assert.assertNull("LogixNG is null", _base.getLogixNG());
    }
    
    @Test
    public void testMaleSocketGetConditionalNG() {
        Assert.assertTrue("conditionalNG is equal",
                _base.getConditionalNG() == _baseMaleSocket.getConditionalNG());
        _base.getConditionalNG().setEnabled(false);
        _base.setParent(null);
        Assert.assertTrue("conditionalNG is equal",
                _base.getConditionalNG() == _baseMaleSocket.getConditionalNG());
    }
    
    @Test
    public void testMaleSocketGetLogixNG() {
        Assert.assertTrue("logixNG is equal",
                _base.getLogixNG() == _baseMaleSocket.getLogixNG());
        _base.getConditionalNG().setEnabled(false);
        _base.setParent(null);
        Assert.assertTrue("logixNG is equal",
                _base.getLogixNG() == _baseMaleSocket.getLogixNG());
    }
    
    @Test
    public void testMaleSocketGetRoot() {
        Assert.assertTrue("root is equal", _base.getRoot() == _baseMaleSocket.getRoot());
        _base.getConditionalNG().setEnabled(false);
        _base.setParent(null);
        Assert.assertTrue("root is equal", _base.getRoot() == _baseMaleSocket.getRoot());
    }
    
    @Test
    public void testGetParent() {
        if (_base.getParent() != _baseMaleSocket.getParent()) {
            log.error("Invalid parent for objects: {}, {}", _base, _baseMaleSocket);
        }
        Assert.assertTrue("parent is equal", _base.getParent() == _baseMaleSocket.getParent());
        _base.getConditionalNG().setEnabled(false);
        _base.setParent(null);
        Assert.assertTrue("parent is equal", _base.getParent() == _baseMaleSocket.getParent());
    }
    
    /**
     * Set parent to null for all children to item, and their children.
     */
    private void clearParent(Base item) {
        for (int i=0; i < item.getChildCount(); i++) {
            FemaleSocket femaleSocket = item.getChild(i);
            femaleSocket.setParent(null);
            
            if (femaleSocket.isConnected()) {
                clearParent(femaleSocket.getConnectedSocket());
            }
        }
    }
    
    /**
     * Check that parent is correct for all children to item, and their children.
     */
    private void checkParent(Base item) {
        for (int i=0; i < item.getChildCount(); i++) {
            FemaleSocket femaleSocket = item.getChild(i);
            if (item != femaleSocket.getParent()) {
                log.error("item: {}, {} - parent: {}, {}", item, item.getClass().getName(), femaleSocket.getParent(), femaleSocket.getParent().getClass().getName());
            }
            Assert.assertTrue("parent is correct", item == femaleSocket.getParent());
            
            if (femaleSocket.isConnected()) {
                MaleSocket connectedSocket = femaleSocket.getConnectedSocket();
                
                if (femaleSocket != connectedSocket.getParent()) {
                    log.error("femaleSocket: {}, {} - parent: {}, {} - child: {}, {}", femaleSocket, femaleSocket.getClass().getName(), connectedSocket.getParent(), connectedSocket.getParent().getClass().getName(), connectedSocket, connectedSocket.getClass().getName());
                }
                Assert.assertTrue("parent is correct", femaleSocket == connectedSocket.getParent());
                checkParent(connectedSocket);
            }
        }
    }
    
    /**
     * Test that the method setParentForAllChildren() works when there are
     * connected children.
     */
    @Test
    public void testSetParentForAllChildren_WithConnectedChildren() {
        _base.getConditionalNG().setEnabled(false);
        clearParent(_base);
        _base.setParentForAllChildren();
        checkParent(_base);
    }
    
    /**
     * Test that the method setParentForAllChildren() works when there are
     * no connected children.
     */
    @Test
    public void testSetParentForAllChildren_WithoutConnectedChildren() {
        _base.getConditionalNG().setEnabled(false);
        clearParent(_base);
        for (int i=0; i < _base.getChildCount(); i++) {
            FemaleSocket femaleSocket = _base.getChild(i);
            femaleSocket.disconnect();
        }
        _base.setParentForAllChildren();
        checkParent(_base);
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
    
    @Test
    public void testMaleSocketGetPrintTree() {
        /// Test that the male socket of the item prints the same tree
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        _baseMaleSocket.printTree(Locale.ENGLISH, printWriter, TREE_INDENT);
        Assert.assertEquals("Tree is equal", getExpectedPrintedTree(), stringWriter.toString());
    }
    
    @Test
    public void testGetPrintTreeWithStandardLocale() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        _base.printTree(printWriter, TREE_INDENT);
        Assert.assertEquals("Tree is equal", getExpectedPrintedTree(), stringWriter.toString());
    }
    
    @Test
    public void testMaleSocketGetPrintTreeWithStandardLocale() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        _baseMaleSocket.printTree(printWriter, TREE_INDENT);
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
    public void testIsActive() {
        Assert.assertTrue(_base.isActive());
        if (_base instanceof MaleSocket) {
            ((MaleSocket)_base).setEnabled(false);
            Assert.assertFalse("_base is not active", _base.isActive());
            ((MaleSocket)_base).setEnabled(true);
        } else if (_base.getParent() instanceof MaleSocket) {
            ((MaleSocket)_base.getParent()).setEnabled(false);
            Assert.assertFalse("_base is not active", _base.isActive());
            ((MaleSocket)_base.getParent()).setEnabled(true);
        }
        
        Assert.assertTrue(_base.isActive());
        ConditionalNG conditionalNG = _base.getConditionalNG();
        if (conditionalNG != null) {
            conditionalNG.setEnabled(false);
            Assert.assertFalse("_base is not active", _base.isActive());
            conditionalNG.setEnabled(true);
        } else {
            log.error("_base has no ConditionalNG as ancestor");
        }
        
        Assert.assertTrue("_base is active", _base.isActive());
        LogixNG logixNG = _base.getLogixNG();
        if (logixNG != null) {
            logixNG.setEnabled(false);
            Assert.assertFalse("_base is not active", _base.isActive());
            logixNG.setEnabled(true);
            Assert.assertTrue("_base is active", _base.isActive());
        } else {
            log.error("_base has no LogixNG as ancestor");
        }
        
        Assert.assertTrue("_base is active", _base.isActive());
        _base.getConditionalNG().setEnabled(false);
        _base.setParent(null);
        Assert.assertTrue("_base is active", _base.isActive());
    }
    
    @Test
    public void testMaleSocketIsActive() {
        Assert.assertTrue("_baseMaleSocket is active", _baseMaleSocket.isActive());
        _baseMaleSocket.setEnabled(false);
        Assert.assertFalse("_baseMaleSocket is not active", _baseMaleSocket.isActive());
        _baseMaleSocket.setEnabled(true);
        
        Base parent = _baseMaleSocket.getParent();
        while ((parent != null) && !(parent instanceof MaleSocket)) {
            parent = parent.getParent();
        }
        if (parent != null) {
//            ((MaleSocket)_baseMaleSocket.getParent()).setEnabled(false);
            ((MaleSocket)parent).setEnabled(false);
            Assert.assertFalse("_baseMaleSocket is not active", _baseMaleSocket.isActive());
            ((MaleSocket)parent).setEnabled(true);
//            ((MaleSocket)_baseMaleSocket.getParent()).setEnabled(true);
        }
        
        Assert.assertTrue("_baseMaleSocket is active", _baseMaleSocket.isActive());
        ConditionalNG conditionalNG = _baseMaleSocket.getConditionalNG();
        if (conditionalNG != null) {
            conditionalNG.setEnabled(false);
            Assert.assertFalse("_baseMaleSocket is not active", _baseMaleSocket.isActive());
            conditionalNG.setEnabled(true);
        } else {
            log.error("_base has no ConditionalNG as ancestor");
        }
        
        Assert.assertTrue("_baseMaleSocket is active", _baseMaleSocket.isActive());
        LogixNG logixNG = _baseMaleSocket.getLogixNG();
        if (logixNG != null) {
            logixNG.setEnabled(false);
            Assert.assertFalse("_baseMaleSocket is not active", _baseMaleSocket.isActive());
            logixNG.setEnabled(true);
            Assert.assertTrue("_baseMaleSocket is active", _baseMaleSocket.isActive());
        } else {
            log.error("_base has no LogixNG as ancestor");
        }
        
        Assert.assertTrue("_baseMaleSocket is active", _baseMaleSocket.isActive());
        _base.getConditionalNG().setEnabled(false);
        _baseMaleSocket.setParent(null);
        Assert.assertTrue("_baseMaleSocket is active", _baseMaleSocket.isActive());
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
        _base.getConditionalNG().setEnabled(false);
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
    
    @Test
    public void testRunOnGUIDelayed() {
        // Many tests doesn't work if runOnGUIDelayed is true, so this test
        // is to ensure that all the other tests behave as they should.
        // If a test want to test with runOnGUIDelayed true, that test can
        // set runOnGUIDelayed to true.
        Assert.assertFalse("runOnGUIDelayed is false",
                _base.getConditionalNG().getRunOnGUIDelayed());
    }
    
    @Test
    public void testChildAndChildCount() {
        Assert.assertEquals("childCount is equal", _base.getChildCount(), _baseMaleSocket.getChildCount());
        for (int i=0; i < _base.getChildCount(); i++) {
            Assert.assertTrue("child is equal", _base.getChild(i) == _baseMaleSocket.getChild(i));
        }
    }
    
    @Test
    public void testBeanType() {
        Assert.assertEquals("getbeanType() is equal",
                ((NamedBean)_base).getBeanType(),
                ((NamedBean)_baseMaleSocket).getBeanType());
    }
    
    @Test
    public void testDescribeState() {
        Assert.assertEquals("description matches",
                "Unknown",
                ((NamedBean)_baseMaleSocket).describeState(NamedBean.UNKNOWN));
    }
    
    private void connect(FemaleSocket femaleSocket, MaleSocket maleSocket) throws SocketAlreadyConnectedException {
        if (femaleSocket.isConnected()) femaleSocket.disconnect();
        femaleSocket.connect(maleSocket);
    }
    
    private PropertyChangeListener getPropertyChangeListener(String name, AtomicBoolean flag, Object expectedNewValue) {
        return (PropertyChangeEvent evt) -> {
            if (name.equals(evt.getPropertyName())) {
                Assert.assertTrue("socket is correct", expectedNewValue == evt.getNewValue());
                flag.set(true);
            }
        };
    }
    
    // Test these methods:
    // * addPropertyChangeListener(PropertyChangeListener)
    // * removePropertyChangeListener(PropertyChangeListener)
    // * getNumPropertyChangeListeners()
    // * getPropertyChangeListeners()
    @Test
    public void testPropertyChangeListener() throws SocketAlreadyConnectedException {
        Assume.assumeTrue("We need at least one child to do this test", _base.getChildCount() > 0);
        MaleSocket maleSocket = getConnectableChild();
        
        FemaleSocket child = _base.getChild(0);
        
        AtomicBoolean flagConnected = new AtomicBoolean();
        PropertyChangeListener lc =
                getPropertyChangeListener(Base.PROPERTY_SOCKET_CONNECTED, flagConnected, child);
        
        AtomicBoolean flagDisconnected = new AtomicBoolean();
        PropertyChangeListener ld =
                getPropertyChangeListener(Base.PROPERTY_SOCKET_DISCONNECTED, flagDisconnected, child);
        
//        System.out.format("%n%ntestPropertyChangeListener()%n");
//        for (PropertyChangeListener l : ((NamedBean)_baseMaleSocket).getPropertyChangeListeners()) {
//            System.out.format("Property change listener: %s: %s%n", l, l.getClass().getName());
//        }
        
        // Note that AbstractManager.register() register itself as a
        // PropertyChangeListener. Therefore we have one listener before
        // adding our own listeners.
        Assert.assertEquals("num property change listeners matches",
                1, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        
        ((NamedBean)_baseMaleSocket).addPropertyChangeListener(lc);
        Assert.assertEquals("num property change listeners matches",
                2, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        
        ((NamedBean)_baseMaleSocket).addPropertyChangeListener(ld);
        Assert.assertEquals("num property change listeners matches",
                3, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        
        System.out.format("Ref: %s%n", ((NamedBean)_baseMaleSocket).getListenerRef(lc));
        Assert.assertNull("listener ref is null", ((NamedBean)_baseMaleSocket).getListenerRef(lc));
        
        boolean hasManagerListener = false;
        boolean hasConnectedListener = false;
        boolean hasDisconnectedListener = false;
        for (PropertyChangeListener l : ((NamedBean)_baseMaleSocket).getPropertyChangeListeners()) {
            if (l instanceof jmri.Manager) hasManagerListener = true;
            else if (l == lc) hasConnectedListener = true;
            else if (l == ld) hasDisconnectedListener = true;
            else Assert.fail("getPropertyChangeListeners() returns unknown listener: " + l.toString());
            System.out.format("Property change listener: %s: %s%n", l, l.getClass().getName());
        }
        Assert.assertTrue("getPropertyChangeListeners() has manager listener", hasManagerListener);
        Assert.assertTrue("getPropertyChangeListeners() has connected listener", hasConnectedListener);
        Assert.assertTrue("getPropertyChangeListeners() has disconnected listener", hasDisconnectedListener);
        
        // Connect shall do a firePropertyChange which will set the flag
        flagConnected.set(false);
        connect(child, maleSocket);
        Assert.assertTrue("flag is set", flagConnected.get());
        
        // Disconnect shall do a firePropertyChange which will set the flag
        flagDisconnected.set(false);
        _base.getChild(0).disconnect();
        Assert.assertTrue("flag is set", flagDisconnected.get());
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

    private final static Logger log = LoggerFactory.getLogger(AbstractBaseTestBase.class);

}
