package apps.tests;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.Log4JUtil;
import org.apache.log4j.Level;
import org.junit.Assert;

public class Log4JFixture {

    private Log4JFixture() {
        // prevent instanciation
    }

    public static void setUp() {
        // always init logging if needed
        initLogging();
        //
        try {
            JUnitAppender.start();
        } catch (Throwable e) {
            System.err.println("Could not start JUnitAppender, but test continues:\n" + e);
        }
        // silence the Jemmy GUI unit testing framework
        JUnitUtil.silenceGUITestOutput();
    }

    public static void tearDown() {
        JUnitAppender.end();
        Level severity = Level.ERROR;
        boolean unexpectedMessageSeen = JUnitAppender.unexpectedMessageSeen(severity);
        JUnitAppender.verifyNoBacklog();
        JUnitAppender.resetUnexpectedMessageFlags(severity);
        Assert.assertFalse("Unexpected ERROR or FATAL messages emitted", unexpectedMessageSeen);
    }

    public static void initLogging() {
        String filename = System.getProperty("jmri.log4jconfigfilename", "tests.lcf");
        Log4JUtil.initLogging(filename);
    }
}
