<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Process a JMRI decoder file, moving the label attribute to an          -->
<!-- item attribute if the item doesn't already exist.                      -->
<!-- xsltproc decoderLabelToItem.xsl 0NMRA.xml | diff - 0NMRA.xml   -->

<!-- The sequence of operations to normalize a decoder file is -->
<!--  decoderLabelToItem.xsl                                   -->
<!--  decoderAddLabelElement.xsl                               -->
<!--  decoderSuppressRedundantLabel.xsl                        -->
<!--  xmllint -format                                          -->

<!-- This file is part of JMRI.  Copyright 2009-2011.                            -->
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

<!--specific template match for variable element with item already present-->
    <xsl:template match="variable[@item]">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
    </xsl:template>

<!--specific template match for variable element with item not present-->
    <xsl:template match="variable">
      <xsl:copy>
        <xsl:attribute name="item"><xsl:value-of select="@label"/></xsl:attribute>
        <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
    </xsl:template>

<!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
