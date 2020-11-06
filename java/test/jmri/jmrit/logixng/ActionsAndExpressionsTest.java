package jmri.jmrit.logixng;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
// import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

// import org.apache.log4j.Level;
import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test that all the action and expression classes are registed and have
 * configurexml and swing classes.
 * <P>
 * Requirements that this class checks for:
 * 
 * Each action and expression needs to
 * * be registered in its manager
 * * have a configurexml class
 * * have a swing configurator class
 * * have a test class for its swing configurator class
 * 
 * @author Daniel Bergqvist 2020
 */
public class ActionsAndExpressionsTest {
    
    private boolean errorsFound = false;
    
    
    private void checkFolder(Path path, String packageName, Map<Category, List<Class<? extends Base>>> registeredClasses, String[] classesToIgnore) {
        
        Set<String> files = Stream.of(path.toFile().listFiles())
                  .filter(file -> !file.isDirectory())
                  .map(File::getName)
                  .collect(Collectors.toSet());
        
        filesLoop:
        for (String file : files) {
            if (file.endsWith(".properties")) continue;
            
            file = file.substring(0, file.indexOf('.'));
            
            for (String c : classesToIgnore) {
                if (file.equals(c)) continue filesLoop;
            }
            
            // Check that all actions and expressions is registered in its manager
            Set<Class<? extends Base>> setOfClasses = new HashSet<>();
            boolean isRegistered = false;
            for (Map.Entry<Category, List<Class<? extends Base>>> entry : registeredClasses.entrySet()) {
                for (Class<? extends Base> c : entry.getValue()) {
//                    System.out.format("Registered class: %s%n", c.getName());
                    if (c.getName().equals(packageName+"."+file)) isRegistered = true;
                    
                    Assert.assertFalse(String.format("Class %s is registered more than once in the manager", packageName+"."+c.getName()), setOfClasses.contains(c));
                    
                    setOfClasses.add(c);
//                    if (setOfClasses.contains(c)) {
//                        System.out.format("Class %s is registered more than once in the manager", packageName+"."+c.getName());
//                        errorsFound = true;
//                    } else {
//                        setOfClasses.add(c);
//                    }
                }
            }
            
//            if (!isRegistered) {
//                System.out.format("Class %s.%s is not registered in the manager%n", packageName, file);
//                errorsFound = true;
//            }
            Assert.assertTrue(String.format("Class %s is registred%n", file), isRegistered);
            
            String fullConfigName;
            
            // Ignore this for now
            
            // Check that all actions and expressions has a xml class
            Object configureXml = null;
            fullConfigName = packageName + ".configurexml." + file + "Xml";
            log.debug("getAdapter looks for {}", fullConfigName);
            try {
                Class<?> configClass = Class.forName(fullConfigName);
                configureXml = configClass.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
            }
            if (configureXml == null) {
                System.out.format("Class %s.%s has no configurexml class%n", packageName, file);
                errorsFound = true;
            }
            
            // Disable for now
//            Assert.assertNotNull(String.format("Class %s has xml class%n", file), configureXml);
            
            // Check that all actions and expressions has a swing class
            SwingConfiguratorInterface configureSwing = null;
            fullConfigName = packageName + ".swing." + file + "Swing";
            log.debug("getAdapter looks for {}", fullConfigName);
            try {
                Class<?> configClass = Class.forName(fullConfigName);
                configureSwing = (SwingConfiguratorInterface)configClass.getDeclaredConstructor().newInstance();
                configureSwing.getConfigPanel(new JPanel());
                MaleSocket socket = configureSwing.createNewObject(configureSwing.getAutoSystemName(), null);
                Assert.assertEquals("SwingConfiguratorInterface creates an object of correct type", socket.getObject().getClass().getName(), packageName+"."+file);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
            }
//            if (configureSwing == null) {
//                System.out.format("Class %s.%s has no swing class%n", packageName, file);
//                errorsFound = true;
//            }
            Assert.assertNotNull(String.format("Class %s has swing class%n", file), configureSwing);
            
            // Ignore for now
/*            
            // Check that all actions and expressions has a test class for the swing class
//            Class configureSwingTest = null;
            fullConfigName = packageName + ".swing." + file + "SwingTest";
            log.debug("getAdapter looks for {}", fullConfigName);
            Class<?> configClass = null;
            try {
                configClass = Class.forName(fullConfigName);
            } catch (ClassNotFoundException e) {
            }
            if (configClass == null) {
                System.out.format("Class %s.%s has no test class for its swing class%n", packageName, file);
                errorsFound = true;
            }
//            Assert.assertNotNull("The swing class has a test class", configClass);
*/          
            
/*            
            System.out.format("Class: %s%n", packageName+"."+file);
            Level severity = Level.ERROR; // level at or above which we'll complain
            boolean unexpectedMessageSeen = JUnitAppender.unexpectedMessageSeen(severity);
            String unexpectedMessageContent = JUnitAppender.unexpectedMessageContent(severity);
            JUnitAppender.verifyNoBacklog();
            JUnitAppender.resetUnexpectedMessageFlags(severity);
            Assert.assertFalse("Unexpected "+severity+" or higher messages emitted: "+unexpectedMessageContent, unexpectedMessageSeen);
//            JUnitAppender.assertNoErrorMessage();
*/            
        }
    }
    
    private Path getPath(String subFolder) {
        return FileSystems.getDefault().getPath("java/src/jmri/jmrit/logixng/" + subFolder);
    }
    
    public Map<Category, List<Class<? extends Base>>> getAnalogActionClasses() {
        return InstanceManager.getDefault(AnalogActionManager.class).getActionClasses();
    }
    
    public Map<Category, List<Class<? extends Base>>> getAnalogExpressionClasses() {
        return InstanceManager.getDefault(AnalogExpressionManager.class).getExpressionClasses();
    }
    
    public Map<Category, List<Class<? extends Base>>> getDigitalActionClasses() {
        return InstanceManager.getDefault(DigitalActionManager.class).getActionClasses();
    }
    
    public Map<Category, List<Class<? extends Base>>> getDigitalBooleanActionClasses() {
        return InstanceManager.getDefault(DigitalBooleanActionManager.class).getActionClasses();
    }
    
    public Map<Category, List<Class<? extends Base>>> getDigitalExpressionClasses() {
        return InstanceManager.getDefault(DigitalExpressionManager.class).getExpressionClasses();
    }
    
    public Map<Category, List<Class<? extends Base>>> getStringActionClasses() {
        return InstanceManager.getDefault(StringActionManager.class).getActionClasses();
    }
    
    public Map<Category, List<Class<? extends Base>>> getStringExpressionClasses() {
        return InstanceManager.getDefault(StringExpressionManager.class).getExpressionClasses();
    }
    
    @Test
    public void testGetBeanType() {
        checkFolder(
                getPath("analog/actions"),
                "jmri.jmrit.logixng.analog.actions",
                getAnalogActionClasses(),
                new String[]{"AbstractAnalogAction","Bundle","Factory"});
        
        checkFolder(
                getPath("analog/expressions"),
                "jmri.jmrit.logixng.analog.expressions",
                getAnalogExpressionClasses(),
                new String[]{"AbstractAnalogExpression","Bundle","Factory"});
        
        checkFolder(
                getPath("actions"),
                "jmri.jmrit.logixng.actions",
                getDigitalActionClasses(),
                new String[]{"AbstractDigitalAction","ActionAtomicBoolean","AbstractScriptDigitalAction","Bundle","Factory"});
        
        checkFolder(
                getPath("digital/boolean_actions"),
                "jmri.jmrit.logixng.digital.boolean_actions",
                getDigitalBooleanActionClasses(),
                new String[]{"AbstractDigitalBooleanAction","Bundle","Factory"});
        
        checkFolder(
                getPath("digital/expressions"),
                "jmri.jmrit.logixng.digital.expressions",
                getDigitalExpressionClasses(),
                new String[]{"AbstractDigitalExpression","AbstractScriptDigitalExpression","Bundle","Factory"});
        
        checkFolder(
                getPath("string/actions"),
                "jmri.jmrit.logixng.string.actions",
                getStringActionClasses(),
                new String[]{"AbstractStringAction","Bundle","Factory"});
        
        checkFolder(
                getPath("string/expressions"),
                "jmri.jmrit.logixng.string.expressions",
                getStringExpressionClasses(),
                new String[]{"AbstractStringExpression","Bundle","Factory"});
        
        Assert.assertFalse("No errors found", errorsFound);
    }
    
    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
        
        // Temporary let the error messages from this test be shown to the user
//        JUnitAppender.end();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionsAndExpressionsTest.class);
    
}
