/**
 * Create a where used report for a selected table object.
 * <p>
 * The main window has a combo box for selecting the item type, such as sensor.  When a
 * type is selected, the second combo box is used to select the item.  When the Create
 * button is pressed the matches are listed in the text area.
 * <p>
 * Each item type has a class which finds the references using the WhereUsedCollectors methods.
 * Each collector method calls the getUsageReport in the related implementation class.
 * The resulting report is passed back to the window class for display.
 * <pre>
 * WhereUsedFrame
 *     |-- {beanname}WhereUsed.getWhereUsed(bean)  :: return text area
 *       |-- WhereUsedCollectors.{method}(bean)    :: return usage string
 *         |-- {instance}.getUsageReport(bean)     :: return NamedBeanUsageReport array
 * </pre>
 */

//@annotations for the entire package go here
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.jmrit.whereused;
