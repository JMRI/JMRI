package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public interface StartupActionModelUtil {
    @CheckForNull
    String getActionName(@Nonnull Class<?> clazz);

    @CheckForNull
    String getActionName(@Nonnull String className);

    boolean isSystemConnectionAction(@Nonnull Class<?> clazz);

    boolean isSystemConnectionAction(@Nonnull String className);

    @CheckForNull
    String getClassName(@CheckForNull String name);

    @CheckForNull
    String[] getNames();

    @Nonnull
    Class<?>[] getClasses();

    void addAction(@Nonnull String strClass, @Nonnull String name) throws ClassNotFoundException;

    void removeAction(@Nonnull String strClass) throws ClassNotFoundException;

    @CheckForNull
    String getOverride(@CheckForNull String name);

}
