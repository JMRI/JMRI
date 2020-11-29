/**
 * Defines classes for communicating with SPROG Generation 5 hardware via a
 * serial com port (or USB virtual COM port) with CBUS opcodes.
 *
 * <p>
 * SPROG Generation 5 hardware can use CBUS opcodes and the MERG version of the
 * gridconnect protocol over the layout connection but does not necessarily
 * implement a CAN bus interface. The SPROG 3 Plus, for example, integrates the
 * functionality of a CANUSB equivalent gridconnect adapter and a CBUS command
 * station but has no CAN interface.
 * 
 * <p>
 * This package imports from {@link jmri.jmrix.can.adapters.gridconnect.canrs}
 * package for the MERG specific gridconnect implementation.
 * 
 * <p>
 * The CBUS implementation is held in the {@link jmri.jmrix.can.cbus} package.
 * 
 * <h2>Related Documentation</h2>
 *
 * For SPROG documentation, please see:
 * <ul>
 * <li><a href="http://www.sprog-dcc.co.uk/">SPROG DCC website</a>
 * </ul>
 *
 * <!-- Put @see and @since tags down here. -->
 * @since 4.19
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.jmrix.can.adapters.gridconnect.sproggen5;
