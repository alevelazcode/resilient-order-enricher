package main

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"enricher-api-go/internal/customer"
	"enricher-api-go/internal/product"

	"github.com/labstack/echo/v4"
	"github.com/stretchr/testify/assert"
)

func setupTestApp() *echo.Echo {
	e := echo.New()

	// Initialize repositories
	customerRepo := customer.NewInMemoryRepository()
	productRepo := product.NewInMemoryRepository()

	// Initialize services
	customerService := customer.NewService(customerRepo)
	productService := product.NewService(productRepo)

	// Initialize handlers
	customerHandler := customer.NewHandler(customerService)
	productHandler := product.NewHandler(productService)

	// Health check endpoint
	e.GET("/health", func(c echo.Context) error {
		return c.JSON(200, map[string]string{
			"status":  "healthy",
			"service": "enricher-api-go",
		})
	})

	// Customer routes
	customerGroup := e.Group("/v1/customers")
	customerGroup.GET("", customerHandler.ListCustomers)
	customerGroup.GET("/:id", customerHandler.GetCustomer)

	// Product routes
	productGroup := e.Group("/v1/products")
	productGroup.GET("", productHandler.ListProducts)
	productGroup.GET("/:id", productHandler.GetProduct)

	return e
}

func TestHealthEndpoint(t *testing.T) {
	// Arrange
	e := setupTestApp()
	req := httptest.NewRequest(http.MethodGet, "/health", nil)
	rec := httptest.NewRecorder()

	// Act
	e.ServeHTTP(rec, req)

	// Assert
	assert.Equal(t, http.StatusOK, rec.Code)

	var response map[string]string
	err := json.Unmarshal(rec.Body.Bytes(), &response)
	assert.NoError(t, err)
	assert.Equal(t, "healthy", response["status"])
	assert.Equal(t, "enricher-api-go", response["service"])
}

func TestGetCustomerEndpoint(t *testing.T) {
	// Arrange
	e := setupTestApp()
	req := httptest.NewRequest(http.MethodGet, "/v1/customers/customer-456", nil)
	rec := httptest.NewRecorder()

	// Act
	e.ServeHTTP(rec, req)

	// Assert
	assert.Equal(t, http.StatusOK, rec.Code)

	var response customer.CustomerResponse
	err := json.Unmarshal(rec.Body.Bytes(), &response)
	assert.NoError(t, err)
	assert.Equal(t, "customer-456", response.CustomerID)
	assert.Equal(t, "Jane Doe", response.Name)
	assert.Equal(t, "ACTIVE", response.Status)
}

func TestGetCustomerEndpoint_NotFound(t *testing.T) {
	// Arrange
	e := setupTestApp()
	req := httptest.NewRequest(http.MethodGet, "/v1/customers/non-existent", nil)
	rec := httptest.NewRecorder()

	// Act
	e.ServeHTTP(rec, req)

	// Assert
	assert.Equal(t, http.StatusNotFound, rec.Code)

	var response map[string]string
	err := json.Unmarshal(rec.Body.Bytes(), &response)
	assert.NoError(t, err)
	assert.Equal(t, "Customer not found", response["error"])
}

func TestGetProductEndpoint(t *testing.T) {
	// Arrange
	e := setupTestApp()
	req := httptest.NewRequest(http.MethodGet, "/v1/products/product-789", nil)
	rec := httptest.NewRecorder()

	// Act
	e.ServeHTTP(rec, req)

	// Assert
	assert.Equal(t, http.StatusOK, rec.Code)

	var response product.ProductResponse
	err := json.Unmarshal(rec.Body.Bytes(), &response)
	assert.NoError(t, err)
	assert.Equal(t, "product-789", response.ProductID)
	assert.Equal(t, "Laptop", response.Name)
	assert.Equal(t, 999.00, response.Price)
}

func TestGetProductEndpoint_NotFound(t *testing.T) {
	// Arrange
	e := setupTestApp()
	req := httptest.NewRequest(http.MethodGet, "/v1/products/non-existent", nil)
	rec := httptest.NewRecorder()

	// Act
	e.ServeHTTP(rec, req)

	// Assert
	assert.Equal(t, http.StatusNotFound, rec.Code)

	var response map[string]string
	err := json.Unmarshal(rec.Body.Bytes(), &response)
	assert.NoError(t, err)
	assert.Equal(t, "Product not found", response["error"])
}

func TestListCustomersEndpoint(t *testing.T) {
	// Arrange
	e := setupTestApp()
	req := httptest.NewRequest(http.MethodGet, "/v1/customers", nil)
	rec := httptest.NewRecorder()

	// Act
	e.ServeHTTP(rec, req)

	// Assert
	assert.Equal(t, http.StatusOK, rec.Code)

	var response map[string]interface{}
	err := json.Unmarshal(rec.Body.Bytes(), &response)
	assert.NoError(t, err)

	_, exists := response["customers"]
	assert.True(t, exists)

	count, exists := response["count"]
	assert.True(t, exists)
	assert.Equal(t, float64(5), count) // Should match sample data count
}

func TestListProductsEndpoint(t *testing.T) {
	// Arrange
	e := setupTestApp()
	req := httptest.NewRequest(http.MethodGet, "/v1/products", nil)
	rec := httptest.NewRecorder()

	// Act
	e.ServeHTTP(rec, req)

	// Assert
	assert.Equal(t, http.StatusOK, rec.Code)

	var response map[string]interface{}
	err := json.Unmarshal(rec.Body.Bytes(), &response)
	assert.NoError(t, err)

	_, exists := response["products"]
	assert.True(t, exists)

	count, exists := response["count"]
	assert.True(t, exists)
	assert.Equal(t, float64(5), count) // Should match sample data count
}
