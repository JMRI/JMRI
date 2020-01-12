/**
 * Tests for the jmri.jmrit.catalog package
 *
 * @author	Bob Jacobsen 2009
 */
package jmri.jmrit.catalog;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    CatalogTreeFSTest.class,
    CatalogTreeIndexTest.class,
    ImageIndexEditorTest.class,     
    BundleTest.class,
    jmri.jmrit.catalog.configurexml.PackageTest.class,
    CatalogPaneTest.class,
    CatalogPanelTest.class,
    CatalogTreeModelTest.class,
    DefaultCatalogTreeManagerTest.class,
    CatalogTreeNodeTest.class,
    NamedIconTest.class,
    DirectorySearcherTest.class,
    CatalogTreeLeafTest.class,
    DragJLabelTest.class,
    PreviewDialogTest.class,
})

public class PackageTest {
}
