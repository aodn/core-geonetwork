//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search.classifier;

import static org.fao.geonet.kernel.search.facet.CategoryHelper.addParentCategory;

import java.util.ArrayList;
import java.util.List;

import jeeves.utils.Log;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.constants.Geonet;

public class Dimension implements Classifier {

	private String dimensionName;
	private Classifier wrappedClassifier;

	public Dimension(String dimensionName, Classifier wrappedClassifier) {
		this.dimensionName = dimensionName;
		this.wrappedClassifier = wrappedClassifier;
	}

	@Override
	public List<CategoryPath> classify(String value) {
		List<CategoryPath> dimensionSubCategoryPaths = wrappedClassifier.classify(value);
		return addDimensionCategory(dimensionSubCategoryPaths);
	}

	private List<CategoryPath> addDimensionCategory(List<CategoryPath> dimensionSubCategoryPaths) {
		List<CategoryPath> result = new ArrayList<CategoryPath>();

		for (CategoryPath dimensionSubCategoryPath: dimensionSubCategoryPaths) {
			CategoryPath dimensionCategoryPath = addParentCategory(
				dimensionName,
				dimensionSubCategoryPath
			);

			if(Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
				Log.debug(Geonet.INDEX_ENGINE, "Adding category path: " + dimensionCategoryPath);
			}

			result.add(dimensionCategoryPath);
		}

		return result;
	}
}
