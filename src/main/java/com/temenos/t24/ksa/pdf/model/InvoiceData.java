package com.temenos.t24.ksa.pdf.model;

import java.util.List;

public class InvoiceData {
    public String path;
    public String arabicFontPath;
    public String logoPath;
    public String footerPath;
    public InvoiceHeader header;
    public CustomerDetails customer;
    public List<InvoiceLineItem> lineItems;
    public InvoiceTotals totals;
    public IbanDetails iban;
}