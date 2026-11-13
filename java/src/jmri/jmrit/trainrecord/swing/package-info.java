/**
 * Windows for train records ({@link jmri.TrainRecord}): the set-up checks,
 * reached from the Tools menu's Train Tracking submenu, which list what in a
 * layout's set-up makes following trains less sure; and the Block dialog,
 * which shows the trains a Block holds, why, and what has happened there.
 * <p>
 * Everything here only reads: nothing holds, releases or drives a train.
 *
 * @since 5.19.1
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.jmrit.trainrecord.swing;
