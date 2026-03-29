
package com.vdc.pdi.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JsonUtils 单元测试
 */
class JsonUtilsTest {

    @Test
    void testToJson() {
        String result = JsonUtils.toJson("hello");
        assertEquals("\"hello\"", result);

        result = JsonUtils.toJson(123);
        assertEquals("123", result);

        result = JsonUtils.toJson(Arrays.asList("a", "b", "c"));
        assertEquals("[\"a\",\"b\",\"c\"]", result);

        assertNull(JsonUtils.toJson(null));
    }

    @Test
    void testToPrettyJson() {
        String result = JsonUtils.toPrettyJson(Map.of("key", "value"));
        assertNotNull(result);
        assertTrue(result.contains("{\n"));
        assertTrue(result.contains("\"key\""));
    }

    @Test
    void testFromJson() {
        String json = "\"hello\"";
        String result = JsonUtils.fromJson(json, String.class);
        assertEquals("hello", result);

        json = "123";
        Integer intResult = JsonUtils.fromJson(json, Integer.class);
        assertEquals(123, intResult);
    }

    @Test
    void testFromJsonWithTypeReference() {
        String json = "[\"a\",\"b\",\"c\"]";
        List<String> result = JsonUtils.fromJson(json, new TypeReference<List<String>>() {});
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    @Test
    void testConvert() {
        Map<String, Object> map = Map.of("id", 1, "name", "test");
        TestObject obj = JsonUtils.convert(map, TestObject.class);

        assertNotNull(obj);
        assertEquals(1, obj.getId());
        assertEquals("test", obj.getName());

        assertNull(JsonUtils.convert(null, TestObject.class));
    }

    @Test
    void testIsValidJson() {
        assertTrue(JsonUtils.isValidJson("{\"key\":\"value\"}"));
        assertTrue(JsonUtils.isValidJson("[1,2,3]"));
        assertTrue(JsonUtils.isValidJson("\"string\""));
        assertTrue(JsonUtils.isValidJson("123"));

        assertFalse(JsonUtils.isValidJson(null));
        assertFalse(JsonUtils.isValidJson(""));
        assertFalse(JsonUtils.isValidJson("{invalid json}"));
        assertFalse(JsonUtils.isValidJson("not json"));
    }

    @Test
    void testFromJsonWithEmptyOrNull() {
        assertNull(JsonUtils.fromJson(null, String.class));
        assertNull(JsonUtils.fromJson("", String.class));
    }

    /**
     * 测试对象
     */
    static class TestObject {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
