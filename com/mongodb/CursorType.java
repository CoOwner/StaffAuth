package com.mongodb;

public enum CursorType
{
    NonTailable {
        @Override
        public boolean isTailable() {
            return false;
        }
    }, 
    Tailable {
        @Override
        public boolean isTailable() {
            return true;
        }
    }, 
    TailableAwait {
        @Override
        public boolean isTailable() {
            return true;
        }
    };
    
    public abstract boolean isTailable();
}
