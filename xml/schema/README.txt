This is the location of XML Schema definitions for JMRI



Do not put an index.shtml file in this directory, because it must
be directly browsable on the web at http://jmri.org/xml/schema

Note: Somewhere between JMRI 2.9.6 and JMRI 3.8.2, 
backwards-incompatible changes were made in the layout schema.
This means that files written early (pre some version X) cannot
be validated with later releases (post some version X).
To try to remedy this, we updated the schema version
number to 3-8-2 in JMRI version 3-8-2.  That fixes the
problem going forward, but not for versions between
X and 3.8.2. That will require additional work later.
For a file with an example of the problem, see 
https://sourceforge.net/p/jmri/code/26760/log/?path=/trunk/jmri/java/test/jmri/jmrit/display/layoutEditor/LEConnectTest.xml

