REM Start the PanelPro program from JMRI 2.1.7 ($Revision: 1.16 $)

java -noverify -Dsun.java2d.d3d=false -Djava.security.policy=security.policy -Djava.library.path=.;lib -Djava.rmi.server.codebase=file:java/classes/ -Dsun.java2d.noddraw -Djava.class.path=".;classes;jmriplugins.jar;jmriweb.jar;jmriserver.jar;jmri.jar;comm.jar;Serialio.jar;log4j.jar;jhall.jar;crimson.jar;jdom.jar;jython.jar;javacsv.jar;jinput.jar;ch.ntb.usb.jar;MRJAdapter.jar;vecmath.jar" apps.PanelPro.PanelPro %1 %2 %3 %4 %5 %6 %7 %8 %9
