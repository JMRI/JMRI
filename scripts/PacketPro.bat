
REM ##############################################################################

REM Invoke the the PacketPro JMRI application, which provide a Jython
REM command line.

REM There must be a local jython interpreter and JMRI libraries available

REM $Revision: 1.3 $ (CVS maintains this line, do not edit please)

SET CLASSPATH=.;classes;jmriplugins.jar;lib\jmriplugins.jar;jmri.jar;lib\log4j.jar;lib\collections.jar;lib\jh.jar;lib\crimson.jar;lib\jdom-jdk11.jar
     
jython jython/PacketPro.py

