package jmri.jmrix.loconet.downloader;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LoaderPaneTest {

   private LnTrafficController lnis = null;
   private LocoNetSystemConnectionMemo memo = null;
   private SlotManager slotmanager = null;

    @Test
    public void testCTor() {
        LoaderPane t = new LoaderPane();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testInitComponents() throws Exception{
        LoaderPane t = new LoaderPane();
        // for now, just makes ure there isn't an exception.
        t.initComponents(memo);
    }

    @Test
    public void testInitContext() throws Exception {
        LoaderPane t = new LoaderPane();
        // for now, just makes ure there isn't an exception.
        t.initContext(memo);
    }

    @Test
    public void testGetHelpTarget(){
        LoaderPane t = new LoaderPane();
        Assert.assertEquals("help target","package.jmri.jmrix.loconet.downloader.LoaderFrame",t.getHelpTarget());
    }

    @Test
    public void testGetTitle(){
        LoaderPane t = new LoaderPane();
        Assert.assertEquals("title","Firmware Downloader",t.getTitle());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis,slotmanager);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LoaderPaneTest.class);

}
