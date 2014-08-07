package org.fao.geonet.kernel.search.classifier;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class SplitTest {

	@Test
	public void testClassify() {
		Split splitClassifier = new Split("-");

		List<String> result = splitClassifier.classify("ant-bat-car", "eng");

		assertEquals(result.size(), 3);
		assertEquals(result.get(0), "ant");
		assertEquals(result.get(1), "bat");
		assertEquals(result.get(2), "car");
	}

}
