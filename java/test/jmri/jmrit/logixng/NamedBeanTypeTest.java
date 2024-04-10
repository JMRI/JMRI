package jmri.jmrit.logixng;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.*;

/**
 * Test NamedBeanType.
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class NamedBeanTypeTest {

    // Find an interface that's a manager for this manager class
    private Class<?> getSuperManager(Class<?> managerClass) {
        Map<Integer, Class<?>> classMap = new HashMap<>();
        Class<?>[] interfaces = managerClass.getInterfaces();
        for (Class<?> c : interfaces) {
            if (Manager.class.equals(c)) continue;
            if (ProvidingManager.class.equals(c)) continue;
//            System.out.format("%s, %d%n", c.getName(), c.getInterfaces().length);
            if (Manager.class.isAssignableFrom(c)) {
                classMap.put(c.getInterfaces().length, c);
            }
        }
        for (int i=0; i < 100; i++) {
            if (classMap.containsKey(i)) {
                return classMap.get(i);
            }
        }
        return null;
    }

    // Find the root manager class for this manager, for example
    // jmri.SensorManager for jmri.managers.ProxySensorManager
    private Class<?> getRootManagerClass(Manager<? extends NamedBean> manager) {
        Class<?> managerClass = manager.getClass();
        Class<?> superManagerClass = getSuperManager(managerClass);
        if (superManagerClass == null && managerClass.getSuperclass() != null) {
            managerClass = managerClass.getSuperclass();
            superManagerClass = getSuperManager(managerClass);
        }

        if (superManagerClass == null) {
            superManagerClass = manager.getClass();
        }

        return superManagerClass;
    }

    @SuppressWarnings("unchecked")  // Unchecked cast due to type erasure
    private void testNamedBeanType(NamedBeanType namedBeanType, Class<?> rootManagerClass)
            throws JmriException {

        Manager<? extends NamedBean> instanceManagerManager =
                (Manager<? extends NamedBean>)InstanceManager.getDefault(rootManagerClass);

        NamedBeanType.CreateBean createBean = namedBeanType.getCreateBean();
        NamedBeanType.DeleteBean deleteBean = namedBeanType.getDeleteBean();

        if (createBean != null) {
            String prefix = namedBeanType.getManager().getSystemNamePrefix();
            if (NamedBeanType.Audio.equals(namedBeanType)) {
                prefix += Audio.BUFFER;
            } else if (NamedBeanType.GlobalVariable.equals(namedBeanType)) {
                prefix = ((GlobalVariableManager)namedBeanType.getManager()).getSubSystemNamePrefix()
                        + "$";  // System names with dollar sign allow any characters in the name
            }
            String namedBeanName = prefix
                    + CreateLogixNGTreeScaffold.getRandomString(20);

            String namedBeanUserName = "UserName__" + CreateLogixNGTreeScaffold.getRandomString(20);

            NamedBean namedBean = createBean.createBean(namedBeanName, namedBeanUserName);
            Assert.assertNotNull("Manager: "+rootManagerClass.getName(), namedBean);

            try {
                Assert.assertNotNull(namedBeanType.getManager().getBySystemName(namedBeanName));
                Assert.assertNotNull(namedBeanType.getManager().getByUserName(namedBeanUserName));
                Assert.assertNotNull(instanceManagerManager.getBySystemName(namedBeanName));
                Assert.assertNotNull(instanceManagerManager.getByUserName(namedBeanUserName));

//                deleteBean.deleteBean(namedBean, "CanDelete");

                Assert.assertNotNull(namedBeanType.getManager().getBySystemName(namedBeanName));
                Assert.assertNotNull(namedBeanType.getManager().getByUserName(namedBeanUserName));
                Assert.assertNotNull(instanceManagerManager.getBySystemName(namedBeanName));
                Assert.assertNotNull(instanceManagerManager.getByUserName(namedBeanUserName));

                deleteBean.deleteBean(namedBean, "DoDelete");

                Assert.assertNull(namedBeanType.getManager().getBySystemName(namedBeanName));
                Assert.assertNull(namedBeanType.getManager().getByUserName(namedBeanUserName));
                Assert.assertNull(instanceManagerManager.getBySystemName(namedBeanName));
                Assert.assertNull(instanceManagerManager.getByUserName(namedBeanUserName));
            } catch (PropertyVetoException e) {
                Assert.fail("No exception expected: "+e.toString()+", "+namedBeanType.getManager().getClass().getName());
            }
        }


    }

    @Test
    @SuppressWarnings("unchecked")  // Static analysis complains despite checked by Assert
    public void testNamedBeanType() throws JmriException {
        Map<NamedBeanType, Class<?>> rootManagerClassMap = new HashMap<>();

        Map<NamedBeanType, Manager<? extends NamedBean>> map = new HashMap<>();

        Map<NamedBeanType, Manager<? extends NamedBean>> instanceManagerManagerMap = new HashMap<>();


        // Ensure we have cleared everything
        setUp();
        setUp();
        for (NamedBeanType namedBeanType : NamedBeanType.values()) {
            map.put(namedBeanType, namedBeanType.getManager());

            Class<?> rootManagerClass = getRootManagerClass(namedBeanType.getManager());
            Assert.assertNotNull(rootManagerClass);

            rootManagerClassMap.put(namedBeanType, rootManagerClass);

//            System.out.format("Type: %s, Manager: %s, Super manager: %s%n",
//                    namedBeanType.name(),
//                    namedBeanType.getManager(),
//                    rootManagerClass.getName());

            Object instanceManagerManagerObj = InstanceManager.getDefault(rootManagerClass);
            Assert.assertNotNull(instanceManagerManagerObj);
            Assert.assertTrue(instanceManagerManagerObj instanceof Manager);
            Manager<? extends NamedBean> instanceManagerManager = (Manager<? extends NamedBean>) instanceManagerManagerObj;
            instanceManagerManagerMap.put(namedBeanType, instanceManagerManager);

            Assert.assertEquals(instanceManagerManager, namedBeanType.getManager());

            testNamedBeanType(namedBeanType, rootManagerClass);
        }


        // Ensure we have cleared everything
        setUp();
        setUp();

        for (NamedBeanType namedBeanType : NamedBeanType.values()) {
            Manager<? extends NamedBean> instanceManagerManager =
                    instanceManagerManagerMap.get(namedBeanType);

            Assert.assertNotEquals(instanceManagerManager, namedBeanType.getManager());

            testNamedBeanType(namedBeanType, rootManagerClassMap.get(namedBeanType));
        }
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
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        InstanceManager.getDefault(AudioManager.class).cleanup();
//        jmri.util.JUnitAppender.clearBacklog(); // REMOVE THIS!!!
        JUnitUtil.tearDown();
    }

}
