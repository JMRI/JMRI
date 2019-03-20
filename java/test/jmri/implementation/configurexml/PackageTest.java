package jmri.implementation.configurexml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DccSignalHeadXmlTest.class,
    DccSignalMastXmlTest.class,
    DoubleTurnoutSignalHeadXmlTest.class,
    LsDecSignalHeadXmlTest.class,
    MatrixSignalMastXmlTest.class,
    MergSD2SignalHeadXmlTest.class,
    QuadOutputSignalHeadXmlTest.class,
    SchemaTest.class,
    LoadAndStoreTest.class,
    SE8cSignalHeadXmlTest.class,
    SignalHeadSignalMastXmlTest.class,
    SingleTurnoutSignalHeadXmlTest.class,
    TripleOutputSignalHeadXmlTest.class,
    TripleTurnoutSignalHeadXmlTest.class,
    TurnoutSignalMastXmlTest.class,
    VirtualSignalHeadXmlTest.class,
    VirtualSignalMastXmlTest.class
})
/**
 * Tests for the jmri.implementation.configurexml package.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class PackageTest {
}
