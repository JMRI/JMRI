REM Start the PanelPro Java program ($Revision: 1.4 $)

java -noverify -Djava.security.policy=lib/security.policy -Djava.rmi.server.codebase=file:java/classes/ -Dsun.java2d.noddraw -Djava.class.path=".;classes;jmriplugins.jar;lib/jmriplugins.jar;jmri.jar;lib/log4j.jar;lib/collections.jar;lib/jh.jar;lib/comm.jar;lib/crimson.jar;lib/jdom-jdk11.jar;lib/jython.jar" apps.PanelPro.PanelPro
