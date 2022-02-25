package jmri;

import org.junit.jupiter.api.*;

import com.tngtech.archunit.lang.*;
import com.tngtech.archunit.junit.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Check the architecture of the JMRI library
 * <p>
 * This is run as part of CI, so it's expected to kept passing at all times.
 * It includes some exceptions that have been grandfathered in
 * via the archunit_ignore_patterns.txt file.
 * <p>
 * Checks that are not yet passing are added in the
 * {link ArchitectureCheck} class, which can be run independently.
 * <p>
 * Note that this only checks the classes in target/classes, which come from java/src, not
 * the ones in target/test-classes, which come from java/test.  It's relying on the common
 * build procedure to make this distinction.
 *
 * See examples in the <a href='https://github.com/TNG/ArchUnit-Examples/tree/master/example-plain/src/test/java/com/tngtech/archunit/exampletest">ArchUnit sample code</a>.
 *
 * @author Bob Jacobsen 2019
 */

// Pick up all classes from the target/classes directory, which is just the main (not test) code
@AnalyzeClasses(packages = {"target/classes"}) // "jmri","apps"

public class ArchitectureTest {

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
     * No access to System.err and System.out except as specified
     */
    @ArchTest // Initially 50 flags in JMRI 4.17.4 - see archunit_ignore_patterns.txt
    public static final ArchRule checkStandardStreams = noClasses().that()
                                // classes with permitted access (temporary violations go in archunit_ignore_patterns.txt)
                                .doNotHaveFullyQualifiedName("apps.JavaVersionCheckWindow").and()
                                .doNotHaveFullyQualifiedName("apps.gui3.paned.QuitAction").and()
                                .doNotHaveFullyQualifiedName("apps.jmrit.decoderdefn.DecoderIndexBuilder").and()
                                .doNotHaveFullyQualifiedName("jmri.util.GetArgumentList").and()
                                .doNotHaveFullyQualifiedName("jmri.util.GetClassPath").and()
                                .doNotHaveFullyQualifiedName("jmri.util.GetJavaProperty").and()
                                .doNotHaveFullyQualifiedName("jmri.Version").and()
                                .doNotHaveFullyQualifiedName("jmri.util.JTextPaneAppender").and()
                                .doNotHaveFullyQualifiedName("jmri.util.EarlyInitializationPreferences").and()
                                .doNotHaveFullyQualifiedName("jmri.script.jsr223graalpython.GraalJSEngineFactory").and()
                                // generated code that we don't have enough control over
                                .resideOutsideOfPackage("jmri.jmris.simpleserver..").and()
                                .resideOutsideOfPackage("jmri.jmris.srcp..").and()
                                .resideOutsideOfPackage("jmri.jmrix.srcp..")
                            .should(
                                com.tngtech.archunit.library.GeneralCodingRules.
                                ACCESS_STANDARD_STREAMS
                            );

    /**
     * No access to java.util.Timer except jmri.util.TimerUtil
     */
    @ArchTest
    public static final ArchRule checkTimerClassRestricted = noClasses().that()
                                // classes with permitted access
                                .haveNameNotMatching("jmri\\.util\\.TimerUtil")
                            .should()
                                .dependOnClassesThat().haveFullyQualifiedName("java.util.Timer");

    /**
     * No access to javax.annotation.Nullable except the FindBugsCheck test routine
     */
    @ArchTest
    public static final ArchRule checkNullableAnnotationRestricted = noClasses().that()
                                // classes with permitted access
                                .haveNameNotMatching("apps\\.FindBugsCheck")
                            .should()
                                .dependOnClassesThat().haveFullyQualifiedName("javax.annotation.Nullable");

   /**
     * No jmri.jmrix in basic interfaces.
     * <p>
     * Intentionally redundant with the check for references to
     * jmri.jmrix outside itself; fix these first!
     */
    @ArchTest // Initially 1 flags in JMRI 4.17.3 - see archunit_ignore_patterns.txt
    public static final ArchRule checkJmriPackageJmrix = noClasses()
        .that().resideInAPackage("jmri")
        .should().dependOnClassesThat().resideInAPackage("jmri.jmrix..");

    /**
     * Jmri.jmris should not reference jmri.jmrix
     * <p>
     * Intentionally redundant with the check for references to
     * jmri.jmrix outside itself; fix these first!
     * <p>
     *
     */
    @ArchTest
    public static final ArchRule checkJmrisPackageJmrix = noClasses()
        .that().resideInAPackage("jmri.jmris")
        .should().dependOnClassesThat().resideInAPackage("jmri.jmrix..");

    /**
     * Jmri.server should not reference jmri.jmrix
     * <p>
     * Intentionally redundant with the check for references to
     * jmri.jmrix outside itself; fix these first!
     * <p>
     *
     */
    @ArchTest
    public static final ArchRule checkServerPackageJmrix = noClasses()
        .that().resideInAPackage("jmri.server")
        .should().dependOnClassesThat().resideInAPackage("jmri.jmrix..");

    /**
     * Jmri.server should not reference jmri.jmrit
     * <p>
     * Intentionally redundant with the check for references to
     * jmri.jmrit outside itself; fix these first!
     * <p>
     *
     */
    @ArchTest
    public static final ArchRule checkServerPackageJmrit = noClasses()
            .that().resideInAPackage("jmri.server")
            .should().dependOnClassesThat().resideInAPackage("jmri.jmrit..");

    /**
     * Jmri.web should not reference jmri.jmrit
     * <p>
     * Intentionally redundant with the check for references to
     * jmri.jmrit outside itself; fix these first!
     * <p>
     *
     */
    @ArchTest
    public static final ArchRule checkWebPackageJmrit = noClasses()
        .that().resideInAPackage("jmri.web")
        .should().dependOnClassesThat().resideInAPackage("jmri.jmrit..");

    /**
     * Jmri.web should not reference jmri.jmrix
     * <p>
     * Intentionally redundant with the check for references to
     * jmri.jmrix outside itself; fix these first!
     * <p>
     *
     */
    @ArchTest
    public static final ArchRule checkWebPackageJmrix = noClasses()
        .that().resideInAPackage("jmri.web")
        .should().dependOnClassesThat().resideInAPackage("jmri.jmrix..");

    /**
     * No AWT in basic interfaces.
     */
    @ArchTest // Initially 8 flags in JMRI 4.17.3 - see archunit_ignore_patterns.txt
    public static final ArchRule checkJmriPackageAwt = noClasses()
        .that().resideInAPackage("jmri")
        .should().dependOnClassesThat().resideInAPackage("java.awt..");

    /**
     * No Swing in basic interfaces.
     */
    @ArchTest // Initially 5 flags in JMRI 4.17.3 - see archunit_ignore_patterns.txt
    public static final ArchRule checkJmriPackageSwing = noClasses()
        .that().resideInAPackage("jmri")
        .should().dependOnClassesThat().resideInAPackage("javax.swing..");

    /**
     * No JDOM in basic interfaces.
     */
    @ArchTest // Initially 3 flags in JMRI 4.17.3 - see archunit_ignore_patterns.txt
    public static final ArchRule checkJmriPackageJdom = noClasses()
        .that().resideInAPackage("jmri")
        .should().dependOnClassesThat().resideInAPackage("org.jdom2..");

    /**
     * jmri (but not apps) should not reference org.apache.log4j to allow jmri
     * to be used as library in applications that choose not to use Log4J.
     */
    @ArchTest
    public static final ArchRule noLog4JinJmri = noClasses()
            .that().resideInAPackage("jmri..")
            .and().areNotAnnotatedWith(Deprecated.class)
            .should().dependOnClassesThat().resideInAPackage("org.apache.log4j");

    /**
     * (Try to) confine JDOM to configurexml packages.
     * (Is this working right? Seems to not flag anything)
     *  Probably not working because the JDOM classes are not part of initially-read set
     */
    @ArchTest // Not complete
    public static final ArchRule checkJdomOutsideConfigurexml = classes()
            .that().resideInAPackage("org.jdom2..")
            .should().onlyBeAccessed().byAnyPackage("..configurexml..");

    /**
     * (Try to) confine purejavacomm to jmri.jmrix packages.
     * (Is this working right? Seems to not flag anything; note jmri.jmrit as a test below)
     *  Probably not working because the purejavacomm classes are not part of initially-read set
     */
    @ArchTest // Not complete
    public static final ArchRule checkPurejavacoomOutsideConfigurexml = classes()
            .that().resideInAPackage("purejavacomm..")
            .should().onlyBeAccessed().byAnyPackage("jmri.jmrit");

    /**
     * Check that *Bundle classes inherit from their parent.
     * (not done yet, not sure how to do it)
     */
    @ArchTest // Not complete
    public static final ArchRule checkBundleInheritance = classes()
            .that().areAssignableTo(jmri.Bundle.class)
            .should().haveSimpleNameEndingWith("Bundle");

    /**
     * Check that *Bundle classes are named Bundle
     */
    @ArchTest
    public static final ArchRule checkBundleNames = classes()
            .that().areAssignableTo(jmri.Bundle.class)
            .should().haveSimpleName("Bundle");

    /**
     * Check that classes named *Bundle are Bundles
     */
    @ArchTest
    public static final ArchRule checkBundleNamesOnlyOnBundleClass = classes()
            .that().haveSimpleNameEndingWith("Bundle")
            .should().beAssignableTo(jmri.Bundle.class);

}
