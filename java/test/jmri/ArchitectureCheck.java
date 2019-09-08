package jmri;

import org.junit.*;

import com.tngtech.archunit.*;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Check the architecture of the JMRI library
 * <p>
 * Note that this only checkst the classes in target/classes, which come from java/src, not
 * the ones in target/test-classes, which come from java/test.  It's relying on the common
 * build procedure to make this distinction.
 *
 * @author Bob Jacobsen 2019
 */
public class ArchitectureCheck {

    /**
     * No access to apps outside of itself.
     */
    @Test // Initially 92 flags in JMRI 4.17.3
    public void checkAppsPackage() {
        ArchRule thisRule = classes()
            .that().resideInAPackage("apps..")
            .should().onlyBeAccessed().byAnyPackage("apps..");
          
        thisRule.check(importedAllClasses);      
    }

    /**
     * No access to jmri.jmrix outside of itself and apps
     */
    @Test // Initially 226 flags in JMRI 4.17.3
    public void checkJmrixPackage() {
        ArchRule thisRule = classes()
            .that().resideInAPackage("jmri.jmrix..")
            .should().onlyBeAccessed().byAnyPackage("jmri.jmrix..", "apps..");
          
        thisRule.check(importedAllClasses);      
    }

    /**
     * No access to jmri.jmrit outside of itself and apps
     */
    @Test // Initially 2061 flags in JMRI 4.17.3
    public void checkJmritPackage() {
        ArchRule thisRule = classes()
            .that().resideInAPackage("jmri.jmrit..")
            .should().onlyBeAccessed().byAnyPackage("jmri.jmrit..", "apps..");
          
        thisRule.check(importedAllClasses);      
    }

    /**
     * No jmri.jmrix in basic interfaces.
     * <p>
     * Intentionally redundant with the check for references to
     * jmri.jmrix outside itself; fix these first!
     */
    @Test // Initially 1 flags in JMRI 4.17.3
    public void checkJmriPackageJmrix() {
        ArchRule thisRule = noClasses()
        .that().resideInAPackage("jmri")
        .should().dependOnClassesThat().resideInAPackage("jmri.jmrix..");
          
        thisRule.check(importedAllClasses);      
    }

    /**
     * No jmri.jmrit in basic interfaces.
     * <p>
     * Intentionally redundant with the check for references to
     * jmri.jmrit outside itself; fix these first!
     * <p>
     * 
     */
    @Test // Initially 458 flags in JMRI 4.17.3
    public void checkJmriPackageJmrit() {
        ArchRule thisRule = noClasses()
        .that().resideInAPackage("jmri")
        .should().dependOnClassesThat().resideInAPackage("jmri.jmrit..");
          
        thisRule.check(importedAllClasses);      
    }

    /**
     * No AWT in basic interfaces.
     */
    @Test // Initially 8 flags in JMRI 4.17.3
    public void checkJmriPackageAwt() {
        ArchRule thisRule = noClasses()
        .that().resideInAPackage("jmri")
        .should().dependOnClassesThat().resideInAPackage("java.awt..");
          
        thisRule.check(importedAllClasses);      
    }

    /**
     * No Swing in basic interfaces.
     */
    @Test // Initially 5 flags in JMRI 4.17.3
    public void checkJmriPackageSwing() {
        ArchRule thisRule = noClasses()
        .that().resideInAPackage("jmri")
        .should().dependOnClassesThat().resideInAPackage("javax.swing..");
          
        thisRule.check(importedAllClasses);      
    }

    /**
     * No JDOM in basic interfaces.
     */
    @Test // Initially 3 flags in JMRI 4.17.3
    public void checkJmriPackageJdom() {
        ArchRule thisRule = noClasses()
        .that().resideInAPackage("jmri")
        .should().dependOnClassesThat().resideInAPackage("org.jdom2..");
          
        thisRule.check(importedAllClasses);      
    }

    /**
     * (Try to) confine JDOM to configurexml packages.
     * (Is this working right? Seems to not flag anything)
     */
    @Test
    public void checkJdomOutsideConfigurexml() {
        ArchRule thisRule = classes()
            .that().resideInAPackage("org.jdom2..")
            .should().onlyBeAccessed().byAnyPackage("..configurexml..");
          
        thisRule.check(importedAllClasses);      
    }

    /**
     * Check that *Bundle classes inherit from their parent.
     * (not done yet, not sure how to do it)
     */
    @Test
    public void checkBundleInheritance() {
        ArchRule thisRule = classes()
            .that().areAssignableTo(jmri.Bundle.class)
            .should().haveSimpleNameEndingWith("Bundle");
        thisRule.check(importedAllClasses);      
    }

    /**
     * Check that *Bundle classes are named Bundle
     */
    @Test
    public void checkBundleNames() {
        ArchRule thisRule = classes()
            .that().areAssignableTo(jmri.Bundle.class)
            .should().haveSimpleName("Bundle");
        thisRule.check(importedAllClasses);      
    }

    /**
     * Check that classes named *Bundle are Bundles
     */
    @Test
    public void checkBundleNamesOnlyOnBundleClass() {
        ArchRule thisRule = classes()
            .that().haveSimpleNameEndingWith("Bundle")
            .should().beAssignableTo(jmri.Bundle.class);
        thisRule.check(importedAllClasses);      
    }

    JavaClasses importedAllClasses; // inclusive contents of all packages compiled from jmri/src
    
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        importedAllClasses = new ClassFileImporter().importPath("target/classes/");
    }

    @After
    public void tearDown() {
        importedAllClasses = null;
        jmri.util.JUnitUtil.tearDown();
    }

}
