package jmri.implementation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by bracz on 12/4/18.
 */

class LogixRecursionException extends RuntimeException {
    // Stores the this reference to the object which detected the recursion and first thrown the
    // exception.
    private final Object triggerSource;
    private boolean isCollectingStack = true;
    private LinkedList<String> description = new LinkedList<>();

    public LogixRecursionException(Object triggerSource, String explanationForUser) {
        this.triggerSource = triggerSource;
        this.description.add(explanationForUser);
    }

    /**
     * Add some explanation for the user on why this recursion has happened. Calling this
     * repeatedly will prepend the explanation, as it is expected to be called as the stack frame
     * is unwound.
     * @param explanationForUser text that describes the current stack frame; will be printed for
     *                          the user.
     */
    public void prependDescription(String explanationForUser) {
        description.addFirst(explanationForUser);
    }

    /**
     * @return The full user-visible explanation of the recursion stack.
     */
    public String getStackExplanation() {
        return String.join(" -> ", description);
    }

    /**
     * @return object that detected the recursion at first.
     */
    public Object getTriggerSource() {
        return triggerSource;
    }

    /**
     * @return true if we are still collecting information about the stack for the user.
     */
    public boolean isCollectingStack() {
        return this.isCollectingStack;
    }

    public void stopCollectingStack() {
        isCollectingStack = false;
    }

    @Override
    public String toString() {
        return "Logix encountered recursive loop: " + getStackExplanation() + "\n" + super
                .toString();
    }
}
