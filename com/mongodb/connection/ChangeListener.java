package com.mongodb.connection;

interface ChangeListener<T>
{
    void stateChanged(final ChangeEvent<T> p0);
}
