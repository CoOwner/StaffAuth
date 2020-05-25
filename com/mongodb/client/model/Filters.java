package com.mongodb.client.model;

import org.bson.conversions.*;
import com.mongodb.assertions.*;
import java.util.regex.*;
import org.bson.codecs.configuration.*;
import com.mongodb.client.model.geojson.*;
import org.bson.*;
import java.util.*;

public final class Filters
{
    private Filters() {
    }
    
    public static <TItem> Bson eq(final TItem value) {
        return eq("_id", value);
    }
    
    public static <TItem> Bson eq(final String fieldName, final TItem value) {
        return new SimpleEncodingFilter<Object>(fieldName, value);
    }
    
    public static <TItem> Bson ne(final String fieldName, final TItem value) {
        return new OperatorFilter<Object>("$ne", fieldName, value);
    }
    
    public static <TItem> Bson gt(final String fieldName, final TItem value) {
        return new OperatorFilter<Object>("$gt", fieldName, value);
    }
    
    public static <TItem> Bson lt(final String fieldName, final TItem value) {
        return new OperatorFilter<Object>("$lt", fieldName, value);
    }
    
    public static <TItem> Bson gte(final String fieldName, final TItem value) {
        return new OperatorFilter<Object>("$gte", fieldName, value);
    }
    
    public static <TItem> Bson lte(final String fieldName, final TItem value) {
        return new OperatorFilter<Object>("$lte", fieldName, value);
    }
    
    public static <TItem> Bson in(final String fieldName, final TItem... values) {
        return in(fieldName, Arrays.asList(values));
    }
    
    public static <TItem> Bson in(final String fieldName, final Iterable<TItem> values) {
        return new IterableOperatorFilter<Object>(fieldName, "$in", values);
    }
    
    public static <TItem> Bson nin(final String fieldName, final TItem... values) {
        return nin(fieldName, Arrays.asList(values));
    }
    
    public static <TItem> Bson nin(final String fieldName, final Iterable<TItem> values) {
        return new IterableOperatorFilter<Object>(fieldName, "$nin", values);
    }
    
    public static Bson and(final Iterable<Bson> filters) {
        return new AndFilter(filters);
    }
    
    public static Bson and(final Bson... filters) {
        return and(Arrays.asList(filters));
    }
    
    public static Bson or(final Iterable<Bson> filters) {
        return new OrNorFilter(OrNorFilter.Operator.OR, filters);
    }
    
    public static Bson or(final Bson... filters) {
        return or(Arrays.asList(filters));
    }
    
    public static Bson not(final Bson filter) {
        return new NotFilter(filter);
    }
    
    public static Bson nor(final Bson... filters) {
        return nor(Arrays.asList(filters));
    }
    
    public static Bson nor(final Iterable<Bson> filters) {
        return new OrNorFilter(OrNorFilter.Operator.NOR, filters);
    }
    
    public static Bson exists(final String fieldName) {
        return exists(fieldName, true);
    }
    
    public static Bson exists(final String fieldName, final boolean exists) {
        return new OperatorFilter<Object>("$exists", fieldName, BsonBoolean.valueOf(exists));
    }
    
    public static Bson type(final String fieldName, final BsonType type) {
        return new OperatorFilter<Object>("$type", fieldName, new BsonInt32(type.getValue()));
    }
    
    public static Bson type(final String fieldName, final String type) {
        return new OperatorFilter<Object>("$type", fieldName, new BsonString(type));
    }
    
    public static Bson mod(final String fieldName, final long divisor, final long remainder) {
        return new OperatorFilter<Object>("$mod", fieldName, new BsonArray(Arrays.asList(new BsonInt64(divisor), new BsonInt64(remainder))));
    }
    
    public static Bson regex(final String fieldName, final String pattern) {
        return regex(fieldName, pattern, null);
    }
    
    public static Bson regex(final String fieldName, final String pattern, final String options) {
        Assertions.notNull("pattern", pattern);
        return new SimpleFilter(fieldName, (BsonValue)new BsonRegularExpression(pattern, options));
    }
    
    public static Bson regex(final String fieldName, final Pattern pattern) {
        Assertions.notNull("pattern", pattern);
        return new SimpleEncodingFilter<Object>(fieldName, pattern);
    }
    
    public static Bson text(final String search) {
        Assertions.notNull("search", search);
        return text(search, new TextSearchOptions());
    }
    
    @Deprecated
    public static Bson text(final String search, final String language) {
        Assertions.notNull("search", search);
        return text(search, new TextSearchOptions().language(language));
    }
    
    public static Bson text(final String search, final TextSearchOptions textSearchOptions) {
        Assertions.notNull("search", search);
        Assertions.notNull("textSearchOptions", textSearchOptions);
        return new TextFilter(search, textSearchOptions);
    }
    
    public static Bson where(final String javaScriptExpression) {
        Assertions.notNull("javaScriptExpression", javaScriptExpression);
        return new BsonDocument("$where", new BsonString(javaScriptExpression));
    }
    
    public static <TItem> Bson all(final String fieldName, final TItem... values) {
        return all(fieldName, Arrays.asList(values));
    }
    
    public static <TItem> Bson all(final String fieldName, final Iterable<TItem> values) {
        return new IterableOperatorFilter<Object>(fieldName, "$all", values);
    }
    
    public static Bson elemMatch(final String fieldName, final Bson filter) {
        return new Bson() {
            @Override
            public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
                return new BsonDocument(fieldName, new BsonDocument("$elemMatch", filter.toBsonDocument(documentClass, codecRegistry)));
            }
        };
    }
    
    public static Bson size(final String fieldName, final int size) {
        return new OperatorFilter<Object>("$size", fieldName, (Object)size);
    }
    
    public static Bson bitsAllClear(final String fieldName, final long bitmask) {
        return new OperatorFilter<Object>("$bitsAllClear", fieldName, (Object)bitmask);
    }
    
    public static Bson bitsAllSet(final String fieldName, final long bitmask) {
        return new OperatorFilter<Object>("$bitsAllSet", fieldName, (Object)bitmask);
    }
    
    public static Bson bitsAnyClear(final String fieldName, final long bitmask) {
        return new OperatorFilter<Object>("$bitsAnyClear", fieldName, (Object)bitmask);
    }
    
    public static Bson bitsAnySet(final String fieldName, final long bitmask) {
        return new OperatorFilter<Object>("$bitsAnySet", fieldName, (Object)bitmask);
    }
    
    public static Bson geoWithin(final String fieldName, final Geometry geometry) {
        return new GeometryOperatorFilter<Object>("$geoWithin", fieldName, geometry);
    }
    
    public static Bson geoWithin(final String fieldName, final Bson geometry) {
        return new GeometryOperatorFilter<Object>("$geoWithin", fieldName, geometry);
    }
    
    public static Bson geoWithinBox(final String fieldName, final double lowerLeftX, final double lowerLeftY, final double upperRightX, final double upperRightY) {
        final BsonDocument box = new BsonDocument("$box", new BsonArray(Arrays.asList(new BsonArray(Arrays.asList(new BsonDouble(lowerLeftX), new BsonDouble(lowerLeftY))), new BsonArray(Arrays.asList(new BsonDouble(upperRightX), new BsonDouble(upperRightY))))));
        return new OperatorFilter<Object>("$geoWithin", fieldName, box);
    }
    
    public static Bson geoWithinPolygon(final String fieldName, final List<List<Double>> points) {
        final BsonArray pointsArray = new BsonArray();
        for (final List<Double> point : points) {
            pointsArray.add(new BsonArray(Arrays.asList(new BsonDouble(point.get(0)), new BsonDouble(point.get(1)))));
        }
        final BsonDocument polygon = new BsonDocument("$polygon", pointsArray);
        return new OperatorFilter<Object>("$geoWithin", fieldName, polygon);
    }
    
    public static Bson geoWithinCenter(final String fieldName, final double x, final double y, final double radius) {
        final BsonDocument center = new BsonDocument("$center", new BsonArray(Arrays.asList(new BsonArray(Arrays.asList(new BsonDouble(x), new BsonDouble(y))), new BsonDouble(radius))));
        return new OperatorFilter<Object>("$geoWithin", fieldName, center);
    }
    
    public static Bson geoWithinCenterSphere(final String fieldName, final double x, final double y, final double radius) {
        final BsonDocument centerSphere = new BsonDocument("$centerSphere", new BsonArray(Arrays.asList(new BsonArray(Arrays.asList(new BsonDouble(x), new BsonDouble(y))), new BsonDouble(radius))));
        return new OperatorFilter<Object>("$geoWithin", fieldName, centerSphere);
    }
    
    public static Bson geoIntersects(final String fieldName, final Bson geometry) {
        return new GeometryOperatorFilter<Object>("$geoIntersects", fieldName, geometry);
    }
    
    public static Bson geoIntersects(final String fieldName, final Geometry geometry) {
        return new GeometryOperatorFilter<Object>("$geoIntersects", fieldName, geometry);
    }
    
    public static Bson near(final String fieldName, final Point geometry, final Double maxDistance, final Double minDistance) {
        return new GeometryOperatorFilter<Object>("$near", fieldName, geometry, maxDistance, minDistance);
    }
    
    public static Bson near(final String fieldName, final Bson geometry, final Double maxDistance, final Double minDistance) {
        return new GeometryOperatorFilter<Object>("$near", fieldName, geometry, maxDistance, minDistance);
    }
    
    public static Bson near(final String fieldName, final double x, final double y, final Double maxDistance, final Double minDistance) {
        return createNearFilterDocument(fieldName, x, y, maxDistance, minDistance, "$near");
    }
    
    public static Bson nearSphere(final String fieldName, final Point geometry, final Double maxDistance, final Double minDistance) {
        return new GeometryOperatorFilter<Object>("$nearSphere", fieldName, geometry, maxDistance, minDistance);
    }
    
    public static Bson nearSphere(final String fieldName, final Bson geometry, final Double maxDistance, final Double minDistance) {
        return new GeometryOperatorFilter<Object>("$nearSphere", fieldName, geometry, maxDistance, minDistance);
    }
    
    public static Bson nearSphere(final String fieldName, final double x, final double y, final Double maxDistance, final Double minDistance) {
        return createNearFilterDocument(fieldName, x, y, maxDistance, minDistance, "$nearSphere");
    }
    
    private static Bson createNearFilterDocument(final String fieldName, final double x, final double y, final Double maxDistance, final Double minDistance, final String operator) {
        final BsonDocument nearFilter = new BsonDocument(operator, new BsonArray(Arrays.asList(new BsonDouble(x), new BsonDouble(y))));
        if (maxDistance != null) {
            nearFilter.append("$maxDistance", new BsonDouble(maxDistance));
        }
        if (minDistance != null) {
            nearFilter.append("$minDistance", new BsonDouble(minDistance));
        }
        return new BsonDocument(fieldName, nearFilter);
    }
    
    private static String operatorFilterToString(final String fieldName, final String operator, final Object value) {
        return "Operator Filter{fieldName='" + fieldName + '\'' + ", operator='" + operator + '\'' + ", value=" + value + '}';
    }
    
    private static final class SimpleFilter implements Bson
    {
        private final String fieldName;
        private final BsonValue value;
        
        private SimpleFilter(final String fieldName, final BsonValue value) {
            this.fieldName = Assertions.notNull("fieldName", fieldName);
            this.value = Assertions.notNull("value", value);
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            return new BsonDocument(this.fieldName, this.value);
        }
        
        @Override
        public String toString() {
            return operatorFilterToString(this.fieldName, "$eq", this.value);
        }
    }
    
    private static final class OperatorFilter<TItem> implements Bson
    {
        private final String operatorName;
        private final String fieldName;
        private final TItem value;
        
        OperatorFilter(final String operatorName, final String fieldName, final TItem value) {
            this.operatorName = Assertions.notNull("operatorName", operatorName);
            this.fieldName = Assertions.notNull("fieldName", fieldName);
            this.value = value;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeName(this.fieldName);
            writer.writeStartDocument();
            writer.writeName(this.operatorName);
            BuildersHelper.encodeValue(writer, this.value, codecRegistry);
            writer.writeEndDocument();
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return operatorFilterToString(this.fieldName, this.operatorName, this.value);
        }
    }
    
    private static class AndFilter implements Bson
    {
        private final Iterable<Bson> filters;
        
        public AndFilter(final Iterable<Bson> filters) {
            this.filters = Assertions.notNull("filters", filters);
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            final BsonDocument andRenderable = new BsonDocument();
            for (final Bson filter : this.filters) {
                final BsonDocument renderedRenderable = filter.toBsonDocument(documentClass, codecRegistry);
                for (final Map.Entry<String, BsonValue> element : renderedRenderable.entrySet()) {
                    this.addClause(andRenderable, element);
                }
            }
            if (andRenderable.isEmpty()) {
                andRenderable.append("$and", new BsonArray());
            }
            return andRenderable;
        }
        
        private void addClause(final BsonDocument document, final Map.Entry<String, BsonValue> clause) {
            if (clause.getKey().equals("$and")) {
                for (final BsonValue value : clause.getValue().asArray()) {
                    for (final Map.Entry<String, BsonValue> element : value.asDocument().entrySet()) {
                        this.addClause(document, element);
                    }
                }
            }
            else if (document.size() == 1 && document.keySet().iterator().next().equals("$and")) {
                document.get("$and").asArray().add(new BsonDocument(clause.getKey(), clause.getValue()));
            }
            else if (document.containsKey(clause.getKey())) {
                if (document.get(clause.getKey()).isDocument() && clause.getValue().isDocument()) {
                    final BsonDocument existingClauseValue = document.get(clause.getKey()).asDocument();
                    final BsonDocument clauseValue = clause.getValue().asDocument();
                    if (this.keysIntersect(clauseValue, existingClauseValue)) {
                        this.promoteRenderableToDollarForm(document, clause);
                    }
                    else {
                        existingClauseValue.putAll(clauseValue);
                    }
                }
                else {
                    this.promoteRenderableToDollarForm(document, clause);
                }
            }
            else {
                document.append(clause.getKey(), clause.getValue());
            }
        }
        
        private boolean keysIntersect(final BsonDocument first, final BsonDocument second) {
            for (final String name : first.keySet()) {
                if (second.containsKey(name)) {
                    return true;
                }
            }
            return false;
        }
        
        private void promoteRenderableToDollarForm(final BsonDocument document, final Map.Entry<String, BsonValue> clause) {
            final BsonArray clauses = new BsonArray();
            for (final Map.Entry<String, BsonValue> queryElement : document.entrySet()) {
                clauses.add(new BsonDocument(queryElement.getKey(), queryElement.getValue()));
            }
            clauses.add(new BsonDocument(clause.getKey(), clause.getValue()));
            document.clear();
            document.put("$and", clauses);
        }
        
        @Override
        public String toString() {
            return "And Filter{filters=" + this.filters + '}';
        }
    }
    
    private static class OrNorFilter implements Bson
    {
        private final Operator operator;
        private final Iterable<Bson> filters;
        
        public OrNorFilter(final Operator operator, final Iterable<Bson> filters) {
            this.operator = Assertions.notNull("operator", operator);
            this.filters = Assertions.notNull("filters", filters);
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            final BsonDocument orRenderable = new BsonDocument();
            final BsonArray filtersArray = new BsonArray();
            for (final Bson filter : this.filters) {
                filtersArray.add(filter.toBsonDocument(documentClass, codecRegistry));
            }
            orRenderable.put(this.operator.name, filtersArray);
            return orRenderable;
        }
        
        @Override
        public String toString() {
            return this.operator.toStringName + " Filter{" + "filters=" + this.filters + '}';
        }
        
        private enum Operator
        {
            OR("$or", "Or"), 
            NOR("$nor", "Nor");
            
            private final String name;
            private final String toStringName;
            
            private Operator(final String name, final String toStringName) {
                this.name = name;
                this.toStringName = toStringName;
            }
        }
    }
    
    private static class IterableOperatorFilter<TItem> implements Bson
    {
        private final String fieldName;
        private final String operatorName;
        private final Iterable<TItem> values;
        
        IterableOperatorFilter(final String fieldName, final String operatorName, final Iterable<TItem> values) {
            this.fieldName = Assertions.notNull("fieldName", fieldName);
            this.operatorName = Assertions.notNull("operatorName", operatorName);
            this.values = Assertions.notNull("values", values);
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeName(this.fieldName);
            writer.writeStartDocument();
            writer.writeName(this.operatorName);
            writer.writeStartArray();
            for (final TItem value : this.values) {
                BuildersHelper.encodeValue(writer, value, codecRegistry);
            }
            writer.writeEndArray();
            writer.writeEndDocument();
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return operatorFilterToString(this.fieldName, this.operatorName, this.values);
        }
    }
    
    private static class SimpleEncodingFilter<TItem> implements Bson
    {
        private final String fieldName;
        private final TItem value;
        
        public SimpleEncodingFilter(final String fieldName, final TItem value) {
            this.fieldName = Assertions.notNull("fieldName", fieldName);
            this.value = value;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeName(this.fieldName);
            BuildersHelper.encodeValue(writer, this.value, codecRegistry);
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Filter{fieldName='" + this.fieldName + '\'' + ", value=" + this.value + '}';
        }
    }
    
    private static class NotFilter implements Bson
    {
        private final Bson filter;
        
        public NotFilter(final Bson filter) {
            this.filter = Assertions.notNull("filter", filter);
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            final BsonDocument filterDocument = this.filter.toBsonDocument(documentClass, codecRegistry);
            if (filterDocument.size() == 1) {
                final Map.Entry<String, BsonValue> entry = filterDocument.entrySet().iterator().next();
                return this.createFilter(entry.getKey(), entry.getValue());
            }
            final BsonArray values = new BsonArray();
            for (final Map.Entry<String, BsonValue> docs : filterDocument.entrySet()) {
                values.add(new BsonDocument(docs.getKey(), docs.getValue()));
            }
            return this.createFilter("$and", values);
        }
        
        private BsonDocument createFilter(final String fieldName, final BsonValue value) {
            if (fieldName.startsWith("$")) {
                return new BsonDocument("$not", new BsonDocument(fieldName, value));
            }
            if (value.isDocument() || value.isRegularExpression()) {
                return new BsonDocument(fieldName, new BsonDocument("$not", value));
            }
            return new BsonDocument(fieldName, new BsonDocument("$not", new BsonDocument("$eq", value)));
        }
        
        @Override
        public String toString() {
            return "Not Filter{filter=" + this.filter + '}';
        }
    }
    
    private static class GeometryOperatorFilter<TItem> implements Bson
    {
        private final String operatorName;
        private final String fieldName;
        private final TItem geometry;
        private final Double maxDistance;
        private final Double minDistance;
        
        public GeometryOperatorFilter(final String operatorName, final String fieldName, final TItem geometry) {
            this(operatorName, fieldName, geometry, null, null);
        }
        
        public GeometryOperatorFilter(final String operatorName, final String fieldName, final TItem geometry, final Double maxDistance, final Double minDistance) {
            this.operatorName = operatorName;
            this.fieldName = Assertions.notNull("fieldName", fieldName);
            this.geometry = Assertions.notNull("geometry", geometry);
            this.maxDistance = maxDistance;
            this.minDistance = minDistance;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeName(this.fieldName);
            writer.writeStartDocument();
            writer.writeName(this.operatorName);
            writer.writeStartDocument();
            writer.writeName("$geometry");
            BuildersHelper.encodeValue(writer, this.geometry, codecRegistry);
            if (this.maxDistance != null) {
                writer.writeDouble("$maxDistance", this.maxDistance);
            }
            if (this.minDistance != null) {
                writer.writeDouble("$minDistance", this.minDistance);
            }
            writer.writeEndDocument();
            writer.writeEndDocument();
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Geometry Operator Filter{fieldName='" + this.fieldName + '\'' + ", operator='" + this.operatorName + '\'' + ", geometry=" + this.geometry + ", maxDistance=" + this.maxDistance + ", minDistance=" + this.minDistance + '}';
        }
    }
    
    private static class TextFilter implements Bson
    {
        private final String search;
        private final TextSearchOptions textSearchOptions;
        
        public TextFilter(final String search, final TextSearchOptions textSearchOptions) {
            this.search = search;
            this.textSearchOptions = textSearchOptions;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            final BsonDocument searchDocument = new BsonDocument("$search", new BsonString(this.search));
            if (this.textSearchOptions.getLanguage() != null) {
                searchDocument.put("$language", new BsonString(this.textSearchOptions.getLanguage()));
            }
            if (this.textSearchOptions.getCaseSensitive() != null) {
                searchDocument.put("$caseSensitive", BsonBoolean.valueOf(this.textSearchOptions.getCaseSensitive()));
            }
            if (this.textSearchOptions.getDiacriticSensitive() != null) {
                searchDocument.put("$diacriticSensitive", BsonBoolean.valueOf(this.textSearchOptions.getDiacriticSensitive()));
            }
            return new BsonDocument("$text", searchDocument);
        }
        
        @Override
        public String toString() {
            return "Text Filter{search='" + this.search + '\'' + ", textSearchOptions=" + this.textSearchOptions + '}';
        }
    }
}
