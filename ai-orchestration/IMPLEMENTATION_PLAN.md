# AI Orchestration Service - Step-by-Step Implementation Plan

## 🎯 Goal
Build a Python FastAPI service that extracts structured data from invoice PDFs using LLMs.

---

## Step 1: Python Environment Setup

### What We're Doing
Setting up a clean Python environment and installing dependencies.

### Files to Create
- `requirements.txt` - List of Python packages

### What You'll Learn
- Virtual environments
- Package management
- Dependency versioning

### Implementation Details

**1.1 Create `requirements.txt`**
```
# Web Framework
fastapi==0.104.1
uvicorn[standard]==0.24.0

# Data Validation
pydantic==2.5.0
pydantic-settings==2.1.0

# LLM Integration
langchain==0.1.0
langchain-openai==0.0.2
langchain-anthropic==0.1.0

# PDF Processing
pypdf==3.17.4
pdfplumber==0.10.3

# AWS Integration
boto3==1.29.7

# Utilities
python-multipart==0.0.6  # For file uploads
aiofiles==23.2.1         # Async file operations
python-dotenv==1.0.0     # Environment variables
httpx==0.25.2            # HTTP client
tenacity==8.2.3          # Retry logic
```

**1.2 Commands to Run**
```bash
# Create virtual environment
python3 -m venv venv

# Activate it (Mac/Linux)
source venv/bin/activate

# Activate it (Windows)
venv\Scripts\activate

# Install packages
pip install -r requirements.txt
```

**1.3 Verify Setup**
```bash
python --version  # Should show Python 3.11+
pip list          # Should show installed packages
```

---

## Step 2: FastAPI Basics - Hello World API

### What We're Doing
Creating a basic FastAPI application with a health check endpoint.

### Files to Create
- `main.py` - Main application file

### What You'll Learn
- FastAPI app structure
- Route decorators
- HTTP methods
- Running a development server

### Implementation Details

**2.1 Create `main.py`**
```python
"""
LedgerFlow AI Orchestration Service
A FastAPI service for extracting invoice data using LLMs
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# Create FastAPI application instance
app = FastAPI(
    title="LedgerFlow AI Orchestration",
    description="Agentic AI service for invoice extraction",
    version="1.0.0"
)

# Enable CORS (Cross-Origin Resource Sharing)
# This allows frontend to call this API
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],  # Frontend URL
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Define a route (endpoint)
@app.get("/health")
async def health_check():
    """
    Health check endpoint
    Returns status of the service
    """
    return {
        "status": "healthy",
        "service": "ai-orchestration",
        "version": "1.0.0"
    }

# Run the server (only when script is executed directly)
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
```

**2.2 Run the Server**
```bash
# Method 1: Using uvicorn directly
uvicorn main:app --reload --port 8001

# Method 2: Using Python
python main.py
```

**2.3 Test the API**
```bash
# Test health endpoint
curl http://localhost:8001/health

# Or open in browser
# http://localhost:8001/health
# http://localhost:8001/docs (automatic API documentation!)
```

**Key Concepts:**
- `@app.get()` - Decorator that creates a GET endpoint
- `async def` - Asynchronous function (non-blocking)
- `FastAPI()` - Creates the application
- `uvicorn` - ASGI server that runs FastAPI

---

## Step 3: PDF Text Extraction

### What We're Doing
Creating utilities to extract text from PDF files.

### Files to Create
- `utils/__init__.py` - Makes utils a Python package
- `utils/pdf_utils.py` - PDF processing functions

### What You'll Learn
- Working with files in Python
- Error handling
- Multiple library approaches
- File I/O

### Implementation Details

**3.1 Create `utils/__init__.py`**
```python
# Empty file - makes utils a Python package
```

**3.2 Create `utils/pdf_utils.py`**
```python
"""
PDF Utilities
Functions for extracting text from PDF files
"""

import logging
from typing import Optional
import pypdf
import pdfplumber

logger = logging.getLogger(__name__)


def extract_text_from_pdf(file_path: str) -> str:
    """
    Extract text from a PDF file using multiple methods
    
    Args:
        file_path: Path to the PDF file
        
    Returns:
        Extracted text as a string
        
    Raises:
        FileNotFoundError: If file doesn't exist
        ValueError: If text extraction fails
    """
    text = ""
    
    # Method 1: Try pdfplumber (better for tables)
    try:
        with pdfplumber.open(file_path) as pdf:
            for page in pdf.pages:
                page_text = page.extract_text()
                if page_text:
                    text += page_text + "\n"
        
        if text.strip():
            logger.info(f"Successfully extracted text using pdfplumber")
            return text.strip()
    
    except Exception as e:
        logger.warning(f"pdfplumber failed: {e}, trying pypdf")
    
    # Method 2: Fallback to pypdf
    try:
        with open(file_path, 'rb') as file:
            pdf_reader = pypdf.PdfReader(file)
            for page in pdf_reader.pages:
                text += page.extract_text() + "\n"
        
        if text.strip():
            logger.info(f"Successfully extracted text using pypdf")
            return text.strip()
        else:
            raise ValueError("Could not extract text from PDF")
    
    except Exception as e:
        logger.error(f"Both PDF extraction methods failed: {e}")
        raise ValueError(f"Failed to extract text from PDF: {e}")


def validate_pdf(file_path: str) -> bool:
    """
    Validate that a file is a valid PDF
    
    Args:
        file_path: Path to the file
        
    Returns:
        True if valid PDF, False otherwise
    """
    try:
        with open(file_path, 'rb') as file:
            # Check PDF magic number
            header = file.read(4)
            return header == b'%PDF'
    except Exception:
        return False
```

**3.3 Test PDF Extraction**
```python
# Create a test script: test_pdf.py
from utils.pdf_utils import extract_text_from_pdf

text = extract_text_from_pdf("sample_invoice.pdf")
print(text)
```

**Key Concepts:**
- `with open()` - Context manager for file handling (auto-closes)
- `try/except` - Error handling
- Multiple fallback strategies
- Logging for debugging

---

## Step 4: Data Models with Pydantic

### What We're Doing
Defining the structure of invoice data using Pydantic models.

### Files to Create
- `models/__init__.py`
- `models/invoice_models.py`

### What You'll Learn
- Data modeling
- Type validation
- Optional vs required fields
- Field descriptions

### Implementation Details

**4.1 Create `models/__init__.py`**
```python
# Makes models a Python package
```

**4.2 Create `models/invoice_models.py`**
```python
"""
Invoice Data Models
Pydantic models for structured invoice data
"""

from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime


class LineItem(BaseModel):
    """Represents a single line item on an invoice"""
    description: str = Field(description="Item description")
    quantity: float = Field(description="Quantity ordered")
    unit_price: float = Field(description="Price per unit")
    amount: float = Field(description="Total amount (quantity × unit_price)")


class InvoiceData(BaseModel):
    """Structured invoice data extracted from PDF"""
    vendor_name: str = Field(description="Name of the vendor/company")
    invoice_number: str = Field(description="Invoice number or ID")
    invoice_date: str = Field(description="Invoice date in YYYY-MM-DD format")
    due_date: Optional[str] = Field(None, description="Due date in YYYY-MM-DD format")
    total_amount: float = Field(description="Total invoice amount")
    currency: str = Field(default="USD", description="Currency code (USD, EUR, etc.)")
    line_items: List[LineItem] = Field(description="List of line items")
    tax_amount: Optional[float] = Field(None, description="Tax amount")
    shipping_amount: Optional[float] = Field(None, description="Shipping amount")
    payment_terms: Optional[str] = Field(None, description="Payment terms (e.g., Net 30)")
    vendor_address: Optional[str] = Field(None, description="Vendor address")
    billing_address: Optional[str] = Field(None, description="Billing address")
    
    class Config:
        """Pydantic configuration"""
        json_schema_extra = {
            "example": {
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
```

**Key Concepts:**
- `BaseModel` - Base class for Pydantic models
- `Field()` - Field definition with description/validation
- `Optional[]` - Field that can be None
- `List[]` - List of items
- Type hints for validation

---

## Step 5: LLM Integration

### What We're Doing
Integrating with OpenAI/Anthropic to extract structured data from invoice text.

### Files to Create
- `services/__init__.py`
- `services/invoice_extractor.py`

### What You'll Learn
- API integration
- Prompt engineering
- Structured output parsing
- Error handling and retries

### Implementation Details

**5.1 Create `services/__init__.py`**
```python
# Makes services a Python package
```

**5.2 Create `services/invoice_extractor.py`**
```python
"""
Invoice Extractor Service
Uses LLMs to extract structured data from invoices
"""

import os
import json
import logging
from typing import Dict, Any
from langchain_openai import ChatOpenAI
from langchain_anthropic import ChatAnthropic
from langchain.prompts import ChatPromptTemplate
from langchain.output_parsers import PydanticOutputParser
from pydantic import ValidationError

from models.invoice_models import InvoiceData
from utils.pdf_utils import extract_text_from_pdf

logger = logging.getLogger(__name__)


class InvoiceExtractor:
    """Extracts structured data from invoices using LLM"""
    
    def __init__(self):
        # Initialize LLM client
        # Try OpenAI first, fallback to Anthropic
        openai_key = os.getenv("OPENAI_API_KEY")
        anthropic_key = os.getenv("ANTHROPIC_API_KEY")
        
        if openai_key:
            self.llm = ChatOpenAI(
                model="gpt-4-turbo-preview",
                temperature=0,  # Low temperature for consistent results
                api_key=openai_key
            )
            logger.info("Using OpenAI GPT-4")
        elif anthropic_key:
            self.llm = ChatAnthropic(
                model="claude-3-opus-20240229",
                temperature=0,
                api_key=anthropic_key
            )
            logger.info("Using Anthropic Claude")
        else:
            raise ValueError("No API key found. Set OPENAI_API_KEY or ANTHROPIC_API_KEY")
        
        # Set up output parser
        self.parser = PydanticOutputParser(pydantic_object=InvoiceData)
        
        # Create prompt template
        self.prompt_template = ChatPromptTemplate.from_messages([
            ("system", """You are an expert invoice extraction agent. 
            
            Extract structured data from invoices with high accuracy.
            
            Instructions:
            1. Read the invoice text carefully
            2. Extract all relevant fields accurately
            3. Parse line items with quantities, unit prices, and amounts
            4. Calculate totals and verify consistency
            5. Return structured JSON data
            
            Be precise with numbers and dates. If a field is not found, use null.
            {format_instructions}"""),
            ("human", "Extract data from this invoice:\n\n{invoice_text}")
        ])
    
    async def extract(self, file_path: str, tenant_id: str) -> Dict[str, Any]:
        """
        Extract structured data from invoice PDF
        
        Args:
            file_path: Path to the invoice PDF
            tenant_id: Tenant identifier for multi-tenancy
            
        Returns:
            Dictionary with extracted invoice data and metadata
        """
        try:
            logger.info(f"Extracting invoice from {file_path} for tenant {tenant_id}")
            
            # Step 1: Extract text from PDF
            invoice_text = extract_text_from_pdf(file_path)
            
            if not invoice_text or len(invoice_text.strip()) < 50:
                raise ValueError("Could not extract sufficient text from PDF")
            
            # Step 2: Limit text length (LLMs have token limits)
            # Keep first 8000 characters
            invoice_text_limited = invoice_text[:8000]
            
            # Step 3: Prepare prompt
            prompt = self.prompt_template.format_messages(
                invoice_text=invoice_text_limited,
                format_instructions=self.parser.get_format_instructions()
            )
            
            # Step 4: Call LLM
            logger.info("Calling LLM for extraction...")
            response = self.llm.invoke(prompt)
            
            # Step 5: Parse response
            try:
                extracted_data = self.parser.parse(response.content)
                invoice_dict = extracted_data.model_dump()
            except ValidationError as e:
                logger.warning(f"Failed to parse structured output: {e}")
                # Fallback: try to extract JSON from response
                import re
                json_match = re.search(r'\{.*\}', response.content, re.DOTALL)
                if json_match:
                    invoice_dict = json.loads(json_match.group())
                else:
                    raise ValueError("Could not parse LLM response")
            
            # Step 6: Calculate confidence score
            confidence_score = self._calculate_confidence(invoice_dict, invoice_text)
            
            return {
                "extracted_data": invoice_dict,
                "confidence_score": confidence_score,
                "raw_text_length": len(invoice_text),
                "extraction_method": "agentic_llm"
            }
        
        except Exception as e:
            logger.error(f"Error extracting invoice: {str(e)}", exc_info=True)
            raise
    
    def _calculate_confidence(self, extracted_data: Dict[str, Any], raw_text: str) -> float:
        """Calculate confidence score for extraction"""
        score = 0.0
        max_score = 10.0
        
        # Check required fields
        required_fields = ["vendor_name", "invoice_number", "invoice_date", "total_amount"]
        for field in required_fields:
            if extracted_data.get(field):
                score += 2.0
        
        # Check line items
        if extracted_data.get("line_items") and len(extracted_data["line_items"]) > 0:
            score += 2.0
        
        # Normalize to 0-1
        return min(score / max_score, 1.0)
```

**5.3 Update `main.py` to use extractor**
```python
# Add to main.py
from services.invoice_extractor import InvoiceExtractor

extractor = InvoiceExtractor()

@app.post("/extract-invoice")
async def extract_invoice(file_path: str):
    result = await extractor.extract(file_path, tenant_id="default")
    return result
```

**Key Concepts:**
- API keys from environment variables
- Prompt engineering (clear instructions)
- Structured output parsing
- Error handling with fallbacks
- Async/await for API calls

---

## Step 6: File Upload Endpoint

### What We're Doing
Adding an endpoint to accept PDF file uploads.

### Files to Modify
- `main.py` - Add upload endpoint

### Implementation Details

**6.1 Update `main.py`**
```python
from fastapi import File, UploadFile
import aiofiles
import os

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@app.post("/extract-invoice")
async def extract_invoice(
    file: UploadFile = File(...),
    tenant_id: str = "default"
):
    """
    Extract structured data from uploaded invoice PDF
    """
    # Save uploaded file temporarily
    file_path = os.path.join(UPLOAD_DIR, file.filename)
    
    async with aiofiles.open(file_path, 'wb') as out_file:
        content = await file.read()
        await out_file.write(content)
    
    try:
        # Extract invoice data
        result = await extractor.extract(file_path, tenant_id)
        
        # Clean up
        os.remove(file_path)
        
        return {
            "status": "success",
            "filename": file.filename,
            **result
        }
    except Exception as e:
        # Clean up on error
        if os.path.exists(file_path):
            os.remove(file_path)
        raise HTTPException(status_code=500, detail=str(e))
```

**Key Concepts:**
- `UploadFile` - FastAPI file upload type
- `aiofiles` - Async file operations
- Temporary file handling
- Cleanup on success/error

---

## Step 7: S3 Integration (Optional for MVP)

### What We're Doing
Adding AWS S3 integration for cloud file storage.

### Files to Create
- `services/s3_service.py`

### Implementation Details

**7.1 Create `services/s3_service.py`**
```python
"""
S3 Service
Handles file storage in AWS S3
"""

import boto3
import os
from typing import Optional
import logging

logger = logging.getLogger(__name__)


class S3Service:
    """Service for AWS S3 operations"""
    
    def __init__(self):
        self.s3_client = boto3.client(
            's3',
            region_name=os.getenv("AWS_REGION", "us-east-1"),
            aws_access_key_id=os.getenv("AWS_ACCESS_KEY_ID"),
            aws_secret_access_key=os.getenv("AWS_SECRET_ACCESS_KEY")
        )
        self.bucket_name = os.getenv("S3_BUCKET_NAME")
    
    async def upload_file(self, file_path: str, s3_key: str) -> str:
        """Upload file to S3"""
        try:
            self.s3_client.upload_file(file_path, self.bucket_name, s3_key)
            return f"s3://{self.bucket_name}/{s3_key}"
        except Exception as e:
            logger.error(f"Failed to upload to S3: {e}")
            raise
    
    async def download_file(self, s3_key: str, local_path: str) -> str:
        """Download file from S3"""
        try:
            self.s3_client.download_file(self.bucket_name, s3_key, local_path)
            return local_path
        except Exception as e:
            logger.error(f"Failed to download from S3: {e}")
            raise
```

---

## Step 8: Docker Configuration

### What We're Doing
Containerizing the application for deployment.

### Files to Create
- `Dockerfile`

### Implementation Details

**8.1 Create `Dockerfile`**
```dockerfile
FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    g++ \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY . .

# Create uploads directory
RUN mkdir -p uploads

# Expose port
EXPOSE 8001

# Run the application
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8001"]
```

**8.2 Build and Run**
```bash
# Build image
docker build -t ledgerflow-ai .

# Run container
docker run -p 8001:8001 --env-file .env ledgerflow-ai
```

---

## Step 9: Complete Integration

### What We're Doing
Putting all pieces together with proper error handling and logging.

### Files to Modify
- `main.py` - Complete implementation

### Final `main.py` Structure
```python
"""
Complete FastAPI application with all features
"""
from fastapi import FastAPI, File, UploadFile, HTTPException, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
import logging
import os
from dotenv import load_dotenv

from services.invoice_extractor import InvoiceExtractor
from services.s3_service import S3Service

load_dotenv()

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="LedgerFlow AI Orchestration",
    description="Agentic AI service for invoice extraction",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize services
extractor = InvoiceExtractor()
s3_service = S3Service()

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "ai-orchestration"}

@app.post("/extract-invoice")
async def extract_invoice(
    file: UploadFile = File(...),
    tenant_id: str = "default"
):
    """Extract structured data from invoice PDF"""
    # Implementation from Step 6
    pass

# Add more endpoints as needed
```

---

## Testing Checklist

- [ ] Health endpoint returns 200
- [ ] Can upload PDF file
- [ ] PDF text extraction works
- [ ] LLM extraction returns structured data
- [ ] Error handling works for invalid PDFs
- [ ] Error handling works for API failures
- [ ] Docker container builds and runs
- [ ] Environment variables load correctly

---

## Next Steps After MVP

1. Add policy validation (RAG)
2. Add conflict resolution
3. Add SQS integration
4. Add comprehensive logging
5. Add unit tests
6. Add API documentation

---

## Common Issues & Solutions

**Issue: Import errors**
- Solution: Make sure you're in the virtual environment and packages are installed

**Issue: LLM API errors**
- Solution: Check API key, check rate limits, add retry logic

**Issue: PDF extraction fails**
- Solution: Try different PDF libraries, check if PDF is image-based (needs OCR)

**Issue: Docker build fails**
- Solution: Check Dockerfile syntax, ensure all files are copied

---

This plan provides a complete learning path from zero to a working AI orchestration service!



