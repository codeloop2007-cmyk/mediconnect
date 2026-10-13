# MediConnect AI Services (Python Microservice)

Lightweight, high-performance Python AI microservice for **MediConnect AI**. Provides text vector embeddings and a LangChain clinical triage agent. 

> **Architecture Note**: This service contains **no database connections**. The PostgreSQL database with `pgvector` is managed entirely by your **Java Spring Boot backend**. Java calls this AI service to compute vector embeddings when creating/updating doctors or searching for matching physicians.

---

## Quick Start

### 1. Start the Microservice
```bash
cd ai_services
./venv/bin/uvicorn main:app --host 0.0.0.0 --port 8000
```
Interactive Swagger documentation is available at: `http://localhost:8000/docs`

### 2. Run Tests
```bash
./venv/bin/python test_service.py
```

---

## API Endpoints

### 1. Vector Embedding Endpoint
- **URL**: `POST /api/v1/embeddings` (or `POST /embed`)
- **Purpose**: Generates a dense 384-dimensional vector embedding for any text string. Use this in your Java backend when creating or updating a doctor/user.

#### Request (Single String):
```json
{
  "text": "Dr. Sarah Chen, Cardiologist specializing in heart disease and hypertension."
}
```

#### Request (Batch Strings):
```json
{
  "texts": [
    "Dr. Sarah Chen, Cardiologist in Boston",
    "Dr. Marcus Vance, Neurologist in New York"
  ]
}
```

#### Response:
```json
{
  "model": "BAAI/bge-small-en-v1.5",
  "dimension": 384,
  "embedding": [
    0.031434,
    0.055889,
    0.04582,
    0.02227,
    -0.01922,
    ...
  ]
}
```

---

### 2. LangChain AI Agent Endpoint
- **URL**: `POST /api/v1/agent/query`
- **Purpose**: Evaluates patient symptoms, performs clinical triage with LangChain, generates the query vector embedding for pgvector search, and formulates consultation recommendations.

#### Request:
```json
{
  "query": "I have been having sharp chest pain, shortness of breath, and heart palpitations after climbing stairs.",
  "doctors": [
    {
      "doctorId": 2,
      "name": "Dr. Sarah Chen",
      "specialization": "Cardiology",
      "qualification": "MD, FACC",
      "consultationFee": 180.0
    }
  ]
}
```

#### Response:
```json
{
  "query": "I have been having sharp chest pain, shortness of breath, and heart palpitations after climbing stairs.",
  "recommended_specialty": "Cardiology",
  "inferred_specialties": [
    "Cardiology"
  ],
  "query_embedding": [
    0.0241,
    -0.0312,
    ...
  ],
  "candidate_doctors": [...],
  "reasoning": "Based on your reported symptoms ('...'), our LangChain clinical triage identified Cardiology as the primary recommended medical specialty. Dr. Sarah Chen (Cardiology) is recommended for a comprehensive consultation.",
  "disclaimer": "Medical Disclaimer: MediConnect AI provides informational recommendations based on symptom matching..."
}
```

---

## How to Use in Java Spring Boot Backend

### A. Add Vector Column in PostgreSQL (Run Once)
```sql
CREATE EXTENSION IF NOT EXISTS vector;
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS embedding vector(384);
```

### B. Calling Python from Java when Creating / Updating Doctor
```java
// 1. Prepare profile text
String profileText = doctor.getName() + ", " + doctor.getSpecialization() + ", " + doctor.getQualification();

// 2. Call Python AI Service
RestTemplate restTemplate = new RestTemplate();
Map<String, String> request = Map.of("text", profileText);
Map<String, Object> response = restTemplate.postForObject(
    "http://localhost:8000/api/v1/embeddings", 
    request, 
    Map.class
);

List<Double> embedding = (List<Double>) response.get("embedding");

// 3. Save vector string into PostgreSQL using native query
// Format: "[0.0123, -0.0456, ...]"
String vectorString = embedding.toString();
entityManager.createNativeQuery(
    "UPDATE doctors SET embedding = CAST(:vector AS vector) WHERE doctor_id = :id"
)
.setParameter("vector", vectorString)
.setParameter("id", doctor.getDoctorId())
.executeUpdate();
```

### C. Semantic Search from Java using pgvector
```java
// Query nearest doctors by cosine distance (<=>)
String vectorString = patientQueryEmbedding.toString();

List<Doctor> matchedDoctors = entityManager.createNativeQuery(
    "SELECT * FROM doctors ORDER BY embedding <=> CAST(:vector AS vector) LIMIT :topK",
    Doctor.class
)
.setParameter("vector", vectorString)
.setParameter("topK", 5)
.getResultList();
```

