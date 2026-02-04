package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;

import jmri.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.swing.SwingTools;
import jmri.util.*;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test the swing classes for the LogixNG actions and expressions
 *
 * @author Daniel Bergqvist 2022
 */
public class TestSwingClasses {

    private JDialog dialog;

    private Map<Category, List<Class<? extends Base>>> getAnalogActionClasses() {
        return InstanceManager.getDefault(AnalogActionManager.class).getActionClasses();
    }

    private Map<Category, List<Class<? extends Base>>> getAnalogExpressionClasses() {
        return InstanceManager.getDefault(AnalogExpressionManager.class).getExpressionClasses();
    }

    private Map<Category, List<Class<? extends Base>>> getDigitalActionClasses() {
        return InstanceManager.getDefault(DigitalActionManager.class).getActionClasses();
    }

    private Map<Category, List<Class<? extends Base>>> getDigitalBooleanActionClasses() {
        return InstanceManager.getDefault(DigitalBooleanActionManager.class).getActionClasses();
    }

    private Map<Category, List<Class<? extends Base>>> getDigitalExpressionClasses() {
        return InstanceManager.getDefault(DigitalExpressionManager.class).getExpressionClasses();
    }

    private Map<Category, List<Class<? extends Base>>> getStringActionClasses() {
        return InstanceManager.getDefault(StringActionManager.class).getActionClasses();
    }

    private Map<Category, List<Class<? extends Base>>> getStringExpressionClasses() {
        return InstanceManager.getDefault(StringExpressionManager.class).getExpressionClasses();
    }

    private void testClass(Class<? extends Base> clazz) {
        SwingConfiguratorInterface configureSwing;
        configureSwing = SwingTools.getSwingConfiguratorForClass(clazz);
        configureSwing.setJDialog(dialog);
        JDialog testDialog  = new JDialog((JFrame)null, "Test dialog", false);
        JPanel panel = configureSwing.getConfigPanel(new JPanel());
        testDialog.getContentPane().add(panel);
        testDialog.pack();
        ThreadingUtil.runOnGUI(() -> { testDialog.setVisible(true); });
        String systemName = configureSwing.getAutoSystemName();
        Base object = configureSwing.createNewObject(systemName, null);
//        Base object = configureSwing.createNewObject(configureSwing.getAutoSystemName(), null);
        while (object instanceof MaleSocket) {
            object = ((MaleSocket)object).getObject();
        }
        List<String> errorMessages = new ArrayList<>();
        configureSwing.setDefaultValues();
        boolean validationResult = configureSwing.validate(errorMessages);
        String objectShort = object.getShortDescription();
        assertTrue( errorMessages.isEmpty(), () -> {
            String toReturn = "Error messages is not empty for " + objectShort + ", "
                + "class: " + configureSwing.getClass().getName() + System.lineSeparator();
            for (String s : errorMessages) {
                toReturn += s + System.lineSeparator();
            }
            return toReturn;
        });
        assertTrue(validationResult);
        configureSwing.updateObject(object);
        assertTrue(configureSwing.canClose());
//        try { Thread.sleep(500); } catch (InterruptedException e) {}
        testDialog.dispose();
    }

    public void testClasses(Map<Category, List<Class<? extends Base>>> newClasses) {
        newClasses.entrySet().forEach((entry) -> {
            entry.getValue().forEach((clazz) -> {
//                System.out.format("Add action/expression: %s, %s%n", entry.getKey().name(), clazz.getName());
                testClass(clazz);
            });
        });
    }

    @Test
    @DisabledIfHeadless
    public void testSwingClasses() {

        dialog = new JDialog();

        testClasses(getAnalogActionClasses());
        testClasses(getDigitalActionClasses());
        testClasses(getDigitalBooleanActionClasses());
        testClasses(getStringActionClasses());
        testClasses(getAnalogExpressionClasses());
        testClasses(getDigitalExpressionClasses());
        testClasses(getStringExpressionClasses());
    }

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initLogixNGManager();

        InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        InstanceManager.getDefault(ReporterManager.class).provide("IR1");

        // Temporary let the error messages from this test be shown to the user
//        JUnitAppender.end();
    }

    @AfterEach
    public void tearDown() {
        if ( dialog != null ) {
            dialog.dispose();
        }
        dialog = null;
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }


    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestSwingClasses.class);

}
