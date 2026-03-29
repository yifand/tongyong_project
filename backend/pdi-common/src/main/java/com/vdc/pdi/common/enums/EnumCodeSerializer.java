
package com.vdc.pdi.common.enums;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * EnumCode枚举序列化器
 * 将枚举序列化为 {code, message} 对象格式
 */
public class EnumCodeSerializer extends JsonSerializer<EnumCode<?>> {

    @Override
    public void serialize(EnumCode<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        gen.writeStartObject();
        gen.writeObjectField("code", value.getCode());
        gen.writeStringField("message", value.getMessage());
        gen.writeEndObject();
    }
}
