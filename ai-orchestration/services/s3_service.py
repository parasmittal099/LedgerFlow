import boto3
import os
from typing import Optional
import logging

logger = logging.getLogger(__name__)

class S3Service:
    def __init__(self):
        self.s3_client = boto3.client(
            's3',
            region_name=os.getenv("AWS_REGION", "us-east-2"),
            aws_access_key_id=os.getenv("AWS_ACCESS_KEY_ID"),
            aws_secret_access_key=os.getenv("AWS_SECRET_ACCESS_KEY")
        )
        self.bucket_name = os.getenv("S3_BUCKET_NAME")

    async def upload_file(self,file_path: str, s3_key: str) -> str:
        try:
            self.s3_client.upload_file(file_path, self.bucket_name, s3_key)
            return f"s3://{self.bucket_name}/{s3_key}"
        except Exception as e:
            logger.error(f"Error uploading file to S3: {e}")
            raise 
    
    async def download_file(self, s3_key: str, file_path: str) -> None:
        try:
            self.s3_client.download_file(self.bucket_name, s3_key, file_path)
        except Exception as e:
            logger.error(f"Error downloading file from S3: {e}")
            raise 
    

    