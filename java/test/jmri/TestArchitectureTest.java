package jmri;

import org.junit.jupiter.api.*;

import com.tngtech.archunit.lang.*;
import com.tngtech.archunit.junit.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

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
    public static final ArchRule repeatedTestRule = noMethods().should()
        .beAnnotatedWith(org.junit.jupiter.api.RepeatedTest.class);

    /**
     * Please use org.junit.jupiter.api.Test
     */
    @ArchTest
    public static final ArchRule junit4TestRule = noClasses().that()
        .doNotHaveFullyQualifiedName("jmri.util.junit.rules.RetryRuleTest").and()
        .doNotHaveFullyQualifiedName("jmri.jmrit.display.logixng.ActionPositionableTest").and()
        .resideOutsideOfPackage("jmri.jmrit.logixng..").and()
        .resideOutsideOfPackage("jmri.jmrix.bidib..")
        .should().dependOnClassesThat().haveFullyQualifiedName("org.junit.Test");

    /**
     * Please use org.junit.jupiter.api.BeforeEach
     */
    @ArchTest
    public static final ArchRule junit4BeforeRule = noClasses().that()
        .doNotHaveFullyQualifiedName("jmri.util.junit.rules.RetryRuleTest").and()
        .doNotHaveFullyQualifiedName("jmri.jmrit.display.logixng.ActionPositionableTest").and()
        .resideOutsideOfPackage("jmri.jmrit.logixng..").and()
        .resideOutsideOfPackage("jmri.jmrix.bidib..")
        .should().dependOnClassesThat().haveFullyQualifiedName("org.junit.Before");
    
    /**
     * Please use org.junit.jupiter.api.AfterEach
     */
    @ArchTest
    public static final ArchRule junit4AfterRule = noClasses().that()
        .doNotHaveFullyQualifiedName("jmri.util.junit.rules.RetryRuleTest").and()
        .doNotHaveFullyQualifiedName("jmri.jmrit.display.logixng.ActionPositionableTest").and()
        .resideOutsideOfPackage("jmri.jmrit.logixng..").and()
        .resideOutsideOfPackage("jmri.jmrix.bidib..")
        .should().dependOnClassesThat().haveFullyQualifiedName("org.junit.After");

    /**
     * jmri should not reference org.apache.log4j to allow jmri
     * to be used as library in applications that choose not to use Log4J.
     */
    @ArchTest
    public static final ArchRule noLog4JinJmriTestsRule = noClasses()
        .that().doNotHaveFullyQualifiedName("jmri.util.JUnitAppender")
        .and().doNotHaveFullyQualifiedName("jmri.util.TestingLoggerConfiguration")
        .and().doNotHaveFullyQualifiedName("apps.jmrit.log.Log4JTreePaneTest")
        .should().dependOnClassesThat().resideInAPackage("org.apache.logging.log4j");

    /**
     * JUnit5 should not have abstract methods with Test annotation.
     * Instead, the overriding method should have the Test annotation.
     */
    @ArchTest
    public static final ArchRule noAbstractTestMethods = noMethods()
        .that().areAnnotatedWith(Test.class)
        .or().areAnnotatedWith(org.junit.jupiter.params.ParameterizedTest.class)
        .should()
        .haveModifier(com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT);

    /**
     * JUnit5 should not have abstract methods with LifeCycle annotation.
     * Instead, the overriding method should have the annotation.
     */
    @ArchTest
    public static final ArchRule noAbstractLifeCycleMethods = noMethods()
        .that().areAnnotatedWith(BeforeEach.class)
        .or().areAnnotatedWith(AfterEach.class)
        .or().areAnnotatedWith(BeforeAll.class)
        .or().areAnnotatedWith(AfterAll.class)
        .should()
        .haveModifier(com.tngtech.archunit.core.domain.JavaModifier.ABSTRACT);

}
