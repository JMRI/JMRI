package jmri.jmrix.nce.simulator;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SimulatorAdapterTest {
        
    private SimulatorAdapter adapter;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",adapter);
    }

    @Test
    public void testOpenPort() {
        Assert.assertFalse("status before open",adapter.status());
        Assert.assertNull("InputStream Before Open",adapter.getInputStream());
        jmri.util.JUnitAppender.assertErrorMessage("getInputStream called before load(), stream not available");
        Assert.assertNull("OutputStream Before Open",adapter.getOutputStream());
        jmri.util.JUnitAppender.assertErrorMessage("getOutputStream called before load(), stream not available");
        Assert.assertNull("port opens",adapter.openPort("test","test"));
        Assert.assertTrue("status after open",adapter.status());
        Assert.assertNotNull("InputStream After Open",adapter.getInputStream());
        Assert.assertNotNull("OutputStream After Open",adapter.getOutputStream());
    }

    @Test
    public void testBaudRates(){
       Assert.assertArrayEquals("empty baud rates array for simulator", new String[]{}, adapter.validBaudRates());
       Assert.assertEquals("no currentbaudrate set for simulator","", adapter.getCurrentBaudRate());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        adapter = new SimulatorAdapter();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
        adapter.dispose();
        adapter = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(SimulatorAdapterTest.class);

}
