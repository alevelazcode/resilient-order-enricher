// Package main wires the Echo HTTP server for the enrichment API: customer + product
// CRUD endpoints backed by in-memory repositories, with structured slog JSON output
// and graceful shutdown on SIGINT / SIGTERM.
package main

import (
	"context"
	"errors"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"enricher-api-go/internal/customer"
	"enricher-api-go/internal/product"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
)

const (
	defaultPort     = ":8080"
	shutdownTimeout = 10 * time.Second
)

func main() {
	configureLogging()

	e := echo.New()
	e.HideBanner = true
	e.HidePort = true

	e.Use(middleware.Recover())
	e.Use(middleware.CORS())
	e.Use(middleware.RequestID())

	customerRepo := customer.NewInMemoryRepository()
	productRepo := product.NewInMemoryRepository()

	customerHandler := customer.NewHandler(customer.NewService(customerRepo))
	productHandler := product.NewHandler(product.NewService(productRepo))

	registerRoutes(e, customerHandler, productHandler)

	go func() {
		slog.Info("server starting", "addr", defaultPort)
		if err := e.Start(defaultPort); err != nil && !errors.Is(err, http.ErrServerClosed) {
			slog.Error("server failed", "error", err)
			os.Exit(1)
		}
	}()

	awaitShutdown(e)
}

func configureLogging() {
	level := slog.LevelInfo
	if v := os.Getenv("LOG_LEVEL"); v == "debug" || v == "DEBUG" {
		level = slog.LevelDebug
	}
	handler := slog.NewJSONHandler(os.Stdout, &slog.HandlerOptions{Level: level})
	slog.SetDefault(slog.New(handler).With("service", "enricher-api-go"))
}

func registerRoutes(e *echo.Echo, c *customer.Handler, p *product.Handler) {
	e.GET("/health", func(ctx echo.Context) error {
		return ctx.JSON(http.StatusOK, map[string]string{
			"status":  "healthy",
			"service": "enricher-api-go",
		})
	})

	customers := e.Group("/v1/customers")
	customers.GET("", c.ListCustomers)
	customers.POST("", c.CreateCustomer)
	customers.GET("/:id", c.GetCustomer)
	customers.PUT("/:id", c.UpdateCustomer)
	customers.DELETE("/:id", c.DeleteCustomer)
	customers.GET("/:id/status", c.CheckCustomerStatus)

	products := e.Group("/v1/products")
	products.GET("", p.ListProducts)
	products.POST("", p.CreateProduct)
	products.GET("/:id", p.GetProduct)
	products.PUT("/:id", p.UpdateProduct)
	products.DELETE("/:id", p.DeleteProduct)
	products.GET("/:id/availability", p.CheckProductAvailability)
}

func awaitShutdown(e *echo.Echo) {
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	slog.Info("shutdown signal received, draining connections")

	ctx, cancel := context.WithTimeout(context.Background(), shutdownTimeout)
	defer cancel()

	if err := e.Shutdown(ctx); err != nil {
		slog.Error("graceful shutdown failed", "error", err)
		os.Exit(1)
	}
	slog.Info("server stopped")
}
