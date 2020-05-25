package com.mongodb.internal.connection;

import java.util.*;
import com.mongodb.*;
import com.mongodb.assertions.*;

public abstract class IndexMap
{
    public static IndexMap create() {
        return new RangeBased();
    }
    
    public static IndexMap create(final int startIndex, final int count) {
        return new RangeBased(startIndex, count);
    }
    
    public abstract IndexMap add(final int p0, final int p1);
    
    public abstract int map(final int p0);
    
    private static class HashBased extends IndexMap
    {
        private final Map<Integer, Integer> indexMap;
        
        public HashBased(final int startIndex, final int count) {
            this.indexMap = new HashMap<Integer, Integer>();
            for (int i = startIndex; i < startIndex + count; ++i) {
                this.indexMap.put(i - startIndex, i);
            }
        }
        
        @Override
        public IndexMap add(final int index, final int originalIndex) {
            this.indexMap.put(index, originalIndex);
            return this;
        }
        
        @Override
        public int map(final int index) {
            final Integer originalIndex = this.indexMap.get(index);
            if (originalIndex == null) {
                throw new MongoInternalException("no mapping found for index " + index);
            }
            return originalIndex;
        }
    }
    
    private static class RangeBased extends IndexMap
    {
        private int startIndex;
        private int count;
        
        public RangeBased() {
        }
        
        public RangeBased(final int startIndex, final int count) {
            Assertions.isTrueArgument("startIndex", startIndex >= 0);
            Assertions.isTrueArgument("count", count > 0);
            this.startIndex = startIndex;
            this.count = count;
        }
        
        @Override
        public IndexMap add(final int index, final int originalIndex) {
            if (this.count == 0) {
                this.startIndex = originalIndex;
                this.count = 1;
                return this;
            }
            if (originalIndex == this.startIndex + this.count) {
                ++this.count;
                return this;
            }
            final IndexMap hashBasedMap = new HashBased(this.startIndex, this.count);
            hashBasedMap.add(index, originalIndex);
            return hashBasedMap;
        }
        
        @Override
        public int map(final int index) {
            if (index < 0) {
                throw new MongoInternalException("no mapping found for index " + index);
            }
            if (index >= this.count) {
                throw new MongoInternalException("index should not be greater than or equal to count");
            }
            return this.startIndex + index;
        }
    }
}
