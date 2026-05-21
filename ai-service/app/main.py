from fastapi import FastAPI

from app.api.health import router as health_router
from app.api.roadmap import router as roadmap_router


app = FastAPI(title="AI Learning Platform AI Service")

app.include_router(health_router)
app.include_router(roadmap_router)
