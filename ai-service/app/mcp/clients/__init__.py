from app.mcp.clients.base_client import BaseClient

class AuthClient(BaseClient):
    pass

class UserClient(BaseClient):
    pass

class CareerClient(BaseClient):
    pass

class PaymentClient(BaseClient):
    pass

__all__ = ["BaseClient", "AuthClient", "UserClient", "CareerClient", "PaymentClient"]
