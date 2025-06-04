package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.io.File;
import java.io.IOException;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.jmrix.can.cbus.CbusConfigurationManager;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import org.netbeans.jemmy.operators.*;

/**
 * Test simple functioning of CbusNodeRestoreFcuFrame
 *
 * @author Paul Bender Copyright (C) 2016, 2019
 * @author Steve Young Copyright (C) 2019
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusNodeRestoreFcuFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testInit() {
        Assertions.assertNotNull(memo);
        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        CbusNodeTableDataModel nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        NodeConfigToolPane mainpane = new NodeConfigToolPane();
        mainpane.initComponents(memo);
        Assert.assertNotNull("menu items initialised so restore frame can disable",mainpane.getMenus());

        CbusNodeRestoreFcuFrame t = new CbusNodeRestoreFcuFrame(mainpane);
        Assert.assertNotNull("exists",t);
        t.initComponents(memo);

        Assert.assertEquals("title",Bundle.getMessage("FcuImportTitle"),t.getTitle());

        JFrameOperator jfo = new JFrameOperator( t.getTitle() );
        Assert.assertFalse("can find button so frame has opened ok",getTeachNodeButtonEnabled(jfo));

        // Ask to close window
        jfo.requestClose();

        nodeModel.dispose();
        mainpane.dispose();
    }

    @Test
    public void testImportData(){
        Assertions.assertNotNull(memo);
        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        CbusNodeTableDataModel nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        NodeConfigToolPane mainpane = new NodeConfigToolPane();
        mainpane.initComponents(memo);
        Assert.assertNotNull("menu items initialised so restore frame can disable",mainpane.getMenus());

        CbusNodeRestoreFcuFrame t = new CbusNodeRestoreFcuFrame(mainpane);
        Assert.assertNotNull("exists",t);
        t.initComponents(memo);
        JFrameOperator jfo = new JFrameOperator( t.getTitle() );

        JListOperator toListOperator = new JListOperator(jfo);
        Assertions.assertEquals(null, toListOperator.getSelectedValue());
        Assertions.assertEquals(1, toListOperator.getModel().getSize());
        Assertions.assertTrue(((String)toListOperator.getModel().getElementAt(0)).contains("Node Table Empty"));

        jmri.jmrix.can.cbus.simulator.moduletypes.MergCanpan mcp = new jmri.jmrix.can.cbus.simulator.moduletypes.MergCanpan();
        CbusNode css = mcp.getNewDummyNode(memo, 256);
        nodeModel.addNode(css);
        JUnitUtil.waitFor(() -> { return ((String)toListOperator.getModel().getElementAt(0)).equals("256 CANPAN"); }, "not not added to list");

        File systemFile = new File("java/test/jmri/jmrix/can/cbus/swing/nodeconfig/", "fcu-n256-canpan.xml");
        t.addFile(systemFile);

        JTableOperator jto = new JTableOperator(jfo);
        JUnitUtil.waitFor(() -> { return (jto.getRowCount()==1); }, "row added to fcu import table");
        jto.selectCell(0, 0);

        JTabbedPaneOperator to = new JTabbedPaneOperator(jfo);
        to.selectPage(0);

        JTextAreaOperator ip = new JTextAreaOperator(jfo);
        String txt = ip.getText();
        Assertions.assertTrue(txt.contains("CANPAN"), "info pane loaded");

        to.selectPage(1);
        JTableOperator nvtable = new JTableOperator(jfo, 1);
        Assertions.assertEquals(1, nvtable.getRowCount(),"1 NV");
        Assertions.assertEquals(1, (int)nvtable.getValueAt(0, 2),"NV1 value 1");

        to.selectPage(2);
        JTableOperator evTable = new JTableOperator(jfo, 1);
        Assertions.assertEquals(2, evTable.getRowCount(),"2 events imported");
        Assertions.assertEquals(127, (int)evTable.getValueAt(0, 0),"node 127");
        Assertions.assertEquals(100, (int)evTable.getValueAt(0, 1),"event 100");
        Assertions.assertEquals("SOD Request DC CANPAN from JMRI", evTable.getValueAt(0, 4),"event name imported");
        Assertions.assertEquals("2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 255", evTable.getValueAt(0, 5), "ev variables imported");

        Assertions.assertEquals(127, (int)evTable.getValueAt(1, 0));
        Assertions.assertEquals(205, (int)evTable.getValueAt(1, 1));
        Assertions.assertEquals("LED DC On", evTable.getValueAt(1, 4));
        Assertions.assertEquals("1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13", evTable.getValueAt(1, 5));

        JListOperator jlo = new JListOperator(jfo);
        jlo.setSelectedIndex(0);

        Thread thr = new Thread(() -> {
            JDialogOperator d = new JDialogOperator("Please Confirm Write to Node");
            JButtonOperator bo = new JButtonOperator(d, "Cancel");
            bo.push(); // Click button to close
        });
        thr.setName("Confirm write cancel Thread");
        thr.start();

        JButtonOperator upb = new JButtonOperator(jfo,"Update Node");
        upb.doClick();
        JUnitUtil.waitFor(() -> { return !thr.isAlive(); }, "cancel write button not clicked");

        Assertions.assertEquals(0,tcis.outbound.size());
        Assertions.assertEquals(0,tcis.inbound.size());

        // JButtonOperator jbo = new JButtonOperator(jfo,"Pause Test");

        jfo.requestClose();
        nodeModel.dispose();
        mainpane.dispose();
    }

    @Test
    public void testFileChooser(){
        Assertions.assertNotNull(memo);

        Assertions.assertEquals(0, memo.getPropertyChangeListeners().length);

        memo.get(CbusPreferences.class).setNodeBackgroundFetchDelay(0);
        CbusNodeTableDataModel nodeModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusNodeTableDataModel.class);

        NodeConfigToolPane mainpane = new NodeConfigToolPane();
        mainpane.initComponents(memo);
        Assert.assertNotNull("menu items initialised so restore frame can disable",mainpane.getMenus());

        CbusNodeRestoreFcuFrame t = new CbusNodeRestoreFcuFrame(mainpane);
        Assert.assertNotNull("exists",t);
        t.initComponents(memo);
        JFrameOperator jfo = new JFrameOperator( t.getTitle() );

        CbusEventTableDataModel evModel = memo.get(CbusConfigurationManager.class)
            .provide(CbusEventTableDataModel.class);
        evModel.skipSaveOnDispose();

        File fromFile = new File("java/test/jmri/jmrix/can/cbus/swing/nodeconfig/", "fcu-n256-canpan.xml");
        File toFile = new File(tempDir, "fcu-n256-canpan.xml");

        try {
            java.nio.file.Files.copy(
                    fromFile.toPath(),
                    toFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
                    java.nio.file.LinkOption.NOFOLLOW_LINKS);
        } catch (IOException ex) {
            Assertions.fail("could not copy fcu file to temp directory", ex);
        }

        Thread thr = new Thread(() -> {
            JFileChooserOperator jj = new JFileChooserOperator(jfo);
            jj.cancel();
        });
        thr.setName("cancel file choose Thread");
        thr.start();

        JButtonOperator jbo = new JButtonOperator(jfo,Bundle.getMessage("SelectFcuFile"));
        jbo.doClick();
        JUnitUtil.waitFor(() -> { return !thr.isAlive(); }, "cancel file choose not clicked");

        Thread thrch = new Thread(() -> {
            JFileChooserOperator jj = new JFileChooserOperator(jfo);
            jj.chooseFile("fcu-n256-canpan.xml");
        });
        thrch.setName("choose file Thread");
        thrch.start();

        jbo.doClick();
        JUnitUtil.waitFor(() -> { return !thrch.isAlive(); }, "file not chosen");

        Assertions.assertEquals(2, evModel.getRowCount());
        Assertions.assertEquals("SOD Request DC CANPAN from JMRI", evModel.getEventName(127, 100));
        Assertions.assertEquals("LED DC On", evModel.getEventName(127, 205));

        jfo.requestClose();
        jfo.waitClosed();
        evModel.dispose();
        nodeModel.dispose();
        mainpane.dispose();
    }
    
    private boolean getTeachNodeButtonEnabled( JFrameOperator jfo ){
        return ( new JButtonOperator(jfo,Bundle.getMessage("UpdateNodeButton")).isEnabled() );
    }

    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;
    public @TempDir File tempDir;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        try {
            JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        } catch (IOException ex) {
            Assertions.fail("could not set temp directory");
        }

        memo = new CanSystemConnectionMemo();
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        frame = new CbusNodeRestoreFcuFrame(null);

    }

    @AfterEach
    @Override
    public void tearDown() {
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        Assertions.assertNotNull(memo);
        memo.dispose();
        tcis = null;
        memo = null;
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusNodeRestoreFcuFrameTest.class);

}
