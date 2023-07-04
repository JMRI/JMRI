package jmri.jmrit.logixng;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.swing.SwingTools;
import jmri.util.JUnitUtil;

import org.junit.*;

/**
 * Try to do a lot of random female socket operations on the female sockets
 * to see if it works. This test does not checks the result, but at least
 * tries to check if a socket operation throws an exception.
 */
public class SocketOperationTest {

    private CreateLogixNGTreeScaffold createLogixNGTreeScaffold;

    @Test
    public void testAddRemoveChildren() throws PropertyVetoException, Exception {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        runTest(true, false);
    }

    @Test
    public void testFemaleSockets() throws PropertyVetoException, Exception {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        runTest(false, true);
    }

    @Test
    public void testAddRemoveChildrenAndFemaleSockets() throws PropertyVetoException, Exception {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        runTest(true, true);
    }

//    int countIterations = 0;
//    final int total = 873;
    Set<Base> listOfSockets = new HashSet<>();
    Map<Class<? extends Base>, SwingConfiguratorInterface> sciSet = new HashMap<>();

    private void runTest(boolean addRemoveChildren, boolean testSocketOperations)
            throws PropertyVetoException, Exception {

        // Add new LogixNG actions and expressions to jmri.jmrit.logixng.CreateLogixNGTreeScaffold
        createLogixNGTreeScaffold.createLogixNGTree();

        LogixNG_Manager logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);

        java.util.Set<LogixNG> newLogixNG_Set = new java.util.HashSet<>(logixNG_Manager.getNamedBeanSet());
        for (LogixNG aLogixNG : newLogixNG_Set) {
            for (int i=0; i < aLogixNG.getNumConditionalNGs(); i++) {

                aLogixNG.getConditionalNG(i).forEntireTreeWithException((b) -> {

//                    System.out.format("Count: %3d / %3d, Percent: %2d%n", ++countIterations, total, (int)(100.0*countIterations/total));

                    if (b instanceof FemaleSocket) {

                        FemaleSocket originSocket = (FemaleSocket)b;
                        if (originSocket.isConnected()) {

                            Base socket = originSocket.getConnectedSocket();
                            listOfSockets.add(socket);
                        }
                    }
                });

                testSockets(addRemoveChildren, testSocketOperations);
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
                    Assert.assertTrue(child.canDisconnect());
                    Assert.assertTrue(child.isConnected());
                    child.disconnect();
                    Assert.assertFalse(child.isConnected());
                }
                break;
            default:
                // Do nothing
        }
    }

    private void tryToAddChild(FemaleSocket child) throws SocketAlreadyConnectedException {

        Assert.assertFalse(child.isConnected());

        Map<Category, List<Class<? extends Base>>> connectableClasses =
                child.getConnectableClasses();

        Assert.assertFalse(connectableClasses.isEmpty());

        int count = 0;
        List<Class<? extends Base>> categoryList = null;

        Set<Category> categorySet = new HashSet<>();
        while (count++ < 200 && (categoryList == null || categoryList.isEmpty())) {
            Category category = Category.values().get(JUnitUtil.getRandom().nextInt(Category.values().size()));
            categoryList = connectableClasses.get(category);
            categorySet.add(category);
        }

        Assert.assertNotNull(categoryList);

        if (categoryList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Category c : categorySet) {
                sb.append(c.name());
                sb.append(", ");
            }
            log.error("Item: {}, child: {}, Category: {}", child.getParent().getLongDescription(), child.getName(), sb.toString());
        }


        Assert.assertFalse(categoryList.isEmpty());

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

        Assert.assertTrue(child.isConnected());
    }

    private void testFemaleSocketOperations(FemaleSocket child) {
        FemaleSocketOperation fso = FemaleSocketOperation.values()[
                JUnitUtil.getRandom().nextInt(FemaleSocketOperation.values().length)];

        if (child.isSocketOperationAllowed(fso)) {
            child.doSocketOperation(fso);
        }
    }

    @Before
    public void setUp() {
        createLogixNGTreeScaffold = new CreateLogixNGTreeScaffold();
        createLogixNGTreeScaffold.setUp();
    }

    @After
    public void tearDown() {
//        JUnitAppender.clearBacklog();    // REMOVE THIS!!!
        createLogixNGTreeScaffold.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeepCopyTest.class);

}
