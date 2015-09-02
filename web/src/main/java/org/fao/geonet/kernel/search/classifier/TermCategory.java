package org.fao.geonet.kernel.search.classifier;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.KeywordBean;

public interface TermCategory {
    public KeywordBean getTerm(); 
    public CategoryPath getCategoryPath(String langCode);
}
