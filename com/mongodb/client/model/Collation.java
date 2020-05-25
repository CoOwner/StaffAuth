package com.mongodb.client.model;

import org.bson.*;
import com.mongodb.annotations.*;

public final class Collation
{
    private final String locale;
    private final Boolean caseLevel;
    private final CollationCaseFirst caseFirst;
    private final CollationStrength strength;
    private final Boolean numericOrdering;
    private final CollationAlternate alternate;
    private final CollationMaxVariable maxVariable;
    private final Boolean normalization;
    private final Boolean backwards;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static Builder builder(final Collation options) {
        return new Builder(options);
    }
    
    public String getLocale() {
        return this.locale;
    }
    
    public Boolean getCaseLevel() {
        return this.caseLevel;
    }
    
    public CollationCaseFirst getCaseFirst() {
        return this.caseFirst;
    }
    
    public CollationStrength getStrength() {
        return this.strength;
    }
    
    public Boolean getNumericOrdering() {
        return this.numericOrdering;
    }
    
    public CollationAlternate getAlternate() {
        return this.alternate;
    }
    
    public CollationMaxVariable getMaxVariable() {
        return this.maxVariable;
    }
    
    public Boolean getNormalization() {
        return this.normalization;
    }
    
    public Boolean getBackwards() {
        return this.backwards;
    }
    
    public BsonDocument asDocument() {
        final BsonDocument collation = new BsonDocument();
        if (this.locale != null) {
            collation.put("locale", new BsonString(this.locale));
        }
        if (this.caseLevel != null) {
            collation.put("caseLevel", new BsonBoolean(this.caseLevel));
        }
        if (this.caseFirst != null) {
            collation.put("caseFirst", new BsonString(this.caseFirst.getValue()));
        }
        if (this.strength != null) {
            collation.put("strength", new BsonInt32(this.strength.getIntRepresentation()));
        }
        if (this.numericOrdering != null) {
            collation.put("numericOrdering", new BsonBoolean(this.numericOrdering));
        }
        if (this.alternate != null) {
            collation.put("alternate", new BsonString(this.alternate.getValue()));
        }
        if (this.maxVariable != null) {
            collation.put("maxVariable", new BsonString(this.maxVariable.getValue()));
        }
        if (this.normalization != null) {
            collation.put("normalization", new BsonBoolean(this.normalization));
        }
        if (this.backwards != null) {
            collation.put("backwards", new BsonBoolean(this.backwards));
        }
        return collation;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Collation that = (Collation)o;
        Label_0062: {
            if (this.getLocale() != null) {
                if (this.getLocale().equals(that.getLocale())) {
                    break Label_0062;
                }
            }
            else if (that.getLocale() == null) {
                break Label_0062;
            }
            return false;
        }
        Label_0095: {
            if (this.getCaseLevel() != null) {
                if (this.getCaseLevel().equals(that.getCaseLevel())) {
                    break Label_0095;
                }
            }
            else if (that.getCaseLevel() == null) {
                break Label_0095;
            }
            return false;
        }
        if (this.getCaseFirst() != that.getCaseFirst()) {
            return false;
        }
        if (this.getStrength() != that.getStrength()) {
            return false;
        }
        Label_0154: {
            if (this.getNumericOrdering() != null) {
                if (this.getNumericOrdering().equals(that.getNumericOrdering())) {
                    break Label_0154;
                }
            }
            else if (that.getNumericOrdering() == null) {
                break Label_0154;
            }
            return false;
        }
        if (this.getAlternate() != that.getAlternate()) {
            return false;
        }
        if (this.getMaxVariable() != that.getMaxVariable()) {
            return false;
        }
        Label_0213: {
            if (this.getNormalization() != null) {
                if (this.getNormalization().equals(that.getNormalization())) {
                    break Label_0213;
                }
            }
            else if (that.getNormalization() == null) {
                break Label_0213;
            }
            return false;
        }
        if (this.getBackwards() != null) {
            if (this.getBackwards().equals(that.getBackwards())) {
                return true;
            }
        }
        else if (that.getBackwards() == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.getLocale() != null) ? this.getLocale().hashCode() : 0;
        result = 31 * result + ((this.getCaseLevel() != null) ? this.getCaseLevel().hashCode() : 0);
        result = 31 * result + ((this.getCaseFirst() != null) ? this.getCaseFirst().hashCode() : 0);
        result = 31 * result + ((this.getStrength() != null) ? this.getStrength().hashCode() : 0);
        result = 31 * result + ((this.getNumericOrdering() != null) ? this.getNumericOrdering().hashCode() : 0);
        result = 31 * result + ((this.getAlternate() != null) ? this.getAlternate().hashCode() : 0);
        result = 31 * result + ((this.getMaxVariable() != null) ? this.getMaxVariable().hashCode() : 0);
        result = 31 * result + ((this.getNormalization() != null) ? this.getNormalization().hashCode() : 0);
        result = 31 * result + ((this.getBackwards() != null) ? this.getBackwards().hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "Collation{locale='" + this.locale + "'" + ", caseLevel=" + this.caseLevel + ", caseFirst=" + this.caseFirst + ", strength=" + this.strength + ", numericOrdering=" + this.numericOrdering + ", alternate=" + this.alternate + ", maxVariable=" + this.maxVariable + ", normalization=" + this.normalization + ", backwards=" + this.backwards + "}";
    }
    
    private Collation(final Builder builder) {
        this.locale = builder.locale;
        this.caseLevel = builder.caseLevel;
        this.caseFirst = builder.caseFirst;
        this.strength = builder.strength;
        this.numericOrdering = builder.numericOrdering;
        this.alternate = builder.alternate;
        this.maxVariable = builder.maxVariable;
        this.normalization = builder.normalization;
        this.backwards = builder.backwards;
    }
    
    @NotThreadSafe
    public static final class Builder
    {
        private String locale;
        private Boolean caseLevel;
        private CollationCaseFirst caseFirst;
        private CollationStrength strength;
        private Boolean numericOrdering;
        private CollationAlternate alternate;
        private CollationMaxVariable maxVariable;
        private Boolean normalization;
        private Boolean backwards;
        
        private Builder() {
        }
        
        private Builder(final Collation options) {
            this.locale = options.getLocale();
            this.caseLevel = options.getCaseLevel();
            this.caseFirst = options.getCaseFirst();
            this.strength = options.getStrength();
            this.numericOrdering = options.getNumericOrdering();
            this.alternate = options.getAlternate();
            this.maxVariable = options.getMaxVariable();
            this.normalization = options.getNormalization();
            this.backwards = options.getBackwards();
        }
        
        public Builder locale(final String locale) {
            this.locale = locale;
            return this;
        }
        
        public Builder caseLevel(final Boolean caseLevel) {
            this.caseLevel = caseLevel;
            return this;
        }
        
        public Builder collationCaseFirst(final CollationCaseFirst caseFirst) {
            this.caseFirst = caseFirst;
            return this;
        }
        
        public Builder collationStrength(final CollationStrength strength) {
            this.strength = strength;
            return this;
        }
        
        public Builder numericOrdering(final Boolean numericOrdering) {
            this.numericOrdering = numericOrdering;
            return this;
        }
        
        public Builder collationAlternate(final CollationAlternate alternate) {
            this.alternate = alternate;
            return this;
        }
        
        public Builder collationMaxVariable(final CollationMaxVariable maxVariable) {
            this.maxVariable = maxVariable;
            return this;
        }
        
        public Builder normalization(final Boolean normalization) {
            this.normalization = normalization;
            return this;
        }
        
        public Builder backwards(final Boolean backwards) {
            this.backwards = backwards;
            return this;
        }
        
        public Collation build() {
            return new Collation(this, null);
        }
    }
}
