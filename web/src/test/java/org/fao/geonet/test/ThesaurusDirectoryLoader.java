package org.fao.geonet.test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.openrdf.sesame.config.ConfigurationException;

public class ThesaurusDirectoryLoader implements ThesaurusFinder {

    private final IsoLanguagesMapper isoLangMapper = new IsoLanguagesMapper() {
        {
            isoLanguagesMap639.put("en", "eng");
        }
    };

    private Map<String, Thesaurus> nameMap = new HashMap<String, Thesaurus>();
    
    public ThesaurusDirectoryLoader(String thesauriLocation) {
        File thesauriDirectory = new File(thesauriLocation);
        
        File[] thesauri = thesauriDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File arg0, String arg1) {
                return arg1.endsWith(".rdf");
            }
        });
        
        for (File thesaurusFile : thesauri) {
            Thesaurus thesaurus = loadThesaurusFile(isoLangMapper, thesaurusFile);
            nameMap.put(thesaurusFile.getName(), thesaurus);
        }
    }

    @Override
    public boolean existsThesaurus(String name) {
        return nameMap.containsKey(name);
    }

    @Override
    public Thesaurus getThesaurusByName(String thesaurusName) {
        return nameMap.get(thesaurusName);
    }

    @Override
    public Thesaurus getThesaurusByConceptScheme(String thesaurusUri) {
        for (Thesaurus thesaurus : nameMap.values()) {
            if (thesaurus.hasConceptScheme(thesaurusUri)) {
                return thesaurus;
            }
        }
        
        return null;
    }

    @Override
    public Map<String, Thesaurus> getThesauriMap() {
        return nameMap;
    }

    private Thesaurus loadThesaurusFile(IsoLanguagesMapper isoLanguagesMapper, File thesaurusFile) {
        Thesaurus thesaurus = new Thesaurus(isoLanguagesMapper, thesaurusFile.getName(), "external", "theme", thesaurusFile, "http://dummy.org/geonetwork");

        try {
            thesaurus.initRepository();
        } catch (ConfigurationException e) {
            throw new RuntimeException("Unexpected error", e);
        }

        return thesaurus;
    }
}
