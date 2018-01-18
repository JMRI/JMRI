package jmri.util.junit.rules;

/**
 * <P>based on code at
 * http://www.swtestacademy.com/rerun-failed-test-junit/
 * </P>
 *
 * @author ONUR BASKIRT 27.03.2016.
 */
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryRule implements TestRule {
    private int retryCount;

    public RetryRule (int retryCount) {
        this.retryCount = retryCount;
    }

    public Statement apply(Statement base, Description description) {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable caughtThrowable = null;

                // implement retry logic here
                for (int i = 0; i < retryCount; i++) {
                    try {
                        base.evaluate();
                        return;
                    } catch (Throwable t) {
                        caughtThrowable = t;
                        log.info("{} : run  {} failed",description.getDisplayName(), (i + 1));
                    }
                }
                log.error("{} : giving up after {} failures",description.getDisplayName(), retryCount);
                throw caughtThrowable;
            }
        };
    }

    private final static Logger log = LoggerFactory.getLogger(RetryRule.class);
}
