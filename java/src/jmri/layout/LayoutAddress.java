package jmri.layout;

/**
 * Define an address consisting of a layout name, a type, and an offset number.
 *
 * @author Alex Shepherd Copyright (c) 2002
 * @deprecated 4.3.5
 */
@Deprecated
public class LayoutAddress implements Comparable<Object> {

    public static final int ELEMENT_TYPE_FIRST = 0;
    public static final int ELEMENT_TYPE_SENSOR = ELEMENT_TYPE_FIRST;
    public static final int ELEMENT_TYPE_TURNOUT = ELEMENT_TYPE_FIRST + 1;
    public static final int ELEMENT_TYPE_LOCO = ELEMENT_TYPE_FIRST + 2;
    public static final int ELEMENT_TYPE_MISC = ELEMENT_TYPE_FIRST + 3;
    public static final int ELEMENT_TYPE_LAST = ELEMENT_TYPE_MISC;

    private static String[] mTypeDescriptions = {"Sensor", "Turnout", "Loco", "Misc"};

    private String mLayoutName;
    private int mType;
    private int mOffset;

    /**
     * Cache the hash code for the string
     */
    private int hash = 0;

    LayoutAddress(String pLayoutName, int pType, int pOffset) {
        mLayoutName = pLayoutName;
        mType = pType;
        mOffset = pOffset;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EQ_COMPARETO_USE_OBJECT_EQUALS")
    // compareTo used for specific purpose, equals and hashCode not needed
    public int compareTo(Object o) {
        LayoutAddress vLayoutAddress = (LayoutAddress) o;

        int vResult = mLayoutName.compareTo(vLayoutAddress.mLayoutName);
        if (vResult != 0) {
            return vResult;
        }

        vResult = mType - vLayoutAddress.mType;
        if (vResult != 0) {
            return vResult;
        }

        return mOffset - vLayoutAddress.mOffset;
    }

    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }

    public String getLayoutName() {
        return mLayoutName;
    }

    public int getType() {
        return mType;
    }

    public String getTypeDescription() {
        return mTypeDescriptions[mType];
    }

    public static String getTypeDescription(int pType) {
        return mTypeDescriptions[pType];
    }

    public int getOffset() {
        return mOffset;
    }

    public String toString() {
        return mLayoutName + "." + getTypeDescription(mType) + "." + mOffset;
    }
}
