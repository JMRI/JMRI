#!/bin/bash

# Check that the default_lcf.xml and scripts/default.xml files are in synch.


sed '0,/development/{s/development/distribution/}' default_lcf.xml | diff - scripts/default_lcf.xml

result=$?

if [[ $result == 1 ]]; then
    echo ""
    echo ""
    echo "default_lcf.xml and scripts/default_lcf.xml differs."
    echo "Run \"diff default_lcf.xml scripts/default_lcf.xml\" to check the differences"
    echo ""
    echo "There is one difference that should be keept:"
    echo ""
    echo "default_lcf.xml has the line:"
    echo "Default logging configuration file for JMRI project development."
    echo ""
    echo "scripts/default_lcf.xml has the line:"
    echo "Default logging configuration file for JMRI project distribution."
    exit 1
fi

if [[ $result != 0 ]]; then
    echo "sed or diff failed to execute properly."
    exit 1
fi

