// Package product provides product-related business logic and storage abstractions.
package product

import (
	"fmt"
	"log/slog"
)

// Service is the business-logic surface for products.
type Service interface {
	GetProduct(productID string) (*Product, error)
	CreateProduct(req ProductRequest) (*Product, error)
	UpdateProduct(productID string, req ProductRequest) (*Product, error)
	DeleteProduct(productID string) error
	ListProducts() ([]*Product, error)
	GetProductsByCategory(category string) ([]*Product, error)
	IsProductAvailable(productID string) (bool, error)
}

// ProductService implements Service against a Repository.
type ProductService struct {
	repo Repository
	log  *slog.Logger
}

// NewService constructs a ProductService backed by the supplied repository.
func NewService(repo Repository) *ProductService {
	return &ProductService{
		repo: repo,
		log:  slog.Default().With("component", "product"),
	}
}

func (s *ProductService) GetProduct(productID string) (*Product, error) {
	if productID == "" {
		return nil, fmt.Errorf("product ID cannot be empty")
	}
	product, err := s.repo.GetByID(productID)
	if err != nil {
		s.log.Warn("get product failed", "id", productID, "error", err)
		return nil, fmt.Errorf("failed to get product: %w", err)
	}
	return product, nil
}

func (s *ProductService) CreateProduct(req ProductRequest) (*Product, error) {
	if err := s.validateProductRequest(req); err != nil {
		return nil, fmt.Errorf("validation failed: %w", err)
	}
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
		s.log.Warn("create product failed", "id", productID, "error", err)
		return nil, fmt.Errorf("failed to create product: %w", err)
	}
	s.log.Info("product created", "id", productID, "name", product.Name)
	return product, nil
}

func (s *ProductService) UpdateProduct(productID string, req ProductRequest) (*Product, error) {
	if productID == "" {
		return nil, fmt.Errorf("product ID cannot be empty")
	}
	if err := s.validateProductRequest(req); err != nil {
		return nil, fmt.Errorf("validation failed: %w", err)
	}
	existing, err := s.repo.GetByID(productID)
	if err != nil {
		return nil, fmt.Errorf("product not found: %w", err)
	}
	existing.Name = req.Name
	existing.Description = req.Description
	existing.Price = req.Price
	existing.Category = req.Category
	existing.InStock = req.InStock
	if err := s.repo.Update(existing); err != nil {
		s.log.Warn("update product failed", "id", productID, "error", err)
		return nil, fmt.Errorf("failed to update product: %w", err)
	}
	s.log.Info("product updated", "id", productID)
	return existing, nil
}

func (s *ProductService) DeleteProduct(productID string) error {
	if productID == "" {
		return fmt.Errorf("product ID cannot be empty")
	}
	if err := s.repo.Delete(productID); err != nil {
		s.log.Warn("delete product failed", "id", productID, "error", err)
		return fmt.Errorf("failed to delete product: %w", err)
	}
	s.log.Info("product deleted", "id", productID)
	return nil
}

func (s *ProductService) ListProducts() ([]*Product, error) {
	products, err := s.repo.List()
	if err != nil {
		s.log.Warn("list products failed", "error", err)
		return nil, fmt.Errorf("failed to list products: %w", err)
	}
	return products, nil
}

func (s *ProductService) GetProductsByCategory(category string) ([]*Product, error) {
	if category == "" {
		return nil, fmt.Errorf("category cannot be empty")
	}
	products, err := s.repo.GetByCategory(category)
	if err != nil {
		s.log.Warn("get products by category failed", "category", category, "error", err)
		return nil, fmt.Errorf("failed to get products by category: %w", err)
	}
	return products, nil
}

func (s *ProductService) IsProductAvailable(productID string) (bool, error) {
	product, err := s.GetProduct(productID)
	if err != nil {
		return false, err
	}
	return product.IsValid(), nil
}

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
