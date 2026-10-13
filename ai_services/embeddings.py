from typing import List
from fastembed import TextEmbedding
import logging

logger = logging.getLogger(__name__)

EMBEDDING_MODEL_NAME = "BAAI/bge-small-en-v1.5"
EMBEDDING_DIM = 384

_model = None

def get_embedding_model() -> TextEmbedding:
    """Lazy-load the FastEmbed embedding model."""
    global _model
    if _model is None:
        logger.info(f"Loading FastEmbed model: {EMBEDDING_MODEL_NAME}")
        _model = TextEmbedding(model_name=EMBEDDING_MODEL_NAME)
    return _model

def generate_embedding(text: str) -> List[float]:
    """
    Generate dense vector embedding for a single text string.
    Returns a list of 384 float values.
    """
    model = get_embedding_model()
    embeddings = list(model.embed([text]))
    return [round(float(x), 6) for x in embeddings[0]]

def generate_embeddings(texts: List[str]) -> List[List[float]]:
    """
    Generate dense vector embeddings for a list of text strings.
    Returns a list of 384-dimensional float vectors.
    """
    model = get_embedding_model()
    embeddings = list(model.embed(texts))
    return [[round(float(x), 6) for x in emb] for emb in embeddings]
