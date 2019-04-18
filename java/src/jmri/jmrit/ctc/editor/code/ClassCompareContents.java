package jmri.jmrit.ctc.editor.code;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 * The purpose of this class is to compare the CONTENTS of two
 * passed Objects (they are ASSUMED to be of the same class).  This routine
 * will use recursion and reflection to compare the most primitive types.
 * It does not return ordering information (though it could be modified to do
 * so), it only returns EXACTLY EQUAL contents.  Used primarily for GUI programs
 * to determine if what they save/restore to/from the disk has been changed.
 * 
 * Passing two different classes, though an error which won't throw an exception,
 * but instead will ALWAYS return false (i.e. not equal).
 * Passing either parameter as null ALWAYS returns false.
 * 
 * Presently, I support the following "types" of primitives:
 *  byte    (Byte)
 *  short   (Short)
 *  int     (Integer)
 *  long    (Long)
 *  float   (Float)
 *  double  (Double)
 *  boolean (Boolean)
 *  char    (Character)
 *  void    (Void)
 *  String  (String)
 *  enum    (Enum)
 * 
 * Supports the following also:
 * 
 * Native arrays of any type (including classes!)
 * Collections of any type (including classes!)
 * 
 */

public class ClassCompareContents {
    public static boolean objectsEqual(Object obj1, Object obj2) {
//  Basic safety checks:        
        if (obj1 == null || obj2 == null) return false;
        if (obj1.getClass() != obj2.getClass()) return false;
//  Ok, both obj1FieldArray safe and both same object type:
//  Lets find out if this is a primitive type, or another "nested" class:
        Class obj1Class = obj1.getClass();
        if (obj1Class == Byte.class) {
            return ((Byte)obj1).byteValue() == ((Byte)obj2).byteValue();
        } else if (obj1Class == Short.class) {
            return ((Short)obj1).shortValue() == ((Short)obj2).shortValue();
        } else if (obj1Class == Integer.class) {
            return ((Integer)obj1).intValue() == ((Integer)obj2).intValue();
        } else if (obj1Class == Long.class) {
            return ((Long)obj1).longValue() == ((Long)obj2).longValue();
        } else if (obj1Class == Float.class) {
            return ((Float)obj1).floatValue() == ((Float)obj2).floatValue();
        } else if (obj1Class == Double.class) {
            return ((Double)obj1).doubleValue() == ((Double)obj2).doubleValue();
        } else if (obj1Class == Boolean.class) {
            return ((Boolean)obj1).booleanValue() == ((Boolean)obj2).booleanValue();
        } else if (obj1Class == Character.class) {
            return ((Character)obj1).charValue() == ((Character)obj2).charValue();
        } else if (obj1Class == Void.class) {       // Voids are ALWAYS void, ergo equal!
            return true;
        } else if (obj1Class == String.class) {
            return ((String)obj1).equals(obj2);
        }
//  Must be another class or an enum:
        Field[] obj1Fields = obj1Class.getDeclaredFields();
        Field[] obj2Fields = obj2.getClass().getDeclaredFields();
        for (int fieldIndex = 0; fieldIndex < obj1Fields.length; fieldIndex++) {
            Object obj1Field;
            Object obj2Field;
            try {
                obj1Field = obj1Fields[fieldIndex].get(obj1);
                obj2Field = obj2Fields[fieldIndex].get(obj2);
            }
            catch (IllegalArgumentException | IllegalAccessException ex) {
                continue; // Skip this field.  Probably "serialVersionUID"
            }
//  enum is a special type of class, handle it special:            
            if (obj1Fields[fieldIndex].getType().isEnum()) {
                if (!obj1Field.toString().equals(obj2Field.toString())) {
                    return false;
                }
//  Check for any kind of array:
            } else if (obj1Fields[fieldIndex].getType().isArray()) {
                try {
                    Object[] obj1FieldArray = (Object[])obj1Fields[fieldIndex].get(obj1);
                    Object[] obj2FieldArray = (Object[])obj2Fields[fieldIndex].get(obj2);
                    if (obj1FieldArray.length != obj2FieldArray.length) return false;
//  RECURSE all values in the array:
                    for (int arrayIndex = 0; arrayIndex< obj1FieldArray.length; arrayIndex++) {
                        if (!objectsEqual(obj1FieldArray[arrayIndex], obj2FieldArray[arrayIndex])) return false;
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
//                  continue;   // Skip this field.
                }
//  Check for any kind of Collection:
//https://stackoverflow.com/questions/8423390/java-how-to-check-if-a-field-is-of-type-java-util-collection 
// Technically Collection<?> and Iterator<?> should be Collection<Object> and Iterator<Object>, but the compiler complains, even though we know it is of type Object!
            } else if (Collection.class.isAssignableFrom(obj1Fields[fieldIndex].getType())) {
                try {
                    Collection<?> obj1FieldCollection = (Collection<?>)obj1Fields[fieldIndex].get(obj1);
                    Collection<?> obj2FieldCollection = (Collection<?>)obj2Fields[fieldIndex].get(obj2);
                    if (obj1FieldCollection.size() != obj2FieldCollection.size()) return false;
                    Iterator<?> obj2FieldCollectionIterator = obj2FieldCollection.iterator();
                    for (Iterator<?> obj1FieldCollectionIterator = obj1FieldCollection.iterator(); obj1FieldCollectionIterator.hasNext();) {
                        if (!objectsEqual(obj1FieldCollectionIterator.next(), obj2FieldCollectionIterator.next())) return false;
                    }
                } catch (IllegalArgumentException | IllegalAccessException ex) {
//                  continue;   // Skip this field.
                }
            } else { // Standard class, reCURSE:
                if (!objectsEqual(obj1Field, obj2Field)) {
                    return false;
                }
            }
        }
        return true;    // If we get here, nothing compared "not equal", therefore equal.
    }
}
