package customer

import (
	"testing"
)

func TestCustomerService_GetCustomer(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	// Act
	customer, err := service.GetCustomer("customer-456")
	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if customer == nil {
		t.Fatal("Expected customer, got nil")
	}

	if customer.CustomerID != "customer-456" {
		t.Errorf("Expected customer ID 'customer-456', got %s", customer.CustomerID)
	}

	if customer.Name != "Jane Doe" {
		t.Errorf("Expected customer name 'Jane Doe', got %s", customer.Name)
	}

	if customer.Status != "ACTIVE" {
		t.Errorf("Expected customer status 'ACTIVE', got %s", customer.Status)
	}
}

func TestCustomerService_GetCustomer_NotFound(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	// Act
	customer, err := service.GetCustomer("non-existent")

	// Assert
	if err == nil {
		t.Fatal("Expected error, got nil")
	}

	if customer != nil {
		t.Fatal("Expected nil customer, got result")
	}
}

func TestCustomerService_CreateCustomer(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	req := CustomerRequest{
		Name:   "Test Customer",
		Status: "ACTIVE",
	}

	// Act
	customer, err := service.CreateCustomer(req)
	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if customer == nil {
		t.Fatal("Expected customer, got nil")
	}

	if customer.Name != "Test Customer" {
		t.Errorf("Expected customer name 'Test Customer', got %s", customer.Name)
	}

	if customer.Status != "ACTIVE" {
		t.Errorf("Expected customer status 'ACTIVE', got %s", customer.Status)
	}

	// Verify customer can be retrieved
	retrievedCustomer, err := service.GetCustomer(customer.CustomerID)
	if err != nil {
		t.Fatalf("Expected no error retrieving customer, got %v", err)
	}

	if retrievedCustomer.CustomerID != customer.CustomerID {
		t.Errorf("Expected same customer ID, got %s vs %s", retrievedCustomer.CustomerID, customer.CustomerID)
	}
}

func TestCustomerService_CreateCustomer_ValidationError(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	testCases := []struct {
		name    string
		request CustomerRequest
	}{
		{
			name: "Empty name",
			request: CustomerRequest{
				Name:   "",
				Status: "ACTIVE",
			},
		},
		{
			name: "Invalid status",
			request: CustomerRequest{
				Name:   "Test Customer",
				Status: "INVALID",
			},
		},
		{
			name: "Name too short",
			request: CustomerRequest{
				Name:   "A",
				Status: "ACTIVE",
			},
		},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			// Act
			customer, err := service.CreateCustomer(tc.request)

			// Assert
			if err == nil {
				t.Fatal("Expected validation error, got nil")
			}

			if customer != nil {
				t.Fatal("Expected nil customer, got result")
			}
		})
	}
}

func TestCustomerService_IsCustomerActive(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	// Test active customer
	isActive, err := service.IsCustomerActive("customer-456")
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if !isActive {
		t.Error("Expected customer to be active")
	}

	// Test inactive customer
	isActive, err = service.IsCustomerActive("customer-789")
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if isActive {
		t.Error("Expected customer to be inactive")
	}
}

func TestCustomerService_UpdateCustomer(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	req := CustomerRequest{
		Name:   "Updated Name",
		Status: "INACTIVE",
	}

	// Act
	customer, err := service.UpdateCustomer("customer-456", req)
	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if customer.Name != "Updated Name" {
		t.Errorf("Expected updated name 'Updated Name', got %s", customer.Name)
	}

	if customer.Status != "INACTIVE" {
		t.Errorf("Expected updated status 'INACTIVE', got %s", customer.Status)
	}

	// Verify changes persisted
	retrievedCustomer, err := service.GetCustomer("customer-456")
	if err != nil {
		t.Fatalf("Expected no error retrieving customer, got %v", err)
	}

	if retrievedCustomer.Name != "Updated Name" {
		t.Errorf("Expected persisted name 'Updated Name', got %s", retrievedCustomer.Name)
	}
}

func TestCustomerService_DeleteCustomer(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	// Verify customer exists first
	_, err := service.GetCustomer("customer-456")
	if err != nil {
		t.Fatalf("Expected customer to exist, got error: %v", err)
	}

	// Act
	err = service.DeleteCustomer("customer-456")
	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	// Verify customer no longer exists
	_, err = service.GetCustomer("customer-456")
	if err == nil {
		t.Fatal("Expected error when getting deleted customer, got nil")
	}
}

func TestCustomerService_ListCustomers(t *testing.T) {
	// Arrange
	repo := NewInMemoryRepository()
	service := NewService(repo)

	// Act
	customers, err := service.ListCustomers()
	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if len(customers) == 0 {
		t.Fatal("Expected customers to be returned")
	}

	// Should have the sample customers from repository initialization
	expectedCount := 5 // Based on NewInMemoryRepository sample data
	if len(customers) != expectedCount {
		t.Errorf("Expected %d customers, got %d", expectedCount, len(customers))
	}
}
