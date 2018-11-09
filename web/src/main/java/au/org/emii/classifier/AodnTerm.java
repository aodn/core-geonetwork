package au.org.emii.classifier;

import java.util.ArrayList;
import java.util.List;

/**
 * An AODN vocab term
 */

public class AodnTerm {
    private String uri;
    private String prefLabel;
    private List<String> altLabels;
    private String displayLabel;
    private String replaces;
    private String replacedBy;

    public AodnTerm(String uri, String prefLabel) {
        this.uri = uri;
        this.prefLabel = prefLabel;
        this.altLabels = new ArrayList<String>();
    }

    public String getUri() {
        return uri;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setAltLabels(List<String> altLabels) {
        if (altLabels == null) {
            throw new IllegalArgumentException("altLabels cannot be null");

        }
        this.altLabels = new ArrayList<String>(altLabels);
    }

    public List<String> getAltLabels() {
        return new ArrayList<String>(altLabels);
    }

    public void setDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public void setReplaces(String replaces) {
        this.replaces = replaces;
    }

    public String getReplaces() {
        return replaces;
    }

    public String getReplacedBy() {
        return replacedBy;
    }

    public void setReplacedBy(String replacedBy) {
        this.replacedBy = replacedBy;
    }
}
