package org.fao.geonet.kernel.search.classifier;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.search.facet.CategoryHelper;

public class TermCategoryParent implements TermCategory {
    private KeywordBean term;
    private TermCategory subCategory;

    public TermCategoryParent(KeywordBean term, TermCategory subCategory) {
        this.term = term;
        this.subCategory = subCategory;
    }

    @Override
    public KeywordBean getTerm() {
        return term;
    }

    @Override
    public CategoryPath getCategoryPath(String langCode) {
        CategoryPath subCategoryPath = subCategory.getCategoryPath(langCode);
        return CategoryHelper.addParentCategory(term.getPreferredLabel(langCode), subCategoryPath);
    }
}

