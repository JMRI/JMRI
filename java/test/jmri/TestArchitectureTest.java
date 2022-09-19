package jmri;

import org.junit.jupiter.api.*;

import com.tngtech.archunit.lang.*;
import com.tngtech.archunit.junit.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * Check the architecture of the JMRI library Tests 
* <p>
 * This is run as part of CI, so it's expected to kept passing at all times.
 * <p>
 * Note that this only checks the classes in target/test-classes, which come from java/test, not
 * the ones in target/classes, which come from java/src.  It's relying on the common
 * build procedure to make this distinction.
 * Based on {@link ArchitectureTest}
 * 
 * See examples in the <a href='https://github.com/TNG/ArchUnit-Examples/tree/master/example-plain/src/test/java/com/tngtech/archunit/exampletest">ArchUnit sample code</a>.
 *
 * @author Bob Jacobsen 2019
 * @author Steve Young Copyright (c) 2022
 */

// Pick up all classes from the target/test-classes directory, which is the test code
@AnalyzeClasses(packages = {"target/test-classes"}) // "jmri","apps"

public class TestArchitectureTest {

    // want these statics first in class, to initialize
    // logging before various static items are constructed
    @BeforeAll  // tests are static
    static public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterAll
    static public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    /**
     * Prevent @RepeatedTest annotations from being accidentally merged.
     */
    @ArchTest
    public static final ArchRule rule = noMethods().should()
        .beAnnotatedWith(org.junit.jupiter.api.RepeatedTest.class);

}
