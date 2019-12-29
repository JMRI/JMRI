package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.*;
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
 * @author George Warner Copyright (C) 2019
 */
public class LayoutEditorAuxToolsTest {

    @Rule   // 5 second timeout for methods in this test class.
    public Timeout globalTimeout = Timeout.seconds(3);

    @Rule   // allow 3 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(0);

    private static Operator.StringComparator stringComparator;

    private static EditorFrameOperator layoutEditorFrameOperator = null;
    private static LayoutEditor layoutEditor = null;
    private static LayoutEditorAuxTools layoutEditorAuxTools = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor ", layoutEditor);
        layoutEditorAuxTools = layoutEditor.getLEAuxTools();
        Assert.assertNotNull("layoutEditorAuxTools ", layoutEditorAuxTools);
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
    public void testGetConnectivityList() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SortedSet<LayoutBlock> layoutBlockBeans = InstanceManager.getDefault(LayoutBlockManager.class).getNamedBeanSet();
        Assert.assertEquals("layoutBlockBeans.size()", 48, layoutBlockBeans.size());
        LayoutBlock[] layoutBlocks = layoutBlockBeans.toArray(new LayoutBlock[0]);

        List<LayoutConnectivity> lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[0]);
        Assert.assertEquals("lcs0.size()", 1, lcs.size());
        Assert.assertEquals("lcs0[0]",
                "between ILB1 and ILB2 in direction East, track: T2, connect2: TO1, type2: 2",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[1]);
        Assert.assertEquals("lcs1.size()", 3, lcs.size());
        Assert.assertEquals("lcs1[0]",
                "between ILB2 and ILB3 in direction East, xover: TO1, xoverBoundaryType: 1",
                lcs.get(0).toString());
        Assert.assertEquals("lcs1[1]",
                "between ILB2 and ILB4 in direction East, xover: TO1, xoverBoundaryType: 3",
                lcs.get(1).toString());
        Assert.assertEquals("lcs1[2]",
                "between ILB1 and ILB2 in direction East, track: T2, connect2: TO1, type2: 2",
                lcs.get(2).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[2]);
        Assert.assertEquals("lcs2.size()", 1, lcs.size());
        Assert.assertEquals("lcs2[0]",
                "between ILB2 and ILB3 in direction East, xover: TO1, xoverBoundaryType: 1",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[3]);
        Assert.assertEquals("lcs3.size()", 3, lcs.size());
        Assert.assertEquals("lcs3[0]",
                "between ILB2 and ILB4 in direction East, xover: TO1, xoverBoundaryType: 3",
                lcs.get(0).toString());
        Assert.assertEquals("lcs3[1]",
                "between ILB4 and ILB6 in direction West, xover: TO1, xoverBoundaryType: 2",
                lcs.get(1).toString());
        Assert.assertEquals("lcs3[2]",
                "between ILB5 and ILB4 in direction West, track: T4, connect2: TO1, type2: 4",
                lcs.get(2).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[4]);
        Assert.assertEquals("lcs4.size()", 1, lcs.size());
        Assert.assertEquals("lcs4[0]",
                "between ILB5 and ILB4 in direction West, track: T4, connect2: TO1, type2: 4",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[5]);
        Assert.assertEquals("lcs5.size()", 1, lcs.size());
        Assert.assertEquals("lcs5[0]",
                "between ILB4 and ILB6 in direction West, xover: TO1, xoverBoundaryType: 2",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[6]);
        Assert.assertEquals("lcs6.size()", 1, lcs.size());
        Assert.assertEquals("lcs6[0]",
                "between ILB10 and ILB11 in direction East, track: T10, connect2: TO2, type2: 2",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[7]);
        Assert.assertEquals("lcs7.size()", 2, lcs.size());
        Assert.assertEquals("lcs7[0]",
                "between ILB11 and ILB12 in direction East, xover: TO2, xoverBoundaryType: 1",
                lcs.get(0).toString());
        Assert.assertEquals("lcs7[1]",
                "between ILB10 and ILB11 in direction East, track: T10, connect2: TO2, type2: 2",
                lcs.get(1).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[8]);
        Assert.assertEquals("lcs8.size()", 3, lcs.size());
        Assert.assertEquals("lcs8[0]",
                "between ILB11 and ILB12 in direction East, xover: TO2, xoverBoundaryType: 1",
                lcs.get(0).toString());
        Assert.assertEquals("lcs8[1]",
                "between ILB12 and ILB14 in direction West, xover: TO2, xoverBoundaryType: 4",
                lcs.get(1).toString());
        Assert.assertEquals("lcs8[2]",
                "between ILB15 and ILB12 in direction West, track: T11, connect2: TO2, type2: 3",
                lcs.get(2).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[9]);
        Assert.assertEquals("lcs9.size()", 2, lcs.size());
        Assert.assertEquals("lcs9[0]",
                "between ILB13 and ILB14 in direction West, xover: TO2, xoverBoundaryType: 2",
                lcs.get(0).toString());
        Assert.assertEquals("lcs9[1]",
                "between ILB13 and ILB16 in direction East, track: T13, connect2: T14, type2: 10",
                lcs.get(1).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[10]);
        Assert.assertEquals("lcs10.size()", 3, lcs.size());
        Assert.assertEquals("lcs10[0]",
                "between ILB13 and ILB14 in direction West, xover: TO2, xoverBoundaryType: 2",
                lcs.get(0).toString());
        Assert.assertEquals("lcs10[1]",
                "between ILB12 and ILB14 in direction West, xover: TO2, xoverBoundaryType: 4",
                lcs.get(1).toString());
        Assert.assertEquals("lcs10[2]",
                "between ILB17 and ILB14 in direction East, track: T15, connect2: TO2, type2: 5",
                lcs.get(2).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[11]);
        Assert.assertEquals("lcs11.size()", 1, lcs.size());
        Assert.assertEquals("lcs11[0]",
                "between ILB15 and ILB12 in direction West, track: T11, connect2: TO2, type2: 3",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[12]);
        Assert.assertEquals("lcs12.size()", 1, lcs.size());
        Assert.assertEquals("lcs12[0]",
                "between ILB13 and ILB16 in direction East, track: T13, connect2: T14, type2: 10",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[13]);
        Assert.assertEquals("lcs13.size()", 1, lcs.size());
        Assert.assertEquals("lcs13[0]",
                "between ILB17 and ILB14 in direction East, track: T15, connect2: TO2, type2: 5",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[14]);
        Assert.assertEquals("lcs14.size()", 1, lcs.size());
        Assert.assertEquals("lcs14[0]",
                "between ILB20 and ILB21 in direction East, track: T19, connect2: TO3, type2: 2",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[15]);
        Assert.assertEquals("lcs15.size()", 3, lcs.size());
        Assert.assertEquals("lcs15[0]",
                "between ILB21 and ILB22 in direction East, xover: TO3, xoverBoundaryType: 1",
                lcs.get(0).toString());
        Assert.assertEquals("lcs15[1]",
                "between ILB21 and ILB26 in direction East, xover: TO3, xoverBoundaryType: 3",
                lcs.get(1).toString());
        Assert.assertEquals("lcs15[2]",
                "between ILB20 and ILB21 in direction East, track: T19, connect2: TO3, type2: 2",
                lcs.get(2).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[16]);
        Assert.assertEquals("lcs16.size()", 3, lcs.size());
        Assert.assertEquals("lcs16[0]",
                "between ILB21 and ILB22 in direction East, xover: TO3, xoverBoundaryType: 1",
                lcs.get(0).toString());
        Assert.assertEquals("lcs16[1]",
                "between ILB22 and ILB25 in direction West, xover: TO3, xoverBoundaryType: 4",
                lcs.get(1).toString());
        Assert.assertEquals("lcs16[2]",
                "between ILB23 and ILB22 in direction West, track: T17, connect2: TO3, type2: 3",
                lcs.get(2).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[17]);
        Assert.assertEquals("lcs17.size()", 1, lcs.size());
        Assert.assertEquals("lcs17[0]",
                "between ILB23 and ILB22 in direction West, track: T17, connect2: TO3, type2: 3",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[18]);
        Assert.assertEquals("lcs18.size()", 1, lcs.size());
        Assert.assertEquals("lcs18[0]",
                "between ILB24 and ILB25 in direction East, track: T20, connect2: TO3, type2: 5",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[19]);
        Assert.assertEquals("lcs19.size()", 3, lcs.size());
        Assert.assertEquals("lcs19[0]",
                "between ILB26 and ILB25 in direction West, xover: TO3, xoverBoundaryType: 2",
                lcs.get(0).toString());
        Assert.assertEquals("lcs19[1]",
                "between ILB22 and ILB25 in direction West, xover: TO3, xoverBoundaryType: 4",
                lcs.get(1).toString());
        Assert.assertEquals("lcs19[2]",
                "between ILB24 and ILB25 in direction East, track: T20, connect2: TO3, type2: 5",
                lcs.get(2).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[20]);
        Assert.assertEquals("lcs20.size()", 3, lcs.size());
        Assert.assertEquals("lcs20[0]",
                "between ILB21 and ILB26 in direction East, xover: TO3, xoverBoundaryType: 3",
                lcs.get(0).toString());
        Assert.assertEquals("lcs20[1]",
                "between ILB26 and ILB25 in direction West, xover: TO3, xoverBoundaryType: 2",
                lcs.get(1).toString());
        Assert.assertEquals("lcs20[2]",
                "between ILB27 and ILB26 in direction West, track: T18, connect2: TO3, type2: 4",
                lcs.get(2).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[21]);
        Assert.assertEquals("lcs21.size()", 1, lcs.size());
        Assert.assertEquals("lcs21[0]",
                "between ILB27 and ILB26 in direction West, track: T18, connect2: TO3, type2: 4",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[22]);
        Assert.assertEquals("lcs22.size()", 1, lcs.size());
        Assert.assertEquals("lcs22[0]",
                "between ILB31 and ILB32 in direction East, track: T27, connect2: T28, type2: 10",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[23]);
        Assert.assertEquals("lcs23.size()", 5, lcs.size());
        Assert.assertEquals("lcs23[0]",
                "between ILB31 and ILB32 in direction East, track: T27, connect2: T28, type2: 10",
                lcs.get(0).toString());
        Assert.assertEquals("lcs23[1]",
                "between ILB32 and ILB33 in direction East, track: T33, connect2: T37, type2: 10",
                lcs.get(1).toString());
        Assert.assertEquals("lcs23[2]",
                "between ILB32 and ILB34 in direction East, track: T34, connect2: T38, type2: 10",
                lcs.get(2).toString());
        Assert.assertEquals("lcs23[3]",
                "between ILB32 and ILB35 in direction East, track: T35, connect2: T39, type2: 10",
                lcs.get(3).toString());
        Assert.assertEquals("lcs23[4]",
                "between ILB32 and ILB36 in direction East, track: T36, connect2: T40, type2: 10",
                lcs.get(4).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[24]);
        Assert.assertEquals("lcs24.size()", 1, lcs.size());
        Assert.assertEquals("lcs24[0]",
                "between ILB32 and ILB33 in direction East, track: T33, connect2: T37, type2: 10",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[25]);
        Assert.assertEquals("lcs25.size()", 1, lcs.size());
        Assert.assertEquals("lcs25[0]",
                "between ILB32 and ILB34 in direction East, track: T34, connect2: T38, type2: 10",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[26]);
        Assert.assertEquals("lcs26.size()", 1, lcs.size());
        Assert.assertEquals("lcs26[0]",
                "between ILB32 and ILB35 in direction East, track: T35, connect2: T39, type2: 10",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[27]);
        Assert.assertEquals("lcs27.size()", 1, lcs.size());
        Assert.assertEquals("lcs27[0]",
                "between ILB32 and ILB36 in direction East, track: T36, connect2: T40, type2: 10",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[28]);
        Assert.assertEquals("lcs28.size()", 2, lcs.size());
        Assert.assertEquals("lcs28[0]",
                "between ILB42 and ILB40 in direction Southeast, track: T45, connect2: X1, type2: 6",
                lcs.get(0).toString());
        Assert.assertEquals("lcs28[1]",
                "between ILB44 and ILB40 in direction Northwest, track: T47, connect2: X1, type2: 8",
                lcs.get(1).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[29]);
        Assert.assertEquals("lcs29.size()", 2, lcs.size());
        Assert.assertEquals("lcs29[0]",
                "between ILB43 and ILB41 in direction Northeast, track: T46, connect2: X1, type2: 7",
                lcs.get(0).toString());
        Assert.assertEquals("lcs29[1]",
                "between ILB45 and ILB41 in direction Southwest, track: T48, connect2: X1, type2: 9",
                lcs.get(1).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[30]);
        Assert.assertEquals("lcs30.size()", 1, lcs.size());
        Assert.assertEquals("lcs30[0]",
                "between ILB42 and ILB40 in direction Southeast, track: T45, connect2: X1, type2: 6",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[31]);
        Assert.assertEquals("lcs31.size()", 1, lcs.size());
        Assert.assertEquals("lcs31[0]",
                "between ILB43 and ILB41 in direction Northeast, track: T46, connect2: X1, type2: 7",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[32]);
        Assert.assertEquals("lcs32.size()", 1, lcs.size());
        Assert.assertEquals("lcs32[0]",
                "between ILB44 and ILB40 in direction Northwest, track: T47, connect2: X1, type2: 8",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[33]);
        Assert.assertEquals("lcs33.size()", 1, lcs.size());
        Assert.assertEquals("lcs33[0]",
                "between ILB45 and ILB41 in direction Southwest, track: T48, connect2: X1, type2: 9",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[34]);
        Assert.assertEquals("lcs34.size()", 1, lcs.size());
        Assert.assertEquals("lcs34[0]",
                "between ILB62 and ILB61 in direction West, track: T21, connect2: T24, type2: 10",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[35]);
        Assert.assertEquals("lcs35.size()", 3, lcs.size());
        Assert.assertEquals("lcs35[0]",
                "between ILB62 and ILB61 in direction West, track: T21, connect2: T24, type2: 10",
                lcs.get(0).toString());
        Assert.assertEquals("lcs35[1]",
                "between ILB62 and ILB63 in direction East, track: T22, connect2: T25, type2: 10",
                lcs.get(1).toString());
        Assert.assertEquals("lcs35[2]",
                "between ILB62 and ILB64 in direction East, track: T23, connect2: T26, type2: 10",
                lcs.get(2).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[36]);
        Assert.assertEquals("lcs36.size()", 1, lcs.size());
        Assert.assertEquals("lcs36[0]",
                "between ILB62 and ILB63 in direction East, track: T22, connect2: T25, type2: 10",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[37]);
        Assert.assertEquals("lcs37.size()", 1, lcs.size());
        Assert.assertEquals("lcs37[0]",
                "between ILB62 and ILB64 in direction East, track: T23, connect2: T26, type2: 10",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[38]);
        Assert.assertEquals("lcs38.size()", 4, lcs.size());
        Assert.assertEquals("lcs38[0]",
                "between ILB71 and ILB70 in direction Southeast, track: T41, connect2: SL1, type2: 21",
                lcs.get(0).toString());
        Assert.assertEquals("lcs38[1]",
                "between ILB74 and ILB70 in direction Southwest, track: T42, connect2: SL1, type2: 24",
                lcs.get(1).toString());
        Assert.assertEquals("lcs38[2]",
                "between ILB72 and ILB70 in direction Northeast, track: T43, connect2: SL1, type2: 22",
                lcs.get(2).toString());
        Assert.assertEquals("lcs38[3]",
                "between ILB73 and ILB70 in direction Northwest, track: T44, connect2: SL1, type2: 23",
                lcs.get(3).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[39]);
        Assert.assertEquals("lcs39.size()", 1, lcs.size());
        Assert.assertEquals("lcs39[0]",
                "between ILB71 and ILB70 in direction Southeast, track: T41, connect2: SL1, type2: 21",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[40]);
        Assert.assertEquals("lcs40.size()", 1, lcs.size());
        Assert.assertEquals("lcs40[0]",
                "between ILB72 and ILB70 in direction Northeast, track: T43, connect2: SL1, type2: 22",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[41]);
        Assert.assertEquals("lcs41.size()", 1, lcs.size());
        Assert.assertEquals("lcs41[0]",
                "between ILB73 and ILB70 in direction Northwest, track: T44, connect2: SL1, type2: 23",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[42]);
        Assert.assertEquals("lcs42.size()", 1, lcs.size());
        Assert.assertEquals("lcs42[0]",
                "between ILB74 and ILB70 in direction Southwest, track: T42, connect2: SL1, type2: 24",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[43]);
        Assert.assertEquals("lcs43.size()", 4, lcs.size());
        Assert.assertEquals("lcs43[0]",
                "between ILB76 and ILB75 in direction Southeast, track: T49, connect2: SL2, type2: 21",
                lcs.get(0).toString());
        Assert.assertEquals("lcs43[1]",
                "between ILB77 and ILB75 in direction Northeast, track: T50, connect2: SL2, type2: 22",
                lcs.get(1).toString());
        Assert.assertEquals("lcs43[2]",
                "between ILB78 and ILB75 in direction Northwest, track: T51, connect2: SL2, type2: 23",
                lcs.get(2).toString());
        Assert.assertEquals("lcs43[3]",
                "between ILB79 and ILB75 in direction Southwest, track: T52, connect2: SL2, type2: 24",
                lcs.get(3).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[44]);
        Assert.assertEquals("lcs44.size()", 1, lcs.size());
        Assert.assertEquals("lcs44[0]",
                "between ILB76 and ILB75 in direction Southeast, track: T49, connect2: SL2, type2: 21",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[45]);
        Assert.assertEquals("lcs45.size()", 1, lcs.size());
        Assert.assertEquals("lcs45[0]",
                "between ILB77 and ILB75 in direction Northeast, track: T50, connect2: SL2, type2: 22",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[46]);
        Assert.assertEquals("lcs46.size()", 1, lcs.size());
        Assert.assertEquals("lcs46[0]",
                "between ILB78 and ILB75 in direction Northwest, track: T51, connect2: SL2, type2: 23",
                lcs.get(0).toString());

        lcs = layoutEditorAuxTools.getConnectivityList(layoutBlocks[47]);
        Assert.assertEquals("lcs47.size()", 1, lcs.size());
        Assert.assertEquals("lcs47[0]",
                "between ILB79 and ILB75 in direction Southwest, track: T52, connect2: SL2, type2: 24",
                lcs.get(0).toString());
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

            //save the old string comparator
            stringComparator = Operator.getDefaultStringComparator();
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
            layoutEditor = (LayoutEditor) JmriJFrame.getFrame("LayoutConnectivityTest");
            Assert.assertNotNull("layoutEditor ", layoutEditor);

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
            //restore the default string matching comparator
            Operator.setDefaultStringComparator(stringComparator);
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
    //private final static Logger log = LoggerFactory.getLogger(LayoutEditorAuxToolsTest.class.getName());
}
