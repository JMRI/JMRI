/**
 *        This package contains software specific to the RPS system from 
 *        <A HREF="http://www.nacservicesinc.com/">NAC Services, Inc</A>.
 *       <p>
 *            Tools are provided to input (and eventually output)
 *            several layers of information:
 *       <ul>
 *            <li>Raw input (Readings)
 *            <li>Processed values (Measurements)
 *            <li>...
 *        </ul>
 *        There are several pieces that do the behind the scene operation.
 *        <dl>
 *            <dt>Distributor<dd>There's exactly one of these. It's basic 
 *                function is to handle all the computations.  These include:
 *                <ol>
 *                    <li>Hold the alignment and control information
 *                    <li>Receive Readings
 *                    <li>Distribute the Readings to anybody interested
 *                    <li>Compute Measurements from them
 *                    <li>Distribute the Measurements to whoever is interested
 *                </ol>
 *            <dt>ControlPanel<dd>There can be at most one of these, which can
 *                manually update the alignment and control information. 
 *            <dt>Monitor<dd>Shows the Readings and Measurements as they are produced.
 *            <dt>SerialAdapter<dd>Connect (as configured) to a serial port
 *                and create Readings for the Distributor
 *        </dl>
 *
 *        <h2>Limitations</h2>
 *
 *        <ol>
 *            <li>This provides only one computation at a time (through the Distributor),
 *                which is a bit of pain when doing comparative work.
 *            <li>The alignment contants are stored in a fixed file location in the
 *                JMRI preferences directory.
 *        </ol>
 *
 *        <h2>Related Documentation</h2>
 *
 *        For overviews, tutorials, examples, guides, and tool documentation, please see:
 *        <ul>
 *            <li>
 *        </ul>
 *        <!-- Put @see and @since tags down here. -->
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings({})
package jmri.jmrix.rps;
