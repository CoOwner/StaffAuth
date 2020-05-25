package com.mongodb.connection;

import com.mongodb.annotations.*;
import com.mongodb.assertions.*;

@NotThreadSafe
class ExponentiallyWeightedMovingAverage
{
    private final double alpha;
    private long average;
    
    ExponentiallyWeightedMovingAverage(final double alpha) {
        this.average = -1L;
        Assertions.isTrueArgument("alpha >= 0.0 and <= 1.0", alpha >= 0.0 && alpha <= 1.0);
        this.alpha = alpha;
    }
    
    void reset() {
        this.average = -1L;
    }
    
    long addSample(final long sample) {
        if (this.average == -1L) {
            this.average = sample;
        }
        else {
            this.average = (long)(this.alpha * sample + (1.0 - this.alpha) * this.average);
        }
        return this.average;
    }
    
    long getAverage() {
        return (this.average == -1L) ? 0L : this.average;
    }
}
