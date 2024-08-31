package jmri.util.junit.annotations;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Disables JUnit5 Tests with the @NotApplicable annotation.
 * @author Steve Young Copyright 2024
 */
public class NotApplicableExecutionCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED_BY_DEFAULT =
        ConditionEvaluationResult.enabled("@NotApplicable is not present");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return context.getElement()
            .map(el -> el.getAnnotation(NotApplicable.class))
            .map(disabled -> ConditionEvaluationResult.disabled("Test is Not Applicable: " + disabled.value()))
            .orElse(ENABLED_BY_DEFAULT);
    }

}
