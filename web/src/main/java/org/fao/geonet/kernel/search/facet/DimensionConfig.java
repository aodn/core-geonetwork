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

import org.jdom.Element;

public class DimensionConfig extends FacetConfig {
	private String name;

	private int depth;

	/**
	 * Create a facet configuration from a summary dimension configuration element.
	 * 
	 * @param summaryElement
	 */
	public DimensionConfig(Element summaryElement) {
		addCommonConfig(summaryElement);
		
		name = summaryElement.getAttributeValue("name");
		
		String depthAttribute = summaryElement.getAttributeValue("depth");

		if (depthAttribute == null) {
			depth = Integer.MAX_VALUE;
		} else {
			depth = Integer.parseInt(depthAttribute);
		}
	}

	/**
	 * @return the dimension name used when indexing
	 */
	public String getCategoryTag() {
		return "category";
	};

	/**
	 * @return the dimension tag to be used in summary
	 */
	public String getDimensionTag() {
		return "dimension";
	};

	/**
	 * @return the dimension name used when indexing
	 */
	public String getDimensionName() {
		return name;
	};

	/**
	 * @return a string representation of this configuration item
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("Dimension: ");
		sb.append(name);
		sb.append("\tmax:");
		sb.append(getMax() + "");
		sb.append("\tsort by");
		sb.append(getSortBy().toString());
		sb.append("\tsort order:");
		sb.append(getSortOrder().toString());
		sb.append("\tdepth:");
		sb.append(Integer.toString(depth));
		return sb.toString();
	}

	/**
	 * @return the facet depth
	 */
	@Override
	public int getDepth() {
		return depth;
	}
}

