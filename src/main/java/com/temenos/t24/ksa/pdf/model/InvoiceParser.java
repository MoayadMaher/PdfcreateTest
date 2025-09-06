package com.temenos.t24.ksa.pdf.model;

import com.temenos.t24.ksa.pdf.util.SecurityUtils;
import com.temenos.t24.ksa.pdf.util.ExceptionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class to parse the raw argument string received from T24 into a structured
 * InvoiceData object. This parser understands the <fm>, <sm>, <sm1> and <vm>
 * delimiters used in the input.
 */
public final class InvoiceParser {
    private static final Logger LOGGER = Logger.getLogger(InvoiceParser.class.getName());
    
    private InvoiceParser() {
        // Prevent instantiation
    }

    public static InvoiceData parse(String args) {
        // Input validation
        if (args == null || args.trim().isEmpty()) {
            throw new IllegalArgumentException("Input arguments cannot be null or empty");
        }
        
        // Sanitize input to remove control characters
        args = SecurityUtils.sanitizeInput(args);
        
        InvoiceData data = new InvoiceData();
        String[] argsArray = args.split("<fm>");
        
        // Validate minimum required fields
        if (argsArray.length < 6) {
            throw new IllegalArgumentException("Invalid input format: missing required fields");
        }
        
        data.path = SecurityUtils.sanitizeInput(argsArray[0]);

        String[] taxBillDetailsArr = argsArray[1].split("<sm>");
        String[] custDetailsArr    = argsArray[2].split("<sm>");
        List<String> taxDetailsArrList = Arrays.asList(argsArray[3].split("<vm>"));
        String[] taxTotalDetailsArr = argsArray[4].split("<sm>");
        String[] taxIbanDetArr      = argsArray[5].split("<sm>");

        // Validate file paths if provided
        data.arabicFontPath = argsArray.length > 6 ? SecurityUtils.sanitizeInput(argsArray[6]) : null;
        data.logoPath       = argsArray.length > 7 ? SecurityUtils.sanitizeInput(argsArray[7]) : null;
        data.footerPath     = argsArray.length > 8 ? SecurityUtils.sanitizeInput(argsArray[8]) : null;

        // Header details with bounds checking
        InvoiceHeader header = new InvoiceHeader();
        header.invoiceNumber    = SecurityUtils.isValidArrayIndex(taxBillDetailsArr, 0) ? SecurityUtils.sanitizeInput(taxBillDetailsArr[0]) : null;
        header.invoiceDateTime  = SecurityUtils.isValidArrayIndex(taxBillDetailsArr, 1) ? SecurityUtils.sanitizeInput(taxBillDetailsArr[1]) : null;
        header.serviceStartDate = SecurityUtils.isValidArrayIndex(taxBillDetailsArr, 2) ? SecurityUtils.sanitizeInput(taxBillDetailsArr[2]) : null;
        header.serviceEndDate   = SecurityUtils.isValidArrayIndex(taxBillDetailsArr, 3) ? SecurityUtils.sanitizeInput(taxBillDetailsArr[3]) : null;
        data.header = header;

        CustomerDetails customer = new CustomerDetails();
        customer.vatNumber      = SecurityUtils.isValidArrayIndex(custDetailsArr, 0) ? SecurityUtils.sanitizeInput(custDetailsArr[0]) : null;
        customer.vatGroupNumber = SecurityUtils.isValidArrayIndex(custDetailsArr, 1) ? SecurityUtils.sanitizeInput(custDetailsArr[1]) : null;
        // Names: english and arabic
        if (SecurityUtils.isValidArrayIndex(custDetailsArr, 2)) {
            String[] nameParts = custDetailsArr[2].split("<sm1>");
            customer.englishName = SecurityUtils.isValidArrayIndex(nameParts, 0) ? SecurityUtils.sanitizeInput(nameParts[0]) : null;
            customer.arabicName  = SecurityUtils.isValidArrayIndex(nameParts, 1) ? SecurityUtils.sanitizeInput(nameParts[1]) : null;
        }
        // Addresses: english and arabic
        if (SecurityUtils.isValidArrayIndex(custDetailsArr, 3)) {
            String[] addrParts = custDetailsArr[3].split("<sm1>");
            customer.englishAddress = SecurityUtils.isValidArrayIndex(addrParts, 0) ? SecurityUtils.sanitizeInput(addrParts[0]) : null;
            customer.arabicAddress  = SecurityUtils.isValidArrayIndex(addrParts, 1) ? SecurityUtils.sanitizeInput(addrParts[1]) : null;
        }
        // Cities: english and arabic
        if (SecurityUtils.isValidArrayIndex(custDetailsArr, 4)) {
            String[] cityParts = custDetailsArr[4].split("<sm1>");
            customer.englishCity = SecurityUtils.isValidArrayIndex(cityParts, 0) ? SecurityUtils.sanitizeInput(cityParts[0]) : null;
            customer.arabicCity  = SecurityUtils.isValidArrayIndex(cityParts, 1) ? SecurityUtils.sanitizeInput(cityParts[1]) : null;
        }
        // Countries: english and arabic
        if (SecurityUtils.isValidArrayIndex(custDetailsArr, 5)) {
            String[] countryParts = custDetailsArr[5].split("<sm1>");
            customer.englishCountry = SecurityUtils.isValidArrayIndex(countryParts, 0) ? SecurityUtils.sanitizeInput(countryParts[0]) : null;
            customer.arabicCountry  = SecurityUtils.isValidArrayIndex(countryParts, 1) ? SecurityUtils.sanitizeInput(countryParts[1]) : null;
        }
        data.customer = customer;

        // Totals with bounds checking
        InvoiceTotals totals = new InvoiceTotals();
        totals.totalExcludingVat = SecurityUtils.isValidArrayIndex(taxTotalDetailsArr, 0) ? SecurityUtils.sanitizeInput(taxTotalDetailsArr[0]) : null;
        totals.totalDiscount     = SecurityUtils.isValidArrayIndex(taxTotalDetailsArr, 1) ? SecurityUtils.sanitizeInput(taxTotalDetailsArr[1]) : null;
        totals.totalVat          = SecurityUtils.isValidArrayIndex(taxTotalDetailsArr, 2) ? SecurityUtils.sanitizeInput(taxTotalDetailsArr[2]) : null;
        totals.amountIncludesVat = SecurityUtils.isValidArrayIndex(taxTotalDetailsArr, 3) ? SecurityUtils.sanitizeInput(taxTotalDetailsArr[3]) : null;
        data.totals = totals;

        // IBAN / Tax registration details with validation
        IbanDetails iban = new IbanDetails();
        if (SecurityUtils.isValidArrayIndex(taxIbanDetArr, 0)) {
            iban.taxRegistrationNumber = SecurityUtils.sanitizeInput(taxIbanDetArr[0]);
            if (SecurityUtils.isValidArrayIndex(taxIbanDetArr, 1)) {
                iban.ibanNumber = SecurityUtils.sanitizeInput(taxIbanDetArr[1]);
            }
        }
        data.iban = iban;

        // Line items with enhanced validation
        List<InvoiceLineItem> items = new ArrayList<>();
        for (String itemStr : taxDetailsArrList) {
            if (itemStr == null || itemStr.trim().isEmpty()) continue;
            
            try {
                String[] taxDetailsArr = itemStr.split("<sm>");
                if (taxDetailsArr.length < 2) {
                    LOGGER.warning("Skipping invalid line item with insufficient data: " + itemStr);
                    continue;
                }
                
                InvoiceLineItem item = new InvoiceLineItem();
                // taxDetailsArr[0] = supply date (yyyyMMdd)
                item.supplyDate = SecurityUtils.isValidArrayIndex(taxDetailsArr, 0) ? SecurityUtils.sanitizeInput(taxDetailsArr[0]) : null;

                // Enhanced validation for description parsing
                if (SecurityUtils.isValidArrayIndex(taxDetailsArr, 1)) {
                    String[] descArr = taxDetailsArr[1].split("<sm1>");
                    // first part is Arabic, second part (if any) is English
                    item.arabicDescription = SecurityUtils.isValidArrayIndex(descArr, 0) ? SecurityUtils.sanitizeInput(descArr[0]) : null;
                    item.englishDescription = SecurityUtils.isValidArrayIndex(descArr, 1) ? SecurityUtils.sanitizeInput(descArr[1]) : null;
                }

                item.unitPrice         = SecurityUtils.isValidArrayIndex(taxDetailsArr, 2) ? SecurityUtils.sanitizeInput(taxDetailsArr[2]) : null;
                item.quantity          = SecurityUtils.isValidArrayIndex(taxDetailsArr, 3) ? SecurityUtils.sanitizeInput(taxDetailsArr[3]) : null;
                item.discount          = SecurityUtils.isValidArrayIndex(taxDetailsArr, 4) ? SecurityUtils.sanitizeInput(taxDetailsArr[4]) : null;
                item.totalExcludingTax = SecurityUtils.isValidArrayIndex(taxDetailsArr, 5) ? SecurityUtils.sanitizeInput(taxDetailsArr[5]) : null;
                item.rate              = SecurityUtils.isValidArrayIndex(taxDetailsArr, 6) ? SecurityUtils.sanitizeInput(taxDetailsArr[6]) : null;
                item.taxAmount         = SecurityUtils.isValidArrayIndex(taxDetailsArr, 7) ? SecurityUtils.sanitizeInput(taxDetailsArr[7]) : null;
                item.totalPrice        = SecurityUtils.isValidArrayIndex(taxDetailsArr, 8) ? SecurityUtils.sanitizeInput(taxDetailsArr[8]) : null;

                items.add(item);
            } catch (Exception e) {
                String errorMsg = ExceptionUtils.handleParsingException("line item", e);
                LOGGER.warning("Error parsing line item: " + errorMsg);
                // Continue processing other items rather than failing completely
            }
        }
        data.lineItems = items;

        return data;
    }
}