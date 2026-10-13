from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
import logging

from embeddings import generate_embedding, generate_embeddings, EMBEDDING_MODEL_NAME, EMBEDDING_DIM
from agent import doctor_agent

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")
logger = logging.getLogger("mediconnect_ai")

app = FastAPI(
    title="MediConnect AI Services",
    description="Python AI Microservice: Text Vector Embeddings and LangChain Clinical Triage Agent for MediConnect",
    version="1.0.0"
)

# Enable CORS for Spring Boot backend and web clients
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ==========================================
# Pydantic Schemas
# ==========================================

class EmbeddingRequest(BaseModel):
    text: Optional[str] = Field(None, description="Single text string to generate vector embedding for", example="Cardiology specialist treating hypertension and heart disease")
    texts: Optional[List[str]] = Field(None, description="Optional list of texts for batch embedding generation")

class EmbeddingResponse(BaseModel):
    model: str
    dimension: int
    embedding: Optional[List[float]] = None
    embeddings: Optional[List[List[float]]] = None

class AgentQueryRequest(BaseModel):
    query: str = Field(..., description="Patient symptoms or clinical inquiry", example="I have persistent chest tightness, heart palpitations and shortness of breath")
    doctors: Optional[List[Dict[str, Any]]] = Field(None, description="Optional candidate doctors retrieved from PostgreSQL pgvector to evaluate")

class AgentQueryResponse(BaseModel):
    query: str
    recommended_specialty: str
    inferred_specialties: List[str]
    query_embedding: List[float]
    candidate_doctors: List[Dict[str, Any]]
    reasoning: str
    disclaimer: str

# ==========================================
# Endpoints
# ==========================================

@app.get("/", tags=["Health"])
def root():
    return {
        "service": "MediConnect AI Services",
        "status": "UP",
        "version": "1.0.0",
        "endpoints": [
            "POST /api/v1/embeddings (or /embed)",
            "POST /api/v1/agent/query",
            "GET /health",
            "GET /docs"
        ]
    }

@app.get("/health", tags=["Health"])
def health():
    return {"status": "healthy"}

# ------------------------------------------
# Endpoint 1: Vector Embedding Endpoint
# ------------------------------------------

@app.post(
    "/api/v1/embeddings",
    response_model=EmbeddingResponse,
    tags=["Embeddings"],
    summary="Generate vector embeddings for input text string(s)",
    description="Takes a string (e.g. doctor bio, symptoms, or user profile) and returns a 384-dimensional dense vector for PostgreSQL pgvector."
)
@app.post(
    "/embed",
    response_model=EmbeddingResponse,
    tags=["Embeddings"],
    include_in_schema=False
)
def create_embedding(request: EmbeddingRequest):
    if not request.text and not request.texts:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Either 'text' or 'texts' field must be provided."
        )
    
    try:
        if request.text:
            emb = generate_embedding(request.text)
            return EmbeddingResponse(
                model=EMBEDDING_MODEL_NAME,
                dimension=len(emb),
                embedding=emb
            )
        else:
            embs = generate_embeddings(request.texts)
            return EmbeddingResponse(
                model=EMBEDDING_MODEL_NAME,
                dimension=len(embs[0]) if embs else EMBEDDING_DIM,
                embeddings=embs
            )
    except Exception as e:
        logger.error(f"Error generating embedding: {e}")
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e))

# ------------------------------------------
# Endpoint 2: LangChain AI Agent Endpoint
# ------------------------------------------

@app.post(
    "/api/v1/agent/query",
    response_model=AgentQueryResponse,
    tags=["AI Agent"],
    summary="LangChain AI Agent for patient symptom triage and vector query generation",
    description="Analyzes patient symptoms, determines recommended medical specialties, generates query vector for pgvector search, and synthesizes clinical reasoning."
)
def query_agent(request: AgentQueryRequest):
    if not request.query or not request.query.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="The 'query' field cannot be empty."
        )
    
    try:
        response = doctor_agent.run(
            query=request.query,
            candidate_doctors=request.doctors
        )
        return response
    except Exception as e:
        logger.error(f"Error in LangChain agent query: {e}")
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
