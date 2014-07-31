package org.fao.geonet.kernel.search.facet;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.kernel.search.Translator;
import org.jdom.Element;

public class ItemSummaryBuilder extends FacetSummaryBuilder {

	private ItemConfig config;
	
	private String langCode;
	
	public ItemSummaryBuilder(ItemConfig config, String langCode) {
		this.config = config;
		this.langCode = langCode;
	}

	@Override
	protected FacetConfig getConfig() {
		return config;
	}

	@Override
	protected Element buildDimensionTag(String value, String count) {
		return new Element(config.getPlural());
	}

	@Override
	protected Element buildCategoryTag(String value, String count) {
		Translator translator;
		
		if (ServiceContext.get() != null) {
			try {
				ServiceContext context = ServiceContext.get();
				
				translator = config.getTranslator(context, langCode);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			translator = Translator.NULL_TRANSLATOR;
		}

		String translatedValue = translator.translate(value);

		Element categoryTag = new Element(config.getName());
		
		categoryTag.setAttribute("count", count);
		categoryTag.setAttribute("name", value);

		if (translatedValue != null) {
			categoryTag.setAttribute("label", translatedValue);
		}

		return categoryTag;
	}
}
