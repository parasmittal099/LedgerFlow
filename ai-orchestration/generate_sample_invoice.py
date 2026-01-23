"""
Generate Sample Invoice PDF
Creates a test invoice PDF for testing the extraction endpoint
"""

from reportlab.lib.pagesizes import letter
from reportlab.lib.units import inch
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from datetime import datetime, timedelta
import os

def generate_sample_invoice(output_path="sample_invoice.pdf"):
    """Generate a sample invoice PDF"""
    
    # Create canvas
    c = canvas.Canvas(output_path, pagesize=letter)
    width, height = letter
    
    # Invoice data
    invoice_number = "INV-2024-001"
    invoice_date = datetime.now().strftime("%B %d, %Y")
    due_date = (datetime.now() + timedelta(days=30)).strftime("%B %d, %Y")
    
    vendor_name = "Acme Corporation"
    vendor_address = "123 Business Street\nSan Francisco, CA 94105\nUnited States\nPhone: (555) 123-4567"
    
    bill_to = "Tech Solutions Inc.\n456 Client Avenue\nNew York, NY 10001\nUnited States"
    
    # Line items
    line_items = [
        {"description": "Software License - Enterprise Plan", "quantity": 10, "unit_price": 150.00},
        {"description": "Technical Support - Annual", "quantity": 1, "unit_price": 500.00},
        {"description": "Custom Integration Services", "quantity": 20, "unit_price": 75.00},
    ]
    
    # Calculate totals
    subtotal = sum(item["quantity"] * item["unit_price"] for item in line_items)
    tax_rate = 0.08  # 8% tax
    tax_amount = subtotal * tax_rate
    total_amount = subtotal + tax_amount
    
    # Header Section
    c.setFont("Helvetica-Bold", 28)
    c.setFillColor(colors.HexColor("#1a1a1a"))
    c.drawString(50, height - 60, "INVOICE")
    
    # Invoice details (top right)
    c.setFont("Helvetica", 10)
    c.drawRightString(width - 50, height - 60, f"Invoice #: {invoice_number}")
    c.drawRightString(width - 50, height - 75, f"Date: {invoice_date}")
    c.drawRightString(width - 50, height - 90, f"Due Date: {due_date}")
    
    # Vendor information (left side)
    c.setFont("Helvetica-Bold", 12)
    c.drawString(50, height - 150, "From:")
    c.setFont("Helvetica", 10)
    y_pos = height - 170
    for line in vendor_address.split("\n"):
        c.drawString(50, y_pos, line)
        y_pos -= 15
    
    # Bill To (right side)
    c.setFont("Helvetica-Bold", 12)
    c.drawString(350, height - 150, "Bill To:")
    c.setFont("Helvetica", 10)
    y_pos = height - 170
    for line in bill_to.split("\n"):
        c.drawString(350, y_pos, line)
        y_pos -= 15
    
    # Line items table
    y_table_start = height - 280
    
    # Table header
    c.setFont("Helvetica-Bold", 10)
    c.setFillColor(colors.HexColor("#2c3e50"))
    c.rect(50, y_table_start - 20, 500, 25, fill=1, stroke=0)
    c.setFillColor(colors.white)
    c.drawString(60, y_table_start - 12, "Description")
    c.drawString(350, y_table_start - 12, "Qty")
    c.drawString(400, y_table_start - 12, "Unit Price")
    c.drawString(480, y_table_start - 12, "Amount")
    
    # Line items
    c.setFillColor(colors.black)
    c.setFont("Helvetica", 10)
    y_pos = y_table_start - 45
    
    for i, item in enumerate(line_items):
        # Alternate row colors
        if i % 2 == 0:
            c.setFillColor(colors.HexColor("#f8f9fa"))
            c.rect(50, y_pos - 5, 500, 20, fill=1, stroke=0)
        
        c.setFillColor(colors.black)
        amount = item["quantity"] * item["unit_price"]
        c.drawString(60, y_pos, item["description"])
        c.drawString(350, y_pos, str(item["quantity"]))
        c.drawString(400, y_pos, f"${item['unit_price']:.2f}")
        c.drawString(480, y_pos, f"${amount:.2f}")
        y_pos -= 25
    
    # Totals section
    y_totals = y_pos - 30
    c.setFillColor(colors.black)
    c.line(400, y_totals, 550, y_totals)
    
    c.setFont("Helvetica", 10)
    c.drawString(400, y_totals - 20, "Subtotal:")
    c.drawRightString(540, y_totals - 20, f"${subtotal:.2f}")
    
    c.drawString(400, y_totals - 40, f"Tax ({tax_rate*100:.0f}%):")
    c.drawRightString(540, y_totals - 40, f"${tax_amount:.2f}")
    
    c.setFont("Helvetica-Bold", 12)
    c.drawString(400, y_totals - 70, "Total:")
    c.drawRightString(540, y_totals - 70, f"${total_amount:.2f}")
    
    # Payment terms and notes
    c.setFont("Helvetica", 9)
    c.drawString(50, y_totals - 110, "Payment Terms: Net 30")
    c.drawString(50, y_totals - 125, "Payment Method: Bank Transfer or Credit Card")
    c.drawString(50, y_totals - 140, "Thank you for your business!")
    
    # Footer
    c.setFont("Helvetica", 8)
    c.setFillColor(colors.grey)
    c.drawString(50, 50, f"Generated on {datetime.now().strftime('%Y-%m-%d %H:%M:%S')} for testing purposes")
    
    # Save PDF
    c.save()
    print(f"✅ Sample invoice generated: {output_path}")
    print(f"   Invoice Number: {invoice_number}")
    print(f"   Total Amount: ${total_amount:.2f}")
    return output_path


if __name__ == "__main__":
    # Generate sample invoice
    invoice_path = generate_sample_invoice()
    print(f"\n📄 You can now test the extraction endpoint with:")
    print(f"   curl -X POST http://localhost:8001/extract-invoice \\")
    print(f"     -F \"file=@{invoice_path}\" \\")
    print(f"     -F \"tenant_id=test-tenant-123\"")

