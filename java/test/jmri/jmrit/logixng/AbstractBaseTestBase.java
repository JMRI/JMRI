package jmri.jmrit.logixng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.implementation.AbstractBase;

import org.apache.commons.lang3.RandomStringUtils;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

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
    
    /**
     * Returns the LogixNG for _base.
     * @return the LogixNG for _base or null if _base doesn't have any LogixNG
     */
    public abstract LogixNG getLogixNG();
    
    /**
     * Creates a new socket.
     * Some items can create new sockets automaticly and this method is used
     * to test that.
     * @return true if a new socket is added. false if this item doesn't
     * support adding new sockets.
     * @throws jmri.jmrit.logixng.SocketAlreadyConnectedException if socket is already connected
     */
    abstract public boolean addNewSocket() throws SocketAlreadyConnectedException;
    
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
//        _base.getConditionalNG().setEnabled(false);
//        _base.setParent(null);
//        Assert.assertTrue("conditionalNG is equal",
//                _base.getConditionalNG() == _baseMaleSocket.getConditionalNG());
    }
    
    @Test
    public void testMaleSocketGetLogixNG() {
        Assert.assertTrue("logixNG is equal",
                _base.getLogixNG() == _baseMaleSocket.getLogixNG());
//        _base.getConditionalNG().setEnabled(false);
//        _base.setParent(null);
//        Assert.assertTrue("logixNG is equal",
//                _base.getLogixNG() == _baseMaleSocket.getLogixNG());
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
        Assert.assertTrue("Object of _baseMaleSocket is _base", _base == _baseMaleSocket.getObject());
        Assert.assertTrue("Parent of _base is _baseMaleSocket", _base.getParent() == _baseMaleSocket);
    }
    
    /*.*
     * Set parent to null for all children to item, and their children.
     *./
    private void clearParent(Base item) {
        for (int i=0; i < item.getChildCount(); i++) {
            FemaleSocket femaleSocket = item.getChild(i);
            femaleSocket.setParent(null);
            
            if (femaleSocket.isConnected()) {
                clearParent(femaleSocket.getConnectedSocket());
            }
        }
    }
    
    /*.*
     * Check that parent is correct for all children to item, and their children.
     *./
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
    
    /*.*
     * Test that the method setParentForAllChildren() works when there are
     * connected children.
     *./
    @Test
    public void testSetParentForAllChildren_WithConnectedChildren() {
        _base.getConditionalNG().setEnabled(false);
        clearParent(_base);     // Parent can't be set to null for AbstractFemaleSocket
        _base.setParentForAllChildren();
        checkParent(_base);
    }
    
    /*.*
     * Test that the method setParentForAllChildren() works when there are
     * no connected children.
     *./
    @Test
    public void testSetParentForAllChildren_WithoutConnectedChildren() {
        _base.getConditionalNG().setEnabled(false);
        clearParent(_base);     // Parent can't be set to null for AbstractFemaleSocket
        for (int i=0; i < _base.getChildCount(); i++) {
            FemaleSocket femaleSocket = _base.getChild(i);
            femaleSocket.disconnect();
        }
        _base.setParentForAllChildren();
        checkParent(_base);
    }
*/    
    /**
     * Returns the expected result of _base.printTree(writer, TREE_INDENT)
     * @return the expected printed tree
     */
    public abstract String getExpectedPrintedTree();
    
    @Test
    public void testGetPrintTree() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        _baseMaleSocket.printTree(Locale.ENGLISH, printWriter, TREE_INDENT);
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
        _baseMaleSocket.printTree(printWriter, TREE_INDENT);
        Assert.assertEquals("Tree is equal", getExpectedPrintedTree(), stringWriter.toString());
    }
    
    @Test
    public void testMaleSocketGetPrintTreeWithStandardLocale() {
        Locale oldLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        _baseMaleSocket.printTree(printWriter, TREE_INDENT);
        Assert.assertEquals("Tree is equal", getExpectedPrintedTree(), stringWriter.toString());
        Locale.setDefault(oldLocale);
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
    public void testGetDeepCopy() throws JmriException {
        Map<String, String> systemNames = new HashMap<>();
        Map<String, String> userNames = new HashMap<>();
        Map<String, String> comments = new HashMap<>();
        
        // The copy is not a male socket so it will not get the local variables
        _baseMaleSocket.clearLocalVariables();
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        _baseMaleSocket.printTree(Locale.ENGLISH, printWriter, TREE_INDENT);
        String originalTree = stringWriter.toString();
        
        Base copy = _base.getDeepCopy(systemNames, userNames);
        
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        
        Assert.assertTrue(copy != null);
        
        copy.printTree(Locale.ENGLISH, printWriter, TREE_INDENT);
        String copyTree = stringWriter.toString();
        
        if (! originalTree.equals(copyTree)) {
            System.out.format("---------------------%n%nOriginal tree:%n%s%n---------------------%n%nCopy tree:%n%s%n---------------------%n%n", originalTree, copyTree);
        }
        
        // REMOVE LATER!!!!!!!!
        // REMOVE LATER!!!!!!!!
        // REMOVE LATER!!!!!!!!
//        Assume.assumeTrue(originalTree.equals(copyTree));
        // REMOVE LATER!!!!!!!!
        // REMOVE LATER!!!!!!!!
        // REMOVE LATER!!!!!!!!
        
        Assert.assertEquals("Tree is equal", originalTree, copyTree);
        
        
        // Test that we can give the copied items new system names and user names
        
        List<Base> originalList = new ArrayList<>();
        _baseMaleSocket.forEntireTree((Base b) -> {
            if (b instanceof MaleSocket) {
                b.setComment(RandomStringUtils.randomAlphabetic(10));
                
                originalList.add(b);
                
                // A system name with a dollar sign after the sub system prefix
                // can have any character after the dollar sign.
                String newSystemName =
                        ((MaleSocket)b).getManager()
                                .getSubSystemNamePrefix() + "$" + RandomStringUtils.randomAlphabetic(10);
                String newUserName = RandomStringUtils.randomAlphabetic(20);
                
                systemNames.put(b.getSystemName(), newSystemName);
                userNames.put(b.getSystemName(), newUserName);
                comments.put(b.getSystemName(), b.getComment());
            }
        });
        
        copy = _base.getDeepCopy(systemNames, userNames);
        
        List<Base> copyList = new ArrayList<>();
        copy.forEntireTree((Base b) -> {
            if (b instanceof MaleSocket) {
                copyList.add(b);
            }
        });
        
        for (int i=0; i < originalList.size(); i++) {
            Assert.assertEquals(copyList.get(i).getSystemName(),
                    systemNames.get(originalList.get(i).getSystemName()));
            
            Assert.assertEquals(copyList.get(i).getUserName(),
                    userNames.get(originalList.get(i).getSystemName()));
            
            Assert.assertEquals(copyList.get(i).getComment(),
                    comments.get(originalList.get(i).getSystemName()));
        }
    }
    
    @Test
    public void testIsActive() {
        Assert.assertEquals(_base.getParent(), _baseMaleSocket);
        
        Assert.assertTrue("_base is active", _base.isActive());
        _baseMaleSocket.setEnabled(false);
        Assert.assertFalse("_base is not active", _base.isActive());
        _baseMaleSocket.setEnabled(true);
        Assert.assertTrue("_base is active", _base.isActive());
        
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
        _baseMaleSocket.setEnabled(false);
        Assert.assertFalse("_baseMaleSocket is not active", _baseMaleSocket.isActive());
        _baseMaleSocket.setEnabled(true);
        Assert.assertTrue("_baseMaleSocket is active", _baseMaleSocket.isActive());
        
        Base parent = _baseMaleSocket.getParent();
        while ((parent != null) && !(parent instanceof MaleSocket)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            ((MaleSocket)parent).setEnabled(false);
            Assert.assertFalse("_baseMaleSocket is not active", _baseMaleSocket.isActive());
            ((MaleSocket)parent).setEnabled(true);
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
        _baseMaleSocket.setEnabled(false);
        _base.dispose();
    }
    
    @Test
    public void testRunOnGUIDelayed() {
        // Many tests doesn't work if runOnGUIDelayed is true, so this test
        // is to ensure that all the other tests behave as they should.
        // If a test want to test with runOnGUIDelayed true, that test can
        // set runOnGUIDelayed to true.
        Assert.assertFalse("runOnGUIDelayed is false",
                _base.getConditionalNG().getRunDelayed());
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
    
    @Test
    public void testAddAndRemoveSocket() throws SocketAlreadyConnectedException {
        AtomicBoolean ab = new AtomicBoolean(false);
        AtomicReference<PropertyChangeEvent> ar = new AtomicReference<>();
        
        _base.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            ab.set(true);
            ar.set(evt);
        });
        
        ab.set(false);
        
        _baseMaleSocket.setEnabled(false);
        
        // Some item doesn't support adding new sockets.
        // Return if the item under test doesn't.
        if (!addNewSocket()) return;
        
        Assert.assertTrue("PropertyChangeEvent fired", ab.get());
        Assert.assertEquals(Base.PROPERTY_CHILD_COUNT, ar.get().getPropertyName());
        System.out.format("%s: New value: %s%n", _base.getClass().getName(), ar.get().getNewValue());
        Assert.assertTrue(ar.get().getNewValue() instanceof List);
        List list = (List)ar.get().getNewValue();
        for (Object o : list) {
            Assert.assertTrue(o instanceof FemaleSocket);
        }
    }
/*    
    @Test
    public void testRemoveChild() {
        AtomicBoolean ab = new AtomicBoolean(false);
        
        _base.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            ab.set(true);
        });
        
        for (int i=_base.getChildCount()-1; i > 0; i--) {
            ab.set(true);
            boolean hasThrown = false;
            
            try {
                _base.removeChild(i);
            } catch (UnsupportedOperationException e) {
                hasThrown = true;
                Assert.assertEquals("Correct error message",
                        "Child "+Integer.toString(i)+" cannot be removed",
                        e.getMessage());
            }
            
            if (_base.canRemoveChild(i)) {
                Assert.assertTrue("PropertyChange is fired", ab.get());
            } else {
                Assert.assertTrue("Exception is thrown", hasThrown);
            }
        }
    }
/*    
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
    
    private void assertListeners(
            PropertyChangeListener lc,
            PropertyChangeListener ld,
            boolean expectManagerListener,
            boolean expectConnectedListener,
            boolean expectDisconnectedListener) {
        
        boolean hasManagerListener = false;
        boolean hasConnectedListener = false;
        boolean hasDisconnectedListener = false;
        for (PropertyChangeListener l : ((NamedBean)_baseMaleSocket).getPropertyChangeListeners()) {
            if (l instanceof jmri.Manager) hasManagerListener = true;
            else if (l == lc) hasConnectedListener = true;
            else if (l == ld) hasDisconnectedListener = true;
            else Assert.fail("getPropertyChangeListeners() returns unknown listener: " + l.toString());
        }
        if (expectManagerListener) {
            Assert.assertTrue("getPropertyChangeListeners() has manager listener", hasManagerListener);
        } else {
            Assert.assertFalse("getPropertyChangeListeners() has not manager listener", hasManagerListener);
        }
        if (expectConnectedListener) {
            Assert.assertTrue("getPropertyChangeListeners() has connected listener", hasConnectedListener);
        } else {
            Assert.assertFalse("getPropertyChangeListeners() has not connected listener", hasConnectedListener);
        }
        if (expectDisconnectedListener) {
            Assert.assertTrue("getPropertyChangeListeners() has disconnected listener", hasDisconnectedListener);
        } else {
            Assert.assertFalse("getPropertyChangeListeners() has not disconnected listener", hasDisconnectedListener);
        }
    }
    
    // This method is needet to test property change methods which only listen
    // to a particular property, since these property change listeners uses a
    // proxy listener.
    private void assertListeners(
            String propertyName,
            FemaleSocket child,
            AtomicBoolean flag,
            boolean expectedResult) {
        
        // Check that we have the expected listener
        PropertyChangeListener[] listeners =
                ((NamedBean)_baseMaleSocket).getPropertyChangeListeners(propertyName);
        
        if (expectedResult) {
            Assert.assertEquals("num property change listeners matches",
                    1, listeners.length);
        } else {
            Assert.assertEquals("num property change listeners matches",
                    0, listeners.length);
            return;
        }
        
        // If here, we expect success.
        
        // We call propertyChange to check that it's the correct listener we have
        flag.set(false);
        listeners[0].propertyChange(new PropertyChangeEvent(this, propertyName, null, child));
        Assert.assertTrue("flag is set", flag.get());
        Assert.assertTrue("getPropertyChangeListeners("+propertyName+") has listener", flag.get());
    }
    
    
    
/*  
    Note: PROPERTY_SOCKET_CONNECTED and PROPERTY_SOCKET_DISCONNECTED has been
    moved to FemaleSocket.
    
    
    // Test these methods:
    // * addPropertyChangeListener(PropertyChangeListener l)
    // * removePropertyChangeListener(PropertyChangeListener l)
    // * getNumPropertyChangeListeners()
    // * getPropertyChangeListeners()
    @Test
    public void testPropertyChangeListener1() throws SocketAlreadyConnectedException {
        // We need at least one child to do this test.
        // Some item doesn't have children.
        // Return if the item under test doesn't.
        if (_base.getChildCount() == 0) return;
        
        _baseMaleSocket.setEnabled(false);
        
        MaleSocket maleSocket = getConnectableChild();
        
        FemaleSocket child = _base.getChild(0);
        
        AtomicBoolean flagConnected = new AtomicBoolean();
        PropertyChangeListener lc =
                getPropertyChangeListener(Base.PROPERTY_SOCKET_CONNECTED, flagConnected, child);
        
        AtomicBoolean flagDisconnected = new AtomicBoolean();
        PropertyChangeListener ld =
                getPropertyChangeListener(Base.PROPERTY_SOCKET_DISCONNECTED, flagDisconnected, child);
        
        // Note that AbstractManager.register() register itself as a
        // PropertyChangeListener. Therefore we have one listener before
        // adding our own listeners.
        Assert.assertEquals("num property change listeners matches",
                1, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        // Check that we have the expected listeners
        assertListeners(lc, ld, true, false, false);
        
        ((NamedBean)_baseMaleSocket).addPropertyChangeListener(lc);
        Assert.assertEquals("num property change listeners matches",
                2, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        // Check that we have the expected listeners
        assertListeners(lc, ld, true, true, false);
        
        ((NamedBean)_baseMaleSocket).addPropertyChangeListener(ld);
        Assert.assertEquals("num property change listeners matches",
                3, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        // Check that we have the expected listeners
        assertListeners(lc, ld, true, true, true);
        
        Assert.assertNull("listener ref is null", ((NamedBean)_baseMaleSocket).getListenerRef(lc));
        
        // Connect shall do a firePropertyChange which will set the flag
        flagConnected.set(false);
        connect(child, maleSocket);
        Assert.assertTrue("flag is set", flagConnected.get());
        
        // Disconnect shall do a firePropertyChange which will set the flag
        flagDisconnected.set(false);
        _base.getChild(0).disconnect();
        Assert.assertTrue("flag is set", flagDisconnected.get());
        
        // Try to remove the listeners
        
        // Check that we have the expected listeners
        assertListeners(lc, ld, true, true, true);
        ((NamedBean)_baseMaleSocket).removePropertyChangeListener(lc);
        
        // Check that we have the expected listeners
        assertListeners(lc, ld, true, false, true);
        ((NamedBean)_baseMaleSocket).removePropertyChangeListener(ld);
        
        // Check that we have the expected listeners
        assertListeners(lc, ld, true, false, false);
    }
    
    // Test these methods:
    // * addPropertyChangeListener(String propertyName, PropertyChangeListener l)
    // * removePropertyChangeListener(String propertyName, PropertyChangeListener l)
    // * getNumPropertyChangeListeners()
    @Test
    public void testPropertyChangeListener2() throws SocketAlreadyConnectedException {
        // We need at least one child to do this test.
        // Some item doesn't have children.
        // Return if the item under test doesn't.
        if (_base.getChildCount() == 0) return;
        
        _baseMaleSocket.setEnabled(false);
        
        MaleSocket maleSocket = getConnectableChild();
        
        FemaleSocket child = _base.getChild(0);
        
        AtomicBoolean flagConnected = new AtomicBoolean();
        PropertyChangeListener lc =
                getPropertyChangeListener(Base.PROPERTY_SOCKET_CONNECTED, flagConnected, child);
        
        AtomicBoolean flagDisconnected = new AtomicBoolean();
        PropertyChangeListener ld =
                getPropertyChangeListener(Base.PROPERTY_SOCKET_DISCONNECTED, flagDisconnected, child);
        
        // Note that AbstractManager.register() register itself as a
        // PropertyChangeListener. Therefore we have one listener before
        // adding our own listeners.
        Assert.assertEquals("num property change listeners matches",
                1, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        // Check that we have the expected listeners
        assertListeners(lc, ld, true, false, false);
        
        ((NamedBean)_baseMaleSocket).addPropertyChangeListener(Base.PROPERTY_SOCKET_CONNECTED, lc);
        Assert.assertEquals("num property change listeners matches",
                2, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        
        // Check that we have the expected listener
        PropertyChangeListener[] listeners = ((NamedBean)_baseMaleSocket).getPropertyChangeListeners(Base.PROPERTY_SOCKET_CONNECTED);
        Assert.assertEquals("num property change listeners matches",
                1, listeners.length);
        
        // We call propertyChange to check that it's the correct listener we have
        flagConnected.set(false);
        listeners[0].propertyChange(new PropertyChangeEvent(this, Base.PROPERTY_SOCKET_CONNECTED, null, child));
        Assert.assertTrue("flag is set", flagConnected.get());
        
        ((NamedBean)_baseMaleSocket).addPropertyChangeListener(Base.PROPERTY_SOCKET_DISCONNECTED, ld);
        Assert.assertEquals("num property change listeners matches",
                3, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        
        // Check that we have the expected listener
        listeners = ((NamedBean)_baseMaleSocket).getPropertyChangeListeners(Base.PROPERTY_SOCKET_DISCONNECTED);
        Assert.assertEquals("num property change listeners matches",
                1, listeners.length);
        
        // We call propertyChange to check that it's the correct listener we have
        flagDisconnected.set(false);
        listeners[0].propertyChange(new PropertyChangeEvent(this, Base.PROPERTY_SOCKET_DISCONNECTED, null, child));
        Assert.assertTrue("flag is set", flagConnected.get());
        
        Assert.assertNull("listener ref is null", ((NamedBean)_baseMaleSocket).getListenerRef(lc));
        
        // Connect shall do a firePropertyChange which will set the flag
        flagConnected.set(false);
        connect(child, maleSocket);
        Assert.assertTrue("flag is set", flagConnected.get());
        
        // Disconnect shall do a firePropertyChange which will set the flag
        flagDisconnected.set(false);
        _base.getChild(0).disconnect();
        Assert.assertTrue("flag is set", flagDisconnected.get());
        
        // Try to remove the listeners
        
        // Check that we have the expected listeners
        assertListeners(Base.PROPERTY_SOCKET_CONNECTED, child, flagConnected, true);
        assertListeners(Base.PROPERTY_SOCKET_DISCONNECTED, child, flagDisconnected, true);
        
        // This should be ignored since the name doesn't match the listener
        ((NamedBean)_baseMaleSocket).removePropertyChangeListener(Base.PROPERTY_SOCKET_DISCONNECTED, lc);
        
        // Check that we have the expected listeners
        assertListeners(Base.PROPERTY_SOCKET_CONNECTED, child, flagConnected, true);
        assertListeners(Base.PROPERTY_SOCKET_DISCONNECTED, child, flagDisconnected, true);
        
        // This should work
        ((NamedBean)_baseMaleSocket).removePropertyChangeListener(Base.PROPERTY_SOCKET_CONNECTED, lc);
        
        // Check that we have the expected listeners
        assertListeners(Base.PROPERTY_SOCKET_CONNECTED, child, flagConnected, false);
        assertListeners(Base.PROPERTY_SOCKET_DISCONNECTED, child, flagDisconnected, true);
        
        // This should be ignored since the name doesn't match the listener
        ((NamedBean)_baseMaleSocket).removePropertyChangeListener(Base.PROPERTY_SOCKET_CONNECTED, ld);
        
        // Check that we have the expected listeners
        assertListeners(Base.PROPERTY_SOCKET_CONNECTED, child, flagConnected, false);
        assertListeners(Base.PROPERTY_SOCKET_DISCONNECTED, child, flagDisconnected, true);
        
        // This should work
        ((NamedBean)_baseMaleSocket).removePropertyChangeListener(Base.PROPERTY_SOCKET_DISCONNECTED, ld);
        
        // Check that we have the expected listeners
        assertListeners(Base.PROPERTY_SOCKET_CONNECTED, child, flagConnected, false);
        assertListeners(Base.PROPERTY_SOCKET_DISCONNECTED, child, flagDisconnected, false);
    }
    
    // Test these methods:
    // * addPropertyChangeListener(PropertyChangeListener l, String name, String listenerRef)
    // * removePropertyChangeListener(PropertyChangeListener l, String name, String listenerRef)
    // * getNumPropertyChangeListeners()
    // * getPropertyChangeListeners()
    @Test
    public void testPropertyChangeListener3() throws SocketAlreadyConnectedException {
        // We need at least one child to do this test.
        // Some item doesn't have children.
        // Return if the item under test doesn't.
        if (_base.getChildCount() == 0) return;
        
        _baseMaleSocket.setEnabled(false);
        
        MaleSocket maleSocket = getConnectableChild();
        
        FemaleSocket child = _base.getChild(0);
        
        AtomicBoolean flagConnected = new AtomicBoolean();
        PropertyChangeListener lc =
                getPropertyChangeListener(Base.PROPERTY_SOCKET_CONNECTED, flagConnected, child);
        
        AtomicBoolean flagDisconnected = new AtomicBoolean();
        PropertyChangeListener ld =
                getPropertyChangeListener(Base.PROPERTY_SOCKET_DISCONNECTED, flagDisconnected, child);
        
        // Note that AbstractManager.register() register itself as a
        // PropertyChangeListener. Therefore we have one listener before
        // adding our own listeners.
        Assert.assertEquals("num property change listeners matches",
                1, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        // Check that we have the expected listeners
        assertListeners(lc, ld, true, false, false);
        
        ((NamedBean)_baseMaleSocket).addPropertyChangeListener(lc, _baseMaleSocket.getSystemName(), "Connected listener");
        Assert.assertEquals("num property change listeners matches",
                2, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        // Check that we have the expected listeners
        assertListeners(lc, ld, true, true, false);
        
        ((NamedBean)_baseMaleSocket).addPropertyChangeListener(ld, _baseMaleSocket.getSystemName(), "Disconnected listener");
        Assert.assertEquals("num property change listeners matches",
                3, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        // Check that we have the expected listeners
        assertListeners(lc, ld, true, true, true);
        
        Assert.assertEquals("listener ref is correct", "Connected listener", ((NamedBean)_baseMaleSocket).getListenerRef(lc));
        Assert.assertEquals("listener ref is correct", "Disconnected listener", ((NamedBean)_baseMaleSocket).getListenerRef(ld));
        
        // Connect shall do a firePropertyChange which will set the flag
        flagConnected.set(false);
        connect(child, maleSocket);
        Assert.assertTrue("flag is set", flagConnected.get());
        
        // Disconnect shall do a firePropertyChange which will set the flag
        flagDisconnected.set(false);
        _base.getChild(0).disconnect();
        Assert.assertTrue("flag is set", flagDisconnected.get());
    }
    
    // Test these methods:
    // * addPropertyChangeListener(String propertyName, PropertyChangeListener l, String name, String listenerRef)
    // * removePropertyChangeListener(String propertyName, PropertyChangeListener l)
    // * getNumPropertyChangeListeners()
    @Test
    public void testPropertyChangeListener4() throws SocketAlreadyConnectedException {
        // We need at least one child to do this test.
        // Some item doesn't have children.
        // Return if the item under test doesn't.
        if (_base.getChildCount() == 0) return;
        
        _baseMaleSocket.setEnabled(false);
        
        MaleSocket maleSocket = getConnectableChild();
        
        FemaleSocket child = _base.getChild(0);
        
        AtomicBoolean flagConnected = new AtomicBoolean();
        PropertyChangeListener lc =
                getPropertyChangeListener(Base.PROPERTY_SOCKET_CONNECTED, flagConnected, child);
        
        AtomicBoolean flagDisconnected = new AtomicBoolean();
        PropertyChangeListener ld =
                getPropertyChangeListener(Base.PROPERTY_SOCKET_DISCONNECTED, flagDisconnected, child);
        
        // Note that AbstractManager.register() register itself as a
        // PropertyChangeListener. Therefore we have one listener before
        // adding our own listeners.
        Assert.assertEquals("num property change listeners matches",
                1, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        // Check that we have the expected listeners
        assertListeners(lc, ld, true, false, false);
        
        ((NamedBean)_baseMaleSocket).addPropertyChangeListener(Base.PROPERTY_SOCKET_CONNECTED, lc, _baseMaleSocket.getSystemName(), "Connected listener");
        Assert.assertEquals("num property change listeners matches",
                2, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        // Check that we have the expected listeners
//        assertListeners(lc, ld, true, true, false);
        
        ((NamedBean)_baseMaleSocket).addPropertyChangeListener(Base.PROPERTY_SOCKET_DISCONNECTED, ld, _baseMaleSocket.getSystemName(), "Disconnected listener");
        Assert.assertEquals("num property change listeners matches",
                3, ((NamedBean)_baseMaleSocket).getNumPropertyChangeListeners());
        // Check that we have the expected listeners
//        assertListeners(lc, ld, true, true, true);
        
        Assert.assertEquals("listener ref is correct", "Connected listener", ((NamedBean)_baseMaleSocket).getListenerRef(lc));
        Assert.assertEquals("listener ref is correct", "Disconnected listener", ((NamedBean)_baseMaleSocket).getListenerRef(ld));
        
        // Connect shall do a firePropertyChange which will set the flag
        flagConnected.set(false);
        connect(child, maleSocket);
        Assert.assertTrue("flag is set", flagConnected.get());
        
        // Disconnect shall do a firePropertyChange which will set the flag
        flagDisconnected.set(false);
        _base.getChild(0).disconnect();
        Assert.assertTrue("flag is set", flagDisconnected.get());
    }
    
    @Test
    public void testPropertyChangeListeners5() {
        
        PropertyChangeListener listener1 = (PropertyChangeEvent evt) -> {};
        PropertyChangeListener listener2 = (PropertyChangeEvent evt) -> {};
        
        _baseMaleSocket.addPropertyChangeListener(listener1, "BeanA", "A name");
        _baseMaleSocket.addPropertyChangeListener(listener2, "BeanB", "Another name");
        Assert.assertEquals("A name", _baseMaleSocket.getListenerRef(listener1));
        List<String> listenerRefs = _baseMaleSocket.getListenerRefs();
        
        // The order of the listener refs may differ between runs so sort the list
        Collections.sort(listenerRefs);
        
        String listString = listenerRefs.stream().map(Object::toString)
                        .collect(Collectors.joining(", "));
        
        Assert.assertEquals("A name, Another name", listString);
        
        _baseMaleSocket.updateListenerRef(listener1, "New name");
        Assert.assertEquals("New name", _baseMaleSocket.getListenerRef(listener1));
        
        Assert.assertEquals(listener1,
                _baseMaleSocket.getPropertyChangeListenersByReference("BeanA")[0]);
        
        Assert.assertEquals(listener2,
                _baseMaleSocket.getPropertyChangeListenersByReference("BeanB")[0]);
    }
*/    
    
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

        @Override
        public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base deepCopyChildren(Base original, Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }
        
    }


    /**
     * Executes the method.
     * This interface is used by the method
     * {@link #assertIndexOutOfBoundsException(RunnableWithIndex, int, int)}
     */
    public interface RunnableWithIndex {
        /**
         * Run the method.
         * @param index the index
         */
        public void run(int index);
    }
    
    /**
     * Assert that an IndexOutOfBoundsException is thrown and has the correct
     * error message.
     * <P>
     * This method is added since different Java versions gives different
     * error messages.
     * @param r the method to run
     * @param index the index
     * @param arraySize the size of the array
     */
    public void assertIndexOutOfBoundsException(RunnableWithIndex r, int index, int arraySize) {
        boolean hasThrown = false;
        try {
            r.run(index);
        } catch (IndexOutOfBoundsException ex) {
            hasThrown = true;
            String msg1 = String.format("Index: %d, Size: %d", index, arraySize);
            String msg2 = String.format("Index %d out of bounds for length %d", index, arraySize);
            if (!msg1.equals(ex.getMessage()) && !msg2.equals(ex.getMessage())) {
                Assert.fail("Wrong error message: " + ex.getMessage());
            }
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractBaseTestBase.class);

}
