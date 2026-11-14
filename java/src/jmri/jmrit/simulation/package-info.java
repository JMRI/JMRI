/**
 * Simulated trains: virtual trains that move on the layout's own track model,
 * driven by the speed commands JMRI sends, and report their passage through
 * the sensors a real train would trip.
 * <p>
 * Nothing here runs until a train is placed, and a train can only be placed
 * in a profile whose connections are all simulators (see
 * {@link jmri.jmrit.simulation.SimulationCheck}). Placed trains obey the
 * Internal connection's throttles
 * ({@link jmri.jmrit.simulation.InternalThrottleSpeedSource}) and set internal
 * sensors ({@link jmri.jmrit.simulation.InternalSensorOutput}), so in a
 * profile with an Internal connection Dispatcher, Warrants, throttle windows
 * and scripts drive them with no further set-up.
 * <h2>Independence</h2>
 * The simulator reads the layout's configuration (Blocks, Paths, sensors,
 * turnouts, the Layout Editor drawing, roster data) but works everything out
 * itself: routes, decoder momentum, speed from the speed profile's
 * measurements, crossings and occupancy. It repeats some calculations JMRI
 * already has on purpose, so that a fault in JMRI's own logic shows as a
 * difference between JMRI and the simulator rather than the two agreeing.
 * An architecture test stops classes here from using the logic under test.
 *
 * @since 5.19.1
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.jmrit.simulation;
