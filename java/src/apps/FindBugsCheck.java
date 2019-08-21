package apps;

/**
 * Check how SpotBugs (formally FindBugs) and annotations interact.
 * <p>
 * Note: This deliberately causes SpotBugs warnings!  Do not remove or annotate them!
 *       Instead, when past its useful point, just comment out the body of the
 *       class so as to leave the code examples present.
 * <p>
 * Tests Nonnull, Nullable and CheckForNull from both
 * javax.annotation and edu.umd.cs.findbugs annotations
 * packages.
 * <p>
 * Comments indicate observed (and unobserved) warnings from SpotBugs 3.0.1
 * <p>
 * This has no main() because it's not expected to run:  It will certainly
 * throw a NullPointerException right away.  The idea is for SpotBugs to
 * find those in static analysis. This is in java/src, instead of java/test,
 * so that our usual CI infrastructure builds it.
 * <p>
 * Types are explicitly qualified (instead of using 'import') to make it
 * completely clear which is being used at each point.  That makes this the
 * code less readable, so it's not recommended for general use.
 * <p>
 * The "ja" prefix means that javax annotations are used; "fb"
 * that FindBugs/SpotBugs annotations are used. "no" means un-annotated.
 * <p>
 * The comments are the warnings thrown by SpotBugs 3.1.9
 * <p>
 * Summary: <ul>
 * <li>The javax.annotation and edu.umd.cs.findbugs.annotations versions work the same
 *          (So we should use the javax.annotation ones as they're the future standard)
 * <li>Nullable doesn't detect the errors that CheckForNull does flag
 * <li>Parameter passing isn't always being checked by SpotBugs
 * </ul>
 * @see apps.CheckerFrameworkCheck
 * @author Bob Jacobsen 2016
 */
public class FindBugsCheck {

    void test() { // something that has to be executed on an object
        System.out.println("test "+this.getClass());
    }

 //  comment out the rest of the file to avoid SpotBugs counting these deliberate warnings

    public FindBugsCheck noAnnotationReturn() {
        return null;
    }
    public void noAnnotationParm(FindBugsCheck p) {
        p.test();
    }
    public void noAnnotationTest() {
        noAnnotationReturn().test();

        noAnnotationParm(this);
        noAnnotationParm(null); // Null passed for non-null parameter NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS
        
        noAnnotationParm(noAnnotationReturn());
        
        noAnnotationParm(jaNonnullReturn());
        noAnnotationParm(jaNullableReturn()); // should be flagged?
        noAnnotationParm(jaCheckForNullReturn()); // should be flagged?

        noAnnotationParm(fbNonnullReturn());
        noAnnotationParm(fbNullableReturn()); // should be flagged?
        noAnnotationParm(fbCheckForNullReturn()); // should be flagged?
    }

    // Test Nonnull

    @javax.annotation.Nonnull public FindBugsCheck jaNonnullReturn() {
        return null; // may return null, but is declared @Nonnull NP_NONNULL_RETURN_VIOLATION
    }
    public void jaNonNullParm(@javax.annotation.Nonnull FindBugsCheck p) {
        p.test();
    }
    public void jaTestNonnull() {
        jaNonnullReturn().test();

        jaNonNullParm(this);
        jaNonNullParm(null); // Null passed for non-null parameter NP_NONNULL_PARAM_VIOLATION
        
        jaNonNullParm(noAnnotationReturn());

        jaNonNullParm(jaNonnullReturn());
        jaNonNullParm(jaNullableReturn()); // should be flagged?
        jaNonNullParm(jaCheckForNullReturn()); // definitely should be flagged!

        jaNonNullParm(fbNonnullReturn());
        jaNonNullParm(fbNullableReturn()); // should be flagged?
        jaNonNullParm(fbCheckForNullReturn()); // definitely should be flagged!
    }

    @edu.umd.cs.findbugs.annotations.NonNull public FindBugsCheck fbNonnullReturn() {
        return null; // may return null, but is declared @Nonnull NP_NONNULL_RETURN_VIOLATION
    }
    public void fbNonNullParm(@edu.umd.cs.findbugs.annotations.NonNull FindBugsCheck p) {
        p.test();
    }
    public void fbTestNonnull() {
        fbNonnullReturn().test();

        fbNonNullParm(this);
        fbNonNullParm(null); // Null passed for non-null parameter NP_NONNULL_PARAM_VIOLATION

        fbNonNullParm(noAnnotationReturn()); // should be flagged?

        fbNonNullParm(fbNonnullReturn());
        fbNonNullParm(fbNullableReturn()); // should be flagged?
        fbNonNullParm(fbCheckForNullReturn()); // definitely should be flagged!

        fbNonNullParm(jaNonnullReturn());
        fbNonNullParm(jaNullableReturn()); // should be flagged?
        fbNonNullParm(jaCheckForNullReturn()); // definitely should be flagged!
    }


    // Test Nullable

    @javax.annotation.Nullable public FindBugsCheck jaNullableReturn() {
        return null;
    }
    public void jaNullableParm(@javax.annotation.Nullable FindBugsCheck p) {
        p.test(); // p must be non-null but is marked as nullable NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE
    }
    public void jaTestNullable() {
        jaNullableReturn().test(); // isn't flagged

        jaNullableParm(this);
        jaNullableParm(null);

        jaNullableParm(noAnnotationReturn());

        jaNullableParm(jaNonnullReturn());
        jaNullableParm(jaNullableReturn());
        jaNullableParm(jaCheckForNullReturn());

        jaNullableParm(fbNonnullReturn());
        jaNullableParm(fbNullableReturn());
        jaNullableParm(fbCheckForNullReturn());
    }

    @edu.umd.cs.findbugs.annotations.Nullable public FindBugsCheck fbNullableReturn() {
        return null;
    }
    public void fbNullableParm(@edu.umd.cs.findbugs.annotations.Nullable FindBugsCheck p) {
        p.test(); // p must be non-null but is marked as nullable NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE
    }
    public void fbTestNullable() {
        fbNullableReturn().test(); // isn't flagged

        fbNullableParm(this);
        fbNullableParm(null);

        fbNullableParm(noAnnotationReturn());
        
        fbNullableParm(fbNonnullReturn());
        fbNullableParm(fbNullableReturn());
        fbNullableParm(fbCheckForNullReturn());

        fbNullableParm(jaNonnullReturn());
        fbNullableParm(jaNullableReturn());
        fbNullableParm(jaCheckForNullReturn());
    }


    // Test CheckForNull

    @javax.annotation.CheckForNull public FindBugsCheck jaCheckForNullReturn() {
        return null;
    }
    public void jaCheckForNullParm(@javax.annotation.CheckForNull FindBugsCheck p) {
        p.test(); // p must be non-null but is marked as nullable NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE
    }
    public void jaTestCheckForNull() {
        jaCheckForNullReturn().test(); // Possible null pointer dereference NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE

        jaCheckForNullParm(this);
        jaCheckForNullParm(null);

        jaCheckForNullParm(noAnnotationReturn());
        
        jaCheckForNullParm(jaNonnullReturn());
        jaCheckForNullParm(jaNullableReturn());
        jaCheckForNullParm(jaCheckForNullReturn());

        jaCheckForNullParm(fbNonnullReturn());
        jaCheckForNullParm(fbNullableReturn());
        jaCheckForNullParm(fbCheckForNullReturn());
    }

    @edu.umd.cs.findbugs.annotations.CheckForNull public FindBugsCheck fbCheckForNullReturn() {
        return null;
    }
    public void fbCheckForNullParm(@edu.umd.cs.findbugs.annotations.CheckForNull FindBugsCheck p) {
        p.test(); // p must be non-null but is marked as nullable NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE
    }
    public void fbTestCheckForNull() {
        fbCheckForNullReturn().test(); // Possible null pointer dereference NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE

        fbCheckForNullParm(this);
        fbCheckForNullParm(null);

        fbCheckForNullParm(noAnnotationReturn());
        
        fbCheckForNullParm(fbNonnullReturn());
        fbCheckForNullParm(fbNullableReturn());
        fbCheckForNullParm(fbCheckForNullReturn());

        fbCheckForNullParm(jaNonnullReturn());
        fbCheckForNullParm(jaNullableReturn());
        fbCheckForNullParm(jaCheckForNullReturn());
    }

 //end of commenting out file

}