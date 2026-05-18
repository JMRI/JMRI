package jmri.jmrix.dccpp;

/**
 * Represents a single DCC-EX EXRAIL Route or Automation entry from a {@code <jA>} reply.
 * <p>
 * State and caption may be updated later via {@code <jB>} replies.
 *
 * @author Chad Francis Copyright (C) 2026
 */
public class DCCppExrailEntry {

    private final int id;
    private final String type;
    private final String description;
    private String caption;
    private int state = -1; // -1 = unknown until first <jB> state reply

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

    /** State from {@code <jB id state>}; -1 if not yet received. */
    public int getState() { return state; }

    public void setState(int state) { this.state = state; }
}
