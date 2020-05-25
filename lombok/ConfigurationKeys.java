package lombok;

import lombok.core.configuration.*;
import java.util.*;

public class ConfigurationKeys
{
    @Deprecated
    public static final ConfigurationKey<Boolean> ADD_GENERATED_ANNOTATIONS;
    public static final ConfigurationKey<Boolean> ADD_JAVAX_GENERATED_ANNOTATIONS;
    public static final ConfigurationKey<Boolean> ADD_LOMBOK_GENERATED_ANNOTATIONS;
    public static final ConfigurationKey<Boolean> ADD_FINDBUGS_SUPPRESSWARNINGS_ANNOTATIONS;
    public static final ConfigurationKey<FlagUsageType> ANY_CONSTRUCTOR_FLAG_USAGE;
    public static final ConfigurationKey<Boolean> ANY_CONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES;
    public static final ConfigurationKey<FlagUsageType> ALL_ARGS_CONSTRUCTOR_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> NO_ARGS_CONSTRUCTOR_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> REQUIRED_ARGS_CONSTRUCTOR_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> DATA_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> VALUE_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> GETTER_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> GETTER_LAZY_FLAG_USAGE;
    public static final ConfigurationKey<Boolean> GETTER_CONSEQUENT_BOOLEAN;
    public static final ConfigurationKey<FlagUsageType> SETTER_FLAG_USAGE;
    public static final ConfigurationKey<Boolean> EQUALS_AND_HASH_CODE_DO_NOT_USE_GETTERS;
    public static final ConfigurationKey<CallSuperType> EQUALS_AND_HASH_CODE_CALL_SUPER;
    public static final ConfigurationKey<FlagUsageType> EQUALS_AND_HASH_CODE_FLAG_USAGE;
    public static final ConfigurationKey<Boolean> TO_STRING_DO_NOT_USE_GETTERS;
    public static final ConfigurationKey<FlagUsageType> TO_STRING_FLAG_USAGE;
    public static final ConfigurationKey<Boolean> TO_STRING_INCLUDE_FIELD_NAMES;
    public static final ConfigurationKey<FlagUsageType> BUILDER_FLAG_USAGE;
    public static final ConfigurationKey<Boolean> SINGULAR_USE_GUAVA;
    public static final ConfigurationKey<Boolean> SINGULAR_AUTO;
    public static final ConfigurationKey<FlagUsageType> CLEANUP_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> DELEGATE_FLAG_USAGE;
    public static final ConfigurationKey<NullCheckExceptionType> NON_NULL_EXCEPTION_TYPE;
    public static final ConfigurationKey<FlagUsageType> NON_NULL_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> SNEAKY_THROWS_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> SYNCHRONIZED_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> VAL_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> VAR_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> LOG_ANY_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> LOG_COMMONS_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> LOG_JUL_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> LOG_LOG4J_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> LOG_LOG4J2_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> LOG_SLF4J_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> LOG_XSLF4J_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> LOG_JBOSSLOG_FLAG_USAGE;
    public static final ConfigurationKey<String> LOG_ANY_FIELD_NAME;
    public static final ConfigurationKey<Boolean> LOG_ANY_FIELD_IS_STATIC;
    public static final ConfigurationKey<FlagUsageType> EXPERIMENTAL_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> ACCESSORS_FLAG_USAGE;
    public static final ConfigurationKey<List<String>> ACCESSORS_PREFIX;
    public static final ConfigurationKey<Boolean> ACCESSORS_CHAIN;
    public static final ConfigurationKey<Boolean> ACCESSORS_FLUENT;
    public static final ConfigurationKey<FlagUsageType> EXTENSION_METHOD_FLAG_USAGE;
    public static final ConfigurationKey<Boolean> FIELD_DEFAULTS_PRIVATE_EVERYWHERE;
    public static final ConfigurationKey<Boolean> FIELD_DEFAULTS_FINAL_EVERYWHERE;
    public static final ConfigurationKey<FlagUsageType> FIELD_DEFAULTS_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> HELPER_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> UTILITY_CLASS_FLAG_USAGE;
    public static final ConfigurationKey<FlagUsageType> WITHER_FLAG_USAGE;
    public static final ConfigurationKey<Boolean> STOP_BUBBLING;
    
    private ConfigurationKeys() {
    }
    
    static {
        ADD_GENERATED_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.addGeneratedAnnotation", "Generate @javax.annotation.Generated on all generated code (default: true). Deprecated, use 'lombok.addJavaxGeneratedAnnotation' instead.") {};
        ADD_JAVAX_GENERATED_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.addJavaxGeneratedAnnotation", "Generate @javax.annotation.Generated on all generated code (default: follow lombok.addGeneratedAnnotation).") {};
        ADD_LOMBOK_GENERATED_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.addLombokGeneratedAnnotation", "Generate @lombok.Generated on all generated code (default: false).") {};
        ADD_FINDBUGS_SUPPRESSWARNINGS_ANNOTATIONS = new ConfigurationKey<Boolean>("lombok.extern.findbugs.addSuppressFBWarnings", "Generate @edu.umd.cs.findbugs.annotations.SuppressFBWArnings on all generated code (default: false).") {};
        ANY_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.anyConstructor.flagUsage", "Emit a warning or error if any of the XxxArgsConstructor annotations are used.") {};
        ANY_CONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES = new ConfigurationKey<Boolean>("lombok.anyConstructor.suppressConstructorProperties", "Suppress the generation of @ConstructorProperties for generated constructors (default: false).") {};
        ALL_ARGS_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.allArgsConstructor.flagUsage", "Emit a warning or error if @AllArgsConstructor is used.") {};
        NO_ARGS_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.noArgsConstructor.flagUsage", "Emit a warning or error if @NoArgsConstructor is used.") {};
        REQUIRED_ARGS_CONSTRUCTOR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.requiredArgsConstructor.flagUsage", "Emit a warning or error if @RequiredArgsConstructor is used.") {};
        DATA_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.data.flagUsage", "Emit a warning or error if @Data is used.") {};
        VALUE_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.value.flagUsage", "Emit a warning or error if @Value is used.") {};
        GETTER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.getter.flagUsage", "Emit a warning or error if @Getter is used.") {};
        GETTER_LAZY_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.getter.lazy.flagUsage", "Emit a warning or error if @Getter(lazy=true) is used.") {};
        GETTER_CONSEQUENT_BOOLEAN = new ConfigurationKey<Boolean>("lombok.getter.noIsPrefix", "If true, generate and use getFieldName() for boolean getters instead of isFieldName().") {};
        SETTER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.setter.flagUsage", "Emit a warning or error if @Setter is used.") {};
        EQUALS_AND_HASH_CODE_DO_NOT_USE_GETTERS = new ConfigurationKey<Boolean>("lombok.equalsAndHashCode.doNotUseGetters", "Don't call the getters but use the fields directly in the generated equals and hashCode method (default = false).") {};
        EQUALS_AND_HASH_CODE_CALL_SUPER = new ConfigurationKey<CallSuperType>("lombok.equalsAndHashCode.callSuper", "When generating equals and hashCode for classes that don't extend Object, either automatically take into account superclass implementation (call), or don't (skip), or warn and don't (warn). (default = warn).") {};
        EQUALS_AND_HASH_CODE_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.equalsAndHashCode.flagUsage", "Emit a warning or error if @EqualsAndHashCode is used.") {};
        TO_STRING_DO_NOT_USE_GETTERS = new ConfigurationKey<Boolean>("lombok.toString.doNotUseGetters", "Don't call the getters but use the fields directly in the generated toString method (default = false).") {};
        TO_STRING_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.toString.flagUsage", "Emit a warning or error if @ToString is used.") {};
        TO_STRING_INCLUDE_FIELD_NAMES = new ConfigurationKey<Boolean>("lombok.toString.includeFieldNames", "Include the field names in the generated toString method (default = true).") {};
        BUILDER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.builder.flagUsage", "Emit a warning or error if @Builder is used.") {};
        SINGULAR_USE_GUAVA = new ConfigurationKey<Boolean>("lombok.singular.useGuava", "Generate backing immutable implementations for @Singular on java.util.* types by using guava's ImmutableList, etc. Normally java.util's mutable types are used and wrapped to make them immutable.") {};
        SINGULAR_AUTO = new ConfigurationKey<Boolean>("lombok.singular.auto", "If true (default): Automatically singularize the assumed-to-be-plural name of your variable/parameter when using {@code @Singular}.") {};
        CLEANUP_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.cleanup.flagUsage", "Emit a warning or error if @Cleanup is used.") {};
        DELEGATE_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.delegate.flagUsage", "Emit a warning or error if @Delegate is used.") {};
        NON_NULL_EXCEPTION_TYPE = new ConfigurationKey<NullCheckExceptionType>("lombok.nonNull.exceptionType", "The type of the exception to throw if a passed-in argument is null (Default: NullPointerException).") {};
        NON_NULL_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.nonNull.flagUsage", "Emit a warning or error if @NonNull is used.") {};
        SNEAKY_THROWS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.sneakyThrows.flagUsage", "Emit a warning or error if @SneakyThrows is used.") {};
        SYNCHRONIZED_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.synchronized.flagUsage", "Emit a warning or error if @Synchronized is used.") {};
        VAL_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.val.flagUsage", "Emit a warning or error if 'val' is used.") {};
        VAR_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.var.flagUsage", "Emit a warning or error if 'var' is used.") {};
        LOG_ANY_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.flagUsage", "Emit a warning or error if any of the log annotations is used.") {};
        LOG_COMMONS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.apacheCommons.flagUsage", "Emit a warning or error if @CommonsLog is used.") {};
        LOG_JUL_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.javaUtilLogging.flagUsage", "Emit a warning or error if @Log is used.") {};
        LOG_LOG4J_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.log4j.flagUsage", "Emit a warning or error if @Log4j is used.") {};
        LOG_LOG4J2_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.log4j2.flagUsage", "Emit a warning or error if @Log4j2 is used.") {};
        LOG_SLF4J_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.slf4j.flagUsage", "Emit a warning or error if @Slf4j is used.") {};
        LOG_XSLF4J_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.xslf4j.flagUsage", "Emit a warning or error if @XSlf4j is used.") {};
        LOG_JBOSSLOG_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.log.jbosslog.flagUsage", "Emit a warning or error if @JBossLog is used.") {};
        LOG_ANY_FIELD_NAME = new ConfigurationKey<String>("lombok.log.fieldName", "Use this name for the generated logger fields (default: 'log').") {};
        LOG_ANY_FIELD_IS_STATIC = new ConfigurationKey<Boolean>("lombok.log.fieldIsStatic", "Make the generated logger fields static (default: true).") {};
        EXPERIMENTAL_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.experimental.flagUsage", "Emit a warning or error if an experimental feature is used.") {};
        ACCESSORS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.accessors.flagUsage", "Emit a warning or error if @Accessors is used.") {};
        ACCESSORS_PREFIX = new ConfigurationKey<List<String>>("lombok.accessors.prefix", "Strip this field prefix, like 'f' or 'm_', from the names of generated getters and setters.") {};
        ACCESSORS_CHAIN = new ConfigurationKey<Boolean>("lombok.accessors.chain", "Generate setters that return 'this' instead of 'void' (default: false).") {};
        ACCESSORS_FLUENT = new ConfigurationKey<Boolean>("lombok.accessors.fluent", "Generate getters and setters using only the field name (no get/set prefix) (default: false).") {};
        EXTENSION_METHOD_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.extensionMethod.flagUsage", "Emit a warning or error if @ExtensionMethod is used.") {};
        FIELD_DEFAULTS_PRIVATE_EVERYWHERE = new ConfigurationKey<Boolean>("lombok.fieldDefaults.defaultPrivate", "If true, fields without any access modifier, in any file (lombok annotated or not) are marked as private. Use @PackagePrivate or an explicit modifier to override this.") {};
        FIELD_DEFAULTS_FINAL_EVERYWHERE = new ConfigurationKey<Boolean>("lombok.fieldDefaults.defaultFinal", "If true, fields, in any file (lombok annotated or not) are marked as final. Use @NonFinal to override this.") {};
        FIELD_DEFAULTS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.fieldDefaults.flagUsage", "Emit a warning or error if @FieldDefaults is used.") {};
        HELPER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.helper.flagUsage", "Emit a warning or error if @Helper is used.") {};
        UTILITY_CLASS_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.utilityClass.flagUsage", "Emit a warning or error if @UtilityClass is used.") {};
        WITHER_FLAG_USAGE = new ConfigurationKey<FlagUsageType>("lombok.wither.flagUsage", "Emit a warning or error if @Wither is used.") {};
        STOP_BUBBLING = new ConfigurationKey<Boolean>("config.stopBubbling", "Tell the configuration system it should stop looking for other configuration files (default: false).") {};
    }
}
