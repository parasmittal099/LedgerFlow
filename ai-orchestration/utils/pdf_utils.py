import pdfplumber
import pypdf
import logging

logger = logging.getLogger(__name__)

"""
Extract text from a PDF file using pdfplumber.
"""
def extract_text_from_pdf(pdf_path: str) -> str:
    """
    Extract text from a PDF file using pdfplumber and pypdf.
    """
    text = ""
    try:
        with pdfplumber.open(pdf_path) as pdf:
            for page in pdf.pages:
                page_text = page.extract_text()
                if page_text:
                    text += page_text + "\n"
        
        if text.strip():
            return text.strip()
        
    except Exception as e:
        logger.warning(f"pdfplumber failed: {e}")
    
""" 
    Validating a PDF's "magic number" means checking its first few bytes for the signature %PDF-, which should start with %PDF (hex: 25 50 44 46
"""    
def validate_pdf(file_path: str) -> bool:
    try:
        with open(file_path,'rb') as file:
            magic_number = file.read(4)
            return magic_number == b'%PDF-'
    except Exception as e:
        logger.warning(f"Error validating PDF: {e}")
        return False

