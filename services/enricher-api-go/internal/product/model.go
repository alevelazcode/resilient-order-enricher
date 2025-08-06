package product

// Product represents a product entity
type Product struct {
	ProductID   string  `json:"productId" db:"product_id"`
	Name        string  `json:"name" db:"name"`
	Description string  `json:"description" db:"description"`
	Price       float64 `json:"price" db:"price"`
	Category    string  `json:"category" db:"category"`
	InStock     bool    `json:"inStock" db:"in_stock"`
}

// ProductRequest represents the request for product creation/update
type ProductRequest struct {
	Name        string  `json:"name" validate:"required,min=2,max=100"`
	Description string  `json:"description" validate:"required,min=10,max=500"`
	Price       float64 `json:"price" validate:"required,gt=0"`
	Category    string  `json:"category" validate:"required,min=2,max=50"`
	InStock     bool    `json:"inStock"`
}

// ProductResponse represents the response for product operations
type ProductResponse struct {
	ProductID   string  `json:"productId"`
	Name        string  `json:"name"`
	Description string  `json:"description"`
	Price       float64 `json:"price"`
	Category    string  `json:"category"`
	InStock     bool    `json:"inStock"`
}

// IsValid checks if the product is valid for orders
func (p *Product) IsValid() bool {
	return p.Name != "" && p.Price > 0 && p.InStock
}

// ToResponse converts a Product to ProductResponse
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
