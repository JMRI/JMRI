package jmri.jmrit.display;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import jmri.spi.JmriServiceProviderInterface;

/**
 * Factory for creating {@link AbstractAction}s that create new {@link Editor}s.
 *
 * @author Randall Wood Copyright 2020
 */
public interface EditorActionFactory extends JmriServiceProviderInterface {

    /**
     * Create a new action that should create a new {@link Editor}.
     *
     * @return the action
     */
    @Nonnull
    public AbstractAction createAction();

    /**
     * Get the title used for the action returned by {@link #createAction()}.
     * 
     * @return the title
     */
    @Nonnull
    public String getTitle();
}
