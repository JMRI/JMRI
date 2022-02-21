package jmri.jmrit.logixng;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JDialog;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
// import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

// import org.apache.log4j.Level;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
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

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    private boolean errorsFound = false;
    private final Set<String> languages = new HashSet<>();
    private final Set<Locale> locales = new HashSet<>();


    @Test
    private void getLocales() throws IOException {
        Path path = FileSystems.getDefault().getPath("java/src/jmri/jmrit/logixng/");

        Files.walk(path)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith(".properties"))
                .filter(p -> p.getFileName().toString().contains("_"))
                .forEach(p -> {
                    String file = p.getFileName().toString();
                    languages.add(file.substring(file.indexOf("_")+1, file.length()-".properties".length()));
                });

        for (String lang : languages) {
//            System.out.format("Language: '%s'%n", lang);
            locales.add(new Locale(lang));
        }

    }

    private void checkFolder(Path path, String packageName, Map<Category, List<Class<? extends Base>>> registeredClasses, String[] classesToIgnore) {

        JDialog dialog = new JDialog();

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
                configureSwing.setJDialog(dialog);
                configureSwing.getConfigPanel(new JPanel());

                MaleSocket socket = configureSwing.createNewObject(configureSwing.getAutoSystemName(), null);
                MaleSocket lastMaleSocket = socket;
                Base base = socket;
                while ((base != null) && (base instanceof MaleSocket)) {
                    lastMaleSocket = (MaleSocket) base;
                    base = ((MaleSocket)base).getObject();
                }
                Assert.assertNotNull(base);
                Assert.assertEquals("SwingConfiguratorInterface creates an object of correct type", base.getClass().getName(), packageName+"."+file);
//                System.out.format("Swing: %s, Class: %s, class: %s%n", configureSwing.toString(), socket.getShortDescription(), socket.getObject().getClass().getName());
                Assert.assertEquals("Swing class has correct name", socket.getShortDescription(), configureSwing.toString());
//                System.out.format("MaleSocket class: %s, socket class: %s%n",
//                        configureSwing.getManager().getMaleSocketClass().getName(),
//                        socket.getClass().getName());
                Assert.assertTrue(configureSwing.getManager().getMaleSocketClass().isAssignableFrom(lastMaleSocket.getClass()));

                // Test all locales. This mainly tests that the female socket
                // names are valid for each locale, for example that the name
                // doesn't contain any spaces.
                for (Locale locale : locales) {
                    Locale.setDefault(locale);
                    configureSwing.createNewObject(configureSwing.getAutoSystemName(), null);
                }
                Locale.setDefault(DEFAULT_LOCALE);
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

    public void addClasses(Map<Category, List<Class<? extends Base>>> classes, Map<Category, List<Class<? extends Base>>> newClasses) {
            newClasses.entrySet().forEach((entry) -> {
//                System.out.format("Add action: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                entry.getValue().forEach((clazz) -> {
                    classes.get(entry.getKey()).add(clazz);
                });
            });
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testGetBeanType() {
        Map<Category, List<Class<? extends Base>>> classes = new HashMap<>();
        for (Category category : Category.values()) {
            classes.put(category, new ArrayList<>());
        }
        addClasses(classes, getAnalogActionClasses());
        addClasses(classes, getDigitalActionClasses());
        addClasses(classes, getDigitalBooleanActionClasses());
        addClasses(classes, getStringActionClasses());

        checkFolder(
                getPath("actions"),
                "jmri.jmrit.logixng.actions",
                classes,
                new String[]{
                    "Bundle",
                    "NamedBeanType",
                    "AbstractAnalogAction","AnalogFactory",         // Analog
                    "AbstractDigitalAction","ActionAtomicBoolean","AbstractScriptDigitalAction","DigitalFactory",   // Digital
                    "AbstractDigitalBooleanAction","DigitalBooleanFactory",     // Boolean digital
                    "AbstractStringAction","StringFactory"          // String
                });


        classes = new HashMap<>();
        for (Category category : Category.values()) {
            classes.put(category, new ArrayList<>());
        }
        addClasses(classes, getAnalogExpressionClasses());
        addClasses(classes, getDigitalExpressionClasses());
        addClasses(classes, getStringExpressionClasses());

        checkFolder(
                getPath("expressions"),
                "jmri.jmrit.logixng.expressions",
                classes,
                new String[]{
                    "Bundle",
                    "AbstractAnalogExpression","AnalogFactory",     // Analog
                    "AbstractDigitalExpression","AbstractScriptDigitalExpression","DigitalFactory",     // Digital
                    "AbstractStringExpression","StringFactory"      // String
                });

        Assert.assertFalse("No errors found", errorsFound);
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        // Get the list of languages LogixNG has been translated to
        getLocales();

        // Temporary let the error messages from this test be shown to the user
//        JUnitAppender.end();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionsAndExpressionsTest.class);

}
