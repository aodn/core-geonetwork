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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jeeves.utils.Log;

import org.apache.lucene.facet.search.results.FacetResult;
import org.apache.lucene.facet.search.results.FacetResultNode;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.facet.DimensionConfig;
import org.jdom.Element;

public abstract class FacetSummaryBuilder {

	public static FacetSummaryBuilder newInstance(FacetConfig config, String langCode) {
		if (config instanceof ItemConfig) {
			return new ItemSummaryBuilder((ItemConfig) config, langCode);
		} else if (config instanceof DimensionConfig) {
			return new DimensionSummaryBuilder((DimensionConfig) config);
		} else {
			throw new RuntimeException("Unknown facet configuration type");
		}
	}
	
	protected abstract FacetConfig getConfig();

	public Element build(FacetResult result) {
		FacetConfig config = getConfig();

		FacetResultNode resultNode = result.getFacetResultNode();
		String dimensionValue = resultNode.getLabel().toString();
		String dimensionCount = doubleFormat.format(resultNode.getValue());
		Element facets = buildDimensionTag(dimensionValue, dimensionCount);
		
		addSubResults(facets, config, resultNode);

		return facets;
	}

	private void addSubResults(Element parent, FacetConfig config, FacetResultNode resultNode) {
		if (resultNode.getNumSubResults() == 0) {
			return;
		}

		List<Entry<String, FacetResultNode>> entries = sortResults(config,
				resultNode);

		for (Entry<String, FacetResultNode> entry : entries) {
			String facetValue = entry.getKey();
			String facetCount = doubleFormat.format(entry
					.getValue().getValue());

			if (Log.isDebugEnabled(Geonet.FACET_ENGINE)) {
				Log.debug(Geonet.FACET_ENGINE, " - " + facetValue
						+ " (" + facetCount + ")");
			}

			Element category = buildCategoryTag(facetValue, facetCount);
			addSubResults(category, config, entry.getValue());
			
			parent.addContent(category);
		}
	}

	private List<Entry<String, FacetResultNode>> sortResults(
			FacetConfig config, FacetResultNode resultNode) {
		List<Entry<String, FacetResultNode>> entries = toResultList(resultNode);

		if (Log.isDebugEnabled(Geonet.FACET_ENGINE)) {
			Log.debug(Geonet.FACET_ENGINE, config.getDimensionName()
					+ ":\tSorting facet by " + config.getSortBy().toString()
					+ " (" + config.getSortOrder().toString() + ")");
		}

		// No need for a custom comparator Lucene facet request is
		// made by count descending order
		if (Facet.SortBy.COUNT != config.getSortBy()) {
			Comparator c;

			if (Facet.SortBy.NUMVALUE == config.getSortBy()) {
				c = numericComparator();
			} else {
				c = valueComparator();
			}

			Collections.sort(entries, c);

			if (Facet.SortOrder.DESCENDING == config.getSortOrder()) {
				Collections.reverse(entries);
			}
		}

		return entries;
	}

	private Comparator valueComparator() {
		return new Comparator<Entry<String, FacetResultNode>>() {
			public int compare(
					final Entry<String, FacetResultNode> e1,
					final Entry<String, FacetResultNode> e2) {
				return e1.getKey().compareTo(e2.getKey());
			}
		};
	}

	private Comparator numericComparator() {
		return new Comparator<Entry<String, FacetResultNode>>() {
			public int compare(
					final Entry<String, FacetResultNode> e1,
					final Entry<String, FacetResultNode> e2) {
				try {
					Double d1 = Double.valueOf(e1.getKey());
					Double d2 = Double.valueOf(e2.getKey());

					return d1.compareTo(d2);
				} catch (NumberFormatException e) {
					// String comparison
					Log.warning(
							Geonet.FACET_ENGINE,
							"Failed to compare numeric values ("
									+ e1.getKey()
									+ " / "
									+ e2.getKey()
									+ ") for facet. Check sortBy option in summary configuration.");
					return e1.getKey().compareTo(
							e2.getKey());
				}
			}
		};
	}

	private List<Entry<String, FacetResultNode>> toResultList(
			FacetResultNode resultNode) {
		Map<String, FacetResultNode> facetValues = new LinkedHashMap<String, FacetResultNode>();

		for (Iterator subresults = resultNode.getSubResults().iterator(); subresults
				.hasNext();) {
			FacetResultNode node = (FacetResultNode) subresults
					.next();
			facetValues.put(node.getLabel().components[node.getLabel().length-1],
					node);
		}
		
		List<Entry<String, FacetResultNode>> entries = new ArrayList<Entry<String, FacetResultNode>>(
				facetValues.entrySet());
		return entries;
	}

	protected abstract Element buildDimensionTag(String value, String count);

	protected abstract Element buildCategoryTag(String value, String count);

	private DecimalFormat doubleFormat = new DecimalFormat("0");

}
