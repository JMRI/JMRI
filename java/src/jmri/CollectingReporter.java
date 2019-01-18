package jmri;

/**
 * This is an extension of a reporter device that is capable of collecting 
 * multiple reports in a collection.  The type of collection is not specified 
 * by the interface, since that may be determined by the application.
 * <BR>
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * </P><P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * </P>
 *
 * @author Paul Bender Copyright (C) 2019
 * @see jmri.Reporter
 */
public interface CollectingReporter extends Reporter {

     /**
      * @return the collection of elements associated with this reporter.
      */
     public java.util.Collection getCollection();

}
