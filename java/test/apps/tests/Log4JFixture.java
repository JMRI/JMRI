package apps.tests;

import java.util.*;
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
        // do not set the UncaughtExceptionHandler while unit testing
        // individual tests can explicitely set it after calling this method
        Thread.setDefaultUncaughtExceptionHandler(null);
        try {
            JUnitAppender.start();
        } catch (Throwable e) {
            System.err.println("Could not start JUnitAppender, but test continues:\n" + e);
        }
        // silence the Jemmy GUI unit testing framework
        JUnitUtil.silenceGUITestOutput();
    }

    static int count = 0;
    public static void tearDown() {
        JUnitAppender.end();
        Level severity = Level.ERROR; // level at or above which we'll complain
        boolean unexpectedMessageSeen = JUnitAppender.unexpectedMessageSeen(severity);
        JUnitAppender.verifyNoBacklog();
        JUnitAppender.resetUnexpectedMessageFlags(severity);
        Assert.assertFalse("Unexpected ERROR or FATAL messages emitted", unexpectedMessageSeen);
        
        // checkThreads(false);  // true means stop on 1st extra thread
    }

    static List<String> threadNames = new ArrayList<String>(Arrays.asList(new String[]{
        // names we know about from normal running
        "main",
        "Java2D Disposer",
        "AWT-Shutdown",
        "AWT-EventQueue",
        "GC Daemon",
        "Finalizer",
        "Reference Handler",
        "Signal Dispatcher",
        "Java2D Queue Flusher",
        "Time-limited test",
        "WindowMonitor-DispatchThread",
        "RMI Reaper",
        "RMI TCP Accept",
        "TimerQueue",
        "Java Sound Event Dispatcher",
        "Aqua L&F",
        "AppKit Thread"
    }));
    static List<Thread> threadsSeen = new ArrayList<Thread>();

    /**
     * Do a diagnostic check of threads, 
     * providing a traceback if any new ones are still around.
     * <p>
     * First implementation is rather simplistic.
     * @param stop If true, this stop execution after the 1st new thread is found
     */
    static void checkThreads(boolean stop) {
        // now check for extra threads
        count = 0;
        Thread.getAllStackTraces().keySet().forEach((t) -> 
            {
                if (threadsSeen.contains(t)) return;
                String name = t.getName();
                if (! (threadNames.contains(name)
                     || name.startsWith("RMI TCP Accept")
                     || name.startsWith("AWT-EventQueue")
                     || name.startsWith("Aqua L&F")
                     || name.startsWith("Image Fetcher ")
                     || name.startsWith("JmDNS(")
                     || name.startsWith("SocketListener(")
                     || (name.startsWith("Timer-") && 
                            ( t.getThreadGroup() != null && 
                                (t.getThreadGroup().getName().contains("FailOnTimeoutGroup") || t.getThreadGroup().getName().contains("main") )
                            ) 
                        )
                    )) {  
                    
                        count++;
                        threadsSeen.add(t);
                        System.out.println("New thread \""+t.getName()+"\" group \""+ (t.getThreadGroup()!=null ? t.getThreadGroup().getName() : "(null)")+"\"");
                    
                        // for anonymous threads, show the traceback in hopes of finding what it is
                        if (name.startsWith("Thread-")) {
                            for (StackTraceElement e : Thread.getAllStackTraces().get(t)) {
                                if (! e.toString().startsWith("java")) System.out.println("    "+e);
                            }
                        }
                }
            });
        if (count > 0) {
            //Thread.getAllStackTraces().keySet().forEach((t) -> System.err.println("  thread "+t+" "+t.getName()));
            if (stop) {
                new Exception("Stopping by request on 1st extra thread").printStackTrace();
                System.exit(0);
            }
        }
    }
    
    public static void initLogging() {
        String filename = System.getProperty("jmri.log4jconfigfilename", "tests.lcf");
        Log4JUtil.initLogging(filename);
    }
}
