package jmri.util.junit;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import org.junit.internal.TextListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

public class PrintingTestListener extends SummaryGeneratingListener {


    /**
     * A {@link TextListener} implementation that can display the name
     * of run, failed and ignored tests run by JUnit
     * <p>
     * When the <code>jmri.util.junit.PrintingTestListener.verbose</code>
     * property is empty or false, this acts like it's super-class
     * to provide default behavior. When true, the names of 
     * classes and tests are displayed as they run.
     * <p>
     * When the <code>jmri.util.junit.PrintingTestListener.quiet</code>
     * property is non-empty and non-false, the output is reduced to a minimal
     * summary.
     */

    public PrintingTestListener(){
        this(new PrintStream(System.out));
    }

    public PrintingTestListener(PrintStream writer) {
        super();
        this.writer = writer;  
        if (System.getProperty("jmri.util.junit.PrintingTestListener.verbose", "false").equals("false")) {
            verbose = false;
        } else {
            verbose = true;
        }
        if (System.getProperty("jmri.util.junit.PrintingTestListener.quiet", "false").equals("false")) {
            quiet = false;
        } else {
            quiet = true;
        }
    }
    
    private final PrintStream writer;
    private final boolean verbose;
    private final boolean quiet;

    protected String formattedClassName(TestIdentifier identifier) {
        // JUnit4 and JUnit5 return different values for Description#getClassName
        return identifier.getDisplayName();
    }
    
    /**
     *  Called when an atomic test has finished, whether the test succeeds or fails.
     */
    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (verbose) {
            switch (testExecutionResult.getStatus()) {
                case SUCCESSFUL:
                    // don't do anything for this, it's the normal case
                    break;
                case ABORTED:
                    writer.println("ABORTED:          "+formattedClassName(testIdentifier)+"\n");
                    break;
                case FAILED:
                    writer.println("FAILED:           "+formattedClassName(testIdentifier)+"\n");
                    break;
                default:
                    writer.println("UNKNOWN:          "+formattedClassName(testIdentifier)+"\n");
                    break;
            }
        } else if (quiet) {
            switch (testExecutionResult.getStatus()) {
                case SUCCESSFUL:
                    writer.print(".");
                    break;
                case ABORTED:
                    writer.print("A");
                    writer.flush();
                    break;
                case FAILED:
                    writer.print("F");
                    writer.flush();
                    break;
                default:
                    writer.println("UNKNOWN:          "+formattedClassName(testIdentifier)+"\n");
                    break;
            }
        }
        
        // for accounting
        super.executionFinished(testIdentifier, testExecutionResult);
    }

    /**
     *  Called when a test will not be run, generally because a test method is annotated with Ignore.
     */
    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        if (verbose) {
            writer.println("Ignored : "+formattedClassName(testIdentifier)+" - "+reason);
        } else if (quiet) {
            writer.print("I");
            writer.flush();
        }
        
        // for accounting
        super.executionSkipped(testIdentifier, reason);
    }
  
    /**
     *  Called when an atomic test starts. Reimplemented to skip printing "."
     */
    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (verbose) {
            if (testIdentifier.isContainer()) {
                writer.println(" Class Started: "+formattedClassName(testIdentifier));
            } else if (testIdentifier.isTest()) {
                writer.println("   Test Started:  "+formattedClassName(testIdentifier));
            } else {
                writer.println("UNEXPECTED; neither container nor test: "+formattedClassName(testIdentifier));
            }
        }
        
        // for accounting
        super.executionStarted(testIdentifier);
    }

    /*
     * OK (946 tests)
     * Tests run: 42353, Failures: 1, Errors: 0, Skipped: 3129
     */
    public TestExecutionSummary getSummary() {
        if (!quiet) return super.getSummary();
        
        // create our own summary
        LocalTestExecutionSummary summary = new LocalTestExecutionSummary(super.getSummary());
        return summary;
    }
    
    /*
     * Made from JUnit5 MutableTestExecutionSummary which really should have been public
     */
    static private class LocalTestExecutionSummary implements TestExecutionSummary {
        public LocalTestExecutionSummary(TestExecutionSummary p) {
            provided = p;
        }
        TestExecutionSummary provided;
        
        @Override
        public void printTo(PrintWriter writer) {
            if (getTotalFailureCount() == 0 && getContainersAbortedCount() == 0) {
                writer.println("\nOK ("+getTestsSucceededCount()+" tests)");
                writer.flush();
                return;
            } else {
                writer.println("\nTests run: "+getTestsStartedCount()+", Failures and Errors: "+getTestsFailedCount()
                                +", Skipped: "+(getTestsSkippedCount()+getTestsAbortedCount()));
                writer.flush();
                return;
            }
        }

        @Override
        public long getTimeStarted() {
            return provided.getTimeStarted();
        }

        @Override
        public long getTimeFinished() {
            return provided.getTimeFinished();
        }

        @Override
        public long getTotalFailureCount() {
            return provided.getTotalFailureCount();
        }

        @Override
        public long getContainersFoundCount() {
            return provided.getContainersFoundCount();
        }

        @Override
        public long getContainersStartedCount() {
            return provided.getContainersStartedCount();
        }

        @Override
        public long getContainersSkippedCount() {
            return provided.getContainersSkippedCount();
        }

        @Override
        public long getContainersAbortedCount() {
            return provided.getContainersAbortedCount();
        }

        @Override
        public long getContainersSucceededCount() {
            return provided.getContainersSucceededCount();
        }

        @Override
        public long getContainersFailedCount() {
            return provided.getContainersFailedCount();
        }

        @Override
        public long getTestsFoundCount() {
            return provided.getTestsFoundCount();
        }

        @Override
        public long getTestsStartedCount() {
            return provided.getTestsFoundCount();
        }

        @Override
        public long getTestsSkippedCount() {
            return provided.getTestsSkippedCount();
        }

        @Override
        public long getTestsAbortedCount() {
            return provided.getTestsSkippedCount();
        }

        @Override
        public long getTestsSucceededCount() {
            return provided.getTestsSucceededCount();
        }

        @Override
        public long getTestsFailedCount() {
            return provided.getTestsFailedCount();
        }

        @Override
        public void printFailuresTo(PrintWriter writer) {
            provided.printFailuresTo(writer);
        }

        @Override
        public void printFailuresTo(PrintWriter writer, int maxStackTraceLines) {
            provided.printFailuresTo(writer, maxStackTraceLines);
        }

        @Override
        public List<Failure> getFailures() {
            return provided.getFailures();
        }    
    }
}
