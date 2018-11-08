package au.org.emii.classifier;

/**
 * This class defines the possible SKOS relationship types that can be used when searching and their opposite
 * relationship types
 * Same as org.fao.geonet.kernel.search.keyword with relatedMatch included as enums can't be extended
 */

public enum SkosRelation {

    RELATED("related") {
        @Override
        public SkosRelation opposite() {
            return this;
        }
    },
    NARROWER("narrower") {

        @Override
        public SkosRelation opposite() {
            return BROADER;
        }

    },
    BROADER("broader") {

        @Override
        public SkosRelation opposite() {
            return NARROWER;
        }

    },
    NARROWER_MATCH("narrowMatch") {

        @Override
        public SkosRelation opposite() {
            return BROADER_MATCH;
        }

    },
    BROADER_MATCH("broadMatch") {

        @Override
        public SkosRelation opposite() {
            return NARROWER_MATCH;
        }

    },
    RELATED_MATCH("relatedMatch") {

        @Override
        public SkosRelation opposite() {
            return this;
        }

    };

    public final String name;

    SkosRelation(String name) {
        this.name = name;
    }

    public abstract SkosRelation opposite();

    @Override
    public String toString() {
        return name;
    }

}
