package jmri.jmrit.display;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManagerAutoDefault;
import jmri.beans.Bean;

/**
 * Manager for JMRI Editors. This manager tracks editors, extending the Set
 * interface to do so (so it can be interacted with as a normal set), while also
 * providing some methods specific to editors.
 * <p>
 * This manager listens to the {@code title} property of Editors to be notified
 * to changes to the title of the Editor that could affect the order of editors.
 * <p>
 * This manager generates an {@link java.beans.IndexedPropertyChangeEvent} for
 * the property named {@code editors} when an editor is added or removed and
 * forwards the {@link java.beans.PropertyChangeEvent} for the {@code title}
 * property of Editors in the manager.
 *
 * @author Randall Wood Copyright 2020
 */
public class EditorManager extends Bean implements PropertyChangeListener, InstanceManagerAutoDefault {

    public static final String EDITORS = "editors";
    public static final String TITLE = "title";
    private final SortedSet<Editor> set = Collections.synchronizedSortedSet(new TreeSet<>(Comparator.comparing(Editor::getTitle)));

    public EditorManager() {
        super(false);
    }

    /**
     * Add an editor to this manager.
     *
     * @param editor the editor to add
     */
    public void add(@Nonnull Editor editor) {
        boolean result = set.add(editor);
        if (result) {
            fireIndexedPropertyChange(EDITORS, set.size(), null, editor);
            editor.addPropertyChangeListener(TITLE, this);
        }
    }

    /**
     * Check if an editor is in the manager.
     *
     * @param editor the editor to check for
     * @return true if this manager contains an editor with name; false
     * otherwise
     */
    public boolean contains(@Nonnull Editor editor) {
        return set.contains(editor);
    }

    /**
     * Get all managed editors. This set is sorted by the title of the editor.
     *
     * @return the set of all editors
     */
    @Nonnull
    public SortedSet<Editor> getAll() {
        return new TreeSet<>(set);
    }

    /**
     * Get all managed editors that implement the specified type. This set is
     * sorted by the title of the editor.
     *
     * @param <T> the specified type
     * @param type the specified type
     * @return the set of all editors of the specified type
     */
    @Nonnull
    public <T extends Editor> SortedSet<T> getAll(@Nonnull Class<T> type) {
        return set.stream()
                .filter(e -> type.isAssignableFrom(e.getClass()))
                .map(type::cast)
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Editor::getTitle))));
    }

    /**
     * Get the editor with the given name.
     *
     * @param name the name of the editor
     * @return the editor with the given name or null if no editor by that name
     * exists
     */
    @CheckForNull
    public Editor get(@Nonnull String name) {
        return getAll().stream().filter(e -> e.getTitle().equals(name)).findFirst().orElse(null);
    }

    /**
     * Get the editor with the given name and type.
     *
     * @param <T> the type of the editor
     * @param type the type of the editor
     * @param name the name of the editor
     * @return the editor with the given name or null if no editor by that name
     * exists
     */
    @CheckForNull
    public <T extends Editor> T get(@Nonnull Class<T> type, @Nonnull String name) {
        return type.cast(set.stream()
                .filter(e -> e.getClass().isAssignableFrom(type) && e.getTitle().equals(name))
                .findFirst().orElse(null));
    }

    /**
     * Remove an editor from this manager.
     *
     * @param editor the editor to remove
     */
    public void remove(@Nonnull Editor editor) {
        boolean result = set.remove(editor);
        if (result) {
            fireIndexedPropertyChange(EDITORS, set.size(), editor, null);
            editor.removePropertyChangeListener(TITLE, this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Editor) {
            Editor editor = (Editor) evt.getSource();
            if (contains(editor) && TITLE.equals(evt.getPropertyName())) {
                set.remove(editor);
                set.add(editor);
                firePropertyChange(evt);
            }
        }
    }

    /**
     * Check if an editor with the specified name is in the manager.
     *
     * @param name the name to check for
     * @return true if this manager contains an editor with name; false
     * otherwise
     */
    public boolean contains(String name) {
        return get(name) != null;
    }

    /**
     * Get the set of all Editors as a List. This is a convenience method for
     * use in scripts.
     *
     * @return the set of all Editors
     */
    @Nonnull
    public List<Editor> getList() {
        return new ArrayList<>(getAll());
    }

    /**
     * Get the set of all editors that implement the specified type. This is a
     * convenience method for use in scripts.
     *
     * @param <T> the specified type
     * @param type the specified type
     * @return the set of all editors that implement the specified type
     */
    @Nonnull
    public <T extends Editor> List<T> getList(@Nonnull Class<T> type) {
        return new ArrayList<>(getAll(type));
    }

    /**
     * Get a List of the currently-existing Editor objects. The returned list is
     * a copy made at the time of the call, so it can be manipulated as needed
     * by the caller.
     *
     * @return a List of Editors
     * @deprecated since 4.19.6; use {@link #getAll()} instead
     */
    @Deprecated
    synchronized public List<Editor> getEditorsList() {
        return new ArrayList<>(getAll());
    }

    /**
     * Get a list of currently-existing Editor objects that are specific
     * sub-classes of Editor.
     * <p>
     * The returned list is a copy made at the time of the call, so it can be
     * manipulated as needed by the caller.
     *
     * @param <T> the type of Editor
     * @param type the Class the list should be limited to.
     * @return a List of Editors.
     * @deprecated since 4.19.6; use {@link #getAll(java.lang.Class)} instead
     */
    @Deprecated
    public <T extends Editor> List<T> getEditorsList(@Nonnull Class<T> type) {
        return new ArrayList<>(getAll(type));
    }

    /**
     * Get an Editor of a particular name. If more than one exists, there's no
     * guarantee as to which is returned.
     *
     * @param name the editor to get
     * @return the first matching Editor or null if no matching Editor could be
     * found
     * @deprecated since 4.19.6; use {@link #get(java.lang.String)} instead
     */
    @Deprecated
    public Editor getEditor(String name) {
        return get(name);
    }

    /**
     * Add an Editor to the set of Editors. This only changes the set of Editors
     * if the Editor is not already in the set.
     *
     * @param editor the editor to add
     * @return true if the set was changed; false otherwise
     * @deprecated since 4.19.6; use {@link #add(jmri.jmrit.display.Editor)}
     * instead
     */
    @Deprecated
    public boolean addEditor(@Nonnull Editor editor) {
        return set.add(editor);
    }

    /**
     * Add an Editor from the set of Editors. This only changes the set of
     * Editors if the Editor is in the set.
     *
     * @param editor the editor to remove
     * @return true if the set was changed; false otherwise
     * @deprecated since 4.19.6; use {@link #remove(jmri.jmrit.display.Editor)}
     * instead
     */
    @Deprecated
    public boolean removeEditor(@Nonnull Editor editor) {
        return set.remove(editor);
    }
}
