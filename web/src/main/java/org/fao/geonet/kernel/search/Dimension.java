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

package org.fao.geonet.kernel.search;

import java.util.List;

import org.fao.geonet.kernel.search.classifier.Value;
import org.jdom.Element;

public class Dimension {
	
	public static final String DEFAULT_CLASSIFIER = Value.class.getName();

	private String name;

	private String indexKey;

	private String label;

	private String classifier;

	private List<Element> params;

	public Dimension(Element e) {
		Element elem = (Element) e.clone();

		name = elem.getAttributeValue("name");
		indexKey = elem.getAttributeValue("indexKey");
		label = elem.getAttributeValue("label");
		classifier = elem.getAttributeValue("classifier");

		if (classifier == null) {
			classifier = DEFAULT_CLASSIFIER;
		}

		@SuppressWarnings("unchecked")
		List<Element> params = (List<Element>) elem.getChildren();

		this.params = params;
	}

	public String getName() {
		return name;
	}

	public String getIndexKey() {
		return indexKey;
	}

	public String getLabel() {
		return label;
	}

	public String getClassifier() {
		return classifier;
	}

	public List<Element> getParams() {
		return params;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("dimension: ");
		sb.append(name);
		sb.append("\tindexKey: ");
		sb.append(indexKey);
		sb.append("\tclassifier: ");
		sb.append(classifier);
		return sb.toString();
	}
}
