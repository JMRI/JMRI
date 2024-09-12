package jmri;

import org.junit.jupiter.api.*;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.*;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
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
        .resideOutsideOfPackage("jmri.jmrit.logixng..")
        .should().dependOnClassesThat().haveFullyQualifiedName("org.junit.Test");

    /**
     * Please use org.junit.jupiter.api.BeforeEach
     */
    @ArchTest
    public static final ArchRule junit4BeforeRule = noClasses().that()
        .doNotHaveFullyQualifiedName("jmri.util.junit.rules.RetryRuleTest").and()
        .doNotHaveFullyQualifiedName("jmri.jmrit.display.logixng.ActionPositionableTest").and()
        .resideOutsideOfPackage("jmri.jmrit.logixng..")
        .should().dependOnClassesThat().haveFullyQualifiedName("org.junit.Before");
    
    /**
     * Please use org.junit.jupiter.api.AfterEach
     */
    @ArchTest
    public static final ArchRule junit4AfterRule = noClasses().that()
        .doNotHaveFullyQualifiedName("jmri.util.junit.rules.RetryRuleTest").and()
        .doNotHaveFullyQualifiedName("jmri.jmrit.display.logixng.ActionPositionableTest").and()
        .resideOutsideOfPackage("jmri.jmrit.logixng..")
        .should().dependOnClassesThat().haveFullyQualifiedName("org.junit.After");

    /**
     * JMRI should not reference org.apache.log4j to allow JMRI
     * to be used as library in applications that choose not to use Log4J.
     */
    @ArchTest
    public static final ArchRule noLog4JinJmriTestsRule = noClasses()
        .that().doNotHaveFullyQualifiedName("jmri.util.JUnitAppender")
        .and().doNotHaveFullyQualifiedName("jmri.util.TestingLoggerConfiguration")
        .and().doNotHaveFullyQualifiedName("apps.jmrit.log.Log4JTreePaneTest")
        .should().dependOnClassesThat().resideInAPackage("org.apache.logging.log4j")
        .because("Tests should normally use org.slf4j.Logger instead of Log4J");

    /**
     * JMRI tests should use org.slf4j.Logger instead of JUL.
     */
    @ArchTest
    public static final ArchRule minimalJULinJmriTests = noClasses()
        .that().resideInAPackage("jmri..")
        .and().doNotHaveFullyQualifiedName("jmri.util.TestingLoggerConfiguration") // tests jul routed to l4j OK
        .and().doNotHaveFullyQualifiedName("jmri.util.web.BrowserFactory") // 3rd party lib setup
        .and().doNotHaveFullyQualifiedName("jmri.web.WebServerAcceptanceSteps") // testing output from lib
        .should().dependOnClassesThat().resideInAPackage("java.util.logging")
        .because("Tests should normally use org.slf4j.Logger instead of JUL");

    /**
     * setUp methods should normally use the org.junit.jupiter.api.BeforeEach annotation.
     */
    @ArchTest
    public static final ArchRule setUpMethodsHaveBeforeEachAnnotation =
        methods()
        .that().haveName("setUp")
        .and().doNotHaveModifier(JavaModifier.ABSTRACT)
        .and().areNotDeclaredIn(jmri.util.JUnitUtil.class)
        // JUnit4
        .and().areDeclaredInClassesThat().resideOutsideOfPackage("jmri.jmrit.logixng..")
        .and().areNotDeclaredIn(jmri.jmrit.display.logixng.ActionPositionableTest.class)
        .and().areNotDeclaredIn(jmri.util.junit.rules.RetryRuleTest.class)
        .should()
        .beAnnotatedWith(BeforeEach.class)
        .orShould().beAnnotatedWith(BeforeAll.class)
        .because("setUp methods should normally use the BeforeEach annotation");

    /**
     * tearDown methods should normally use the org.junit.jupiter.api.AfterEach annotation.
     */
    @ArchTest
    public static final ArchRule tearDownMethodsHaveAfterEachAnnotation =
        methods()
        .that().haveName("tearDown")
        .and().doNotHaveModifier(JavaModifier.ABSTRACT)
        .and().areNotDeclaredIn(jmri.util.JUnitUtil.class)
        // JUnit4
        .and().areDeclaredInClassesThat().resideOutsideOfPackage("jmri.jmrit.logixng..")
        .and().areNotDeclaredIn(jmri.jmrit.display.logixng.ActionPositionableTest.class)
        .and().areNotDeclaredIn(jmri.util.junit.rules.RetryRuleTest.class)
        .should()
        .beAnnotatedWith(AfterEach.class)
        .orShould().beAnnotatedWith(AfterAll.class)
        .because("tearDown methods should normally use the AfterEach annotation");

    /**
     * JMRI does not require PackageTest.class .
     */
    @ArchTest
    public static final ArchRule noJUnit4PackageTestsRule = noClasses()
        .should().haveSimpleName("PackageTest")
        .because("JMRI does not require PackageTest.class");

    /**
     * JUnit5 should not have abstract methods with Test annotation.
     * Instead, the overriding method should have the Test annotation.
     */
    @ArchTest
    public static final ArchRule noAbstractTestMethods = noMethods()
        .that().areAnnotatedWith(Test.class)
        .or().areAnnotatedWith(org.junit.jupiter.params.ParameterizedTest.class)
        .should()
        .haveModifier(JavaModifier.ABSTRACT)
        .because("The overriding method should have the Test annotation, not the abstract.");

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
        .haveModifier(JavaModifier.ABSTRACT)
        .because("The overriding method should have the Test Lifecycle annotation, not the abstract.");

    /**
     * Tests should not have empty methods
     * as this saves invoking both the setUp and tearDown Class methods.
     */
    @ArchTest
    public static final ArchRule no_empty_test_methods = 
        ArchRuleDefinition.methods()
            .that().areAnnotatedWith(Test.class)
            .and().areNotAnnotatedWith(Disabled.class)
            .and().areNotAnnotatedWith(jmri.util.junit.annotations.NotApplicable.class)
            // java assert may be removed in compilation resulting in an empty method
            .and().areNotDeclaredIn(jmri.util.junit.AssertTest.class)
            .should(notBeEmpty())
            .because("this saves invoking both the setUp and tearDown Class methods. "
            +"Please use @Disabled or @jmri.util.junit.annotations.NotApplicable");

    private static ArchCondition<JavaMethod> notBeEmpty() {
        return new ArchCondition<>("not be empty") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                if (method.getMethodCallsFromSelf().isEmpty() && method.getRawParameterTypes().isEmpty()) {
                    String message = String.format("Method %s is empty", method.getFullName());
                    events.add(SimpleConditionEvent.violated(method, message));
                }
            }
        };
    }

}
