package com.example.ubl.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Container for invoice data. */
public class InvoiceData {
    private String invoiceId;
    private LocalDate issueDate;
    private LocalTime issueTime;
    private String currency = "SAR";
    private Seller seller;
    private Buyer buyer;
    private List<InvoiceLine> lines = new ArrayList<>();
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String paymentMeansCode;
    private QrFields qrFields; // optional

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalTime getIssueTime() { return issueTime; }
    public void setIssueTime(LocalTime issueTime) { this.issueTime = issueTime; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }
    public Buyer getBuyer() { return buyer; }
    public void setBuyer(Buyer buyer) { this.buyer = buyer; }
    public List<InvoiceLine> getLines() { return lines; }
    public void setLines(List<InvoiceLine> lines) { this.lines = lines; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public String getPaymentMeansCode() { return paymentMeansCode; }
    public void setPaymentMeansCode(String paymentMeansCode) { this.paymentMeansCode = paymentMeansCode; }
    public QrFields getQrFields() { return qrFields; }
    public void setQrFields(QrFields qrFields) { this.qrFields = qrFields; }

    /** Optional QR fields container. */
    public static class QrFields {
        public String sellerName;
        public String sellerVat;
        public String isoTimestamp;
        public String totalInclVat;
        public String vatTotal;

        public QrFields() {}
        public QrFields(String sellerName, String sellerVat, String isoTimestamp,
                        String totalInclVat, String vatTotal) {
            this.sellerName = sellerName;
            this.sellerVat = sellerVat;
            this.isoTimestamp = isoTimestamp;
            this.totalInclVat = totalInclVat;
            this.vatTotal = vatTotal;
        }
    }
}
