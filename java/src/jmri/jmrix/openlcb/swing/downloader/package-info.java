/**
 * Provides a download (bootload) tool for OpenLCB nodes.
 *
 * <h2>Related Documentation</h2>
 *
 * This tool relies for reliable operation on an OpenLCB feature called "Freeze/Unfreeze". 
 * Although properly adopted by the group, 
 * and in defiance of a written agreement between the group and the NMRA,
 * this was removed during the move toward NMRA adoption in February 2015. 
 * JMRI is using the feature for three reasons:
 * <ol>
 * <li>It's the right technical solution
 * <li>It's properly implemented in the OpenLCB Java library, which was part of
 * OpenLCB before prototype development was pushed out of that effort.
 * <li>It's the most-recent properly adopted specification.
 * </ol>
 * People doing future development in this area might want to consider the info 
 * <a href="http://jmri.org/help/en/package/jmri/jmrix/openlcb/swing/downloader/OpenLCBlibrary.html">presented in a separate page</a>, 
 * taken in part from the OpenLCB documentation before Freeze/Unfreeze was removed.
 *
 * <!-- Put @see and @since tags down here. -->
 * @since JMRI 4.1.1
 * @see jmri.managers
 * @see jmri.implementation
 */
package jmri.jmrix.openlcb.swing.downloader;
