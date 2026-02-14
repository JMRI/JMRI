package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.CheckForNull;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.implementation.AbstractBase;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import org.junit.jupiter.api.Test;

/**
 * Test AbstractAnalogExpression
 *
 * @author Daniel Bergqvist 2018
 */
public abstract class AbstractBaseTestBase {

    final static String TREE_INDENT = "   ";
    protected Base _base;
    protected MaleSocket _baseMaleSocket;
    protected LogixNG_Category _category;
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

    public static MaleSocket getLastMaleSocket(MaleSocket socket) {
        MaleSocket lastMaleSocket = socket;
        Base base = socket;
        while ((base != null) && (base instanceof MaleSocket)) {
            lastMaleSocket = (MaleSocket) base;
            base = ((MaleSocket)base).getObject();
        }
        return lastMaleSocket;
    }

    @Test
    public void testGetConditionalNG() {
        assertNotNull( getConditionalNG(),
            () -> "Method getConditionalNG() returns null for class "+ this.getClass().getName());
        assertSame( getConditionalNG(), _base.getConditionalNG(), "ConditionalNG is equal");

        _base.getConditionalNG().setEnabled(false);
        _base.setParent(null);
        assertNull( _base.getConditionalNG(), "ConditionalNG is null");
    }

    @Test
    public void testGetLogixNG() {
        assertNotNull( getLogixNG(),
            () -> "Method getLogixNG() returns null for class "+ this.getClass().getName());
        assertSame( getLogixNG(), _base.getLogixNG(), "LogixNG is equal");

        _base.getConditionalNG().setEnabled(false);
        _base.setParent(null);
        assertNull( _base.getLogixNG(), "LogixNG is null");
    }

    @Test
    public void testMaleSocketGetConditionalNG() {
        assertSame( _base.getConditionalNG(), _baseMaleSocket.getConditionalNG(),
            "conditionalNG is equal");
//        _base.getConditionalNG().setEnabled(false);
//        _base.setParent(null);
//        Assert.assertTrue("conditionalNG is equal",
//                _base.getConditionalNG() == _baseMaleSocket.getConditionalNG());
    }

    @Test
    public void testMaleSocketGetLogixNG() {
        assertSame( _base.getLogixNG(), _baseMaleSocket.getLogixNG(),
            "logixNG is equal");
//        _base.getConditionalNG().setEnabled(false);
//        _base.setParent(null);
//        Assert.assertTrue("logixNG is equal",
//                _base.getLogixNG() == _baseMaleSocket.getLogixNG());
    }

    @Test
    public void testMaleSocketGetRoot() {
        assertSame( _base.getRoot(), _baseMaleSocket.getRoot(), "root is equal");
        _base.getConditionalNG().setEnabled(false);
        _base.setParent(null);
        assertSame( _base.getRoot(), _baseMaleSocket.getRoot(), "root is equal");
    }

    @Test
    public void testGetParent() {
        assertSame( _base, getLastMaleSocket(_baseMaleSocket).getObject(), "Object of _baseMaleSocket is _base");
        assertSame( _base.getParent(), getLastMaleSocket(_baseMaleSocket), "Parent of _base is _baseMaleSocket");
    }

    @Test
    public void testFemaleSocketSystemName() {
        for (int i=0; i < _base.getChildCount(); i++) {
            assertEquals(_base.getSystemName(), _base.getChild(i).getSystemName());
        }
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
        _baseMaleSocket.printTree(Locale.ENGLISH, printWriter, TREE_INDENT, new MutableInt(0));
        assertEquals( getExpectedPrintedTree(), stringWriter.toString(), "Tree is equal");
    }

    @Test
    public void testMaleSocketGetPrintTree() {
        /// Test that the male socket of the item prints the same tree
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        _baseMaleSocket.printTree(Locale.ENGLISH, printWriter, TREE_INDENT, new MutableInt(0));
        assertEquals( getExpectedPrintedTree(), stringWriter.toString(), "Tree is equal");
    }

    @Test
    public void testGetPrintTreeWithStandardLocale() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        _baseMaleSocket.printTree(printWriter, TREE_INDENT, new MutableInt(0));
        assertEquals( getExpectedPrintedTree(), stringWriter.toString(), "Tree is equal");
    }

    @Test
    public void testMaleSocketGetPrintTreeWithStandardLocale() {
        Locale oldLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        _baseMaleSocket.printTree(printWriter, TREE_INDENT, new MutableInt(0));
        assertEquals( getExpectedPrintedTree(), stringWriter.toString(), "Tree is equal");
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
        _base.getRoot().printTree(Locale.ENGLISH, printWriter, TREE_INDENT, new MutableInt(0));
        assertEquals( getExpectedPrintedTreeFromRoot(), stringWriter.toString(), "Tree is equal");
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
        _baseMaleSocket.printTree(Locale.ENGLISH, printWriter, TREE_INDENT, new MutableInt(0));
        String originalTree = stringWriter.toString();

        Base copy = _base.getDeepCopy(systemNames, userNames);

        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        assertNotNull(copy );

        copy.printTree(Locale.ENGLISH, printWriter, TREE_INDENT, new MutableInt(0));
        String copyTree = stringWriter.toString();

        assertEquals( originalTree, copyTree,
            () -> "Tree should be equal --------------------" +
                String.format("-%n%nOriginal tree:%n%s%n---------------------%n%nCopy tree:%n%s%n---------------------%n%n",
                    originalTree, copyTree)    );

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
            assertEquals(copyList.get(i).getSystemName(),
                    systemNames.get(originalList.get(i).getSystemName()));

            assertEquals(copyList.get(i).getUserName(),
                    userNames.get(originalList.get(i).getSystemName()));

            assertEquals(copyList.get(i).getComment(),
                    comments.get(originalList.get(i).getSystemName()));
        }
    }

    @Test
    public void testIsActive() {
        assertEquals(_base.getParent(), getLastMaleSocket(_baseMaleSocket));

        assertTrue( _base.isActive(), "_base is active");
        _baseMaleSocket.setEnabled(false);
        assertFalse( _base.isActive(), "_base is not active");
        _baseMaleSocket.setEnabled(true);
        assertTrue( _base.isActive(), "_base is active");

        assertTrue(_base.isActive());
        ConditionalNG conditionalNG = _base.getConditionalNG();
        assertNotNull( conditionalNG, "_base has no ConditionalNG as ancestor");
        conditionalNG.setEnabled(false);
        assertFalse( _base.isActive(), "_base is not active");
        conditionalNG.setEnabled(true);


        assertTrue( _base.isActive(), "_base is active");
        LogixNG logixNG = _base.getLogixNG();
        assertNotNull( logixNG, "_base has no LogixNG as ancestor");
        logixNG.setEnabled(false);
        assertFalse( _base.isActive(), "_base is not active");
        logixNG.setEnabled(true);
        assertTrue( _base.isActive(), "_base is active");

        assertTrue( _base.isActive(), "_base is active");
        _base.getConditionalNG().setEnabled(false);
        _base.setParent(null);
        assertTrue( _base.isActive(), "_base is active");
    }

    @Test
    public void testMaleSocketIsActive() {
        _baseMaleSocket.setEnabled(false);
        assertFalse( _baseMaleSocket.isActive(), "_baseMaleSocket is not active");
        _baseMaleSocket.setEnabled(true);
        assertTrue( _baseMaleSocket.isActive(), "_baseMaleSocket is active");

        Base parent = _baseMaleSocket.getParent();
        while ((parent != null) && !(parent instanceof MaleSocket)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            ((MaleSocket)parent).setEnabled(false);
            assertFalse( _baseMaleSocket.isActive(), "_baseMaleSocket is not active");
            ((MaleSocket)parent).setEnabled(true);
        }

        assertTrue( _baseMaleSocket.isActive(), "_baseMaleSocket is active");
        ConditionalNG conditionalNG = _baseMaleSocket.getConditionalNG();
        assertNotNull( conditionalNG, "_base has no ConditionalNG as ancestor");
        conditionalNG.setEnabled(false);
        assertFalse( _baseMaleSocket.isActive(), "_baseMaleSocket is not active");
        conditionalNG.setEnabled(true);

        assertTrue( _baseMaleSocket.isActive(), "_baseMaleSocket is active");
        LogixNG logixNG = _baseMaleSocket.getLogixNG();
        assertNotNull( logixNG, "_base has no LogixNG as ancestor");
        logixNG.setEnabled(false);
        assertFalse( _baseMaleSocket.isActive(), "_baseMaleSocket is not active");
        logixNG.setEnabled(true);
        assertTrue( _baseMaleSocket.isActive(), "_baseMaleSocket is active");

        assertTrue( _baseMaleSocket.isActive(), "_baseMaleSocket is active");
        _base.getConditionalNG().setEnabled(false);
        _baseMaleSocket.setParent(null);
        assertTrue( _baseMaleSocket.isActive(), "_baseMaleSocket is active");
    }

    @Test
    public void testConstants() {
        assertTrue( "ChildCount".equals(Base.PROPERTY_CHILD_COUNT), "String matches");
        assertTrue( "SocketConnected".equals(Base.PROPERTY_SOCKET_CONNECTED), "String matches 2");
        assertEquals( 0x02, Base.SOCKET_CONNECTED, "integer matches");
        assertEquals( 0x04, Base.SOCKET_DISCONNECTED, "integer matches 2");
    }

    @Test
    public void testNames() {
        assertNotNull( _base.getSystemName(), "system name not null");
        assertFalse( _base.getSystemName().isEmpty(), "system name is not empty string");

        _base.setUserName("One user name");
        assertTrue( "One user name".equals(_base.getUserName()), "User name matches");
        _base.setUserName("Another user name");
        assertTrue( "Another user name".equals(_base.getUserName()), "User name matches");
        _base.setUserName(null);
        assertNull( _base.getUserName(), "User name matches");
        _base.setUserName("One user name");
        assertTrue( "One user name".equals(_base.getUserName()), "User name matches");
    }

    @Test
    public void testParent() {
        _base.getConditionalNG().setEnabled(false);
        MyBase a = new MyBase();
        _base.setParent(null);
        assertNull( _base.getParent(), "Parent matches");
        _base.setParent(a);
        assertSame( a, _base.getParent(), "Parent matches");
        _base.setParent(null);
        assertNull( _base.getParent(), "Parent matches");
    }

    @Test
    public void testIsEnabled() {
        MyBase a = new MyBase();
        assertTrue( a.isEnabled(), "isEnabled() returns true by default");
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
        assertFalse( _base.getConditionalNG().getRunDelayed(),
            "runOnGUIDelayed is false");
    }

    @Test
    public void testChildAndChildCount() {
        assertEquals( _base.getChildCount(), _baseMaleSocket.getChildCount(), "childCount is equal");
        for (int i=0; i < _base.getChildCount(); i++) {
            assertSame( _base.getChild(i), _baseMaleSocket.getChild(i), "child is equal");
        }
    }

    @Test
    public void testBeanType() {
        assertEquals( ((NamedBean)_base).getBeanType(),
            ((NamedBean)_baseMaleSocket).getBeanType(), "getbeanType() is equal");
    }

    @Test
    public void testDescribeState() {
        assertEquals( "Unknown",
                ((NamedBean)_baseMaleSocket).describeState(NamedBean.UNKNOWN),
                "description matches");
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
        // End here if the item under test doesn't.
        org.junit.Assume.assumeTrue( "Item doesn't support adding new sockets", addNewSocket());

        assertTrue( ab.get(), "PropertyChangeEvent fired");
        assertEquals(Base.PROPERTY_CHILD_COUNT, ar.get().getPropertyName());
        assertInstanceOf( List.class, ar.get().getNewValue());
        List<?> list = (List<?>)ar.get().getNewValue();
        for (Object o : list) {
            assertInstanceOf( FemaleSocket.class, o);
        }
    }

    private static class MyBase extends AbstractBase {

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
        public LogixNG_Category getCategory() {
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
        IndexOutOfBoundsException ex = assertThrows( IndexOutOfBoundsException.class,
            () -> r.run(index), "Exception is thrown");

        String msg1 = String.format("Index: %d, Size: %d", index, arraySize);
        String msg2 = String.format("Index %d out of bounds for length %d", index, arraySize);
        if (!msg1.equals(ex.getMessage()) && !msg2.equals(ex.getMessage())) {
            fail("Wrong error message: " + ex.getMessage());
        }

    }


    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractBaseTestBase.class);

}
