package product

import (
	"net/http"

	"github.com/labstack/echo/v4"
)

// Handler handles HTTP requests for products
type Handler struct {
	service Service
}

// NewHandler creates a new product handler
func NewHandler(service Service) *Handler {
	return &Handler{
		service: service,
	}
}

// GetProduct handles GET /v1/products/:id
func (h *Handler) GetProduct(c echo.Context) error {
	productID := c.Param("id")

	product, err := h.service.GetProduct(productID)
	if err != nil {
		if err == ErrProductNotFound || err.Error() == "failed to get product: product not found" {
			return c.JSON(http.StatusNotFound, map[string]string{
				"error": "Product not found",
			})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error": err.Error(),
		})
	}

	return c.JSON(http.StatusOK, product.ToResponse())
}

// CreateProduct handles POST /v1/products
func (h *Handler) CreateProduct(c echo.Context) error {
	var req ProductRequest
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": "Invalid request body",
		})
	}

	product, err := h.service.CreateProduct(req)
	if err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": err.Error(),
		})
	}

	return c.JSON(http.StatusCreated, product.ToResponse())
}

// UpdateProduct handles PUT /v1/products/:id
func (h *Handler) UpdateProduct(c echo.Context) error {
	productID := c.Param("id")

	var req ProductRequest
	if err := c.Bind(&req); err != nil {
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": "Invalid request body",
		})
	}

	product, err := h.service.UpdateProduct(productID, req)
	if err != nil {
		if err == ErrProductNotFound {
			return c.JSON(http.StatusNotFound, map[string]string{
				"error": "Product not found",
			})
		}
		return c.JSON(http.StatusBadRequest, map[string]string{
			"error": err.Error(),
		})
	}

	return c.JSON(http.StatusOK, product.ToResponse())
}

// DeleteProduct handles DELETE /v1/products/:id
func (h *Handler) DeleteProduct(c echo.Context) error {
	productID := c.Param("id")

	err := h.service.DeleteProduct(productID)
	if err != nil {
		if err == ErrProductNotFound {
			return c.JSON(http.StatusNotFound, map[string]string{
				"error": "Product not found",
			})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error": err.Error(),
		})
	}

	return c.NoContent(http.StatusNoContent)
}

// ListProducts handles GET /v1/products
func (h *Handler) ListProducts(c echo.Context) error {
	category := c.QueryParam("category")

	var products []*Product
	var err error

	if category != "" {
		products, err = h.service.GetProductsByCategory(category)
	} else {
		products, err = h.service.ListProducts()
	}

	if err != nil {
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error": err.Error(),
		})
	}

	responses := make([]ProductResponse, len(products))
	for i, product := range products {
		responses[i] = product.ToResponse()
	}

	return c.JSON(http.StatusOK, map[string]interface{}{
		"products": responses,
		"count":    len(responses),
		"category": category,
	})
}

// CheckProductAvailability handles GET /v1/products/:id/availability
func (h *Handler) CheckProductAvailability(c echo.Context) error {
	productID := c.Param("id")

	isAvailable, err := h.service.IsProductAvailable(productID)
	if err != nil {
		if err == ErrProductNotFound {
			return c.JSON(http.StatusNotFound, map[string]string{
				"error": "Product not found",
			})
		}
		return c.JSON(http.StatusInternalServerError, map[string]string{
			"error": err.Error(),
		})
	}

	return c.JSON(http.StatusOK, map[string]interface{}{
		"productId": productID,
		"available": isAvailable,
		"inStock":   isAvailable,
	})
}
