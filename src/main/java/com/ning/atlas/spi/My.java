package com.ning.atlas.spi;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

@JsonSerialize(using = My.MySerializer.class)
public class My
{
    private static ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Object> attributes = new ConcurrentSkipListMap<String, Object>();

    public My()
    {
        this(Collections.<String, Object>emptyMap());
    }

    public My(Map<String, Object> attributes)
    {
        this.attributes.putAll(attributes);
    }

    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Map<String, Object> asMap() {
        return ImmutableMap.copyOf(attributes);
    }

    public Object get(String key)
    {
        return attributes.get(key);
    }

    public static class MySerializer extends JsonSerializer<My>
    {
        @Override
        public void serialize(My value, JsonGenerator jgen, SerializerProvider provider) throws IOException
        {
            jgen.writeStartObject();
            for (Map.Entry<String, Object> entry : value.attributes.entrySet()) {
                jgen.writeFieldName(entry.getKey());
                jgen.writeObject(entry.getValue());
            }
            jgen.writeEndObject();
        }
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this, true);
    }

}
