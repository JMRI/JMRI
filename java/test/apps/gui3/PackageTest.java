package apps.gui3;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    apps.gui3.dp3.PackageTest.class,
    apps.gui3.mdi.PackageTest.class,
    TabbedPreferencesActionTest.class,
    TabbedPreferencesFrameTest.class,
    TabbedPreferencesProfileActionTest.class,
    TabbedPreferencesTest.class,
    apps.gui3.paned.PackageTest.class,
    FirstTimeStartUpWizardTest.class,
    FirstTimeStartUpWizardActionTest.class,})

/**
 * Tests for GUI3 base class.
 * <p>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class PackageTest {

}
