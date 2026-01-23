from pydantic import BaseModel, Field
from typing import Optional,List 
from datetime import datetime 

class LineItem(BaseModel):
    description: str = Field(description="Item description")
    quantity: float = Field(description="Quantity ordered")
    unit_price: float = Field(description="Price per unit")
    amount: float = Field(description="Total amount for the item")
    
class InvoiceData(BaseModel):
    vendor_name: str = Field(description="Name of the vendor")
    invoice_number: str = Field(description="Invoice number")
    invoice_date: datetime = Field(description="Date of the invoice")
    due_date: Optional[str] = Field(None,description="Due date in YYYY-MM-DD format")
    total_amount: float = Field(description="Total invoice amount")
    currency: str = Field(default="USD",description="Currency of the invoice")
    line_items: List[LineItem] = Field(description="List of line items")
    tax_amount: Optional[float] = Field(None, description="Tax amount")
    shipping_amount: Optional[float] = Field(None, description="Shipping amount")
    payment_terms: Optional[str] = Field(None, description="Payment terms (e.g., Net 30)")
    vendor_address: Optional[str] = Field(None, description="Vendor address")
    billing_address: Optional[str] = Field(None, description="Billing address")
    

class Config:
    json_schema_extra = {
        "example":{
            "vendor_name": "Acme Corp",
                "invoice_number": "INV-2024-001",
                "invoice_date": "2024-01-15",
                "total_amount": 1500.00,
                "currency": "USD",
                "line_items": [
                    {
                        "description": "Software License",
                        "quantity": 10,
                        "unit_price": 150.00,
                        "amount": 1500.00
                    }
                ]
        }
    }