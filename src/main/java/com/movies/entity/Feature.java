package com.movies.entity;

import static java.util.Objects.isNull;

public enum Feature {
    TRAILERS("Trailers"),
    COMMENTARIES("Commentaries"),
    DELETEDSCENES("Deleted Scenes"),
    BEHINDTHESCENES("Behind the Scenes");

    private final String value;

    Feature(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Feature getValueByValue(String value){

        if (isNull(value) || value.isEmpty())
            return null;


        Feature[] features = Feature.values();

        for (Feature f: features) {
            if (f.value.equals(value))
                return f;
        }

        return null;
    }
}
