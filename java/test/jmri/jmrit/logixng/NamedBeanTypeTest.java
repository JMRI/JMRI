package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
            if (Manager.class.equals(c)) {
                continue;
            }
            if (ProvidingManager.class.equals(c)) {
                continue;
            }
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
    private void testNamedBeanType(NamedBeanType namedBeanType, Class<?> rootManagerClass) {

        Manager<? extends NamedBean> instanceManagerManager =
                (Manager<? extends NamedBean>)InstanceManager.getDefault(rootManagerClass);

        NamedBeanType.CreateBean createBean = namedBeanType.getCreateBean();
        NamedBeanType.DeleteBean deleteBean = namedBeanType.getDeleteBean();

        if (createBean != null) {
            String namedBeanName = namedBeanType.getManager()
                    .getSystemNamePrefix()
                    + CreateLogixNGTreeScaffold.getRandomString(20);

            String namedBeanUserName = "UserName_" + CreateLogixNGTreeScaffold.getRandomString(20);

            NamedBean namedBean;
            if (jmri.jmrit.logixng.GlobalVariable.class.isAssignableFrom(namedBeanType.getClazz())) {
                namedBeanName = InstanceManager.getDefault(GlobalVariableManager.class).getAutoSystemName();
            }
            namedBean = createBean.createBean(namedBeanName, namedBeanUserName);
            assertNotNull( namedBean, "Manager: "+rootManagerClass.getName());


            assertNotNull(namedBeanType.getManager().getBySystemName(namedBeanName));
            assertNotNull(namedBeanType.getManager().getByUserName(namedBeanUserName));
            assertNotNull(instanceManagerManager.getBySystemName(namedBeanName));
            assertNotNull(instanceManagerManager.getByUserName(namedBeanUserName));

//                deleteBean.deleteBean(namedBean, "CanDelete");

            assertNotNull(namedBeanType.getManager().getBySystemName(namedBeanName));
            assertNotNull(namedBeanType.getManager().getByUserName(namedBeanUserName));
            assertNotNull(instanceManagerManager.getBySystemName(namedBeanName));
            assertNotNull(instanceManagerManager.getByUserName(namedBeanUserName));

            assertDoesNotThrow( () -> deleteBean.deleteBean(namedBean, "DoDelete"),
                "No exception expected: " + namedBeanType.getManager().getClass().getName());

            assertNull(namedBeanType.getManager().getBySystemName(namedBeanName));
            assertNull(namedBeanType.getManager().getByUserName(namedBeanUserName));
            assertNull(instanceManagerManager.getBySystemName(namedBeanName));
            assertNull(instanceManagerManager.getByUserName(namedBeanUserName));

        }


    }

    @Test
    @SuppressWarnings("unchecked")  // Static analysis complains despite checked by Assert
    public void testNamedBeanType() {
        Map<NamedBeanType, Class<?>> rootManagerClassMap = new HashMap<>();

        // Map<NamedBeanType, Manager<? extends NamedBean>> map = new HashMap<>();

        Map<NamedBeanType, Manager<? extends NamedBean>> instanceManagerManagerMap = new HashMap<>();


        // Ensure we have cleared everything
        setUp();
        setUp();
        for (NamedBeanType namedBeanType : NamedBeanType.values()) {
            // map.put(namedBeanType, namedBeanType.getManager());

            Class<?> rootManagerClass = getRootManagerClass(namedBeanType.getManager());
            assertNotNull(rootManagerClass);

            rootManagerClassMap.put(namedBeanType, rootManagerClass);

//            System.out.format("Type: %s, Manager: %s, Super manager: %s%n",
//                    namedBeanType.name(),
//                    namedBeanType.getManager(),
//                    rootManagerClass.getName());

            Object instanceManagerManagerObj = InstanceManager.getDefault(rootManagerClass);
            assertNotNull(instanceManagerManagerObj);
            Manager<? extends NamedBean> instanceManagerManager =
                assertInstanceOf( Manager.class, instanceManagerManagerObj);
            instanceManagerManagerMap.put(namedBeanType, instanceManagerManager);

            assertEquals(instanceManagerManager, namedBeanType.getManager());

            testNamedBeanType(namedBeanType, rootManagerClass);
        }


        // Ensure we have cleared everything
        setUp();
        setUp();

        for (NamedBeanType namedBeanType : NamedBeanType.values()) {
            Manager<? extends NamedBean> instanceManagerManager =
                    instanceManagerManagerMap.get(namedBeanType);

            assertNotEquals(instanceManagerManager, namedBeanType.getManager());

            testNamedBeanType(namedBeanType, rootManagerClassMap.get(namedBeanType));
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
