package jmri.jmrix.easydcc.easydccmon;

import jmri.jmrix.easydcc.EasyDccMessage;
import jmri.jmrix.easydcc.EasyDccReply;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * JUnit tests for the EasyDccMonFrame class
 *
 * @author Bob Jacobsen
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class EasyDccMonFrameTest {

    @Test
    public void testCreate() {
        EasyDccMonFrame f = new EasyDccMonFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));
        Assert.assertNotNull("exists", f);
    }

    @Test
    @Disabled("Test fails to get text from frame")
    public void testMsg() {
        EasyDccMessage m = new EasyDccMessage(3);
        m.setOpCode('L');
        m.setElement(1, '0');
        m.setElement(2, 'A');

        EasyDccMonFrame f = new EasyDccMonFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));
        f.setVisible(true);
        f.message(m);

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        JUnitUtil.waitFor(()-> { return f.getFrameText().length()>0; },"No Text in getFrameText");
        Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), f.getFrameText().length());
        Assert.assertEquals("display", "cmd: \"L0A\"\n", f.getFrameText());
    }

    @Test
    @Disabled("Test fails to get text from frame")
    public void testReply() {
        EasyDccReply m = new EasyDccReply();
        m.setOpCode('C');
        m.setElement(1, 'o');
        m.setElement(2, ':');

        EasyDccMonFrame f = new EasyDccMonFrame(new EasyDccSystemConnectionMemo("E", "EasyDCC via Serial"));
        f.setVisible(true);
        f.reply(m);

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        JUnitUtil.waitFor(()-> { return f.getFrameText().length()>0; },"No Text in getFrameText");
        Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), f.getFrameText().length());
        Assert.assertEquals("display", "rep: \"Co:\"\n", f.getFrameText());
    }

    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown(){
       JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EasyDccMonFrameTest.class);

}
