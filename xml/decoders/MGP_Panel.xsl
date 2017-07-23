<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet	version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    >
<xsl:output method="xml" encoding="utf-8"/>


<!--for MGP Signal 10 -->

<!-- * * * * *  Variables * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * INPUT * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
-->
<xsl:template name="InputVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Input {$index} Type" CV="{$CV1}" mask="VVVVVVVV" default="0">
        <enumVal>
			<enumChoice choice="none" value='0'/>
			<enumChoice choice="Switch Toggle" value='1'/>
			<enumChoice choice="Switch Thrown" value='2'/>
			<enumChoice choice="Switch Closed" value='3'/>
			<enumChoice choice="Switch Closed/Thrown" value='4'/>
			<enumChoice choice="Status Toggle" value='5'/>
			<enumChoice choice="Status Occupied" value='6'/>
			<enumChoice choice="Status Free" value='7'/>
			<enumChoice choice="Status Free/Occupied" value='8'/>
			<enumChoice choice="Interrogate" value='9'/>
        </enumVal>
    </variable>
	<variable item="Input {$index} Address" CV="{$CV1 +1}" default="0">
        <splitVal highCV="{$CV1 +2}" upperMask="XXXXVVVV"/>
    </variable>
</xsl:template>

<xsl:template name="AllInputVars">
  <xsl:param name="CV1" select="30"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="16 >= $index">
    <xsl:call-template name="InputVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="AllInputVars">
      <xsl:with-param name="CV1" select="$CV1 +3"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** Input Pane  ********************************************************************** -->
<xsl:template name="InputInPane">
	<xsl:param name="index" select="1"/>

	<xsl:if test="16 >= $index">

		<label><text>&#160;</text></label>

		<label><text>Input <xsl:value-of select="$index"/></text></label>
        <display item="Input {$index} Type">
			<label>Type</label>
		</display>
		<display item="Input {$index} Address">
			<label>Address</label>
		</display>

		<xsl:call-template name="InputInPane">
			<xsl:with-param name="index" select="$index+1"/>
		</xsl:call-template>
		
  </xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='InputPane']">
	<pane>
	<name>Input</name>
    <column>
		<xsl:call-template name="InputInPane">
			<xsl:with-param name="index" select="1"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * LED   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
-->
<xsl:template name="LedVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="LED {$index} Type" CV="{$CV1}" mask="VVVVVVVV" default="0">
        <enumVal>
			<enumChoice choice="none" value='0'/>
			<enumChoice choice="Switch Thrown" value='1'/>
			<enumChoice choice="Switch Closed" value='2'/>
			<enumChoice choice="Status Occupied" value='3'/>
			<enumChoice choice="Status Free" value='4'/>
			<enumChoice choice="Switch Known state" value='5'/>
			<enumChoice choice="Switch Unknown state" value='6'/>
			<enumChoice choice="Signal Stop" value='10'/>
			<enumChoice choice="Signal Go" value='11'/>
			<enumChoice choice="Signal Go Slow" value='12'/>
			<enumChoice choice="Combo active" value='15'/>
        </enumVal>
    </variable>
	<variable item="LED {$index} Address" CV="{$CV1 +1}" default="0">
        <splitVal highCV="{$CV1 +2}" upperMask="XXXXVVVV"/>
    </variable>
</xsl:template>

<xsl:template name="x64LedVars">
  <xsl:param name="CV1" select="104"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="64 >= $index">
    <xsl:call-template name="LedVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="x64LedVars">
      <xsl:with-param name="CV1" select="$CV1 +3"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="AllLedVars">

    <xsl:call-template name="x64LedVars">
      <xsl:with-param name="CV1" select="104"/>
      <xsl:with-param name="index" select="1"/>
    </xsl:call-template>

</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** LED Pane  ********************************************************************** -->
<xsl:template name="LedInPane">
	<xsl:param name="index" select="1"/>

	<xsl:if test="64 >= $index">

		<label><text>&#160;</text></label>

		<label><text>LED <xsl:value-of select="$index"/></text></label>
        <display item="LED {$index} Type">
			<label>Type</label>
		</display>
		<display item="LED {$index} Address">
			<label>Address</label>
		</display>

		<xsl:call-template name="LedInPane">
			<xsl:with-param name="index" select="$index+1"/>
		</xsl:call-template>
		
  </xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='LedPane']">
	<pane>
	<name>LED</name>
    <column>
		<xsl:call-template name="LedInPane">
			<xsl:with-param name="index" select="1"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

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
		<variable item="Route {$routeIndex}, Switch {$swIndex}, Direction" mask="XXXVXXXX" CV="{$CV1+1}">
			<enumVal>
				<enumChoice choice="Thrown" value="0"/>
				<enumChoice choice="Closed" value="1"/>
			</enumVal>
		</variable>

		<xsl:call-template name="RouteSwVar">
			<xsl:with-param name="CV1" select="$CV1 +2"/>
			<xsl:with-param name="routeIndex" select="$routeIndex"/>
			<xsl:with-param name="swIndex" select="$swIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<xsl:template name="x8RouteVars">
	<xsl:param name="CV1" select="402"/>
    <xsl:param name="routeIndex" select="1"/>

	<xsl:if test="8 >= $routeIndex">
  
		<variable CV="{$CV1}" item="Route {$routeIndex}, Active" mask="XXXXXXXV" default="0">
			<enumVal>
				<enumChoice choice="No" value="0"/>
				<enumChoice choice="Yes" value="1"/>
			</enumVal>
		</variable>	  

		<xsl:call-template name="RouteSwVar">
			<xsl:with-param name="CV1" select="$CV1+1"/>
			<xsl:with-param name="routeIndex" select="$routeIndex"/>
			<xsl:with-param name="swIndex" select="1"/>
		</xsl:call-template>

		<xsl:call-template name="x8RouteVars">
			<xsl:with-param name="CV1" select="$CV1+13"/>
			<xsl:with-param name="routeIndex" select="$routeIndex+1"/>
		</xsl:call-template>

	</xsl:if>
</xsl:template>

<xsl:template name="AllRouteVars">
	<xsl:param name="CV1" select="400"/>
 
	<variable CV="{$CV1}" item="Route Start Address" default="0">
		<splitVal highCV="{$CV1+1}" upperMask="XXXXXVVV"/>
	</variable>	  

	<xsl:call-template name="x8RouteVars">
		<xsl:with-param name="CV1" select="402"/>
		<xsl:with-param name="routeIndex" select="1"/>
	</xsl:call-template>

</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** Routes Pane  ********************************************************************** -->
<xsl:template name="RoutesSwInPane">
    <xsl:param name="routeIndex"/>
    <xsl:param name="swIndex"/>

	<xsl:if test="6 >= $swIndex">
	
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

	<xsl:if test="8 >= $routeIndex">
  
		<label><text>&#160;</text></label>
		<label><text>Route <xsl:value-of select="$routeIndex"/></text></label>
        <display item="Route {$routeIndex}, Active">
			<label>Route <xsl:value-of select="$routeIndex"/> Active</label>
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
		<label><text>&#160;</text></label>
        <display item="Route Start Address">
		</display>
		<label><text>&#160;</text></label>

		<xsl:call-template name="RoutesInPane">
		  <xsl:with-param name="routeIndex" select="1"/>
		</xsl:call-template>
    </column>
   </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Button Combos * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!--
-->
<xsl:template name="ButtonComboVar">
    <xsl:param name="CV1"/>
    <xsl:param name="index"/>

    <variable item="Combo {$index} Active" CV="{$CV1}" mask="XXXXXXXV" default="0">
        <enumVal>
			<enumChoice choice="No" value='0'/>
			<enumChoice choice="yes" value='1'/>
        </enumVal>
    </variable>
    <variable item="Combo {$index} Button A" CV="{$CV1+1}" mask="XXXXVVVV" default="0">
        <enumVal>
			<enumChoice choice="1" value='0'/>
			<enumChoice choice="2" value='1'/>
			<enumChoice choice="3" value='2'/>
			<enumChoice choice="4" value='3'/>
			<enumChoice choice="5" value='4'/>
			<enumChoice choice="6" value='5'/>
			<enumChoice choice="7" value='6'/>
			<enumChoice choice="8" value='7'/>
			<enumChoice choice="9" value='8'/>
			<enumChoice choice="10" value='9'/>
			<enumChoice choice="11" value='10'/>
			<enumChoice choice="12" value='11'/>
			<enumChoice choice="13" value='12'/>
			<enumChoice choice="14" value='13'/>
			<enumChoice choice="15" value='14'/>
			<enumChoice choice="16" value='15'/>
        </enumVal>
    </variable>
    <variable item="Combo {$index} Button B" CV="{$CV1+1}" mask="VVVVXXXX" default="0">
        <enumVal>
			<enumChoice choice="1" value='0'/>
			<enumChoice choice="2" value='1'/>
			<enumChoice choice="3" value='2'/>
			<enumChoice choice="4" value='3'/>
			<enumChoice choice="5" value='4'/>
			<enumChoice choice="6" value='5'/>
			<enumChoice choice="7" value='6'/>
			<enumChoice choice="8" value='7'/>
			<enumChoice choice="9" value='8'/>
			<enumChoice choice="10" value='9'/>
			<enumChoice choice="11" value='10'/>
			<enumChoice choice="12" value='11'/>
			<enumChoice choice="13" value='12'/>
			<enumChoice choice="14" value='13'/>
			<enumChoice choice="15" value='14'/>
			<enumChoice choice="16" value='15'/>
        </enumVal>
    </variable>
    <variable item="Combo {$index} Order Type 1" CV="{$CV1+2}" mask="XXXXVVVV" default="0">
        <enumVal>
			<enumChoice choice="Not used" value='0'/>
			<enumChoice choice="Switch Thrown" value='1'/>
			<enumChoice choice="Switch Closed" value='2'/>
			<enumChoice choice="Status Free" value='3'/>
			<enumChoice choice="Status Occupied" value='4'/>
        </enumVal>
    </variable>
	<variable item="Combo {$index} Order Address 1" CV="{$CV1 +2}"  mask="VVVVXXXX" default="0">
        <splitVal highCV="{$CV1 +3}" upperMask="VVVVVVVV"/>
    </variable>
    <variable item="Combo {$index} Order Type 2" CV="{$CV1+4}" mask="XXXXVVVV" default="0">
        <enumVal>
			<enumChoice choice="Not used" value='0'/>
			<enumChoice choice="Switch Thrown" value='1'/>
			<enumChoice choice="Switch Closed" value='2'/>
			<enumChoice choice="Status Free" value='3'/>
			<enumChoice choice="Status Occupied" value='4'/>
        </enumVal>
    </variable>
	<variable item="Combo {$index} Order Address 2" CV="{$CV1 +4}"  mask="VVVVXXXX" default="0">
        <splitVal highCV="{$CV1 +5}" upperMask="VVVVVVVV"/>
    </variable>
	
</xsl:template>

<xsl:template name="x16ButtonComboVars">
  <xsl:param name="CV1" select="604"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="16 >= $index">
    <xsl:call-template name="ButtonComboVar">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>

    <xsl:call-template name="x16ButtonComboVars">
      <xsl:with-param name="CV1" select="$CV1 +6"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="AllButtonComboVars">
	<xsl:param name="CV1" select="600"/>

	<variable item="Direction dependant" CV="{$CV1}" mask="XXXXXXXV" default="0">
		<enumVal>
			<enumChoice choice="No" value='0'/>
			<enumChoice choice="Yes" value='1'/>
		</enumVal>
	</variable>
	<variable item="Max time between (0-15s)" CV="{$CV1}" mask="XXXVVVVX" default="0">
	</variable>
	<xsl:call-template name="x16ButtonComboVars">
		<xsl:with-param name="CV1" select="604"/>
		<xsl:with-param name="index" select="1"/>
	</xsl:call-template>

</xsl:template>

<!--  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- ***** Button Combo Pane  ********************************************************************** -->
<xsl:template name="ComboInPane">
	<xsl:param name="index" select="1"/>

	<xsl:if test="16 >= $index">

	<label><text>&#160;</text></label>

		<label><text>Combo <xsl:value-of select="$index"/></text></label>
        <display item="Combo {$index} Active">
			<label>Active</label>
		</display>
		<display item="Combo {$index} Button A">
			<label>Button A</label>
		</display>
		<display item="Combo {$index} Button B">
			<label>Button B</label>
		</display>
		<display item="Combo {$index} Order Type 1">
			<label>Order 1 Type</label>
		</display>
		<display item="Combo {$index} Order Address 1">
			<label>Order 1 Address</label>
		</display>
		<display item="Combo {$index} Order Type 2">
			<label>Order 2 Type</label>
		</display>
		<display item="Combo {$index} Order Address 2">
			<label>Order 2 Address</label>
		</display>

		<xsl:call-template name="ComboInPane">
			<xsl:with-param name="index" select="$index+1"/>
		</xsl:call-template>
		
  </xsl:if>
</xsl:template>

<!-- - - - MATCH - - - -->
<xsl:template match="pane[name='ComboPane']">
	<pane>
	<name>Combination Buttons</name>
    <column>

		<label><text>&#160;</text></label>
		<display item="Direction dependant">
		</display>
		<display item="Max time between (0-15s)">
		</display>

		<xsl:call-template name="ComboInPane">
			<xsl:with-param name="index" select="1"/>
		</xsl:call-template>

		<label><text>&#160;</text></label>

    </column>
   </pane>
</xsl:template>
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->



<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * Run All   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->
<!-- * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  -->

<!-- - - - MATCH - - - -->
<!-- install new variables at end of variables element-->
 <xsl:template match="variables">
   <variables>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="AllInputVars"/>
     <xsl:call-template name="AllLedVars"/>
     <xsl:call-template name="AllRouteVars"/>
	 <xsl:call-template name="AllButtonComboVars"/>
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
