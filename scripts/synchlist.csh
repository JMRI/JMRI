#! /bin/csh -f
#
# Prepare filenames for compilation of JMRI for MacOS Classic.
#
# There are several problems in the Mac Classic JVM that 
# have to be dealt with at compile time.
#
# 1) Synchronized methods must be compiled with the "classic" compiler.
#    This problem results in deadlocks if compiled with newer versions.
#
# 2) Certain forms of anonymous inner classes (e.g. in beantable)
#    cannot be compiled with the "classic" compiler, or 
#    NullPointerExceptions will result.
#
# This is further compilicated by the compiler's desire to recompile the
# complete closure of modified files.
#
# This script creates an Ant XML file to handle classic compilation properly. 
# The output file is called "ClassicCompile.xml".  It expects to have it's
# ClassicCompile target invoked with the "compile1.1.8.path" set
# to the locations of the Java 1.1.8 jar files and classes for
# compilation. Typically, this setup is done by the check.1.1.8.xml 
# ant file of the JMRIproject.
#
# After this has been run, a "normal" compilation should be able to handle
# everything else.
#
# Copyright 2004 Bob Jacobsen

rm -f ClassicCompile.xml

echo '<project name="JMRI" default="check" basedir=".">' >>ClassicCompile.xml

echo '<\!-- Ant Build file created by synchlist.csh -->' >>ClassicCompile.xml
echo '<description>' >>ClassicCompile.xml
echo 'Control Mac Classic compilation' >>ClassicCompile.xml
echo '</description>' >>ClassicCompile.xml
echo '' >>ClassicCompile.xml

echo '<\!-- Force use of 1.1/1.2 compiler to avoid problem with synchronized keyword -->' >>ClassicCompile.xml
echo '' >>ClassicCompile.xml

echo '  <path id="project.class.path">						' >>ClassicCompile.xml
echo '    <pathelement location="../lib/collections.jar" />	' >>ClassicCompile.xml
echo '    <pathelement location="../lib/crimson.jar" />		' >>ClassicCompile.xml
echo '    <pathelement location="../lib/comm.jar" />		' >>ClassicCompile.xml
echo '    <pathelement location="../lib/jdom-jdk11.jar" />	' >>ClassicCompile.xml
echo '    <pathelement location="../lib/log4j.jar" />		' >>ClassicCompile.xml
echo '    <pathelement location="../lib/jh.jar" />			' >>ClassicCompile.xml
echo '    <pathelement location="../lib/Serialio.jar" />	' >>ClassicCompile.xml
echo '    <pathelement location="../lib/junit.jar" />		' >>ClassicCompile.xml
echo '    <pathelement location="../lib/jython.jar" />		' >>ClassicCompile.xml
echo '    <pathelement location="classes/" />				' >>ClassicCompile.xml
echo '  </path>												' >>ClassicCompile.xml

echo '  <\!-- path including Java 1.1.8 libraries -->		' >>ClassicCompile.xml
echo '  <path id="compile1.1.8.path">						' >>ClassicCompile.xml
echo '      <pathelement location="lib118/JDKClasses.zip"/>	' >>ClassicCompile.xml
echo '      <pathelement location="lib118/swingall.jar"/>		' >>ClassicCompile.xml
echo '      <path refid="project.class.path"/>				' >>ClassicCompile.xml
echo '  </path>												' >>ClassicCompile.xml


echo '  <target name="ClassicCompile" description="compile files requiring Java 1.1.8"> ' >>ClassicCompile.xml
echo '    <\!-- Compile specific classes with regular path --> 	' >>ClassicCompile.xml
echo '    <javac srcdir="${source}" target="1.1" 				' >>ClassicCompile.xml
echo '      destdir="${target}" debug="yes" verbose="no"		' >>ClassicCompile.xml
echo '      fork="yes" executable="${Env.JDK1}"  				' >>ClassicCompile.xml
echo '      includes =" ' >>ClassicCompile.xml

# now write the output filenames
# The cd-then-search-directory approach is used to get a specific output path format
(cd src; find jmri -name \*.java \( -exec grep -q synchronized {} \; \) -exec touch {} \; -print -exec echo "," \;) >>ClassicCompile.xml 
(cd src; find apps -name \*.java \( -exec grep -q synchronized {} \; \) -exec touch {} \; -print -exec echo "," \;) >>ClassicCompile.xml 

# add Version.java after the last comma
echo ' 		jmri/Version.java  ' >>ClassicCompile.xml
echo '           "> 											' >>ClassicCompile.xml
echo '      <classpath refid="project.class.path"    /> 		' >>ClassicCompile.xml
echo '      <bootclasspath refid="compile1.1.8.path" />			' >>ClassicCompile.xml
echo '    </javac> 												' >>ClassicCompile.xml
echo '  </target> 												' >>ClassicCompile.xml

echo '' >>ClassicCompile.xml
echo '' >>ClassicCompile.xml
echo '' >>ClassicCompile.xml
echo '' >>ClassicCompile.xml
echo '' >>ClassicCompile.xml
echo '' >>ClassicCompile.xml


echo '</project>' >>ClassicCompile.xml

