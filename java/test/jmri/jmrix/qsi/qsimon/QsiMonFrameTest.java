package jmri.jmrix.qsi.qsimon;

import jmri.ProgrammingMode;
import jmri.jmrix.qsi.QsiMessage;
import jmri.jmrix.qsi.QsiReply;
import jmri.jmrix.qsi.QsiTrafficControlScaffold;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrameTestBase;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * JUnit tests for the QsiProgrammer class
 *
 * @author Bob Jacobsen
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class QsiMonFrameTest extends JmriJFrameTestBase {

    @Test
    public void testCreate() {
        Assertions.assertNotNull(frame, "exists");
    }

    @Test
    public void testMsg() {
        QsiMessage m = QsiMessage.getReadCV(25, ProgrammingMode.PAGEMODE);
        ((QsiMonFrame)frame).message(m);

        JUnitUtil.waitFor(() -> (!((QsiMonFrame)frame).getTextArea().getText().isEmpty()), "Text Area populated");
        Assertions.assertTrue( ((QsiMonFrame)frame).getTextArea().getText().contains("M: OP_REQ_READ_CV with CV=25"));
    }

    @Test
    public void testReply() {
        QsiReply m = new QsiReply();
        m.setOpCode('C');
        m.setElement(1, 'o');
        m.setElement(2, ':');
        ((QsiMonFrame)frame).reply(m);

        JUnitUtil.waitFor(() -> (!((QsiMonFrame)frame).getTextArea().getText().isEmpty()), "Text Area populated");
        Assertions.assertTrue( ((QsiMonFrame)frame).getTextArea().getText().contains("U: Untranslated reply: <67><111><58>"));
    }

    private jmri.jmrix.qsi.QsiSystemConnectionMemo memo = null;
    private QsiTrafficControlScaffold tc = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new QsiTrafficControlScaffold();
        memo = new jmri.jmrix.qsi.QsiSystemConnectionMemo(tc);
        frame = new QsiMonFrame(memo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        if(frame!=null) {
           JUnitUtil.dispose(frame); // close frame before memo
        }
        frame = null;
        Assertions.assertNotNull(memo);
        Assertions.assertNotNull(tc);
        memo.dispose();
        memo = null;
        tc = null;
        super.tearDown();

    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QsiMonFrameTest.class);

}
