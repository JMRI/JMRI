package jmri.jmrix.easydcc.easydccmon;

import jmri.jmrix.easydcc.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.*;

/**
 * JUnit tests for the EasyDccMonFrame class
 *
 * @author Bob Jacobsen
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class EasyDccMonFrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testCreate() {
        Assert.assertNotNull("exists", frame);
    }

    @Test
    public void testMsg() {
        EasyDccMessage m = new EasyDccMessage(3);
        m.setOpCode('L');
        m.setElement(1, '0');
        m.setElement(2, 'A');

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            frame.setVisible(true);
        });

        EasyDccMonFrame f = (EasyDccMonFrame)frame;

        f.message(m);

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        JUnitUtil.waitFor(()-> { return f.getTextArea().getText().length()>0; },"No Text in getFrameText");
        Assert.assertEquals("length ", "cmd: \"L0A\"\n".length(), f.getTextArea().getText().length());
        Assert.assertEquals("display", "cmd: \"L0A\"\n", f.getTextArea().getText());

        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        jfo.requestClose();
        jfo.waitClosed();

    }

    @Test
    public void testReply() {
        EasyDccReply m = new EasyDccReply();
        m.setOpCode('C');
        m.setElement(1, 'o');
        m.setElement(2, ':');

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            frame.setVisible(true);
        });

        EasyDccMonFrame f = (EasyDccMonFrame)frame;
        f.reply(m);

        new org.netbeans.jemmy.QueueTool().waitEmpty(100);
        JUnitUtil.waitFor(()-> { return f.getTextArea().getText().length()>0; },"No Text in getFrameText");
        Assert.assertEquals("length ", "rep: \"Co:\"\n".length(), f.getTextArea().getText().length());
        Assert.assertEquals("display", "rep: \"Co:\"\n", f.getTextArea().getText());

        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        jfo.requestClose();
        jfo.waitClosed();

    }

    private EasyDccSystemConnectionMemo memo = null;
    private EasyDccTrafficControlScaffold tc = null;

    @Override
    @BeforeEach
    public void setUp(){
        JUnitUtil.setUp();

        memo = new EasyDccSystemConnectionMemo("E", "EasyDCC Test");
        tc = new EasyDccTrafficControlScaffold(memo);
        memo.setEasyDccTrafficController(tc);

        frame = new EasyDccMonFrame(memo);
    }

    @Override
    @AfterEach
    public void tearDown(){

        Assertions.assertNotNull(frame);
        frame.dispose();
        frame = null;
        Assertions.assertNotNull(tc);
        Assertions.assertNotNull(memo);
        tc.terminateThreads();
        memo.dispose();
        tc = null;
        memo = null;

        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EasyDccMonFrameTest.class);

}
