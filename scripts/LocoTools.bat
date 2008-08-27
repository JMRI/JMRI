REM Start the LocoTools program from JMRI 2.3.2 ($Revision: 1.21 $)

java -noverify -Dsun.java2d.d3d=false -Djava.security.policy=security.policy -Djava.library.path=.;lib -Djava.rmi.server.codebase=file:java/classes/ -Dsun.java2d.noddraw -Djava.class.path=".;classes;jmriplugins.jar;jmriweb.jar;jmriserver.jar;jmri.jar;comm.jar;Serialio.jar;log4j.jar;jhall.jar;crimson.jar;jdom.jar;jython.jar;javacsv.jar;jinput.jar;ch.ntb.usb.jar;MRJAdapter.jar;servlet.jar;vecmath.jar" apps.LocoTools.LocoTools %1 %2 %3 %4 %5 %6 %7 %8 %9
