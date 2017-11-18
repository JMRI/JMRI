<?xml version="1.0" encoding="iso-8859-1"?>
<!-- Process a JMRI decoder file translating iVariable to Variable elements -->

<!-- xsltproc xml/XSLT/transformIVariableToVariable.xsl xml/decoders/QSI_Steam.xml | diff - xml/decoders/QSI_Steam.xml   -->

<!-- The sequence of operations to translate a file is                      -->
<!--  xml/XSLT/transformIVariableToVariable.xsl                                   -->
<!--  xmllint -format                                                       -->

<!-- This file is part of JMRI.  Copyright 2009-2015.                       -->
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
    >

<xsl:output method="xml" encoding="utf-8"/>

<!--specific template match for ivariable element -->
<!-- Attributes renamed:                          -->
<!--      CVname  to CV                           -->
    <xsl:template match="ivariable" priority="5">
      <variable>
        <xsl:apply-templates select="@label" />
        <xsl:attribute name="CV"><xsl:value-of select="@CVname" /></xsl:attribute>
        <xsl:apply-templates select="@mask" />
        <xsl:apply-templates select="@readOnly" />
        <xsl:apply-templates select="@infoOnly" />
        <xsl:apply-templates select="@opsOnly" />
        <xsl:apply-templates select="@writeOnly" />
        <xsl:apply-templates select="@default" />
        <xsl:apply-templates select="@comment" />
        <xsl:apply-templates select="@item" />
        <xsl:apply-templates select="@minFn" />
        <xsl:apply-templates select="@minOut" />
        <xsl:apply-templates select="@inOptions" />
        <xsl:apply-templates select="@tooltip" />
        <xsl:apply-templates select="@include" />
        <xsl:apply-templates select="@exclude" />
        <xsl:apply-templates select="node()" />
      </variable>
     </xsl:template>

<!--rename ienumVal element to enumVal -->
    <xsl:template match="ienumVal" priority="5">
        <enumVal>
            <xsl:apply-templates select="@*|node()" />
        </enumVal>
    </xsl:template>

<!--rename ienumChoice element to enumChoice -->
    <xsl:template match="ienumChoice" priority="5">
        <enumChoice>
            <xsl:apply-templates select="@*|node()" />
        </enumChoice>
    </xsl:template>

<!--rename indexedVal element to decVal -->
    <xsl:template match="indexedVal" priority="5">
        <decVal>
            <xsl:apply-templates select="@*|node()" />
        </decVal>
    </xsl:template>

    
<!--rename indexedPairVal element to splitVal -->
    <xsl:template match="indexedPairVal" priority="5">
        <splitVal>
            <xsl:attribute name="highCV"><xsl:value-of select="@highCVname" /></xsl:attribute>
            <xsl:apply-templates select="@min" />
            <xsl:apply-templates select="@max" />
            <xsl:apply-templates select="@default" />
            <xsl:apply-templates select="@factor" />
            <xsl:apply-templates select="@offset" />
            <xsl:apply-templates select="@comment" />
            <xsl:apply-templates select="@upperMask" />
            <xsl:apply-templates select="node()" />
        </splitVal>
    </xsl:template>

    
<!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>



</xsl:stylesheet>
