<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet	version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    >
<xsl:output method="xml" encoding="utf-8"/>

<!-- Copyright (C) JMRI 2002, 2005, 2007 All rights reserved -->
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


<!-- * * * * *  Variables * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Routes variable * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<xsl:template name="RouteSwVar">
    <xsl:param name="CV1"/>
    <xsl:param name="routeIndex"/>
    <xsl:param name="swIndex"/>

	<xsl:if test="6 >= $swIndex">
	
		<variable CV="{$CV1}" item="Route {$routeIndex}, Switch {$swIndex}, Address" default="0">
		<splitVal highCV="{$CV1+1}" upperMask="XXXXXVVV"/>
		</variable>	  
		<variable item="Route {$routeIndex}, Switch {$swIndex}, Direction" CV="{$CV1+2}">
			<enumVal>
				<enumChoice choice="Thrown" value="0"/>
				<enumChoice choice="Closed" value="1"/>
			</enumVal>
		</variable>

		<xsl:call-template name="RouteSwVar">
			<xsl:with-param name="CV1" select="$CV1 +3"/>
			<xsl:with-param name="routeIndex" select="$routeIndex"/>
			<xsl:with-param name="swIndex" select="$swIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<xsl:template name="AllRouteVars">
  <xsl:param name="CV1" select="50"/>
    <xsl:param name="routeIndex" select="1"/>

  <xsl:if test="5 >= $routeIndex">
  
	<variable CV="{$CV1}" item="Route {$routeIndex}, Address" default="0">
		<splitVal highCV="{$CV1+1}" upperMask="XXXXXVVV"/>
	</variable>	  

    <xsl:call-template name="RouteSwVar">
		<xsl:with-param name="CV1" select="$CV1+2"/>
		<xsl:with-param name="routeIndex" select="$routeIndex"/>
		<xsl:with-param name="swIndex" select="1"/>
	</xsl:call-template>

    <xsl:call-template name="AllRouteVars">
		<xsl:with-param name="CV1" select="$CV1+20"/>
		<xsl:with-param name="routeIndex" select="$routeIndex+1"/>
	</xsl:call-template>

  </xsl:if>
</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** Routes Pane  ********************************************************************** -->
<xsl:template name="RoutesSwInPane">
    <xsl:param name="routeIndex"/>
    <xsl:param name="swIndex"/>

	<xsl:if test="6 >= $swIndex">
	
		<!-- <label><text>Route <xsl:value-of select="$routeIndex"/>, Switch <xsl:value-of select="$swIndex"/></text></label> -->
        <display item="Route {$routeIndex}, Switch {$swIndex}, Address">
			<label>Switch <xsl:value-of select="$swIndex"/>, Address  </label>
		</display>
        <display item="Route {$routeIndex}, Switch {$swIndex}, Direction">
			<label>Switch <xsl:value-of select="$swIndex"/>, Direction</label>
		</display>

		<xsl:call-template name="RoutesSwInPane">
			<xsl:with-param name="routeIndex" select="$routeIndex"/>
			<xsl:with-param name="swIndex" select="$swIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<xsl:template name="RoutesInPane">
    <xsl:param name="routeIndex" select="1"/>

  <xsl:if test="5 >= $routeIndex">
  
		<label><text>&#160;</text></label>
		<label><text>Route <xsl:value-of select="$routeIndex"/></text></label>
        <display item="Route {$routeIndex}, Address">
			<label>Route <xsl:value-of select="$routeIndex"/> Address</label>
		</display>

		<xsl:call-template name="RoutesSwInPane">
			<xsl:with-param name="routeIndex" select="$routeIndex"/>
			<xsl:with-param name="swIndex" select="1"/>
		</xsl:call-template>

		<xsl:call-template name="RoutesInPane">
			<xsl:with-param name="routeIndex" select="$routeIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='RoutesPane']">
	<pane>
	<name>Routes</name>
    <column>
		<xsl:call-template name="RoutesInPane">
		  <xsl:with-param name="routeIndex" select="1"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Rules * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
	<array svNumber="221" name="Rule 1, Stmt " tip="" startIndex='1' length='5' rw='1' minVersion='5'>
		<SV offset="0" type="bits" start='0' length='1' name="Logic" rw="1" minVersion='5'>
				<v name="OR" value='0'/>
				<v name="AND" value='1'/>
		 <SV offset="0" type="bits" start='1' length='1' name="Status" rw="1" minVersion='5'>
				<v name="0/Thrown/Free" value='0'/>
				<v name="1/Closed/Occupied" value='1'/>
		 <SV offset="0" type="bits" start='2' length='3' name="Type" rw="1" minVersion='5'>
				<v name="Not used" value='0'/>
				<v name="Switch status" value='2'/>
				<v name="Sensor" value='3'/>
				<v name="Other rule" value='4'/>
		 <SV offset="1" type="int2" name="Address" maxValue='4096' rw="1" minVersion='5'/>
	</array>
-->
<xsl:template name="RuleStmntVars">
    <xsl:param name="CV1"/>
    <xsl:param name="ruleIndex"/>
    <xsl:param name="stmntIndex"/>

	<xsl:if test="5 >= $stmntIndex">
	
		<variable item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Logic" CV="{$CV1}" mask="XXXXXXXV">
			<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>5</value></qualifier>
			<enumVal>
				<enumChoice choice="OR" value="0"/>
				<enumChoice choice="AND" value="1"/>
			</enumVal>
		</variable>
		<variable item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Status" CV="{$CV1}" mask="XXXXXXVX">
			<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>5</value></qualifier>
			<enumVal>
				<enumChoice choice="0/Thrown/Free" value="0"/>
				<enumChoice choice="1/Closed/Occupied" value="1"/>
			</enumVal>
		</variable>
		<variable item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Type" CV="{$CV1}" mask="XXXVVVXX">
			<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>5</value></qualifier>
			<enumVal>
				<enumChoice choice="Not used" value="0"/>
				<enumChoice choice="Switch status" value="2"/>
				<enumChoice choice="Sensor" value="3"/>
				<enumChoice choice="Other rule" value="4"/>
			</enumVal>
		</variable>
		<variable CV="{$CV1+1}" item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Address" default="0">
			<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>5</value></qualifier>
			<splitVal highCV="{$CV1+2}" upperMask="XXXXXVVV"/>
		</variable>	  

		<xsl:call-template name="RuleStmntVars">
			<xsl:with-param name="CV1" select="$CV1 +3"/>
			<xsl:with-param name="ruleIndex" select="$ruleIndex"/>
			<xsl:with-param name="stmntIndex" select="$stmntIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<!--
	<SV type="bits" start='0' length='1' name="Rule 1, Active" svNumber="220" rw="1" minVersion='5'>
				<v name="Not used" value='0'/>
				<v name="Yes" value='1'/>
	<SV type="bits" start='1' length='3' name="Rule 1, Switch Number" svNumber="220" rw="1" minVersion='5'>
		<values>
				<v name="No switch" value='0'/>
				<v name="Switch 1" value='1'/>
				<v name="Switch 2" value='2'/>
				<v name="Switch 3" value='3'/>
				<v name="Switch 4" value='4'/>
				<v name="Switch 5" value='5'/>
	<SV type="bits" start='4' length='1' name="Rule 1, Controlled status" svNumber="220" rw="1" minVersion='5'>
				<v name="Switch Thrown" value='0'/>
				<v name="Switch Closed" value='1'/>
	<SV type="bits" start='5' length='1' name="Rule 1, Triggering" svNumber="220" rw="1" minVersion='5'>
				<v name="No active triggering" value='0'/>
				<v name="Triggers switch" value='1'/>
	<SV type="bits" start='6' length='1' name="Rule 1, Mandatory" svNumber="220" rw="1" minVersion='5'>
				<v name="Not mandatory" value='0'/>
				<v name="Mandatory for switch state" value='1'/>
	<SV type="bits" start='7' length='1' name="Rule 1, Scope" svNumber="220" rw="1" minVersion='5'>
				<v name="Active only at true state" value='0'/>
				<v name="Active at both states" value='1'/>
-->
<xsl:template name="AllRuleVars">
  <xsl:param name="CV1" select="220"/>
    <xsl:param name="ruleIndex" select="1"/>

  <xsl:if test="5 >= $ruleIndex">
  
		<variable item="Rule {$ruleIndex}, Active" CV="{$CV1}" mask="XXXXXXXV">
			<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>5</value></qualifier>
			<enumVal>
				<enumChoice choice="Not used" value="0"/>
				<enumChoice choice="Yes" value="1"/>
			</enumVal>
		</variable>
		<variable item="Rule {$ruleIndex}, Switch Number" CV="{$CV1}" mask="XXXXVVVX">
			<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>5</value></qualifier>
			<enumVal>
				<enumChoice choice="No switch" value='0'/>
				<enumChoice choice="Switch 1" value='1'/>
				<enumChoice choice="Switch 2" value='2'/>
				<enumChoice choice="Switch 3" value='3'/>
				<enumChoice choice="Switch 4" value='4'/>
				<enumChoice choice="Switch 5" value='5'/>
			</enumVal>
		</variable>
		<variable item="Rule {$ruleIndex}, Controlled status" CV="{$CV1}" mask="XXXVXXXX">
			<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>5</value></qualifier>
			<enumVal>
				<enumChoice choice="Switch Thrown" value="0"/>
				<enumChoice choice="Switch Closed" value="1"/>
			</enumVal>
		</variable>
		<variable item="Rule {$ruleIndex}, Triggering" CV="{$CV1}" mask="XXVXXXXX">
			<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>5</value></qualifier>
			<enumVal>
				<enumChoice choice="No active triggering" value="0"/>
				<enumChoice choice="Triggers switch" value="1"/>
			</enumVal>
		</variable>
		<variable item="Rule {$ruleIndex}, Mandatory" CV="{$CV1}" mask="XVXXXXXX">
			<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>5</value></qualifier>
			<enumVal>
				<enumChoice choice="Not mandatory" value="0"/>
				<enumChoice choice="Mandatory for switch state" value="1"/>
			</enumVal>
		</variable>
		<variable item="Rule {$ruleIndex}, Scope" CV="{$CV1}" mask="XVXXXXXX">
			<qualifier><variableref>Decoder Version</variableref><relation>ge</relation><value>5</value></qualifier>
			<enumVal>
				<enumChoice choice="Active only at true state" value="0"/>
				<enumChoice choice="Active at both states" value="1"/>
			</enumVal>
		</variable>

    <xsl:call-template name="RuleStmntVars">
		<xsl:with-param name="CV1" select="$CV1+1"/>
		<xsl:with-param name="ruleIndex" select="$ruleIndex"/>
		<xsl:with-param name="stmntIndex" select="1"/>
	</xsl:call-template>

    <xsl:call-template name="AllRuleVars">
		<xsl:with-param name="CV1" select="$CV1+16"/>
		<xsl:with-param name="ruleIndex" select="$ruleIndex+1"/>
	</xsl:call-template>

  </xsl:if>
</xsl:template>

<!-- ***** Routes Pane  ********************************************************************** -->
<!--
		<variable item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Logic" CV="{$CV1} mask="XXXXXXXV">
		<variable item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Status" CV="{$CV1} mask="XXXXXXVX">
		<variable item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Type" CV="{$CV1}" mask="XXXVVVXX">
		<variable CV="{$CV1+1}" item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Address" default="0">
-->
<xsl:template name="RuleStmntsInPane">
    <xsl:param name="ruleIndex"/>
    <xsl:param name="stmntIndex"/>

	<xsl:if test="5 >= $stmntIndex">
	
		<label><text>&#160;</text></label>
        <display item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Logic">
			<label>Stmnt <xsl:value-of select="$stmntIndex"/>, Logic  </label>
		</display>
        <display item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Status">
			<label>Stmnt <xsl:value-of select="$stmntIndex"/>, Status</label>
		</display>
        <display item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Type">
			<label>Stmnt <xsl:value-of select="$stmntIndex"/>, Type  </label>
		</display>
        <display item="Rule {$ruleIndex}, Stmnt {$stmntIndex}, Address">
			<label>Stmnt <xsl:value-of select="$stmntIndex"/>, Address</label>
		</display>

		<xsl:call-template name="RuleStmntsInPane">
			<xsl:with-param name="ruleIndex" select="$ruleIndex"/>
			<xsl:with-param name="stmntIndex" select="$stmntIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<!--
		<variable item="Rule {$ruleIndex}, Active" CV="{$CV1}" mask="XXXXXXXV">
		<variable item="Rule {$ruleIndex}, Switch Number" CV="{$CV1}" mask="XXXXVVVX">
		<variable item="Rule {$ruleIndex}, Controlled status" CV="{$CV1}" mask="XXXVXXXX">
		<variable item="Rule {$ruleIndex}, Triggering" CV="{$CV1}" mask="XXVXXXXX">
		<variable item="Rule {$ruleIndex}, Mandatory" CV="{$CV1}" mask="XVXXXXXX">
		<variable item="Rule {$ruleIndex}, Scope" CV="{$CV1}" mask="XVXXXXXX">
-->
<xsl:template name="RulesInPane">
    <xsl:param name="ruleIndex" select="1"/>

  <xsl:if test="5 >= $ruleIndex">
  
		<label><text>&#160;</text></label>
		<label><text>- - - Rule <xsl:value-of select="$ruleIndex"/> - - - - - - - - - - - - - - - - - - - - - - - - - - -</text></label>
        <display item="Rule {$ruleIndex}, Active"><label>Active</label></display>
        <display item="Rule {$ruleIndex}, Switch Number"><label>Switch Number</label></display>
        <display item="Rule {$ruleIndex}, Controlled status"><label>Controlled status</label></display>
        <display item="Rule {$ruleIndex}, Triggering"><label>Triggering</label></display>
        <display item="Rule {$ruleIndex}, Mandatory"><label>Mandatory</label></display>
        <display item="Rule {$ruleIndex}, Scope"><label>Scope</label></display>

		<xsl:call-template name="RuleStmntsInPane">
			<xsl:with-param name="ruleIndex" select="$ruleIndex"/>
			<xsl:with-param name="stmntIndex" select="1"/>
		</xsl:call-template>

		<xsl:call-template name="RulesInPane">
			<xsl:with-param name="ruleIndex" select="$ruleIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='RulesPane']">
	<pane>
	<name>Rules</name>
    <column>
		<xsl:call-template name="RulesInPane">
		  <xsl:with-param name="ruleIndex" select="1"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Run All   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- - - - MATCH - - - -->
<!-- install new variables at end of variables element-->
 <xsl:template match="variables">
   <variables>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="AllRouteVars"/>
     <xsl:call-template name="AllRuleVars"/>
   </variables>
 </xsl:template>


<!--Identity template copies content forward -->
<!-- - - - MATCH - - - -->
 <xsl:template match="@*|node()">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
 </xsl:template>
 
</xsl:stylesheet>
