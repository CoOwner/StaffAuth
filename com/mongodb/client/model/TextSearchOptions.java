package com.mongodb.client.model;

public final class TextSearchOptions
{
    private String language;
    private Boolean caseSensitive;
    private Boolean diacriticSensitive;
    
    public String getLanguage() {
        return this.language;
    }
    
    public TextSearchOptions language(final String language) {
        this.language = language;
        return this;
    }
    
    public Boolean getCaseSensitive() {
        return this.caseSensitive;
    }
    
    public TextSearchOptions caseSensitive(final Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }
    
    public Boolean getDiacriticSensitive() {
        return this.diacriticSensitive;
    }
    
    public TextSearchOptions diacriticSensitive(final Boolean diacriticSensitive) {
        this.diacriticSensitive = diacriticSensitive;
        return this;
    }
    
    @Override
    public String toString() {
        return "Text Search Options{language='" + this.language + '\'' + ", caseSensitive=" + this.caseSensitive + ", diacriticSensitive=" + this.diacriticSensitive + '}';
    }
}
