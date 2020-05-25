package com.mongodb.connection;

import java.net.*;
import java.io.*;
import java.security.*;
import java.util.jar.*;
import com.mongodb.client.*;
import java.nio.charset.*;
import com.mongodb.assertions.*;
import org.bson.io.*;
import org.bson.codecs.*;
import org.bson.*;
import java.util.*;

final class ClientMetadataHelper
{
    public static final BsonDocument CLIENT_METADATA_DOCUMENT;
    private static final String SEPARATOR = "|";
    private static final String APPLICATION_FIELD = "application";
    private static final String APPLICATION_NAME_FIELD = "name";
    private static final String DRIVER_FIELD = "driver";
    private static final String DRIVER_NAME_FIELD = "name";
    private static final String DRIVER_VERSION_FIELD = "version";
    private static final String PLATFORM_FIELD = "platform";
    private static final String OS_FIELD = "os";
    private static final String OS_TYPE_FIELD = "type";
    private static final String OS_NAME_FIELD = "name";
    private static final String OS_ARCHITECTURE_FIELD = "architecture";
    private static final String OS_VERSION_FIELD = "version";
    private static final int MAXIMUM_CLIENT_METADATA_ENCODED_SIZE = 512;
    
    private static String getOperatingSystemType(final String operatingSystemName) {
        if (nameMatches(operatingSystemName, "linux")) {
            return "Linux";
        }
        if (nameMatches(operatingSystemName, "mac")) {
            return "Darwin";
        }
        if (nameMatches(operatingSystemName, "windows")) {
            return "Windows";
        }
        if (nameMatches(operatingSystemName, "hp-ux", "aix", "irix", "solaris", "sunos")) {
            return "Unix";
        }
        return "unknown";
    }
    
    private static boolean nameMatches(final String name, final String... prefixes) {
        for (final String prefix : prefixes) {
            if (name.toLowerCase().startsWith(prefix.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    private static String getDriverVersion() {
        String driverVersion = "unknown";
        try {
            final CodeSource codeSource = InternalStreamConnectionInitializer.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                final String path = codeSource.getLocation().getPath();
                URL url;
                if (path.endsWith(".jar")) {
                    final StringBuilder sb;
                    url = new URL(sb.append("jar:file:").append(path).append("!/").toString());
                    sb = new StringBuilder();
                }
                else {
                    url = null;
                }
                final URL jarUrl = url;
                if (jarUrl != null) {
                    final JarURLConnection jarURLConnection = (JarURLConnection)jarUrl.openConnection();
                    final Manifest manifest = jarURLConnection.getManifest();
                    final String version = (String)manifest.getMainAttributes().get(new Attributes.Name("Build-Version"));
                    if (version != null) {
                        driverVersion = version;
                    }
                }
            }
        }
        catch (SecurityException ex) {}
        catch (IOException ex2) {}
        return driverVersion;
    }
    
    static BsonDocument createClientMetadataDocument(final String applicationName) {
        return createClientMetadataDocument(applicationName, null);
    }
    
    static BsonDocument createClientMetadataDocument(final String applicationName, final MongoDriverInformation mongoDriverInformation) {
        return createClientMetadataDocument(applicationName, mongoDriverInformation, ClientMetadataHelper.CLIENT_METADATA_DOCUMENT);
    }
    
    static BsonDocument createClientMetadataDocument(final String applicationName, final MongoDriverInformation mongoDriverInformation, final BsonDocument templateDocument) {
        if (applicationName != null) {
            Assertions.isTrueArgument("applicationName UTF-8 encoding length <= 128", applicationName.getBytes(Charset.forName("UTF-8")).length <= 128);
        }
        BsonDocument document = templateDocument.clone();
        if (applicationName != null) {
            document.append("application", new BsonDocument("name", new BsonString(applicationName)));
        }
        if (mongoDriverInformation != null) {
            addDriverInformation(mongoDriverInformation, document);
        }
        if (clientMetadataDocumentTooLarge(document)) {
            final BsonDocument operatingSystemDocument = document.getDocument("os", null);
            if (operatingSystemDocument != null) {
                operatingSystemDocument.remove("version");
                operatingSystemDocument.remove("architecture");
                operatingSystemDocument.remove("name");
            }
            if (operatingSystemDocument == null || clientMetadataDocumentTooLarge(document)) {
                document.remove("platform");
                if (clientMetadataDocumentTooLarge(document)) {
                    document = new BsonDocument("driver", templateDocument.getDocument("driver"));
                    document.append("os", new BsonDocument("type", new BsonString("unknown")));
                    if (clientMetadataDocumentTooLarge(document)) {
                        document = null;
                    }
                }
            }
        }
        return document;
    }
    
    private static BsonDocument addDriverInformation(final MongoDriverInformation mongoDriverInformation, final BsonDocument document) {
        final MongoDriverInformation driverInformation = getDriverInformation(mongoDriverInformation);
        final BsonDocument driverMetadataDocument = new BsonDocument("name", listToBsonString(driverInformation.getDriverNames())).append("version", listToBsonString(driverInformation.getDriverVersions()));
        document.append("driver", driverMetadataDocument);
        document.append("platform", listToBsonString(driverInformation.getDriverPlatforms()));
        return document;
    }
    
    static boolean clientMetadataDocumentTooLarge(final BsonDocument document) {
        final BasicOutputBuffer buffer = new BasicOutputBuffer(512);
        new BsonDocumentCodec().encode((BsonWriter)new BsonBinaryWriter(buffer), document, EncoderContext.builder().build());
        return buffer.getPosition() > 512;
    }
    
    static MongoDriverInformation getDriverInformation(final MongoDriverInformation mongoDriverInformation) {
        final MongoDriverInformation.Builder builder = (mongoDriverInformation != null) ? MongoDriverInformation.builder(mongoDriverInformation) : MongoDriverInformation.builder();
        return builder.driverName("mongo-java-driver").driverVersion(getDriverVersion()).driverPlatform(String.format("Java/%s/%s", System.getProperty("java.vendor", "unknown-vendor"), System.getProperty("java.runtime.version", "unknown-version"))).build();
    }
    
    static BsonString listToBsonString(final List<String> listOfStrings) {
        final StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (final String val : listOfStrings) {
            if (i > 0) {
                stringBuilder.append("|");
            }
            stringBuilder.append(val);
            ++i;
        }
        return new BsonString(stringBuilder.toString());
    }
    
    private ClientMetadataHelper() {
    }
    
    static {
        CLIENT_METADATA_DOCUMENT = new BsonDocument();
        final BsonDocument driverMetadataDocument = addDriverInformation(null, new BsonDocument());
        ClientMetadataHelper.CLIENT_METADATA_DOCUMENT.append("driver", driverMetadataDocument.get("driver"));
        try {
            final String operatingSystemName = System.getProperty("os.name", "unknown");
            ClientMetadataHelper.CLIENT_METADATA_DOCUMENT.append("os", new BsonDocument().append("type", new BsonString(getOperatingSystemType(operatingSystemName))).append("name", new BsonString(operatingSystemName)).append("architecture", new BsonString(System.getProperty("os.arch", "unknown"))).append("version", new BsonString(System.getProperty("os.version", "unknown")))).append("platform", driverMetadataDocument.get("platform", new BsonString("")));
        }
        catch (SecurityException ex) {}
    }
}
