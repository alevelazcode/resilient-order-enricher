// Package customer provides HTTP handlers for customer-related operations
// in the Resilient Order Enricher API.
//
// This package contains the HTTP layer for customer operations, including
// request/response handling, validation, and error management.
package customer

import (
	"net/http"

	"github.com/labstack/echo/v4"
)

// Handler handles HTTP requests for customer operations.
//
// This struct provides HTTP endpoints for customer CRUD operations,
// including GET, POST, PUT, DELETE, and status check operations.
// It integrates with the customer service layer for business logic.
//
// Example usage:
//
//	service := customer.NewService(repo)
//	handler := customer.NewHandler(service)
//	e.GET("/v1/customers/:id", handler.GetCustomer)
type Handler struct {
	service Service
}

// NewHandler creates a new customer handler instance.
//
// This function creates and returns a new Handler with the provided
// service dependency.
//
// Args:
//   - service: Service implementation for business logic
//
// Returns:
//   - *Handler: new customer handler instance
//
// Example usage:
//
//	service := customer.NewService(repo)
//	handler := customer.NewHandler(service)
func NewHandler(service Service) *Handler {
	return &Handler{
		service: service,
	}
}

// GetCustomer handles GET /v1/customers/:id requests.
//
// This method retrieves a customer by their unique identifier and returns
// the customer information in JSON format. It includes comprehensive error
// handling for various scenarios.
//
// Args:
//   - c: Echo context containing the HTTP request and response
//
// Returns:
//   - error: error if the operation fails
//
// Example request:
//
//	GET /v1/customers/customer-12345
//
// Example response:
//
//	{
//		"customerId": "customer-12345",
//		"name": "John Doe",
//		"status": "ACTIVE"
//	}
//
// Error responses:
//   - 404: Customer not found
//   - 500: Internal server error
func (h *Handler) GetCustomer(c echo.Context) error {
	customerID := c.Param("id")

	customer, err := h.service.GetCustomer(customerID)
	if err != nil {
		if err == ErrCustomerNotFound || err.Error() == "failed to get customer: customer not found" {
			return c.JSON(http.StatusNotFound, map[string]string{
				"error": "Customer not found",
			})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error": err.Error(),
		})
	}

	return c.JSON(http.StatusOK, customer.ToResponse())
}

// CreateCustomer handles POST /v1/customers requests.
//
// This method creates a new customer with the provided information and returns
// the created customer in JSON format. It validates the request body and
// handles various error scenarios.
//
// Args:
//   - c: Echo context containing the HTTP request and response
//
// Returns:
//   - error: error if the operation fails
//
// Example request:
//
//	POST /v1/customers
//	Content-Type: application/json
//
//	{
//		"name": "Jane Smith",
//		"status": "ACTIVE"
//	}
//
// Example response:
//
//	{
//		"customerId": "customer-67890",
//		"name": "Jane Smith",
//		"status": "ACTIVE"
//	}
//
// Error responses:
//   - 400: Invalid request body or validation error
//   - 500: Internal server error
func (h *Handler) CreateCustomer(c echo.Context) error {
	var req CustomerRequest
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": "Invalid request body",
		})
	}

	customer, err := h.service.CreateCustomer(req)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": err.Error(),
		})
	}

	return c.JSON(http.StatusCreated, customer.ToResponse())
}

// UpdateCustomer handles PUT /v1/customers/:id requests.
//
// This method updates an existing customer's information and returns
// the updated customer in JSON format. It validates the request body
// and handles various error scenarios including customer not found.
//
// Args:
//   - c: Echo context containing the HTTP request and response
//
// Returns:
//   - error: error if the operation fails
//
// Example request:
//
//	PUT /v1/customers/customer-12345
//	Content-Type: application/json
//
//	{
//		"name": "John Doe Updated",
//		"status": "INACTIVE"
//	}
//
// Example response:
//
//	{
//		"customerId": "customer-12345",
//		"name": "John Doe Updated",
//		"status": "INACTIVE"
//	}
//
// Error responses:
//   - 400: Invalid request body or validation error
//   - 404: Customer not found
//   - 500: Internal server error
func (h *Handler) UpdateCustomer(c echo.Context) error {
	customerID := c.Param("id")

	var req CustomerRequest
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": "Invalid request body",
		})
	}

	customer, err := h.service.UpdateCustomer(customerID, req)
	if err != nil {
		if err == ErrCustomerNotFound {
			return c.JSON(http.StatusNotFound, map[string]string{
				"error": "Customer not found",
			})
		}
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": err.Error(),
		})
	}

	return c.JSON(http.StatusOK, customer.ToResponse())
}

// DeleteCustomer handles DELETE /v1/customers/:id requests.
//
// This method removes a customer from the system and returns a success
// response. It handles various error scenarios including customer not found.
//
// Args:
//   - c: Echo context containing the HTTP request and response
//
// Returns:
//   - error: error if the operation fails
//
// Example request:
//
//	DELETE /v1/customers/customer-12345
//
// Example response:
//
//	{
//		"message": "Customer deleted successfully"
//	}
//
// Error responses:
//   - 404: Customer not found
//   - 500: Internal server error
func (h *Handler) DeleteCustomer(c echo.Context) error {
	customerID := c.Param("id")

	err := h.service.DeleteCustomer(customerID)
	if err != nil {
		if err == ErrCustomerNotFound {
			return c.JSON(http.StatusNotFound, map[string]string{
				"error": "Customer not found",
			})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error": err.Error(),
		})
	}

	return c.NoContent(http.StatusNoContent)
}

// ListCustomers handles GET /v1/customers
func (h *Handler) ListCustomers(c echo.Context) error {
	customers, err := h.service.ListCustomers()
	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error": err.Error(),
		})
	}

	responses := make([]CustomerResponse, len(customers))
	for i, customer := range customers {
		responses[i] = customer.ToResponse()
	}

	return c.JSON(http.StatusOK, map[string]interface{}{
		"customers": responses,
		"count":     len(responses),
	})
}

// CheckCustomerStatus handles GET /v1/customers/:id/status
func (h *Handler) CheckCustomerStatus(c echo.Context) error {
	customerID := c.Param("id")

	isActive, err := h.service.IsCustomerActive(customerID)
	if err != nil {
		if err == ErrCustomerNotFound {
			return c.JSON(http.StatusNotFound, map[string]string{
				"error": "Customer not found",
			})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error": err.Error(),
		})
	}

	status := "INACTIVE"
	if isActive {
		status = "ACTIVE"
	}

	return c.JSON(http.StatusOK, map[string]interface{}{
		"customerId": customerID,
		"status":     status,
		"isActive":   isActive,
	})
}
