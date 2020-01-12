package jmri.jmrit.display;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import jmri.InstanceManagerAutoDefault;

public class EditorManager implements InstanceManagerAutoDefault {

    private final SortedSet<Editor> editors = new TreeSet<>(new Comparator<Editor>() {

        @Override
        public int compare(Editor o1, Editor o2) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    });

    /**
     * Get a List of the currently-existing Editor objects. The returned list is
     * a copy made at the time of the call, so it can be manipulated as needed
     * by the caller.
     *
     * @return a List of Editors
     */
    synchronized public List<Editor> getEditorsList() {
        return new ArrayList<>(this.editors);
    }

    /**
     * Get a list of currently-existing Editor objects that are specific
     * sub-classes of Editor.
     * <p>
     * The returned list is a copy made at the time of the call, so it can be
     * manipulated as needed by the caller.
     *
     * @param type the Class the list should be limited to.
     * @return a List of Editors.
     */
    @SuppressWarnings("unchecked")
    synchronized public <T extends Editor> List<T> getEditorsList(@Nonnull Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Editor e : this.getEditorsList()) {
            if (type.isInstance(e)) {
                result.add((T) e);
            }
        }
        return result;
    }

    /**
     * Get an Editor of a particular name. If more than one exists, there's no
     * guarantee as to which is returned.
     *
     * @param name the editor to get
     * @return the first matching Editor or null if no matching Editor could be
     *         found
     */
    public Editor getEditor(String name) {
        for (Editor e : this.getEditorsList()) {
            if (e.getTitle().equals(name)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Add an Editor to the set of Editors. This only changes the set of Editors
     * if the Editor is not already in the set.
     *
     * @param editor the editor to add
     * @return true if the set was changed; false otherwise
     */
    public boolean addEditor(@Nonnull Editor editor) {
        return this.editors.add(editor);
    }

    /**
     * Add an Editor from the set of Editors. This only changes the set of
     * Editors if the Editor is in the set.
     *
     * @param editor the editor to remove
     * @return true if the set was changed; false otherwise
     */
    public boolean removeEditor(@Nonnull Editor editor) {
        return this.editors.remove(editor);
    }
}
