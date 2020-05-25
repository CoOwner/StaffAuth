package com.mongodb;

import com.mongodb.client.model.*;

final class DBObjectCollationHelper
{
    static Collation createCollationFromOptions(final DBObject options) {
        if (options.get("collation") == null) {
            return null;
        }
        if (!(options.get("collation") instanceof DBObject)) {
            throw new IllegalArgumentException("collation options should be a document");
        }
        final Collation.Builder builder = Collation.builder();
        final DBObject collation = (DBObject)options.get("collation");
        if (collation.get("locale") == null) {
            throw new IllegalArgumentException("'locale' is required when providing collation options");
        }
        final Object locale = collation.get("locale");
        if (!(locale instanceof String)) {
            throw new IllegalArgumentException("collation 'locale' should be a String");
        }
        builder.locale((String)locale);
        if (collation.get("caseLevel") != null) {
            final Object caseLevel = collation.get("caseLevel");
            if (!(caseLevel instanceof Boolean)) {
                throw new IllegalArgumentException("collation 'caseLevel' should be a Boolean");
            }
            builder.caseLevel((Boolean)caseLevel);
        }
        if (collation.get("caseFirst") != null) {
            final Object caseFirst = collation.get("caseFirst");
            if (!(caseFirst instanceof String)) {
                throw new IllegalArgumentException("collation 'caseFirst' should be a String");
            }
            builder.collationCaseFirst(CollationCaseFirst.fromString((String)caseFirst));
        }
        if (collation.get("strength") != null) {
            final Object strength = collation.get("strength");
            if (!(strength instanceof Integer)) {
                throw new IllegalArgumentException("collation 'strength' should be an Integer");
            }
            builder.collationStrength(CollationStrength.fromInt((int)strength));
        }
        if (collation.get("numericOrdering") != null) {
            final Object numericOrdering = collation.get("numericOrdering");
            if (!(numericOrdering instanceof Boolean)) {
                throw new IllegalArgumentException("collation 'numericOrdering' should be a Boolean");
            }
            builder.numericOrdering((Boolean)numericOrdering);
        }
        if (collation.get("alternate") != null) {
            final Object alternate = collation.get("alternate");
            if (!(alternate instanceof String)) {
                throw new IllegalArgumentException("collation 'alternate' should be a String");
            }
            builder.collationAlternate(CollationAlternate.fromString((String)alternate));
        }
        if (collation.get("maxVariable") != null) {
            final Object maxVariable = collation.get("maxVariable");
            if (!(maxVariable instanceof String)) {
                throw new IllegalArgumentException("collation 'maxVariable' should be a String");
            }
            builder.collationMaxVariable(CollationMaxVariable.fromString((String)maxVariable));
        }
        if (collation.get("normalization") != null) {
            final Object normalization = collation.get("normalization");
            if (!(normalization instanceof Boolean)) {
                throw new IllegalArgumentException("collation 'normalization' should be a Boolean");
            }
            builder.normalization((Boolean)normalization);
        }
        if (collation.get("backwards") != null) {
            final Object backwards = collation.get("backwards");
            if (!(backwards instanceof Boolean)) {
                throw new IllegalArgumentException("collation 'backwards' should be a Boolean");
            }
            builder.backwards((Boolean)backwards);
        }
        return builder.build();
    }
    
    private DBObjectCollationHelper() {
    }
}
