package org.fao.geonet.kernel.search.classifier;

import java.util.ArrayList;
import java.util.List;

public class Value implements Classifier {

	@Override
	public List<String> classify(String value) {
		List<String> result = new ArrayList<String>();
		result.add(value);
		return result;
	}

}
