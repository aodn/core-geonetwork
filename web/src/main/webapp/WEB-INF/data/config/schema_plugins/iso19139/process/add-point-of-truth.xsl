<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs"
                version="2.0">

    <xsl:output indent="yes"/>

    <!-- Parameters passed to the stylesheet -->

    <xsl:param name="url_template"/>     <!-- template for creating point of truth url - ${uuid} will be replaced -->

    <xsl:variable name="metadata-uuid" select="//gmd:fileIdentifier/*/text()"/>

    <!-- Default rule - copy, applying templates to attributes/child nodes -->

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Add point of truth online resource element to the first transferOptions -->
    <!-- element in the MD_Distribution section                                  -->

    <xsl:template match="gmd:MD_Distribution/gmd:transferOptions[1]/gmd:MD_DigitalTransferOptions[1]">
        <xsl:copy>
            <xsl:apply-templates select="node()"/>
            <gmd:onLine>
                <gmd:CI_OnlineResource>
                    <gmd:linkage>
                        <gmd:URL><xsl:value-of select="replace($url_template, '\$\{uuid\}', $metadata-uuid)"/></gmd:URL>
                    </gmd:linkage>
                    <gmd:protocol>
                        <gco:CharacterString>WWW:LINK-1.0-http--metadata-URL</gco:CharacterString>
                    </gmd:protocol>
                    <gmd:description>
                        <gco:CharacterString>Point of truth URL of this metadata record</gco:CharacterString>
                    </gmd:description>
                </gmd:CI_OnlineResource>
            </gmd:onLine>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
