
REM ##############################################################################

REM Invoke the the PacketPro JMRI application, which provide a Jython
REM command line.

REM There must be a local jython interpreter and JMRI libraries available

REM $Revision: 1.6 $ (CVS maintains this line, do not edit please)

SET CLASSPATH=.;classes;jmriplugins.jar;jmri.jar;comm.jar;Serialio.jar;log4j.jar;collections.jar;jh.jar;crimson.jar;jdom-jdk11.jar;jython.jar;MRJAdapter.jar
     
jython jython/PacketPro.py %*

