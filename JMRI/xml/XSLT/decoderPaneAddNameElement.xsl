<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Process a JMRI decoder or programmer file, adding a name element (with default     -->
<!-- language) based on the name (preferred) of a pane element          -->

<!-- Note: An existing default-language name element is not replaced.     -->

<!-- xsltproc decoderPaneAddNameElement.xsl ../programmers/TrainShowBasic.xml | diff - ../programmers/TrainShowBasic.xml     -->

<!-- The sequence of operations to normalize a decoder file is -->
<!--  decoderLabelToItem.xsl                                   -->
<!--  decoderAddLabelElement.xsl                               -->
<!--  decoderSuppressRedundantLabel.xsl                        -->
<!--  xmllint -format                                          -->


<!-- This file is part of JMRI.  Copyright 2009-2014.                       -->
<!--                                                                        -->
<!-- JMRI is free software; you can redistribute it and/or modify it under  -->
<!-- the terms of version 2 of the GNU General Public License as published  -->
<!-- by the Free Software Foundation. See the "COPYING" file for a copy     -->
<!-- of this license.                                                       -->
<!--                                                                        -->
<!-- JMRI is distributed in the hope that it will be useful, but WITHOUT    -->
<!-- ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or  -->
<!-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License  -->
<!-- for more details.                                                      -->
 
<xsl:stylesheet	version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    >

<xsl:output method="xml" encoding="utf-8"/>

<!--specific template match for pane element with default-language name element -->
    <xsl:template match="pane[name[not(@xml:lang)]]" priority="5">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
    </xsl:template>

<!--specific template match for pane element with no name element but with name attribute present-->
    <xsl:template match="pane[@name]" priority="4">
      <xsl:copy>
        <xsl:apply-templates select="@*" /><!-- copy attributes before adding elements-->
        <xsl:element name="name"><xsl:value-of select="@name"/></xsl:element>
        <xsl:apply-templates select="node()" /> <!-- copy elements -->
      </xsl:copy>
    </xsl:template>

<!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
