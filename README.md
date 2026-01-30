# LedgerFlow
**Agentic AI Invoice Lifecycle & Conflict Resolution Platform**

LedgerFlow is a next-generation, agentic SaaS platform designed to transform Accounts Payable from a manual data-entry chore into an autonomous "Digital Employee" workflow. Inspired by platforms like Zamp Pace, LedgerFlow leverages LLMs, RAG, and an event-driven microservices architecture to handle unstructured financial data, resolve complex billing conflicts, and provide real-time spend visibility.

## 🚀 The Vision

Traditional invoice systems rely on rigid templates that break when a vendor changes a PDF layout. LedgerFlow uses Agentic ETL to semantically understand any invoice format, performing a "Three-Way Match" (Invoice vs. PO vs. Receipts) and autonomously drafting dispute resolutions for human approval.

## 🏗️ Technical Architecture

The project is built on a **Polyglot Microservices** foundation to demonstrate expertise in distributed systems and scalability:

- **AI Orchestration (Python/FastAPI)**: Orchestrates LangGraph agents for data extraction and "Agentic RAG" to validate invoices against company policy documents stored in a Vector Database (ChromaDB).
- **Core Business Logic (Java/Spring Boot)**: Handles the high-integrity transactional ledger, complex matching logic, and multi-tenant data isolation.
- **Event-Driven Pipeline**: Uses AWS SQS as a message broker to decouple file ingestion from heavy AI processing, ensuring system resilience.
- **Infrastructure**: Fully containerized with Docker, deployed on AWS ECS (Fargate), using PostgreSQL for relational data and S3 for document storage.

## 🌟 Key Features

- **Zero-Template Extraction**: Uses LLMs to pull line items and metadata from any invoice layout with high confidence.
- **Agentic Conflict Resolution**: When a price or quantity mismatch occurs, the AI analyzes historical context and drafts a professional dispute email for the user.
- **Policy-Aware Auditing**: A RAG (Retrieval-Augmented Generation) pipeline that checks every invoice against internal procurement PDFs to flag non-compliance.
- **Human-in-the-Loop Dashboard**: A modern React interface that highlights "AI Reasoning," allowing humans to supervise and override agent decisions easily.

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React, Next.js, Tailwind CSS, Shadcn/UI |
| AI / NLP | Python, LangChain, LangGraph, OpenAI/Claude |
| Backend | Java (Spring Boot) |
| Database | PostgreSQL (Relational), ChromaDB (Vector), Redis (Caching) |
| Cloud/DevOps | AWS (S3, SQS, ECS), Docker, GitHub Actions |

## 📁 Project Structure

```
LedgerFlow/
├── frontend/              # Next.js React application
├── ai-orchestration/      # Python FastAPI service
├── backend/               # Java Spring Boot service
├── docker-compose.yml     # Local development setup
├── .github/workflows/     # CI/CD pipelines
└── docs/                  # Architecture documentation
```

## 🚦 Getting Started

### Prerequisites

- Node.js 18+
- Python 3.11+
- Java 17+
- Docker & Docker Compose
- AWS Account (for S3, SQS, ECS)

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd LedgerFlow
   ```

2. **Start services with Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Set up environment variables**
   - Copy `.env.example` files in each service directory
   - Add your API keys (OpenAI/Claude, AWS credentials)

4. **Run services individually (for development)**
   ```bash
   # Frontend
   cd frontend && npm install && npm run dev
   
   # AI Orchestration
   cd ai-orchestration && pip install -r requirements.txt && uvicorn main:app --reload
   
   # Backend
   cd backend && ./mvnw spring-boot:run
   ```

## 📈 Why This Matters

This project solves several high-level engineering challenges:

- **Distributed Systems**: Handling asynchronous state across multiple languages and services.
- **LLM Reliability**: Moving beyond simple prompts to structured, validated JSON outputs and stateful agentic loops.
- **Observability**: Implementing structured logging and health checks across a containerized environment.
- **Security**: Implementing multi-tenant architecture to ensure strict data perimeter security.

## 🔐 Security & Multi-Tenancy

- Tenant isolation at the database level
- JWT-based authentication
- S3 bucket policies for document access control
- Encrypted data in transit and at rest

## 📝 License

MIT License

