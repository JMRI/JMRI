package jmri;

import com.tngtech.archunit.lang.*;
import com.tngtech.archunit.junit.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.io.File;

import jmri.util.FileUtil;

import org.junit.jupiter.api.*;

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
 * See {@link TestArchitectureTest}
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
     * No access to java.awt.event.MouseEvent except as specified
     */
    @ArchTest // Initially 50 flags in JMRI 4.17.4 - see archunit_ignore_patterns.txt
    public static final ArchRule checkMouseEvent = noClasses().that()
                                // classes with permitted access (temporary violations go in archunit_ignore_patterns.txt)
                                .doNotHaveFullyQualifiedName("jmri.util.swing.JmriMouseEvent").and()
                                .doNotHaveFullyQualifiedName("jmri.util.swing.JmriMouseListener").and()
                                .doNotHaveFullyQualifiedName("jmri.util.swing.JmriMouseMotionListener").and()

                                .doNotHaveFullyQualifiedName("apps.SystemConsole$PopupListener").and()
                                .doNotHaveFullyQualifiedName("apps.startup.StartupActionsPreferencesPanel$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.beantable.BeanTableDataModel$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.beantable.ListedTableFrame$ActionJList").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.beantable.RowComboBoxPanel").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.beantable.RowComboBoxPanel$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.beantable.block.BlockTableDataModel$ImageIconRenderer$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.beantable.light.LightTableDataModel$ImageIconRenderer$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.beantable.oblock.TableFrames$2").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.beantable.oblock.TableFrames").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.beantable.sensor.SensorTableDataModel$ImageIconRenderer$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.beantable.turnout.TurnoutTableDataModel$ImageIconRenderer$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.beantable.turnout.TurnoutTableJTable").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.catalog.ImageIndexEditor").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.catalog.ImageIndexEditor$2").and()
                                .doNotHaveFullyQualifiedName("mri.jmrit.display.layoutEditor.LayoutEditorToolBarPanel$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.display.switchboardEditor.BeanSwitch").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.display.switchboardEditor.BeanSwitch$2").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.display.switchboardEditor.BeanSwitch$3").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.display.layoutEditor.LayoutEditorToolBarPanel$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.entryexit.EntryExitPairs$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.jython.JynstrumentPopupMenu$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.logix.WarrantRoute$RouteLocation").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.logixng.tools.swing.ConditionalNGDebugger$PopupMenu$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.logixng.tools.swing.TreeEditor$5").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.operations.locations.LocationEditFrame$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.operations.locations.LocationEditFrame$2").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.operations.locations.LocationEditFrame$3").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.roster.swing.RosterGroupsPanel$MouseAdapter").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.roster.swing.RosterTable$RosterCellEditor").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.symbolicprog.CombinedLocoSelTreePane$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.throttle.FunctionButton$PopupListener").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.can.cbus.swing.nodeconfig.CbusNodeNVEditTablePane$NvSpinnerEditor").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrit.symbolicprog.CombinedLocoSelTreePane$2").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetMetricsFrame$DataButtonMouseListener").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.cmri.serial.cmrinetmanager.CMRInetMetricsFrame$ErrMetricButtonMouseListener").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.dccpp.swing.ConfigBaseStationFrame$1").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.dccpp.swing.ConfigBaseStationFrame$2").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.dccpp.swing.ConfigBaseStationFrame$3").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.dccpp.swing.ConfigBaseStationFrame$4").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.dccpp.swing.ConfigBaseStationFrame$5").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.dccpp.swing.ConfigBaseStationFrame").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.ecos.utilities.EcosLocoToRoster$4").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.ecos.utilities.EcosLocoToRoster$6").and()
                                .doNotHaveFullyQualifiedName("jmri.jmrix.rps.trackingpanel.RpsTrackingPanel").and()
                                .doNotHaveFullyQualifiedName("jmri.profile.ProfileManagerDialog").and()
                                .doNotHaveFullyQualifiedName("jmri.profile.ProfileManagerDialog$1").and()
                                .doNotHaveFullyQualifiedName("jmri.profile.ProfilePreferencesPanel$1").and()
                                .doNotHaveFullyQualifiedName("jmri.profile.ProfilePreferencesPanel$2").and()
                                .doNotHaveFullyQualifiedName("jmri.swing.EditableList$MouseListener").and()
                                .doNotHaveFullyQualifiedName("jmri.util.BusyGlassPane$CBListener").and()
                                .doNotHaveFullyQualifiedName("jmri.util.JTreeWithPopup$1").and()
                                .doNotHaveFullyQualifiedName("jmri.util.swing.JmriMouseListener$1").and()
                                .doNotHaveFullyQualifiedName("jmri.util.swing.JmriMouseMotionListener$1").and()
                                .doNotHaveFullyQualifiedName("jmri.util.swing.TriStateJCheckBox$1").and()
                                .doNotHaveFullyQualifiedName("jmri.util.table.JTableWithColumnToolTips$1").and()
                                .doNotHaveFullyQualifiedName("jmri.util.table.ButtonEditor").and()
                                .doNotHaveFullyQualifiedName("jmri.util.table.ToggleButtonEditor").and()
                                .doNotHaveFullyQualifiedName("jmri.web.servlet.frameimage.JmriJFrameServlet")

                            .should()
                                .dependOnClassesThat().haveFullyQualifiedName("java.awt.event.MouseEvent");

    /**
     * No access to System.err and System.out except as specified
     */
    @ArchTest // Initially 50 flags in JMRI 4.17.4 - see archunit_ignore_patterns.txt
    public static final ArchRule checkStandardStreams = noClasses().that()
                                // classes with permitted access (temporary violations go in archunit_ignore_patterns.txt)
                                .doNotHaveFullyQualifiedName("apps.JavaVersionCheckWindow").and()
                                .doNotHaveFullyQualifiedName("apps.gui3.paned.QuitAction").and()
                                .doNotHaveFullyQualifiedName("apps.jmrit.decoderdefn.DecoderIndexBuilder").and()
                                .doNotHaveFullyQualifiedName("jmri.util.FileUtilSupport").and() // used in log4j init
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
            .should().dependOnClassesThat().resideInAPackage("org.apache.logging.log4j");

    /**
     * JMRI (but not apps) should use org.slf4j.Logger instead of JUL.
     */
    @ArchTest
    public static final ArchRule noJULinJmri = noClasses()
        .that().resideInAPackage("jmri..")
        .should().dependOnClassesThat().resideInAPackage("java.util.logging");

    /**
     * Confine JDOM to configurexml packages.
     */
    @ArchTest
    @ArchIgnore // 5792 flags September 2022
    public static final ArchRule checkJdomOutsideConfigurexml = noClasses()
        .that().resideOutsideOfPackage("..configurexml..")
        .should().accessClassesThat().resideInAPackage("org.jdom2..");

    /**
     * Purejavacomm is the previous serial library. It should not be used
     * by JMRI java code, but there are scripts that uses it. And there
     * might be users that have scripts using it. So we need to keep the
     * library but prevent Java code to use it.
     */
    @ArchTest
    public static final ArchRule checkPurejavacommUsage = noClasses()
        .should().accessClassesThat().resideInAPackage("purejavacomm..");

    /**
     * Confine jSerialComm to jmri.jmrix.AbstractSerialPortController with
     * limited exceptions
     */
    @ArchTest
    public static final ArchRule checkJSerialCommAllowedUses = noClasses()
        .that()

        // all the standard serial access should be confined to here:
        .doNotHaveFullyQualifiedName("jmri.jmrix.AbstractSerialPortController").and()
        .doNotHaveFullyQualifiedName("jmri.jmrix.AbstractSerialPortController$SerialPort").and()
        .doNotHaveFullyQualifiedName("jmri.jmrix.AbstractSerialPortController$SerialPortEvent")

        .should().accessClassesThat().resideInAPackage("com.fazecast.jSerialComm..");

    /**
     * Check that *Bundle classes inherit from their parent.
     * (not done yet, not sure how to do it)
     */
    @ArchTest
    @ArchIgnore // Not complete
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

    /**
     * No classes in jmri.jmrit.logixng.actions should access jmri.NamedBeanHandle.
     * They should use jmri.jmrit.logixng.util.LogixNG_SelectNamedBean instead.
     */
    @ArchTest
    public static final ArchRule checkLogixNGActionsNotUsingNamedBeanHandle = noClasses()
            .that()
            .resideInAPackage("jmri.jmrit.logixng.actions")
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionListenOnBeans")                            // This class doesn't seem to be able to use LogixNG_SelectNamedBean
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionListenOnBeans$NamedBeanReference")         // This class doesn't seem to be able to use LogixNG_SelectNamedBean
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionListenOnBeansTable")                       // This class doesn't seem to be able to use LogixNG_SelectNamedBean
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionListenOnBeansTable$NamedBeanReference")    // This class doesn't seem to be able to use LogixNG_SelectNamedBean
            .should()
            .dependOnClassesThat().haveFullyQualifiedName("jmri.NamedBeanHandle");

    /**
     * No classes in jmri.jmrit.logixng.actions should access jmri.NamedBeanHandle.
     * They should use jmri.jmrit.logixng.util.LogixNG_SelectNamedBean instead.
     */
    @ArchTest
    public static final ArchRule checkLogixNGActionsXmlNotUsingNamedBeanHandle = noClasses()
            .that()
            .resideInAPackage("jmri.jmrit.logixng.actions.configurexml")
            .should()
            .dependOnClassesThat().haveFullyQualifiedName("jmri.NamedBeanHandle");

    /**
     * No classes in jmri.jmrit.logixng.expressions should access jmri.NamedBeanHandle.
     * They should use jmri.jmrit.logixng.util.LogixNG_SelectNamedBean instead.
     */
    @ArchTest
    public static final ArchRule checkLogixNGExpressionsNotUsingNamedBeanHandle = noClasses()
            .that()
            .resideInAPackage("jmri.jmrit.logixng.expressions")
            .should()
            .dependOnClassesThat().haveFullyQualifiedName("jmri.NamedBeanHandle");

    /**
     * No classes in jmri.jmrit.logixng.expressions should access jmri.NamedBeanHandle.
     * They should use jmri.jmrit.logixng.util.LogixNG_SelectNamedBean instead.
     */
    @ArchTest
    public static final ArchRule checkLogixNGExpressionsXmlNotUsingNamedBeanHandle = noClasses()
            .that()
            .resideInAPackage("jmri.jmrit.logixng.expressions.configurexml")
            .should()
            .dependOnClassesThat().haveFullyQualifiedName("jmri.NamedBeanHandle");

    /**
     * No classes in jmri.jmrit.logixng.actions should access jmri.NamedBeanHandle.
     * They should use jmri.jmrit.logixng.util.LogixNG_SelectNamedBean instead.
     */
    @ArchTest
    public static final ArchRule checkLogixNGActionsNotUsingBeanSelectPanel = noClasses()
            .that()
            .resideInAPackage("jmri.jmrit.logixng.actions..")
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionCreateBeansFromTableSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionFindTableRowOrColumnSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionListenOnBeansTableSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionLocalVariableSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionMemorySwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionOBlockSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionReporterSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionSetReporterSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionSignalHeadSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionSignalMastSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionTableSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.ActionWarrantSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.swing.TableForEachSwing")   // Not converted to Select... yet
            .should()
            .dependOnClassesThat().haveFullyQualifiedName("jmri.util.swing.BeanSelectPanel");

    /**
     * No classes in jmri.jmrit.logixng.expressions should access jmri.NamedBeanHandle.
     * They should use jmri.jmrit.logixng.util.LogixNG_SelectNamedBean instead.
     */
    @ArchTest
    public static final ArchRule checkLogixNGExpressionsNotUsingBeanSelectPanel = noClasses()
            .that()
            .resideInAPackage("jmri.jmrit.logixng.expressions..")
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.swing.ExpressionLocalVariableSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.swing.ExpressionMemorySwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.swing.ExpressionReporterSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.swing.ExpressionSignalHeadSwing")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.swing.ExpressionSignalMastSwing")   // Not converted to Select... yet
            .should()
            .dependOnClassesThat().haveFullyQualifiedName("jmri.util.swing.BeanSelectPanel");

    /**
     * No classes in jmri.jmrit.logixng.actions should access jmri.jmrit.logixng.NamedBeanAddressing.
     * They should use jmri.jmrit.logixng.util.LogixNG_Select* instead.
     */
    @ArchTest
    public static final ArchRule checkLogixNGActionsNotUsingNamedBeanAddressing = noClasses()
            .that()
            .resideInAPackage("jmri.jmrit.logixng.actions")
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionDispatcher")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionDispatcher$1") // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionLight")        // Data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionLight$1")      // Data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionOBlock")       // Data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionOBlock$1")     // Data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionReporter")     // Data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionReporter$1")   // Data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionScript")       // Oper and data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionScript$1")     // Oper and data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionSignalHead")   // Oper and data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionSignalHead$1") // Oper and data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionSignalMast")   // Oper and data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionSignalMast$1") // Oper and data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionSound")        // "Sound" not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionSound$1")      // "Sound" not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionWarrant")      // Data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ActionWarrant$1")    // Data not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ExecuteDelayed")     // State not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ExecuteDelayed$1")   // State not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.ExecuteDelayed$2")   // State not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.TableForEach")       // "Row or column converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.actions.TableForEach$1")     // "Row or column converted to Select... yet
            .should()
            .dependOnClassesThat().haveFullyQualifiedName("jmri.jmrit.logixng.NamedBeanAddressing");

    /**
     * No classes in jmri.jmrit.logixng.actions should access jmri.jmrit.logixng.NamedBeanAddressing.
     * They should use jmri.jmrit.logixng.util.LogixNG_Select* instead.
     */
    @ArchTest
    public static final ArchRule checkLogixNGExpressionsNotUsingNamedBeanAddressing = noClasses()
            .that()
            .resideInAPackage("jmri.jmrit.logixng.expressions")
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionAudio")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionAudio$1")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionConditional")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionConditional$1")   // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionDispatcher")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionDispatcher$1")  // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionEntryExit")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionEntryExit$1")  // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionLight")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionLight$1")  // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionOBlock")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionOBlock$1")  // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionScript")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionScript$1")  // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionSignalHead")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionSignalHead$1")  // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionSignalMast")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionSignalMast$1")  // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionTurnout")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionTurnout$1")  // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionWarrant")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.ExpressionWarrant$1")  // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.Timer")    // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.Timer$1")  // Not converted to Select... yet
            .and().doNotHaveFullyQualifiedName("jmri.jmrit.logixng.expressions.Timer$2")  // Not converted to Select... yet
            .should()
            .dependOnClassesThat().haveFullyQualifiedName("jmri.jmrit.logixng.NamedBeanAddressing");


    @Test
    public void testHelpFileNamesUseShtml(){
        String path = FileUtil.getExternalFilename(FileUtil.PROGRAM + "help");

        // allow
        // local/index.html
        // local/stub_template.html
        // /local/stub/

        String[] allowList = {"local"+File.separator+ "index.html",
            "local"+File.separator+ "stub_template.html",
            File.separator + "local" + File.separator + "stub" + File.separator };
        recursivelyCheckFiles(new File(path), allowList, ".html");
    }

    private void recursivelyCheckFiles(File directory, String[] allowList, String deniedsuffix) {
        File[] files = directory.listFiles();
        if (files == null) {
            Assertions.fail("Failed to list files in the directory: " + directory.getAbsolutePath());
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                recursivelyCheckFiles(file, allowList, deniedsuffix);
            } else {
                String fname = file.getAbsolutePath();
                if (fname.endsWith(deniedsuffix) && notOnAllowList(fname,allowList)) {
                    // System.out.println("Incorrect fileType: "+fname);
                    Assertions.fail("filename " +fname+ " should not end with "+deniedsuffix);
                }
            }
        }
    }

    private boolean notOnAllowList(@javax.annotation.Nonnull String filePath, String[] allowList) {
        for (String allowed : allowList) {
            if ( filePath.contains(allowed) ) {
                return false;
            }
        }
        return true;
    }

}
