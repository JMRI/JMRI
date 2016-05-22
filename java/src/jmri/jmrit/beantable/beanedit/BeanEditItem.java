package jmri.jmrit.beantable.beanedit;

import javax.swing.JComponent;

/**
 * Hold the information for each bean panel in a structured manner.
 */
public class BeanEditItem {

    String help;
    String description;
    JComponent component;

    /**
     * Create the item structure to be added. If the component and description
     * are null, then the help text will be displayed across the width of the
     * panel.
     *
     * @param component Optional Contains the item to be edited
     * @param description Optional Contains the text for the label that will be
     * to the left of the component
     * @param help Optional Contains the help or hint text, that will be
     * displayed to the right of the component
     */
    public BeanEditItem(JComponent component, String description, String help) {
        this.component = component;
        this.description = description;
        this.help = help;
    }

    public String getDescription() {
        return description;
    }

    public String getHelp() {
        return help;
    }

    public JComponent getComponent() {
        return component;
    }
}