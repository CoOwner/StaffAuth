package com.sun.mail.imap.protocol;

import com.sun.mail.iap.*;
import java.io.*;
import javax.mail.search.*;
import javax.mail.*;
import java.util.*;

class SearchSequence
{
    private static String[] monthTable;
    private static Calendar cal;
    
    static Argument generateSequence(final SearchTerm term, final String charset) throws SearchException, IOException {
        if (term instanceof AndTerm) {
            return and((AndTerm)term, charset);
        }
        if (term instanceof OrTerm) {
            return or((OrTerm)term, charset);
        }
        if (term instanceof NotTerm) {
            return not((NotTerm)term, charset);
        }
        if (term instanceof HeaderTerm) {
            return header((HeaderTerm)term, charset);
        }
        if (term instanceof FlagTerm) {
            return flag((FlagTerm)term);
        }
        if (term instanceof FromTerm) {
            final FromTerm fterm = (FromTerm)term;
            return from(fterm.getAddress().toString(), charset);
        }
        if (term instanceof FromStringTerm) {
            final FromStringTerm fterm2 = (FromStringTerm)term;
            return from(fterm2.getPattern(), charset);
        }
        if (term instanceof RecipientTerm) {
            final RecipientTerm rterm = (RecipientTerm)term;
            return recipient(rterm.getRecipientType(), rterm.getAddress().toString(), charset);
        }
        if (term instanceof RecipientStringTerm) {
            final RecipientStringTerm rterm2 = (RecipientStringTerm)term;
            return recipient(rterm2.getRecipientType(), rterm2.getPattern(), charset);
        }
        if (term instanceof SubjectTerm) {
            return subject((SubjectTerm)term, charset);
        }
        if (term instanceof BodyTerm) {
            return body((BodyTerm)term, charset);
        }
        if (term instanceof SizeTerm) {
            return size((SizeTerm)term);
        }
        if (term instanceof SentDateTerm) {
            return sentdate((DateTerm)term);
        }
        if (term instanceof ReceivedDateTerm) {
            return receiveddate((DateTerm)term);
        }
        if (term instanceof MessageIDTerm) {
            return messageid((MessageIDTerm)term, charset);
        }
        throw new SearchException("Search too complex");
    }
    
    static boolean isAscii(final SearchTerm term) {
        if (term instanceof AndTerm || term instanceof OrTerm) {
            SearchTerm[] terms;
            if (term instanceof AndTerm) {
                terms = ((AndTerm)term).getTerms();
            }
            else {
                terms = ((OrTerm)term).getTerms();
            }
            for (int i = 0; i < terms.length; ++i) {
                if (!isAscii(terms[i])) {
                    return false;
                }
            }
        }
        else {
            if (term instanceof NotTerm) {
                return isAscii(((NotTerm)term).getTerm());
            }
            if (term instanceof StringTerm) {
                return isAscii(((StringTerm)term).getPattern());
            }
            if (term instanceof AddressTerm) {
                return isAscii(((AddressTerm)term).getAddress().toString());
            }
        }
        return true;
    }
    
    private static boolean isAscii(final String s) {
        for (int l = s.length(), i = 0; i < l; ++i) {
            if (s.charAt(i) > '\u007f') {
                return false;
            }
        }
        return true;
    }
    
    private static Argument and(final AndTerm term, final String charset) throws SearchException, IOException {
        final SearchTerm[] terms = term.getTerms();
        final Argument result = generateSequence(terms[0], charset);
        for (int i = 1; i < terms.length; ++i) {
            result.append(generateSequence(terms[i], charset));
        }
        return result;
    }
    
    private static Argument or(OrTerm term, final String charset) throws SearchException, IOException {
        SearchTerm[] terms = term.getTerms();
        if (terms.length > 2) {
            SearchTerm t = terms[0];
            for (int i = 1; i < terms.length; ++i) {
                t = new OrTerm(t, terms[i]);
            }
            term = (OrTerm)t;
            terms = term.getTerms();
        }
        final Argument result = new Argument();
        result.writeAtom("OR");
        if (terms[0] instanceof AndTerm || terms[0] instanceof FlagTerm) {
            result.writeArgument(generateSequence(terms[0], charset));
        }
        else {
            result.append(generateSequence(terms[0], charset));
        }
        if (terms[1] instanceof AndTerm || terms[1] instanceof FlagTerm) {
            result.writeArgument(generateSequence(terms[1], charset));
        }
        else {
            result.append(generateSequence(terms[1], charset));
        }
        return result;
    }
    
    private static Argument not(final NotTerm term, final String charset) throws SearchException, IOException {
        final Argument result = new Argument();
        result.writeAtom("NOT");
        final SearchTerm nterm = term.getTerm();
        if (nterm instanceof AndTerm || nterm instanceof FlagTerm) {
            result.writeArgument(generateSequence(nterm, charset));
        }
        else {
            result.append(generateSequence(nterm, charset));
        }
        return result;
    }
    
    private static Argument header(final HeaderTerm term, final String charset) throws SearchException, IOException {
        final Argument result = new Argument();
        result.writeAtom("HEADER");
        result.writeString(term.getHeaderName());
        result.writeString(term.getPattern(), charset);
        return result;
    }
    
    private static Argument messageid(final MessageIDTerm term, final String charset) throws SearchException, IOException {
        final Argument result = new Argument();
        result.writeAtom("HEADER");
        result.writeString("Message-ID");
        result.writeString(term.getPattern(), charset);
        return result;
    }
    
    private static Argument flag(final FlagTerm term) throws SearchException {
        final boolean set = term.getTestSet();
        final Argument result = new Argument();
        final Flags flags = term.getFlags();
        final Flags.Flag[] sf = flags.getSystemFlags();
        final String[] uf = flags.getUserFlags();
        if (sf.length == 0 && uf.length == 0) {
            throw new SearchException("Invalid FlagTerm");
        }
        for (int i = 0; i < sf.length; ++i) {
            if (sf[i] == Flags.Flag.DELETED) {
                result.writeAtom(set ? "DELETED" : "UNDELETED");
            }
            else if (sf[i] == Flags.Flag.ANSWERED) {
                result.writeAtom(set ? "ANSWERED" : "UNANSWERED");
            }
            else if (sf[i] == Flags.Flag.DRAFT) {
                result.writeAtom(set ? "DRAFT" : "UNDRAFT");
            }
            else if (sf[i] == Flags.Flag.FLAGGED) {
                result.writeAtom(set ? "FLAGGED" : "UNFLAGGED");
            }
            else if (sf[i] == Flags.Flag.RECENT) {
                result.writeAtom(set ? "RECENT" : "OLD");
            }
            else if (sf[i] == Flags.Flag.SEEN) {
                result.writeAtom(set ? "SEEN" : "UNSEEN");
            }
        }
        for (int i = 0; i < uf.length; ++i) {
            result.writeAtom(set ? "KEYWORD" : "UNKEYWORD");
            result.writeAtom(uf[i]);
        }
        return result;
    }
    
    private static Argument from(final String address, final String charset) throws SearchException, IOException {
        final Argument result = new Argument();
        result.writeAtom("FROM");
        result.writeString(address, charset);
        return result;
    }
    
    private static Argument recipient(final Message.RecipientType type, final String address, final String charset) throws SearchException, IOException {
        final Argument result = new Argument();
        if (type == Message.RecipientType.TO) {
            result.writeAtom("TO");
        }
        else if (type == Message.RecipientType.CC) {
            result.writeAtom("CC");
        }
        else {
            if (type != Message.RecipientType.BCC) {
                throw new SearchException("Illegal Recipient type");
            }
            result.writeAtom("BCC");
        }
        result.writeString(address, charset);
        return result;
    }
    
    private static Argument subject(final SubjectTerm term, final String charset) throws SearchException, IOException {
        final Argument result = new Argument();
        result.writeAtom("SUBJECT");
        result.writeString(term.getPattern(), charset);
        return result;
    }
    
    private static Argument body(final BodyTerm term, final String charset) throws SearchException, IOException {
        final Argument result = new Argument();
        result.writeAtom("BODY");
        result.writeString(term.getPattern(), charset);
        return result;
    }
    
    private static Argument size(final SizeTerm term) throws SearchException {
        final Argument result = new Argument();
        switch (term.getComparison()) {
            case 5: {
                result.writeAtom("LARGER");
                break;
            }
            case 2: {
                result.writeAtom("SMALLER");
                break;
            }
            default: {
                throw new SearchException("Cannot handle Comparison");
            }
        }
        result.writeNumber(term.getNumber());
        return result;
    }
    
    private static String toIMAPDate(final Date date) {
        final StringBuffer s = new StringBuffer();
        SearchSequence.cal.setTime(date);
        s.append(SearchSequence.cal.get(5)).append("-");
        s.append(SearchSequence.monthTable[SearchSequence.cal.get(2)]).append('-');
        s.append(SearchSequence.cal.get(1));
        return s.toString();
    }
    
    private static Argument sentdate(final DateTerm term) throws SearchException {
        final Argument result = new Argument();
        final String date = toIMAPDate(term.getDate());
        switch (term.getComparison()) {
            case 5: {
                result.writeAtom("SENTSINCE " + date);
                break;
            }
            case 3: {
                result.writeAtom("SENTON " + date);
                break;
            }
            case 2: {
                result.writeAtom("SENTBEFORE " + date);
                break;
            }
            case 6: {
                result.writeAtom("OR SENTSINCE " + date + " SENTON " + date);
                break;
            }
            case 1: {
                result.writeAtom("OR SENTBEFORE " + date + " SENTON " + date);
                break;
            }
            case 4: {
                result.writeAtom("NOT SENTON " + date);
                break;
            }
            default: {
                throw new SearchException("Cannot handle Date Comparison");
            }
        }
        return result;
    }
    
    private static Argument receiveddate(final DateTerm term) throws SearchException {
        final Argument result = new Argument();
        final String date = toIMAPDate(term.getDate());
        switch (term.getComparison()) {
            case 5: {
                result.writeAtom("SINCE " + date);
                break;
            }
            case 3: {
                result.writeAtom("ON " + date);
                break;
            }
            case 2: {
                result.writeAtom("BEFORE " + date);
                break;
            }
            case 6: {
                result.writeAtom("OR SINCE " + date + " ON " + date);
                break;
            }
            case 1: {
                result.writeAtom("OR BEFORE " + date + " ON " + date);
                break;
            }
            case 4: {
                result.writeAtom("NOT ON " + date);
                break;
            }
            default: {
                throw new SearchException("Cannot handle Date Comparison");
            }
        }
        return result;
    }
    
    static {
        SearchSequence.monthTable = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        SearchSequence.cal = new GregorianCalendar();
    }
}
