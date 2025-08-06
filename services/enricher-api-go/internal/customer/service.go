package customer

import (
	"fmt"
	"log"
)

// Service defines the business logic interface for customers
type Service interface {
	GetCustomer(customerID string) (*Customer, error)
	CreateCustomer(req CustomerRequest) (*Customer, error)
	UpdateCustomer(customerID string, req CustomerRequest) (*Customer, error)
	DeleteCustomer(customerID string) error
	ListCustomers() ([]*Customer, error)
	IsCustomerActive(customerID string) (bool, error)
}

// CustomerService implements the Service interface
type CustomerService struct {
	repo Repository
}

// NewService creates a new customer service
func NewService(repo Repository) *CustomerService {
	return &CustomerService{
		repo: repo,
	}
}

// GetCustomer retrieves a customer by ID
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

// CreateCustomer creates a new customer
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

// UpdateCustomer updates an existing customer
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
