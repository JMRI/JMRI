 /**
 * Support for implementing USS CTC systems in JMRI.
 *
 * <h2>Related Documentation</h2>
 *
 * For overviews, tutorials, examples, guides, and tool documentation, please see:
 * <ul>
 *   <li><a href="http://jmri.org/help/en//html/tools/uss/index.shtml">JMRI USS CTC overview page</a>
 *   <li><a href="http://jmri.org/resources/icons/USSpanels/">Icons and other useful graphical bits</a>
 * </ul>
 *
 * Sequence diagrams of how all this goes together:
 * <a href="doc-files/CodeButtonPressTurnout.png"><img src="doc-files/CodeButtonPressTurnout.png" style="text-align: right;" alt="Code press" height="25%" width="25%"></a>
 * <a href="doc-files/TurnoutChangeIndication.png"><img src="doc-files/TurnoutChangeIndication.png" style="text-align: right;" alt="Indication" height="25%" width="25%"></a>
 * <HR>
 * These are intended to be connected together and initialized by Jython glue code.
 * An example can be found in
 * < href="https:jmri.org/jython/ctc">https:jmri.org/jython/ctc</a>.
 *
 * <p>
 *     Some older tools also have *Panel, *Frame and *Action support classes, and
 *     are implemented and persisted via Routes and Logix.  This approach didn't
 *     scale well enough, so is no longer being used in favor of the Jython approach.
 * <HR>
 * <!-- Put @see and @since tags down here. -->
 * @see jmri.managers
 * @see jmri.implementation
*/

//@annotations for the entire package go here
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.jmrit.ussctc;
