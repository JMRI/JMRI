package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import jmri.*;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrix.internal.InternalAnalogIOManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Test the ProxyAnalogIOManager.
 *
 * @author  Bob Jacobsen 2003, 2006, 2008
 * @author  Daniel Bergqvist Copyright (C) 2020
 */
public class ProxyAnalogIOManagerTest extends AbstractProxyManagerTestBase<ProxyAnalogIOManager, AnalogIO> {

    public String getSystemName(int i) {
        return "JV" + i;
    }

    private AnalogIO newAnalogIO(String sysName, String userName) {
        return new MyAnalogIO(sysName, userName);
    }

    @Test
    public void testDispose() {
        l.dispose();  // all we're really doing here is making sure the method exists
        l = null; // save being re-disposed by afterEach
    }

    @Test
    public void testAnalogIOPutGet() {
        // create
        AnalogIO t = newAnalogIO(getSystemName(getNumToTest1()), "mine");
        l.register(t);
        // check
        assertNotNull( t, "real object returned ");
        assertSame( t, l.getByUserName("mine"), "user name correct ");
        assertSame( t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct ");
    }

    @Test
    public void testSingleObject() {
        // test that you always get the same representation
        AnalogIO t1 = newAnalogIO(getSystemName(getNumToTest1()), "mine");
        l.register(t1);
        assertNotNull( t1, "t1 real object returned ");
        assertSame( t1, l.getByUserName("mine"), "same by user ");
        assertSame( t1, l.getBySystemName(getSystemName(getNumToTest1())), "same by system ");
    }

    @Test
    public void testMisses() {
        // try to get nonexistant lights
        assertNull( l.getByUserName("foo"));
        assertNull( l.getBySystemName("bar"));
    }

    @Test
    public void testRename() {
        // get light
        AnalogIO t1 = newAnalogIO(getSystemName(getNumToTest1()), "before");
        assertNotNull( t1, "t1 real object ");
        l.register(t1);
        t1.setUserName("after");
        AnalogIO t2 = l.getByUserName("after");
        assertEquals( t1, t2, "same object");
        assertNull( l.getByUserName("before"), "no old object");
    }

    @Test
    public void testInstanceManagerIntegration() {
        JUnitUtil.resetInstanceManager();
        assertNotNull(InstanceManager.getDefault(AnalogIOManager.class));

        assertInstanceOf( ProxyAnalogIOManager.class,
            InstanceManager.getDefault(AnalogIOManager.class));

        assertNotNull(InstanceManager.getDefault(AnalogIOManager.class));
        AnalogIO b = newAnalogIO("IV1", null);
        InstanceManager.getDefault(AnalogIOManager.class).register(b);
        assertNotNull(InstanceManager.getDefault(AnalogIOManager.class).getBySystemName("IV1"));

        InternalAnalogIOManager m = new InternalAnalogIOManager(new InternalSystemConnectionMemo("J", "Juliet"));
        InstanceManager.setAnalogIOManager(m);

        b = newAnalogIO("IV2", null);
        InstanceManager.getDefault(AnalogIOManager.class).register(b);
        assertNotNull(InstanceManager.getDefault(AnalogIOManager.class).getBySystemName("IV1"));
        b = newAnalogIO("IV3", null);
        InstanceManager.getDefault(AnalogIOManager.class).register(b);
        assertNotNull(InstanceManager.getDefault(AnalogIOManager.class).getBySystemName("IV1"));

        m.dispose();
    }

    @Test
    public void testLights() {
        // Test that the AnalogIOManager registers variable lights but not
        // lights that are not variable.

        Light light = new MyLight("JL1");
        InstanceManager.getDefault(LightManager.class).register(light);
        AnalogIO analogIO = InstanceManager.getDefault(AnalogIOManager.class).getBySystemName("JL1");
        assertNull( analogIO, "light does not exists in AnalogIOManager");

        // Check that we can deregister light without problem
        InstanceManager.getDefault(LightManager.class).deregister(light);
        analogIO = InstanceManager.getDefault(AnalogIOManager.class).getBySystemName("JL1");
        assertNull( analogIO, "light does not exists in AnalogIOManager");

        Light variableLight = new MyVariableLight("JL2");
        InstanceManager.getDefault(LightManager.class).register(variableLight);
        analogIO = InstanceManager.getDefault(AnalogIOManager.class).getBySystemName("JL2");
        assertNotNull( analogIO, "variable light exists in AnalogIOManager");

        // Check that we can deregister light and that it get deregstered from AnalogIOManager as well
        InstanceManager.getDefault(LightManager.class).deregister(variableLight);
        analogIO = InstanceManager.getDefault(AnalogIOManager.class).getBySystemName("JL2");
        assertNull( analogIO, "light does not exists in AnalogIOManager");
    }

    @Test
    public void testLights_UserName() {
        // Test that the AnalogIOManager registers variable lights but not
        // lights that are not variable.

        Light light = new MyLight("JL1");
        InstanceManager.getDefault(LightManager.class).register(light);
        AnalogIO analogIO = InstanceManager.getDefault(AnalogIOManager.class).getByUserName("A light");
        assertNull( analogIO, "light does not exists in AnalogIOManager");

        // Check that we can deregister light without problem
        InstanceManager.getDefault(LightManager.class).deregister(light);
        analogIO = InstanceManager.getDefault(AnalogIOManager.class).getByUserName("A light");
        assertNull( analogIO, "light does not exists in AnalogIOManager");

        Light variableLight = new MyVariableLight("JL2", "A variable light");
        InstanceManager.getDefault(LightManager.class).register(variableLight);
        analogIO = InstanceManager.getDefault(AnalogIOManager.class).getByUserName("A variable light");
        assertNotNull( analogIO, "variableLight exists in AnalogIOManager");

        // Check that we can deregister variableLight and that it get deregstered from AnalogIOManager as well
        InstanceManager.getDefault(LightManager.class).deregister(variableLight);
        analogIO = InstanceManager.getDefault(AnalogIOManager.class).getByUserName("A variable light");
        assertNull( analogIO, "variableLight does not exists in AnalogIOManager");
    }

    @Test
    public void testOtherBean() {
        // Test that the AnalogIOManager registers a bean from another manager.

        InstanceManager.setDefault(SomeDeviceManager.class, new SomeDeviceManagerImplementation());

        SomeDevice someDevice = new SomeDeviceBean("JL1");
        InstanceManager.getDefault(SomeDeviceManager.class).register(someDevice);
        AnalogIO analogIO = InstanceManager.getDefault(AnalogIOManager.class).getBySystemName("JL1");
        assertNull( analogIO, "someDevice does not exists in AnalogIOManager");

        // Check that we can deregister light without problem
        InstanceManager.getDefault(SomeDeviceManager.class).deregister(someDevice);
        analogIO = InstanceManager.getDefault(AnalogIOManager.class).getBySystemName("JL1");
        assertNull( analogIO, "someDevice does not exists in AnalogIOManager");

        // Tell AnalogIOManager to register SomeDevice beans in the manager
        InstanceManager.getDefault(AnalogIOManager.class)
                .addBeanType(SomeDevice.class, InstanceManager.getDefault(SomeDeviceManager.class));

        SomeDevice anotherSomeDevice = new SomeDeviceBean("JL2");
        InstanceManager.getDefault(SomeDeviceManager.class).register(anotherSomeDevice);
        analogIO = InstanceManager.getDefault(AnalogIOManager.class).getBySystemName("JL2");
        assertNotNull( analogIO, "anotherSomeDevice exists in AnalogIOManager");

        // Check that we can deregister anotherSomeDevice and that it get deregstered from AnalogIOManager as well
        InstanceManager.getDefault(SomeDeviceManager.class).deregister(anotherSomeDevice);
        analogIO = InstanceManager.getDefault(AnalogIOManager.class).getBySystemName("JL2");
        assertNull( analogIO, "anotherSomeDevice does not exists in AnalogIOManager");
    }

    @Test
    public void testAnalogIOManager() {
        AnalogIOManager m = new MyAnalogIOManager();

        Throwable thrown = catchThrowable(() -> {
            m.addBeanType(VariableLight.class, InstanceManager.getDefault(VariableLightManager.class));
        });
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class)
                .hasNoCause();

        thrown = catchThrowable(() -> {
            m.removeBeanType(VariableLight.class, InstanceManager.getDefault(VariableLightManager.class));
        });
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class)
                .hasNoCause();
    }

    /**
     * Number of light to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers.
     *
     * @return the number to test
     */
    protected int getNumToTest1() {
        return 9;
    }

    protected int getNumToTest2() {
        return 7;
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // create and register the manager object
        AnalogIOManager aiom = new InternalAnalogIOManager(new InternalSystemConnectionMemo("J", "Juliet"));

        InstanceManager.setAnalogIOManager(aiom);

        JUnitUtil.initInternalLightManager();
        AnalogIOManager irman = InstanceManager.getDefault(AnalogIOManager.class);
        assertInstanceOf( ProxyAnalogIOManager.class, irman,
            "AnalogIOManager is not a ProxyAnalogIOManager");
        l = (ProxyAnalogIOManager) irman;
    }

    @AfterEach
    public void tearDown() {
        // kill timebase created by variable light
        var timebase = InstanceManager.getNullableDefault(Timebase.class);
        if (timebase != null) {
            timebase.setRun(false);
            for (var listener : timebase.getMinuteChangeListeners()) {
                timebase.removeMinuteChangeListener(listener);
            }
        }

        if ( l != null ) {
            l.dispose();
        }

        JUnitUtil.tearDown();
    }


    private static class MyAnalogIO extends AbstractNamedBean implements AnalogIO {

        double _value = 0.0;

        MyAnalogIO(String sys, String userName) {
            super(sys, userName);
        }

        @Override
        public void setState(int s) throws JmriException {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public void setState(double value) throws JmriException {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public double getState(double v) {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public String getBeanType() {
            return "AnalogIO";
        }

        @Override
        public void setCommandedAnalogValue(double value) throws JmriException {
            _value = value;
        }

        @Override
        public double getCommandedAnalogValue() {
            return _value;
        }

        @Override
        public double getMin() {
            return Float.MIN_VALUE;
        }

        @Override
        public double getMax() {
            return Float.MAX_VALUE;
        }

        @Override
        public double getResolution() {
            return 0.1;
        }

        @Override
        public AbsoluteOrRelative getAbsoluteOrRelative() {
            return AbsoluteOrRelative.ABSOLUTE;
        }

    }


    private static class MyLight extends jmri.implementation.AbstractLight {

        MyLight(String systemName) {
            super(systemName);
        }

    }


    private static class MyVariableLight extends jmri.implementation.AbstractVariableLight {

        MyVariableLight(String systemName) {
            super(systemName);
        }

        MyVariableLight(String systemName, String userName) {
            super(systemName, userName);
        }

        @Override
        protected void sendIntensity(double intensity) {
            // No need to implement this method for this test
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        protected void sendOnOffCommand(int newState) {
            // No need to implement this method for this test
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        protected int getNumberOfSteps() {
            // No need to implement this method for this test
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

    }


    private interface SomeDevice extends AnalogIO {
    }

    private static class SomeDeviceBean extends AbstractNamedBean implements SomeDevice {

        SomeDeviceBean(String systemName) {
            super(systemName);
        }

        @Override
        public void setState(int s) throws JmriException {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public void setState(double value) throws JmriException {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public double getState(double v) {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public String getBeanType() {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public void setCommandedAnalogValue(double value) throws JmriException {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public double getCommandedAnalogValue() {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public double getMin() {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public double getMax() {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public double getResolution() {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

        @Override
        public AbsoluteOrRelative getAbsoluteOrRelative() {
            throw new UnsupportedOperationException("Not supported in "+getDisplayName());
        }

    }


    private interface SomeDeviceManager extends Manager<SomeDevice> {
    }

    private static class SomeDeviceManagerImplementation
            extends AbstractManager<SomeDevice> implements SomeDeviceManager {

        SomeDeviceManagerImplementation() {
            super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }

        @Override
        public char typeLetter() {
            return '$';
        }

        @Override
        public Class<SomeDevice> getNamedBeanClass() {
            return SomeDevice.class;
        }

        @Override
        public int getXMLOrder() {
            throw new UnsupportedOperationException("Not supported");
        }

        @Override
        public String getBeanTypeHandled(boolean plural) {
            throw new UnsupportedOperationException("Not supported");
        }

    }


    private static class MyAnalogIOManager extends AbstractAnalogIOManager {

        MyAnalogIOManager() {
            super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }

    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProxyAnalogIOManagerTest.class);

}
