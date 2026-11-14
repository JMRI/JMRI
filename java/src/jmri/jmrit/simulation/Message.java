package jmri.jmrit.simulation;

import java.util.Arrays;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A message from the package Bundle, kept as its key and arguments so it can
 * be shown to the user in their language and logged in English, as JMRI logs
 * are.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@Immutable
final class Message {

    private final String key;
    private final Object[] args;

    /**
     * @param key  the Bundle key
     * @param args its arguments
     */
    Message(@Nonnull String key, Object... args) {
        this.key = key;
        this.args = args.clone();
    }

    /**
     * @return the text in the user's language
     */
    @Nonnull
    String text() {
        return Bundle.getMessage(key, args.clone());
    }

    /**
     * @return the text in English, for the log
     */
    @Nonnull
    String english() {
        return Bundle.getMessage(Locale.ENGLISH, key, args.clone());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Message && ((Message) o).key.equals(key) && Arrays.equals(((Message) o).args, args);
    }

    @Override
    public int hashCode() {
        return 31 * key.hashCode() + Arrays.hashCode(args);
    }

    @Override
    public String toString() {
        return english();
    }
}
