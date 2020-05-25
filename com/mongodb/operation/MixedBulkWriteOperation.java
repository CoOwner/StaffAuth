package com.mongodb.operation;

import com.mongodb.assertions.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import com.mongodb.internal.connection.*;
import com.mongodb.*;
import com.mongodb.bulk.*;
import java.util.*;
import org.bson.*;

public class MixedBulkWriteOperation implements AsyncWriteOperation<BulkWriteResult>, WriteOperation<BulkWriteResult>
{
    private final MongoNamespace namespace;
    private final List<? extends WriteRequest> writeRequests;
    private final boolean ordered;
    private final WriteConcern writeConcern;
    private Boolean bypassDocumentValidation;
    private static final List<String> IGNORED_KEYS;
    
    public MixedBulkWriteOperation(final MongoNamespace namespace, final List<? extends WriteRequest> writeRequests, final boolean ordered, final WriteConcern writeConcern) {
        this.ordered = ordered;
        this.namespace = Assertions.notNull("namespace", namespace);
        this.writeRequests = Assertions.notNull("writes", writeRequests);
        this.writeConcern = Assertions.notNull("writeConcern", writeConcern);
        Assertions.isTrueArgument("writes is not an empty list", !writeRequests.isEmpty());
    }
    
    public MongoNamespace getNamespace() {
        return this.namespace;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public boolean isOrdered() {
        return this.ordered;
    }
    
    public List<? extends WriteRequest> getWriteRequests() {
        return this.writeRequests;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public MixedBulkWriteOperation bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
    
    @Override
    public BulkWriteResult execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<BulkWriteResult>)new OperationHelper.CallableWithConnection<BulkWriteResult>() {
            @Override
            public BulkWriteResult call(final Connection connection) {
                OperationHelper.validateWriteRequests(connection, MixedBulkWriteOperation.this.bypassDocumentValidation, MixedBulkWriteOperation.this.writeRequests, MixedBulkWriteOperation.this.writeConcern);
                final BulkWriteBatchCombiner bulkWriteBatchCombiner = new BulkWriteBatchCombiner(connection.getDescription().getServerAddress(), MixedBulkWriteOperation.this.ordered, MixedBulkWriteOperation.this.writeConcern);
                for (final Run run : MixedBulkWriteOperation.this.getRunGenerator(connection.getDescription())) {
                    try {
                        final BulkWriteResult result = run.execute(connection);
                        if (!result.wasAcknowledged()) {
                            continue;
                        }
                        bulkWriteBatchCombiner.addResult(result, run.indexMap);
                    }
                    catch (MongoBulkWriteException e) {
                        bulkWriteBatchCombiner.addErrorResult(e, run.indexMap);
                        if (bulkWriteBatchCombiner.shouldStopSendingMoreBatches()) {
                            break;
                        }
                        continue;
                    }
                }
                return bulkWriteBatchCombiner.getResult();
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncWriteBinding binding, final SingleResultCallback<BulkWriteResult> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<BulkWriteResult> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else {
                    OperationHelper.validateWriteRequests(connection, MixedBulkWriteOperation.this.bypassDocumentValidation, MixedBulkWriteOperation.this.writeRequests, MixedBulkWriteOperation.this.writeConcern, new OperationHelper.AsyncCallableWithConnection() {
                        @Override
                        public void call(final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                OperationHelper.releasingCallback((SingleResultCallback<Object>)errHandlingCallback, connection).onResult(null, t);
                            }
                            else {
                                final Iterator<Run> runs = MixedBulkWriteOperation.this.getRunGenerator(connection.getDescription()).iterator();
                                MixedBulkWriteOperation.this.executeRunsAsync(runs, connection, new BulkWriteBatchCombiner(connection.getDescription().getServerAddress(), MixedBulkWriteOperation.this.ordered, MixedBulkWriteOperation.this.writeConcern), errHandlingCallback);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private void executeRunsAsync(final Iterator<Run> runs, final AsyncConnection connection, final BulkWriteBatchCombiner bulkWriteBatchCombiner, final SingleResultCallback<BulkWriteResult> callback) {
        final Run run = runs.next();
        final SingleResultCallback<BulkWriteResult> wrappedCallback = OperationHelper.releasingCallback(callback, connection);
        run.executeAsync(connection, new SingleResultCallback<BulkWriteResult>() {
            @Override
            public void onResult(final BulkWriteResult result, final Throwable t) {
                if (t != null) {
                    if (!(t instanceof MongoBulkWriteException)) {
                        wrappedCallback.onResult(null, t);
                        return;
                    }
                    bulkWriteBatchCombiner.addErrorResult((MongoBulkWriteException)t, run.indexMap);
                }
                else if (result.wasAcknowledged()) {
                    bulkWriteBatchCombiner.addResult(result, run.indexMap);
                }
                if (runs.hasNext() && !bulkWriteBatchCombiner.shouldStopSendingMoreBatches()) {
                    MixedBulkWriteOperation.this.executeRunsAsync(runs, connection, bulkWriteBatchCombiner, callback);
                }
                else if (bulkWriteBatchCombiner.hasErrors()) {
                    wrappedCallback.onResult(null, bulkWriteBatchCombiner.getError());
                }
                else {
                    wrappedCallback.onResult(bulkWriteBatchCombiner.getResult(), null);
                }
            }
        });
    }
    
    private boolean shouldUseWriteCommands(final ConnectionDescription description) {
        return this.writeConcern.isAcknowledged() && this.serverSupportsWriteCommands(description);
    }
    
    private boolean serverSupportsWriteCommands(final ConnectionDescription connectionDescription) {
        return connectionDescription.getServerVersion().compareTo(new ServerVersion(2, 6)) >= 0;
    }
    
    private Iterable<Run> getRunGenerator(final ConnectionDescription connectionDescription) {
        if (this.ordered) {
            return new OrderedRunGenerator(connectionDescription, this.bypassDocumentValidation);
        }
        return new UnorderedRunGenerator(connectionDescription, this.bypassDocumentValidation);
    }
    
    static {
        IGNORED_KEYS = Arrays.asList("ok", "err", "code");
    }
    
    private class OrderedRunGenerator implements Iterable<Run>
    {
        private final int maxBatchCount;
        private final Boolean bypassDocumentValidation;
        
        public OrderedRunGenerator(final ConnectionDescription connectionDescription, final Boolean bypassDocumentValidation) {
            this.maxBatchCount = connectionDescription.getMaxBatchCount();
            this.bypassDocumentValidation = bypassDocumentValidation;
        }
        
        @Override
        public Iterator<Run> iterator() {
            return new Iterator<Run>() {
                private int curIndex;
                
                @Override
                public boolean hasNext() {
                    return this.curIndex < MixedBulkWriteOperation.this.writeRequests.size();
                }
                
                @Override
                public Run next() {
                    final Run run = new Run(MixedBulkWriteOperation.this.writeRequests.get(this.curIndex).getType(), true, OrderedRunGenerator.this.bypassDocumentValidation);
                    final int nextIndex = this.getNextIndex();
                    for (int i = this.curIndex; i < nextIndex; ++i) {
                        run.add(MixedBulkWriteOperation.this.writeRequests.get(i), i);
                    }
                    this.curIndex = nextIndex;
                    return run;
                }
                
                private int getNextIndex() {
                    final WriteRequest.Type type = MixedBulkWriteOperation.this.writeRequests.get(this.curIndex).getType();
                    for (int i = this.curIndex; i < MixedBulkWriteOperation.this.writeRequests.size(); ++i) {
                        if (i == this.curIndex + OrderedRunGenerator.this.maxBatchCount || MixedBulkWriteOperation.this.writeRequests.get(i).getType() != type) {
                            return i;
                        }
                    }
                    return MixedBulkWriteOperation.this.writeRequests.size();
                }
                
                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not implemented");
                }
            };
        }
    }
    
    private class UnorderedRunGenerator implements Iterable<Run>
    {
        private final int maxBatchCount;
        private final Boolean bypassDocumentValidation;
        
        public UnorderedRunGenerator(final ConnectionDescription connectionDescription, final Boolean bypassDocumentValidation) {
            this.maxBatchCount = connectionDescription.getMaxBatchCount();
            this.bypassDocumentValidation = bypassDocumentValidation;
        }
        
        @Override
        public Iterator<Run> iterator() {
            return new Iterator<Run>() {
                private final List<Run> runs = new ArrayList<Run>();
                private int curIndex;
                
                @Override
                public boolean hasNext() {
                    return this.curIndex < MixedBulkWriteOperation.this.writeRequests.size() || !this.runs.isEmpty();
                }
                
                @Override
                public Run next() {
                    while (this.curIndex < MixedBulkWriteOperation.this.writeRequests.size()) {
                        final WriteRequest writeRequest = MixedBulkWriteOperation.this.writeRequests.get(this.curIndex);
                        Run run = this.findRunOfType(writeRequest.getType());
                        if (run == null) {
                            run = new Run(writeRequest.getType(), false, UnorderedRunGenerator.this.bypassDocumentValidation);
                            this.runs.add(run);
                        }
                        run.add(writeRequest, this.curIndex);
                        ++this.curIndex;
                        if (run.size() == UnorderedRunGenerator.this.maxBatchCount) {
                            this.runs.remove(run);
                            return run;
                        }
                    }
                    return this.runs.remove(0);
                }
                
                private Run findRunOfType(final WriteRequest.Type type) {
                    for (final Run cur : this.runs) {
                        if (cur.type == type) {
                            return cur;
                        }
                    }
                    return null;
                }
                
                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not implemented");
                }
            };
        }
    }
    
    private class Run
    {
        private final List runWrites;
        private final WriteRequest.Type type;
        private final boolean ordered;
        private final Boolean bypassDocumentValidation;
        private IndexMap indexMap;
        
        Run(final WriteRequest.Type type, final boolean ordered, final Boolean bypassDocumentValidation) {
            this.runWrites = new ArrayList();
            this.indexMap = IndexMap.create();
            this.type = type;
            this.ordered = ordered;
            this.bypassDocumentValidation = bypassDocumentValidation;
        }
        
        void add(final WriteRequest writeRequest, final int originalIndex) {
            this.indexMap = this.indexMap.add(this.runWrites.size(), originalIndex);
            this.runWrites.add(writeRequest);
        }
        
        public int size() {
            return this.runWrites.size();
        }
        
        BulkWriteResult execute(final Connection connection) {
            BulkWriteResult nextWriteResult;
            if (this.type == WriteRequest.Type.UPDATE || this.type == WriteRequest.Type.REPLACE) {
                nextWriteResult = this.getUpdatesRunExecutor(this.runWrites, this.bypassDocumentValidation, connection).execute();
            }
            else if (this.type == WriteRequest.Type.INSERT) {
                nextWriteResult = this.getInsertsRunExecutor(this.runWrites, this.bypassDocumentValidation, connection).execute();
            }
            else {
                if (this.type != WriteRequest.Type.DELETE) {
                    throw new UnsupportedOperationException(String.format("Unsupported write of type %s", this.type));
                }
                nextWriteResult = this.getDeletesRunExecutor(this.runWrites, connection).execute();
            }
            return nextWriteResult;
        }
        
        void executeAsync(final AsyncConnection connection, final SingleResultCallback<BulkWriteResult> callback) {
            if (this.type == WriteRequest.Type.UPDATE || this.type == WriteRequest.Type.REPLACE) {
                this.getUpdatesRunExecutor(this.runWrites, this.bypassDocumentValidation, connection).executeAsync(callback);
            }
            else if (this.type == WriteRequest.Type.INSERT) {
                this.getInsertsRunExecutor(this.runWrites, this.bypassDocumentValidation, connection).executeAsync(callback);
            }
            else if (this.type == WriteRequest.Type.DELETE) {
                this.getDeletesRunExecutor(this.runWrites, connection).executeAsync(callback);
            }
            else {
                callback.onResult(null, new UnsupportedOperationException(String.format("Unsupported write of type %s", this.type)));
            }
        }
        
        RunExecutor getDeletesRunExecutor(final List<DeleteRequest> deleteRequests, final Connection connection) {
            return new RunExecutor(connection) {
                @Override
                WriteConcernResult executeWriteProtocol(final int index) {
                    return connection.delete(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, Collections.singletonList(deleteRequests.get(index)));
                }
                
                @Override
                BulkWriteResult executeWriteCommandProtocol() {
                    return connection.deleteCommand(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, deleteRequests);
                }
                
                @Override
                WriteRequest.Type getType() {
                    return WriteRequest.Type.DELETE;
                }
            };
        }
        
        RunExecutor getInsertsRunExecutor(final List<InsertRequest> insertRequests, final Boolean bypassDocumentValidation, final Connection connection) {
            return new RunExecutor(connection) {
                @Override
                WriteConcernResult executeWriteProtocol(final int index) {
                    return connection.insert(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, Collections.singletonList(insertRequests.get(index)));
                }
                
                @Override
                BulkWriteResult executeWriteCommandProtocol() {
                    return connection.insertCommand(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, bypassDocumentValidation, insertRequests);
                }
                
                @Override
                WriteRequest.Type getType() {
                    return WriteRequest.Type.INSERT;
                }
                
                @Override
                int getCount(final WriteConcernResult writeConcernResult) {
                    return 1;
                }
            };
        }
        
        RunExecutor getUpdatesRunExecutor(final List<UpdateRequest> updates, final Boolean bypassDocumentValidation, final Connection connection) {
            return new RunExecutor(connection) {
                @Override
                WriteConcernResult executeWriteProtocol(final int index) {
                    return connection.update(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, Collections.singletonList(updates.get(index)));
                }
                
                @Override
                BulkWriteResult executeWriteCommandProtocol() {
                    return connection.updateCommand(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, bypassDocumentValidation, updates);
                }
                
                @Override
                WriteRequest.Type getType() {
                    return WriteRequest.Type.UPDATE;
                }
            };
        }
        
        AsyncRunExecutor getDeletesRunExecutor(final List<DeleteRequest> deleteRequests, final AsyncConnection connection) {
            return new AsyncRunExecutor(connection) {
                @Override
                void executeWriteProtocolAsync(final int index, final SingleResultCallback<WriteConcernResult> callback) {
                    connection.deleteAsync(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, Collections.singletonList(deleteRequests.get(index)), callback);
                }
                
                @Override
                void executeWriteCommandProtocolAsync(final SingleResultCallback<BulkWriteResult> callback) {
                    connection.deleteCommandAsync(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, deleteRequests, callback);
                }
                
                @Override
                WriteRequest.Type getType() {
                    return WriteRequest.Type.DELETE;
                }
            };
        }
        
        AsyncRunExecutor getInsertsRunExecutor(final List<InsertRequest> insertRequests, final Boolean bypassDocumentValidation, final AsyncConnection connection) {
            return new AsyncRunExecutor(connection) {
                @Override
                void executeWriteProtocolAsync(final int index, final SingleResultCallback<WriteConcernResult> callback) {
                    connection.insertAsync(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, Collections.singletonList(insertRequests.get(index)), callback);
                }
                
                @Override
                void executeWriteCommandProtocolAsync(final SingleResultCallback<BulkWriteResult> callback) {
                    connection.insertCommandAsync(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, bypassDocumentValidation, insertRequests, callback);
                }
                
                @Override
                WriteRequest.Type getType() {
                    return WriteRequest.Type.INSERT;
                }
                
                @Override
                int getCount(final WriteConcernResult writeConcernResult) {
                    return 1;
                }
            };
        }
        
        AsyncRunExecutor getUpdatesRunExecutor(final List<UpdateRequest> updates, final Boolean bypassDocumentValidation, final AsyncConnection connection) {
            return new AsyncRunExecutor(connection) {
                @Override
                void executeWriteProtocolAsync(final int index, final SingleResultCallback<WriteConcernResult> callback) {
                    connection.updateAsync(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, Collections.singletonList(updates.get(index)), callback);
                }
                
                @Override
                void executeWriteCommandProtocolAsync(final SingleResultCallback<BulkWriteResult> callback) {
                    connection.updateCommandAsync(MixedBulkWriteOperation.this.namespace, Run.this.ordered, MixedBulkWriteOperation.this.writeConcern, bypassDocumentValidation, updates, callback);
                }
                
                @Override
                WriteRequest.Type getType() {
                    return WriteRequest.Type.UPDATE;
                }
            };
        }
        
        private abstract class BaseRunExecutor
        {
            abstract WriteRequest.Type getType();
            
            int getCount(final WriteConcernResult writeConcernResult) {
                return (this.getType() == WriteRequest.Type.INSERT) ? 1 : writeConcernResult.getCount();
            }
            
            BulkWriteResult getResult(final WriteConcernResult writeConcernResult) {
                return this.getResult(writeConcernResult, this.getUpsertedItems(writeConcernResult));
            }
            
            BulkWriteResult getResult(final WriteConcernResult writeConcernResult, final UpdateRequest updateRequest) {
                return this.getResult(writeConcernResult, this.getUpsertedItems(writeConcernResult, updateRequest));
            }
            
            BulkWriteResult getResult(final WriteConcernResult writeConcernResult, final List<BulkWriteUpsert> upsertedItems) {
                final int count = this.getCount(writeConcernResult);
                final Integer modifiedCount = (this.getType() == WriteRequest.Type.UPDATE || this.getType() == WriteRequest.Type.REPLACE) ? null : Integer.valueOf(0);
                return BulkWriteResult.acknowledged(this.getType(), count - upsertedItems.size(), modifiedCount, upsertedItems);
            }
            
            List<BulkWriteUpsert> getUpsertedItems(final WriteConcernResult writeConcernResult) {
                return (writeConcernResult.getUpsertedId() == null) ? Collections.emptyList() : Collections.singletonList(new BulkWriteUpsert(0, writeConcernResult.getUpsertedId()));
            }
            
            List<BulkWriteUpsert> getUpsertedItems(final WriteConcernResult writeConcernResult, final UpdateRequest updateRequest) {
                if (writeConcernResult.getUpsertedId() != null) {
                    return Collections.singletonList(new BulkWriteUpsert(0, writeConcernResult.getUpsertedId()));
                }
                if (writeConcernResult.isUpdateOfExisting() || !updateRequest.isUpsert()) {
                    return Collections.emptyList();
                }
                final BsonDocument update = updateRequest.getUpdate();
                final BsonDocument filter = updateRequest.getFilter();
                if (update.containsKey("_id")) {
                    return Collections.singletonList(new BulkWriteUpsert(0, update.get("_id")));
                }
                if (filter.containsKey("_id")) {
                    return Collections.singletonList(new BulkWriteUpsert(0, filter.get("_id")));
                }
                return Collections.emptyList();
            }
            
            BulkWriteError getBulkWriteError(final WriteConcernException writeException) {
                return new BulkWriteError(writeException.getErrorCode(), writeException.getErrorMessage(), this.translateGetLastErrorResponseToErrInfo(writeException.getResponse()), 0);
            }
            
            WriteConcernError getWriteConcernError(final WriteConcernException writeException) {
                return new WriteConcernError(writeException.getErrorCode(), ((BsonString)writeException.getResponse().get("err")).getValue(), this.translateGetLastErrorResponseToErrInfo(writeException.getResponse()));
            }
            
            private BsonDocument translateGetLastErrorResponseToErrInfo(final BsonDocument response) {
                final BsonDocument errInfo = new BsonDocument();
                for (final Map.Entry<String, BsonValue> entry : response.entrySet()) {
                    if (MixedBulkWriteOperation.IGNORED_KEYS.contains(entry.getKey())) {
                        continue;
                    }
                    errInfo.put(entry.getKey(), entry.getValue());
                }
                return errInfo;
            }
        }
        
        private abstract class RunExecutor extends BaseRunExecutor
        {
            private final Connection connection;
            
            RunExecutor(final Connection connection) {
                this.connection = connection;
            }
            
            abstract WriteConcernResult executeWriteProtocol(final int p0);
            
            abstract BulkWriteResult executeWriteCommandProtocol();
            
            BulkWriteResult execute() {
                if (MixedBulkWriteOperation.this.shouldUseWriteCommands(this.connection.getDescription())) {
                    return this.executeWriteCommandProtocol();
                }
                final BulkWriteBatchCombiner bulkWriteBatchCombiner = new BulkWriteBatchCombiner(this.connection.getDescription().getServerAddress(), Run.this.ordered, MixedBulkWriteOperation.this.writeConcern);
                for (int i = 0; i < Run.this.runWrites.size(); ++i) {
                    IndexMap indexMap = IndexMap.create(i, 1);
                    indexMap = indexMap.add(0, i);
                    try {
                        final WriteConcernResult result = this.executeWriteProtocol(i);
                        if (result.wasAcknowledged()) {
                            BulkWriteResult bulkWriteResult;
                            if (this.getType() == WriteRequest.Type.UPDATE || this.getType() == WriteRequest.Type.REPLACE) {
                                bulkWriteResult = this.getResult(result, Run.this.runWrites.get(i));
                            }
                            else {
                                bulkWriteResult = this.getResult(result);
                            }
                            bulkWriteBatchCombiner.addResult(bulkWriteResult, indexMap);
                        }
                    }
                    catch (WriteConcernException writeException) {
                        if (writeException.getResponse().get("wtimeout") != null) {
                            bulkWriteBatchCombiner.addWriteConcernErrorResult(this.getWriteConcernError(writeException));
                        }
                        else {
                            bulkWriteBatchCombiner.addWriteErrorResult(this.getBulkWriteError(writeException), indexMap);
                        }
                        if (bulkWriteBatchCombiner.shouldStopSendingMoreBatches()) {
                            break;
                        }
                    }
                }
                return bulkWriteBatchCombiner.getResult();
            }
        }
        
        private abstract class AsyncRunExecutor extends BaseRunExecutor
        {
            private final AsyncConnection connection;
            
            AsyncRunExecutor(final AsyncConnection connection) {
                this.connection = connection;
            }
            
            abstract void executeWriteProtocolAsync(final int p0, final SingleResultCallback<WriteConcernResult> p1);
            
            abstract void executeWriteCommandProtocolAsync(final SingleResultCallback<BulkWriteResult> p0);
            
            void executeAsync(final SingleResultCallback<BulkWriteResult> callback) {
                if (MixedBulkWriteOperation.this.shouldUseWriteCommands(this.connection.getDescription())) {
                    this.executeWriteCommandProtocolAsync(callback);
                }
                else {
                    final BulkWriteBatchCombiner bulkWriteBatchCombiner = new BulkWriteBatchCombiner(this.connection.getDescription().getServerAddress(), Run.this.ordered, MixedBulkWriteOperation.this.writeConcern);
                    this.executeRunWritesAsync(Run.this.runWrites.size(), 0, bulkWriteBatchCombiner, callback);
                }
            }
            
            private void executeRunWritesAsync(final int numberOfRuns, final int currentPosition, final BulkWriteBatchCombiner bulkWriteBatchCombiner, final SingleResultCallback<BulkWriteResult> callback) {
                final IndexMap indexMap = IndexMap.create(currentPosition, 1).add(0, currentPosition);
                this.executeWriteProtocolAsync(currentPosition, new SingleResultCallback<WriteConcernResult>() {
                    @Override
                    public void onResult(final WriteConcernResult result, final Throwable t) {
                        final int nextRunPosition = currentPosition + 1;
                        if (t != null) {
                            if (!(t instanceof WriteConcernException)) {
                                callback.onResult(null, t);
                                return;
                            }
                            final WriteConcernException writeException = (WriteConcernException)t;
                            if (writeException.getResponse().get("wtimeout") != null) {
                                bulkWriteBatchCombiner.addWriteConcernErrorResult(AsyncRunExecutor.this.getWriteConcernError(writeException));
                            }
                            else {
                                bulkWriteBatchCombiner.addWriteErrorResult(AsyncRunExecutor.this.getBulkWriteError(writeException), indexMap);
                            }
                        }
                        else if (result.wasAcknowledged()) {
                            BulkWriteResult bulkWriteResult;
                            if (AsyncRunExecutor.this.getType() == WriteRequest.Type.UPDATE || AsyncRunExecutor.this.getType() == WriteRequest.Type.REPLACE) {
                                bulkWriteResult = AsyncRunExecutor.this.getResult(result, Run.this.runWrites.get(currentPosition));
                            }
                            else {
                                bulkWriteResult = AsyncRunExecutor.this.getResult(result);
                            }
                            bulkWriteBatchCombiner.addResult(bulkWriteResult, indexMap);
                        }
                        if (numberOfRuns != nextRunPosition && !bulkWriteBatchCombiner.shouldStopSendingMoreBatches()) {
                            AsyncRunExecutor.this.executeRunWritesAsync(numberOfRuns, nextRunPosition, bulkWriteBatchCombiner, callback);
                        }
                        else if (bulkWriteBatchCombiner.hasErrors()) {
                            callback.onResult(null, bulkWriteBatchCombiner.getError());
                        }
                        else {
                            callback.onResult(bulkWriteBatchCombiner.getResult(), null);
                        }
                    }
                });
            }
        }
    }
}
