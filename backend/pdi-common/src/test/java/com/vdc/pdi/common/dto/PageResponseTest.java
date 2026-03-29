
package com.vdc.pdi.common.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageResponse 单元测试
 */
class PageResponseTest {

    @Test
    void testOfWithSpringPage() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 25);

        PageResponse<String> response = PageResponse.of(page);

        assertNotNull(response);
        assertEquals(content, response.getList());
        assertEquals(25L, response.getTotal());
        assertEquals(1, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    void testOfWithSpringPageLastPage() {
        List<String> content = Collections.singletonList("item1");
        Page<String> page = new PageImpl<>(content, PageRequest.of(2, 10), 21);

        PageResponse<String> response = PageResponse.of(page);

        assertNotNull(response);
        assertEquals(3, response.getPage());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }

    @Test
    void testOfWithManualParams() {
        List<Integer> content = Arrays.asList(1, 2, 3, 4, 5);

        PageResponse<Integer> response = PageResponse.of(content, 100L, 2, 5);

        assertNotNull(response);
        assertEquals(content, response.getList());
        assertEquals(100L, response.getTotal());
        assertEquals(2, response.getPage());
        assertEquals(5, response.getSize());
        assertEquals(20, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }

    @Test
    void testOfWithEmptyList() {
        Page<String> emptyPage = Page.empty();
        PageResponse<String> response = PageResponse.of(emptyPage);

        assertNotNull(response);
        assertTrue(response.getList().isEmpty());
        assertEquals(0L, response.getTotal());
        assertEquals(1, response.getPage());
        assertEquals(0, response.getSize());
        assertEquals(0, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    void testSettersAndGetters() {
        PageResponse<String> response = new PageResponse<>();
        response.setList(Arrays.asList("a", "b"));
        response.setTotal(100L);
        response.setPage(1);
        response.setSize(10);
        response.setTotalPages(10);
        response.setHasNext(true);
        response.setHasPrevious(false);

        assertEquals(Arrays.asList("a", "b"), response.getList());
        assertEquals(100L, response.getTotal());
        assertEquals(1, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(10, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }
}
