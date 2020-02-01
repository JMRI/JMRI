package jmri.util.junit;

import java.io.PrintStream;

import org.junit.internal.TextListener;
import org.junit.runner.*;
import org.junit.runner.notification.*;

public class PrintingTestListener extends TextListener {

    /**
     * A {@link TextListener} implementation that can display the name
     * of run, failed and ignored tests run by JUnit
     * <p>
     * When the <code>jmri.util.junit.PrintingTestListener.verbose</code>
     * property is empty or false, this acts like it's super-class
     * to provide default behavior.
     * <p>
     * Based on the shipped 
     * <a href="https://github.com/junit-team/junit4/blob/master/src/main/java/org/junit/internal/TextListener.java">
        JUnit4 implementation of TextListener
     * </a>
     */
    
    public PrintingTestListener(PrintStream writer) {
        super(writer);
        this.writer = writer;  
        if (System.getProperty("jmri.util.junit.PrintingTestListener.verbose", "false").equals("false")) {
            verbose = false;
        } else {
            verbose = true;
        }
        
    }
    
    private final PrintStream writer;  // duplicate of private in superclass
    private final boolean verbose;

    protected String formattedClassName(Description description) {
        // JUnit4 and JUnit5 return different values for Description#getClassName
        return description.getTestClass().getName()+"#"+description.getMethodName();
    }
    
    /**
     *  Called when an atomic test has finished, whether the test succeeds or fails.
     */
    @Override
    public void testFinished(Description description) throws java.lang.Exception {
        if (verbose) {
            writer.println("Finished: "+formattedClassName(description));
        } else {
            super.testFinished(description);
        }
    }

    /**
     *  Called when an atomic test fails.
     */
    @Override
    public void testFailure(Failure failure) {
        if (verbose) {
            writer.println("\nFAILED  : "+ failure.getMessage()+ " <===============================\n");
        } else {
            super.testFailure(failure);
        }
    }

    /**
     *  Called when a test will not be run, generally because a test method is annotated with Ignore.
     */
    @Override
    public void testIgnored(Description description) {
        if (verbose) {
            writer.println("Ignored : "+formattedClassName(description));
        } else {
            super.testIgnored(description);
        }
    }
  
    /**
     *  Called when an atomic test starts. Reimplemented to skip printing "."
     */
    @Override
    public void testStarted(Description description) {
        if (verbose) {
            // doing nothing verbose here
        } else {
            super.testStarted(description);
        }
    }
  
}
