package com.mongodb.connection;

interface InternalConnectionFactory
{
    InternalConnection create(final ServerId p0);
}
