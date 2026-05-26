from fastapi import APIRouter
from fastapi.responses import JSONResponse

from app.core.exceptions import AiServiceError
from app.schemas.flashcards import GenerateFlashcardsRequest, GenerateFlashcardsResponse
from app.schemas.roadmap import ErrorResponse
from app.services.flashcard_generation_service import FlashcardGenerationService


router = APIRouter(prefix="/api/ai", tags=["flashcards"])


@router.post(
    "/generate-flashcards",
    response_model=GenerateFlashcardsResponse,
    responses={
        502: {"model": ErrorResponse},
        503: {"model": ErrorResponse},
        504: {"model": ErrorResponse},
    },
)
async def generate_flashcards(request: GenerateFlashcardsRequest) -> GenerateFlashcardsResponse | JSONResponse:
    service = FlashcardGenerationService()

    try:
        return await service.generate(request)
    except AiServiceError as exc:
        return JSONResponse(
            status_code=exc.status_code,
            content={"code": exc.code, "message": exc.message},
        )
