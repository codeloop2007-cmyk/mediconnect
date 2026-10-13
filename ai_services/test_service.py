from fastapi.testclient import TestClient
from main import app
import json

client = TestClient(app)

def test_health():
    res = client.get("/health")
    print("Health response:", res.status_code, res.json())
    assert res.status_code == 200

def test_embeddings_single():
    payload = {"text": "Dr. Sarah Chen, Cardiologist specializing in heart disease and hypertension."}
    res = client.post("/api/v1/embeddings", json=payload)
    print("Single embedding status:", res.status_code)
    data = res.json()
    print("Dimension:", data.get("dimension"))
    print("Embedding sample (first 5 numbers):", data.get("embedding")[:5])
    assert res.status_code == 200
    assert data.get("dimension") == 384
    assert len(data.get("embedding")) == 384

def test_embeddings_batch():
    payload = {"texts": ["Doctor bio 1", "Doctor bio 2"]}
    res = client.post("/api/v1/embeddings", json=payload)
    print("Batch embedding status:", res.status_code)
    data = res.json()
    print("Number of embeddings:", len(data.get("embeddings", [])))
    assert res.status_code == 200
    assert len(data.get("embeddings")) == 2

def test_agent_query():
    payload = {
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
    res = client.post("/api/v1/agent/query", json=payload)
    print("Agent query status:", res.status_code)
    data = res.json()
    print("Recommended specialty:", data.get("recommended_specialty"))
    print("Inferred specialties:", data.get("inferred_specialties"))
    print("Query vector dimension:", len(data.get("query_embedding", [])))
    print("Reasoning:", data.get("reasoning"))
    print("Disclaimer:", data.get("disclaimer")[:80] + "...")
    assert res.status_code == 200
    assert data.get("recommended_specialty") == "Cardiology"
    assert len(data.get("query_embedding")) == 384

if __name__ == "__main__":
    print("=== RUNNING FASTAPI ENDPOINT TESTS ===")
    test_health()
    test_embeddings_single()
    test_embeddings_batch()
    test_agent_query()
    print("=== ALL TESTS PASSED SUCCESSFULLY! ===")

