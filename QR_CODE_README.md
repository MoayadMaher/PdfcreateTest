# QR Code Integration in PDF Creator

## Overview
This document explains the QR code functionality that has been added to the PDF Creator application. The QR code is automatically generated and placed in the top-left corner of the generated PDF invoice.

## Features

### QR Code Content
The QR code contains the following invoice information:
- Invoice Number
- Invoice Date
- Total Amount (including VAT)
- VAT Amount
- Customer VAT Number

### Technical Implementation

#### Dependencies Added
The following dependencies were added to `pom.xml`:
```xml
<!-- QR Code generation -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
</dependency>
```

#### Key Methods Added

1. **`generateQRCode(String text, int width, int height)`**
   - Generates a QR code image from text
   - Uses ZXing library for QR code generation
   - Supports UTF-8 encoding
   - Uses high error correction level (H)
   - Returns byte array of PNG image data

2. **`addQRCodeToDocument(Document document, String text, int pageNumber, float x, float y, float width, float height)`**
   - Utility method to add QR code to PDF document
   - Handles positioning and scaling
   - Returns boolean indicating success/failure

#### QR Code Configuration
- **Size**: 120x120 pixels
- **Position**: Top-left corner (x: 36, y: 750)
- **Error Correction**: High level (H) for better reliability
- **Character Set**: UTF-8 for international support
- **Margin**: 2 pixels for clean appearance

## Usage

The QR code is automatically generated and added to every PDF invoice. No additional configuration is required.

### Example QR Code Content
```
Invoice: INV-2024-001
Date: 20241201
Total: 157.50
VAT: 7.50
Customer: 123456789
```

## Benefits

1. **Quick Access**: Users can scan the QR code to quickly access invoice details
2. **Digital Integration**: Enables easy integration with mobile apps and digital systems
3. **Error Resilience**: High error correction ensures QR codes remain scannable even if partially damaged
4. **International Support**: UTF-8 encoding supports Arabic and other languages

## Testing

To test the QR code functionality:
1. Run the application: `mvn exec:java -Dexec.mainClass="com.temenos.t24.Main"`
2. Check the generated PDF in `test-output/tax_invoice_test.pdf`
3. Use any QR code scanner app to verify the content

## Troubleshooting

If QR codes are not appearing:
1. Check console output for "QR code added successfully to PDF" message
2. Verify that ZXing dependencies are properly downloaded
3. Ensure the PDF generation process completes without errors

## Future Enhancements

Potential improvements for the QR code feature:
1. Configurable QR code content
2. Multiple QR code positions
3. Custom QR code styling
4. QR code with company logo
5. Different QR code formats (URL, contact info, etc.)
