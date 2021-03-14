package jmri.jmrit.beantable.beanedit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JPanel;

/**
 * Hold the information for each bean panel in a structured manner.
 * The Panel enabled status and Tool-tip are used in the Tab Selection Menu Titles.
 */
public class BeanItemPanel extends JPanel {

    public BeanItemPanel() {
        super();
    }

    public void saveItem() {
        if (save != null) {
            save.actionPerformed(null);
        }
    }

    public void resetField() {
        if (reset != null) {
            reset.actionPerformed(null);
        }
    }

    /**
     * Set the action to be performed when the Save button is pressed.
     *
     * @param save the new save action
     */
    public void setSaveItem(AbstractAction save) {
        this.save = save;
    }

    /**
     * Set the action to be performed when the Cancel button is pressed.
     *
     * @param reset the new reset action
     */
    public void setResetItem(AbstractAction reset) {
        this.reset = reset;
    }

    private AbstractAction save;
    private AbstractAction reset;

    private final ArrayList<BeanEditItem> items = new ArrayList<>();

    public void addItem(BeanEditItem bei) {
        items.add(bei);
    }

    /**
     * Get List of Bean Edit Items
     * @return unmodifiable List.
     */
    public List<BeanEditItem> getListOfItems() {
        return Collections.unmodifiableList(items);
    }

    private String name;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
