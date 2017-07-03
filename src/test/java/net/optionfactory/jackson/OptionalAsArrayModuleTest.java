package net.optionfactory.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class OptionalAsArrayModuleTest {

    final ObjectMapper mapper;

    public OptionalAsArrayModuleTest() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModules(new OptionalAsArrayModule());
    }

    @Test
    public void optionalEmptySerializesToEmptyArray() throws Exception {
        final Container c = new Container(Optional.<String>empty());
        final String json = mapper.writeValueAsString(c);
        Assert.assertEquals("{\"optional\":[]}", json);
    }

    @Test
    public void emptyArrayDeserializesToOptionalEmpty() throws Exception {
        final Container got = mapper.readValue("{\"optional\":[]}", Container.class);
        Assert.assertEquals(Optional.empty(), got.optional);
    }

    @Test
    public void optionalStringSerializesToSingleValueArray() throws Exception {
        final Container c = new Container(Optional.of("a"));
        final String json = mapper.writeValueAsString(c);
        Assert.assertEquals("{\"optional\":[\"a\"]}", json);
    }

    @Test
    public void singleValueArrayDeserializesToOptionalString() throws Exception {
        final Container got = mapper.readValue("{\"optional\":[\"a\"]}", Container.class);
        Assert.assertEquals(Optional.of("a"), got.optional);
    }

    public static class Container {

        public Optional<String> optional;

        public Container() {
        }

        public Container(Optional<String> optional) {
            this.optional = optional;
        }

    }

}
