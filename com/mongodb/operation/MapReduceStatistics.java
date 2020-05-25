package com.mongodb.operation;

public class MapReduceStatistics
{
    private final int inputCount;
    private final int outputCount;
    private final int emitCount;
    private final int duration;
    
    public MapReduceStatistics(final int inputCount, final int outputCount, final int emitCount, final int duration) {
        this.inputCount = inputCount;
        this.outputCount = outputCount;
        this.emitCount = emitCount;
        this.duration = duration;
    }
    
    public int getInputCount() {
        return this.inputCount;
    }
    
    public int getOutputCount() {
        return this.outputCount;
    }
    
    public int getEmitCount() {
        return this.emitCount;
    }
    
    public int getDuration() {
        return this.duration;
    }
}
