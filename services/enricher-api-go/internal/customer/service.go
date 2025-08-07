// Package customer provides customer-related business logic and data management
// for the Resilient Order Enricher API.
//
// This package contains the service layer for customer operations, including
// CRUD operations, validation, and business logic for customer management.
package customer

import (
	"fmt"
	"log"
)

// Service defines the business logic interface for customer operations.
//
// This interface provides a contract for customer-related business operations
// including CRUD operations, validation, and status checks.
//
// Example usage:
//
//	var customerService Service
//	customer, err := customerService.GetCustomer("customer-12345")
//	if err != nil {
//		// Handle error
//	}
type Service interface {
	// GetCustomer retrieves a customer by their unique identifier.
	//
	// Args:
	//   - customerID: the unique identifier of the customer
	//
	// Returns:
	//   - *Customer: the customer if found
	//   - error: error if customer not found or other issues occur
	GetCustomer(customerID string) (*Customer, error)

	// CreateCustomer creates a new customer with the provided information.
	//
	// Args:
	//   - req: CustomerRequest containing customer details
	//
	// Returns:
	//   - *Customer: the newly created customer
	//   - error: error if creation fails
	CreateCustomer(req CustomerRequest) (*Customer, error)

	// UpdateCustomer updates an existing customer's information.
	//
	// Args:
	//   - customerID: the unique identifier of the customer to update
	//   - req: CustomerRequest containing updated customer details
	//
	// Returns:
	//   - *Customer: the updated customer
	//   - error: error if update fails or customer not found
	UpdateCustomer(customerID string, req CustomerRequest) (*Customer, error)

	// DeleteCustomer removes a customer from the system.
	//
	// Args:
	//   - customerID: the unique identifier of the customer to delete
	//
	// Returns:
	//   - error: error if deletion fails or customer not found
	DeleteCustomer(customerID string) error

	// ListCustomers retrieves all customers in the system.
	//
	// Returns:
	//   - []*Customer: list of all customers
	//   - error: error if retrieval fails
	ListCustomers() ([]*Customer, error)

	// IsCustomerActive checks if a customer is currently active.
	//
	// Args:
	//   - customerID: the unique identifier of the customer
	//
	// Returns:
	//   - bool: true if customer is active, false otherwise
	//   - error: error if check fails or customer not found
	IsCustomerActive(customerID string) (bool, error)
}

// CustomerService implements the Service interface for customer operations.
//
// This struct provides the concrete implementation of customer business logic,
// including validation, data transformation, and repository coordination.
//
// Example usage:
//
//	repo := customer.NewRepository()
//	service := customer.NewService(repo)
//	customer, err := service.GetCustomer("customer-12345")
type CustomerService struct {
	repo Repository
}

// NewService creates a new customer service instance.
//
// This function creates and returns a new CustomerService with the provided
// repository dependency.
//
// Args:
//   - repo: Repository implementation for data access
//
// Returns:
//   - *CustomerService: new customer service instance
//
// Example usage:
//
//	repo := customer.NewRepository()
//	service := customer.NewService(repo)
func NewService(repo Repository) *CustomerService {
	return &CustomerService{
		repo: repo,
	}
}

// GetCustomer retrieves a customer by their unique identifier.
//
// This method validates the customer ID and retrieves the customer from
// the repository. It includes comprehensive error handling and logging.
//
// Args:
//   - customerID: the unique identifier of the customer
//
// Returns:
//   - *Customer: the customer if found
//   - error: error if customer not found or other issues occur
//
// Example usage:
//
//	customer, err := service.GetCustomer("customer-12345")
//	if err != nil {
//		log.Printf("Failed to get customer: %v", err)
//		return
//	}
//	log.Printf("Retrieved customer: %s", customer.Name)
func (s *CustomerService) GetCustomer(customerID string) (*Customer, error) {
	log.Printf("Getting customer with ID: %s", customerID)

	if customerID == "" {
		return nil, fmt.Errorf("customer ID cannot be empty")
	}

	customer, err := s.repo.GetByID(customerID)
	if err != nil {
		log.Printf("Error getting customer %s: %v", customerID, err)
		return nil, fmt.Errorf("failed to get customer: %w", err)
	}

	log.Printf("Successfully retrieved customer: %s", customer.Name)
	return customer, nil
}

// CreateCustomer creates a new customer with the provided information.
//
// This method validates the customer request, generates a unique ID,
// creates the customer entity, and persists it to the repository.
//
// Args:
//   - req: CustomerRequest containing customer details
//
// Returns:
//   - *Customer: the newly created customer
//   - error: error if creation fails
//
// Example usage:
//
//	req := CustomerRequest{
//		Name:   "John Doe",
//		Status: "ACTIVE",
//	}
//	customer, err := service.CreateCustomer(req)
//	if err != nil {
//		log.Printf("Failed to create customer: %v", err)
//		return
//	}
//	log.Printf("Created customer with ID: %s", customer.CustomerID)
func (s *CustomerService) CreateCustomer(req CustomerRequest) (*Customer, error) {
	log.Printf("Creating new customer: %s", req.Name)

	if err := s.validateCustomerRequest(req); err != nil {
		return nil, fmt.Errorf("validation failed: %w", err)
	}

	// Generate a simple ID (in production, use UUID)
	customerID := fmt.Sprintf("customer-%d", len(req.Name)*100+len(req.Status))

	customer := &Customer{
		CustomerID: customerID,
		Name:       req.Name,
		Status:     req.Status,
	}

	if err := s.repo.Create(customer); err != nil {
		log.Printf("Error creating customer: %v", err)
		return nil, fmt.Errorf("failed to create customer: %w", err)
	}

	log.Printf("Successfully created customer with ID: %s", customerID)
	return customer, nil
}

// UpdateCustomer updates an existing customer's information.
//
// This method validates the customer ID and request, checks if the customer
// exists, updates the customer information, and persists the changes.
//
// Args:
//   - customerID: the unique identifier of the customer to update
//   - req: CustomerRequest containing updated customer details
//
// Returns:
//   - *Customer: the updated customer
//   - error: error if update fails or customer not found
//
// Example usage:
//
//	req := CustomerRequest{
//		Name:   "Jane Smith",
//		Status: "INACTIVE",
//	}
//	customer, err := service.UpdateCustomer("customer-12345", req)
//	if err != nil {
//		log.Printf("Failed to update customer: %v", err)
//		return
//	}
//	log.Printf("Updated customer: %s", customer.Name)
func (s *CustomerService) UpdateCustomer(customerID string, req CustomerRequest) (*Customer, error) {
	log.Printf("Updating customer with ID: %s", customerID)

	if customerID == "" {
		return nil, fmt.Errorf("customer ID cannot be empty")
	}

	if err := s.validateCustomerRequest(req); err != nil {
		return nil, fmt.Errorf("validation failed: %w", err)
	}

	// Check if customer exists
	existingCustomer, err := s.repo.GetByID(customerID)
	if err != nil {
		return nil, fmt.Errorf("customer not found: %w", err)
	}

	// Update customer fields
	existingCustomer.Name = req.Name
	existingCustomer.Status = req.Status

	if err := s.repo.Update(existingCustomer); err != nil {
		log.Printf("Error updating customer: %v", err)
		return nil, fmt.Errorf("failed to update customer: %w", err)
	}

	log.Printf("Successfully updated customer: %s", customerID)
	return existingCustomer, nil
}

// DeleteCustomer removes a customer
func (s *CustomerService) DeleteCustomer(customerID string) error {
	log.Printf("Deleting customer with ID: %s", customerID)

	if customerID == "" {
		return fmt.Errorf("customer ID cannot be empty")
	}

	if err := s.repo.Delete(customerID); err != nil {
		log.Printf("Error deleting customer: %v", err)
		return fmt.Errorf("failed to delete customer: %w", err)
	}

	log.Printf("Successfully deleted customer: %s", customerID)
	return nil
}

// ListCustomers returns all customers
func (s *CustomerService) ListCustomers() ([]*Customer, error) {
	log.Println("Listing all customers")

	customers, err := s.repo.List()
	if err != nil {
		log.Printf("Error listing customers: %v", err)
		return nil, fmt.Errorf("failed to list customers: %w", err)
	}

	log.Printf("Successfully retrieved %d customers", len(customers))
	return customers, nil
}

// IsCustomerActive checks if a customer is active
func (s *CustomerService) IsCustomerActive(customerID string) (bool, error) {
	customer, err := s.GetCustomer(customerID)
	if err != nil {
		return false, err
	}

	return customer.IsActive(), nil
}

// validateCustomerRequest validates the customer request
func (s *CustomerService) validateCustomerRequest(req CustomerRequest) error {
	if req.Name == "" {
		return fmt.Errorf("customer name is required")
	}

	if len(req.Name) < 2 {
		return fmt.Errorf("customer name must be at least 2 characters")
	}

	if len(req.Name) > 100 {
		return fmt.Errorf("customer name must be at most 100 characters")
	}

	if req.Status != "ACTIVE" && req.Status != "INACTIVE" {
		return fmt.Errorf("customer status must be either ACTIVE or INACTIVE")
	}

	return nil
}
