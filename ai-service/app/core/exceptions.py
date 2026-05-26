class AiServiceError(Exception):
    code = "AI_SERVICE_ERROR"
    status_code = 500

    def __init__(self, message: str):
        super().__init__(message)
        self.message = message


class AiUnavailableError(AiServiceError):
    code = "AI_UNAVAILABLE"
    status_code = 503


class AiTimeoutError(AiServiceError):
    code = "AI_TIMEOUT"
    status_code = 504


class InvalidAiResponseError(AiServiceError):
    code = "INVALID_AI_RESPONSE"
    status_code = 502


class EmptyAiResultError(AiServiceError):
    code = "EMPTY_AI_RESULT"
    status_code = 502
