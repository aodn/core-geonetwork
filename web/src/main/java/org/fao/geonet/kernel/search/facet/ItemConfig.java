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

import jeeves.server.context.ServiceContext;

import org.fao.geonet.kernel.search.Translator;
import org.jdom.Element;

public class ItemConfig extends FacetConfig {
	private String name;
	
	private String plural;
	
	private String indexKey;
	
	private String translator;
	
	/**
	 * Create a facet configuration from a summary item configuration element.
	 * 
	 * @param summaryElement
	 */
	public ItemConfig(Element summaryElement) {
		name = summaryElement.getAttributeValue("name");
		plural = summaryElement.getAttributeValue("plural");
		indexKey = summaryElement.getAttributeValue("indexKey");
		translator = summaryElement.getAttributeValue("translator");

		addCommonConfig(summaryElement);
	}

	/**
	 * @return category element
	 */
	public Element buildCategoryTag(String label, Double value, String langCode) {
		Element result = new Element(name);
		result.setAttribute("count", value.toString());
		result.setAttribute("name", label);
		return result;
	};

	/**
	 * @return the dimension tag to be used in summary
	 */
	public String getDimensionElement() {
		return plural;
	};

	/**
	 * @return the dimension name used when indexing
	 */
	public String getDimensionName() {
		return indexKey;
	};

	/**
	 * @return a string representation of this configuration item
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("Field: ");
		sb.append(indexKey);
		sb.append("\tname:");
		sb.append(name);
		sb.append("\tmax:");
		sb.append(getMax() + "");
		sb.append("\tsort by");
		sb.append(getSortBy().toString());
		sb.append("\tsort order:");
		sb.append(getSortOrder().toString());
		return sb.toString();
	}

	/**
	 * @return the name of the facet (ie. the tag name in the XML response)
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the plural for the name (ie. the parent tag of each facet values)
	 */
	public String getPlural() {
		return plural;
	}

	/**
	 * @return the name of the field in the index
	 */
	public String getIndexKey() {
		return indexKey;
	}

	public Translator getTranslator(ServiceContext context, String langCode) {
		try {
			return Translator.createTranslator(translator, context, langCode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the facet depth - for item config it's always 1
	 */
	@Override
	public int getDepth() {
		return 1;
	}
}