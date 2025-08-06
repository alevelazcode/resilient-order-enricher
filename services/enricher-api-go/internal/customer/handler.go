package customer

import (
	"net/http"

	"github.com/labstack/echo/v4"
)

// Handler handles HTTP requests for customers
type Handler struct {
	service Service
}

// NewHandler creates a new customer handler
func NewHandler(service Service) *Handler {
	return &Handler{
		service: service,
	}
}

// GetCustomer handles GET /v1/customers/:id
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

// CreateCustomer handles POST /v1/customers
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

// UpdateCustomer handles PUT /v1/customers/:id
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

// DeleteCustomer handles DELETE /v1/customers/:id
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
