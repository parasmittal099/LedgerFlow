from fastapi import FastAPI,UploadFile,File,Form,HTTPException
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
from services.invoice_extractor import InvoiceExtractor
from services.s3_service import S3Service
import aiofiles
import os
from dotenv import load_dotenv
from datetime import datetime
import uuid
import logging 

load_dotenv()

app = FastAPI(
    title = "LedgerFlow AI Orchestration Service",
    description="Agentic AI service for invoice extraction and processing",
    version="1.0.0"
)

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

# Initialize S3 service
s3_service = None

def get_s3_service():
    """Lazy initialization of S3 service"""
    global s3_service
    if s3_service is None:
        s3_service = S3Service()
    return s3_service

# Enable CORS for local frontend development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)



@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "service": "ai-orchestration",
        "version": "1.0.0"
    }



@app.post("/extract-invoice")
async def extract_invoice(file: UploadFile = File(...), tenant_id: str = Form(...)):
    """
    Extract invoice data from uploaded PDF and upload to S3
    
    - Saves file locally temporarily
    - Uploads to S3 with organized path structure
    - Extracts invoice data using AI
    - Returns extraction result with S3 URL
    - Cleans up local file
    """
    # Generate unique filename to avoid conflicts
    file_extension = os.path.splitext(file.filename)[1] if file.filename else ".pdf"
    unique_filename = f"{uuid.uuid4()}{file_extension}"
    file_path = os.path.join(UPLOAD_DIR, unique_filename)
    
    s3_key = None
    s3_url = None
    
    try:
        # Step 1: Save file locally
        async with aiofiles.open(file_path, 'wb') as out_file:
            content = await file.read()
            await out_file.write(content)
        
        # Step 2: Upload to S3
        try:
            s3_service_instance = get_s3_service()
            # Organize S3 path: tenant_id/year/month/filename
            year = datetime.now().strftime("%Y")
            month = datetime.now().strftime("%m")
            s3_key = f"{tenant_id}/{year}/{month}/{unique_filename}"
            s3_url = await s3_service_instance.upload_file(file_path, s3_key)
        except Exception as s3_error:
            logging.warning(f"S3 upload failed: {s3_error}. Continuing with extraction...")
        
        # Step 3: Extract invoice data
        extractor_instance = InvoiceExtractor()
        result = await extractor_instance.extract(file_path, tenant_id)
        
        # Step 4: Clean up local file
        if os.path.exists(file_path):
            os.remove(file_path)
        
        # Step 5: Return result with S3 URL
        return {
            "status": "success",
            "filename": file.filename,
            "s3_key": s3_key,
            "s3_url": s3_url,
            **result
        }
        
    except Exception as e:
        # Clean up on error
        if os.path.exists(file_path):
            os.remove(file_path)
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8001)
    

