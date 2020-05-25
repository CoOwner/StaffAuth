package javax.mail.internet;

import java.text.*;

class MailDateParser
{
    int index;
    char[] orig;
    
    public MailDateParser(final char[] orig) {
        this.index = 0;
        this.orig = null;
        this.orig = orig;
    }
    
    public void skipUntilNumber() throws ParseException {
        try {
        Label_0064:
            while (true) {
                switch (this.orig[this.index]) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9': {
                        break Label_0064;
                    }
                    default: {
                        ++this.index;
                        continue;
                    }
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new ParseException("No Number Found", this.index);
        }
    }
    
    public void skipWhiteSpace() {
        final int len = this.orig.length;
        while (this.index < len) {
            switch (this.orig[this.index]) {
                case '\t':
                case '\n':
                case '\r':
                case ' ': {
                    ++this.index;
                    continue;
                }
                default: {}
            }
        }
    }
    
    public int peekChar() throws ParseException {
        if (this.index < this.orig.length) {
            return this.orig[this.index];
        }
        throw new ParseException("No more characters", this.index);
    }
    
    public void skipChar(final char c) throws ParseException {
        if (this.index >= this.orig.length) {
            throw new ParseException("No more characters", this.index);
        }
        if (this.orig[this.index] == c) {
            ++this.index;
            return;
        }
        throw new ParseException("Wrong char", this.index);
    }
    
    public boolean skipIfChar(final char c) throws ParseException {
        if (this.index >= this.orig.length) {
            throw new ParseException("No more characters", this.index);
        }
        if (this.orig[this.index] == c) {
            ++this.index;
            return true;
        }
        return false;
    }
    
    public int parseNumber() throws ParseException {
        final int length = this.orig.length;
        boolean gotNum = false;
        int result = 0;
        while (this.index < length) {
            switch (this.orig[this.index]) {
                case '0': {
                    result *= 10;
                    gotNum = true;
                    break;
                }
                case '1': {
                    result = result * 10 + 1;
                    gotNum = true;
                    break;
                }
                case '2': {
                    result = result * 10 + 2;
                    gotNum = true;
                    break;
                }
                case '3': {
                    result = result * 10 + 3;
                    gotNum = true;
                    break;
                }
                case '4': {
                    result = result * 10 + 4;
                    gotNum = true;
                    break;
                }
                case '5': {
                    result = result * 10 + 5;
                    gotNum = true;
                    break;
                }
                case '6': {
                    result = result * 10 + 6;
                    gotNum = true;
                    break;
                }
                case '7': {
                    result = result * 10 + 7;
                    gotNum = true;
                    break;
                }
                case '8': {
                    result = result * 10 + 8;
                    gotNum = true;
                    break;
                }
                case '9': {
                    result = result * 10 + 9;
                    gotNum = true;
                    break;
                }
                default: {
                    if (gotNum) {
                        return result;
                    }
                    throw new ParseException("No Number found", this.index);
                }
            }
            ++this.index;
        }
        if (gotNum) {
            return result;
        }
        throw new ParseException("No Number found", this.index);
    }
    
    public int parseMonth() throws ParseException {
        try {
            switch (this.orig[this.index++]) {
                case 'J':
                case 'j': {
                    switch (this.orig[this.index++]) {
                        case 'A':
                        case 'a': {
                            final char curr = this.orig[this.index++];
                            if (curr == 'N' || curr == 'n') {
                                return 0;
                            }
                            break;
                        }
                        case 'U':
                        case 'u': {
                            final char curr = this.orig[this.index++];
                            if (curr == 'N' || curr == 'n') {
                                return 5;
                            }
                            if (curr == 'L' || curr == 'l') {
                                return 6;
                            }
                            break;
                        }
                    }
                    break;
                }
                case 'F':
                case 'f': {
                    char curr = this.orig[this.index++];
                    if (curr != 'E' && curr != 'e') {
                        break;
                    }
                    curr = this.orig[this.index++];
                    if (curr == 'B' || curr == 'b') {
                        return 1;
                    }
                    break;
                }
                case 'M':
                case 'm': {
                    char curr = this.orig[this.index++];
                    if (curr != 'A' && curr != 'a') {
                        break;
                    }
                    curr = this.orig[this.index++];
                    if (curr == 'R' || curr == 'r') {
                        return 2;
                    }
                    if (curr == 'Y' || curr == 'y') {
                        return 4;
                    }
                    break;
                }
                case 'A':
                case 'a': {
                    char curr = this.orig[this.index++];
                    if (curr == 'P' || curr == 'p') {
                        curr = this.orig[this.index++];
                        if (curr == 'R' || curr == 'r') {
                            return 3;
                        }
                        break;
                    }
                    else {
                        if (curr != 'U' && curr != 'u') {
                            break;
                        }
                        curr = this.orig[this.index++];
                        if (curr == 'G' || curr == 'g') {
                            return 7;
                        }
                        break;
                    }
                    break;
                }
                case 'S':
                case 's': {
                    char curr = this.orig[this.index++];
                    if (curr != 'E' && curr != 'e') {
                        break;
                    }
                    curr = this.orig[this.index++];
                    if (curr == 'P' || curr == 'p') {
                        return 8;
                    }
                    break;
                }
                case 'O':
                case 'o': {
                    char curr = this.orig[this.index++];
                    if (curr != 'C' && curr != 'c') {
                        break;
                    }
                    curr = this.orig[this.index++];
                    if (curr == 'T' || curr == 't') {
                        return 9;
                    }
                    break;
                }
                case 'N':
                case 'n': {
                    char curr = this.orig[this.index++];
                    if (curr != 'O' && curr != 'o') {
                        break;
                    }
                    curr = this.orig[this.index++];
                    if (curr == 'V' || curr == 'v') {
                        return 10;
                    }
                    break;
                }
                case 'D':
                case 'd': {
                    char curr = this.orig[this.index++];
                    if (curr != 'E' && curr != 'e') {
                        break;
                    }
                    curr = this.orig[this.index++];
                    if (curr == 'C' || curr == 'c') {
                        return 11;
                    }
                    break;
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException ex) {}
        throw new ParseException("Bad Month", this.index);
    }
    
    public int parseTimeZone() throws ParseException {
        if (this.index >= this.orig.length) {
            throw new ParseException("No more characters", this.index);
        }
        final char test = this.orig[this.index];
        if (test == '+' || test == '-') {
            return this.parseNumericTimeZone();
        }
        return this.parseAlphaTimeZone();
    }
    
    public int parseNumericTimeZone() throws ParseException {
        boolean switchSign = false;
        final char first = this.orig[this.index++];
        if (first == '+') {
            switchSign = true;
        }
        else if (first != '-') {
            throw new ParseException("Bad Numeric TimeZone", this.index);
        }
        final int tz = this.parseNumber();
        final int offset = tz / 100 * 60 + tz % 100;
        if (switchSign) {
            return -offset;
        }
        return offset;
    }
    
    public int parseAlphaTimeZone() throws ParseException {
        int result = 0;
        boolean foundCommon = false;
        try {
            switch (this.orig[this.index++]) {
                case 'U':
                case 'u': {
                    final char curr = this.orig[this.index++];
                    if (curr == 'T' || curr == 't') {
                        result = 0;
                        break;
                    }
                    throw new ParseException("Bad Alpha TimeZone", this.index);
                }
                case 'G':
                case 'g': {
                    char curr = this.orig[this.index++];
                    if (curr == 'M' || curr == 'm') {
                        curr = this.orig[this.index++];
                        if (curr == 'T' || curr == 't') {
                            result = 0;
                            break;
                        }
                    }
                    throw new ParseException("Bad Alpha TimeZone", this.index);
                }
                case 'E':
                case 'e': {
                    result = 300;
                    foundCommon = true;
                    break;
                }
                case 'C':
                case 'c': {
                    result = 360;
                    foundCommon = true;
                    break;
                }
                case 'M':
                case 'm': {
                    result = 420;
                    foundCommon = true;
                    break;
                }
                case 'P':
                case 'p': {
                    result = 480;
                    foundCommon = true;
                    break;
                }
                default: {
                    throw new ParseException("Bad Alpha TimeZone", this.index);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            throw new ParseException("Bad Alpha TimeZone", this.index);
        }
        if (foundCommon) {
            char curr = this.orig[this.index++];
            if (curr == 'S' || curr == 's') {
                curr = this.orig[this.index++];
                if (curr != 'T' && curr != 't') {
                    throw new ParseException("Bad Alpha TimeZone", this.index);
                }
            }
            else if (curr == 'D' || curr == 'd') {
                curr = this.orig[this.index++];
                if (curr != 'T' && curr == 't') {
                    throw new ParseException("Bad Alpha TimeZone", this.index);
                }
                result -= 60;
            }
        }
        return result;
    }
    
    int getIndex() {
        return this.index;
    }
}
