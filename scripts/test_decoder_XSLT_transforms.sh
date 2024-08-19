#!/bin/bash

# Check that the decoder XSLT transforms work

cd xml/XSLT
ant clean
ant
cd ../..

