package jmri;

import java.lang.annotation.*;

/**
 * Annotation denoting that specific code was auto-generated
 * <p>
 * This is used in preference to javax.annotation.Generated and
 * javax.annotation.processing.Generated because the first of those
 * is only available up to Java 10, and the second is only available in Java 11
 * or later; there's no single one that works across all (current) JMRI-supported JDKs.
 * <p>
 * The implementation is from the OpenJDK 11 sources via their license.
 *
 * @author Bob Jacobsen  Copyright 2019
 */

@Retention(RetentionPolicy.CLASS)  // For access by JaCoCo et al 
@Target({ElementType.PACKAGE,
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.PARAMETER})
@Documented
@Inherited

public @interface Generated {

    /**
     * The value element MUST have the name of the code generator. The
     * name is the fully qualified name of the code generator.
     *
     * @return The name of the code generator
     */
    String[] value();

    /**
     * Date when the source was generated. The date element must follow the ISO
     * 8601 standard. For example the date element would have the following
     * value 2017-07-04T12:08:56.235-0700 which represents 2017-07-04 12:08:56
     * local time in the U.S. Pacific Time time zone.
     *
     * @return The date the source was generated
     */
    String date() default "";

    /**
     * A place holder for any comments that the code generator may want to
     * include in the generated code.
     *
     * @return Comments that the code generated included
     */
    String comments() default "";
}
