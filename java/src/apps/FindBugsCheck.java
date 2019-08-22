package apps;

/**
 * Check how SpotBugs (formally FindBugs) and annotations interact.
 * <p>
 * Note: This deliberately causes SpotBugs warnings!  Do not remove or annotate them!
 *       Instead, when past its useful point, just comment out the body of the
 *       class so as to leave the code examples present.
 * <p>
 * Tests Nonnull, Nullable and CheckForNull from 
 * javax.annotation annotations.
 * <p>
 * This has no main() because it's not expected to run:  Many methods will certainly
 * throw a NullPointerException right away.  The idea is for SpotBugs to
 * find those in static analysis. This is in java/src, instead of java/test,
 * so that our usual CI infrastructure builds it.
 * <p>
 * Annotations are explicitly qualified (instead of using 'import') to make it
 * completely clear which is being used at each point.  That makes this the
 * code less readable, so it's not recommended for general use.
 * <p>
 * The "ja" prefix means that javax annotations are used. "no" means un-annotated.
 * <p>
 * A previous version (Git SHA 4049c5d690) also had "fb" as a prefix 
 * for using the edu.umd.cs.findbugs.annotations form of annotations.
 * There were found to work exactly the same as the (preferred) javax.annotation forms,
 * including when intermixed with each other.
 * <p>
 * The comments are the warnings thrown (and not thrown) by SpotBugs 3.1.9
 * <p>
 * Summary: <ul>
 * <li>Parameter declaration handling:
 *   <ul>
 *   <li>@Nonnull means that references are assumed OK
 *   <li>Both @CheckForNull and @Nullable are checked for dereferences
 *   <li>Parameters with no annotation are not checked (i.e. acts like @Nonnull, no null checks required before dereferencing)
 *   </ul>
 * <li>Passing explicit null parameters
 *   <ul>
 *   <li>@Nonnull will flag a passed null
 *   <li>@CheckForNull and @Nullable accept a passed null (but previously flagged any dereferences in the method declaration, i.e. that the annotation wasn't OK)
 *   <li>No annotation results in a NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS warning from analyzing the body of the method, effectively working like @Nonnull with a different error
 *   </ul>
 * <li>Parameter passing of return values isn't always checked by SpotBugs.
 *      For example, a @CheckForNull return value is accepted for an @Nonnull parameter.
 *      Perhaps this will improve with time.
 * <li>Return values are properly checked for @CheckForNull, but not for @Nullable.
 *   <ul>
 *   <li>A @CheckForNull return value is flagged if it's dereferenced.
 *   <li>A @Nullable return value is <u>not</u> flagged if it's dereferenced.
 *   <li>Return values without annotation are also not flagged when dereferenced.
 *   </ul>
 * </ul>
 * Bottom line:  When flagging return values, use @CheckForNull.  
 * @see apps.CheckerFrameworkCheck
 * @author Bob Jacobsen 2016, 2019
 */
public class FindBugsCheck {

    void test() { // something that has to be executed on an object
        System.out.println("test "+this.getClass());
    }

 /* //  comment out the rest of the file to avoid SpotBugs counting these deliberate warnings

    // Test no annotations 
    
    public FindBugsCheck noAnnotationReturn() {
        return null;
    }
    public void noAnnotationParm(FindBugsCheck p) {
        p.test();
    }
    public void noAnnotationTest() {
        FindBugsCheck p;

        noAnnotationReturn().test();
        p = noAnnotationReturn();
        p.test();

        noAnnotationParm(this);
        noAnnotationParm(null); // Null passed for non-null parameter NP_NULL_PARAM_DEREF_ALL_TARGETS_DANGEROUS
        
        noAnnotationParm(noAnnotationReturn()); // maybe should be flagged?
        p = noAnnotationReturn();
        noAnnotationParm(p);
        
        noAnnotationParm(jaNonnullReturn());
        p = jaNonnullReturn();
        noAnnotationParm(p);

        noAnnotationParm(jaNullableReturn()); // maybe should be flagged?
        p = jaNullableReturn();
        noAnnotationParm(p);

        noAnnotationParm(jaCheckForNullReturn()); // maybe should be flagged?
        p = jaCheckForNullReturn();
        noAnnotationParm(p);
    }

    // Test Nonnull

    @javax.annotation.Nonnull public FindBugsCheck jaNonnullReturn() {
        return null; // may return null, but is declared @Nonnull NP_NONNULL_RETURN_VIOLATION
    }
    public void jaNonNullParm(@javax.annotation.Nonnull FindBugsCheck p) {
        p.test();
    }
    public void jaTestNonnull() {
        FindBugsCheck p;

        jaNonnullReturn().test();
        p = jaNonnullReturn();
        if (p!=null) p.test(); // Redundant nullcheck of p, which is known to be non-null RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE

        jaNonNullParm(this);
        jaNonNullParm(null); // Null passed for non-null parameter NP_NONNULL_PARAM_VIOLATION
        
        jaNonNullParm(noAnnotationReturn()); // should be flagged
        p = noAnnotationReturn();
        jaNonNullParm(p);

        jaNonNullParm(jaNonnullReturn());
        p = jaNonnullReturn();
        jaNonNullParm(p);

        jaNonNullParm(jaNullableReturn()); // should be flagged
        p = jaNullableReturn();
        jaNonNullParm(p);

        jaNonNullParm(jaCheckForNullReturn()); // should be flagged
        p = jaCheckForNullReturn();
        jaNonNullParm(p);

    }

    // Test Nullable

    @javax.annotation.Nullable public FindBugsCheck jaNullableReturn() {
        return null;
    }
    public void jaNullableParm(@javax.annotation.Nullable FindBugsCheck p) {
        p.test(); // p must be non-null but is marked as nullable NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE
    }
    public void jaTestNullable() {
        FindBugsCheck p;

        jaNullableReturn().test(); // should be flagged
        p = jaNullableReturn();
        if (p!=null) p.test();

        jaNullableParm(this);
        jaNullableParm(null);

        jaNullableParm(noAnnotationReturn());
        p = noAnnotationReturn();
        jaNullableParm(p);

        jaNullableParm(jaNonnullReturn());
        p = jaNonnullReturn();
        jaNullableParm(p);

        jaNullableParm(jaNullableReturn());
        p = jaNullableReturn();
        jaNullableParm(p);

        jaNullableParm(jaCheckForNullReturn());
        p = jaCheckForNullReturn();
        jaNullableParm(p);

    }

    // Test CheckForNull

    @javax.annotation.CheckForNull public FindBugsCheck jaCheckForNullReturn() {
        return null;
    }
    public void jaCheckForNullParm(@javax.annotation.CheckForNull FindBugsCheck p) {
        p.test(); // p must be non-null but is marked as nullable NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE
    }
    public void jaTestCheckForNull() {
        FindBugsCheck p;

        jaCheckForNullReturn().test(); // Possible null pointer dereference NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE
        p = jaCheckForNullReturn(); 
        if (p!=null) p.test();

        jaCheckForNullParm(this);
        jaCheckForNullParm(null);

        jaCheckForNullParm(noAnnotationReturn());
        p = noAnnotationReturn();
        jaCheckForNullParm(p);
        
        jaCheckForNullParm(jaNonnullReturn());
        p = jaNonnullReturn();
        jaCheckForNullParm(p);
        
        jaCheckForNullParm(jaNullableReturn());
        p = jaNullableReturn();
        jaCheckForNullParm(p);
        
        jaCheckForNullParm(jaCheckForNullReturn());
        p = jaCheckForNullReturn();
        jaCheckForNullParm(p);
    }

 */ // end of commenting out file

}