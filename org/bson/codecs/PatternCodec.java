package org.bson.codecs;

import java.util.regex.*;
import org.bson.*;
import java.util.*;

public class PatternCodec implements Codec<Pattern>
{
    private static final int GLOBAL_FLAG = 256;
    
    @Override
    public void encode(final BsonWriter writer, final Pattern value, final EncoderContext encoderContext) {
        writer.writeRegularExpression(new BsonRegularExpression(value.pattern(), getOptionsAsString(value)));
    }
    
    @Override
    public Pattern decode(final BsonReader reader, final DecoderContext decoderContext) {
        final BsonRegularExpression regularExpression = reader.readRegularExpression();
        return Pattern.compile(regularExpression.getPattern(), getOptionsAsInt(regularExpression));
    }
    
    @Override
    public Class<Pattern> getEncoderClass() {
        return Pattern.class;
    }
    
    private static String getOptionsAsString(final Pattern pattern) {
        int flags = pattern.flags();
        final StringBuilder buf = new StringBuilder();
        for (final RegexFlag flag : RegexFlag.values()) {
            if ((pattern.flags() & flag.javaFlag) > 0) {
                buf.append(flag.flagChar);
                flags -= flag.javaFlag;
            }
        }
        if (flags > 0) {
            throw new IllegalArgumentException("some flags could not be recognized.");
        }
        return buf.toString();
    }
    
    private static int getOptionsAsInt(final BsonRegularExpression regularExpression) {
        int optionsInt = 0;
        String optionsString = regularExpression.getOptions();
        if (optionsString == null || optionsString.length() == 0) {
            return optionsInt;
        }
        optionsString = optionsString.toLowerCase();
        for (int i = 0; i < optionsString.length(); ++i) {
            final RegexFlag flag = RegexFlag.getByCharacter(optionsString.charAt(i));
            if (flag == null) {
                throw new IllegalArgumentException("unrecognized flag [" + optionsString.charAt(i) + "] " + (int)optionsString.charAt(i));
            }
            optionsInt |= flag.javaFlag;
            if (flag.unsupported != null) {}
        }
        return optionsInt;
    }
    
    private enum RegexFlag
    {
        CANON_EQ(128, 'c', "Pattern.CANON_EQ"), 
        UNIX_LINES(1, 'd', "Pattern.UNIX_LINES"), 
        GLOBAL(256, 'g', (String)null), 
        CASE_INSENSITIVE(2, 'i', (String)null), 
        MULTILINE(8, 'm', (String)null), 
        DOTALL(32, 's', "Pattern.DOTALL"), 
        LITERAL(16, 't', "Pattern.LITERAL"), 
        UNICODE_CASE(64, 'u', "Pattern.UNICODE_CASE"), 
        COMMENTS(4, 'x', (String)null);
        
        private static final Map<Character, RegexFlag> BY_CHARACTER;
        private final int javaFlag;
        private final char flagChar;
        private final String unsupported;
        
        public static RegexFlag getByCharacter(final char ch) {
            return RegexFlag.BY_CHARACTER.get(ch);
        }
        
        private RegexFlag(final int f, final char ch, final String u) {
            this.javaFlag = f;
            this.flagChar = ch;
            this.unsupported = u;
        }
        
        static {
            BY_CHARACTER = new HashMap<Character, RegexFlag>();
            for (final RegexFlag flag : values()) {
                RegexFlag.BY_CHARACTER.put(flag.flagChar, flag);
            }
        }
    }
}
