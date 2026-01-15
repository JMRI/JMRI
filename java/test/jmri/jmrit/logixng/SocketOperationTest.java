package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.swing.SwingTools;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Try to do a lot of random female socket operations on the female sockets
 * to see if it works. This test does not checks the result, but at least
 * tries to check if a socket operation throws an exception.
 */
public class SocketOperationTest {

    private CreateLogixNGTreeScaffold createLogixNGTreeScaffold;

    @Test
    @DisabledIfHeadless
    public void testAddRemoveChildren() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        runTest(true, false);
    }

    @Test
    @DisabledIfHeadless
    public void testFemaleSockets() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        runTest(false, true);
    }

    @Test
    @DisabledIfHeadless
    public void testAddRemoveChildrenAndFemaleSockets() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        runTest(true, true);
    }

//    int countIterations = 0;
//    final int total = 873;
    private final Set<Base> listOfSockets = new HashSet<>();
    private final Map<Class<? extends Base>, SwingConfiguratorInterface> sciSet = new HashMap<>();

    private void runTest(boolean addRemoveChildren, boolean testSocketOperations) {

        // Add new LogixNG actions and expressions to jmri.jmrit.logixng.CreateLogixNGTreeScaffold
        assertDoesNotThrow( () -> createLogixNGTreeScaffold.createLogixNGTree() );
        

        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);

        java.util.Set<LogixNG> newLogixNG_Set = new java.util.HashSet<>(logixNG_Manager.getNamedBeanSet());
        for (LogixNG aLogixNG : newLogixNG_Set) {
            for (int i=0; i < aLogixNG.getNumConditionalNGs(); i++) {
                final int ii = i;
                assertDoesNotThrow( () -> {

                    aLogixNG.getConditionalNG(ii).forEntireTreeWithException((b) -> {

                        // System.out.format("Count: %3d / %3d, Percent: %2d%n", ++countIterations, total, (int)(100.0*countIterations/total));

                        if (b instanceof FemaleSocket) {

                            FemaleSocket originSocket = (FemaleSocket)b;
                            if (originSocket.isConnected()) {

                                Base socket = originSocket.getConnectedSocket();
                                listOfSockets.add(socket);
                            }
                        }
                    });
                });
                assertDoesNotThrow( () -> testSockets(addRemoveChildren, testSocketOperations) );
            }
        }
    }

    private void testSockets(boolean addRemoveChildren, boolean testSocketOperations) throws SocketAlreadyConnectedException {

        for (Base socket : listOfSockets) {
            for (int count=0; count < 100; count++) {

                if (socket.getChildCount() > 0) {
                    FemaleSocket child = socket.getChild(
                            JUnitUtil.getRandom().nextInt(socket.getChildCount()));

                    if (addRemoveChildren) {
                        testAddRemoveChild(child);
                    }

                    if (testSocketOperations) {
                        testFemaleSocketOperations(child);
                    }
                }
            }
        }
    }

    private void testAddRemoveChild(FemaleSocket child) throws SocketAlreadyConnectedException {
        int oper = JUnitUtil.getRandom().nextInt(5);

        switch (oper) {
            case 0:  // Add
                if (!child.isConnected()) {
                    tryToAddChild(child);
                }
                break;
            case 1:  // Remove
                if (child.isConnected()) {
                    assertTrue(child.canDisconnect());
                    assertTrue(child.isConnected());
                    child.disconnect();
                    assertFalse(child.isConnected());
                }
                break;
            default:
                // Do nothing
        }
    }

    @SuppressWarnings("null") // if (categoryList.isEmpty()) { false positive, should be fixed in JUnit6
    private void tryToAddChild(FemaleSocket child) throws SocketAlreadyConnectedException {

        assertFalse(child.isConnected());

        Map<Category, List<Class<? extends Base>>> connectableClasses =
                child.getConnectableClasses();

        assertFalse(connectableClasses.isEmpty());

        int count = 0;
        List<Class<? extends Base>> categoryList = null;

        Set<Category> categorySet = new HashSet<>();
        while (count++ < 200 && (categoryList == null || categoryList.isEmpty())) {
            Category category = Category.values().get(JUnitUtil.getRandom().nextInt(Category.values().size()));
            categoryList = connectableClasses.get(category);
            categorySet.add(category);
        }

        assertNotNull(categoryList);

        if (categoryList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Category c : categorySet) {
                sb.append(c.name());
                sb.append(", ");
            }
            log.error("Item: {}, child: {}, Category: {}", child.getParent().getLongDescription(), child.getName(), sb.toString());
        }


        assertFalse(categoryList.isEmpty());

        Class<? extends Base> clazz = categoryList.get(JUnitUtil.getRandom().nextInt(categoryList.size()));

        SwingConfiguratorInterface sci = sciSet.get(clazz);
        if (sci == null) {
            sci = SwingTools.getSwingConfiguratorForClass(clazz);
            sci.setJDialog(new javax.swing.JDialog());
            sci.getConfigPanel(new javax.swing.JPanel());
            sciSet.put(clazz, sci);
        }

        sci.setDefaultValues();

        MaleSocket maleSocket = sci.createNewObject(sci.getAutoSystemName(), null);
        child.connect(maleSocket);

        assertTrue(child.isConnected());
    }

    private void testFemaleSocketOperations(FemaleSocket child) {
        FemaleSocketOperation fso = FemaleSocketOperation.values()[
                JUnitUtil.getRandom().nextInt(FemaleSocketOperation.values().length)];

        if (child.isSocketOperationAllowed(fso)) {
            child.doSocketOperation(fso);
        }
    }

    @BeforeEach
    public void setUp() {
        createLogixNGTreeScaffold = new CreateLogixNGTreeScaffold();
        createLogixNGTreeScaffold.setUp();
    }

    @AfterEach
    public void tearDown() {
//        JUnitAppender.clearBacklog();    // REMOVE THIS!!!
        createLogixNGTreeScaffold.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SocketOperationTest.class);

}
