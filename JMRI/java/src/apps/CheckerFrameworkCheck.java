package apps;

/**
 * Check how the Checker Framework and annotations interact.
 * <p>
 * Note: This deliberately causes SpotBugs (formally FindBugs) warnings! Do not
 * remove or annotate them! Instead, when past its useful point, just comment
 * out the body of the class so as to leave the code examples present.
 * <p>
 * Tests Nonnull, Nullable and CheckForNull from the javax.annotation package.
 * <p>
 * Comments indicate observed (and unobserved) warnings from the Checker
 * Framework nullness processor
 * <p>
 * This has no main() because it's not expected to run: It will certainly throw
 * a NullPointerException right away. The idea is for the CheckerFramework to
 * find those in static analysis
 * <p>
 * Types are explicitly qualified (instead of using 'import') to make it
 * completely clear which is being used at each point. That makes this the code
 * less readable, so it's not recommended for general use.
 * <p>
 * @see apps.FindBugsCheck
 * @author Bob Jacobsen 2016
 */
public class CheckerFrameworkCheck {

    void test() { // something that has to be executed on an object
        System.out.println("test " + this.getClass());
    }

    /*  commenting out the rest of the file to avoid SpotBugs counting the deliberate warnings


    public CheckerFrameworkCheck noAnnotationReturn() {
        return null;                                // error: [return.type.incompatible] incompatible types in return. required: @Initialized @NonNull CheckerFrameworkCheck
    }
    public void noAnnotationParm(CheckerFrameworkCheck p) {
        p.test();
    }
    public void noAnnotationTest() {
        noAnnotationReturn().test();

        noAnnotationParm(this);
        noAnnotationParm(null);                     // error: [argument.type.incompatible] incompatible types in argument. required: @Initialized @NonNull CheckerFrameworkCheck

        noAnnotationParm(noAnnotationReturn());
        noAnnotationParm(jaNonnullReturn());
        noAnnotationParm(jaNullableReturn());       // error: [argument.type.incompatible] incompatible types in argument. required: @Initialized @NonNull CheckerFrameworkCheck
        noAnnotationParm(jaCheckForNullReturn());   // error: [argument.type.incompatible] incompatible types in argument. required: @Initialized @NonNull CheckerFrameworkCheck
    }

    // Test Nonnull

    Nonnull public CheckerFrameworkCheck jaNonnullReturn() {
        return null;                                // error: [return.type.incompatible] incompatible types in return. required: @Initialized @NonNull CheckerFrameworkCheck
    }
    public void jaNonNullParm(Nonnull CheckerFrameworkCheck p) {
        p.test();
    }
    public void jaTestNonnull() {
        jaNonnullReturn().test();

        jaNonNullParm(this);
        jaNonNullParm(null);                        // error: [argument.type.incompatible] incompatible types in argument. required: @Initialized @NonNull CheckerFrameworkCheck

        jaNonNullParm(noAnnotationReturn());
        jaNonNullParm(jaNonnullReturn());
        jaNonNullParm(jaNullableReturn());          // error: [argument.type.incompatible] incompatible types in argument. required: @Initialized @NonNull CheckerFrameworkCheck
        jaNonNullParm(jaCheckForNullReturn());      // error: [argument.type.incompatible] incompatible types in argument. required: @Initialized @NonNull CheckerFrameworkCheck
    }

    // Test Nullable

    Nullable public CheckerFrameworkCheck jaNullableReturn() {
        return null;
    }
    public void jaNullableParm(Nullable CheckerFrameworkCheck p) {
        p.test();                                   // error: [dereference.of.nullable] dereference of possibly-null reference p
    }
    public void jaTestNullable() {
        jaNullableReturn().test();                  // error: [dereference.of.nullable] dereference of possibly-null reference jaNullableReturn()

        jaNullableParm(this);
        jaNullableParm(null);

        jaNullableParm(noAnnotationReturn());
        jaNullableParm(jaNonnullReturn());
        jaNullableParm(jaNullableReturn());
        jaNullableParm(jaCheckForNullReturn());
    }

    // Test CheckForNull

    CheckForNull public CheckerFrameworkCheck jaCheckForNullReturn() {
        return null;
    }
    public void jaCheckForNullParm(CheckForNull CheckerFrameworkCheck p) {
        p.test();                                   // error: [dereference.of.nullable] dereference of possibly-null reference p
    }
    public void jaTestCheckForNull() {
        jaCheckForNullReturn().test();              // error: [dereference.of.nullable] dereference of possibly-null reference jaNullableReturn()

        jaCheckForNullParm(this);
        jaCheckForNullParm(null);

        jaCheckForNullParm(noAnnotationReturn());
        jaCheckForNullParm(jaNonnullReturn());
        jaCheckForNullParm(jaNullableReturn());
        jaCheckForNullParm(jaCheckForNullReturn());
    }

    end of commenting out file */
}
