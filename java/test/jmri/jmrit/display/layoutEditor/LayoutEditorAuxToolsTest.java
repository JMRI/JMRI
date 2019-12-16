package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.io.File;
import jmri.InstanceManager;
import jmri.configurexml.*;
import jmri.jmrit.display.EditorFrameOperator;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.Operator;

/**
 * Test simple functioning of LayoutEditorAuxTools
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutEditorAuxToolsTest {


    @Rule   // 5 second timeout for methods in this test class.
    public Timeout globalTimeout = Timeout.seconds(5);

    @Rule   // allow 3 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(3);

    private static EditorFrameOperator layoutEditorFrameOperator = null;
    private static LayoutEditor layoutEditor = null;
    private static LayoutEditorAuxTools layoutEditorAuxTools = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        layoutEditorAuxTools  = layoutEditor.getLEAuxTools();
        Assert.assertNotNull("layoutEditorAuxTools ", layoutEditorAuxTools );
    }

    @Test
    public void testCtorX() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LayoutBlock lb1 = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName("1");
        Assert.assertNotNull("lb1", lb1);
        LayoutBlock lb3 = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName("3");
        Assert.assertNotNull("lb3", lb3);

        //good parameters
        Assert.assertNotNull("layoutConnectivity(lb1, lb3)", new LayoutConnectivity(lb1, lb3));
    }

    //@Test TODO:FIX THIS - assertErrorMessage not catching log.error from constructor
    public void testBadParams() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutBlock lb1 = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName("1");
        Assert.assertNotNull("lb1", lb1);
        LayoutBlock lb3 = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName("3");
        Assert.assertNotNull("lb3", lb3);

        //bad parameters
        Assert.assertNotNull("layoutConnectivity(null, null)", new LayoutConnectivity(null, null));
        JUnitAppender.assertErrorMessage("null block1 when creating Layout Connectivity");
        JUnitAppender.assertErrorMessage("null block2 when creating Layout Connectivity");
        Assert.assertNotNull("layoutConnectivity(lb1, null)", new LayoutConnectivity(lb1, null));
        JUnitAppender.assertErrorMessage("null block2 when creating Layout Connectivity");
        Assert.assertNotNull("layoutConnectivity(null, lb3)", new LayoutConnectivity(null, lb3));
        JUnitAppender.assertErrorMessage("null block1 when creating Layout Connectivity");
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutBlock lb1 = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName("1");
        Assert.assertNotNull("lb1", lb1);
        LayoutBlock lb3 = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName("3");
        Assert.assertNotNull("lb3", lb3);

        //bad parameters
        LayoutConnectivity layoutConnectivity = new LayoutConnectivity(lb1, lb3);
        Assert.assertNotNull("layoutConnectivity(lb1, lb3)", layoutConnectivity);
        Assert.assertEquals("toString()", "between ILB1 and ILB3 in direction None", layoutConnectivity.toString());
    }

    @Test
    public void testSingleTurnout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutBlock lb61 = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName("61");
        Assert.assertNotNull("lb61", lb61);
        LayoutBlock lb63 = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName("63");
        Assert.assertNotNull("lb63", lb63);
        LayoutBlock lb64 = InstanceManager.getDefault(LayoutBlockManager.class).getByUserName("63");
        Assert.assertNotNull("lb64", lb64);

        LayoutConnectivity layoutConnectivity61_63 = new LayoutConnectivity(lb61, lb63);
        Assert.assertNotNull("layoutConnectivity61_63", layoutConnectivity61_63);
        Assert.assertEquals("layoutConnectivity61_63",
                "between ILB61 and ILB63 in direction None",
                layoutConnectivity61_63.toString());

        LayoutConnectivity layoutConnectivity61_64 = new LayoutConnectivity(lb61, lb64);
        Assert.assertNotNull("layoutConnectivity61_64", layoutConnectivity61_64);
        Assert.assertEquals("layoutConnectivity61_64",
                "between ILB61 and ILB63 in direction None",
                layoutConnectivity61_64.toString());

        LayoutConnectivity layoutConnectivity63_64 = new LayoutConnectivity(lb63, lb64);
        Assert.assertNotNull("layoutConnectivity63_64",
                layoutConnectivity63_64);
        Assert.assertEquals("layoutConnectivity63_64",
                "between ILB63 and ILB63 in direction None",
                layoutConnectivity63_64.toString());
    }

    //
    // from here down is testing infrastructure
    //
    @BeforeClass
    public static void setUpClass() throws Exception {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            JUnitUtil.initInternalTurnoutManager();
            JUnitUtil.initInternalSensorManager();

            // set default string matching comparator to one that exactly matches and is case sensitive
            Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));
            ThreadingUtil.runOnLayoutEventually(() -> {
                // load and display test panel file
                ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
                };
                File file = new File("java/test/jmri/jmrit/display/layoutEditor/valid/LEConnectTest.xml");
                try {
                    Assert.assertTrue("loaded successfully", cm.load(file));
                } catch (JmriConfigureXmlException ex) {
                    Assert.fail("JmriConfigureXmlException " + ex);
                }
            });

            // Find new window by name (should be more distinctive, comes from sample file)
            layoutEditorFrameOperator = new EditorFrameOperator("LayoutConnectivityTest");

            //wait for layout editor to finish setup and drawing
            new QueueTool().waitEmpty();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            new QueueTool().waitEmpty();
            // Ask to close window
            if (layoutEditorFrameOperator != null) {
                layoutEditorFrameOperator.closeFrameWithConfirmations();
                //jFrameOperator.waitClosed();    //make sure the dialog actually closed
            }
        }
        JUnitUtil.tearDown();
    }

    @Before
    public void setUp() {
        JUnitUtil.resetProfileManager();
//        if (!GraphicsEnvironment.isHeadless()) {
//            layoutConnectivity = new LayoutConnectivity(b, d);
//            Assert.assertNotNull("layoutConnectivity", layoutConnectivity);
//        }
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
