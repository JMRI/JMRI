package jmri.jmrit.display;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.ShutDownManager;
    import jmri.UserPreferencesManager;
import jmri.beans.Bean;
import jmri.implementation.AbstractShutDownTask;

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

    boolean panelSetChanged = false;

    public EditorManager() {
        super(false);
        setShutDownTask();
    }

    /**
     * Panel adds occur during xml data file loading and manual adds.
     * This sets the change flag for manual adds.
     * After a Store is complete, the flag is cleared.
     * @param flag The new value for the panelSetChanged boolean.
     */
    public void setChanged(boolean flag) {
        panelSetChanged = flag;
    }

    /**
     * Set the title for the Preferences / Messages tab.
     * Called by JmriUserPreferencesManager.
     * @return the title string.
     */
    public String getClassDescription() {
        return Bundle.getMessage("TitlePanelDialogs");  // NOI18N
    }

    /**
     * Set the details for Preferences / Messages tab.
     * Called by JmriUserPreferencesManager.
     * <p>
     * The dialogs are in jmri.configurexml.LoadXmlConfigAction and jmri.jmrit.display.Editor.
     * They are anchored here since the preferences system appears to need a class that can instantiated.
     */
    public void setMessagePreferencesDetails() {
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).setPreferenceItemDetails(
                "jmri.jmrit.display.EditorManager", "skipHideDialog", Bundle.getMessage("PanelHideSkip"));  // NOI18N
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).setPreferenceItemDetails(
                "jmri.jmrit.display.EditorManager", "skipDupLoadDialog", Bundle.getMessage("DuplicateLoadSkip"));  // NOI18N
    }

    public transient AbstractShutDownTask shutDownTask = null;
    public void setShutDownTask() {
        shutDownTask = new AbstractShutDownTask("EditorManager") {
            @Override
            public Boolean call() {
                if (panelSetChanged) {
                    notifyStoreNeeded();
                }
                return Boolean.TRUE;
            }

            @Override
            public void run() {
            }
        };
        InstanceManager.getDefault(ShutDownManager.class).register(shutDownTask);
        }

        String getClassName() {
        return EditorManager.class.getName();
    }

    /**
     * Prompt whether to invoke the Store process.
     * The options are "No" and "Yes".
     */
    void notifyStoreNeeded() {
        // Provide option to invoke the store process before the shutdown.
        final JDialog dialog = new JDialog();
        dialog.setTitle(Bundle.getMessage("QuestionTitle"));     // NOI18N
        dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        JPanel container = new JPanel();
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        JLabel question = new JLabel(Bundle.getMessage("EditorManagerQuitNotification"));  // NOI18N
        question.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(question);

        JButton noButton = new JButton(Bundle.getMessage("ButtonNo"));    // NOI18N
        JButton yesButton = new JButton(Bundle.getMessage("ButtonYes"));      // NOI18N
        JPanel button = new JPanel();
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.add(noButton);
        button.add(yesButton);
        container.add(button);

        noButton.addActionListener((ActionEvent e) -> {
            dialog.dispose();
            return;
        });

        yesButton.addActionListener((ActionEvent e) -> {
            dialog.setVisible(false);
            new jmri.configurexml.StoreXmlUserAction("").actionPerformed(null);
            dialog.dispose();
            return;
        });

        container.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.setAlignmentY(Component.CENTER_ALIGNMENT);
        dialog.getContentPane().add(container);
        dialog.pack();
        dialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - dialog.getWidth() / 2, (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - dialog.getHeight() / 2);
        dialog.setModal(true);
        dialog.setVisible(true);
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
     * Get the editor with the given title.
     *
     * @param title the title of the editor
     * @return the editor with the given title or null if no editor by that title
     * exists
     */
    @CheckForNull
    public Editor get(@Nonnull String title) {
        return getAll().stream().filter(e -> e.getTitle().equals(title)).findFirst().orElse(null);
    }

    /**
     * Get the editor with the given name.
     *
     * @param name the name of the editor
     * @return the editor with the given name or null if no editor by that name
     * exists
     */
    @CheckForNull
    public Editor getByName(@Nonnull String name) {
        return getAll().stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
    }

    /**
     * Get the editor with the given name or the editor with the given target frame name.
     *
     * @param name the name of the editor or target frame
     * @return the editor or null
     */
    @CheckForNull
    public Editor getTargetFrame(@Nonnull String name) {
        Editor editor = get(name);
        if (editor != null) {
            return editor;
        }
        return getAll().stream().filter(e -> e.getTargetFrame().getTitle().equals(name)).findFirst().orElse(null);
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
            panelSetChanged = true;
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
}
