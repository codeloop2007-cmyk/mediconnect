from typing import Dict, Any, List, Optional
from langchain_core.tools import tool
from langchain_core.prompts import PromptTemplate
from embeddings import generate_embedding
import logging

logger = logging.getLogger(__name__)

# Medical specialty indicator mappings for clinical triage
SPECIALTY_INDICATORS = {
    "Cardiology": ["chest pain", "heart", "palpitations", "shortness of breath", "hypertension", "angina", "irregular heartbeat", "cardiac", "blood pressure"],
    "Dermatology": ["skin", "rash", "acne", "eczema", "itching", "mole", "hives", "psoriasis", "dermatitis", "dry skin", "blisters"],
    "Neurology": ["headache", "migraine", "seizures", "numbness", "tingling", "dizziness", "vertigo", "nerve pain", "tremor", "memory loss"],
    "Orthopedics": ["joint pain", "knee pain", "back pain", "bone", "fracture", "arthritis", "shoulder pain", "spine", "stiffness", "sprain", "ligament"],
    "Pediatrics": ["child", "baby", "infant", "toddler", "pediatric", "vaccination", "growth milestones"],
    "Infectious Disease": ["fever", "infection", "malaria", "dengue", "viral", "bacterial", "unexplained fever", "chronic fatigue", "sepsis"],
    "Gastroenterology": ["stomach pain", "abdominal", "acid reflux", "gerd", "heartburn", "constipation", "diarrhea", "bloating", "digestion", "nausea"],
    "Pulmonology": ["cough", "breathing difficulty", "asthma", "wheezing", "bronchitis", "lungs", "copd", "respiratory"],
    "Psychiatry": ["anxiety", "depression", "panic attacks", "insomnia", "sleep disturbance", "stress", "mood swings", "bipolar"],
    "General Medicine": ["fever", "cold", "flu", "fatigue", "general malaise", "weakness", "body ache"]
}

@tool
def embed_symptoms_tool(symptoms: str) -> List[float]:
    """
    Generate dense vector embedding for patient symptoms using the 384-dimensional model.
    Use this embedding to perform pgvector cosine distance queries in PostgreSQL.
    """
    return generate_embedding(symptoms)

class LangChainDoctorAgent:
    """
    LangChain AI Agent for Online Consultation Triage.
    Performs clinical symptom analysis, infers specialty, embeds queries for pgvector,
    and formats physician recommendations.
    """

    def __init__(self):
        self.tools = [embed_symptoms_tool]

    def infer_specialties(self, query: str) -> List[str]:
        """Analyze reported symptoms to determine likely medical specialties."""
        q_lower = query.lower()
        matched = []
        for specialty, keywords in SPECIALTY_INDICATORS.items():
            if any(k in q_lower for k in keywords):
                matched.append(specialty)
        if not matched:
            matched.append("General Medicine")
        return matched

    def generate_clinical_reasoning(
        self,
        query: str,
        specialties: List[str],
        candidate_doctors: Optional[List[Dict[str, Any]]] = None
    ) -> str:
        """Synthesize clinical rationale for the recommended doctors."""
        spec_str = ", ".join(specialties)
        if candidate_doctors and len(candidate_doctors) > 0:
            top_doc = candidate_doctors[0]
            name = top_doc.get("name", "the specialist")
            doc_spec = top_doc.get("specialization", spec_str)
            return (
                f"Based on your reported symptoms ('{query}'), our LangChain clinical triage identified "
                f"{spec_str} as the primary recommended medical specialty. {name} ({doc_spec}) is recommended "
                f"for a comprehensive consultation."
            )
        return (
            f"Based on your reported symptoms ('{query}'), our LangChain clinical triage identified "
            f"{spec_str} as the primary recommended medical specialty. Use the provided query embedding "
            f"to retrieve matching doctors from your PostgreSQL pgvector database."
        )

    def run(
        self,
        query: str,
        candidate_doctors: Optional[List[Dict[str, Any]]] = None
    ) -> Dict[str, Any]:
        """
        Execute LangChain Agent workflow:
        1. Infer clinical specialties from query.
        2. Generate 384-d vector embedding via embed_symptoms_tool.
        3. Formulate clinical triage reasoning and doctor recommendations.
        4. Add medical safety disclaimer.
        """
        logger.info(f"LangChain Agent processing query: '{query}'")
        
        # 1. Infer specialty
        inferred_specialties = self.infer_specialties(query)
        primary_specialty = inferred_specialties[0]

        # 2. Generate vector embedding
        query_vector = embed_symptoms_tool.invoke({"symptoms": query})

        # 3. Formulate reasoning
        reasoning = self.generate_clinical_reasoning(query, inferred_specialties, candidate_doctors)

        # 4. Emergency / safety check
        emergency_indicators = ["emergency", "severe chest pain", "cannot breathe", "unconscious", "stroke symptoms", "heavy bleeding"]
        is_emergency = any(k in query.lower() for k in emergency_indicators)
        
        disclaimer = (
            "Medical Disclaimer: MediConnect AI provides informational recommendations based on symptom matching. "
            "It does not replace professional medical judgment. "
        )
        if is_emergency:
            disclaimer += "WARNING: Your symptoms suggest a potential emergency. Please contact emergency services (e.g. 911 or local emergency unit) immediately."
        else:
            disclaimer += "If you experience severe or worsening symptoms, consult an emergency healthcare provider."

        return {
            "query": query,
            "recommended_specialty": primary_specialty,
            "inferred_specialties": inferred_specialties,
            "query_embedding": query_vector,
            "candidate_doctors": candidate_doctors or [],
            "reasoning": reasoning,
            "disclaimer": disclaimer
        }

# Global agent singleton
doctor_agent = LangChainDoctorAgent()
