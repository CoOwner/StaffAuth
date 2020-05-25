package com.mongodb;

import java.util.concurrent.*;
import java.net.*;
import java.io.*;
import com.mongodb.diagnostics.logging.*;
import java.util.*;

public class ConnectionString
{
    private static final String PREFIX = "mongodb://";
    private static final String UTF_8 = "UTF-8";
    private static final Logger LOGGER;
    private final MongoCredential credentials;
    private final List<String> hosts;
    private final String database;
    private final String collection;
    private final String connectionString;
    private ReadPreference readPreference;
    private WriteConcern writeConcern;
    private ReadConcern readConcern;
    private Integer minConnectionPoolSize;
    private Integer maxConnectionPoolSize;
    private Integer threadsAllowedToBlockForConnectionMultiplier;
    private Integer maxWaitTime;
    private Integer maxConnectionIdleTime;
    private Integer maxConnectionLifeTime;
    private Integer connectTimeout;
    private Integer socketTimeout;
    private Boolean sslEnabled;
    private Boolean sslInvalidHostnameAllowed;
    private String streamType;
    private String requiredReplicaSetName;
    private Integer serverSelectionTimeout;
    private Integer localThreshold;
    private Integer heartbeatFrequency;
    private String applicationName;
    private static final Set<String> GENERAL_OPTIONS_KEYS;
    private static final Set<String> AUTH_KEYS;
    private static final Set<String> READ_PREFERENCE_KEYS;
    private static final Set<String> WRITE_CONCERN_KEYS;
    private static final Set<String> ALL_KEYS;
    
    public ConnectionString(final String connectionString) {
        this.connectionString = connectionString;
        if (!connectionString.startsWith("mongodb://")) {
            throw new IllegalArgumentException(String.format("The connection string is invalid. Connection strings must start with '%s'", "mongodb://"));
        }
        String unprocessedConnectionString = connectionString.substring("mongodb://".length());
        String userAndHostInformation = null;
        int idx = unprocessedConnectionString.lastIndexOf("/");
        if (idx == -1) {
            if (unprocessedConnectionString.contains("?")) {
                throw new IllegalArgumentException("The connection string contains options without trailing slash");
            }
            userAndHostInformation = unprocessedConnectionString;
            unprocessedConnectionString = "";
        }
        else {
            userAndHostInformation = unprocessedConnectionString.substring(0, idx);
            unprocessedConnectionString = unprocessedConnectionString.substring(idx + 1);
        }
        String userInfo = null;
        String hostIdentifier = null;
        String userName = null;
        char[] password = null;
        idx = userAndHostInformation.lastIndexOf("@");
        if (idx > 0) {
            userInfo = userAndHostInformation.substring(0, idx);
            hostIdentifier = userAndHostInformation.substring(idx + 1);
            final int colonCount = this.countOccurrences(userInfo, ":");
            if (userInfo.contains("@") || colonCount > 1) {
                throw new IllegalArgumentException("The connection string contains invalid user information. If the username or password contains a colon (:) or an at-sign (@) then it must be urlencoded");
            }
            if (colonCount == 0) {
                userName = this.urldecode(userInfo);
            }
            else {
                idx = userInfo.indexOf(":");
                userName = this.urldecode(userInfo.substring(0, idx));
                password = this.urldecode(userInfo.substring(idx + 1), true).toCharArray();
            }
        }
        else {
            hostIdentifier = userAndHostInformation;
        }
        this.hosts = Collections.unmodifiableList((List<? extends String>)this.parseHosts(Arrays.asList(hostIdentifier.split(","))));
        String nsPart = null;
        idx = unprocessedConnectionString.indexOf("?");
        if (idx == -1) {
            nsPart = unprocessedConnectionString;
            unprocessedConnectionString = "";
        }
        else {
            nsPart = unprocessedConnectionString.substring(0, idx);
            unprocessedConnectionString = unprocessedConnectionString.substring(idx + 1);
        }
        if (nsPart.length() > 0) {
            nsPart = this.urldecode(nsPart);
            idx = nsPart.indexOf(".");
            if (idx < 0) {
                this.database = nsPart;
                this.collection = null;
            }
            else {
                this.database = nsPart.substring(0, idx);
                this.collection = nsPart.substring(idx + 1);
            }
        }
        else {
            this.database = null;
            this.collection = null;
        }
        final Map<String, List<String>> optionsMap = this.parseOptions(unprocessedConnectionString);
        this.translateOptions(optionsMap);
        this.credentials = this.createCredentials(optionsMap, userName, password);
        this.warnOnUnsupportedOptions(optionsMap);
    }
    
    private void warnOnUnsupportedOptions(final Map<String, List<String>> optionsMap) {
        for (final String key : optionsMap.keySet()) {
            if (!ConnectionString.ALL_KEYS.contains(key) && ConnectionString.LOGGER.isWarnEnabled()) {
                ConnectionString.LOGGER.warn(String.format("Unsupported option '%s' in the connection string '%s'.", key, this.connectionString));
            }
        }
    }
    
    private void translateOptions(final Map<String, List<String>> optionsMap) {
        for (final String key : ConnectionString.GENERAL_OPTIONS_KEYS) {
            final String value = this.getLastValue(optionsMap, key);
            if (value == null) {
                continue;
            }
            if (key.equals("maxpoolsize")) {
                this.maxConnectionPoolSize = this.parseInteger(value, "maxpoolsize");
            }
            else if (key.equals("minpoolsize")) {
                this.minConnectionPoolSize = this.parseInteger(value, "minpoolsize");
            }
            else if (key.equals("maxidletimems")) {
                this.maxConnectionIdleTime = this.parseInteger(value, "maxidletimems");
            }
            else if (key.equals("maxlifetimems")) {
                this.maxConnectionLifeTime = this.parseInteger(value, "maxlifetimems");
            }
            else if (key.equals("waitqueuemultiple")) {
                this.threadsAllowedToBlockForConnectionMultiplier = this.parseInteger(value, "waitqueuemultiple");
            }
            else if (key.equals("waitqueuetimeoutms")) {
                this.maxWaitTime = this.parseInteger(value, "waitqueuetimeoutms");
            }
            else if (key.equals("connecttimeoutms")) {
                this.connectTimeout = this.parseInteger(value, "connecttimeoutms");
            }
            else if (key.equals("sockettimeoutms")) {
                this.socketTimeout = this.parseInteger(value, "sockettimeoutms");
            }
            else if (key.equals("sslinvalidhostnameallowed") && this.parseBoolean(value, "sslinvalidhostnameallowed")) {
                this.sslInvalidHostnameAllowed = true;
            }
            else if (key.equals("ssl") && this.parseBoolean(value, "ssl")) {
                this.sslEnabled = true;
            }
            else if (key.equals("streamtype")) {
                this.streamType = value;
            }
            else if (key.equals("replicaset")) {
                this.requiredReplicaSetName = value;
            }
            else if (key.equals("readconcernlevel")) {
                this.readConcern = new ReadConcern(ReadConcernLevel.fromString(value));
            }
            else if (key.equals("serverselectiontimeoutms")) {
                this.serverSelectionTimeout = this.parseInteger(value, "serverselectiontimeoutms");
            }
            else if (key.equals("localthresholdms")) {
                this.localThreshold = this.parseInteger(value, "localthresholdms");
            }
            else if (key.equals("heartbeatfrequencyms")) {
                this.heartbeatFrequency = this.parseInteger(value, "heartbeatfrequencyms");
            }
            else {
                if (!key.equals("appname")) {
                    continue;
                }
                this.applicationName = value;
            }
        }
        this.writeConcern = this.createWriteConcern(optionsMap);
        this.readPreference = this.createReadPreference(optionsMap);
    }
    
    private WriteConcern createWriteConcern(final Map<String, List<String>> optionsMap) {
        Boolean safe = null;
        String w = null;
        Integer wTimeout = null;
        Boolean fsync = null;
        Boolean journal = null;
        for (final String key : ConnectionString.WRITE_CONCERN_KEYS) {
            final String value = this.getLastValue(optionsMap, key);
            if (value == null) {
                continue;
            }
            if (key.equals("safe")) {
                safe = this.parseBoolean(value, "safe");
            }
            else if (key.equals("w")) {
                w = value;
            }
            else if (key.equals("wtimeoutms")) {
                wTimeout = Integer.parseInt(value);
            }
            else if (key.equals("fsync")) {
                fsync = this.parseBoolean(value, "fsync");
            }
            else {
                if (!key.equals("journal")) {
                    continue;
                }
                journal = this.parseBoolean(value, "journal");
            }
        }
        return this.buildWriteConcern(safe, w, wTimeout, fsync, journal);
    }
    
    private ReadPreference createReadPreference(final Map<String, List<String>> optionsMap) {
        String readPreferenceType = null;
        final List<TagSet> tagSetList = new ArrayList<TagSet>();
        long maxStalenessSeconds = -1L;
        for (final String key : ConnectionString.READ_PREFERENCE_KEYS) {
            final String value = this.getLastValue(optionsMap, key);
            if (value == null) {
                continue;
            }
            if (key.equals("readpreference")) {
                readPreferenceType = value;
            }
            else if (key.equals("maxstalenessseconds")) {
                maxStalenessSeconds = this.parseInteger(value, "maxstalenessseconds");
            }
            else {
                if (!key.equals("readpreferencetags")) {
                    continue;
                }
                for (final String cur : optionsMap.get(key)) {
                    final TagSet tagSet = this.getTags(cur.trim());
                    tagSetList.add(tagSet);
                }
            }
        }
        return this.buildReadPreference(readPreferenceType, tagSetList, maxStalenessSeconds);
    }
    
    private MongoCredential createCredentials(final Map<String, List<String>> optionsMap, final String userName, final char[] password) {
        AuthenticationMechanism mechanism = null;
        String authSource = (this.database == null) ? "admin" : this.database;
        String gssapiServiceName = null;
        String authMechanismProperties = null;
        for (final String key : ConnectionString.AUTH_KEYS) {
            final String value = this.getLastValue(optionsMap, key);
            if (value == null) {
                continue;
            }
            if (key.equals("authmechanism")) {
                mechanism = AuthenticationMechanism.fromMechanismName(value);
            }
            else if (key.equals("authsource")) {
                authSource = value;
            }
            else if (key.equals("gssapiservicename")) {
                gssapiServiceName = value;
            }
            else {
                if (!key.endsWith("authmechanismproperties")) {
                    continue;
                }
                authMechanismProperties = value;
            }
        }
        MongoCredential credential = null;
        if (mechanism != null) {
            switch (mechanism) {
                case GSSAPI: {
                    credential = MongoCredential.createGSSAPICredential(userName);
                    if (gssapiServiceName != null) {
                        credential = credential.withMechanismProperty("SERVICE_NAME", gssapiServiceName);
                        break;
                    }
                    break;
                }
                case PLAIN: {
                    credential = MongoCredential.createPlainCredential(userName, authSource, password);
                    break;
                }
                case MONGODB_CR: {
                    credential = MongoCredential.createMongoCRCredential(userName, authSource, password);
                    break;
                }
                case MONGODB_X509: {
                    credential = MongoCredential.createMongoX509Credential(userName);
                    break;
                }
                case SCRAM_SHA_1: {
                    credential = MongoCredential.createScramSha1Credential(userName, authSource, password);
                    break;
                }
                default: {
                    throw new UnsupportedOperationException(String.format("The connection string contains an invalid authentication mechanism'. '%s' is not a supported authentication mechanism", mechanism));
                }
            }
        }
        else if (userName != null) {
            credential = MongoCredential.createCredential(userName, authSource, password);
        }
        if (credential != null && authMechanismProperties != null) {
            for (final String part : authMechanismProperties.split(",")) {
                final String[] mechanismPropertyKeyValue = part.split(":");
                if (mechanismPropertyKeyValue.length != 2) {
                    throw new IllegalArgumentException(String.format("The connection string contains invalid authentication properties. '%s' is not a key value pair", part));
                }
                final String key2 = mechanismPropertyKeyValue[0].trim().toLowerCase();
                final String value2 = mechanismPropertyKeyValue[1].trim();
                if (key2.equals("canonicalize_host_name")) {
                    credential = credential.withMechanismProperty(key2, Boolean.valueOf(value2));
                }
                else {
                    credential = credential.withMechanismProperty(key2, value2);
                }
            }
        }
        return credential;
    }
    
    private String getLastValue(final Map<String, List<String>> optionsMap, final String key) {
        final List<String> valueList = optionsMap.get(key);
        if (valueList == null) {
            return null;
        }
        return valueList.get(valueList.size() - 1);
    }
    
    private Map<String, List<String>> parseOptions(final String optionsPart) {
        final Map<String, List<String>> optionsMap = new HashMap<String, List<String>>();
        if (optionsPart.length() == 0) {
            return optionsMap;
        }
        for (final String part : optionsPart.split("&|;")) {
            if (part.length() != 0) {
                final int idx = part.indexOf("=");
                if (idx < 0) {
                    throw new IllegalArgumentException(String.format("The connection string contains an invalid option '%s'. '%s' is missing the value delimiter eg '%s=value'", optionsPart, part, part));
                }
                final String key = part.substring(0, idx).toLowerCase();
                final String value = part.substring(idx + 1);
                List<String> valueList = optionsMap.get(key);
                if (valueList == null) {
                    valueList = new ArrayList<String>(1);
                }
                valueList.add(this.urldecode(value));
                optionsMap.put(key, valueList);
            }
        }
        if (optionsMap.containsKey("wtimeout") && !optionsMap.containsKey("wtimeoutms")) {
            optionsMap.put("wtimeoutms", optionsMap.remove("wtimeout"));
            if (ConnectionString.LOGGER.isWarnEnabled()) {
                ConnectionString.LOGGER.warn("Uri option 'wtimeout' has been deprecated, use 'wtimeoutms' instead.");
            }
        }
        if (optionsMap.containsKey("slaveok") && !optionsMap.containsKey("readpreference")) {
            final String readPreference = this.parseBoolean(this.getLastValue(optionsMap, "slaveok"), "slaveok") ? "secondaryPreferred" : "primary";
            optionsMap.put("readpreference", Collections.singletonList(readPreference));
            if (ConnectionString.LOGGER.isWarnEnabled()) {
                ConnectionString.LOGGER.warn("Uri option 'slaveok' has been deprecated, use 'readpreference' instead.");
            }
        }
        if (optionsMap.containsKey("j") && !optionsMap.containsKey("journal")) {
            optionsMap.put("journal", optionsMap.remove("j"));
            if (ConnectionString.LOGGER.isWarnEnabled()) {
                ConnectionString.LOGGER.warn("Uri option 'j' has been deprecated, use 'journal' instead.");
            }
        }
        return optionsMap;
    }
    
    private ReadPreference buildReadPreference(final String readPreferenceType, final List<TagSet> tagSetList, final long maxStalenessSeconds) {
        if (readPreferenceType != null) {
            if (tagSetList.isEmpty() && maxStalenessSeconds == -1L) {
                return ReadPreference.valueOf(readPreferenceType);
            }
            if (maxStalenessSeconds == -1L) {
                return ReadPreference.valueOf(readPreferenceType, tagSetList);
            }
            return ReadPreference.valueOf(readPreferenceType, tagSetList, maxStalenessSeconds, TimeUnit.SECONDS);
        }
        else {
            if (!tagSetList.isEmpty() || maxStalenessSeconds != -1L) {
                throw new IllegalArgumentException("Read preference mode must be specified if either read preference tags or max staleness is specified");
            }
            return null;
        }
    }
    
    private WriteConcern buildWriteConcern(final Boolean safe, final String w, final Integer wTimeout, final Boolean fsync, final Boolean journal) {
        WriteConcern retVal = null;
        if (w != null || wTimeout != null || fsync != null || journal != null) {
            if (w == null) {
                retVal = WriteConcern.ACKNOWLEDGED;
            }
            else {
                try {
                    retVal = new WriteConcern(Integer.parseInt(w));
                }
                catch (NumberFormatException e) {
                    retVal = new WriteConcern(w);
                }
            }
            if (wTimeout != null) {
                retVal = retVal.withWTimeout(wTimeout, TimeUnit.MILLISECONDS);
            }
            if (journal != null) {
                retVal = retVal.withJournal(journal);
            }
            if (fsync != null) {
                retVal = retVal.withFsync(fsync);
            }
            return retVal;
        }
        if (safe != null) {
            if (safe) {
                retVal = WriteConcern.ACKNOWLEDGED;
            }
            else {
                retVal = WriteConcern.UNACKNOWLEDGED;
            }
        }
        return retVal;
    }
    
    private TagSet getTags(final String tagSetString) {
        final List<Tag> tagList = new ArrayList<Tag>();
        if (tagSetString.length() > 0) {
            for (final String tag : tagSetString.split(",")) {
                final String[] tagKeyValuePair = tag.split(":");
                if (tagKeyValuePair.length != 2) {
                    throw new IllegalArgumentException(String.format("The connection string contains an invalid read preference tag. '%s' is not a key value pair", tagSetString));
                }
                tagList.add(new Tag(tagKeyValuePair[0].trim(), tagKeyValuePair[1].trim()));
            }
        }
        return new TagSet(tagList);
    }
    
    private boolean parseBoolean(final String input, final String key) {
        final String trimmedInput = input.trim();
        final boolean isTrue = trimmedInput.length() > 0 && (trimmedInput.equals("1") || trimmedInput.toLowerCase().equals("true") || trimmedInput.toLowerCase().equals("yes"));
        if (!input.equals("true") && !input.equals("false") && ConnectionString.LOGGER.isWarnEnabled()) {
            ConnectionString.LOGGER.warn(String.format("Deprecated boolean value ('%s') in the connection string for '%s', please update to %s=%s", input, key, key, isTrue));
        }
        return isTrue;
    }
    
    private int parseInteger(final String input, final String key) {
        try {
            return Integer.parseInt(input);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("The connection string contains an invalid value for '%s'. '%s' is not a valid integer", key, input));
        }
    }
    
    private List<String> parseHosts(final List<String> rawHosts) {
        if (rawHosts.size() == 0) {
            throw new IllegalArgumentException("The connection string must contain at least one host");
        }
        final List<String> hosts = new ArrayList<String>();
        for (final String host : rawHosts) {
            if (host.length() == 0) {
                throw new IllegalArgumentException(String.format("The connection string contains an empty host '%s'. ", rawHosts));
            }
            if (host.endsWith(".sock")) {
                throw new IllegalArgumentException(String.format("The connection string contains an invalid host '%s'. Unix Domain Socket which is not supported by the Java driver", host));
            }
            if (host.startsWith("[")) {
                if (!host.contains("]")) {
                    throw new IllegalArgumentException(String.format("The connection string contains an invalid host '%s'. IPv6 address literals must be enclosed in '[' and ']' according to RFC 2732", host));
                }
                final int idx = host.indexOf("]:");
                if (idx != -1) {
                    this.validatePort(host, host.substring(idx + 2));
                }
            }
            else {
                final int colonCount = this.countOccurrences(host, ":");
                if (colonCount > 1) {
                    throw new IllegalArgumentException(String.format("The connection string contains an invalid host '%s'. Reserved characters such as ':' must be escaped according RFC 2396. Any IPv6 address literal must be enclosed in '[' and ']' according to RFC 2732.", host));
                }
                if (colonCount == 1) {
                    this.validatePort(host, host.substring(host.indexOf(":") + 1));
                }
            }
            hosts.add(host);
        }
        Collections.sort(hosts);
        return hosts;
    }
    
    private void validatePort(final String host, final String port) {
        boolean invalidPort = false;
        try {
            final int portInt = Integer.parseInt(port);
            if (portInt <= 0 || portInt > 65535) {
                invalidPort = true;
            }
        }
        catch (NumberFormatException e) {
            invalidPort = true;
        }
        if (invalidPort) {
            throw new IllegalArgumentException(String.format("The connection string contains an invalid host '%s'. The port '%s' is not a valid, it must be an integer between 0 and 65535", host, port));
        }
    }
    
    private int countOccurrences(final String haystack, final String needle) {
        return haystack.length() - haystack.replace(needle, "").length();
    }
    
    private String urldecode(final String input) {
        return this.urldecode(input, false);
    }
    
    private String urldecode(final String input, final boolean password) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            if (password) {
                throw new IllegalArgumentException("The connection string contained unsupported characters in the password.");
            }
            throw new IllegalArgumentException(String.format("The connection string contained unsupported characters: '%s'.Decoding produced the following error: %s", input, e.getMessage()));
        }
    }
    
    public String getUsername() {
        return (this.credentials != null) ? this.credentials.getUserName() : null;
    }
    
    public char[] getPassword() {
        return (char[])((this.credentials != null) ? this.credentials.getPassword() : null);
    }
    
    public List<String> getHosts() {
        return this.hosts;
    }
    
    public String getDatabase() {
        return this.database;
    }
    
    public String getCollection() {
        return this.collection;
    }
    
    @Deprecated
    public String getURI() {
        return this.getConnectionString();
    }
    
    public String getConnectionString() {
        return this.connectionString;
    }
    
    public List<MongoCredential> getCredentialList() {
        return (this.credentials != null) ? Collections.singletonList(this.credentials) : Collections.emptyList();
    }
    
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public Integer getMinConnectionPoolSize() {
        return this.minConnectionPoolSize;
    }
    
    public Integer getMaxConnectionPoolSize() {
        return this.maxConnectionPoolSize;
    }
    
    public Integer getThreadsAllowedToBlockForConnectionMultiplier() {
        return this.threadsAllowedToBlockForConnectionMultiplier;
    }
    
    public Integer getMaxWaitTime() {
        return this.maxWaitTime;
    }
    
    public Integer getMaxConnectionIdleTime() {
        return this.maxConnectionIdleTime;
    }
    
    public Integer getMaxConnectionLifeTime() {
        return this.maxConnectionLifeTime;
    }
    
    public Integer getConnectTimeout() {
        return this.connectTimeout;
    }
    
    public Integer getSocketTimeout() {
        return this.socketTimeout;
    }
    
    public Boolean getSslEnabled() {
        return this.sslEnabled;
    }
    
    public String getStreamType() {
        return this.streamType;
    }
    
    public Boolean getSslInvalidHostnameAllowed() {
        return this.sslInvalidHostnameAllowed;
    }
    
    public String getRequiredReplicaSetName() {
        return this.requiredReplicaSetName;
    }
    
    public Integer getServerSelectionTimeout() {
        return this.serverSelectionTimeout;
    }
    
    public Integer getLocalThreshold() {
        return this.localThreshold;
    }
    
    public Integer getHeartbeatFrequency() {
        return this.heartbeatFrequency;
    }
    
    public String getApplicationName() {
        return this.applicationName;
    }
    
    @Override
    public String toString() {
        return this.connectionString;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConnectionString)) {
            return false;
        }
        final ConnectionString that = (ConnectionString)o;
        Label_0054: {
            if (this.collection != null) {
                if (this.collection.equals(that.collection)) {
                    break Label_0054;
                }
            }
            else if (that.collection == null) {
                break Label_0054;
            }
            return false;
        }
        Label_0087: {
            if (this.connectTimeout != null) {
                if (this.connectTimeout.equals(that.connectTimeout)) {
                    break Label_0087;
                }
            }
            else if (that.connectTimeout == null) {
                break Label_0087;
            }
            return false;
        }
        Label_0120: {
            if (this.credentials != null) {
                if (this.credentials.equals(that.credentials)) {
                    break Label_0120;
                }
            }
            else if (that.credentials == null) {
                break Label_0120;
            }
            return false;
        }
        Label_0153: {
            if (this.database != null) {
                if (this.database.equals(that.database)) {
                    break Label_0153;
                }
            }
            else if (that.database == null) {
                break Label_0153;
            }
            return false;
        }
        if (!this.hosts.equals(that.hosts)) {
            return false;
        }
        Label_0204: {
            if (this.maxConnectionIdleTime != null) {
                if (this.maxConnectionIdleTime.equals(that.maxConnectionIdleTime)) {
                    break Label_0204;
                }
            }
            else if (that.maxConnectionIdleTime == null) {
                break Label_0204;
            }
            return false;
        }
        Label_0237: {
            if (this.maxConnectionLifeTime != null) {
                if (this.maxConnectionLifeTime.equals(that.maxConnectionLifeTime)) {
                    break Label_0237;
                }
            }
            else if (that.maxConnectionLifeTime == null) {
                break Label_0237;
            }
            return false;
        }
        Label_0270: {
            if (this.maxConnectionPoolSize != null) {
                if (this.maxConnectionPoolSize.equals(that.maxConnectionPoolSize)) {
                    break Label_0270;
                }
            }
            else if (that.maxConnectionPoolSize == null) {
                break Label_0270;
            }
            return false;
        }
        Label_0303: {
            if (this.maxWaitTime != null) {
                if (this.maxWaitTime.equals(that.maxWaitTime)) {
                    break Label_0303;
                }
            }
            else if (that.maxWaitTime == null) {
                break Label_0303;
            }
            return false;
        }
        Label_0336: {
            if (this.minConnectionPoolSize != null) {
                if (this.minConnectionPoolSize.equals(that.minConnectionPoolSize)) {
                    break Label_0336;
                }
            }
            else if (that.minConnectionPoolSize == null) {
                break Label_0336;
            }
            return false;
        }
        Label_0369: {
            if (this.readPreference != null) {
                if (this.readPreference.equals(that.readPreference)) {
                    break Label_0369;
                }
            }
            else if (that.readPreference == null) {
                break Label_0369;
            }
            return false;
        }
        Label_0402: {
            if (this.requiredReplicaSetName != null) {
                if (this.requiredReplicaSetName.equals(that.requiredReplicaSetName)) {
                    break Label_0402;
                }
            }
            else if (that.requiredReplicaSetName == null) {
                break Label_0402;
            }
            return false;
        }
        Label_0435: {
            if (this.socketTimeout != null) {
                if (this.socketTimeout.equals(that.socketTimeout)) {
                    break Label_0435;
                }
            }
            else if (that.socketTimeout == null) {
                break Label_0435;
            }
            return false;
        }
        Label_0468: {
            if (this.sslEnabled != null) {
                if (this.sslEnabled.equals(that.sslEnabled)) {
                    break Label_0468;
                }
            }
            else if (that.sslEnabled == null) {
                break Label_0468;
            }
            return false;
        }
        Label_0501: {
            if (this.threadsAllowedToBlockForConnectionMultiplier != null) {
                if (this.threadsAllowedToBlockForConnectionMultiplier.equals(that.threadsAllowedToBlockForConnectionMultiplier)) {
                    break Label_0501;
                }
            }
            else if (that.threadsAllowedToBlockForConnectionMultiplier == null) {
                break Label_0501;
            }
            return false;
        }
        Label_0534: {
            if (this.writeConcern != null) {
                if (this.writeConcern.equals(that.writeConcern)) {
                    break Label_0534;
                }
            }
            else if (that.writeConcern == null) {
                break Label_0534;
            }
            return false;
        }
        if (this.applicationName != null) {
            if (this.applicationName.equals(that.applicationName)) {
                return true;
            }
        }
        else if (that.applicationName == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.credentials != null) ? this.credentials.hashCode() : 0;
        result = 31 * result + this.hosts.hashCode();
        result = 31 * result + ((this.database != null) ? this.database.hashCode() : 0);
        result = 31 * result + ((this.collection != null) ? this.collection.hashCode() : 0);
        result = 31 * result + ((this.readPreference != null) ? this.readPreference.hashCode() : 0);
        result = 31 * result + ((this.writeConcern != null) ? this.writeConcern.hashCode() : 0);
        result = 31 * result + ((this.minConnectionPoolSize != null) ? this.minConnectionPoolSize.hashCode() : 0);
        result = 31 * result + ((this.maxConnectionPoolSize != null) ? this.maxConnectionPoolSize.hashCode() : 0);
        result = 31 * result + ((this.threadsAllowedToBlockForConnectionMultiplier != null) ? this.threadsAllowedToBlockForConnectionMultiplier.hashCode() : 0);
        result = 31 * result + ((this.maxWaitTime != null) ? this.maxWaitTime.hashCode() : 0);
        result = 31 * result + ((this.maxConnectionIdleTime != null) ? this.maxConnectionIdleTime.hashCode() : 0);
        result = 31 * result + ((this.maxConnectionLifeTime != null) ? this.maxConnectionLifeTime.hashCode() : 0);
        result = 31 * result + ((this.connectTimeout != null) ? this.connectTimeout.hashCode() : 0);
        result = 31 * result + ((this.socketTimeout != null) ? this.socketTimeout.hashCode() : 0);
        result = 31 * result + ((this.sslEnabled != null) ? this.sslEnabled.hashCode() : 0);
        result = 31 * result + ((this.requiredReplicaSetName != null) ? this.requiredReplicaSetName.hashCode() : 0);
        result = 31 * result + ((this.applicationName != null) ? this.applicationName.hashCode() : 0);
        return result;
    }
    
    static {
        LOGGER = Loggers.getLogger("uri");
        GENERAL_OPTIONS_KEYS = new HashSet<String>();
        AUTH_KEYS = new HashSet<String>();
        READ_PREFERENCE_KEYS = new HashSet<String>();
        WRITE_CONCERN_KEYS = new HashSet<String>();
        ALL_KEYS = new HashSet<String>();
        ConnectionString.GENERAL_OPTIONS_KEYS.add("minpoolsize");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("maxpoolsize");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("waitqueuemultiple");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("waitqueuetimeoutms");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("connecttimeoutms");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("maxidletimems");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("maxlifetimems");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("sockettimeoutms");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("sockettimeoutms");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("ssl");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("streamtype");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("sslinvalidhostnameallowed");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("replicaset");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("readconcernlevel");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("serverselectiontimeoutms");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("localthresholdms");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("heartbeatfrequencyms");
        ConnectionString.GENERAL_OPTIONS_KEYS.add("appname");
        ConnectionString.READ_PREFERENCE_KEYS.add("readpreference");
        ConnectionString.READ_PREFERENCE_KEYS.add("readpreferencetags");
        ConnectionString.READ_PREFERENCE_KEYS.add("maxstalenessseconds");
        ConnectionString.WRITE_CONCERN_KEYS.add("safe");
        ConnectionString.WRITE_CONCERN_KEYS.add("w");
        ConnectionString.WRITE_CONCERN_KEYS.add("wtimeoutms");
        ConnectionString.WRITE_CONCERN_KEYS.add("fsync");
        ConnectionString.WRITE_CONCERN_KEYS.add("journal");
        ConnectionString.AUTH_KEYS.add("authmechanism");
        ConnectionString.AUTH_KEYS.add("authsource");
        ConnectionString.AUTH_KEYS.add("gssapiservicename");
        ConnectionString.AUTH_KEYS.add("authmechanismproperties");
        ConnectionString.ALL_KEYS.addAll(ConnectionString.GENERAL_OPTIONS_KEYS);
        ConnectionString.ALL_KEYS.addAll(ConnectionString.AUTH_KEYS);
        ConnectionString.ALL_KEYS.addAll(ConnectionString.READ_PREFERENCE_KEYS);
        ConnectionString.ALL_KEYS.addAll(ConnectionString.WRITE_CONCERN_KEYS);
    }
}
