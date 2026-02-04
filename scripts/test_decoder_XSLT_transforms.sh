#!/bin/bash

# Check that the decoder XSLT transforms work

cd xml/XSLT

ant clean && ant

result=$?

cd ../..

if [[ $result != 0 ]]; then
    echo "decoder XSLT transformations failed"
    exit 1
fi
