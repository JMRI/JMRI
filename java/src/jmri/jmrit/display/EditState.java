package jmri.jmrit.display;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * The state of one item on a panel, as recorded by {@link EditHistory}: the
 * values of the item's panel properties, read through its getters, which can
 * be written back through its setters.
 * <p>
 * The properties of each kind of item are listed once, in a table of
 * {@link Property} entries, in the order they must be written back. A state
 * writes back only the values that differ from the item's current values, so
 * a setter with side effects runs only when its value really changes.
 *
 * @param <T> the type of the item
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class EditState<T> {

    /**
     * Two coordinates or other measures closer than this are the same. Values
     * are written back through setters that may compute them again (for
     * example a point stored relative to a centre), which can change the last
     * bits of a double.
     */
    static final double TOLERANCE = 1.0E-6;

    private final T item;
    private final List<Property<T, ?>> properties;
    private final List<Object> values;

    /**
     * Record the current state of an item.
     *
     * @param item       the item
     * @param properties the item's properties, in the order to write them back
     */
    public EditState(@Nonnull T item, @Nonnull List<Property<T, ?>> properties) {
        this.item = item;
        this.properties = properties;
        List<Object> v = new ArrayList<>(properties.size());
        for (Property<T, ?> p : properties) {
            v.add(p.read(item));
        }
        this.values = Collections.unmodifiableList(v);
    }

    /**
     * @return the item this state belongs to
     */
    @Nonnull
    public T getItem() {
        return item;
    }

    /**
     * Compare with another state of the same item.
     *
     * @param other the other state
     * @return true if every property has the same value in both
     */
    public boolean sameAs(@CheckForNull EditState<?> other) {
        if (other == null || other.item != item || other.values.size() != values.size()) {
            return false;
        }
        for (int i = 0; i < values.size(); i++) {
            if (!sameValue(values.get(i), other.values.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Write this state back to the item: each property whose current value
     * differs from the recorded one is set, in table order.
     */
    public void restore() {
        for (int i = 0; i < properties.size(); i++) {
            properties.get(i).restore(item, values.get(i));
        }
    }

    /**
     * Compare two recorded values: numbers and points within
     * {@link #TOLERANCE}, lists element by element, everything else with
     * {@link Objects#equals}.
     *
     * @param a the first value
     * @param b the second value
     * @return true if they are the same
     */
    static boolean sameValue(@CheckForNull Object a, @CheckForNull Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if ((a instanceof Double || a instanceof Float) && (b instanceof Double || b instanceof Float)) {
            return Math.abs(((Number) a).doubleValue() - ((Number) b).doubleValue()) <= TOLERANCE;
        }
        if (a instanceof Point2D && b instanceof Point2D) {
            Point2D pa = (Point2D) a;
            Point2D pb = (Point2D) b;
            return Math.abs(pa.getX() - pb.getX()) <= TOLERANCE && Math.abs(pa.getY() - pb.getY()) <= TOLERANCE;
        }
        if (a instanceof List && b instanceof List) {
            List<?> la = (List<?>) a;
            List<?> lb = (List<?>) b;
            if (la.size() != lb.size()) {
                return false;
            }
            for (int i = 0; i < la.size(); i++) {
                if (!sameValue(la.get(i), lb.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return Objects.equals(a, b);
    }

    /**
     * Copy a point, so a recorded value can't change with the item.
     *
     * @param p the point, or null
     * @return a copy, or null
     */
    @CheckForNull
    public static Point2D copy(@CheckForNull Point2D p) {
        return (p == null) ? null : new Point2D.Double(p.getX(), p.getY());
    }

    /**
     * Copy a list of points.
     *
     * @param list the points
     * @return an unmodifiable copy
     */
    @Nonnull
    public static List<Point2D> copyPoints(@Nonnull List<? extends Point2D> list) {
        List<Point2D> result = new ArrayList<>(list.size());
        for (Point2D p : list) {
            result.add(copy(p));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Copy a map of strings.
     *
     * @param map the map, or null
     * @return an unmodifiable copy, or null
     */
    @CheckForNull
    public static Map<String, String> copyMap(@CheckForNull Map<String, String> map) {
        return (map == null) ? null : Collections.unmodifiableMap(new HashMap<>(map));
    }

    /**
     * One panel property of an item: how to read it and how to set it.
     *
     * @param <T> the type of the item
     * @param <V> the type of the value
     */
    public static final class Property<T, V> {

        private final Function<T, V> getter;
        private final BiConsumer<T, V> setter;
        private final UnaryOperator<V> copier;

        private Property(Function<T, V> getter, BiConsumer<T, V> setter, UnaryOperator<V> copier) {
            this.getter = getter;
            this.setter = setter;
            this.copier = copier;
        }

        /**
         * A property whose values are immutable (numbers, strings, enums,
         * colours, objects held by reference).
         *
         * @param <T>    the type of the item
         * @param <V>    the type of the value
         * @param getter reads the value
         * @param setter sets the value
         * @return the property
         */
        public static <T, V> Property<T, V> of(@Nonnull Function<T, V> getter, @Nonnull BiConsumer<T, V> setter) {
            return new Property<>(getter, setter, UnaryOperator.identity());
        }

        /**
         * An int property.
         *
         * @param <T>    the type of the item
         * @param getter reads the value
         * @param setter sets the value
         * @return the property
         */
        public static <T> Property<T, Integer> ofInt(@Nonnull ToIntFunction<T> getter,
                @Nonnull ObjIntConsumer<T> setter) {
            return new Property<>(t -> Integer.valueOf(getter.applyAsInt(t)),
                    (t, v) -> setter.accept(t, v.intValue()), UnaryOperator.identity());
        }

        /**
         * A double property.
         *
         * @param <T>    the type of the item
         * @param getter reads the value
         * @param setter sets the value
         * @return the property
         */
        public static <T> Property<T, Double> ofDouble(@Nonnull ToDoubleFunction<T> getter,
                @Nonnull ObjDoubleConsumer<T> setter) {
            return new Property<>(t -> Double.valueOf(getter.applyAsDouble(t)),
                    (t, v) -> setter.accept(t, v.doubleValue()), UnaryOperator.identity());
        }

        /**
         * A boolean property.
         *
         * @param <T>    the type of the item
         * @param getter reads the value
         * @param setter sets the value
         * @return the property
         */
        public static <T> Property<T, Boolean> ofBoolean(@Nonnull Predicate<T> getter,
                @Nonnull BooleanSetter<T> setter) {
            return new Property<>(t -> Boolean.valueOf(getter.test(t)),
                    (t, v) -> setter.set(t, v.booleanValue()), UnaryOperator.identity());
        }

        /**
         * Sets a boolean property of an item.
         *
         * @param <T> the type of the item
         */
        @FunctionalInterface
        public interface BooleanSetter<T> {

            /**
             * @param item  the item
             * @param value the value to set
             */
            void set(T item, boolean value);
        }

        /**
         * A property whose values are mutable, recorded as copies.
         *
         * @param <T>    the type of the item
         * @param <V>    the type of the value
         * @param getter reads the value
         * @param setter sets the value
         * @param copier copies a value
         * @return the property
         */
        public static <T, V> Property<T, V> copied(@Nonnull Function<T, V> getter, @Nonnull BiConsumer<T, V> setter,
                @Nonnull UnaryOperator<V> copier) {
            return new Property<>(getter, setter, copier);
        }

        /**
         * A point property, recorded as a copy.
         *
         * @param <T>    the type of the item
         * @param getter reads the point
         * @param setter sets the point
         * @return the property
         */
        public static <T> Property<T, Point2D> point(@Nonnull Function<T, Point2D> getter,
                @Nonnull BiConsumer<T, Point2D> setter) {
            return new Property<>(getter, setter, EditState::copy);
        }

        Object read(T item) {
            return copier.apply(getter.apply(item));
        }

        @SuppressWarnings("unchecked") // values were read by this property's own getter
        void restore(T item, Object value) {
            if (!sameValue(getter.apply(item), value)) {
                setter.accept(item, copier.apply((V) value));
            }
        }
    }
}
