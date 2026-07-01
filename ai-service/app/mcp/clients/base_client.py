import httpx
from typing import Dict, Any, Optional
from app.logging.logger import get_logger

logger = get_logger(__name__)

class BaseClient:
    """
    Base HTTP client for interacting with internal microservices.
    Handles connection pooling, timeouts, and basic error handling.
    """
    def __init__(self, base_url: str, timeout: float = 10.0):
        self.base_url = base_url.rstrip('/')
        self.timeout = timeout
        # Using a client with connection pooling
        self.client = httpx.AsyncClient(timeout=self.timeout)

    async def close(self):
        await self.client.aclose()

    async def _request(
        self, 
        method: str, 
        endpoint: str, 
        params: Optional[Dict[str, Any]] = None,
        json: Optional[Dict[str, Any]] = None,
        headers: Optional[Dict[str, str]] = None
    ) -> Any:
        url = f"{self.base_url}/{endpoint.lstrip('/')}"
        logger.debug(f"Making {method} request to {url}")
        
        try:
            response = await self.client.request(
                method=method,
                url=url,
                params=params,
                json=json,
                headers=headers
            )
            response.raise_for_status()
            
            # Try to return JSON if available
            if response.headers.get("content-type", "").startswith("application/json"):
                return response.json()
            return response.text
            
        except httpx.HTTPStatusError as e:
            logger.error(f"HTTP error {e.response.status_code} on {method} {url}: {e.response.text}")
            raise Exception(f"Service error (status {e.response.status_code}): {e.response.text}")
        except httpx.RequestError as e:
            logger.error(f"Request error on {method} {url}: {str(e)}")
            raise Exception(f"Failed to communicate with service: {str(e)}")

    async def get(self, endpoint: str, params: Optional[Dict[str, Any]] = None, headers: Optional[Dict[str, str]] = None):
        return await self._request("GET", endpoint, params=params, headers=headers)
        
    async def post(self, endpoint: str, json: Optional[Dict[str, Any]] = None, headers: Optional[Dict[str, str]] = None):
        return await self._request("POST", endpoint, json=json, headers=headers)
