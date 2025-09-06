package com.temenos.t24.ksa.pdf.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exception handling utility that provides secure logging and error handling.
 * Prevents sensitive information disclosure while maintaining useful error information.
 */
public final class ExceptionUtils {
    private static final Logger LOGGER = Logger.getLogger(ExceptionUtils.class.getName());
    
    private ExceptionUtils() {
        // Prevent instantiation
    }
    
    /**
     * Logs an exception securely without exposing sensitive information.
     * 
     * @param context A description of where the exception occurred
     * @param e The exception to log
     * @return A safe error message for user display
     */
    public static String logSecurely(String context, Exception e) {
        if (e == null) {
            return "Unknown error occurred";
        }
        
        // Log the full exception details for debugging (but not to user output)
        LOGGER.log(Level.WARNING, context + " - Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        
        // Return a generic, safe message
        return "An error occurred during " + context + ". Please check the configuration and try again.";
    }
    
    /**
     * Handles file operation exceptions securely.
     * 
     * @param filePath The file path involved (will be sanitized in logging)
     * @param e The exception
     * @return Safe error message
     */
    public static String handleFileException(String filePath, Exception e) {
        String sanitizedPath = sanitizePathForLogging(filePath);
        LOGGER.log(Level.WARNING, "File operation failed for: " + sanitizedPath + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
        
        if (e instanceof java.io.FileNotFoundException) {
            return "Required file not found. Please check the file path and permissions.";
        } else if (e instanceof java.io.IOException) {
            return "File access error. Please check file permissions and disk space.";
        } else if (e instanceof SecurityException) {
            return "Access denied. Please check file permissions.";
        }
        
        return "File operation failed. Please check the file path and permissions.";
    }
    
    /**
     * Handles PDF creation exceptions securely.
     * 
     * @param operation Description of the PDF operation
     * @param e The exception
     * @return Safe error message
     */
    public static String handlePdfException(String operation, Exception e) {
        LOGGER.log(Level.WARNING, "PDF operation failed during " + operation + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
        
        if (e instanceof RuntimeException && e.getMessage() != null && e.getMessage().contains("PdfException")) {
            return "PDF generation error during " + operation + ". Please check the input data format.";
        } else if (e instanceof java.awt.color.CMMException) {
            return "Color profile error during PDF generation. Please check image files.";
        }
        
        return "PDF generation failed during " + operation + ". Please check the input data.";
    }
    
    /**
     * Handles parsing exceptions securely.
     * 
     * @param input Description of what was being parsed (sanitized)
     * @param e The exception
     * @return Safe error message
     */
    public static String handleParsingException(String input, Exception e) {
        String sanitizedInput = input != null ? input.substring(0, Math.min(50, input.length())) + "..." : "null";
        LOGGER.log(Level.WARNING, "Parsing failed for input: " + sanitizedInput + " - " + e.getClass().getSimpleName() + ": " + e.getMessage());
        
        if (e instanceof NumberFormatException) {
            return "Invalid number format in input data.";
        } else if (e instanceof java.text.ParseException) {
            return "Invalid date/time format in input data.";
        } else if (e instanceof ArrayIndexOutOfBoundsException || e instanceof StringIndexOutOfBoundsException) {
            return "Invalid input data format. Please check the data structure.";
        }
        
        return "Failed to parse input data. Please check the data format.";
    }
    
    /**
     * Sanitizes file paths for safe logging by removing sensitive directory information.
     * 
     * @param path The file path to sanitize
     * @return Sanitized path safe for logging
     */
    private static String sanitizePathForLogging(String path) {
        if (path == null) {
            return "null";
        }
        
        // Only show the filename and immediate directory for security
        String[] parts = path.replace("\\", "/").split("/");
        if (parts.length > 2) {
            return ".../" + parts[parts.length - 2] + "/" + parts[parts.length - 1];
        }
        return path;
    }
}