//==============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search.facet;

import org.fao.geonet.kernel.search.facet.DimensionConfig;
import org.jdom.Element;

public class DimensionSummaryBuilder extends FacetSummaryBuilder {
	private DimensionConfig config;

	public DimensionSummaryBuilder(DimensionConfig config) {
		this.config = config;
	}

	@Override
	protected FacetConfig getConfig() {
		return config;
	}

	@Override
	protected Element buildDimensionTag(String value, String count) {
		return buildSummaryTag("dimension", value, count);
	}

	@Override
	protected Element buildCategoryTag(String value, String count) {
		return buildSummaryTag("category", value, count);
	}
	
	private Element buildSummaryTag(String elementName, String value, String count) {
		Element summaryTag = new Element(elementName);
		summaryTag.setAttribute("count", count);
		summaryTag.setAttribute("name", value);
		return summaryTag;
	}
}
