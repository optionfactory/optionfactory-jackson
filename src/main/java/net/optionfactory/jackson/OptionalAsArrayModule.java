package net.optionfactory.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.util.Optional;

public class OptionalAsArrayModule extends SimpleModule {

    public OptionalAsArrayModule() {
        super("optional-serialized-as-array");
        this.addSerializer(Optional.class, new OptionalToArray());
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addDeserializers(new Deserializers.Base() {
            @Override
            public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
                if (Optional.class.isAssignableFrom(type.getRawClass())) {
                    final JavaType[] types = TypeFactory.defaultInstance().findTypeParameters(type, Optional.class);
                    return new OptionalFromArray(types[0]);
                }
                return null;
            }
        });
    }

    /**
     * Serializes a Java 8 Optional class wrapping its value into an array. When a value is present, it's
     * serialized as a single-element array, otherwise as an empty array. E.g:
     * {@code Optional.of("1")} will be serialized as {@code ["1"]}
     * {@code Optional.empty()} will be serialized as {@code []}
     */
    public static class OptionalToArray extends JsonSerializer<Optional> {

        @Override
        public void serialize(Optional value, JsonGenerator jgen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            jgen.writeStartArray();
            if (value.isPresent()) {
                jgen.writeObject(value.get());
            }
            jgen.writeEndArray();
        }
    }

    /**
     * Deserializes a Java 8 Optional class unwrapping a single-value array. Single-values arrays are deserialized
     * as an {@code Optional} with a value, otherwise as {@code Optional.empty}.
     * {@code ["1"]} will be deserialized as an {@code Optional.of("1")}
     * {@code []} will be deserialized as an {@code Optional.of("1")}
     */
    public static class OptionalFromArray extends JsonDeserializer<Optional<?>> {

        private final JavaType valueType;

        public OptionalFromArray(JavaType valueType) {
            this.valueType = valueType;
        }

        @Override
        public Optional<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            boolean hasValue = false;
            Object value = null;
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                value = parser.getCurrentToken() == JsonToken.VALUE_NULL ? null : context.findContextualValueDeserializer(valueType, null).deserialize(parser, context);
                hasValue = true;
            }
            return hasValue ? Optional.of(value) : Optional.empty();
        }
    }

}
