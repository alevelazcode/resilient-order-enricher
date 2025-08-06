package product

import (
	"testing"
)

func TestProductService_GetProduct(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	// Act
	product, err := service.GetProduct("product-789")
	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if product == nil {
		t.Fatal("Expected product, got nil")
	}

	if product.ProductID != "product-789" {
		t.Errorf("Expected product ID 'product-789', got %s", product.ProductID)
	}

	if product.Name != "Laptop" {
		t.Errorf("Expected product name 'Laptop', got %s", product.Name)
	}

	if product.Price != 999.00 {
		t.Errorf("Expected product price 999.00, got %.2f", product.Price)
	}

	if !product.InStock {
		t.Error("Expected product to be in stock")
	}
}

func TestProductService_GetProduct_NotFound(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	// Act
	product, err := service.GetProduct("non-existent")

	// Assert
	if err == nil {
		t.Fatal("Expected error, got nil")
	}

	if product != nil {
		t.Fatal("Expected nil product, got result")
	}
}

func TestProductService_CreateProduct(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	req := ProductRequest{
		Name:        "Test Product",
		Description: "A test product for unit testing",
		Price:       29.99,
		Category:    "Test",
		InStock:     true,
	}

	// Act
	product, err := service.CreateProduct(req)
	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if product == nil {
		t.Fatal("Expected product, got nil")
	}

	if product.Name != "Test Product" {
		t.Errorf("Expected product name 'Test Product', got %s", product.Name)
	}

	if product.Price != 29.99 {
		t.Errorf("Expected product price 29.99, got %.2f", product.Price)
	}

	// Verify product can be retrieved
	retrievedProduct, err := service.GetProduct(product.ProductID)
	if err != nil {
		t.Fatalf("Expected no error retrieving product, got %v", err)
	}

	if retrievedProduct.ProductID != product.ProductID {
		t.Errorf("Expected same product ID, got %s vs %s", retrievedProduct.ProductID, product.ProductID)
	}
}

func TestProductService_CreateProduct_ValidationError(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	testCases := []struct {
		name    string
		request ProductRequest
	}{
		{
			name: "Empty name",
			request: ProductRequest{
				Name:        "",
				Description: "Valid description here",
				Price:       29.99,
				Category:    "Test",
				InStock:     true,
			},
		},
		{
			name: "Invalid price",
			request: ProductRequest{
				Name:        "Test Product",
				Description: "Valid description here",
				Price:       -10.00,
				Category:    "Test",
				InStock:     true,
			},
		},
		{
			name: "Description too short",
			request: ProductRequest{
				Name:        "Test Product",
				Description: "Short",
				Price:       29.99,
				Category:    "Test",
				InStock:     true,
			},
		},
		{
			name: "Empty category",
			request: ProductRequest{
				Name:        "Test Product",
				Description: "Valid description here",
				Price:       29.99,
				Category:    "",
				InStock:     true,
			},
		},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			// Act
			product, err := service.CreateProduct(tc.request)

			// Assert
			if err == nil {
				t.Fatal("Expected validation error, got nil")
			}

			if product != nil {
				t.Fatal("Expected nil product, got result")
			}
		})
	}
}

func TestProductService_IsProductAvailable(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	// Test available product (in stock)
	isAvailable, err := service.IsProductAvailable("product-789")
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if !isAvailable {
		t.Error("Expected product to be available")
	}

	// Test unavailable product (out of stock)
	isAvailable, err = service.IsProductAvailable("product-202")
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if isAvailable {
		t.Error("Expected product to be unavailable (out of stock)")
	}
}

func TestProductService_GetProductsByCategory(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	// Act
	products, err := service.GetProductsByCategory("Electronics")
	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if len(products) == 0 {
		t.Fatal("Expected products to be returned")
	}

	// Verify all products are from Electronics category
	for _, product := range products {
		if product.Category != "Electronics" {
			t.Errorf("Expected product category 'Electronics', got %s", product.Category)
		}
	}

	// Should have multiple electronics products based on sample data
	expectedMinCount := 2
	if len(products) < expectedMinCount {
		t.Errorf("Expected at least %d electronics products, got %d", expectedMinCount, len(products))
	}
}

func TestProductService_UpdateProduct(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	req := ProductRequest{
		Name:        "Updated Product",
		Description: "This product has been updated for testing",
		Price:       1299.99,
		Category:    "Updated",
		InStock:     false,
	}

	// Act
	product, err := service.UpdateProduct("product-789", req)
	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if product.Name != "Updated Product" {
		t.Errorf("Expected updated name 'Updated Product', got %s", product.Name)
	}

	if product.Price != 1299.99 {
		t.Errorf("Expected updated price 1299.99, got %.2f", product.Price)
	}

	if product.InStock {
		t.Error("Expected product to be out of stock")
	}

	// Verify changes persisted
	retrievedProduct, err := service.GetProduct("product-789")
	if err != nil {
		t.Fatalf("Expected no error retrieving product, got %v", err)
	}

	if retrievedProduct.Name != "Updated Product" {
		t.Errorf("Expected persisted name 'Updated Product', got %s", retrievedProduct.Name)
	}
}

func TestProductService_DeleteProduct(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	// Verify product exists first
	_, err := service.GetProduct("product-789")
	if err != nil {
		t.Fatalf("Expected product to exist, got error: %v", err)
	}

	// Act
	err = service.DeleteProduct("product-789")
	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	// Verify product no longer exists
	_, err = service.GetProduct("product-789")
	if err == nil {
		t.Fatal("Expected error when getting deleted product, got nil")
	}
}

func TestProductService_ListProducts(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	// Act
	products, err := service.ListProducts()
	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if len(products) == 0 {
		t.Fatal("Expected products to be returned")
	}

	// Should have the sample products from repository initialization
	expectedCount := 5 // Based on NewInMemoryRepository sample data
	if len(products) != expectedCount {
		t.Errorf("Expected %d products, got %d", expectedCount, len(products))
	}
}
