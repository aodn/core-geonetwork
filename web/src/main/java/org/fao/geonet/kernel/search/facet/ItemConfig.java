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

import java.util.Map;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.kernel.search.Dimension;
import org.fao.geonet.kernel.search.Translator;
import org.jdom.Element;

public class ItemConfig {
	private Dimension dimension;
	
	private Facet.SortBy sortBy = Facet.SortBy.COUNT;
	
	private Facet.SortOrder sortOrder = Facet.SortOrder.DESCENDING;
	
	private int max;

	private int depth;

	private Format format;
	
	private String translator;

	public ItemConfig(Element item, Map<String, Dimension> dimensions) {
		String dimensionName = item.getAttributeValue("dimension");

		if (dimensionName == null) {
			throw new RuntimeException("Check facet configuration. Dimension attribute for item not found");
		}

		dimension = dimensions.get(dimensionName);

		if (dimension == null) {
			throw new RuntimeException("Check facet configuration. Dimension " + dimensionName + " not found");
		}

		String maxString = item.getAttributeValue("max");

		if (maxString == null) {
				max = Facet.DEFAULT_MAX_KEYS;
		} else {
				max = Integer.parseInt(maxString);
		}

		max = Math.min(Facet.MAX_SUMMARY_KEY, max);

		String sortByConfig = item.getAttributeValue("sortBy");
		String sortOrderConfig = item.getAttributeValue("sortOrder");

		if("value".equals(sortByConfig)){
				sortBy = Facet.SortBy.VALUE;
		} else if("numValue".equals(sortByConfig)){
				sortBy = Facet.SortBy.NUMVALUE;
		}

		if("asc".equals(sortOrderConfig)){
				sortOrder = Facet.SortOrder.ASCENDIND;
		}

		String depthString = item.getAttributeValue("depth");

		if (depthString == null) {
			depth = Facet.DEFAULT_DEPTH;
		} else {
			depth = Integer.parseInt(depthString);
		}

		String formatString = item.getAttributeValue("format");

		if (formatString == null) {
			format = Format.FACET_NAME;
		} else {
			format = Format.valueOf(formatString);
		}

		translator = item.getAttributeValue("translator");
	}

	/**
	 * @return the dimension name used when indexing
	 */

	public Dimension getDimension() {
		return dimension;
	}
	/**
	 * @return a string representation of this configuration item.
	 */

	/**
	 * @return a string representation of this configuration item
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("dimension: ");
		sb.append(dimension.getName());
		sb.append("\tmax:");
		sb.append(getMax() + "");
		sb.append("\tsort by");
		sb.append(getSortBy().toString());
		sb.append("\tsort order:");
		sb.append(getSortOrder().toString());
		sb.append("\tdepth:");
		sb.append(Integer.toString(depth));
		sb.append("\tformat:");
		sb.append(format);
		return sb.toString();
	}

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
	public int getDepth() {
		return depth;
	}

	/**
	 * @return (optional) the number of values to be returned for the facet.
	 * Defaults is {@link Facet#DEFAULT_MAX_KEYS} and never greater than
	 * {@link Facet#MAX_SUMMARY_KEY}.
	 */

	public int getMax() {
		return max;
	}

	/**
	 * @return a formatter for creating item summaries
	 */

	public Formatter getFormatter(ServiceContext context) {
		return format.getFormatter(context, this);
	}

	public Translator getTranslator(ServiceContext context, String langCode) {
		if (context == null)
			return Translator.NULL_TRANSLATOR;

		try {
			return Translator.createTranslator(translator, context, langCode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}