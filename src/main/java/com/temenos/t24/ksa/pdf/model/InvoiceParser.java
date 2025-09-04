package com.temenos.t24.ksa.pdf.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to parse the raw argument string received from T24 into a structured
 * InvoiceData object. This parser understands the <fm>, <sm>, <sm1> and <vm>
 * delimiters used in the input.
 */
public final class InvoiceParser {
    private InvoiceParser() {
        // Prevent instantiation
    }

    public static InvoiceData parse(String args) {
        InvoiceData data = new InvoiceData();
        String[] argsArray = args.split("<fm>");
        data.path = argsArray[0];

        String[] taxBillDetailsArr = argsArray[1].split("<sm>");
        String[] custDetailsArr    = argsArray[2].split("<sm>");
        List<String> taxDetailsArrList = Arrays.asList(argsArray[3].split("<vm>"));
        String[] taxTotalDetailsArr = argsArray[4].split("<sm>");
        String[] taxIbanDetArr      = argsArray[5].split("<sm>");


        data.arabicFontPath = argsArray.length > 6 ? argsArray[6] : null;
        data.logoPath       = argsArray.length > 7 ? argsArray[7] : null;
        data.footerPath     = argsArray.length > 8 ? argsArray[8] : null;

        // Header details
        InvoiceHeader header = new InvoiceHeader();
        header.invoiceNumber    = taxBillDetailsArr.length > 0 ? taxBillDetailsArr[0] : null;
        header.invoiceDateTime  = taxBillDetailsArr.length > 1 ? taxBillDetailsArr[1] : null;
        header.serviceStartDate = taxBillDetailsArr.length > 2 ? taxBillDetailsArr[2] : null;
        header.serviceEndDate   = taxBillDetailsArr.length > 3 ? taxBillDetailsArr[3] : null;
        data.header = header;

        CustomerDetails customer = new CustomerDetails();
        customer.vatNumber      = custDetailsArr.length > 0 ? custDetailsArr[0] : null;
        customer.vatGroupNumber = custDetailsArr.length > 1 ? custDetailsArr[1] : null;
        // Names: english and arabic
        if (custDetailsArr.length > 2) {
            String[] nameParts = custDetailsArr[2].split("<sm1>");
            customer.englishName = nameParts.length > 0 ? nameParts[0] : null;
            customer.arabicName  = nameParts.length > 1 ? nameParts[1] : null;
        }
        // Addresses: english and arabic
        if (custDetailsArr.length > 3) {
            String[] addrParts = custDetailsArr[3].split("<sm1>");
            customer.englishAddress = addrParts.length > 0 ? addrParts[0] : null;
            customer.arabicAddress  = addrParts.length > 1 ? addrParts[1] : null;
        }
        // Cities: english and arabic
        if (custDetailsArr.length > 4) {
            String[] cityParts = custDetailsArr[4].split("<sm1>");
            customer.englishCity = cityParts.length > 0 ? cityParts[0] : null;
            customer.arabicCity  = cityParts.length > 1 ? cityParts[1] : null;
        }
        // Countries: english and arabic
        if (custDetailsArr.length > 5) {
            String[] countryParts = custDetailsArr[5].split("<sm1>");
            customer.englishCountry = countryParts.length > 0 ? countryParts[0] : null;
            customer.arabicCountry  = countryParts.length > 1 ? countryParts[1] : null;
        }
        data.customer = customer;

        // Totals
        InvoiceTotals totals = new InvoiceTotals();
        totals.totalExcludingVat = taxTotalDetailsArr.length > 0 ? taxTotalDetailsArr[0] : null;
        totals.totalDiscount     = taxTotalDetailsArr.length > 1 ? taxTotalDetailsArr[1] : null;
        totals.totalVat          = taxTotalDetailsArr.length > 2 ? taxTotalDetailsArr[2] : null;
        totals.amountIncludesVat = taxTotalDetailsArr.length > 3 ? taxTotalDetailsArr[3] : null;
        data.totals = totals;

        // IBAN / Tax registration details
        IbanDetails iban = new IbanDetails();
        if (taxIbanDetArr.length > 0) {
            iban.taxRegistrationNumber = taxIbanDetArr[0];
            if (taxIbanDetArr.length > 1) {
                iban.ibanNumber = taxIbanDetArr[1];
            }
        }
        data.iban = iban;

        // Line items
        List<InvoiceLineItem> items = new ArrayList<>();
        for (String itemStr : taxDetailsArrList) {
            if (itemStr == null || itemStr.isEmpty()) continue;
            String[] taxDetailsArr = itemStr.split("<sm>");
            InvoiceLineItem item = new InvoiceLineItem();
            // taxDetailsArr[0] = supply date (yyyyMMdd)
            item.supplyDate = taxDetailsArr.length > 0 ? taxDetailsArr[0] : null;

            // InvoiceParser.java (inside the line-items loop)
            String[] descArr = taxDetailsArr[1].split("<sm1>");
            // first part is Arabic, second part (if any) is English
            item.arabicDescription = descArr.length > 0 ? descArr[0] : null;
            item.englishDescription         = descArr.length > 1 ? descArr[1] : null;

            item.unitPrice         = taxDetailsArr.length > 2 ? taxDetailsArr[2] : null;
            item.quantity          = taxDetailsArr.length > 3 ? taxDetailsArr[3] : null;
            item.discount          = taxDetailsArr.length > 4 ? taxDetailsArr[4] : null;
            item.totalExcludingTax = taxDetailsArr.length > 5 ? taxDetailsArr[5] : null;
            item.rate              = taxDetailsArr.length > 6 ? taxDetailsArr[6] : null;
            item.taxAmount         = taxDetailsArr.length > 7 ? taxDetailsArr[7] : null;
            item.totalPrice        = taxDetailsArr.length > 8 ? taxDetailsArr[8] : null;

            items.add(item);
        }
        data.lineItems = items;

        return data;
    }
}