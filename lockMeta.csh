#! /bin/sh
# build and freeze the META services
# Ken C

if [ -d ./target/classes/META-INF ] ; then
	echo "unlocking META"
	chmod a+w ./target/classes/META-INF/* -R
fi
ant clean debug
echo "locking META"
chmod a-w ./target/classes/META-INF/* -R
ls -l ./target/classes/META-INF/services
