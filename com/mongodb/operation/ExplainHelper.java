package com.mongodb.operation;

import org.bson.*;
import com.mongodb.*;

final class ExplainHelper
{
    static BsonDocument asExplainCommand(final BsonDocument command, final ExplainVerbosity explainVerbosity) {
        return new BsonDocument("explain", command).append("verbosity", getVerbosityAsString(explainVerbosity));
    }
    
    private static BsonString getVerbosityAsString(final ExplainVerbosity explainVerbosity) {
        switch (explainVerbosity) {
            case QUERY_PLANNER: {
                return new BsonString("queryPlanner");
            }
            case EXECUTION_STATS: {
                return new BsonString("executionStats");
            }
            case ALL_PLANS_EXECUTIONS: {
                return new BsonString("allPlansExecution");
            }
            default: {
                throw new MongoInternalException(String.format("Unsupported explain verbosity %s", explainVerbosity));
            }
        }
    }
    
    private ExplainHelper() {
    }
}
