#! /bin/sh
#
# Script to Uninstall RXTX in a Ubuntu GNU/Linux environment
#
#
# $Revision$ (CVS maintains this line, do not edit please)
# Assumes sun-java6-jre has been downloaded from repositories
#
# INITALISE
# ---------
# The new programme initalises
#
# TEST FOR JAVA
# -------------
# Tests to see if Java is on the computer
# If it is not says so and exit
# ###Gets the user to confirm it is the correct Java version
# ###If it is not exits
#
# FIND THE DIRECTORIES
# --------------------
# Check that the required directories are present
# Exits and reports if there is a problem
#
# DELET THE FILES
# --------------
#
# ##EDIT /etc/group
# ---------------
# ##checks if the user is in /etc/groups and if
# ##not, adds him. 
 
#######################
#  INITALISE          #
#######################

clear
echo "INSTRUCTIONS FOR REMOVING RxTx from Java"
echo "ON UBUNTU AND UBUNTU LIKE OS"
echo
echo "Warning, this programme has not been extensively tested"
echo "You must proceed at own risk"
echo "This programme is for Users who are sucessfully "
echo "Running JMRI versions prior t0 2.9.2"
echo "and wish to switch permemently to a later version"
echo "If you experience any problems please do not hesitate"
echo "to get back to the JMRI Yahoo users group."
echo "If you do it would help if you cut and paste the output"
echo "from this script onto your query"
 
#######################
#  TEST FOR JAVA      #
#######################

# Calls the GetJavaHome programme
# Remember GetJavaHome.class must be in the same directory
if test `java GetJavaHome`; then
	echo "Found Java"
	
else
	echo
	echo "Java not found on this computer"
	#echo "Please check that it is included in" 
	#echo "your path and that it is installed"
	#echo "your current path is displayed below"
	#echo $PATH
	#echo "The command to change your path will most probably be:"
	#echo 
	#echo "PATH=/usr/lib/jvm/java-6-sun/jre/bin/:$PATH"
	exit 1
fi

# Check for the correct version of Java

echo "The Version of Java required is "
	echo "java version "1.6.0_??""
	echo "Java(TM) SE Runtime Environment (build 1.6.0_????)"
	echo "Java HotSpot(TM) Client VM (build 1.6.0_?????, mixed mode, sharing)"
	echo " "
	echo "or"
	echo "java version "1.6.0_0""
	echo "OpenJDK Runtime Environment (IcedTea6 1.6.1) (6b16-1.6.1-3ubuntu1)"
	echo "OpenJDK Client VM (build 14.0-b16, mixed mode, sharing)"


	echo "This is what you have"
	java -version
	echo " "
	echo "Is this (almost) what you get (y or n)?"
	read ans
	if [ "$ans" = "n" ]
		then
		echo "It appears you have the wrong Java Version installed"
		echo "You will have to download the correct Java"
		echo "either sun or OpenJDK"
		echo "Using the Synaptic Package Manager" 
		echo "Then you will be done"
		exit 1;
	fi

#########################
#  FIND THE DIRECTORIES #
#########################



if test "$1"; then
    JAVADIR=$1;
else 
    JAVADIR=`java GetJavaHome`
    echo "Using ${JAVADIR} for java home directory"
fi

# check that the directory contains the right destinations
if test ! -d ${JAVADIR}/lib; then 
    echo Target directory ${JAVADIR} did not contain lib subdirectory
    exit 1;
fi
if test ! -d ${JAVADIR}/lib/ext; then 
    echo Target directory ${JAVADIR} did not contain lib/ext subdirectory
    exit 1;
fi

if test ! -d ${JAVADIR}/lib/i386; then 
 echo Target directory ${JAVADIR} did not contain lib/i386 subdirectory
    exit 1;
fi
#####################
# DELETE THE FILES  #
#####################

echo "Deleting the files"
#sudo cp javax.comm.properties ${JAVADIR}/lib/ext
#sudo cp javax.comm.properties ${JAVADIR}/lib/
sudo rm ${JAVADIR}/lib/ext/javax.comm.properties
sudo rm ${JAVADIR}/lib/javax.comm.properties

#sudo cp ext/comm.jar ${JAVADIR}/lib/ext/
#sudo cp ext/comm.jar ${JAVADIR}/lib/
sudo rm ${JAVADIR}/lib/ext/comm.jar
sudo rm ${JAVADIR}/lib/comm.jar

#sudo cp ext/javax.comm.rxtx.properties ${JAVADIR}/lib/ext
#sudo cp ext/javax.comm.rxtx.properties ${JAVADIR}/lib/
sudo rm ${JAVADIR}/lib/ext/javax.comm.rxtx.properties
sudo rm ${JAVADIR}/lib/javax.comm.rxtx.properties



#sudo cp ext/RXTXcomm.jar ${JAVADIR}/lib/ext/
#sudo cp ext/RXTXcomm.jar ${JAVADIR}/lib/
sudo rm ${JAVADIR}/lib/ext/RXTXcomm.jar
sudo rm ${JAVADIR}/lib/RXTXcomm.jar


#sudo cp i386/* ${JAVADIR}/lib/i386/
sudo rm ${JAVADIR}/lib/i386/librxtxParallel.la
sudo rm ${JAVADIR}/lib/i386/librxtxParallel-2.0.7pre2.so
sudo rm ${JAVADIR}/lib/i386/librxtxSerial.la
sudo rm ${JAVADIR}/lib/i386/librxtxSerial-2.0.7pre2.so

#sudo ln -s ${JAVADIR}/lib/i386/librxtxSerial-2.0.7pre2.so ${JAVADIR}/lib/i386/librxtxSerial.so;
sudo rm ${JAVADIR}/lib/i386/librxtxSerial.so

# add permission to the TTY devices
#sudo chmod 666 /dev/ttyS0
#sudo chmod 666 /dev/ttyS1

echo
echo

####################
#  EDIT            #
#  /etc/group      #
####################


#sudo cp /etc/group /etc/group.old
#echo "I have backed up /etc/group to group.old"
#echo "Checking if you are a member of uucp"
#if (groups | grep -wq 'uucp'); then
#        echo "OK " ${USER} "is a member of uucp"
#        else echo "Adding " ${USER} " to group uucp"
#        sudo gpasswd -a ${USER} uucp
#        echo " You must relogin for the changes to take effect"
#fi


