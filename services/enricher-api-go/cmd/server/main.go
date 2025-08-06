package main

import (
	"log"

	"enricher-api-go/internal/customer"
	"enricher-api-go/internal/product"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
)

func main() {
	// Initialize Echo
	e := echo.New()

	// Middleware
	e.Use(middleware.Logger())
	e.Use(middleware.Recover())
	e.Use(middleware.CORS())

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
	customerGroup.POST("", customerHandler.CreateCustomer)
	customerGroup.GET("/:id", customerHandler.GetCustomer)
	customerGroup.PUT("/:id", customerHandler.UpdateCustomer)
	customerGroup.DELETE("/:id", customerHandler.DeleteCustomer)
	customerGroup.GET("/:id/status", customerHandler.CheckCustomerStatus)

	// Product routes
	productGroup := e.Group("/v1/products")
	productGroup.GET("", productHandler.ListProducts)
	productGroup.POST("", productHandler.CreateProduct)
	productGroup.GET("/:id", productHandler.GetProduct)
	productGroup.PUT("/:id", productHandler.UpdateProduct)
	productGroup.DELETE("/:id", productHandler.DeleteProduct)
	productGroup.GET("/:id/availability", productHandler.CheckProductAvailability)

	// Start server
	log.Println("Starting Enricher API server on :8080")
	e.Logger.Fatal(e.Start(":8080"))
}
