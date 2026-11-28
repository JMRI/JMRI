/**
 * The JMRI JSON protocol server for train records: where JMRI's train
 * tracking places each train, and the operator's corrections to it.
 * <p>
 * Replies include, in the JSON data object:
 * <dl>
 * <dt>name</dt><dd>The train's name.</dd>
 * <dt>rosterEntry</dt><dd>The id of the lead loco's roster entry, or null.</dd>
 * <dt>placed</dt><dd>True if the train is on the layout.</dd>
 * <dt>blocks</dt><dd>The system names of the Blocks it is in, front first
 * when the front is known.</dd>
 * <dt>frontKnown</dt><dd>True if the first Block is known to hold the
 * train's front.</dd>
 * <dt>confidence</dt><dd>How sure train tracking is of where the train
 * is, or null if it is not on the layout.</dd>
 * </dl>
 * A client corrects train tracking by posting to a train record:
 * {@code block} places the train in that Block alone ("the train is here"),
 * and {@code emptyBlock} takes every train out of that Block ("this Block is
 * empty"). A correction needs the Correct where trains are permission: when
 * the permission system is on, the client logs in with {@code sessionLogin}
 * and sends the token it is given as {@code token}.
 *
 * @since 5.19.2
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value={})
package jmri.server.json.trainrecord;
