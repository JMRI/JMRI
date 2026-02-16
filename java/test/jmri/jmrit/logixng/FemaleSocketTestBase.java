package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.*;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JDialog;
import javax.swing.JPanel;

import jmri.*;
import jmri.Manager.NameValidity;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.swing.SwingTools;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.apache.commons.lang3.mutable.MutableInt;

import org.junit.jupiter.api.Test;

import org.slf4j.Logger;

/**
 * Base class for testing FemaleStringExpressionSocket classes
 *
 * @author Daniel Bergqvist 2018
 */
public abstract class FemaleSocketTestBase {

    protected Manager<? extends NamedBean> manager;
    protected AtomicBoolean flag;
    protected AtomicBoolean errorFlag;
    protected MaleSocket maleSocket;
    protected MaleSocket otherMaleSocket;
    protected FemaleSocket _femaleSocket;


    /**
     * Get the manager that handles the type of beans that can connect to this
     * female socket
     * @return the manager
     */
    protected abstract Manager<? extends NamedBean> getManager();


    private SortedSet<String> getClassNames(List<Class<? extends Base>> classes) {
        SortedSet<String> set = new TreeSet<>();

        // If the category doesn't exist in one of the sets, 'classes' is null.
        if (classes == null) {
            return set;
        }

        for (Class<? extends Base> clazz : classes) {
            set.add(clazz.getName());
        }

        return set;
    }

    private boolean isSetsEqual(Category category, SortedSet<String> set1, SortedSet<String> set2) {
        for (String s1 : set1) {
            if (!set2.contains(s1)) {
                System.out.format("set1 contains %s in category %s which is missing in set2%n", s1, category.name());
                return false;
            }
        }
        for (String s2 : set2) {
            if (!set1.contains(s2)) {
                System.out.format("set2 contains %s in category %s which is missing in set1%n", s2, category.name());
                return false;
            }
        }
        if (set1.size() != set2.size()) {
            System.out.format("set1 and set2 has different sizes: %d, %d%n", set1.size(), set2.size());
            return false;
        }
        return true;
    }

    /**
     * Asserts that the two maps of connection classes holds the same classes.
     * The reason to why this method returns a boolean is because every test
     * method is expected to have an assertion. And the statistical analysis is
     * not able to see that that assertion is here, so we therefore also need
     * an assertion in the caller method.
     * @param expectedMap the expected result
     * @param actualMap the actual result
     * @return true if assertion is correct
     */
    public final boolean isConnectionClassesEquals(
            Map<Category, List<Class<? extends Base>>> expectedMap,
            Map<Category, List<Class<? extends Base>>> actualMap) {

        List<Class<? extends Base>> classes;
/*
        for (Category category : Category.values()) {
            System.out.format("FemaleSocket: %s, category: %s%n",
                    femaleSocket.getClass().getName(),
                    category.name());
            classes = femaleSocket.getConnectableClasses().get(category);
            for (Class<? extends Base> clazz : classes) {
                System.out.format("FemaleSocket: %s, category: %s, class: %s%n",
                        femaleSocket.getClass().getName(),
                        category.name(),
                        clazz.getName());
            }
        }
*/
        for (Category category : Category.values()) {

            if (!isSetsEqual(
                    category,
                    getClassNames(expectedMap.get(category)),
                    getClassNames(actualMap.get(category)))) {

                System.err.format("Set of classes are different for category %s:%n", category.name());

                classes = _femaleSocket.getConnectableClasses().get(category);
                for (Class<? extends Base> clazz : classes) {
                    System.err.format("Set of classes are different:%n");
                    System.err.format("FemaleSocket: %s, category: %s, class: %s%n",
                            _femaleSocket.getClass().getName(),
                            category.name(),
                            clazz.getName());
/*
                    log.error("Set of classes are different:");
                    log.error("FemaleSocket: {}, category: {}, class: {}",
                            femaleSocket.getClass().getName(),
                            category.name(),
                            clazz.getName());
*/
                }

                return false;
            }
        }

        // We will not get here if assertion fails.
        return true;
    }

    /**
     * Returns a new FemaleSocket with the specified name.
     * The method is used to test that the constructor throws an exception if
     * invalid name.
     * @param name the name of the socket
     * @return the new female socket
     */
    protected abstract FemaleSocket getFemaleSocket(String name);

    @Test
    public void testBadSocketName() {
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () ->
            getFemaleSocket("----"), "exception thrown");
        assertNotNull(e);
    }

    abstract protected boolean hasSocketBeenSetup();

    @Test
    public void testSetup() throws SocketAlreadyConnectedException {
        assertFalse( _femaleSocket.isConnected(), "not connected");

        // Check that we can call setup() even if the socket is not connected.
        _femaleSocket.setup();

        _femaleSocket.connect(maleSocket);
        assertTrue( _femaleSocket.isConnected(), "is connected");
        assertFalse( hasSocketBeenSetup(), "not setup");
        _femaleSocket.setup();
        assertTrue( hasSocketBeenSetup(), "is setup");
    }

    @Test
    public void testConnectIncompatibleSocket() {
        MaleSocket incompatibleSocket = new IncompatibleMaleSocket();
        assertFalse( _femaleSocket.isCompatible(incompatibleSocket), "socket not compatible");

        // Test connect incompatible male socket
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            _femaleSocket.connect(incompatibleSocket), "socket is not compatible");
        assertNotNull(ex);

        // Test connect null
        NullPointerException npe = assertThrows( NullPointerException.class, () ->
            _femaleSocket.connect(null), "cannot connect socket that is null");
        assertNotNull(npe);

    }

    @Test
    public void testConnect() throws SocketAlreadyConnectedException {

        // Test connect male socket
        flag.set(false);

        _femaleSocket.connect(maleSocket);

        assertTrue( flag.get(), "Socket is connected");

        // Test connect male socket when already connected
        flag.set(false);
        SocketAlreadyConnectedException ex = assertThrows( SocketAlreadyConnectedException.class, () ->
            _femaleSocket.connect(otherMaleSocket), "Socket already connected error");
        assertNotNull(ex);

        assertFalse( flag.get(), "Socket was not connected again");
    }

    @Test
    public void testDisconnect() throws SocketAlreadyConnectedException {

        // Ensure the socket is connected before this test.
        if (!_femaleSocket.isConnected()) {
            _femaleSocket.connect(maleSocket);
        }

        // Test disconnect male socket
        flag.set(false);
        _femaleSocket.disconnect();

        assertTrue( flag.get(), "Socket is disconnected");

        // Test connect male socket
        flag.set(false);
        errorFlag.set(false);
        _femaleSocket.disconnect();

        assertFalse( flag.get(), "Socket is not disconnected again");
    }

    @Test
    public void testSetParentForAllChildren() throws SocketAlreadyConnectedException {
        assertFalse( _femaleSocket.isConnected(), "femaleSocket is not connected");
        assertTrue( _femaleSocket.setParentForAllChildren(new ArrayList<>()));
        assertNull( maleSocket.getParent(), "malesocket.getParent() is null");
        _femaleSocket.connect(maleSocket);
        assertTrue( _femaleSocket.setParentForAllChildren(new ArrayList<>()));
        assertEquals( _femaleSocket, maleSocket.getParent(), "malesocket.getParent() is femaleSocket");
    }

    @Test
    public void testValidateName() {
        // Valid names
        assertTrue(_femaleSocket.validateName("Abc"));
        assertTrue(_femaleSocket.validateName("abc"));
        assertTrue(_femaleSocket.validateName("Abc123"));
        assertTrue(_femaleSocket.validateName("A123bc"));
        assertTrue(_femaleSocket.validateName("Abc___"));
        assertTrue(_femaleSocket.validateName("Abc___fsdffs"));
        assertTrue(_femaleSocket.validateName("Abc3123__2341fsdf"));

        // Invalid names
        assertFalse(_femaleSocket.validateName("12Abc"));  // Starts with a digit
        assertFalse(_femaleSocket.validateName("_Abc"));   // Starts with an underscore
        assertFalse(_femaleSocket.validateName(" Abc"));   // Starts with a non letter
        assertFalse(_femaleSocket.validateName("A bc"));   // Has a character that's not letter, digit or underscore
        assertFalse(_femaleSocket.validateName("A{bc"));   // Has a character that's not letter, digit or underscore
        assertFalse(_femaleSocket.validateName("A+bc"));   // Has a character that's not letter, digit or underscore
    }

    private boolean setName_verifyException(String newName, String expectedExceptionMessage) {
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            _femaleSocket.setName(newName));
        assertEquals( expectedExceptionMessage, ex.getMessage(), "Error message is correct");
        return true;
    }

    @Test
    public void testSetName() {
        // Both letters and digits is OK
        _femaleSocket.setName("X12");
        assertEquals( "X12", _femaleSocket.getName(), "name matches");

        // Only letters is OK
        _femaleSocket.setName("Xyz");
        assertEquals( "Xyz", _femaleSocket.getName(), "name matches");

        // Both letters and digits in random order is OK as long as the first
        // character is a letter
        _femaleSocket.setName("X1b2c3Y");
        assertEquals( "X1b2c3Y", _femaleSocket.getName(), "name matches");

        // Underscore is also a valid letter
        _femaleSocket.setName("X1b2___c3Y");
        assertEquals( "X1b2___c3Y", _femaleSocket.getName(), "name matches");

        // The name must start with a letter, not a digit
        assertTrue( setName_verifyException("123", "the name is not valid: 123"));

        // The name must not contain any spaces
        assertTrue( setName_verifyException(" A123", "the name is not valid:  A123"), "exception is thrown");
        assertTrue( setName_verifyException("A1 23", "the name is not valid: A1 23"), "exception is thrown");
        assertTrue( setName_verifyException("A123 ", "the name is not valid: A123 "), "exception is thrown");

        // The name must not contain any character that's not a letter nor a digit
        assertTrue( setName_verifyException("A12!3", "the name is not valid: A12!3"), "exception is thrown");
        assertTrue( setName_verifyException("A+123", "the name is not valid: A+123"), "exception is thrown");
        assertTrue( setName_verifyException("=A123", "the name is not valid: =A123"), "exception is thrown");
        assertTrue( setName_verifyException("A12*3", "the name is not valid: A12*3"), "exception is thrown");
        assertTrue( setName_verifyException("A123/", "the name is not valid: A123/"), "exception is thrown");
        assertTrue( setName_verifyException("A12(3", "the name is not valid: A12(3"), "exception is thrown");
        assertTrue( setName_verifyException("A12)3", "the name is not valid: A12)3"), "exception is thrown");
        assertTrue( setName_verifyException("A12[3", "the name is not valid: A12[3"), "exception is thrown");
        assertTrue( setName_verifyException("A12]3", "the name is not valid: A12]3"), "exception is thrown");
        assertTrue( setName_verifyException("A12{3", "the name is not valid: A12{3"), "exception is thrown");
        assertTrue( setName_verifyException("A12}3", "the name is not valid: A12}3"), "exception is thrown");
    }

    @Test
    public void testDisposeWithoutChild() {
        _femaleSocket.dispose();
        assertFalse( _femaleSocket.isConnected(), "socket not connected");
    }

    @Test
    public void testDisposeWithChild() throws SocketAlreadyConnectedException {
        assertFalse( _femaleSocket.isConnected(), "socket not connected");
        _femaleSocket.connect(maleSocket);
        assertTrue( _femaleSocket.isConnected(), "socket is connected");
        _femaleSocket.dispose();
        assertFalse( _femaleSocket.isConnected(), "socket not connected");
    }

    @Test
    public void testMethodsThatAreNotSupported() {
        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            _femaleSocket.printTree((PrintWriter)null, "", new MutableInt(0)),
                "method not supported");
        assertNotNull(ex);

        ex = assertThrows( UnsupportedOperationException.class, () ->
            _femaleSocket.printTree((Locale)null, (PrintWriter)null, "", new MutableInt(0)),
                "method not supported");
        assertNotNull(ex);

        ex = assertThrows( UnsupportedOperationException.class, () ->
            _femaleSocket.getCategory(),
                "method not supported");
        assertNotNull(ex);

        ex = assertThrows( UnsupportedOperationException.class, () ->
            _femaleSocket.getChild(0),
                "method not supported");
        assertNotNull(ex);

        ex = assertThrows( UnsupportedOperationException.class, () ->
            _femaleSocket.getChildCount(),
                "method not supported");
        assertNotNull(ex);

        ex = assertThrows( UnsupportedOperationException.class, () ->
            _femaleSocket.getUserName(),
                "method not supported");
        assertNotNull(ex);

        ex = assertThrows( UnsupportedOperationException.class, () ->
            _femaleSocket.setUserName("aaa"),
                "method not supported");
        assertNotNull(ex);

    }

    @Test
    @DisabledIfHeadless
    public void testCategory() {

        JDialog dialog = new JDialog();

        // Test that the classes method getCategory() returns the same value as
        // the factory.
        Map<Category, List<Class<? extends Base>>> map = _femaleSocket.getConnectableClasses();

        for (Map.Entry<Category, List<Class<? extends Base>>> entry : map.entrySet()) {

            for (Class<? extends Base> clazz : entry.getValue()) {
                // The class SwingToolsTest does not have a swing configurator
                SwingConfiguratorInterface iface = SwingTools.getSwingConfiguratorForClass(clazz);
                iface.setJDialog(dialog);
                iface.getConfigPanel(new JPanel());
                Base obj = iface.createNewObject(iface.getAutoSystemName(), null);
                assertEquals( entry.getKey(), obj.getCategory(),
                    "category is correct for "+((MaleSocket)obj).getObject().getClass().getName());
//                Assert.assertEquals("category is correct for "+obj.getShortDescription(), entry.getKey(), obj.getCategory());
            }
        }
    }

    @Test
    public void testSWISystemName() {   // SWI = SwingConfiguratorInterface
        Map<Category, List<Class<? extends Base>>> map = _femaleSocket.getConnectableClasses();

        for (Map.Entry<Category, List<Class<? extends Base>>> entry : map.entrySet()) {

            for (Class<? extends Base> clazz : entry.getValue()) {
                // The class SwingToolsTest does not have a swing configurator
                SwingConfiguratorInterface iface = SwingTools.getSwingConfiguratorForClass(clazz);
                assertEquals( NameValidity.VALID,
                        getManager().validSystemNameFormat(iface.getExampleSystemName()),
                        "example system name is correct for "+clazz);
                assertEquals( NameValidity.VALID,
                        getManager().validSystemNameFormat(iface.getAutoSystemName()),
                        "auto system name is correct for "+clazz);
            }
        }
    }



    private static class IncompatibleMaleSocket implements MaleSocket {

        @Override
        public void setEnabled(boolean enable) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setEnabledFlag(boolean enable) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean isEnabled() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Base getObject() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setDebugConfig(DebugConfig config) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public DebugConfig getDebugConfig() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public DebugConfig createDebugConfig() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getSystemName() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getUserName() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setUserName(String s) throws NamedBean.BadUserNameException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getComment() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void setComment(String s) {
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
        public ConditionalNG getConditionalNG() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public LogixNG getLogixNG() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public LogixNG getRoot() {
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
        public boolean setParentForAllChildren(List<String> errors) {
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
        public void dispose() {
            throw new UnsupportedOperationException("Not supported.");
        }
/*
        @Override
        public void registerListeners() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void unregisterListeners() {
            throw new UnsupportedOperationException("Not supported.");
        }
*/
        @Override
        public void printTree(PrintTreeSettings settings, PrintWriter writer, String indent, MutableInt lineNumber) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void printTree(PrintTreeSettings settings, Locale locale, PrintWriter writer, String indent, MutableInt lineNumber) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void printTree(PrintTreeSettings settings, Locale locale, PrintWriter writer, String indent, String currentIndent, MutableInt lineNumber) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean isActive() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListeners() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public ErrorHandlingType getErrorHandlingType() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setErrorHandlingType(ErrorHandlingType errorHandlingType) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener, String name, String listenerRef) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener, String name, String listenerRef) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void updateListenerRef(PropertyChangeListener l, String newName) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getListenerRef(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public ArrayList<String> getListenerRefs() {
            throw new UnsupportedOperationException("Not supported");
        }

        /** {@inheritDoc} */
        @Override
        public void getListenerRefsIncludingChildren(List<String> list) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public int getNumPropertyChangeListeners() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public PropertyChangeListener[] getPropertyChangeListenersByReference(String name) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public BaseManager<? extends NamedBean> getManager() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void addLocalVariable(String name, SymbolTable.InitialValueType initialValueType, String initialValueData) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void addLocalVariable(SymbolTable.VariableData variableData) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void clearLocalVariables() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public List<SymbolTable.VariableData> getLocalVariables() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void registerListeners() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void unregisterListeners() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base getDeepCopy(Map<String, String> map, Map<String, String> map1) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public Base deepCopyChildren(Base base, Map<String, String> map, Map<String, String> map1) throws JmriException {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean getListen() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setListen(boolean listen) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void getUsageDetail(int level, NamedBean bean, List<jmri.NamedBeanUsageReport> report, NamedBean cdl) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void getUsageTree(int level, NamedBean bean, List<jmri.NamedBeanUsageReport> report, NamedBean cdl) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean isLocked() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setLocked(boolean locked) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean isSystem() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setSystem(boolean system) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public boolean getCatchAbortExecution() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void setCatchAbortExecution(boolean catchAbortExecution) {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public void handleError(Base item, String message, JmriException e, Logger log) throws JmriException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void handleError(Base item, String message, List<String> messageList, JmriException e, Logger log) throws JmriException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void handleError(Base item, String message, RuntimeException e, Logger log) throws JmriException {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FemaleSocketTestBase.class);

}
