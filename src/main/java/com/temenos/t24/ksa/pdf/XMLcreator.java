package com.temenos.t24.ksa.pdf;

import com.temenos.t24.ksa.pdf.model.InvoiceData;
import com.temenos.t24.ksa.pdf.model.InvoiceLineItem;
import com.temenos.t24.ksa.pdf.model.InvoiceParser;
import com.temenos.t24.ksa.pdf.qr.TLVUtils;
import com.temenos.t24.ksa.pdf.qr.ZatcaQRData;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Minimal UBL 2.1 XML generator aligned with ZATCA structure.
 * Focuses on core elements and embeds the Phase I TLV QR code in AdditionalDocumentReference.
 */
public class XMLcreator {

    // Namespaces
    private static final String NS_INVOICE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
    private static final String NS_CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    private static final String NS_CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String NS_EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";

    public static String createXML(String args) {
        InvoiceData data = InvoiceParser.parse(args);
        return createXML(data);
    }

    public static String createXML(InvoiceData data) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            // Root element with namespace declarations
            Element invoice = doc.createElementNS(NS_INVOICE, "Invoice");
            invoice.setAttribute("xmlns:cac", NS_CAC);
            invoice.setAttribute("xmlns:cbc", NS_CBC);
            invoice.setAttribute("xmlns:ext", NS_EXT);
            doc.appendChild(invoice);

            // UBLExtensions with minimal invoice counter placeholder to satisfy schema and KSA rules
            Element ext = doc.createElementNS(NS_EXT, "ext:UBLExtensions");
            Element ublExt = doc.createElementNS(NS_EXT, "ext:UBLExtension");
            Element extContent = doc.createElementNS(NS_EXT, "ext:ExtensionContent");
            Element icv = doc.createElementNS(NS_CBC, "cbc:ID");
            icv.setAttribute("schemeID", "ICV");
            icv.setTextContent("1");
            extContent.appendChild(icv);
            ublExt.appendChild(extContent);
            ext.appendChild(ublExt);
            invoice.appendChild(ext);

            // Basic header
            addTextElement(doc, invoice, NS_CBC, "cbc:ProfileID", "reporting:1.0");
            if (data.header != null) {
                addTextElement(doc, invoice, NS_CBC, "cbc:ID", nullToEmpty(data.header.invoiceNumber));
            } else {
                addTextElement(doc, invoice, NS_CBC, "cbc:ID", "");
            }
            addTextElement(doc, invoice, NS_CBC, "cbc:UUID", UUID.randomUUID().toString());

            String[] dateTime = splitDateTime(data);
            addTextElement(doc, invoice, NS_CBC, "cbc:IssueDate", dateTime[0]);
            addTextElement(doc, invoice, NS_CBC, "cbc:IssueTime", dateTime[1]);

            Element invType = doc.createElementNS(NS_CBC, "cbc:InvoiceTypeCode");
            invType.setAttribute("name", "0200000"); // standard invoice mask commonly used
            invType.setTextContent("388"); // 388 = Commercial invoice
            invoice.appendChild(invType);

            addTextElement(doc, invoice, NS_CBC, "cbc:DocumentCurrencyCode", "SAR");
            addTextElement(doc, invoice, NS_CBC, "cbc:TaxCurrencyCode", "SAR");

            // AdditionalDocumentReference - QR (Phase I TLV as Base64)
            String qrBase64 = generateZatcaQRBase64(data);
            Element adrQR = doc.createElementNS(NS_CAC, "cac:AdditionalDocumentReference");
            addTextElement(doc, adrQR, NS_CBC, "cbc:ID", "QR");
            Element attach = doc.createElementNS(NS_CAC, "cac:Attachment");
            Element emb = doc.createElementNS(NS_CBC, "cbc:EmbeddedDocumentBinaryObject");
            emb.setAttribute("mimeCode", "text/plain");
            emb.setTextContent(qrBase64);
            attach.appendChild(emb);
            adrQR.appendChild(attach);
            invoice.appendChild(adrQR);

            // Supplier (minimal): VAT number + RegistrationName
            Element supplier = doc.createElementNS(NS_CAC, "cac:AccountingSupplierParty");
            Element supParty = doc.createElementNS(NS_CAC, "cac:Party");
            // PartyTaxScheme
            Element supTax = doc.createElementNS(NS_CAC, "cac:PartyTaxScheme");
            addTextElement(doc, supTax, NS_CBC, "cbc:CompanyID", data.iban != null ? nullToEmpty(data.iban.taxRegistrationNumber) : "");
            Element supTaxScheme = doc.createElementNS(NS_CAC, "cac:TaxScheme");
            addTextElement(doc, supTaxScheme, NS_CBC, "cbc:ID", "VAT");
            supTax.appendChild(supTaxScheme);
            supParty.appendChild(supTax);

            // PartyLegalEntity
            Element supLegal = doc.createElementNS(NS_CAC, "cac:PartyLegalEntity");
            addTextElement(doc, supLegal, NS_CBC, "cbc:RegistrationName", "National Bank of Iraq");
            supParty.appendChild(supLegal);

            supplier.appendChild(supParty);
            invoice.appendChild(supplier);

            // Customer
            Element customer = doc.createElementNS(NS_CAC, "cac:AccountingCustomerParty");
            Element cusParty = doc.createElementNS(NS_CAC, "cac:Party");

            // PostalAddress (combine Arabic | English like example, if available)
            Element postal = doc.createElementNS(NS_CAC, "cac:PostalAddress");
            addTextElement(doc, postal, NS_CBC, "cbc:StreetName",
                    joinBiLang(getOrEmpty(() -> data.customer.arabicAddress), getOrEmpty(() -> data.customer.englishAddress)));
            addTextElement(doc, postal, NS_CBC, "cbc:CityName",
                    joinBiLang(getOrEmpty(() -> data.customer.arabicCity), getOrEmpty(() -> data.customer.englishCity)));
            Element country = doc.createElementNS(NS_CAC, "cac:Country");
            addTextElement(doc, country, NS_CBC, "cbc:IdentificationCode", "SA");
            postal.appendChild(country);
            cusParty.appendChild(postal);

            // PartyTaxScheme (customer VAT)
            Element cusTax = doc.createElementNS(NS_CAC, "cac:PartyTaxScheme");
            addTextElement(doc, cusTax, NS_CBC, "cbc:CompanyID", getOrEmpty(() -> data.customer.vatNumber));
            Element cusTaxScheme = doc.createElementNS(NS_CAC, "cac:TaxScheme");
            addTextElement(doc, cusTaxScheme, NS_CBC, "cbc:ID", "VAT");
            cusTax.appendChild(cusTaxScheme);
            cusParty.appendChild(cusTax);

            // PartyLegalEntity (customer name)
            Element cusLegal = doc.createElementNS(NS_CAC, "cac:PartyLegalEntity");
            addTextElement(doc, cusLegal, NS_CBC, "cbc:RegistrationName",
                    joinBiLang(getOrEmpty(() -> data.customer.arabicName), getOrEmpty(() -> data.customer.englishName)));
            cusParty.appendChild(cusLegal);

            customer.appendChild(cusParty);
            invoice.appendChild(customer);

            // TaxTotal (overall) with VAT breakdown group (BG-23)
            if (data.totals != null) {
                Element taxTotal = doc.createElementNS(NS_CAC, "cac:TaxTotal");
                addAmount(doc, taxTotal, "cbc:TaxAmount", nullToEmpty(data.totals.totalVat), "SAR");

                Element taxSubtotal = doc.createElementNS(NS_CAC, "cac:TaxSubtotal");
                addAmount(doc, taxSubtotal, "cbc:TaxableAmount", nullToEmpty(data.totals.totalExcludingVat), "SAR");
                addAmount(doc, taxSubtotal, "cbc:TaxAmount", nullToEmpty(data.totals.totalVat), "SAR");

                Element taxCategory = doc.createElementNS(NS_CAC, "cac:TaxCategory");
                addTextElement(doc, taxCategory, NS_CBC, "cbc:ID", "S");
                String rate = (data.lineItems != null && !data.lineItems.isEmpty())
                        ? nullToEmpty(data.lineItems.get(0).rate)
                        : "15";
                addTextElement(doc, taxCategory, NS_CBC, "cbc:Percent", rate);
                Element taxScheme = doc.createElementNS(NS_CAC, "cac:TaxScheme");
                addTextElement(doc, taxScheme, NS_CBC, "cbc:ID", "VAT");
                taxCategory.appendChild(taxScheme);

                taxSubtotal.appendChild(taxCategory);
                taxTotal.appendChild(taxSubtotal);
                invoice.appendChild(taxTotal);
            }

            // BR-CO-18
            if (data.totals != null) {
                Element fullTax = doc.createElementNS(NS_CAC, "cac:TaxTotal");
                addAmount(doc, fullTax, "cbc:TaxAmount", nullToEmpty(data.totals.totalVat), "SAR");

                Element subtotal  = doc.createElementNS("", "cac:Subtotal");

                addAmount(doc, subtotal, "cbc:TaxableAmount", nullToEmpty(data.totals.totalExcludingVat), "SAR");
                addAmount(doc, subtotal, "cbc:TaxAmount", nullToEmpty(data.totals.totalVat), "SAR");

                Element taxCategory  = doc.createElement( "cac:TaxCategory");

                Element taxCategoryId =  doc.createElementNS( NS_CBC,"cbc:ID");
                taxCategoryId.setAttribute("schemeID", "UN/ECE 5305");
                taxCategoryId.setAttribute("schemeAgencyID","6");
                taxCategoryId.setTextContent("S");

                Element taxCategoryPercent =  doc.createElementNS( NS_CBC,"cbc:Percent");
                taxCategoryPercent.setTextContent("15.00");

                Element taxScheme =   doc.createElement( "cac:TaxScheme");
                Element taxSchemeId =  doc.createElementNS( NS_CBC,"cbc:ID");
                taxSchemeId.setAttribute("schemeID", "UN/ECE 5153");
                taxSchemeId.setAttribute("schemeAgencyID","6");
                taxSchemeId.setTextContent("VAT");

                taxScheme.appendChild(taxSchemeId);

                taxCategory.appendChild(taxCategoryId);
                taxCategory.appendChild(taxCategoryPercent);
                taxCategory.appendChild(taxScheme);

                subtotal.appendChild(taxCategory);

                fullTax.appendChild(subtotal);


                addAmount(doc, subtotal, "cbc:TaxAmount", nullToEmpty(data.totals.totalVat), "SAR");

                invoice.appendChild(fullTax);

            }

            // LegalMonetaryTotal
            if (data.totals != null) {
                Element lmt = doc.createElementNS(NS_CAC, "cac:LegalMonetaryTotal");
                addAmount(doc, lmt, "cbc:LineExtensionAmount", nullToEmpty(data.totals.totalExcludingVat), "SAR");
                addAmount(doc, lmt, "cbc:TaxExclusiveAmount", nullToEmpty(data.totals.totalExcludingVat), "SAR");
                addAmount(doc, lmt, "cbc:TaxInclusiveAmount", nullToEmpty(data.totals.amountIncludesVat), "SAR");
                addAmount(doc, lmt, "cbc:AllowanceTotalAmount", nullToEmpty(data.totals.totalDiscount), "SAR");
                addAmount(doc, lmt, "cbc:PrepaidAmount", "0.00", "SAR");
                addAmount(doc, lmt, "cbc:PayableAmount", nullToEmpty(data.totals.amountIncludesVat), "SAR");
                invoice.appendChild(lmt);
            }

            // InvoiceLines
            List<InvoiceLineItem> items = data.lineItems;
            if (items != null) {
                for (int i = 0; i < items.size(); i++) {
                    InvoiceLineItem it = items.get(i);
                    Element line = doc.createElementNS(NS_CAC, "cac:InvoiceLine");
                    addTextElement(doc, line, NS_CBC, "cbc:ID", String.valueOf(i + 1));
                    addQty(doc, line, "cbc:InvoicedQuantity", nullToEmpty(it.quantity), "PCE");
                    addAmount(doc, line, "cbc:LineExtensionAmount", nullToEmpty(it.totalExcludingTax), "SAR");

                    Element lineTaxTotal = doc.createElementNS(NS_CAC, "cac:TaxTotal");
                    addAmount(doc, lineTaxTotal, "cbc:TaxAmount", nullToEmpty(it.taxAmount), "SAR");
                    addAmount(doc, lineTaxTotal, "cbc:RoundingAmount", nullToEmpty(it.totalPrice), "SAR");
                    line.appendChild(lineTaxTotal);

                    Element item = doc.createElementNS(NS_CAC, "cac:Item");
                    // prefer Arabic like the example, fallback to English
                    String name = nullToEmpty(it.arabicDescription);
                    if (name.isEmpty()) name = nullToEmpty(it.englishDescription);
                    addTextElement(doc, item, NS_CBC, "cbc:Name", name);

                    Element classTax = doc.createElementNS(NS_CAC, "cac:ClassifiedTaxCategory");
                    addTextElement(doc, classTax, NS_CBC, "cbc:ID", "S");
                    addTextElement(doc, classTax, NS_CBC, "cbc:Percent", nullToEmpty(it.rate));
                    Element taxScheme = doc.createElementNS(NS_CAC, "cac:TaxScheme");
                    addTextElement(doc, taxScheme, NS_CBC, "cbc:ID", "VAT");
                    classTax.appendChild(taxScheme);
                    item.appendChild(classTax);
                    line.appendChild(item);

                    Element price = doc.createElementNS(NS_CAC, "cac:Price");
                    addAmount(doc, price, "cbc:PriceAmount", nullToEmpty(it.unitPrice), "SAR");
                    line.appendChild(price);

                    invoice.appendChild(line);
                }
            }

            // Persist file
            String xmlPath = computeOutputPath(data.path);
            ensureParentDirs(xmlPath);
            writeXml(doc, xmlPath);
            return xmlPath;

        } catch (ParserConfigurationException | TransformerException e) {
            throw new RuntimeException("Failed to build XML: " + e.getMessage(), e);
        }
    }

    private static String computeOutputPath(String originalPath) {
        if (originalPath == null || originalPath.isEmpty()) {
            return "test-output/invoice.xml";
        }
        if (originalPath.toLowerCase().endsWith(".pdf")) {
            return originalPath.substring(0, originalPath.length() - 4) + "xml";
        }
        int slash = Math.max(originalPath.lastIndexOf('/'), originalPath.lastIndexOf('\\'));
        String dir = slash >= 0 ? originalPath.substring(0, slash + 1) : "";
        return dir + "invoice.xml";
    }

    private static void ensureParentDirs(String path) {
        File f = new File(path).getParentFile();
        if (f != null && !f.exists()) {
            //noinspection ResultOfMethodCallIgnored
            f.mkdirs();
        }
    }

    private static void writeXml(Document doc, String path) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(doc), new StreamResult(new File(path)));
    }

    private static void addTextElement(Document doc, Element parent, String ns, String qname, String value) {
        Element e = doc.createElementNS(ns, qname);
        e.setTextContent(value == null ? "" : value);
        parent.appendChild(e);
    }

    private static void addAmount(Document doc, Element parent, String qname, String value, String currency) {
        Element e = doc.createElementNS(NS_CBC, qname);
        e.setAttribute("currencyID", currency);
        e.setTextContent(value == null ? "0.00" : value);
        parent.appendChild(e);
    }

    private static void addQty(Document doc, Element parent, String qname, String value, String unitCode) {
        Element e = doc.createElementNS(NS_CBC, qname);
        e.setAttribute("unitCode", unitCode);
        e.setTextContent(value == null ? "0" : value);
        parent.appendChild(e);
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    private static String joinBiLang(String ar, String en) {
        if ((ar == null || ar.isEmpty()) && (en == null || en.isEmpty())) return "";
        if (ar == null || ar.isEmpty()) return en;
        if (en == null || en.isEmpty()) return ar;
        return ar + " | " + en;
    }

    private interface SupplierEx<T> { T get(); }
    private static String getOrEmpty(SupplierEx<String> s) {
        try { return nullToEmpty(s.get()); } catch (Exception e) { return ""; }
    }

    private static String[] splitDateTime(InvoiceData data) {
        String iso = convertDateTimeToIso(data != null && data.header != null ? data.header.invoiceDateTime : null);
        String issueDate = "";
        String issueTime = "";
        int t = iso.indexOf('T');
        if (t > 0) {
            issueDate = iso.substring(0, t);
            issueTime = iso.substring(t + 1);
        }
        return new String[]{issueDate, issueTime};
    }

    // Same logic as PDFcreator.convertDateTimeToIso (duplicated to avoid cross-dependency)
    private static String convertDateTimeToIso(String invoiceDateTime) {
        TimeZone riyadh = TimeZone.getTimeZone("Asia/Riyadh");
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        inputFormat.setTimeZone(riyadh);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        outputFormat.setTimeZone(riyadh);
        Date date = null;
        if (invoiceDateTime != null) {
            try {
                date = inputFormat.parse(invoiceDateTime);
            } catch (ParseException ignored) { }
        }
        if (date == null) {
            date = new Date();
        }
        long now = System.currentTimeMillis();
        if (Math.abs(now - date.getTime()) > 24L * 60 * 60 * 1000) {
            date = new Date(now);
        }
        return outputFormat.format(date);
    }

    private static String generateZatcaQRBase64(InvoiceData data) {
        try {
            ZatcaQRData qr = new ZatcaQRData();
            qr.sellerName = "National Bank of Iraq";
            qr.vatNumber = (data != null && data.iban != null) ? data.iban.taxRegistrationNumber : "";
            qr.timestamp = data != null && data.header != null ? convertDateTimeToIso(data.header.invoiceDateTime) : "";
            qr.invoiceTotalWithVat = data != null && data.totals != null ? data.totals.amountIncludesVat : "";
            qr.vatTotal = data != null && data.totals != null ? data.totals.totalVat : "";
            return TLVUtils.generateBase64TLV(qr);
        } catch (Exception ex) {
            // In case of invalid input, return empty to keep XML creation resilient
            return "";
        }
    }
}

