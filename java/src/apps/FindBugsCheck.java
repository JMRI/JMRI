package apps;

/** 
 * Check how FindBugs and annotations interact.  
 * <p>
 * Note: This deliberately causes FindBugs warnings!  Do not remove or annotate them!
 *       Instead, when past its useful point, just comment out the body of the
 *       class so as to leave the code examples present.
 * <p>
 * Tests Nonnull, Nullable and CheckForNull from both 
 * javax.annotation and edu.umd.cs.findbugs.annotations
 * packages.
 * <p>
 * Comments indicate observed (and unobserved) warnings from FindBugs 3.0.1
 * <p>
 * This has no main() because it's not expected to run:  It will certainly
 * throw a NullPointerException right away.  The idea is for FindBugs to 
 * find those in static analysis
 * <p>
 * Types are explicitly qualified (instead of using 'import') to make it 
 * completely clear which is being used at each point.  That makes this the
 * code less readable, so it's not recommended for general use.
 * <p>
 * Summary: <ul>
 * <li>The javax.annotation and edu.umd.cs.findbugs.annotations versions work the same
 *          (So we should use the javax.annotation ones as they're the future standard)
 * <li>Nullable doesn't detect the errors that CheckForNull does flag
 * <li>Parameter passing isn't always being checked by FindBugs
 * </ul>
 * @see apps.CheckerFrameworkCheck
 * @author Bob Jacobsen 2016
 */
public class FindBugsCheck {
    
    void test() { // something that has to be executed on an object
        System.out.println("test "+this.getClass());
    }

//  commenting out the rest of the file to avoid FindBugs counting the deliberate warnings


    public FindBugsCheck noAnnotationReturn() {
        return null;
    }
    public void noAnnotationParm(FindBugsCheck p) {
        p.test();
    }
    public void noAnnotationTest() {
        noAnnotationReturn().test();

        noAnnotationParm(this);
        noAnnotationParm(null); // (NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS) Null passed for non-null parameter of noAnnotationParm(FindBugsCheck)
                                // That one's interesting, because FindBugs has apparently decided on its own that this the parameter shouldn't be null
        
        noAnnotationParm(noAnnotationReturn());
        noAnnotationParm(jaNonnullReturn());
        noAnnotationParm(jaNullableReturn()); // should be flagged?
        noAnnotationParm(jaCheckForNullReturn()); // definitely should be flagged!
    }

    // Test Nonnull
    
    @javax.annotation.Nonnull public FindBugsCheck jaNonnullReturn() {
        return null; // (NP_NONNULL_RETURN_VIOLATION) may return null, but is declared @Nonnull
    }
    public void jaNonNullParm(@javax.annotation.Nonnull FindBugsCheck p) {
        p.test();
    }
    public void jaTestNonnull() {
        jaNonnullReturn().test();

        jaNonNullParm(this);
        jaNonNullParm(null);  // (NP_NONNULL_PARAM_VIOLATION) Null passed for non-null parameter
        
        jaNonNullParm(noAnnotationReturn());
        jaNonNullParm(jaNonnullReturn());
        jaNonNullParm(jaNullableReturn()); // should be flagged?
        jaNonNullParm(jaCheckForNullReturn()); // definitely should be flagged!
    }

    @edu.umd.cs.findbugs.annotations.NonNull public FindBugsCheck fbNonnullReturn() {
        return null; // (NP_NONNULL_RETURN_VIOLATION) may return null, but is declared @NonNull
    }
    public void fbNonNullParm(@edu.umd.cs.findbugs.annotations.NonNull FindBugsCheck p) {
        p.test();
    }
    public void fbTestNonnull() {
        fbNonnullReturn().test();

        fbNonNullParm(this);
        fbNonNullParm(null); // (NP_NONNULL_PARAM_VIOLATION) Null passed for non-null parameter  
        
        fbNonNullParm(noAnnotationReturn());
        fbNonNullParm(fbNonnullReturn());
        fbNonNullParm(fbNullableReturn()); // should be flagged?
        fbNonNullParm(fbCheckForNullReturn()); // definitely should be flagged!
    }


    // Test Nullable
    
    @javax.annotation.Nullable public FindBugsCheck jaNullableReturn() {
        return null;
    }
    public void jaNullableParm(@javax.annotation.Nullable FindBugsCheck p) {
        p.test(); // (NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE) This parameter is always used in a way that requires it to be non-null, but the parameter is explicitly annotated as being Nullable.
    }
    public void jaTestNullable() {
        jaNullableReturn().test(); // isn't flagged

        jaNullableParm(this);
        jaNullableParm(null);
        
        jaNullableParm(noAnnotationReturn());
        jaNullableParm(jaNonnullReturn());
        jaNullableParm(jaNullableReturn());
        jaNullableParm(jaCheckForNullReturn());
    }

    @edu.umd.cs.findbugs.annotations.Nullable public FindBugsCheck fbNullableReturn() {
        return null;
    }
    public void fbNullableParm(@edu.umd.cs.findbugs.annotations.Nullable FindBugsCheck p) {
        p.test(); // (NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE) This parameter is always used in a way that requires it to be non-null, but the parameter is explicitly annotated as being Nullable.
    }
    public void fbTestNullable() {
        fbNullableReturn().test(); // isn't flagged

        fbNullableParm(this);
        fbNullableParm(null);
        
        fbNullableParm(noAnnotationReturn());
        fbNullableParm(fbNonnullReturn());
        fbNullableParm(fbNullableReturn());
        fbNullableParm(fbCheckForNullReturn());
    }


    // Test CheckForNull

    @javax.annotation.CheckForNull public FindBugsCheck jaCheckForNullReturn() {
        return null;
    }
    public void jaCheckForNullParm(@javax.annotation.CheckForNull FindBugsCheck p) {
        p.test(); // (NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE) This parameter is always used in a way that requires it to be non-null, but the parameter is explicitly annotated as being Nullable.
    }
    public void jaTestCheckForNull() {
        jaCheckForNullReturn().test(); // (NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE) Possible null pointer dereference in .. due to return value of called method

        jaCheckForNullParm(this);
        jaCheckForNullParm(null);
        
        jaCheckForNullParm(noAnnotationReturn());
        jaCheckForNullParm(jaNonnullReturn());
        jaCheckForNullParm(jaNullableReturn());
        jaCheckForNullParm(jaCheckForNullReturn());
    }
    
    @edu.umd.cs.findbugs.annotations.CheckForNull public FindBugsCheck fbCheckForNullReturn() {
        return null;
    }
    public void fbCheckForNullParm(@edu.umd.cs.findbugs.annotations.CheckForNull FindBugsCheck p) {
        p.test(); // (NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE) This parameter is always used in a way that requires it to be non-null, but the parameter is explicitly annotated as being Nullable.
    }
    public void fbTestCheckForNull() {
        fbCheckForNullReturn().test(); // (NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE) Possible null pointer dereference in .. due to return value of called method

        fbCheckForNullParm(this);
        fbCheckForNullParm(null);
        
        fbCheckForNullParm(noAnnotationReturn());
        fbCheckForNullParm(fbNonnullReturn());
        fbCheckForNullParm(fbNullableReturn());
        fbCheckForNullParm(fbCheckForNullReturn());
    }

    //end of commenting out file */

}