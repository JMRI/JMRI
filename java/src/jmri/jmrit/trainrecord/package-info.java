/**
 * Working with train records ({@link jmri.TrainRecord}) where the roster is
 * needed: estimating where a train's leading end is from its loco's speed
 * commands, speed profile and decoder momentum.
 * <p>
 * Everything here only reads: nothing holds, releases or drives a train.
 *
 * @since 5.19.1
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.jmrit.trainrecord;
