package org.fao.geonet.kernel.search.classifier;

import java.util.ArrayList;
import java.util.List;

import org.fao.geonet.kernel.ThesaurusManager;

public class BroaderTerm implements Classifier {

	private ThesaurusManager thesaurusManager;

	private String conceptScheme;

	public BroaderTerm(ThesaurusManager thesaurusManager, String conceptScheme) {
		this.thesaurusManager = thesaurusManager;
		this.conceptScheme = conceptScheme;
	}

	@Override
	public List<String> classify(String value) {
		return new ArrayList<String>();
	}

}
