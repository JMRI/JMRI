package jmri.util.junit.rules;

/**
 * Retries a failing test.
 * <ul>
 * <li>If a test passes on the first time, this rule does nothing; the test is marked as passed
 * <li>If a test fails at first, a warning is logged, and the test is retries up to "retryCount" times.
 *      A pass on any of those marks the test as passing.
 * <li>If the test fails all the retries, an error is logged, and the test is marked as failing.
 * </ul>
 *
 * <p>
 * based on code at
 * http://www.swtestacademy.com/rerun-failed-test-junit/
 *
 * @author ONUR BASKIRT 27.03.2016.
 */
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.junit.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryRule implements TestRule {
    private int retryCount;

    /** 
     * Configure the rule
     * @param retryCount The number of retries, i.e. "1" means a failed test will be tried one more time.
     */
    public RetryRule (int retryCount) {
        this.retryCount = retryCount;
        if (retryCount < 0) log.error("retryCount must be zero (no retries) or greater");
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable caughtThrowable = new Exception("Internal error in RetryRule");

                // implement retry logic here
                for (int i = 0; i <= retryCount; i++) {  // 0 is the 1st pass, 1..retryCount the retries
                    try {
                        base.evaluate();
                        return; // successful return
                    } catch (AssumptionViolatedException ave) {
                        // an assumption was violated, which is normal, so just re-throw ave instead of retrying
                        throw ave;
                    } catch (Throwable t) {
                        // this iteration of the test failed
                        caughtThrowable = t;
                        log.warn("{} : run  {} failed, RetryRule repeats",description.getDisplayName(), (i + 1));
                    }
                }
                log.error("{} : giving up after {} failures", description.getDisplayName(), retryCount+1);
                throw caughtThrowable;
            }
        };
    }

    private final static Logger log = LoggerFactory.getLogger(RetryRule.class);
}
