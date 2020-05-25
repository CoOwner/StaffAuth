package org.bson.codecs;

final class UuidCodecHelper
{
    public static void reverseByteArray(final byte[] data, final int start, final int length) {
        for (int left = start, right = start + length - 1; left < right; ++left, --right) {
            final byte temp = data[left];
            data[left] = data[right];
            data[right] = temp;
        }
    }
    
    private UuidCodecHelper() {
    }
}
