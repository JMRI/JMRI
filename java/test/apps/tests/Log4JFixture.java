package apps.tests;

public class Log4JFixture extends java.lang.Object {

    public Log4JFixture() {
        initLogging();
    }

    static public void setUp() {
        // always init logging if needed
        initLogging();
        //
        try {
            jmri.util.JUnitAppender.start();
        } catch (Throwable e) {
            System.err.println("Could not start JUnitAppender, but test continues:\n" + e);
        }
    }

    static public void tearDown() {
        jmri.util.JUnitAppender.end();
    }

    public static void initLogging() {
        jmri.util.Log4JUtil.initLogging("tests.lcf");
    }
}
