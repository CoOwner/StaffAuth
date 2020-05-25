package com.mongodb.connection;

final class Time
{
    static final long CONSTANT_TIME = 42L;
    private static boolean isConstant;
    
    static void makeTimeConstant() {
        Time.isConstant = true;
    }
    
    static void makeTimeMove() {
        Time.isConstant = false;
    }
    
    static long nanoTime() {
        return Time.isConstant ? 42L : System.nanoTime();
    }
    
    private Time() {
    }
}
