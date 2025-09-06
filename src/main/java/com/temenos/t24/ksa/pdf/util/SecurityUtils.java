package com.temenos.t24.ksa.pdf.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Security utility class for handling file operations and path validation.
 * Provides protection against directory traversal attacks and ensures secure file access.
 */
public final class SecurityUtils {
    private static final Logger LOGGER = Logger.getLogger(SecurityUtils.class.getName());
    
    // Define allowed base directories for security
    private static final String[] ALLOWED_BASE_DIRS = {
        "test-resources",
        "test-output",
        "/tmp",
        System.getProperty("java.io.tmpdir")
    };
    
    private SecurityUtils() {
        // Prevent instantiation
    }
    
    /**
     * Validates that a file path is safe and within allowed directories.
     * Prevents directory traversal attacks.
     * 
     * @param filePath The file path to validate
     * @return true if the path is safe, false otherwise
     */
    public static boolean isPathSafe(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Normalize the path to resolve any .. or . components
            Path normalizedPath = Paths.get(filePath).normalize();
            String normalizedStr = normalizedPath.toString();
            
            // Check for directory traversal patterns
            if (normalizedStr.contains("..") || normalizedStr.contains("./") || normalizedStr.contains(".\\")) {
                LOGGER.warning("Potential directory traversal detected in path: " + filePath);
                return false;
            }
            
            // Check if path is within allowed directories
            for (String allowedDir : ALLOWED_BASE_DIRS) {
                if (normalizedStr.startsWith(allowedDir) || 
                    normalizedStr.startsWith(Paths.get(allowedDir).toAbsolutePath().toString())) {
                    return true;
                }
            }
            
            // For relative paths, check if they resolve to allowed directories
            Path absolutePath = normalizedPath.toAbsolutePath();
            for (String allowedDir : ALLOWED_BASE_DIRS) {
                Path allowedPath = Paths.get(allowedDir).toAbsolutePath();
                if (absolutePath.startsWith(allowedPath)) {
                    return true;
                }
            }
            
        } catch (Exception e) {
            LOGGER.warning("Error validating path: " + filePath + " - " + e.getMessage());
            return false;
        }
        
        LOGGER.warning("Path not in allowed directories: " + filePath);
        return false;
    }
    
    /**
     * Validates that a file exists and is readable.
     * 
     * @param filePath The file path to check
     * @return true if file exists and is readable, false otherwise
     */
    public static boolean isFileAccessible(String filePath) {
        if (!isPathSafe(filePath)) {
            return false;
        }
        
        try {
            File file = new File(filePath);
            return file.exists() && file.isFile() && file.canRead();
        } catch (SecurityException e) {
            LOGGER.warning("Security exception accessing file: " + filePath + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates that a directory exists and is accessible.
     * 
     * @param dirPath The directory path to check
     * @return true if directory exists and is accessible, false otherwise
     */
    public static boolean isDirectoryAccessible(String dirPath) {
        if (!isPathSafe(dirPath)) {
            return false;
        }
        
        try {
            File dir = new File(dirPath);
            return dir.exists() && dir.isDirectory() && dir.canRead();
        } catch (SecurityException e) {
            LOGGER.warning("Security exception accessing directory: " + dirPath + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sanitizes input string by removing potentially dangerous characters.
     * 
     * @param input The input string to sanitize
     * @return Sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove null bytes and other control characters that could cause issues
        return input.replaceAll("[\u0000-\u001f\u007f-\u009f]", "");
    }
    
    /**
     * Validates array bounds before accessing array elements.
     * 
     * @param array The array to check
     * @param index The index to validate
     * @return true if index is valid for the array, false otherwise
     */
    public static boolean isValidArrayIndex(Object[] array, int index) {
        return array != null && index >= 0 && index < array.length;
    }
}