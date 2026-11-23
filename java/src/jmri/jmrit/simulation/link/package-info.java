/**
 * Simulated links: the time a message takes between JMRI and a simulated
 * layout, through the interface (gateway) and over the bus or wire.
 * <p>
 * A {@link jmri.jmrit.simulation.link.SimulatedLink} is two short pipelines
 * of {@link jmri.jmrit.simulation.link.Stage}s, one for each direction, with
 * the shared {@link jmri.jmrit.simulation.link.Medium} between them. The
 * stages and the medium are the same for every system; a system supplies
 * only its {@link jmri.jmrit.simulation.link.WireTiming} (how long a message
 * takes on its wire, and who wins when two want it) and calls the link where
 * its simulator already exchanges messages with JMRI. Interfaces are
 * {@link jmri.jmrit.simulation.link.LinkPreset}s: named lists of stages with
 * their numbers.
 * <p>
 * A link is off until a preset is chosen: every message is then passed on at
 * once, on the caller's thread, exactly as with no link.
 *
 * @since 5.19.2
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.jmrit.simulation.link;
