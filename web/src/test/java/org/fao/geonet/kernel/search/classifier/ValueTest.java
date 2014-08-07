package org.fao.geonet.kernel.search.classifier;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class ValueTest {

	@Test
	public void testClassify() {
		Value valueClassifier = new Value();

		List<String> result = valueClassifier.classify("ant-bat-car", "eng");

		assertEquals(result.size(), 1);
		assertEquals(result.get(0), "ant-bat-car");
	}

}
