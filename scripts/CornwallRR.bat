REM Start the CornwallRR Java program ($Revision: 1.6 $)

java -noverify -Dsun.java2d.d3d=false -Djava.security.policy=lib/security.policy -Djava.rmi.server.codebase=file:java/classes/ -Dsun.java2d.noddraw -Djava.class.path=".;classes;jmriplugins.jar;lib\jmriplugins.jar;jmri.jar;lib\log4j.jar;lib\collections.jar;lib\jh.jar;lib\comm.jar;lib\crimson.jar;lib\jdom-jdk11.jar;lib\jython.jar" apps.cornwall.CornwallRR
