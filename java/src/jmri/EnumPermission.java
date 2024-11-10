package jmri;

import javax.annotation.Nonnull;

/**
 * Defines a permission based on an enum.
 *
 * @author Daniel Bergqvist (C) 2024
 * @param <E> the enum for this permission
 */
public interface EnumPermission<E extends Enum<?>> extends Permission {

    /**
     * Get the values of the enum.
     * @return an array of the enums
     */
    @Nonnull
    E[] getValues();

}
