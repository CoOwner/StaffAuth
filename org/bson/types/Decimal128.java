package org.bson.types;

import java.io.*;
import java.math.*;
import java.util.*;

public final class Decimal128 implements Serializable
{
    private static final long serialVersionUID = 4570973266503637887L;
    private static final long INFINITY_MASK = 8646911284551352320L;
    private static final long NaN_MASK = 8935141660703064064L;
    private static final long SIGN_BIT_MASK = Long.MIN_VALUE;
    private static final int MIN_EXPONENT = -6176;
    private static final int MAX_EXPONENT = 6111;
    private static final int EXPONENT_OFFSET = 6176;
    private static final int MAX_BIT_LENGTH = 113;
    private static final BigInteger BIG_INT_TEN;
    private static final BigInteger BIG_INT_ONE;
    private static final BigInteger BIG_INT_ZERO;
    private static final Set<String> NaN_STRINGS;
    private static final Set<String> NEGATIVE_NaN_STRINGS;
    private static final Set<String> POSITIVE_INFINITY_STRINGS;
    private static final Set<String> NEGATIVE_INFINITY_STRINGS;
    public static final Decimal128 POSITIVE_INFINITY;
    public static final Decimal128 NEGATIVE_INFINITY;
    public static final Decimal128 NEGATIVE_NaN;
    public static final Decimal128 NaN;
    public static final Decimal128 POSITIVE_ZERO;
    public static final Decimal128 NEGATIVE_ZERO;
    private final long high;
    private final long low;
    
    public static Decimal128 parse(final String value) {
        final String lowerCasedValue = value.toLowerCase();
        if (Decimal128.NaN_STRINGS.contains(lowerCasedValue)) {
            return Decimal128.NaN;
        }
        if (Decimal128.NEGATIVE_NaN_STRINGS.contains(lowerCasedValue)) {
            return Decimal128.NEGATIVE_NaN;
        }
        if (Decimal128.POSITIVE_INFINITY_STRINGS.contains(lowerCasedValue)) {
            return Decimal128.POSITIVE_INFINITY;
        }
        if (Decimal128.NEGATIVE_INFINITY_STRINGS.contains(lowerCasedValue)) {
            return Decimal128.NEGATIVE_INFINITY;
        }
        return new Decimal128(new BigDecimal(value), value.charAt(0) == '-');
    }
    
    public static Decimal128 fromIEEE754BIDEncoding(final long high, final long low) {
        return new Decimal128(high, low);
    }
    
    public Decimal128(final long value) {
        this(new BigDecimal(value, MathContext.DECIMAL128));
    }
    
    public Decimal128(final BigDecimal value) {
        this(value, value.signum() == -1);
    }
    
    private Decimal128(final long high, final long low) {
        this.high = high;
        this.low = low;
    }
    
    private Decimal128(final BigDecimal initialValue, final boolean isNegative) {
        long localHigh = 0L;
        long localLow = 0L;
        final BigDecimal value = this.clampAndRound(initialValue);
        final long exponent = -value.scale();
        if (exponent < -6176L || exponent > 6111L) {
            throw new AssertionError((Object)("Exponent is out of range for Decimal128 encoding: " + exponent));
        }
        if (value.unscaledValue().bitLength() > 113) {
            throw new AssertionError((Object)("Unscaled roundedValue is out of range for Decimal128 encoding:" + value.unscaledValue()));
        }
        final BigInteger significand = value.unscaledValue().abs();
        final int bitLength = significand.bitLength();
        for (int i = 0; i < Math.min(64, bitLength); ++i) {
            if (significand.testBit(i)) {
                localLow |= 1L << i;
            }
        }
        for (int i = 64; i < bitLength; ++i) {
            if (significand.testBit(i)) {
                localHigh |= 1L << i - 64;
            }
        }
        final long biasedExponent = exponent + 6176L;
        localHigh |= biasedExponent << 49;
        if (value.signum() == -1 || isNegative) {
            localHigh |= Long.MIN_VALUE;
        }
        this.high = localHigh;
        this.low = localLow;
    }
    
    private BigDecimal clampAndRound(final BigDecimal initialValue) {
        BigDecimal value;
        if (-initialValue.scale() > 6111) {
            final int diff = -initialValue.scale() - 6111;
            if (initialValue.unscaledValue().equals(Decimal128.BIG_INT_ZERO)) {
                value = new BigDecimal(initialValue.unscaledValue(), -6111);
            }
            else {
                if (diff + initialValue.precision() > 34) {
                    throw new NumberFormatException("Exponent is out of range for Decimal128 encoding of " + initialValue);
                }
                final BigInteger multiplier = Decimal128.BIG_INT_TEN.pow(diff);
                value = new BigDecimal(initialValue.unscaledValue().multiply(multiplier), initialValue.scale() + diff);
            }
        }
        else if (-initialValue.scale() < -6176) {
            final int diff = initialValue.scale() - 6176;
            final int undiscardedPrecision = this.ensureExactRounding(initialValue, diff);
            final BigInteger divisor = (undiscardedPrecision == 0) ? Decimal128.BIG_INT_ONE : Decimal128.BIG_INT_TEN.pow(diff);
            value = new BigDecimal(initialValue.unscaledValue().divide(divisor), initialValue.scale() - diff);
        }
        else {
            value = initialValue.round(MathContext.DECIMAL128);
            final int extraPrecision = initialValue.precision() - value.precision();
            if (extraPrecision > 0) {
                this.ensureExactRounding(initialValue, extraPrecision);
            }
        }
        return value;
    }
    
    private int ensureExactRounding(final BigDecimal initialValue, final int extraPrecision) {
        String significand;
        int i;
        int undiscardedPrecision;
        for (significand = initialValue.unscaledValue().abs().toString(), undiscardedPrecision = (i = Math.max(0, significand.length() - extraPrecision)); i < significand.length(); ++i) {
            if (significand.charAt(i) != '0') {
                throw new NumberFormatException("Conversion to Decimal128 would require inexact rounding of " + initialValue);
            }
        }
        return undiscardedPrecision;
    }
    
    public long getHigh() {
        return this.high;
    }
    
    public long getLow() {
        return this.low;
    }
    
    public BigDecimal bigDecimalValue() {
        if (this.isNaN()) {
            throw new ArithmeticException("NaN can not be converted to a BigDecimal");
        }
        if (this.isInfinite()) {
            throw new ArithmeticException("Infinity can not be converted to a BigDecimal");
        }
        final BigDecimal bigDecimal = this.bigDecimalValueNoNegativeZeroCheck();
        if (this.isNegative() && bigDecimal.signum() == 0) {
            throw new ArithmeticException("Negative zero can not be converted to a BigDecimal");
        }
        return bigDecimal;
    }
    
    private BigDecimal bigDecimalValueNoNegativeZeroCheck() {
        final int scale = -this.getExponent();
        if (this.twoHighestCombinationBitsAreSet()) {
            return BigDecimal.valueOf(0L, scale);
        }
        return new BigDecimal(new BigInteger(this.isNegative() ? -1 : 1, this.getBytes()), scale);
    }
    
    private byte[] getBytes() {
        final byte[] bytes = new byte[15];
        long mask = 255L;
        for (int i = 14; i >= 7; --i) {
            bytes[i] = (byte)((this.low & mask) >>> (14 - i << 3));
            mask <<= 8;
        }
        mask = 255L;
        for (int i = 6; i >= 1; --i) {
            bytes[i] = (byte)((this.high & mask) >>> (6 - i << 3));
            mask <<= 8;
        }
        mask = 281474976710656L;
        bytes[0] = (byte)((this.high & mask) >>> 48);
        return bytes;
    }
    
    int getExponent() {
        if (this.twoHighestCombinationBitsAreSet()) {
            return (int)((this.high & 0x1FFFE00000000000L) >>> 47) - 6176;
        }
        return (int)((this.high & 0x7FFF800000000000L) >>> 49) - 6176;
    }
    
    private boolean twoHighestCombinationBitsAreSet() {
        return (this.high & 0x6000000000000000L) == 0x6000000000000000L;
    }
    
    public boolean isNegative() {
        return (this.high & Long.MIN_VALUE) == Long.MIN_VALUE;
    }
    
    public boolean isInfinite() {
        return (this.high & 0x7800000000000000L) == 0x7800000000000000L;
    }
    
    public boolean isFinite() {
        return !this.isInfinite();
    }
    
    public boolean isNaN() {
        return (this.high & 0x7C00000000000000L) == 0x7C00000000000000L;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Decimal128 that = (Decimal128)o;
        return this.high == that.high && this.low == that.low;
    }
    
    @Override
    public int hashCode() {
        int result = (int)(this.low ^ this.low >>> 32);
        result = 31 * result + (int)(this.high ^ this.high >>> 32);
        return result;
    }
    
    @Override
    public String toString() {
        if (this.isNaN()) {
            return "NaN";
        }
        if (!this.isInfinite()) {
            return this.toStringWithBigDecimal();
        }
        if (this.isNegative()) {
            return "-Infinity";
        }
        return "Infinity";
    }
    
    private String toStringWithBigDecimal() {
        final StringBuilder buffer = new StringBuilder();
        final BigDecimal bigDecimal = this.bigDecimalValueNoNegativeZeroCheck();
        final String significand = bigDecimal.unscaledValue().abs().toString();
        if (this.isNegative()) {
            buffer.append('-');
        }
        final int exponent = -bigDecimal.scale();
        final int adjustedExponent = exponent + (significand.length() - 1);
        if (exponent <= 0 && adjustedExponent >= -6) {
            if (exponent == 0) {
                buffer.append(significand);
            }
            else {
                final int pad = -exponent - significand.length();
                if (pad >= 0) {
                    buffer.append('0');
                    buffer.append('.');
                    for (int i = 0; i < pad; ++i) {
                        buffer.append('0');
                    }
                    buffer.append(significand, 0, significand.length());
                }
                else {
                    buffer.append(significand, 0, -pad);
                    buffer.append('.');
                    buffer.append(significand, -pad, -pad - exponent);
                }
            }
        }
        else {
            buffer.append(significand.charAt(0));
            if (significand.length() > 1) {
                buffer.append('.');
                buffer.append(significand, 1, significand.length());
            }
            buffer.append('E');
            if (adjustedExponent > 0) {
                buffer.append('+');
            }
            buffer.append(adjustedExponent);
        }
        return buffer.toString();
    }
    
    static {
        BIG_INT_TEN = new BigInteger("10");
        BIG_INT_ONE = new BigInteger("1");
        BIG_INT_ZERO = new BigInteger("0");
        NaN_STRINGS = new HashSet<String>(Arrays.asList("nan"));
        NEGATIVE_NaN_STRINGS = new HashSet<String>(Arrays.asList("-nan"));
        POSITIVE_INFINITY_STRINGS = new HashSet<String>(Arrays.asList("inf", "+inf", "infinity", "+infinity"));
        NEGATIVE_INFINITY_STRINGS = new HashSet<String>(Arrays.asList("-inf", "-infinity"));
        POSITIVE_INFINITY = fromIEEE754BIDEncoding(8646911284551352320L, 0L);
        NEGATIVE_INFINITY = fromIEEE754BIDEncoding(-576460752303423488L, 0L);
        NEGATIVE_NaN = fromIEEE754BIDEncoding(-288230376151711744L, 0L);
        NaN = fromIEEE754BIDEncoding(8935141660703064064L, 0L);
        POSITIVE_ZERO = fromIEEE754BIDEncoding(3476778912330022912L, 0L);
        NEGATIVE_ZERO = fromIEEE754BIDEncoding(-5746593124524752896L, 0L);
    }
}
