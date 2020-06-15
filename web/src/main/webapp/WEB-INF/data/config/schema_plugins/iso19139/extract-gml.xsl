<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                version="2.0"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:gml32="http://www.opengis.net/gml/3.2"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                exclude-result-prefixes="#all">
    <xsl:output method="xml" indent="no"/>
    <xsl:template match="/" priority="2">
   		<gml:GeometryCollection >
        	<xsl:apply-templates/>
       	</gml:GeometryCollection>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="text()"/>

    <xsl:template match="gmd:EX_BoundingPolygon[string(gmd:extentTypeCode/gco:Boolean) != 'false' and string(gmd:extentTypeCode/gco:Boolean) != '0']" priority="2">
        <xsl:for-each select="gmd:polygon/gml:*|.//gml32:Polygon">
            <xsl:apply-templates mode="gml32-to-gml" select="."/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="gmd:EX_GeographicBoundingBox" priority="2">
        <xsl:variable name="w" select="./gmd:westBoundLongitude/gco:Decimal/text()"/>
        <xsl:variable name="e" select="./gmd:eastBoundLongitude/gco:Decimal/text()"/>
        <xsl:variable name="n" select="./gmd:northBoundLatitude/gco:Decimal/text()"/>
        <xsl:variable name="s" select="./gmd:southBoundLatitude/gco:Decimal/text()"/>
        <xsl:if test="$w!='' and $e!='' and $n!='' and $s!=''">
	        <gml:Polygon>
	            <gml:exterior>
	                <gml:LinearRing>
	                    <gml:coordinates><xsl:value-of select="$w"/>,<xsl:value-of select="$n"/>, <xsl:value-of select="$e"/>,<xsl:value-of select="$n"/>, <xsl:value-of select="$e"/>,<xsl:value-of select="$s"/>, <xsl:value-of select="$w"/>,<xsl:value-of select="$s"/>, <xsl:value-of select="$w"/>,<xsl:value-of select="$n"/></gml:coordinates>
	                </gml:LinearRing>
	            </gml:exterior>
	        </gml:Polygon>
        </xsl:if>
    </xsl:template>

    <!-- gml32 to gml mode - changes any gml32 elements or attributes to gml elements or attributes --> 
    <!-- otherwise just copies the attribute or node to the output -->

    <!-- default rule is to copy -->
    <xsl:template mode="gml32-to-gml" match='@*|node()'>
        <xsl:copy copy-namespaces="no">
            <xsl:apply-templates mode="gml32-to-gml" select='@*|node()'/>
        </xsl:copy>
    </xsl:template>

    <!-- change gml32 attributes to gml attributes -->
    <xsl:template mode="gml32-to-gml" match="@gml32:*">
        <xsl:attribute name="gml:{local-name()}" select="."/>
    </xsl:template>

    <!-- change gml32 elements to gml elements -->
    <xsl:template mode="gml32-to-gml" match="gml32:*">
        <xsl:element name="gml:{local-name()}">
            <xsl:apply-templates mode="gml32-to-gml" select="@*|node()"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
