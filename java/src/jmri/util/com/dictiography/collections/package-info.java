package jmri.util.com.dictiography.collections;
/**
 *  Provides sorted, yet directly accessible, {@link IndexedTreeSet} and {@link IndexedTreeMap} collection classes.
 
 * <p>This package contains indexed collection code from  https://code.google.com/archive/p/indexed-tree-map/
 * with additional updates:
 * <ul>
 * <li> to apply a PR from https://github.com/stephenmcd/indexed-tree-map/commit/124e60f0f6def3d9aa7882f03d254ddded08b4bf
 * <li> to connect the provided JUnit tests to JMRI's test infrastructure
 * <li> to add tests that clarify and confirm various features
 * <li> to bypass various type-safety and Javadoc warnings during JMRI's CI validation process.
 * <li>
 * </ul>
 *
 * <p>
 *   These are Java 1.6 classes, and haven't been updated to Java 1.8 ideas about generics.
 *   That might be an interesting and useful project for somebody.
 * 
 * <!-- Put @see and @since tags down here. -->
 * @since JMRI 4.11.4
 */
