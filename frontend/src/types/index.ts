// Shared domain types for the web client.
//
// Mirrors the `ApiResponse<T>` envelope returned by the Spring services (see careeros-common).
// Concrete entity types (User, Resume, Roadmap, …) are added here as endpoints are built.

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  message: string | null;
}

export interface PaginationResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
