package au.org.emii.classifier;

/**
 * An AODN vocab term
 */

public class AodnTerm {
    private String uri;
    private String prefLabel;
    private String displayLabel;

    public AodnTerm(String uri, String prefLabel, String displayLabel) {
        this.uri = uri;
        this.prefLabel = prefLabel;
        this.displayLabel = displayLabel;
    }

    public String getUri() {
        return uri;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public String getCategoryLabel() {
        return displayLabel != null ? displayLabel : prefLabel;
    }
}
