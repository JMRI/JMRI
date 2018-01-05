package jmri.jmrix.easydcc.easydccmon;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import jmri.jmrix.easydcc.EasyDccListener;
import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccReply;
import jmri.jmrix.easydcc.EasyDccTrafficController;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the EasyDccMonFrame class
 *
 * @author	Bob Jacobsen
 */
public class EasyDccMonFrameTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EasyDccMonFrame f = new EasyDccMonFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));
        Assert.assertNotNull("exists", f);
    }

    @Test
    @Ignore("Test fails to get text from frame")
    public void testMsg() {
        EasyDccMessage m = new EasyDccMessage(3);
        m.setOpCode('L');
        m.setElement(1, '0');
        m.setElement(2, 'A');

        EasyDccMonFrame f = new EasyDccMonFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));

        f.message(m);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), f.getFrameText().length());
        Assert.assertEquals("display", "cmd: \"L0A\"\n", f.getFrameText());
    }

    @Test
    @Ignore("Test fails to get text from frame")
    public void testReply() {
        EasyDccReply m = new EasyDccReply();
        m.setOpCode('C');
        m.setElement(1, 'o');
        m.setElement(2, ':');

        EasyDccMonFrame f = new EasyDccMonFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));

        f.reply(m);
        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), f.getFrameText().length());
        Assert.assertEquals("display", "rep: \"Co:\"\n", f.getFrameText());
    }

    @Before
    public void setUp(){
       jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown(){
       jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccMonFrameTest.class);

}
