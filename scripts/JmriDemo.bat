REM Start the JmriDemo Java program ($Revision: 1.14 $)

java -noverify -Dsun.java2d.d3d=false -Djava.security.policy=security.policy -Djava.rmi.server.codebase=file:java/classes/ -Dsun.java2d.noddraw -Djava.class.path=".;classes;jmriplugins.jar;jmri.jar;comm.jar;Serialio.jar;log4j.jar;jhall.jar;crimson.jar;jdom.jar;jython.jar;javacsv.jar;MRJAdapter.jar;jakarta-regexp-1.5.jar;vecmath.jar" apps.JmriDemo.JMRIdemo %1 %2 %3 %4 %5 %6 %7 %8 %9
