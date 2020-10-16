package jmri.jmrit.logixng;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test that all the action and expression classes are registed and have
 * configurexml and swing classes.
 * 
 * @author Daniel Bergqvist 2020
 */
public class ActionsAndExpressionsTest {
    
    private boolean errorsFound = false;
    private int classesToCheck = 0;
    
    
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
            
            classesToCheck++;
            boolean isRegistered = false;
            for (Map.Entry<Category, List<Class<? extends Base>>> entry : registeredClasses.entrySet()) {
                for (Class<? extends Base> c : entry.getValue()) {
                    if (c.getName().equals(packageName+"."+file)) isRegistered = true;
                }
            }
            
            if (!isRegistered) {
                System.out.format("Class %s is not registered in the manager%n", file);
                errorsFound = true;
            }
            
            Object configureXml = null;
            String fullConfigName = packageName + ".configurexml." + file + "Xml";
            log.debug("getAdapter looks for {}", fullConfigName);
            try {
                Class<?> configClass = Class.forName(fullConfigName);
                configureXml = configClass.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
            }
            if (configureXml == null) {
                System.out.format("Class %s has no configurexml class%n", file);
                errorsFound = true;
            }
            
            Object configureSwing = null;
            fullConfigName = packageName + ".swing." + file + "Swing";
            log.debug("getAdapter looks for {}", fullConfigName);
            try {
                Class<?> configClass = Class.forName(fullConfigName);
                configureSwing = configClass.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
            }
            if (configureSwing == null) {
                System.out.format("Class %s has no swing class%n", file);
                errorsFound = true;
            }
//            Assert.assertNotNull(String.format("Class %s has no swing class%n", file), configureXml);
        }
    }
    
    private Path getPath(String subFolder) {
        return FileSystems.getDefault().getPath("java/src/jmri/jmrit/logixng/" + subFolder);
    }
    
    public Map<Category, List<Class<? extends Base>>> getDigitalExpressionClasses() {
        return InstanceManager.getDefault(DigitalExpressionManager.class).getExpressionClasses();
    }
    
    @Test
    public void testGetBeanType() {
        checkFolder(
                getPath("analog/actions"),
                "jmri.jmrit.logixng.analog.actions",
                getDigitalExpressionClasses(),
                new String[]{"AbstractAnalogAction","Bundle","Factory"});
        
        checkFolder(
                getPath("analog/expressions"),
                "jmri.jmrit.logixng.analog.expressions",
                getDigitalExpressionClasses(),
                new String[]{"AbstractDigitalExpression","Bundle","Factory"});
        
        checkFolder(
                getPath("digital/actions"),
                "jmri.jmrit.logixng.digital.actions",
                getDigitalExpressionClasses(),
                new String[]{"AbstractDigitalAction","Bundle","Factory"});
        
        checkFolder(
                getPath("digital/boolean_actions"),
                "jmri.jmrit.logixng.digital.boolean_actions",
                getDigitalExpressionClasses(),
                new String[]{"AbstractDigitalBooleanAction","Bundle","Factory"});
        
        checkFolder(
                getPath("digital/expressions"),
                "jmri.jmrit.logixng.digital.expressions",
                getDigitalExpressionClasses(),
                new String[]{"AbstractDigitalExpression","Bundle","Factory"});
        
        checkFolder(
                getPath("string/actions"),
                "jmri.jmrit.logixng.string.expressions",
                getDigitalExpressionClasses(),
                new String[]{"AbstractStringAction","Bundle","Factory"});
        
        checkFolder(
                getPath("string/expressions"),
                "jmri.jmrit.logixng.string.expressions",
                getDigitalExpressionClasses(),
                new String[]{"AbstractStringExpression","Bundle","Factory"});
        
        System.out.format("Num classes checked: %d%n", classesToCheck);
        
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
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionsAndExpressionsTest.class);
    
}
