package com.mongodb;

abstract class WriteRequest
{
    abstract com.mongodb.bulk.WriteRequest toNew();
}
