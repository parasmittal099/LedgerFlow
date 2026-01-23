import boto3
import os
from typing import Optional
import logging
import asyncio

logger = logging.getLogger(__name__)

class S3Service:
    def __init__(self):
        aws_access_key = os.getenv("AWS_ACCESS_KEY_ID")
        aws_secret_key = os.getenv("AWS_SECRET_ACCESS_KEY")
        self.bucket_name = os.getenv("S3_BUCKET_NAME")
        
        if not aws_access_key or not aws_secret_key or not self.bucket_name:
            logger.warning("AWS credentials or S3_BUCKET_NAME not set. S3 uploads will be disabled.")
            self.s3_client = None
        else:
            self.s3_client = boto3.client(
                's3',
                region_name=os.getenv("AWS_REGION", "us-east-1"),
                aws_access_key_id=aws_access_key,
                aws_secret_access_key=aws_secret_key
            )

    async def upload_file(self, file_path: str, s3_key: str) -> Optional[str]:
        """
        Upload file to S3
        
        Args:
            file_path: Local file path to upload
            s3_key: S3 key (path) where file should be stored
            
        Returns:
            S3 URL if successful, None if S3 is not configured
        """
        if not self.s3_client or not self.bucket_name:
            logger.warning("S3 not configured. Skipping upload.")
            return None
            
        try:
            # boto3 operations are synchronous, but we're in an async context
            # Use asyncio to run in thread pool to avoid blocking
            loop = asyncio.get_event_loop()
            await loop.run_in_executor(
                None,
                self.s3_client.upload_file,
                file_path,
                self.bucket_name,
                s3_key
            )
            s3_url = f"s3://{self.bucket_name}/{s3_key}"
            logger.info(f"Successfully uploaded file to S3: {s3_url}")
            return s3_url
        except Exception as e:
            logger.error(f"Error uploading file to S3: {e}")
            raise 
    
    async def download_file(self, s3_key: str, file_path: str) -> None:
        """
        Download file from S3
        
        Args:
            s3_key: S3 key (path) of file to download
            file_path: Local path where file should be saved
        """
        if not self.s3_client or not self.bucket_name:
            raise ValueError("S3 not configured")
            
        try:
            loop = asyncio.get_event_loop()
            await loop.run_in_executor(
                None,
                self.s3_client.download_file,
                self.bucket_name,
                s3_key,
                file_path
            )
            logger.info(f"Successfully downloaded file from S3: {s3_key}")
        except Exception as e:
            logger.error(f"Error downloading file from S3: {e}")
            raise 
    

    