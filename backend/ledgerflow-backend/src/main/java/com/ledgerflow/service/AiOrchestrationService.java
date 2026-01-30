package com.ledgerflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiOrchestrationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.ai-orchestration.url:http://localhost:8001}")
    private String aiOrchestrationUrl;

    public AiOrchestrationService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> extractInvoice(MultipartFile file, String tenantId) throws IOException {
        // Create temporary file
        File tempFile = File.createTempFile("invoice_", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        try {
            // Prepare multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile));
            body.add("tenant_id", tenantId);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Call AI orchestration service
            String url = aiOrchestrationUrl + "/extract-invoice";
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                // Parse JSON response - keep dates as strings to avoid auto-parsing issues
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                Map<String, Object> result = new HashMap<>();

                // Extract data - manually extract to keep dates as strings
                if (jsonNode.has("extracted_data")) {
                    JsonNode extractedDataNode = jsonNode.get("extracted_data");
                    Map<String, Object> extractedDataMap = new HashMap<>();

                    // Manually extract each field to keep dates as strings
                    if (extractedDataNode.has("invoice_number")) {
                        extractedDataMap.put("invoice_number", extractedDataNode.get("invoice_number").asText());
                    }
                    if (extractedDataNode.has("vendor_name")) {
                        extractedDataMap.put("vendor_name", extractedDataNode.get("vendor_name").asText());
                    }
                    if (extractedDataNode.has("invoice_date")) {
                        extractedDataMap.put("invoice_date", extractedDataNode.get("invoice_date").asText());
                    }
                    if (extractedDataNode.has("due_date") && !extractedDataNode.get("due_date").isNull()) {
                        extractedDataMap.put("due_date", extractedDataNode.get("due_date").asText());
                    }
                    if (extractedDataNode.has("total_amount")) {
                        extractedDataMap.put("total_amount", extractedDataNode.get("total_amount").asDouble());
                    }
                    if (extractedDataNode.has("currency")) {
                        extractedDataMap.put("currency", extractedDataNode.get("currency").asText());
                    }
                    if (extractedDataNode.has("tax_amount") && !extractedDataNode.get("tax_amount").isNull()) {
                        extractedDataMap.put("tax_amount", extractedDataNode.get("tax_amount").asDouble());
                    }
                    if (extractedDataNode.has("shipping_amount") && !extractedDataNode.get("shipping_amount").isNull()) {
                        extractedDataMap.put("shipping_amount", extractedDataNode.get("shipping_amount").asDouble());
                    }
                    if (extractedDataNode.has("payment_terms") && !extractedDataNode.get("payment_terms").isNull()) {
                        extractedDataMap.put("payment_terms", extractedDataNode.get("payment_terms").asText());
                    }
                    if (extractedDataNode.has("line_items")) {
                        List<Map<String, Object>> lineItems = new ArrayList<>();
                        for (JsonNode itemNode : extractedDataNode.get("line_items")) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("description", itemNode.get("description").asText());
                            item.put("quantity", itemNode.get("quantity").asDouble());
                            item.put("unit_price", itemNode.get("unit_price").asDouble());
                            item.put("amount", itemNode.get("amount").asDouble());
                            lineItems.add(item);
                        }
                        extractedDataMap.put("line_items", lineItems);
                    }

                    result.put("extracted_data", extractedDataMap);
                }
                if (jsonNode.has("confidence_score")) {
                    result.put("confidence_score", jsonNode.get("confidence_score").asDouble());
                }
                if (jsonNode.has("s3_key") && !jsonNode.get("s3_key").isNull()) {
                    result.put("s3_key", jsonNode.get("s3_key").asText());
                }
                if (jsonNode.has("s3_url") && !jsonNode.get("s3_url").isNull()) {
                    result.put("s3_url", jsonNode.get("s3_url").asText());
                }
                if (jsonNode.has("filename")) {
                    result.put("filename", jsonNode.get("filename").asText());
                }

                return result;

            } else {
                throw new RuntimeException("AI orchestration service returned error: " + response.getStatusCode());
            }
        } finally {
            // Clean up temp file
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}