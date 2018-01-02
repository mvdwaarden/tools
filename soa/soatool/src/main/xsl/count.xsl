<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<info>
			<parsed>
				<xsl:value-of select="count(//afleidingsregel[@parsed = 'true'])" />
			</parsed>
			<total>
				<xsl:value-of select="count(//afleidingsregel)" />
			</total>
		</info>
	</xsl:template>

</xsl:stylesheet>