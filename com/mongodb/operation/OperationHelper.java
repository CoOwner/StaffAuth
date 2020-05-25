package com.mongodb.operation;

import com.mongodb.client.model.*;
import com.mongodb.bulk.*;
import org.bson.codecs.*;
import com.mongodb.*;
import org.bson.*;
import com.mongodb.async.*;
import java.util.*;
import com.mongodb.connection.*;
import com.mongodb.binding.*;
import com.mongodb.internal.async.*;
import com.mongodb.diagnostics.logging.*;
import com.mongodb.assertions.*;

final class OperationHelper
{
    public static final Logger LOGGER;
    
    static void validateReadConcern(final Connection connection, final ReadConcern readConcern) {
        if (!serverIsAtLeastVersionThreeDotTwo(connection.getDescription()) && !readConcern.isServerDefault()) {
            throw new IllegalArgumentException(String.format("ReadConcern not supported by server version: %s", connection.getDescription().getServerVersion()));
        }
    }
    
    static void validateReadConcern(final AsyncConnection connection, final ReadConcern readConcern, final AsyncCallableWithConnection callable) {
        Throwable throwable = null;
        if (!serverIsAtLeastVersionThreeDotTwo(connection.getDescription()) && !readConcern.isServerDefault()) {
            throwable = new IllegalArgumentException(String.format("ReadConcern not supported by server version: %s", connection.getDescription().getServerVersion()));
        }
        callable.call(connection, throwable);
    }
    
    static void validateReadConcern(final AsyncConnectionSource source, final AsyncConnection connection, final ReadConcern readConcern, final AsyncCallableWithConnectionAndSource callable) {
        validateReadConcern(connection, readConcern, new AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                callable.call(source, connection, t);
            }
        });
    }
    
    static void validateCollation(final Connection connection, final Collation collation) {
        if (!serverIsAtLeastVersionThreeDotFour(connection.getDescription()) && collation != null) {
            throw new IllegalArgumentException(String.format("Collation not supported by server version: %s", connection.getDescription().getServerVersion()));
        }
    }
    
    static void validateCollationAndWriteConcern(final Connection connection, final Collation collation, final WriteConcern writeConcern) {
        if (!serverIsAtLeastVersionThreeDotFour(connection.getDescription()) && collation != null) {
            throw new IllegalArgumentException(String.format("Collation not supported by server version: %s", connection.getDescription().getServerVersion()));
        }
        if (collation != null && !writeConcern.isAcknowledged()) {
            throw new MongoClientException("Specifying collation with an unacknowledged WriteConcern is not supported");
        }
    }
    
    static void validateCollation(final AsyncConnection connection, final Collation collation, final AsyncCallableWithConnection callable) {
        Throwable throwable = null;
        if (!serverIsAtLeastVersionThreeDotFour(connection.getDescription()) && collation != null) {
            throwable = new IllegalArgumentException(String.format("Collation not supported by server version: %s", connection.getDescription().getServerVersion()));
        }
        callable.call(connection, throwable);
    }
    
    static void validateCollationAndWriteConcern(final AsyncConnection connection, final Collation collation, final WriteConcern writeConcern, final AsyncCallableWithConnection callable) {
        Throwable throwable = null;
        if (!serverIsAtLeastVersionThreeDotFour(connection.getDescription()) && collation != null) {
            throwable = new IllegalArgumentException(String.format("Collation not supported by server version: %s", connection.getDescription().getServerVersion()));
        }
        else if (collation != null && !writeConcern.isAcknowledged()) {
            throwable = new MongoClientException("Specifying collation with an unacknowledged WriteConcern is not supported");
        }
        callable.call(connection, throwable);
    }
    
    static void validateCollation(final AsyncConnectionSource source, final AsyncConnection connection, final Collation collation, final AsyncCallableWithConnectionAndSource callable) {
        validateCollation(connection, collation, new AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                callable.call(source, connection, t);
            }
        });
    }
    
    static void validateWriteRequestCollations(final Connection connection, final List<? extends WriteRequest> requests, final WriteConcern writeConcern) {
        Collation collation = null;
        for (final WriteRequest request : requests) {
            if (request instanceof UpdateRequest) {
                collation = ((UpdateRequest)request).getCollation();
            }
            else if (request instanceof DeleteRequest) {
                collation = ((DeleteRequest)request).getCollation();
            }
            if (collation != null) {
                break;
            }
        }
        validateCollationAndWriteConcern(connection, collation, writeConcern);
    }
    
    static void validateWriteRequestCollations(final AsyncConnection connection, final List<? extends WriteRequest> requests, final WriteConcern writeConcern, final AsyncCallableWithConnection callable) {
        Collation collation = null;
        for (final WriteRequest request : requests) {
            if (request instanceof UpdateRequest) {
                collation = ((UpdateRequest)request).getCollation();
            }
            else if (request instanceof DeleteRequest) {
                collation = ((DeleteRequest)request).getCollation();
            }
            if (collation != null) {
                break;
            }
        }
        validateCollationAndWriteConcern(connection, collation, writeConcern, new AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                callable.call(connection, t);
            }
        });
    }
    
    static void validateWriteRequests(final Connection connection, final Boolean bypassDocumentValidation, final List<? extends WriteRequest> requests, final WriteConcern writeConcern) {
        checkBypassDocumentValidationIsSupported(connection, bypassDocumentValidation, writeConcern);
        validateWriteRequestCollations(connection, requests, writeConcern);
    }
    
    static void validateWriteRequests(final AsyncConnection connection, final Boolean bypassDocumentValidation, final List<? extends WriteRequest> requests, final WriteConcern writeConcern, final AsyncCallableWithConnection callable) {
        checkBypassDocumentValidationIsSupported(connection, bypassDocumentValidation, writeConcern, new AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                if (t != null) {
                    callable.call(connection, t);
                }
                else {
                    OperationHelper.validateWriteRequestCollations(connection, requests, writeConcern, callable);
                }
            }
        });
    }
    
    static void validateIndexRequestCollations(final Connection connection, final List<IndexRequest> requests) {
        for (final IndexRequest request : requests) {
            if (request.getCollation() != null) {
                validateCollation(connection, request.getCollation());
                break;
            }
        }
    }
    
    static void validateIndexRequestCollations(final AsyncConnection connection, final List<IndexRequest> requests, final AsyncCallableWithConnection callable) {
        boolean calledTheCallable = false;
        for (final IndexRequest request : requests) {
            if (request.getCollation() != null) {
                calledTheCallable = true;
                validateCollation(connection, request.getCollation(), new AsyncCallableWithConnection() {
                    @Override
                    public void call(final AsyncConnection connection, final Throwable t) {
                        callable.call(connection, t);
                    }
                });
                break;
            }
        }
        if (!calledTheCallable) {
            callable.call(connection, null);
        }
    }
    
    static void validateReadConcernAndCollation(final Connection connection, final ReadConcern readConcern, final Collation collation) {
        validateReadConcern(connection, readConcern);
        validateCollation(connection, collation);
    }
    
    static void validateReadConcernAndCollation(final AsyncConnection connection, final ReadConcern readConcern, final Collation collation, final AsyncCallableWithConnection callable) {
        validateReadConcern(connection, readConcern, new AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                if (t != null) {
                    callable.call(connection, t);
                }
                else {
                    OperationHelper.validateCollation(connection, collation, callable);
                }
            }
        });
    }
    
    static void validateReadConcernAndCollation(final AsyncConnectionSource source, final AsyncConnection connection, final ReadConcern readConcern, final Collation collation, final AsyncCallableWithConnectionAndSource callable) {
        validateReadConcernAndCollation(connection, readConcern, collation, new AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                callable.call(source, connection, t);
            }
        });
    }
    
    static void checkBypassDocumentValidationIsSupported(final Connection connection, final Boolean bypassDocumentValidation, final WriteConcern writeConcern) {
        if (bypassDocumentValidation != null && serverIsAtLeastVersionThreeDotTwo(connection.getDescription()) && !writeConcern.isAcknowledged()) {
            throw new MongoClientException("Specifying bypassDocumentValidation with an unacknowledged WriteConcern is not supported");
        }
    }
    
    static void checkBypassDocumentValidationIsSupported(final AsyncConnection connection, final Boolean bypassDocumentValidation, final WriteConcern writeConcern, final AsyncCallableWithConnection callable) {
        Throwable throwable = null;
        if (bypassDocumentValidation != null && serverIsAtLeastVersionThreeDotTwo(connection.getDescription()) && !writeConcern.isAcknowledged()) {
            throwable = new MongoClientException("Specifying bypassDocumentValidation with an unacknowledged WriteConcern is not supported");
        }
        callable.call(connection, throwable);
    }
    
    static <T> QueryBatchCursor<T> createEmptyBatchCursor(final MongoNamespace namespace, final Decoder<T> decoder, final ServerAddress serverAddress, final int batchSize) {
        return new QueryBatchCursor<T>(new QueryResult<T>(namespace, Collections.emptyList(), 0L, serverAddress), 0, batchSize, decoder);
    }
    
    static <T> AsyncBatchCursor<T> createEmptyAsyncBatchCursor(final MongoNamespace namespace, final Decoder<T> decoder, final ServerAddress serverAddress, final int batchSize) {
        return new AsyncQueryBatchCursor<T>(new QueryResult<T>(namespace, Collections.emptyList(), 0L, serverAddress), 0, batchSize, decoder);
    }
    
    static <T> BatchCursor<T> cursorDocumentToBatchCursor(final BsonDocument cursorDocument, final Decoder<T> decoder, final ConnectionSource source, final int batchSize) {
        return new QueryBatchCursor<T>(cursorDocumentToQueryResult(cursorDocument, source.getServerDescription().getAddress()), 0, batchSize, decoder, source);
    }
    
    static <T> AsyncBatchCursor<T> cursorDocumentToAsyncBatchCursor(final BsonDocument cursorDocument, final Decoder<T> decoder, final AsyncConnectionSource source, final AsyncConnection connection, final int batchSize) {
        return new AsyncQueryBatchCursor<T>(cursorDocumentToQueryResult(cursorDocument, source.getServerDescription().getAddress()), 0, batchSize, 0L, decoder, source, connection);
    }
    
    static <T> QueryResult<T> cursorDocumentToQueryResult(final BsonDocument cursorDocument, final ServerAddress serverAddress) {
        return cursorDocumentToQueryResult(cursorDocument, serverAddress, "firstBatch");
    }
    
    static <T> QueryResult<T> getMoreCursorDocumentToQueryResult(final BsonDocument cursorDocument, final ServerAddress serverAddress) {
        return cursorDocumentToQueryResult(cursorDocument, serverAddress, "nextBatch");
    }
    
    private static <T> QueryResult<T> cursorDocumentToQueryResult(final BsonDocument cursorDocument, final ServerAddress serverAddress, final String fieldNameContainingBatch) {
        final long cursorId = ((BsonInt64)cursorDocument.get("id")).getValue();
        final MongoNamespace queryResultNamespace = new MongoNamespace(cursorDocument.getString("ns").getValue());
        return new QueryResult<T>(queryResultNamespace, BsonDocumentWrapperHelper.toList(cursorDocument, fieldNameContainingBatch), cursorId, serverAddress);
    }
    
    static <T> SingleResultCallback<T> releasingCallback(final SingleResultCallback<T> wrapped, final AsyncConnection connection) {
        return new ReferenceCountedReleasingWrappedCallback<T>(wrapped, Collections.singletonList((ReferenceCounted)connection));
    }
    
    static <T> SingleResultCallback<T> releasingCallback(final SingleResultCallback<T> wrapped, final AsyncConnectionSource source, final AsyncConnection connection) {
        return new ReferenceCountedReleasingWrappedCallback<T>(wrapped, Arrays.asList(connection, source));
    }
    
    static <T> SingleResultCallback<T> releasingCallback(final SingleResultCallback<T> wrapped, final AsyncReadBinding readBinding, final AsyncConnectionSource source, final AsyncConnection connection) {
        return new ReferenceCountedReleasingWrappedCallback<T>(wrapped, Arrays.asList(readBinding, connection, source));
    }
    
    static boolean serverIsAtLeastVersionTwoDotSix(final ConnectionDescription description) {
        return serverIsAtLeastVersion(description, new ServerVersion(2, 6));
    }
    
    static boolean serverIsAtLeastVersionThreeDotZero(final ConnectionDescription description) {
        return serverIsAtLeastVersion(description, new ServerVersion(3, 0));
    }
    
    static boolean serverIsAtLeastVersionThreeDotTwo(final ConnectionDescription description) {
        return serverIsAtLeastVersion(description, new ServerVersion(3, 2));
    }
    
    static boolean serverIsAtLeastVersionThreeDotFour(final ConnectionDescription description) {
        return serverIsAtLeastVersion(description, new ServerVersion(3, 4));
    }
    
    static boolean serverIsAtLeastVersion(final ConnectionDescription description, final ServerVersion serverVersion) {
        return description.getServerVersion().compareTo(serverVersion) >= 0;
    }
    
    static <T> T withConnection(final ReadBinding binding, final CallableWithConnection<T> callable) {
        final ConnectionSource source = binding.getReadConnectionSource();
        try {
            return withConnectionSource(source, callable);
        }
        finally {
            source.release();
        }
    }
    
    static <T> T withConnection(final ReadBinding binding, final CallableWithConnectionAndSource<T> callable) {
        final ConnectionSource source = binding.getReadConnectionSource();
        try {
            return withConnectionSource(source, callable);
        }
        finally {
            source.release();
        }
    }
    
    static <T> T withConnection(final WriteBinding binding, final CallableWithConnection<T> callable) {
        final ConnectionSource source = binding.getWriteConnectionSource();
        try {
            return withConnectionSource(source, callable);
        }
        finally {
            source.release();
        }
    }
    
    static <T> T withConnectionSource(final ConnectionSource source, final CallableWithConnection<T> callable) {
        final Connection connection = source.getConnection();
        try {
            return callable.call(connection);
        }
        finally {
            connection.release();
        }
    }
    
    static <T> T withConnectionSource(final ConnectionSource source, final CallableWithConnectionAndSource<T> callable) {
        final Connection connection = source.getConnection();
        try {
            return callable.call(source, connection);
        }
        finally {
            connection.release();
        }
    }
    
    static void withConnection(final AsyncWriteBinding binding, final AsyncCallableWithConnection callable) {
        binding.getWriteConnectionSource(ErrorHandlingResultCallback.errorHandlingCallback((SingleResultCallback<AsyncConnectionSource>)new AsyncCallableWithConnectionCallback(callable), OperationHelper.LOGGER));
    }
    
    static void withConnection(final AsyncReadBinding binding, final AsyncCallableWithConnection callable) {
        binding.getReadConnectionSource(ErrorHandlingResultCallback.errorHandlingCallback((SingleResultCallback<AsyncConnectionSource>)new AsyncCallableWithConnectionCallback(callable), OperationHelper.LOGGER));
    }
    
    static void withConnection(final AsyncReadBinding binding, final AsyncCallableWithConnectionAndSource callable) {
        binding.getReadConnectionSource(ErrorHandlingResultCallback.errorHandlingCallback((SingleResultCallback<AsyncConnectionSource>)new AsyncCallableWithConnectionAndSourceCallback(callable), OperationHelper.LOGGER));
    }
    
    private static void withConnectionSource(final AsyncConnectionSource source, final AsyncCallableWithConnection callable) {
        source.getConnection(new SingleResultCallback<AsyncConnection>() {
            @Override
            public void onResult(final AsyncConnection connection, final Throwable t) {
                source.release();
                if (t != null) {
                    callable.call(null, t);
                }
                else {
                    callable.call(connection, null);
                }
            }
        });
    }
    
    private static void withConnectionSource(final AsyncConnectionSource source, final AsyncCallableWithConnectionAndSource callable) {
        source.getConnection(new SingleResultCallback<AsyncConnection>() {
            @Override
            public void onResult(final AsyncConnection result, final Throwable t) {
                callable.call(source, result, t);
            }
        });
    }
    
    private OperationHelper() {
    }
    
    static {
        LOGGER = Loggers.getLogger("operation");
    }
    
    private static class ReferenceCountedReleasingWrappedCallback<T> implements SingleResultCallback<T>
    {
        private final SingleResultCallback<T> wrapped;
        private final List<? extends ReferenceCounted> referenceCounted;
        
        ReferenceCountedReleasingWrappedCallback(final SingleResultCallback<T> wrapped, final List<? extends ReferenceCounted> referenceCounted) {
            this.wrapped = wrapped;
            this.referenceCounted = Assertions.notNull("referenceCounted", referenceCounted);
        }
        
        @Override
        public void onResult(final T result, final Throwable t) {
            for (final ReferenceCounted cur : this.referenceCounted) {
                cur.release();
            }
            this.wrapped.onResult(result, t);
        }
    }
    
    private static class AsyncCallableWithConnectionCallback implements SingleResultCallback<AsyncConnectionSource>
    {
        private final AsyncCallableWithConnection callable;
        
        public AsyncCallableWithConnectionCallback(final AsyncCallableWithConnection callable) {
            this.callable = callable;
        }
        
        @Override
        public void onResult(final AsyncConnectionSource source, final Throwable t) {
            if (t != null) {
                this.callable.call(null, t);
            }
            else {
                withConnectionSource(source, this.callable);
            }
        }
    }
    
    private static class AsyncCallableWithConnectionAndSourceCallback implements SingleResultCallback<AsyncConnectionSource>
    {
        private final AsyncCallableWithConnectionAndSource callable;
        
        public AsyncCallableWithConnectionAndSourceCallback(final AsyncCallableWithConnectionAndSource callable) {
            this.callable = callable;
        }
        
        @Override
        public void onResult(final AsyncConnectionSource source, final Throwable t) {
            if (t != null) {
                this.callable.call(null, null, t);
            }
            else {
                withConnectionSource(source, this.callable);
            }
        }
    }
    
    interface AsyncCallableWithConnectionAndSource
    {
        void call(final AsyncConnectionSource p0, final AsyncConnection p1, final Throwable p2);
    }
    
    interface AsyncCallableWithConnection
    {
        void call(final AsyncConnection p0, final Throwable p1);
    }
    
    interface CallableWithConnectionAndSource<T>
    {
        T call(final ConnectionSource p0, final Connection p1);
    }
    
    interface CallableWithConnection<T>
    {
        T call(final Connection p0);
    }
}
