import os 
import json 
import logging 
from typing import Dict,Any
from langchain_openai import ChatOpenAI
from langchain.prompts import ChatPromptTemplate
from langchain.output_parsers import PydanticOutputParser
from pydantic import ValidationError
from models.invoice_models import InvoiceData
from utils.pdf_utils import extract_text_from_pdf, validate_pdf
import re 


logger = logging.getLogger(__name__)

class InvoiceExtractor:
    def __init__(self):
        openai_api_key = os.getenv("OPENAI_API_KEY")
        self.llm = ChatOpenAI(
                model="gpt-5-mini",api_key=openai_api_key, 
                temperature=1,
                openai_api_key = openai_api_key
            )
        
        parser = PydanticOutputParser(pydantic_object=InvoiceData)
        self.parser = parser
        
        self.prompt_template = ChatPromptTemplate.from_messages([
            ("system","""You are an expert invoice extraction agent. 
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
    
    async def extract(self,file_path:str,tenant_id:str) -> Dict[str,Any]: 
        try:
            logger.info(f"Extracting invoice from {file_path} for tenant {tenant_id}")
            invoice_text = extract_text_from_pdf(file_path)
            if not invoice_text or len(invoice_text.strip()) < 50:
                raise ValueError("Could not extract sufficient text from PDF")
            invoice_text_limited = invoice_text[:8000]
            prompt = self.prompt_template.format_messages(
                invoice_text=invoice_text_limited,
                format_instructions = self.parser.get_format_instructions()
            )
            response = self.llm.invoke(prompt)
            try:
                extracted_data = self.parser.parse(response.content)
                invoice_dict = extracted_data.model_dump()
            except ValidationError as e:
                logger.warning(f"Failed to parse structured output: {e}")    
                json_match = re.search(r'\{.*\}', response.content, re.DOTALL)
                if json_match:
                    invoice_dict = json.loads(json_match.group())
                else:
                    raise ValueError("Could not parse LLM response")
            
            confidence_score = self._calculate_confidence(invoice_dict,invoice_text)
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
        score = 0.0
        max_score = 10.0
        required_fields = ["vendor_name", "invoice_number", "invoice_date", "total_amount"]
        for field in required_fields:
            if extracted_data.get(field):
                score += 2.0
        if extracted_data.get("line_items") and len(extracted_data["line_items"]) > 0:
            score += 2.0
        return min(score / max_score, 1.0)
        
        
        