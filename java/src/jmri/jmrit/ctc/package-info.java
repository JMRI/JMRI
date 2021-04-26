/**
 * The CTC system provides the ability to create and run prototypical CTC dispatching.
 * <p>
 * <a href="doc-files/CTC_Data_Classes.png"><img src="doc-files/CTC_Data_Classes.png" alt="CTC Data Classes" height="33%" width="33%"></a>
 * <p>
 * The data is stored in the standard PanelPro xml files.
 * The data classes have references to JMRI beans.  There are 3 CTC NBH... classes which act as wrappers to the
 * NamedBeanHandles for the JMRI NamedBeans.  The NBH... classes encapsulate the NamedBeanHandles to make program access easier.
 * <p>
 * A list of each NBH type is maintained in CtcManager.  This provides a single location
 * to find the NBH object for a particular name.  To maintain consistency, there can only be one NBH for each
 * name.  When a NamedBeanHandle is renamed, the new name is added to the list using the same NBH object.  This way
 * code that has the name string can still find the object.
 */

//@annotations for the entire package go here
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.jmrit.ctc;
