package com.temenos.t24.ksa.pdf.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the SecurityUtils class to verify proper input validation and path security.
 */
class SecurityUtilsTest {

    @Test
    void testPathSafety() {
        // Test safe paths
        assertTrue(SecurityUtils.isPathSafe("test-resources/fonts/font.ttf"));
        assertTrue(SecurityUtils.isPathSafe("test-output/invoice.pdf"));
        assertTrue(SecurityUtils.isPathSafe("/tmp/temp.txt"));
        
        // Test unsafe paths (directory traversal)
        assertFalse(SecurityUtils.isPathSafe("../../../etc/passwd"));
        assertFalse(SecurityUtils.isPathSafe("test-resources/../../../etc/passwd"));
        assertFalse(SecurityUtils.isPathSafe("..\\windows\\system32\\config"));
        
        // Test null and empty inputs
        assertFalse(SecurityUtils.isPathSafe(null));
        assertFalse(SecurityUtils.isPathSafe(""));
        assertFalse(SecurityUtils.isPathSafe("   "));
    }

    @Test
    void testInputSanitization() {
        // Test normal input
        assertEquals("Hello World", SecurityUtils.sanitizeInput("Hello World"));
        
        // Test input with control characters (null byte removes character)
        assertEquals("HelloWorld", SecurityUtils.sanitizeInput("Hello\u0000World"));
        assertEquals("TestString", SecurityUtils.sanitizeInput("Test\u001fString"));
        
        // Test null input
        assertNull(SecurityUtils.sanitizeInput(null));
    }

    @Test
    void testArrayIndexValidation() {
        String[] testArray = {"item1", "item2", "item3"};
        
        // Test valid indices
        assertTrue(SecurityUtils.isValidArrayIndex(testArray, 0));
        assertTrue(SecurityUtils.isValidArrayIndex(testArray, 1));
        assertTrue(SecurityUtils.isValidArrayIndex(testArray, 2));
        
        // Test invalid indices
        assertFalse(SecurityUtils.isValidArrayIndex(testArray, -1));
        assertFalse(SecurityUtils.isValidArrayIndex(testArray, 3));
        assertFalse(SecurityUtils.isValidArrayIndex(testArray, 100));
        
        // Test null array
        assertFalse(SecurityUtils.isValidArrayIndex(null, 0));
    }

    @Test
    void testDirectoryAccessibility() {
        // Test existing directories
        assertTrue(SecurityUtils.isDirectoryAccessible("test-resources"));
        assertTrue(SecurityUtils.isDirectoryAccessible("test-output"));
        
        // Test non-existent directory
        assertFalse(SecurityUtils.isDirectoryAccessible("non-existent-directory"));
        
        // Test unsafe path
        assertFalse(SecurityUtils.isDirectoryAccessible("../../../etc"));
    }
}