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

public abstract class FacetConfig {
	private Facet.SortBy sortBy = Facet.SortBy.COUNT;
	
	private Facet.SortOrder sortOrder = Facet.SortOrder.DESCENDING;
	
	private int max;

	/**
	 * Instantiate a configuration instance of the right type for the
	 * passed configuration element
	 * 
	 * @param summaryElement
	 */
	public static FacetConfig newInstance(Element summaryElement) {
		if (summaryElement.getName().equals("item")) {
				return new ItemConfig(summaryElement);
		} else if (summaryElement.getName().equals("dimension")) {
				return new DimensionConfig(summaryElement);
		} else {
				throw new RuntimeException("Unknown facet config element: " + summaryElement.getName());
		}
	}

	/**
	 * Extract common configuration information
	 * 
	 * @param summaryElement
	 */
	protected void addCommonConfig(Element summaryElement) {
		
		String maxString = summaryElement.getAttributeValue("max");
		if (maxString == null) {
				max = Facet.DEFAULT_MAX_KEYS;
		} else {
				max = Integer.parseInt(maxString);
		}
		max = Math.min(Facet.MAX_SUMMARY_KEY, max);
		
		String sortByConfig = summaryElement.getAttributeValue("sortBy");
		String sortOrderConfig = summaryElement.getAttributeValue("sortOrder");

		if("value".equals(sortByConfig)){
				sortBy = Facet.SortBy.VALUE;
		} else if("numValue".equals(sortByConfig)){
				sortBy = Facet.SortBy.NUMVALUE;
		}

		if("asc".equals(sortOrderConfig)){
				sortOrder = Facet.SortOrder.ASCENDIND;
		}
	}

	/**
	 * @return the dimension name used when indexing
	 */

	public abstract String getDimensionName();
	/**
	 * @return a string representation of this configuration item.
	 */

	public abstract String toString();
	/**
	 * @return the ordering for the facet. Defaults is by {@link Facet.SortBy#COUNT}.
	 */

	public Facet.SortBy getSortBy() {
		return sortBy;
	}

	/**
	 * @return asc or desc. Defaults is {@link Facet.SortOrder#DESCENDING}.
	 */
	public Facet.SortOrder getSortOrder() {
		return sortOrder;
	}

	/**
	 * @return the depth to go to returning facet values
	 */
	public abstract int getDepth();

	/**
	 * @return (optional) the number of values to be returned for the facet.
	 * Defaults is {@link Facet#DEFAULT_MAX_KEYS} and never greater than
	 * {@link Facet#MAX_SUMMARY_KEY}.
	 */

	public int getMax() {
		return max;
	}
}