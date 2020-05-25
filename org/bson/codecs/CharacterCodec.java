package org.bson.codecs;

import org.bson.assertions.*;
import org.bson.*;

public class CharacterCodec implements Codec<Character>
{
    @Override
    public void encode(final BsonWriter writer, final Character value, final EncoderContext encoderContext) {
        Assertions.notNull("value", value);
        writer.writeString(value.toString());
    }
    
    @Override
    public Character decode(final BsonReader reader, final DecoderContext decoderContext) {
        final String string = reader.readString();
        if (string.length() != 1) {
            throw new BsonInvalidOperationException(String.format("Attempting to decode the string '%s' to a character, but its length is not equal to one", string));
        }
        return string.charAt(0);
    }
    
    @Override
    public Class<Character> getEncoderClass() {
        return Character.class;
    }
}
