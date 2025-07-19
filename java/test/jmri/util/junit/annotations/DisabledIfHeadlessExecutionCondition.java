package jmri.util.junit.annotations;

import org.junit.jupiter.api.extension.*;

/**
 *
 * @author Steve Young Copyright 2024
 */
public class DisabledIfHeadlessExecutionCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled("Test enabled, not in headless mode");
    private static final ConditionEvaluationResult DISABLED = ConditionEvaluationResult.disabled("Test disabled, in headless mode");
    private static final boolean IS_HEADLESS = Boolean.parseBoolean(System.getProperty("java.awt.headless", "false"));

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return IS_HEADLESS ? DISABLED : ENABLED;
    }

}
