package com.mongodb.gridfs;

import java.io.*;
import java.security.*;
import com.mongodb.util.*;
import com.mongodb.*;

public class CLI
{
    private static String host;
    private static String db;
    private static Mongo mongo;
    private static GridFS gridFS;
    
    private static void printUsage() {
        System.out.println("Usage : [--bucket bucketname] action");
        System.out.println("  where  action is one of:");
        System.out.println("      list                      : lists all files in the store");
        System.out.println("      put filename              : puts the file filename into the store");
        System.out.println("      get filename1 filename2   : gets filename1 from store and sends to filename2");
        System.out.println("      md5 filename              : does an md5 hash on a file in the db (for testing)");
    }
    
    private static Mongo getMongo() throws Exception {
        if (CLI.mongo == null) {
            CLI.mongo = new MongoClient(CLI.host);
        }
        return CLI.mongo;
    }
    
    private static GridFS getGridFS() throws Exception {
        if (CLI.gridFS == null) {
            CLI.gridFS = new GridFS(getMongo().getDB(CLI.db));
        }
        return CLI.gridFS;
    }
    
    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            printUsage();
            return;
        }
        for (int i = 0; i < args.length; ++i) {
            final String s = args[i];
            if (s.equals("--db")) {
                CLI.db = args[i + 1];
                ++i;
            }
            else if (s.equals("--host")) {
                CLI.host = args[i + 1];
                ++i;
            }
            else {
                if (s.equals("help")) {
                    printUsage();
                    return;
                }
                if (s.equals("list")) {
                    final GridFS fs = getGridFS();
                    System.out.printf("%-60s %-10s%n", "Filename", "Length");
                    final DBCursor fileListCursor = fs.getFileList();
                    try {
                        while (fileListCursor.hasNext()) {
                            final DBObject o = fileListCursor.next();
                            System.out.printf("%-60s %-10d%n", o.get("filename"), ((Number)o.get("length")).longValue());
                        }
                    }
                    finally {
                        fileListCursor.close();
                    }
                    return;
                }
                if (s.equals("get")) {
                    final GridFS fs = getGridFS();
                    final String fn = args[i + 1];
                    final GridFSDBFile f = fs.findOne(fn);
                    if (f == null) {
                        System.err.println("can't find file: " + fn);
                        return;
                    }
                    f.writeTo(f.getFilename());
                    return;
                }
                else {
                    if (s.equals("put")) {
                        final GridFS fs = getGridFS();
                        final String fn = args[i + 1];
                        final GridFSInputFile f2 = fs.createFile(new File(fn));
                        f2.save();
                        f2.validate();
                        return;
                    }
                    if (!s.equals("md5")) {
                        System.err.println("unknown option: " + s);
                        return;
                    }
                    final GridFS fs = getGridFS();
                    final String fn = args[i + 1];
                    final GridFSDBFile f = fs.findOne(fn);
                    if (f == null) {
                        System.err.println("can't find file: " + fn);
                        return;
                    }
                    final MessageDigest md5 = MessageDigest.getInstance("MD5");
                    md5.reset();
                    int read = 0;
                    final DigestInputStream is = new DigestInputStream(f.getInputStream(), md5);
                    try {
                        while (is.read() >= 0) {
                            ++read;
                            final int r = is.read(new byte[17]);
                            if (r < 0) {
                                break;
                            }
                            read += r;
                        }
                    }
                    finally {
                        is.close();
                    }
                    final byte[] digest = md5.digest();
                    System.out.println("length: " + read + " md5: " + Util.toHex(digest));
                    return;
                }
            }
        }
    }
    
    static {
        CLI.host = "127.0.0.1";
        CLI.db = "test";
        CLI.mongo = null;
    }
}
