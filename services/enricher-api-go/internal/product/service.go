package product

import (
	"fmt"
	"log"
)

// Service defines the business logic interface for products
type Service interface {
	GetProduct(productID string) (*Product, error)
	CreateProduct(req ProductRequest) (*Product, error)
	UpdateProduct(productID string, req ProductRequest) (*Product, error)
	DeleteProduct(productID string) error
	ListProducts() ([]*Product, error)
	GetProductsByCategory(category string) ([]*Product, error)
	IsProductAvailable(productID string) (bool, error)
}

// ProductService implements the Service interface
type ProductService struct {
	repo Repository
}

// NewService creates a new product service
func NewService(repo Repository) *ProductService {
	return &ProductService{
		repo: repo,
	}
}

// GetProduct retrieves a product by ID
func (s *ProductService) GetProduct(productID string) (*Product, error) {
	log.Printf("Getting product with ID: %s", productID)

	if productID == "" {
		return nil, fmt.Errorf("product ID cannot be empty")
	}

	product, err := s.repo.GetByID(productID)
	if err != nil {
		log.Printf("Error getting product %s: %v", productID, err)
		return nil, fmt.Errorf("failed to get product: %w", err)
	}

	log.Printf("Successfully retrieved product: %s", product.Name)
	return product, nil
}

// CreateProduct creates a new product
func (s *ProductService) CreateProduct(req ProductRequest) (*Product, error) {
	log.Printf("Creating new product: %s", req.Name)

	if err := s.validateProductRequest(req); err != nil {
		return nil, fmt.Errorf("validation failed: %w", err)
	}

	// Generate a simple ID (in production, use UUID)
	productID := fmt.Sprintf("product-%d", len(req.Name)*100+int(req.Price))

	product := &Product{
		ProductID:   productID,
		Name:        req.Name,
		Description: req.Description,
		Price:       req.Price,
		Category:    req.Category,
		InStock:     req.InStock,
	}

	if err := s.repo.Create(product); err != nil {
		log.Printf("Error creating product: %v", err)
		return nil, fmt.Errorf("failed to create product: %w", err)
	}

	log.Printf("Successfully created product with ID: %s", productID)
	return product, nil
}

// UpdateProduct updates an existing product
func (s *ProductService) UpdateProduct(productID string, req ProductRequest) (*Product, error) {
	log.Printf("Updating product with ID: %s", productID)

	if productID == "" {
		return nil, fmt.Errorf("product ID cannot be empty")
	}

	if err := s.validateProductRequest(req); err != nil {
		return nil, fmt.Errorf("validation failed: %w", err)
	}

	// Check if product exists
	existingProduct, err := s.repo.GetByID(productID)
	if err != nil {
		return nil, fmt.Errorf("product not found: %w", err)
	}

	// Update product fields
	existingProduct.Name = req.Name
	existingProduct.Description = req.Description
	existingProduct.Price = req.Price
	existingProduct.Category = req.Category
	existingProduct.InStock = req.InStock

	if err := s.repo.Update(existingProduct); err != nil {
		log.Printf("Error updating product: %v", err)
		return nil, fmt.Errorf("failed to update product: %w", err)
	}

	log.Printf("Successfully updated product: %s", productID)
	return existingProduct, nil
}

// DeleteProduct removes a product
func (s *ProductService) DeleteProduct(productID string) error {
	log.Printf("Deleting product with ID: %s", productID)

	if productID == "" {
		return fmt.Errorf("product ID cannot be empty")
	}

	if err := s.repo.Delete(productID); err != nil {
		log.Printf("Error deleting product: %v", err)
		return fmt.Errorf("failed to delete product: %w", err)
	}

	log.Printf("Successfully deleted product: %s", productID)
	return nil
}

// ListProducts returns all products
func (s *ProductService) ListProducts() ([]*Product, error) {
	log.Println("Listing all products")

	products, err := s.repo.List()
	if err != nil {
		log.Printf("Error listing products: %v", err)
		return nil, fmt.Errorf("failed to list products: %w", err)
	}

	log.Printf("Successfully retrieved %d products", len(products))
	return products, nil
}

// GetProductsByCategory returns products filtered by category
func (s *ProductService) GetProductsByCategory(category string) ([]*Product, error) {
	log.Printf("Getting products by category: %s", category)

	if category == "" {
		return nil, fmt.Errorf("category cannot be empty")
	}

	products, err := s.repo.GetByCategory(category)
	if err != nil {
		log.Printf("Error getting products by category: %v", err)
		return nil, fmt.Errorf("failed to get products by category: %w", err)
	}

	log.Printf("Successfully retrieved %d products for category: %s", len(products), category)
	return products, nil
}

// IsProductAvailable checks if a product is available
func (s *ProductService) IsProductAvailable(productID string) (bool, error) {
	product, err := s.GetProduct(productID)
	if err != nil {
		return false, err
	}

	return product.IsValid(), nil
}

// validateProductRequest validates the product request
func (s *ProductService) validateProductRequest(req ProductRequest) error {
	if req.Name == "" {
		return fmt.Errorf("product name is required")
	}

	if len(req.Name) < 2 {
		return fmt.Errorf("product name must be at least 2 characters")
	}

	if len(req.Name) > 100 {
		return fmt.Errorf("product name must be at most 100 characters")
	}

	if req.Description == "" {
		return fmt.Errorf("product description is required")
	}

	if len(req.Description) < 10 {
		return fmt.Errorf("product description must be at least 10 characters")
	}

	if len(req.Description) > 500 {
		return fmt.Errorf("product description must be at most 500 characters")
	}

	if req.Price <= 0 {
		return fmt.Errorf("product price must be greater than 0")
	}

	if req.Category == "" {
		return fmt.Errorf("product category is required")
	}

	if len(req.Category) < 2 {
		return fmt.Errorf("product category must be at least 2 characters")
	}

	if len(req.Category) > 50 {
		return fmt.Errorf("product category must be at most 50 characters")
	}

	return nil
}
