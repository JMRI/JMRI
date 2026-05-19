package jmri.jmrix.dccpp;

/**
 * A single DCC-EX EXRAIL Route or Automation entry; state and caption updated via {@code <jB>} replies.
 *
 * @author Chad Francis Copyright (C) 2026
 */
public class DCCppExrailEntry {

    /** EXRAIL state values from {@code <jB id state>} replies. */
    public enum State {
        INACTIVE(0), ACTIVE(1), HIDDEN(2), DISABLED(4);

        public final int value;
        State(int value) { this.value = value; }

        /** Returns the matching State, or null if the value is unrecognised. */
        public static State fromValue(int value) {
            for (State s : values()) {
                if (s.value == value) return s;
            }
            return null;
        }
    }

    private final int id;
    private final String type;
    private final String description;
    private String caption;
    private State state = null; // null = unknown until first <jB> state reply

    public DCCppExrailEntry(int id, String type, String description) {
        this.id = id;
        this.type = type;
        this.description = description;
    }

    public int getId() { return id; }

    public String getType() { return type; }

    public String getDescription() { return description; }

    public boolean isRoute() { return "R".equals(type); }

    public boolean isAutomation() { return "A".equals(type); }

    /** Caption set via {@code <jB id "caption">}, or null if not yet received. */
    public String getCaption() { return caption; }

    public void setCaption(String caption) { this.caption = caption; }

    /** Returns caption if set, otherwise description. */
    public String getDisplayName() {
        return caption != null ? caption : description;
    }

    /** State from {@code <jB id state>}; null if not yet received. */
    public State getState() { return state; }

    public void setState(State state) { this.state = state; }
}
