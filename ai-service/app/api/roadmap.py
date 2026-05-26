from fastapi import APIRouter
from fastapi.responses import JSONResponse

from app.core.exceptions import AiServiceError
from app.schemas.roadmap import ErrorResponse, GenerateRoadmapRequest, GenerateRoadmapResponse
from app.services.roadmap_generation_service import RoadmapGenerationService


router = APIRouter(prefix="/api/ai", tags=["roadmap"])


@router.post(
    "/generate-roadmap",
    response_model=GenerateRoadmapResponse,
    responses={
        502: {"model": ErrorResponse},
        503: {"model": ErrorResponse},
        504: {"model": ErrorResponse},
    },
)
async def generate_roadmap(request: GenerateRoadmapRequest) -> GenerateRoadmapResponse | JSONResponse:
    service = RoadmapGenerationService()

    try:
        return await service.generate(request)
    except AiServiceError as exc:
        return JSONResponse(
            status_code=exc.status_code,
            content={"code": exc.code, "message": exc.message},
        )
