package jmri.util;

import java.awt.GraphicsEnvironment;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test rule that only executes tests in presence of GUI environment.
 * Declare a field of this type in your class and annotate with {@link org.junit.Rule}:
 * <code>
 * <pre>
 * &#64;Rule public AssumeGUIRule   assumeGUI = new AssumeGUIRule();
 * </pre>
 * </code>
 * All the tests in the class will be skipped in headless environment.
 * 
 * @author Svata Dedic Copyright (c) 2019
 */
public class AssumeGUIRule implements TestRule {
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (GraphicsEnvironment.isHeadless()) {
                    throw new AssumptionViolatedException("Headless environment. Skipping test!");
                } else {
                    base.evaluate();
                }
            }
        };
    }
    
    public boolean hasGUI() {
        return !GraphicsEnvironment.isHeadless();
    }
}
