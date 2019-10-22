package jmri;

import org.junit.*;
import org.junit.runner.*;

import com.tngtech.archunit.*;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.*;
import com.tngtech.archunit.lang.*;
import com.tngtech.archunit.library.freeze.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Check the architecture of the JMRI library
 * <p>
 * This inherits from the 
 * {link ArchitectureTest} class, which includes tests that are passing.
 * This file is for checks that are valid, but not yet passing due to 
 * (too many) existing violations.
 * <p>
 * Note that this only checks the classes in target/classes, which come from java/src, not
 * the ones in target/test-classes, which come from java/test.  It's relying on the common
 * build procedure to make this distinction.
 *
 * @author Bob Jacobsen 2019
 */
 
@RunWith(ArchUnitRunner.class)  // Remove this for JUnit 5
 
// Pick up all classes from the target/classes directory, which is just the main (not test) code
@AnalyzeClasses(packages = {"target/classes"}) // "jmri","apps"

public class ArchitectureCheck extends ArchitectureTest {

    // want these statics first in class, to initialize
    // logging before various static items are constructed
    @BeforeClass  // tests are static
    static public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }
    @AfterClass
    static public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    /**
     * No access to apps outside of itself.
     */
    @ArchTest // Initially 92 flags in JMRI 4.17.3
    public static final ArchRule checkAppsPackage = FreezingArchRule.freeze(
            classes()
            .that().resideInAPackage("apps..")
            .should().onlyBeAccessed().byAnyPackage("apps..")
        );

    /**
     * No access to jmri.jmrix outside of itself and apps
     */
    @ArchTest // Initially 226 flags in JMRI 4.17.3
    public static final ArchRule checkJmrixPackage = FreezingArchRule.freeze(
            classes()
            .that().resideInAPackage("jmri.jmrix..")
            .should().onlyBeAccessed().byAnyPackage("jmri.jmrix..", "apps..")
        );

    /**
     * No access to jmri.jmrit outside of itself and apps
     */
    @ArchTest // Initially 2061 flags in JMRI 4.17.3
    public static final ArchRule checkJmritPackage = FreezingArchRule.freeze(
            classes()
            .that().resideInAPackage("jmri.jmrit..")
            .should().onlyBeAccessed().byAnyPackage("jmri.jmrit..", "apps..")
        );

    /**
     * No jmri.jmrit in basic jmri interfaces.
     * <p>
     * Intentionally redundant with the check for references to
     * jmri.jmrit outside itself; fix these first!
     * <p>
     * 
     */
    @ArchTest // Initially 458 flags in JMRI 4.17.3
    public static final ArchRule checkJmriPackageJmrit = FreezingArchRule.freeze(
            noClasses()
            .that().resideInAPackage("jmri")
            .should().dependOnClassesThat().resideInAPackage("jmri.jmrit..")
        );

    /**
     * Jmri.util should only reference jmri, not any below that (except jmri.util, of course)
     * 
     */
    @ArchTest // Initially 311 flags in JMRI 4.17.3
    public static final ArchRule checkJmriUtil = FreezingArchRule.freeze(
            noClasses()
            .that()
                .resideInAPackage("jmri.util..")
            .should().dependOnClassesThat()
                    .resideOutsideOfPackages("jmri", "jmri.util..",
                                             "java..", "javax..", "org..") // swing et al imported
        );
    /**
     * Jmri.jmris should not reference jmri.jmrit
     * <p>
     * Intentionally redundant with the check for references to
     * jmri.jmrit outside itself; fix these first!
     * <p>
     * 
     */
    @ArchTest
    public static final ArchRule checkJmrisPackageJmrit = FreezingArchRule.freeze(
            noClasses()
            .that().resideInAPackage("jmri.jmris")
            .should().dependOnClassesThat().resideInAPackage("jmri.jmrit..")
        );

    /**
     * Jmri.server should not reference jmri.jmrit
     * <p>
     * Intentionally redundant with the check for references to
     * jmri.jmrit outside itself; fix these first!
     * <p>
     * 
     */
    @ArchTest // initially 45 flags in JMRI 4.17.3
    public static final ArchRule checkServerPackageJmrit = FreezingArchRule.freeze(
            noClasses()
            .that().resideInAPackage("jmri.jmris")
            .should().dependOnClassesThat().resideInAPackage("jmri.jmrit..")
        );
    
}
