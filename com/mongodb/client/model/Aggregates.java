package com.mongodb.client.model;

import org.bson.conversions.*;
import org.bson.assertions.*;
import org.bson.codecs.configuration.*;
import org.bson.*;
import java.util.*;

public final class Aggregates
{
    public static Bson addFields(final Field<?>... fields) {
        return addFields(Arrays.asList(fields));
    }
    
    public static Bson addFields(final List<Field<?>> fields) {
        return new AddFieldsStage(fields);
    }
    
    public static <TExpression, Boundary> Bson bucket(final TExpression groupBy, final List<Boundary> boundaries) {
        return bucket(groupBy, boundaries, new BucketOptions());
    }
    
    public static <TExpression, TBoundary> Bson bucket(final TExpression groupBy, final List<TBoundary> boundaries, final BucketOptions options) {
        return new BucketStage<Object, Object>(groupBy, boundaries, options);
    }
    
    public static <TExpression> Bson bucketAuto(final TExpression groupBy, final int buckets) {
        return bucketAuto(groupBy, buckets, new BucketAutoOptions());
    }
    
    public static <TExpression> Bson bucketAuto(final TExpression groupBy, final int buckets, final BucketAutoOptions options) {
        return new BucketAutoStage<Object>(groupBy, buckets, options);
    }
    
    public static Bson count() {
        return count("count");
    }
    
    public static Bson count(final String field) {
        return new BsonDocument("$count", new BsonString(field));
    }
    
    public static Bson match(final Bson filter) {
        return new SimplePipelineStage("$match", filter);
    }
    
    public static Bson project(final Bson projection) {
        return new SimplePipelineStage("$project", projection);
    }
    
    public static Bson sort(final Bson sort) {
        return new SimplePipelineStage("$sort", sort);
    }
    
    public static <TExpression> Bson sortByCount(final TExpression filter) {
        return new SortByCountStage<Object>(filter);
    }
    
    public static Bson skip(final int skip) {
        return new BsonDocument("$skip", new BsonInt32(skip));
    }
    
    public static Bson limit(final int limit) {
        return new BsonDocument("$limit", new BsonInt32(limit));
    }
    
    public static Bson lookup(final String from, final String localField, final String foreignField, final String as) {
        return new BsonDocument("$lookup", new BsonDocument("from", new BsonString(from)).append("localField", new BsonString(localField)).append("foreignField", new BsonString(foreignField)).append("as", new BsonString(as)));
    }
    
    public static Bson facet(final List<Facet> facets) {
        return new FacetStage(facets);
    }
    
    public static Bson facet(final Facet... facets) {
        return new FacetStage(Arrays.asList(facets));
    }
    
    public static <TExpression> Bson graphLookup(final String from, final TExpression startWith, final String connectFromField, final String connectToField, final String as) {
        return graphLookup(from, startWith, connectFromField, connectToField, as, new GraphLookupOptions());
    }
    
    public static <TExpression> Bson graphLookup(final String from, final TExpression startWith, final String connectFromField, final String connectToField, final String as, final GraphLookupOptions options) {
        Assertions.notNull("options", options);
        return new GraphLookupStage<Object>(from, (Object)startWith, connectFromField, connectToField, as, options);
    }
    
    public static <TExpression> Bson group(final TExpression id, final BsonField... fieldAccumulators) {
        return group(id, Arrays.asList(fieldAccumulators));
    }
    
    public static <TExpression> Bson group(final TExpression id, final List<BsonField> fieldAccumulators) {
        return new GroupStage<Object>(id, fieldAccumulators);
    }
    
    public static Bson unwind(final String fieldName) {
        return new BsonDocument("$unwind", new BsonString(fieldName));
    }
    
    public static Bson unwind(final String fieldName, final UnwindOptions unwindOptions) {
        Assertions.notNull("unwindOptions", unwindOptions);
        final BsonDocument options = new BsonDocument("path", new BsonString(fieldName));
        if (unwindOptions.isPreserveNullAndEmptyArrays() != null) {
            options.append("preserveNullAndEmptyArrays", BsonBoolean.valueOf(unwindOptions.isPreserveNullAndEmptyArrays()));
        }
        if (unwindOptions.getIncludeArrayIndex() != null) {
            options.append("includeArrayIndex", new BsonString(unwindOptions.getIncludeArrayIndex()));
        }
        return new BsonDocument("$unwind", options);
    }
    
    public static Bson out(final String collectionName) {
        return new BsonDocument("$out", new BsonString(collectionName));
    }
    
    public static <TExpression> Bson replaceRoot(final TExpression value) {
        return new ReplaceRootStage<Object>(value);
    }
    
    public static Bson sample(final int size) {
        return new BsonDocument("$sample", new BsonDocument("size", new BsonInt32(size)));
    }
    
    static void writeBucketOutput(final CodecRegistry codecRegistry, final BsonDocumentWriter writer, final List<BsonField> output) {
        if (output != null) {
            writer.writeName("output");
            writer.writeStartDocument();
            for (final BsonField field : output) {
                writer.writeName(field.getName());
                BuildersHelper.encodeValue(writer, field.getValue(), codecRegistry);
            }
            writer.writeEndDocument();
        }
    }
    
    private Aggregates() {
    }
    
    private static class SimplePipelineStage implements Bson
    {
        private final String name;
        private final Bson value;
        
        public SimplePipelineStage(final String name, final Bson value) {
            this.name = name;
            this.value = value;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            return new BsonDocument(this.name, this.value.toBsonDocument(documentClass, codecRegistry));
        }
        
        @Override
        public String toString() {
            return "Stage{name='" + this.name + '\'' + ", value=" + this.value + '}';
        }
    }
    
    private static final class BucketStage<TExpression, TBoundary> implements Bson
    {
        private final TExpression groupBy;
        private final List<TBoundary> boundaries;
        private final BucketOptions options;
        
        BucketStage(final TExpression groupBy, final List<TBoundary> boundaries, final BucketOptions options) {
            Assertions.notNull("options", options);
            this.groupBy = groupBy;
            this.boundaries = boundaries;
            this.options = options;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeStartDocument("$bucket");
            writer.writeName("groupBy");
            BuildersHelper.encodeValue(writer, this.groupBy, codecRegistry);
            writer.writeStartArray("boundaries");
            for (final TBoundary boundary : this.boundaries) {
                BuildersHelper.encodeValue(writer, boundary, codecRegistry);
            }
            writer.writeEndArray();
            if (this.options.getDefaultBucket() != null) {
                writer.writeName("default");
                BuildersHelper.encodeValue(writer, this.options.getDefaultBucket(), codecRegistry);
            }
            Aggregates.writeBucketOutput(codecRegistry, writer, this.options.getOutput());
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Stage{name='$bucket'boundaries=" + this.boundaries + ", groupBy=" + this.groupBy + ", options=" + this.options + '}';
        }
    }
    
    private static final class BucketAutoStage<TExpression> implements Bson
    {
        private final TExpression groupBy;
        private final int buckets;
        private final BucketAutoOptions options;
        
        BucketAutoStage(final TExpression groupBy, final int buckets, final BucketAutoOptions options) {
            Assertions.notNull("options", options);
            this.groupBy = groupBy;
            this.buckets = buckets;
            this.options = options;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeStartDocument("$bucketAuto");
            writer.writeName("groupBy");
            BuildersHelper.encodeValue(writer, this.groupBy, codecRegistry);
            writer.writeInt32("buckets", this.buckets);
            Aggregates.writeBucketOutput(codecRegistry, writer, this.options.getOutput());
            if (this.options.getGranularity() != null) {
                writer.writeString("granularity", this.options.getGranularity().getValue());
            }
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Stage{name='$bucketAuto'buckets=" + this.buckets + ", groupBy=" + this.groupBy + ", options=" + this.options + '}';
        }
    }
    
    private static final class GraphLookupStage<TExpression> implements Bson
    {
        private final String from;
        private final TExpression startWith;
        private final String connectFromField;
        private final String connectToField;
        private final String as;
        private final GraphLookupOptions options;
        
        private GraphLookupStage(final String from, final TExpression startWith, final String connectFromField, final String connectToField, final String as, final GraphLookupOptions options) {
            this.from = from;
            this.startWith = startWith;
            this.connectFromField = connectFromField;
            this.connectToField = connectToField;
            this.as = as;
            this.options = options;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeStartDocument("$graphLookup");
            writer.writeString("from", this.from);
            writer.writeName("startWith");
            BuildersHelper.encodeValue(writer, this.startWith, codecRegistry);
            writer.writeString("connectFromField", this.connectFromField);
            writer.writeString("connectToField", this.connectToField);
            writer.writeString("as", this.as);
            if (this.options.getMaxDepth() != null) {
                writer.writeInt32("maxDepth", this.options.getMaxDepth());
            }
            if (this.options.getDepthField() != null) {
                writer.writeString("depthField", this.options.getDepthField());
            }
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Stage{name='$graphLookup'as='" + this.as + '\'' + ", connectFromField='" + this.connectFromField + '\'' + ", connectToField='" + this.connectToField + '\'' + ", from='" + this.from + '\'' + ", options=" + this.options + ", startWith=" + this.startWith + '}';
        }
    }
    
    private static class GroupStage<TExpression> implements Bson
    {
        private final TExpression id;
        private final List<BsonField> fieldAccumulators;
        
        GroupStage(final TExpression id, final List<BsonField> fieldAccumulators) {
            this.id = id;
            this.fieldAccumulators = fieldAccumulators;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeStartDocument("$group");
            writer.writeName("_id");
            BuildersHelper.encodeValue(writer, this.id, codecRegistry);
            for (final BsonField fieldAccumulator : this.fieldAccumulators) {
                writer.writeName(fieldAccumulator.getName());
                BuildersHelper.encodeValue(writer, fieldAccumulator.getValue(), codecRegistry);
            }
            writer.writeEndDocument();
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Stage{name='$group', id=" + this.id + ", fieldAccumulators=" + this.fieldAccumulators + '}';
        }
    }
    
    private static class SortByCountStage<TExpression> implements Bson
    {
        private final TExpression filter;
        
        SortByCountStage(final TExpression filter) {
            this.filter = filter;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeName("$sortByCount");
            BuildersHelper.encodeValue(writer, this.filter, codecRegistry);
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Stage{name='$sortByCount', id=" + this.filter + '}';
        }
    }
    
    private static class FacetStage implements Bson
    {
        private final List<Facet> facets;
        
        FacetStage(final List<Facet> facets) {
            this.facets = facets;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeName("$facet");
            writer.writeStartDocument();
            for (final Facet facet : this.facets) {
                writer.writeName(facet.getName());
                writer.writeStartArray();
                for (final Bson bson : facet.getPipeline()) {
                    BuildersHelper.encodeValue(writer, bson, codecRegistry);
                }
                writer.writeEndArray();
            }
            writer.writeEndDocument();
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Stage{name='$fact', facets=" + this.facets + '}';
        }
    }
    
    private static class AddFieldsStage implements Bson
    {
        private final List<Field<?>> fields;
        
        AddFieldsStage(final List<Field<?>> fields) {
            this.fields = fields;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeName("$addFields");
            writer.writeStartDocument();
            for (final Field<?> field : this.fields) {
                writer.writeName(field.getName());
                BuildersHelper.encodeValue(writer, field.getValue(), codecRegistry);
            }
            writer.writeEndDocument();
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Stage{name='$addFields', fields=" + this.fields + '}';
        }
    }
    
    private static class ReplaceRootStage<TExpression> implements Bson
    {
        private final TExpression value;
        
        ReplaceRootStage(final TExpression value) {
            this.value = value;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeName("$replaceRoot");
            writer.writeStartDocument();
            writer.writeName("newRoot");
            BuildersHelper.encodeValue(writer, this.value, codecRegistry);
            writer.writeEndDocument();
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Stage{name='$replaceRoot', value=" + this.value + '}';
        }
    }
}
