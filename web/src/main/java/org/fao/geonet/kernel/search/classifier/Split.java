package org.fao.geonet.kernel.search.classifier;

import java.util.Arrays;
import java.util.List;

public class Split implements Classifier {
	
	private String regex;
	
	public Split(String regex) {
		this.regex = regex;
	}

	@Override
	public List<String> classify(String value) {
		return Arrays.asList(value.split(regex));
	}

}
