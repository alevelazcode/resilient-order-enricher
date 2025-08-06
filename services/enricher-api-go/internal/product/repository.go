package product

import (
	"errors"
	"sync"
)

var ErrProductNotFound = errors.New("product not found")

// Repository defines the interface for product data access
type Repository interface {
	GetByID(productID string) (*Product, error)
	Create(product *Product) error
	Update(product *Product) error
	Delete(productID string) error
	List() ([]*Product, error)
	GetByCategory(category string) ([]*Product, error)
}

// InMemoryRepository implements Repository interface using in-memory storage
type InMemoryRepository struct {
	products map[string]*Product
	mutex    sync.RWMutex
}

// NewInMemoryRepository creates a new in-memory product repository with sample data
func NewInMemoryRepository() *InMemoryRepository {
	repo := &InMemoryRepository{
		products: make(map[string]*Product),
		mutex:    sync.RWMutex{},
	}

	// Add sample products
	sampleProducts := []*Product{
		{
			ProductID:   "product-789",
			Name:        "Laptop",
			Description: "14-inch ultrabook with 16GB RAM",
			Price:       999.00,
			Category:    "Electronics",
			InStock:     true,
		},
		{
			ProductID:   "product-123",
			Name:        "Wireless Mouse",
			Description: "Ergonomic wireless mouse with USB receiver",
			Price:       25.99,
			Category:    "Electronics",
			InStock:     true,
		},
		{
			ProductID:   "product-456",
			Name:        "Office Chair",
			Description: "Comfortable ergonomic office chair",
			Price:       199.99,
			Category:    "Furniture",
			InStock:     true,
		},
		{
			ProductID:   "product-101",
			Name:        "Coffee Mug",
			Description: "Ceramic coffee mug 350ml",
			Price:       12.50,
			Category:    "Kitchen",
			InStock:     true,
		},
		{
			ProductID:   "product-202",
			Name:        "Desk Lamp",
			Description: "LED desk lamp with adjustable brightness",
			Price:       45.00,
			Category:    "Electronics",
			InStock:     false,
		},
	}

	for _, product := range sampleProducts {
		repo.products[product.ProductID] = product
	}

	return repo
}

// GetByID retrieves a product by ID
func (r *InMemoryRepository) GetByID(productID string) (*Product, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	product, exists := r.products[productID]
	if !exists {
		return nil, ErrProductNotFound
	}

	// Return a copy to prevent external modifications
	productCopy := *product
	return &productCopy, nil
}

// Create adds a new product
func (r *InMemoryRepository) Create(product *Product) error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	if _, exists := r.products[product.ProductID]; exists {
		return errors.New("product already exists")
	}

	r.products[product.ProductID] = product
	return nil
}

// Update modifies an existing product
func (r *InMemoryRepository) Update(product *Product) error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	if _, exists := r.products[product.ProductID]; !exists {
		return ErrProductNotFound
	}

	r.products[product.ProductID] = product
	return nil
}

// Delete removes a product
func (r *InMemoryRepository) Delete(productID string) error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	if _, exists := r.products[productID]; !exists {
		return ErrProductNotFound
	}

	delete(r.products, productID)
	return nil
}

// List returns all products
func (r *InMemoryRepository) List() ([]*Product, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	products := make([]*Product, 0, len(r.products))
	for _, product := range r.products {
		productCopy := *product
		products = append(products, &productCopy)
	}

	return products, nil
}

// GetByCategory returns products filtered by category
func (r *InMemoryRepository) GetByCategory(category string) ([]*Product, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	var products []*Product
	for _, product := range r.products {
		if product.Category == category {
			productCopy := *product
			products = append(products, &productCopy)
		}
	}

	return products, nil
}
