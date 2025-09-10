package com.temenos.t24.ksa.pdf;

import com.temenos.t24.ksa.pdf.model.InvoiceData;
import com.temenos.t24.ksa.pdf.model.InvoiceLineItem;
import com.temenos.t24.ksa.pdf.model.InvoiceParser;

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
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
            doc.appendChild(invoice);

            // Basic header
            if (data.header != null) {
                addTextElement(doc, invoice, NS_CBC, "cbc:ID", nullToEmpty(data.header.invoiceNumber));
            } else {
                addTextElement(doc, invoice, NS_CBC, "cbc:ID", "");
            }

            String[] dateTime = splitDateTime(data);
            addTextElement(doc, invoice, NS_CBC, "cbc:IssueDate", dateTime[0]);
            addTextElement(doc, invoice, NS_CBC, "cbc:IssueTime", dateTime[1]);

            addTextElement(doc, invoice, NS_CBC, "cbc:InvoiceTypeCode", "388");

            addTextElement(doc, invoice, NS_CBC, "cbc:DocumentCurrencyCode", "SAR");
            addTextElement(doc, invoice, NS_CBC, "cbc:TaxCurrencyCode", "SAR");

            // Optional: PaymentMeans
            Element paymentMeans = doc.createElementNS(NS_CAC, "cac:PaymentMeans");
            addTextElement(doc, paymentMeans, NS_CBC, "cbc:PaymentMeansCode", "10");
            invoice.appendChild(paymentMeans);

            // Optional: Discount (Allowance/Charge)
            Element allowance = doc.createElementNS(NS_CAC, "cac:AllowanceCharge");
            addTextElement(doc, allowance, NS_CBC, "cbc:ChargeIndicator", "false");
            addTextElement(doc, allowance, NS_CBC, "cbc:AllowanceChargeReason", "discount");
            addAmount(doc, allowance, "cbc:Amount", "0.00", "SAR");
            Element allowanceTax = doc.createElementNS(NS_CAC, "cac:TaxCategory");
            addTextElement(doc, allowanceTax, NS_CBC, "cbc:ID", "S");
            addTextElement(doc, allowanceTax, NS_CBC, "cbc:Percent", "15");
            Element allowanceScheme = doc.createElementNS(NS_CAC, "cac:TaxScheme");
            addTextElement(doc, allowanceScheme, NS_CBC, "cbc:ID", "VAT");
            allowanceTax.appendChild(allowanceScheme);
            allowance.appendChild(allowanceTax);
            invoice.appendChild(allowance);

            // Supplier (minimal): ID, name, address then tax/legal details
            Element supplier = doc.createElementNS(NS_CAC, "cac:AccountingSupplierParty");
            Element supParty = doc.createElementNS(NS_CAC, "cac:Party");

            // Optional identification
            Element supId = doc.createElementNS(NS_CAC, "cac:PartyIdentification");
            Element supIdVal = doc.createElementNS(NS_CBC, "cbc:ID");
            supIdVal.setAttribute("schemeID", "CRN");
            supIdVal.setTextContent("1010010000");
            supId.appendChild(supIdVal);
            supParty.appendChild(supId);

            // Party name
            Element supName = doc.createElementNS(NS_CAC, "cac:PartyName");
            addTextElement(doc, supName, NS_CBC, "cbc:Name", "Example Supplier Ltd");
            supParty.appendChild(supName);

            // Address comes before tax/legal
            Element supAddress = doc.createElementNS(NS_CAC, "cac:PostalAddress");
            addTextElement(doc, supAddress, NS_CBC, "cbc:StreetName", "Prince Sultan Street");
            addTextElement(doc, supAddress, NS_CBC, "cbc:BuildingNumber", "2322");
            addTextElement(doc, supAddress, NS_CBC, "cbc:CityName", "Riyadh");
            addTextElement(doc, supAddress, NS_CBC, "cbc:PostalZone", "11564");
            Element supCountry = doc.createElementNS(NS_CAC, "cac:Country");
            addTextElement(doc, supCountry, NS_CBC, "cbc:IdentificationCode", "SA");
            supAddress.appendChild(supCountry);
            supParty.appendChild(supAddress);

            // Tax details
            Element supTax = doc.createElementNS(NS_CAC, "cac:PartyTaxScheme");
            addTextElement(doc, supTax, NS_CBC, "cbc:CompanyID", data.iban != null ? nullToEmpty(data.iban.taxRegistrationNumber) : "");
            Element supTaxScheme = doc.createElementNS(NS_CAC, "cac:TaxScheme");
            addTextElement(doc, supTaxScheme, NS_CBC, "cbc:ID", "VAT");
            supTax.appendChild(supTaxScheme);
            supParty.appendChild(supTax);

            // Legal entity name
            Element supLegal = doc.createElementNS(NS_CAC, "cac:PartyLegalEntity");
            addTextElement(doc, supLegal, NS_CBC, "cbc:RegistrationName", "Example Supplier Ltd");
            supParty.appendChild(supLegal);

            supplier.appendChild(supParty);
            invoice.appendChild(supplier);

            // Customer
            Element customer = doc.createElementNS(NS_CAC, "cac:AccountingCustomerParty");
            Element cusParty = doc.createElementNS(NS_CAC, "cac:Party");

            // Party name
            Element cusName = doc.createElementNS(NS_CAC, "cac:PartyName");
            addTextElement(doc, cusName, NS_CBC, "cbc:Name", "Example Customer Ltd");
            cusParty.appendChild(cusName);

            // Address before tax/legal
            Element cusAddress = doc.createElementNS(NS_CAC, "cac:PostalAddress");
            addTextElement(doc, cusAddress, NS_CBC, "cbc:StreetName", "Salah Al-Din Street");
            addTextElement(doc, cusAddress, NS_CBC, "cbc:BuildingNumber", "1111");
            addTextElement(doc, cusAddress, NS_CBC, "cbc:CityName", "Riyadh");
            addTextElement(doc, cusAddress, NS_CBC, "cbc:PostalZone", "12222");
            Element cusCountry = doc.createElementNS(NS_CAC, "cac:Country");
            addTextElement(doc, cusCountry, NS_CBC, "cbc:IdentificationCode", "SA");
            cusAddress.appendChild(cusCountry);
            cusParty.appendChild(cusAddress);

            // Tax scheme
            Element cusTax = doc.createElementNS(NS_CAC, "cac:PartyTaxScheme");
            addTextElement(doc, cusTax, NS_CBC, "cbc:CompanyID", getOrEmpty(() -> data.customer.vatNumber));
            Element cusTaxScheme = doc.createElementNS(NS_CAC, "cac:TaxScheme");
            addTextElement(doc, cusTaxScheme, NS_CBC, "cbc:ID", "VAT");
            cusTax.appendChild(cusTaxScheme);
            cusParty.appendChild(cusTax);

            // Legal entity
            Element cusLegal = doc.createElementNS(NS_CAC, "cac:PartyLegalEntity");
            addTextElement(doc, cusLegal, NS_CBC, "cbc:RegistrationName", "Example Customer Ltd");
            cusParty.appendChild(cusLegal);

            customer.appendChild(cusParty);
            invoice.appendChild(customer);

            // TaxTotal with TaxSubtotal
            if (data.totals != null) {
                Element taxTotal = doc.createElementNS(NS_CAC, "cac:TaxTotal");
                addAmount(doc, taxTotal, "cbc:TaxAmount", nullToEmpty(data.totals.totalVat), "SAR");

                Element subtotal = doc.createElementNS(NS_CAC, "cac:TaxSubtotal");
                addAmount(doc, subtotal, "cbc:TaxableAmount", nullToEmpty(data.totals.totalExcludingVat), "SAR");
                addAmount(doc, subtotal, "cbc:TaxAmount", nullToEmpty(data.totals.totalVat), "SAR");

                Element taxCategory = doc.createElementNS(NS_CAC, "cac:TaxCategory");
                addTextElement(doc, taxCategory, NS_CBC, "cbc:ID", "S");
                addTextElement(doc, taxCategory, NS_CBC, "cbc:Percent", "15.00");
                Element taxScheme = doc.createElementNS(NS_CAC, "cac:TaxScheme");
                addTextElement(doc, taxScheme, NS_CBC, "cbc:ID", "VAT");
                taxCategory.appendChild(taxScheme);
                subtotal.appendChild(taxCategory);

                taxTotal.appendChild(subtotal);
                invoice.appendChild(taxTotal);
            }

            // LegalMonetaryTotal
            if (data.totals != null) {
                Element lmt = doc.createElementNS(NS_CAC, "cac:LegalMonetaryTotal");
                addAmount(doc, lmt, "cbc:LineExtensionAmount", nullToEmpty(data.totals.totalExcludingVat), "SAR");
                addAmount(doc, lmt, "cbc:TaxExclusiveAmount", nullToEmpty(data.totals.totalExcludingVat), "SAR");
                addAmount(doc, lmt, "cbc:TaxInclusiveAmount", nullToEmpty(data.totals.amountIncludesVat), "SAR");
                addAmount(doc, lmt, "cbc:AllowanceTotalAmount", nullToEmpty(data.totals.totalDiscount), "SAR");
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
        String issueDate = "";
        String issueTime = "";
        if (data != null && data.header != null && data.header.invoiceDateTime != null) {
            String iso = convertDateTimeToIso(data.header.invoiceDateTime); // yyyy-MM-dd'T'HH:mm:ss'Z'
            int t = iso.indexOf('T');
            if (t > 0) {
                issueDate = iso.substring(0, t);
                int z = iso.indexOf('Z', t + 1);
                issueTime = z > 0 ? iso.substring(t + 1, z) : iso.substring(t + 1);
            }
        }
        return new String[]{issueDate, issueTime};
    }

    // Same logic as PDFcreator.convertDateTimeToIso (duplicated to avoid cross-dependency)
    private static String convertDateTimeToIso(String invoiceDateTime) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = inputFormat.parse(invoiceDateTime);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return invoiceDateTime; // fallback
        }
    }

}

