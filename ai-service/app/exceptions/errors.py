class AIPlatformException(Exception):
    """Base exception for all AI platform errors."""
    def __init__(self, message: str, status_code: int = 500, error_code: str = "INTERNAL_SERVER_ERROR"):
        super().__init__(message)
        self.message = message
        self.status_code = status_code
        self.error_code = error_code

class ConfigurationError(AIPlatformException):
    """Raised when application configuration is invalid or missing."""
    def __init__(self, message: str):
        super().__init__(message, status_code=500, error_code="CONFIGURATION_ERROR")

class ProviderTimeoutError(AIPlatformException):
    """Raised when an external AI provider times out."""
    def __init__(self, provider: str):
        super().__init__(f"Provider timeout: {provider}", status_code=504, error_code="PROVIDER_TIMEOUT")

class ProviderAPIError(AIPlatformException):
    """Raised when an external AI provider returns an error."""
    def __init__(self, provider: str, details: str):
        super().__init__(f"Provider API Error ({provider}): {details}", status_code=502, error_code="PROVIDER_API_ERROR")
