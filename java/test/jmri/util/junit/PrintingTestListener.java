package jmri.util.junit;

import java.io.PrintStream;

import org.junit.internal.TextListener;
import org.junit.runner.*;
import org.junit.runner.notification.*;

public class PrintingTestListener extends TextListener {

    /**
     * A {@link TextListener} implementation that displays the name
     * of run, failed and ignored tests run by JUnit
     * <p>
     * based on the shipped 
     * <a href="https://github.com/junit-team/junit4/blob/master/src/main/java/org/junit/internal/TextListener.java">
        JUnit4 implementation of TextListener
     * </a>
     */
    
    public PrintingTestListener(PrintStream writer) {
        super(writer);
        this.writer = writer;  
    }
    
    private final PrintStream writer;  // duplicate of private in superclass

    /**
     *  Called when an atomic test has finished, whether the test succeeds or fails.
     */
    @Override
    public void testFinished(Description description) throws java.lang.Exception {
        writer.println("Finished: "+ description.getClassName()+"#"+description.getMethodName());
    }

    /**
     *  Called when an atomic test fails.
     */
    @Override
    public void testFailure(Failure failure) {
        writer.println("\nFAILED  : "+ failure.getMessage()+ " <===============================\n");
    }

    /**
     *  Called when a test will not be run, generally because a test method is annotated with Ignore.
     */
    @Override
    public void testIgnored(Description description) {
        writer.println("Ignored : "+ description.getClassName()+"#"+description.getMethodName());
    }
  
    /**
     *  Called when an atomic test starts. Reimplemented to skip printing "."
     */
    @Override
    public void testStarted(Description description) {
    }
  
}