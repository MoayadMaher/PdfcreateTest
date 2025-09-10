package com.example.ubl.builder;

import com.example.ubl.model.*;
import com.example.ubl.qr.QrTlvEncoder;
import com.example.ubl.util.InvoiceCalculator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Build a minimal UBL 2.1 invoice for ZATCA phase I. */
public class UblInvoiceBuilder {
    private static final String NS_INVOICE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
    private static final String NS_CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String NS_CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";

    public Document build(InvoiceData data) throws Exception {
        validate(data);
        InvoiceTotals totals = InvoiceCalculator.calculate(data);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element invoice = doc.createElementNS(NS_INVOICE, "Invoice");
        invoice.setAttribute("xmlns", NS_INVOICE);
        invoice.setAttribute("xmlns:cac", NS_CAC);
        invoice.setAttribute("xmlns:cbc", NS_CBC);
        doc.appendChild(invoice);

        appendText(invoice, NS_CBC, "cbc:ID", data.getInvoiceId());
        appendText(invoice, NS_CBC, "cbc:IssueDate", data.getIssueDate().toString());
        appendText(invoice, NS_CBC, "cbc:IssueTime", data.getIssueTime().toString());
        appendText(invoice, NS_CBC, "cbc:InvoiceTypeCode", "388");
        appendText(invoice, NS_CBC, "cbc:DocumentCurrencyCode", data.getCurrency());
        appendText(invoice, NS_CBC, "cbc:TaxCurrencyCode", data.getCurrency());

        buildSupplier(doc, invoice, data.getSeller());
        buildCustomer(doc, invoice, data.getBuyer());

        if (data.getDiscountAmount() != null && data.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            Element allowance = append(invoice, NS_CAC, "cac:AllowanceCharge");
            appendText(allowance, NS_CBC, "cbc:ChargeIndicator", "false");
            appendText(allowance, NS_CBC, "cbc:AllowanceChargeReason", "discount");
            Element amt = appendAmount(allowance, "cbc:Amount", data.getDiscountAmount());
            amt.setAttribute("currencyID", data.getCurrency());
            Element tc = append(allowance, NS_CAC, "cac:TaxCategory");
            appendText(tc, NS_CBC, "cbc:ID", "S");
            appendText(tc, NS_CBC, "cbc:Percent", "15");
            Element ts = append(tc, NS_CAC, "cac:TaxScheme");
            appendText(ts, NS_CBC, "cbc:ID", "VAT");
        }

        // Lines
        for (InvoiceLine line : data.getLines()) {
            BigDecimal lineNet = line.getUnitPriceNet().multiply(line.getQuantity())
                    .setScale(2, RoundingMode.HALF_UP);
            Element lineEl = append(invoice, NS_CAC, "cac:InvoiceLine");
            appendText(lineEl, NS_CBC, "cbc:ID", line.getId());
            Element qty = appendText(lineEl, NS_CBC, "cbc:InvoicedQuantity", line.getQuantity().toPlainString());
            qty.setAttribute("unitCode", line.getUnitCode());
            Element le = appendAmount(lineEl, "cbc:LineExtensionAmount", lineNet);
            le.setAttribute("currencyID", data.getCurrency());
            Element item = append(lineEl, NS_CAC, "cac:Item");
            appendText(item, NS_CBC, "cbc:Name", line.getDescription());
            Element cat = append(item, NS_CAC, "cac:ClassifiedTaxCategory");
            appendText(cat, NS_CBC, "cbc:ID", "S");
            appendText(cat, NS_CBC, "cbc:Percent", line.getVatPercent().setScale(2, RoundingMode.HALF_UP).toPlainString());
            Element scheme = append(cat, NS_CAC, "cac:TaxScheme");
            appendText(scheme, NS_CBC, "cbc:ID", "VAT");
            Element price = append(lineEl, NS_CAC, "cac:Price");
            Element pa = appendAmount(price, "cbc:PriceAmount", line.getUnitPriceNet());
            pa.setAttribute("currencyID", data.getCurrency());
        }

        // Tax total
        Element taxTotal = append(invoice, NS_CAC, "cac:TaxTotal");
        Element ttAmt = appendAmount(taxTotal, "cbc:TaxAmount", totals.getTaxTotal());
        ttAmt.setAttribute("currencyID", data.getCurrency());
        Element sub = append(taxTotal, NS_CAC, "cac:TaxSubtotal");
        Element taxable = appendAmount(sub, "cbc:TaxableAmount", totals.getTaxExclusive());
        taxable.setAttribute("currencyID", data.getCurrency());
        Element ta = appendAmount(sub, "cbc:TaxAmount", totals.getTaxTotal());
        ta.setAttribute("currencyID", data.getCurrency());
        Element cat = append(sub, NS_CAC, "cac:TaxCategory");
        appendText(cat, NS_CBC, "cbc:ID", "S");
        appendText(cat, NS_CBC, "cbc:Percent", "15");
        Element scheme = append(cat, NS_CAC, "cac:TaxScheme");
        appendText(scheme, NS_CBC, "cbc:ID", "VAT");

        // LegalMonetaryTotal
        Element lmt = append(invoice, NS_CAC, "cac:LegalMonetaryTotal");
        Element lineExt = appendAmount(lmt, "cbc:LineExtensionAmount", totals.getLineExtensionTotal());
        lineExt.setAttribute("currencyID", data.getCurrency());
        Element taxExcl = appendAmount(lmt, "cbc:TaxExclusiveAmount", totals.getTaxExclusive());
        taxExcl.setAttribute("currencyID", data.getCurrency());
        Element taxIncl = appendAmount(lmt, "cbc:TaxInclusiveAmount", totals.getTaxInclusive());
        taxIncl.setAttribute("currencyID", data.getCurrency());
        Element allowanceTotal = appendAmount(lmt, "cbc:AllowanceTotalAmount", totals.getDiscount());
        allowanceTotal.setAttribute("currencyID", data.getCurrency());
        Element payable = appendAmount(lmt, "cbc:PayableAmount", totals.getTaxInclusive());
        payable.setAttribute("currencyID", data.getCurrency());

        if (data.getPaymentMeansCode() != null) {
            Element pm = append(invoice, NS_CAC, "cac:PaymentMeans");
            appendText(pm, NS_CBC, "cbc:PaymentMeansCode", data.getPaymentMeansCode());
        }

        if (data.getQrFields() != null) {
            String qr = QrTlvEncoder.encodeBase64(data.getQrFields().sellerName,
                    data.getQrFields().sellerVat,
                    data.getQrFields().isoTimestamp,
                    data.getQrFields().totalInclVat,
                    data.getQrFields().vatTotal);
            Element docRef = append(invoice, NS_CAC, "cac:AdditionalDocumentReference");
            appendText(docRef, NS_CBC, "cbc:ID", "QR");
            Element attachment = append(docRef, NS_CAC, "cac:Attachment");
            Element bin = appendText(attachment, NS_CBC, "cbc:EmbeddedDocumentBinaryObject", qr);
            bin.setAttribute("mimeCode", "text/plain");
        }

        return doc;
    }

    private void buildSupplier(Document doc, Element parent, Seller seller) {
        Element asp = append(parent, NS_CAC, "cac:AccountingSupplierParty");
        Element party = append(asp, NS_CAC, "cac:Party");
        if (seller.getCrn() != null) {
            Element pid = append(party, NS_CAC, "cac:PartyIdentification");
            Element id = appendText(pid, NS_CBC, "cbc:ID", seller.getCrn());
            id.setAttribute("schemeID", "CRN");
        }
        Element pts = append(party, NS_CAC, "cac:PartyTaxScheme");
        appendText(pts, NS_CBC, "cbc:CompanyID", seller.getVatNumber());
        Element ple = append(party, NS_CAC, "cac:PartyLegalEntity");
        appendText(ple, NS_CBC, "cbc:RegistrationName", seller.getName());
        Element addr = append(party, NS_CAC, "cac:PostalAddress");
        appendText(addr, NS_CBC, "cbc:StreetName", seller.getStreet());
        appendText(addr, NS_CBC, "cbc:CityName", seller.getCity());
        if (seller.getRegion() != null) appendText(addr, NS_CBC, "cbc:CountrySubentity", seller.getRegion());
        if (seller.getPostalCode() != null) appendText(addr, NS_CBC, "cbc:PostalZone", seller.getPostalCode());
        Element country = append(addr, NS_CAC, "cac:Country");
        appendText(country, NS_CBC, "cbc:IdentificationCode", seller.getCountryCode());
    }

    private void buildCustomer(Document doc, Element parent, Buyer buyer) {
        Element acp = append(parent, NS_CAC, "cac:AccountingCustomerParty");
        Element party = append(acp, NS_CAC, "cac:Party");
        if (buyer.getName() != null) {
            Element ple = append(party, NS_CAC, "cac:PartyLegalEntity");
            appendText(ple, NS_CBC, "cbc:RegistrationName", buyer.getName());
        }
        if (buyer.getVatNumber() != null) {
            Element pts = append(party, NS_CAC, "cac:PartyTaxScheme");
            appendText(pts, NS_CBC, "cbc:CompanyID", buyer.getVatNumber());
        }
        Element addr = append(party, NS_CAC, "cac:PostalAddress");
        appendText(addr, NS_CBC, "cbc:StreetName", buyer.getStreet());
        appendText(addr, NS_CBC, "cbc:CityName", buyer.getCity());
        if (buyer.getRegion() != null) appendText(addr, NS_CBC, "cbc:CountrySubentity", buyer.getRegion());
        if (buyer.getPostalCode() != null) appendText(addr, NS_CBC, "cbc:PostalZone", buyer.getPostalCode());
        Element country = append(addr, NS_CAC, "cac:Country");
        appendText(country, NS_CBC, "cbc:IdentificationCode", buyer.getCountryCode());
    }

    private void validate(InvoiceData data) {
        Objects.requireNonNull(data.getInvoiceId(), "invoiceId");
        Objects.requireNonNull(data.getIssueDate(), "issueDate");
        Objects.requireNonNull(data.getIssueTime(), "issueTime");
        Seller s = Objects.requireNonNull(data.getSeller(), "seller");
        Objects.requireNonNull(s.getName(), "seller.name");
        Objects.requireNonNull(s.getVatNumber(), "seller.vatNumber");
        Objects.requireNonNull(s.getStreet(), "seller.street");
        Objects.requireNonNull(s.getCity(), "seller.city");
        Buyer b = Objects.requireNonNull(data.getBuyer(), "buyer");
        Objects.requireNonNull(b.getStreet(), "buyer.street");
        Objects.requireNonNull(b.getCity(), "buyer.city");
        if (data.getLines() == null || data.getLines().isEmpty()) {
            throw new IllegalArgumentException("invoice lines required");
        }
        for (InvoiceLine l : data.getLines()) {
            if (l.getQuantity() == null || l.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("line quantity must be positive");
            }
            if (l.getUnitPriceNet() == null || l.getUnitPriceNet().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("line unitPriceNet must be positive");
            }
        }
    }

    private Element append(Element parent, String ns, String name) {
        Document doc = parent.getOwnerDocument();
        Element el = doc.createElementNS(ns, name);
        parent.appendChild(el);
        return el;
    }

    private Element appendText(Element parent, String ns, String name, String text) {
        Element el = append(parent, ns, name);
        el.appendChild(parent.getOwnerDocument().createTextNode(text));
        return el;
    }

    private Element appendAmount(Element parent, String qname, BigDecimal amount) {
        return appendText(parent, NS_CBC, qname, amount.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }
}
