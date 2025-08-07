// Package product provides product-related data models and business logic
// for the Resilient Order Enricher API.
//
// This package contains the core domain models for product management,
// including data structures for product information, request/response
// models, and utility methods for product operations.
package product

// Product represents a product entity in the system.
//
// This struct contains the core product information including unique
// identifier, name, description, price, category, and stock status.
// It is used for both internal business logic and external API responses.
//
// Example usage:
//
//	product := &Product{
//		ProductID:   "product-12345",
//		Name:        "Gaming Laptop",
//		Description: "High-performance gaming laptop with RTX graphics",
//		Price:       1299.99,
//		Category:    "Electronics",
//		InStock:     true,
//	}
type Product struct {
	// ProductID is the unique identifier for the product
	ProductID string `json:"productId" db:"product_id"`
	// Name is the name of the product
	Name string `json:"name" db:"name"`
	// Description is the detailed description of the product
	Description string `json:"description" db:"description"`
	// Price is the price of the product in the base currency
	Price float64 `json:"price" db:"price"`
	// Category is the category or type of the product
	Category string `json:"category" db:"category"`
	// InStock indicates whether the product is currently in stock
	InStock bool `json:"inStock" db:"in_stock"`
}

// ProductRequest represents the request payload for product creation and updates.
//
// This struct is used for incoming API requests when creating or updating
// product information. It includes validation tags for request validation.
//
// Example usage:
//
//	request := ProductRequest{
//		Name:        "Gaming Laptop",
//		Description: "High-performance gaming laptop with RTX graphics",
//		Price:       1299.99,
//		Category:    "Electronics",
//		InStock:     true,
//	}
type ProductRequest struct {
	// Name is the name of the product (required, 2-100 characters)
	Name string `json:"name" validate:"required,min=2,max=100"`
	// Description is the detailed description of the product (required, 10-500 characters)
	Description string `json:"description" validate:"required,min=10,max=500"`
	// Price is the price of the product (required, must be greater than 0)
	Price float64 `json:"price" validate:"required,gt=0"`
	// Category is the category of the product (required, 2-50 characters)
	Category string `json:"category" validate:"required,min=2,max=50"`
	// InStock indicates whether the product is currently in stock
	InStock bool `json:"inStock"`
}

// ProductResponse represents the response payload for product operations.
//
// This struct is used for outgoing API responses when returning product
// information to clients. It provides a clean, consistent response format.
//
// Example usage:
//
//	response := ProductResponse{
//		ProductID:   "product-12345",
//		Name:        "Gaming Laptop",
//		Description: "High-performance gaming laptop with RTX graphics",
//		Price:       1299.99,
//		Category:    "Electronics",
//		InStock:     true,
//	}
type ProductResponse struct {
	// ProductID is the unique identifier for the product
	ProductID string `json:"productId"`
	// Name is the name of the product
	Name string `json:"name"`
	// Description is the detailed description of the product
	Description string `json:"description"`
	// Price is the price of the product in the base currency
	Price float64 `json:"price"`
	// Category is the category or type of the product
	Category string `json:"category"`
	// InStock indicates whether the product is currently in stock
	InStock bool `json:"inStock"`
}

// IsValid checks if the product is valid for order processing.
//
// This method validates that the product has a name, positive price, and is in stock.
// It provides a convenient way to check product validity before processing orders.
//
// Returns:
//   - bool: true if product is valid for orders, false otherwise
//
// Example usage:
//
//	product := &Product{
//		Name:    "Gaming Laptop",
//		Price:   1299.99,
//		InStock: true,
//	}
//	if product.IsValid() {
//		// Process valid product
//	}
func (p *Product) IsValid() bool {
	return p.Name != "" && p.Price > 0 && p.InStock
}

// ToResponse converts a Product to ProductResponse.
//
// This method creates a ProductResponse from the current Product instance,
// providing a clean way to convert internal models to API response models.
//
// Returns:
//   - ProductResponse: the converted response object
//
// Example usage:
//
//	product := &Product{
//		ProductID:   "product-12345",
//		Name:        "Gaming Laptop",
//		Description: "High-performance gaming laptop with RTX graphics",
//		Price:       1299.99,
//		Category:    "Electronics",
//		InStock:     true,
//	}
//	response := product.ToResponse()
func (p *Product) ToResponse() ProductResponse {
	return ProductResponse{
		ProductID:   p.ProductID,
		Name:        p.Name,
		Description: p.Description,
		Price:       p.Price,
		Category:    p.Category,
		InStock:     p.InStock,
	}
}
