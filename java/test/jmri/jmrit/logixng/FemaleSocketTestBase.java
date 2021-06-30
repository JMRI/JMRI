package jmri.jmrit.logixng;

import java.beans.*;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;

import jmri.*;
import jmri.Manager.NameValidity;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.swing.SwingTools;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Assert;
import org.junit.Test;

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
        if (classes == null) return set;

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
        boolean hasThrown = false;
        try {
            getFemaleSocket("----");
        } catch (IllegalArgumentException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }

    abstract protected boolean hasSocketBeenSetup();

    @Test
    public void testSetup() throws SocketAlreadyConnectedException {
        Assert.assertFalse("not connected", _femaleSocket.isConnected());

        // Check that we can call setup() even if the socket is not connected.
        _femaleSocket.setup();

        _femaleSocket.connect(maleSocket);
        Assert.assertTrue("is connected", _femaleSocket.isConnected());
        Assert.assertFalse("not setup", hasSocketBeenSetup());
        _femaleSocket.setup();
        Assert.assertTrue("is setup", hasSocketBeenSetup());
    }

    @Test
    public void testConnectIncompatibleSocket() {
        MaleSocket incompatibleSocket = new IncompatibleMaleSocket();
        Assert.assertFalse("socket not compatible", _femaleSocket.isCompatible(incompatibleSocket));

        // Test connect incompatible male socket
        errorFlag.set(false);
        try {
            _femaleSocket.connect(incompatibleSocket);
        } catch (IllegalArgumentException ex) {
            errorFlag.set(true);
        } catch (SocketAlreadyConnectedException ex) {
            // We shouldn't be here.
            Assert.fail("socket is already connected");
        }

        Assert.assertTrue("socket is not compatible", errorFlag.get());

        // Test connect null
        errorFlag.set(false);
        try {
            _femaleSocket.connect(null);
        } catch (NullPointerException ex) {
            errorFlag.set(true);
        } catch (SocketAlreadyConnectedException ex) {
            // We shouldn't be here.
            Assert.fail("socket is already connected");
        }

        Assert.assertTrue("cannot connect socket that is null", errorFlag.get());
    }

    @Test
    public void testConnect() {

        // Test connect male socket
        flag.set(false);
        errorFlag.set(false);
        try {
            _femaleSocket.connect(maleSocket);
        } catch (SocketAlreadyConnectedException ex) {
            errorFlag.set(true);
        }

        Assert.assertTrue("Socket is connected", flag.get());
        Assert.assertFalse("No error", errorFlag.get());

        // Test connect male socket when already connected
        flag.set(false);
        errorFlag.set(false);
        try {
            _femaleSocket.connect(otherMaleSocket);
        } catch (SocketAlreadyConnectedException ex) {
            errorFlag.set(true);
        }

        Assert.assertFalse("Socket was not connected again", flag.get());
        Assert.assertTrue("Socket already connected error", errorFlag.get());
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

        Assert.assertTrue("Socket is disconnected", flag.get());

        // Test connect male socket
        flag.set(false);
        errorFlag.set(false);
        _femaleSocket.disconnect();

        Assert.assertFalse("Socket is not disconnected again", flag.get());
    }

    @Test
    public void testSetParentForAllChildren() throws SocketAlreadyConnectedException {
        Assert.assertFalse("femaleSocket is not connected", _femaleSocket.isConnected());
        if (! _femaleSocket.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        Assert.assertNull("malesocket.getParent() is null", maleSocket.getParent());
        _femaleSocket.connect(maleSocket);
        if (! _femaleSocket.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        Assert.assertEquals("malesocket.getParent() is femaleSocket", _femaleSocket, maleSocket.getParent());
    }

    @Test
    public void testValidateName() {
        // Valid names
        Assert.assertTrue(_femaleSocket.validateName("Abc"));
        Assert.assertTrue(_femaleSocket.validateName("abc"));
        Assert.assertTrue(_femaleSocket.validateName("Abc123"));
        Assert.assertTrue(_femaleSocket.validateName("A123bc"));
        Assert.assertTrue(_femaleSocket.validateName("Abc___"));
        Assert.assertTrue(_femaleSocket.validateName("Abc___fsdffs"));
        Assert.assertTrue(_femaleSocket.validateName("Abc3123__2341fsdf"));
        
        // Invalid names
        Assert.assertFalse(_femaleSocket.validateName("12Abc"));  // Starts with a digit
        Assert.assertFalse(_femaleSocket.validateName("_Abc"));   // Starts with an underscore
        Assert.assertFalse(_femaleSocket.validateName(" Abc"));   // Starts with a non letter
        Assert.assertFalse(_femaleSocket.validateName("A bc"));   // Has a character that's not letter, digit or underscore
        Assert.assertFalse(_femaleSocket.validateName("A{bc"));   // Has a character that's not letter, digit or underscore
        Assert.assertFalse(_femaleSocket.validateName("A+bc"));   // Has a character that's not letter, digit or underscore
    }
    
    private boolean setName_verifyException(String newName, String expectedExceptionMessage) {
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        try {
            _femaleSocket.setName(newName);
        } catch (IllegalArgumentException ex) {
            hasThrown.set(true);
            Assert.assertTrue("Error message is correct", ex.getMessage().equals(expectedExceptionMessage));
        }
        return hasThrown.get();
    }

    @Test
    public void testSetName() {
        // Both letters and digits is OK
        _femaleSocket.setName("X12");
        Assert.assertTrue("name matches", "X12".equals(_femaleSocket.getName()));

        // Only letters is OK
        _femaleSocket.setName("Xyz");
        Assert.assertTrue("name matches", "Xyz".equals(_femaleSocket.getName()));

        // Both letters and digits in random order is OK as long as the first
        // character is a letter
        _femaleSocket.setName("X1b2c3Y");
        Assert.assertTrue("name matches", "X1b2c3Y".equals(_femaleSocket.getName()));

        // Underscore is also a valid letter
        _femaleSocket.setName("X1b2___c3Y");
        Assert.assertTrue("name matches", "X1b2___c3Y".equals(_femaleSocket.getName()));

        // The name must start with a letter, not a digit
        Assert.assertTrue("exception is thrown", setName_verifyException("123", "the name is not valid: 123"));

        // The name must not contain any spaces
        Assert.assertTrue("exception is thrown", setName_verifyException(" A123", "the name is not valid:  A123"));
        Assert.assertTrue("exception is thrown", setName_verifyException("A1 23", "the name is not valid: A1 23"));
        Assert.assertTrue("exception is thrown", setName_verifyException("A123 ", "the name is not valid: A123 "));

        // The name must not contain any character that's not a letter nor a digit
        Assert.assertTrue("exception is thrown", setName_verifyException("A12!3", "the name is not valid: A12!3"));
        Assert.assertTrue("exception is thrown", setName_verifyException("A+123", "the name is not valid: A+123"));
        Assert.assertTrue("exception is thrown", setName_verifyException("=A123", "the name is not valid: =A123"));
        Assert.assertTrue("exception is thrown", setName_verifyException("A12*3", "the name is not valid: A12*3"));
        Assert.assertTrue("exception is thrown", setName_verifyException("A123/", "the name is not valid: A123/"));
        Assert.assertTrue("exception is thrown", setName_verifyException("A12(3", "the name is not valid: A12(3"));
        Assert.assertTrue("exception is thrown", setName_verifyException("A12)3", "the name is not valid: A12)3"));
        Assert.assertTrue("exception is thrown", setName_verifyException("A12[3", "the name is not valid: A12[3"));
        Assert.assertTrue("exception is thrown", setName_verifyException("A12]3", "the name is not valid: A12]3"));
        Assert.assertTrue("exception is thrown", setName_verifyException("A12{3", "the name is not valid: A12{3"));
        Assert.assertTrue("exception is thrown", setName_verifyException("A12}3", "the name is not valid: A12}3"));
    }

    @Test
    public void testDisposeWithoutChild() {
        _femaleSocket.dispose();
        Assert.assertFalse("socket not connected", _femaleSocket.isConnected());
    }

    @Test
    public void testDisposeWithChild() throws SocketAlreadyConnectedException {
        Assert.assertFalse("socket not connected", _femaleSocket.isConnected());
        _femaleSocket.connect(maleSocket);
        Assert.assertTrue("socket is connected", _femaleSocket.isConnected());
        _femaleSocket.dispose();
        Assert.assertFalse("socket not connected", _femaleSocket.isConnected());
    }

    @Test
    public void testMethodsThatAreNotSupported() {
        errorFlag.set(false);
        try {
            _femaleSocket.printTree((PrintWriter)null, "", new MutableInt(0));
        } catch (UnsupportedOperationException ex) {
            errorFlag.set(true);
        }
        Assert.assertTrue("method not supported", errorFlag.get());

        errorFlag.set(false);
        try {
            _femaleSocket.printTree((Locale)null, (PrintWriter)null, "", new MutableInt(0));
        } catch (UnsupportedOperationException ex) {
            errorFlag.set(true);
        }
        Assert.assertTrue("method not supported", errorFlag.get());

        errorFlag.set(false);
        try {
            _femaleSocket.getCategory();
        } catch (UnsupportedOperationException ex) {
            errorFlag.set(true);
        }
        Assert.assertTrue("method not supported", errorFlag.get());

        errorFlag.set(false);
        try {
            _femaleSocket.isExternal();
        } catch (UnsupportedOperationException ex) {
            errorFlag.set(true);
        }
        Assert.assertTrue("method not supported", errorFlag.get());

        errorFlag.set(false);
        try {
            _femaleSocket.getChild(0);
        } catch (UnsupportedOperationException ex) {
            errorFlag.set(true);
        }
        Assert.assertTrue("method not supported", errorFlag.get());

        errorFlag.set(false);
        try {
            _femaleSocket.getChildCount();
        } catch (UnsupportedOperationException ex) {
            errorFlag.set(true);
        }
        Assert.assertTrue("method not supported", errorFlag.get());

        errorFlag.set(false);
        try {
            _femaleSocket.getUserName();
        } catch (UnsupportedOperationException ex) {
            errorFlag.set(true);
        }
        Assert.assertTrue("method not supported", errorFlag.get());

        errorFlag.set(false);
        try {
            _femaleSocket.setUserName("aaa");
        } catch (UnsupportedOperationException ex) {
            errorFlag.set(true);
        }
        Assert.assertTrue("method not supported", errorFlag.get());
    }

    @Test
    public void testCategory() {
        // Test that the classes method getCategory() returns the same value as
        // the factory.
        Map<Category, List<Class<? extends Base>>> map = _femaleSocket.getConnectableClasses();

        for (Map.Entry<Category, List<Class<? extends Base>>> entry : map.entrySet()) {

            for (Class<? extends Base> clazz : entry.getValue()) {
                // The class SwingToolsTest does not have a swing configurator
                SwingConfiguratorInterface iface = SwingTools.getSwingConfiguratorForClass(clazz);
                iface.getConfigPanel(new JPanel());
                Base obj = iface.createNewObject(iface.getAutoSystemName(), null);
                Assert.assertEquals("category is correct for "+((MaleSocket)obj).getObject().getClass().getName(), entry.getKey(), obj.getCategory());
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
                Assert.assertEquals("example system name is correct for "+clazz,
                        NameValidity.VALID,
                        getManager().validSystemNameFormat(iface.getExampleSystemName()));
                Assert.assertEquals("auto system name is correct for "+clazz,
                        NameValidity.VALID,
                        getManager().validSystemNameFormat(iface.getAutoSystemName()));
            }
        }
    }



    private class IncompatibleMaleSocket implements MaleSocket {

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
        public boolean isExternal() {
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
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FemaleSocketTestBase.class);

}
