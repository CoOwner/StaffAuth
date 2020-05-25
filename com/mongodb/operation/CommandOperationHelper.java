package com.mongodb.operation;

import org.bson.codecs.*;
import com.mongodb.internal.validator.*;
import com.mongodb.assertions.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.binding.*;
import com.mongodb.connection.*;
import org.bson.*;
import com.mongodb.*;

final class CommandOperationHelper
{
    static BsonDocument executeWrappedCommandProtocol(final ReadBinding binding, final String database, final BsonDocument command) {
        return executeWrappedCommandProtocol(binding, database, command, (Decoder<BsonDocument>)new BsonDocumentCodec());
    }
    
    static <T> T executeWrappedCommandProtocol(final ReadBinding binding, final String database, final BsonDocument command, final CommandTransformer<BsonDocument, T> transformer) {
        return executeWrappedCommandProtocol(binding, database, command, new BsonDocumentCodec(), transformer);
    }
    
    static <T> T executeWrappedCommandProtocol(final ReadBinding binding, final String database, final BsonDocument command, final Decoder<T> decoder) {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     1: aload_1         /* database */
        //     2: aload_2         /* command */
        //     3: aload_3         /* decoder */
        //     4: new             Lcom/mongodb/operation/CommandOperationHelper$IdentityTransformer;
        //     7: dup            
        //     8: invokespecial   com/mongodb/operation/CommandOperationHelper$IdentityTransformer.<init>:()V
        //    11: invokestatic    com/mongodb/operation/CommandOperationHelper.executeWrappedCommandProtocol:(Lcom/mongodb/binding/ReadBinding;Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/codecs/Decoder;Lcom/mongodb/operation/CommandOperationHelper$CommandTransformer;)Ljava/lang/Object;
        //    14: areturn        
        //    Signature:
        //  <T:Ljava/lang/Object;>(Lcom/mongodb/binding/ReadBinding;Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/codecs/Decoder<TT;>;)TT;
        // 
        // The error that occurred was:
        // 
        // com.strobel.assembler.metadata.MetadataHelper$AdaptFailure
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2300)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.GenericParameter.accept(GenericParameter.java:85)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2255)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2232)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2245)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.ParameterizedType.accept(ParameterizedType.java:103)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper.adapt(MetadataHelper.java:1312)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:932)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferCall(TypeAnalysis.java:2695)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:1029)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:778)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:1656)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:672)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:655)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:365)
        //     at com.strobel.decompiler.ast.TypeAnalysis.run(TypeAnalysis.java:96)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:109)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
        //     at us.deathmarine.luyten.FileSaver.access$300(FileSaver.java:45)
        //     at us.deathmarine.luyten.FileSaver$4.run(FileSaver.java:112)
        //     at java.lang.Thread.run(Unknown Source)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    
    static <D, T> T executeWrappedCommandProtocol(final ReadBinding binding, final String database, final BsonDocument command, final Decoder<D> decoder, final CommandTransformer<D, T> transformer) {
        final ConnectionSource source = binding.getReadConnectionSource();
        try {
            return transformer.apply(executeWrappedCommandProtocol(database, command, decoder, source, binding.getReadPreference()), source.getServerDescription().getAddress());
        }
        finally {
            source.release();
        }
    }
    
    static BsonDocument executeWrappedCommandProtocol(final ReadBinding binding, final String database, final BsonDocument command, final Connection connection) {
        return executeWrappedCommandProtocol(binding, database, command, connection, (CommandTransformer<BsonDocument, BsonDocument>)new IdentityTransformer<BsonDocument>());
    }
    
    static <T> T executeWrappedCommandProtocol(final ReadBinding binding, final String database, final BsonDocument command, final Connection connection, final CommandTransformer<BsonDocument, T> transformer) {
        return executeWrappedCommandProtocol(binding, database, command, new BsonDocumentCodec(), connection, transformer);
    }
    
    static <T> T executeWrappedCommandProtocol(final ReadBinding binding, final String database, final BsonDocument command, final Decoder<BsonDocument> decoder, final Connection connection, final CommandTransformer<BsonDocument, T> transformer) {
        return executeWrappedCommandProtocol(database, command, decoder, connection, binding.getReadPreference(), transformer);
    }
    
    static BsonDocument executeWrappedCommandProtocol(final WriteBinding binding, final String database, final BsonDocument command) {
        return executeWrappedCommandProtocol(binding, database, command, (CommandTransformer<BsonDocument, BsonDocument>)new IdentityTransformer<BsonDocument>());
    }
    
    static <T> T executeWrappedCommandProtocol(final WriteBinding binding, final String database, final BsonDocument command, final Decoder<T> decoder) {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     1: aload_1         /* database */
        //     2: aload_2         /* command */
        //     3: aload_3         /* decoder */
        //     4: new             Lcom/mongodb/operation/CommandOperationHelper$IdentityTransformer;
        //     7: dup            
        //     8: invokespecial   com/mongodb/operation/CommandOperationHelper$IdentityTransformer.<init>:()V
        //    11: invokestatic    com/mongodb/operation/CommandOperationHelper.executeWrappedCommandProtocol:(Lcom/mongodb/binding/WriteBinding;Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/codecs/Decoder;Lcom/mongodb/operation/CommandOperationHelper$CommandTransformer;)Ljava/lang/Object;
        //    14: areturn        
        //    Signature:
        //  <T:Ljava/lang/Object;>(Lcom/mongodb/binding/WriteBinding;Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/codecs/Decoder<TT;>;)TT;
        // 
        // The error that occurred was:
        // 
        // com.strobel.assembler.metadata.MetadataHelper$AdaptFailure
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2300)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.GenericParameter.accept(GenericParameter.java:85)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2255)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2232)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2245)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.ParameterizedType.accept(ParameterizedType.java:103)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper.adapt(MetadataHelper.java:1312)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:932)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferCall(TypeAnalysis.java:2695)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:1029)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:778)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:1656)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:672)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:655)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:365)
        //     at com.strobel.decompiler.ast.TypeAnalysis.run(TypeAnalysis.java:96)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:109)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
        //     at us.deathmarine.luyten.FileSaver.access$300(FileSaver.java:45)
        //     at us.deathmarine.luyten.FileSaver$4.run(FileSaver.java:112)
        //     at java.lang.Thread.run(Unknown Source)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    
    static <T> T executeWrappedCommandProtocol(final WriteBinding binding, final String database, final BsonDocument command, final CommandTransformer<BsonDocument, T> transformer) {
        return executeWrappedCommandProtocol(binding, database, command, new BsonDocumentCodec(), transformer);
    }
    
    static <D, T> T executeWrappedCommandProtocol(final WriteBinding binding, final String database, final BsonDocument command, final Decoder<D> decoder, final CommandTransformer<D, T> transformer) {
        return executeWrappedCommandProtocol(binding, database, command, new NoOpFieldNameValidator(), decoder, transformer);
    }
    
    static <T> T executeWrappedCommandProtocol(final WriteBinding binding, final String database, final BsonDocument command, final Connection connection, final CommandTransformer<BsonDocument, T> transformer) {
        return executeWrappedCommandProtocol(binding, database, command, new BsonDocumentCodec(), connection, transformer);
    }
    
    static <T> T executeWrappedCommandProtocol(final WriteBinding binding, final String database, final BsonDocument command, final Decoder<BsonDocument> decoder, final Connection connection, final CommandTransformer<BsonDocument, T> transformer) {
        Assertions.notNull("binding", binding);
        return executeWrappedCommandProtocol(database, command, decoder, connection, ReadPreference.primary(), transformer);
    }
    
    static <T> T executeWrappedCommandProtocol(final WriteBinding binding, final String database, final BsonDocument command, final FieldNameValidator fieldNameValidator, final Decoder<BsonDocument> decoder, final Connection connection, final CommandTransformer<BsonDocument, T> transformer) {
        Assertions.notNull("binding", binding);
        return executeWrappedCommandProtocol(database, command, fieldNameValidator, decoder, connection, ReadPreference.primary(), transformer);
    }
    
    static <D, T> T executeWrappedCommandProtocol(final WriteBinding binding, final String database, final BsonDocument command, final FieldNameValidator fieldNameValidator, final Decoder<D> decoder, final CommandTransformer<D, T> transformer) {
        final ConnectionSource source = binding.getWriteConnectionSource();
        try {
            return transformer.apply(executeWrappedCommandProtocol(database, command, fieldNameValidator, decoder, source, ReadPreference.primary()), source.getServerDescription().getAddress());
        }
        finally {
            source.release();
        }
    }
    
    static BsonDocument executeWrappedCommandProtocol(final WriteBinding binding, final String database, final BsonDocument command, final Connection connection) {
        Assertions.notNull("binding", binding);
        return executeWrappedCommandProtocol(database, command, (Decoder<BsonDocument>)new BsonDocumentCodec(), connection, ReadPreference.primary());
    }
    
    private static <T> T executeWrappedCommandProtocol(final String database, final BsonDocument command, final Decoder<T> decoder, final ConnectionSource source, final ReadPreference readPreference) {
        return executeWrappedCommandProtocol(database, command, new NoOpFieldNameValidator(), decoder, source, readPreference);
    }
    
    private static <T> T executeWrappedCommandProtocol(final String database, final BsonDocument command, final FieldNameValidator fieldNameValidator, final Decoder<T> decoder, final ConnectionSource source, final ReadPreference readPreference) {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     2: invokeinterface com/mongodb/binding/ConnectionSource.getConnection:()Lcom/mongodb/connection/Connection;
        //     7: astore          connection
        //     9: aload_0         /* database */
        //    10: aload_1         /* command */
        //    11: aload_2         /* fieldNameValidator */
        //    12: aload_3         /* decoder */
        //    13: aload           connection
        //    15: aload           readPreference
        //    17: new             Lcom/mongodb/operation/CommandOperationHelper$IdentityTransformer;
        //    20: dup            
        //    21: invokespecial   com/mongodb/operation/CommandOperationHelper$IdentityTransformer.<init>:()V
        //    24: invokestatic    com/mongodb/operation/CommandOperationHelper.executeWrappedCommandProtocol:(Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/FieldNameValidator;Lorg/bson/codecs/Decoder;Lcom/mongodb/connection/Connection;Lcom/mongodb/ReadPreference;Lcom/mongodb/operation/CommandOperationHelper$CommandTransformer;)Ljava/lang/Object;
        //    27: astore          7
        //    29: aload           connection
        //    31: invokeinterface com/mongodb/connection/Connection.release:()V
        //    36: aload           7
        //    38: areturn        
        //    39: astore          8
        //    41: aload           connection
        //    43: invokeinterface com/mongodb/connection/Connection.release:()V
        //    48: aload           8
        //    50: athrow         
        //    Signature:
        //  <T:Ljava/lang/Object;>(Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/FieldNameValidator;Lorg/bson/codecs/Decoder<TT;>;Lcom/mongodb/binding/ConnectionSource;Lcom/mongodb/ReadPreference;)TT;
        //    StackMapTable: 00 01 FF 00 27 00 07 07 00 71 07 00 72 07 00 92 07 00 73 07 00 75 07 00 9A 07 00 9B 00 01 07 00 76
        //    Exceptions:
        //  Try           Handler
        //  Start  End    Start  End    Type
        //  -----  -----  -----  -----  ----
        //  9      29     39     51     Any
        //  39     41     39     51     Any
        // 
        // The error that occurred was:
        // 
        // com.strobel.assembler.metadata.MetadataHelper$AdaptFailure
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2300)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.GenericParameter.accept(GenericParameter.java:85)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2255)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2232)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2245)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.ParameterizedType.accept(ParameterizedType.java:103)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper.adapt(MetadataHelper.java:1312)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:932)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferCall(TypeAnalysis.java:2695)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:1029)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:770)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:881)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:672)
        //     at com.strobel.decompiler.ast.TypeAnalysis.invalidateDependentExpressions(TypeAnalysis.java:759)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:1011)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:778)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:1656)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:672)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:655)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:365)
        //     at com.strobel.decompiler.ast.TypeAnalysis.run(TypeAnalysis.java:96)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:109)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
        //     at us.deathmarine.luyten.FileSaver.access$300(FileSaver.java:45)
        //     at us.deathmarine.luyten.FileSaver$4.run(FileSaver.java:112)
        //     at java.lang.Thread.run(Unknown Source)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    
    private static <T> T executeWrappedCommandProtocol(final String database, final BsonDocument command, final Decoder<T> decoder, final Connection connection, final ReadPreference readPreference) {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     1: aload_1         /* command */
        //     2: new             Lcom/mongodb/internal/validator/NoOpFieldNameValidator;
        //     5: dup            
        //     6: invokespecial   com/mongodb/internal/validator/NoOpFieldNameValidator.<init>:()V
        //     9: aload_2         /* decoder */
        //    10: aload_3         /* connection */
        //    11: aload           readPreference
        //    13: new             Lcom/mongodb/operation/CommandOperationHelper$IdentityTransformer;
        //    16: dup            
        //    17: invokespecial   com/mongodb/operation/CommandOperationHelper$IdentityTransformer.<init>:()V
        //    20: invokestatic    com/mongodb/operation/CommandOperationHelper.executeWrappedCommandProtocol:(Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/FieldNameValidator;Lorg/bson/codecs/Decoder;Lcom/mongodb/connection/Connection;Lcom/mongodb/ReadPreference;Lcom/mongodb/operation/CommandOperationHelper$CommandTransformer;)Ljava/lang/Object;
        //    23: areturn        
        //    Signature:
        //  <T:Ljava/lang/Object;>(Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/codecs/Decoder<TT;>;Lcom/mongodb/connection/Connection;Lcom/mongodb/ReadPreference;)TT;
        // 
        // The error that occurred was:
        // 
        // com.strobel.assembler.metadata.MetadataHelper$AdaptFailure
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2300)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.GenericParameter.accept(GenericParameter.java:85)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2255)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2232)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2245)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.ParameterizedType.accept(ParameterizedType.java:103)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper.adapt(MetadataHelper.java:1312)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:932)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferCall(TypeAnalysis.java:2695)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:1029)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:778)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:1656)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:672)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:655)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:365)
        //     at com.strobel.decompiler.ast.TypeAnalysis.run(TypeAnalysis.java:96)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:109)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
        //     at us.deathmarine.luyten.FileSaver.access$300(FileSaver.java:45)
        //     at us.deathmarine.luyten.FileSaver$4.run(FileSaver.java:112)
        //     at java.lang.Thread.run(Unknown Source)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    
    private static <D, T> T executeWrappedCommandProtocol(final String database, final BsonDocument command, final Decoder<D> decoder, final Connection connection, final ReadPreference readPreference, final CommandTransformer<D, T> transformer) {
        return executeWrappedCommandProtocol(database, command, new NoOpFieldNameValidator(), decoder, connection, readPreference, transformer);
    }
    
    private static <D, T> T executeWrappedCommandProtocol(final String database, final BsonDocument command, final FieldNameValidator fieldNameValidator, final Decoder<D> decoder, final Connection connection, final ReadPreference readPreference, final CommandTransformer<D, T> transformer) {
        return transformer.apply(connection.command(database, wrapCommand(command, readPreference, connection.getDescription()), readPreference.isSlaveOk(), fieldNameValidator, decoder), connection.getDescription().getServerAddress());
    }
    
    static void executeWrappedCommandProtocolAsync(final AsyncReadBinding binding, final String database, final BsonDocument command, final SingleResultCallback<BsonDocument> callback) {
        executeWrappedCommandProtocolAsync(binding, database, command, new BsonDocumentCodec(), callback);
    }
    
    static <T> void executeWrappedCommandProtocolAsync(final AsyncReadBinding binding, final String database, final BsonDocument command, final Decoder<T> decoder, final SingleResultCallback<T> callback) {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     1: aload_1         /* database */
        //     2: aload_2         /* command */
        //     3: aload_3         /* decoder */
        //     4: new             Lcom/mongodb/operation/CommandOperationHelper$IdentityTransformer;
        //     7: dup            
        //     8: invokespecial   com/mongodb/operation/CommandOperationHelper$IdentityTransformer.<init>:()V
        //    11: aload           callback
        //    13: invokestatic    com/mongodb/operation/CommandOperationHelper.executeWrappedCommandProtocolAsync:(Lcom/mongodb/binding/AsyncReadBinding;Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/codecs/Decoder;Lcom/mongodb/operation/CommandOperationHelper$CommandTransformer;Lcom/mongodb/async/SingleResultCallback;)V
        //    16: return         
        //    Signature:
        //  <T:Ljava/lang/Object;>(Lcom/mongodb/binding/AsyncReadBinding;Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/codecs/Decoder<TT;>;Lcom/mongodb/async/SingleResultCallback<TT;>;)V
        // 
        // The error that occurred was:
        // 
        // com.strobel.assembler.metadata.MetadataHelper$AdaptFailure
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2300)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.GenericParameter.accept(GenericParameter.java:85)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2255)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2232)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2245)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.ParameterizedType.accept(ParameterizedType.java:103)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper.adapt(MetadataHelper.java:1312)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:932)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferCall(TypeAnalysis.java:2695)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:1029)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:672)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:655)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:365)
        //     at com.strobel.decompiler.ast.TypeAnalysis.run(TypeAnalysis.java:96)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:109)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
        //     at us.deathmarine.luyten.FileSaver.access$300(FileSaver.java:45)
        //     at us.deathmarine.luyten.FileSaver$4.run(FileSaver.java:112)
        //     at java.lang.Thread.run(Unknown Source)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    
    static <T> void executeWrappedCommandProtocolAsync(final AsyncReadBinding binding, final String database, final BsonDocument command, final CommandTransformer<BsonDocument, T> transformer, final SingleResultCallback<T> callback) {
        executeWrappedCommandProtocolAsync(binding, database, command, new BsonDocumentCodec(), transformer, callback);
    }
    
    static <D, T> void executeWrappedCommandProtocolAsync(final AsyncReadBinding binding, final String database, final BsonDocument command, final Decoder<D> decoder, final CommandTransformer<D, T> transformer, final SingleResultCallback<T> callback) {
        binding.getReadConnectionSource(new CommandProtocolExecutingCallback<Object, Object>(database, command, new NoOpFieldNameValidator(), decoder, binding.getReadPreference(), transformer, ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER)));
    }
    
    static <T> void executeWrappedCommandProtocolAsync(final AsyncReadBinding binding, final String database, final BsonDocument command, final AsyncConnection connection, final CommandTransformer<BsonDocument, T> transformer, final SingleResultCallback<T> callback) {
        executeWrappedCommandProtocolAsync(binding, database, command, new BsonDocumentCodec(), connection, transformer, callback);
    }
    
    static <T> void executeWrappedCommandProtocolAsync(final AsyncReadBinding binding, final String database, final BsonDocument command, final Decoder<BsonDocument> decoder, final AsyncConnection connection, final CommandTransformer<BsonDocument, T> transformer, final SingleResultCallback<T> callback) {
        Assertions.notNull("binding", binding);
        executeWrappedCommandProtocolAsync(database, command, decoder, connection, binding.getReadPreference(), transformer, callback);
    }
    
    static void executeWrappedCommandProtocolAsync(final AsyncWriteBinding binding, final String database, final BsonDocument command, final SingleResultCallback<BsonDocument> callback) {
        executeWrappedCommandProtocolAsync(binding, database, command, new BsonDocumentCodec(), callback);
    }
    
    static <T> void executeWrappedCommandProtocolAsync(final AsyncWriteBinding binding, final String database, final BsonDocument command, final Decoder<T> decoder, final SingleResultCallback<T> callback) {
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     1: aload_1         /* database */
        //     2: aload_2         /* command */
        //     3: aload_3         /* decoder */
        //     4: new             Lcom/mongodb/operation/CommandOperationHelper$IdentityTransformer;
        //     7: dup            
        //     8: invokespecial   com/mongodb/operation/CommandOperationHelper$IdentityTransformer.<init>:()V
        //    11: aload           callback
        //    13: invokestatic    com/mongodb/operation/CommandOperationHelper.executeWrappedCommandProtocolAsync:(Lcom/mongodb/binding/AsyncWriteBinding;Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/codecs/Decoder;Lcom/mongodb/operation/CommandOperationHelper$CommandTransformer;Lcom/mongodb/async/SingleResultCallback;)V
        //    16: return         
        //    Signature:
        //  <T:Ljava/lang/Object;>(Lcom/mongodb/binding/AsyncWriteBinding;Ljava/lang/String;Lorg/bson/BsonDocument;Lorg/bson/codecs/Decoder<TT;>;Lcom/mongodb/async/SingleResultCallback<TT;>;)V
        // 
        // The error that occurred was:
        // 
        // com.strobel.assembler.metadata.MetadataHelper$AdaptFailure
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2300)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitGenericParameter(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.GenericParameter.accept(GenericParameter.java:85)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2255)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.adaptRecursive(MetadataHelper.java:2232)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2245)
        //     at com.strobel.assembler.metadata.MetadataHelper$Adapter.visitParameterizedType(MetadataHelper.java:2221)
        //     at com.strobel.assembler.metadata.ParameterizedType.accept(ParameterizedType.java:103)
        //     at com.strobel.assembler.metadata.DefaultTypeVisitor.visit(DefaultTypeVisitor.java:25)
        //     at com.strobel.assembler.metadata.MetadataHelper.adapt(MetadataHelper.java:1312)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:932)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferCall(TypeAnalysis.java:2695)
        //     at com.strobel.decompiler.ast.TypeAnalysis.doInferTypeForExpression(TypeAnalysis.java:1029)
        //     at com.strobel.decompiler.ast.TypeAnalysis.inferTypeForExpression(TypeAnalysis.java:803)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:672)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:655)
        //     at com.strobel.decompiler.ast.TypeAnalysis.runInference(TypeAnalysis.java:365)
        //     at com.strobel.decompiler.ast.TypeAnalysis.run(TypeAnalysis.java:96)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:109)
        //     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:782)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:675)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:552)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:519)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:161)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:150)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:125)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
        //     at us.deathmarine.luyten.FileSaver.access$300(FileSaver.java:45)
        //     at us.deathmarine.luyten.FileSaver$4.run(FileSaver.java:112)
        //     at java.lang.Thread.run(Unknown Source)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    
    static <T> void executeWrappedCommandProtocolAsync(final AsyncWriteBinding binding, final String database, final BsonDocument command, final CommandTransformer<BsonDocument, T> transformer, final SingleResultCallback<T> callback) {
        executeWrappedCommandProtocolAsync(binding, database, command, new BsonDocumentCodec(), transformer, callback);
    }
    
    static <D, T> void executeWrappedCommandProtocolAsync(final AsyncWriteBinding binding, final String database, final BsonDocument command, final Decoder<D> decoder, final CommandTransformer<D, T> transformer, final SingleResultCallback<T> callback) {
        executeWrappedCommandProtocolAsync(binding, database, command, new NoOpFieldNameValidator(), decoder, transformer, callback);
    }
    
    static <T> void executeWrappedCommandProtocolAsync(final AsyncWriteBinding binding, final String database, final BsonDocument command, final Decoder<BsonDocument> decoder, final AsyncConnection connection, final CommandTransformer<BsonDocument, T> transformer, final SingleResultCallback<T> callback) {
        Assertions.notNull("binding", binding);
        executeWrappedCommandProtocolAsync(database, command, decoder, connection, ReadPreference.primary(), transformer, callback);
    }
    
    static <T> void executeWrappedCommandProtocolAsync(final AsyncWriteBinding binding, final String database, final BsonDocument command, final FieldNameValidator fieldNameValidator, final Decoder<BsonDocument> decoder, final AsyncConnection connection, final CommandTransformer<BsonDocument, T> transformer, final SingleResultCallback<T> callback) {
        Assertions.notNull("binding", binding);
        executeWrappedCommandProtocolAsync(database, command, fieldNameValidator, decoder, connection, ReadPreference.primary(), transformer, callback);
    }
    
    static <D, T> void executeWrappedCommandProtocolAsync(final AsyncWriteBinding binding, final String database, final BsonDocument command, final FieldNameValidator fieldNameValidator, final Decoder<D> decoder, final CommandTransformer<D, T> transformer, final SingleResultCallback<T> callback) {
        binding.getWriteConnectionSource(new CommandProtocolExecutingCallback<Object, Object>(database, command, fieldNameValidator, decoder, ReadPreference.primary(), transformer, ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER)));
    }
    
    static void executeWrappedCommandProtocolAsync(final AsyncWriteBinding binding, final String database, final BsonDocument command, final AsyncConnection connection, final SingleResultCallback<BsonDocument> callback) {
        executeWrappedCommandProtocolAsync(binding, database, command, connection, new IdentityTransformer<BsonDocument>(), callback);
    }
    
    static <T> void executeWrappedCommandProtocolAsync(final AsyncWriteBinding binding, final String database, final BsonDocument command, final AsyncConnection connection, final CommandTransformer<BsonDocument, T> transformer, final SingleResultCallback<T> callback) {
        Assertions.notNull("binding", binding);
        executeWrappedCommandProtocolAsync(database, command, new BsonDocumentCodec(), connection, ReadPreference.primary(), transformer, callback);
    }
    
    private static <D, T> void executeWrappedCommandProtocolAsync(final String database, final BsonDocument command, final Decoder<D> decoder, final AsyncConnection connection, final ReadPreference readPreference, final CommandTransformer<D, T> transformer, final SingleResultCallback<T> callback) {
        executeWrappedCommandProtocolAsync(database, command, new NoOpFieldNameValidator(), decoder, connection, readPreference, transformer, callback);
    }
    
    private static <D, T> void executeWrappedCommandProtocolAsync(final String database, final BsonDocument command, final FieldNameValidator fieldNameValidator, final Decoder<D> decoder, final AsyncConnection connection, final ReadPreference readPreference, final CommandTransformer<D, T> transformer, final SingleResultCallback<T> callback) {
        connection.commandAsync(database, wrapCommand(command, readPreference, connection.getDescription()), readPreference.isSlaveOk(), fieldNameValidator, decoder, new SingleResultCallback<D>() {
            @Override
            public void onResult(final D result, final Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                }
                else {
                    try {
                        final T transformedResult = transformer.apply(result, connection.getDescription().getServerAddress());
                        callback.onResult(transformedResult, null);
                    }
                    catch (Exception e) {
                        callback.onResult(null, e);
                    }
                }
            }
        });
    }
    
    static void rethrowIfNotNamespaceError(final MongoCommandException e) {
        rethrowIfNotNamespaceError(e, (Object)null);
    }
    
    static <T> T rethrowIfNotNamespaceError(final MongoCommandException e, final T defaultValue) {
        if (!isNamespaceError(e)) {
            throw e;
        }
        return defaultValue;
    }
    
    static boolean isNamespaceError(final Throwable t) {
        if (t instanceof MongoCommandException) {
            final MongoCommandException e = (MongoCommandException)t;
            return e.getErrorMessage().contains("ns not found") || e.getErrorCode() == 26;
        }
        return false;
    }
    
    static BsonDocument wrapCommand(final BsonDocument command, final ReadPreference readPreference, final ConnectionDescription connectionDescription) {
        if (connectionDescription.getServerType() == ServerType.SHARD_ROUTER && !readPreference.equals(ReadPreference.primary())) {
            return new BsonDocument("$query", command).append("$readPreference", readPreference.toDocument());
        }
        return command;
    }
    
    private CommandOperationHelper() {
    }
    
    static class IdentityTransformer<T> implements CommandTransformer<T, T>
    {
        @Override
        public T apply(final T t, final ServerAddress serverAddress) {
            return t;
        }
    }
    
    static class VoidTransformer<T> implements CommandTransformer<T, Void>
    {
        @Override
        public Void apply(final T t, final ServerAddress serverAddress) {
            return null;
        }
    }
    
    private static class CommandProtocolExecutingCallback<D, R> implements SingleResultCallback<AsyncConnectionSource>
    {
        private final String database;
        private final BsonDocument command;
        private final Decoder<D> decoder;
        private final ReadPreference readPreference;
        private final FieldNameValidator fieldNameValidator;
        private final CommandTransformer<D, R> transformer;
        private final SingleResultCallback<R> callback;
        
        public CommandProtocolExecutingCallback(final String database, final BsonDocument command, final FieldNameValidator fieldNameValidator, final Decoder<D> decoder, final ReadPreference readPreference, final CommandTransformer<D, R> transformer, final SingleResultCallback<R> callback) {
            this.database = database;
            this.command = command;
            this.fieldNameValidator = fieldNameValidator;
            this.decoder = decoder;
            this.readPreference = readPreference;
            this.transformer = transformer;
            this.callback = callback;
        }
        
        @Override
        public void onResult(final AsyncConnectionSource source, final Throwable t) {
            if (t != null) {
                this.callback.onResult(null, t);
            }
            else {
                source.getConnection(new SingleResultCallback<AsyncConnection>() {
                    @Override
                    public void onResult(final AsyncConnection connection, final Throwable t) {
                        if (t != null) {
                            CommandProtocolExecutingCallback.this.callback.onResult(null, t);
                        }
                        else {
                            final SingleResultCallback<R> wrappedCallback = OperationHelper.releasingCallback(CommandProtocolExecutingCallback.this.callback, source, connection);
                            connection.commandAsync(CommandProtocolExecutingCallback.this.database, CommandOperationHelper.wrapCommand(CommandProtocolExecutingCallback.this.command, CommandProtocolExecutingCallback.this.readPreference, connection.getDescription()), CommandProtocolExecutingCallback.this.readPreference.isSlaveOk(), CommandProtocolExecutingCallback.this.fieldNameValidator, CommandProtocolExecutingCallback.this.decoder, (SingleResultCallback<Object>)new SingleResultCallback<D>() {
                                @Override
                                public void onResult(final D response, final Throwable t) {
                                    if (t != null) {
                                        wrappedCallback.onResult(null, t);
                                    }
                                    else {
                                        wrappedCallback.onResult(CommandProtocolExecutingCallback.this.transformer.apply(response, connection.getDescription().getServerAddress()), null);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }
    
    interface CommandTransformer<T, R>
    {
        R apply(final T p0, final ServerAddress p1);
    }
}
