// Package customer provides customer-related business logic and storage abstractions.
package customer

import (
	"fmt"
	"log/slog"
)

// Service is the business-logic surface for customers.
type Service interface {
	GetCustomer(customerID string) (*Customer, error)
	CreateCustomer(req CustomerRequest) (*Customer, error)
	UpdateCustomer(customerID string, req CustomerRequest) (*Customer, error)
	DeleteCustomer(customerID string) error
	ListCustomers() ([]*Customer, error)
	IsCustomerActive(customerID string) (bool, error)
}

// CustomerService implements Service against a Repository.
type CustomerService struct {
	repo Repository
	log  *slog.Logger
}

// NewService constructs a CustomerService backed by the supplied repository.
func NewService(repo Repository) *CustomerService {
	return &CustomerService{
		repo: repo,
		log:  slog.Default().With("component", "customer"),
	}
}

func (s *CustomerService) GetCustomer(customerID string) (*Customer, error) {
	if customerID == "" {
		return nil, fmt.Errorf("customer ID cannot be empty")
	}
	customer, err := s.repo.GetByID(customerID)
	if err != nil {
		s.log.Warn("get customer failed", "id", customerID, "error", err)
		return nil, fmt.Errorf("failed to get customer: %w", err)
	}
	return customer, nil
}

func (s *CustomerService) CreateCustomer(req CustomerRequest) (*Customer, error) {
	if err := s.validateCustomerRequest(req); err != nil {
		return nil, fmt.Errorf("validation failed: %w", err)
	}
	customerID := fmt.Sprintf("customer-%d", len(req.Name)*100+len(req.Status))
	customer := &Customer{
		CustomerID: customerID,
		Name:       req.Name,
		Status:     req.Status,
	}
	if err := s.repo.Create(customer); err != nil {
		s.log.Warn("create customer failed", "id", customerID, "error", err)
		return nil, fmt.Errorf("failed to create customer: %w", err)
	}
	s.log.Info("customer created", "id", customerID, "name", customer.Name)
	return customer, nil
}

func (s *CustomerService) UpdateCustomer(customerID string, req CustomerRequest) (*Customer, error) {
	if customerID == "" {
		return nil, fmt.Errorf("customer ID cannot be empty")
	}
	if err := s.validateCustomerRequest(req); err != nil {
		return nil, fmt.Errorf("validation failed: %w", err)
	}
	existing, err := s.repo.GetByID(customerID)
	if err != nil {
		return nil, fmt.Errorf("customer not found: %w", err)
	}
	existing.Name = req.Name
	existing.Status = req.Status
	if err := s.repo.Update(existing); err != nil {
		s.log.Warn("update customer failed", "id", customerID, "error", err)
		return nil, fmt.Errorf("failed to update customer: %w", err)
	}
	s.log.Info("customer updated", "id", customerID)
	return existing, nil
}

func (s *CustomerService) DeleteCustomer(customerID string) error {
	if customerID == "" {
		return fmt.Errorf("customer ID cannot be empty")
	}
	if err := s.repo.Delete(customerID); err != nil {
		s.log.Warn("delete customer failed", "id", customerID, "error", err)
		return fmt.Errorf("failed to delete customer: %w", err)
	}
	s.log.Info("customer deleted", "id", customerID)
	return nil
}

func (s *CustomerService) ListCustomers() ([]*Customer, error) {
	customers, err := s.repo.List()
	if err != nil {
		s.log.Warn("list customers failed", "error", err)
		return nil, fmt.Errorf("failed to list customers: %w", err)
	}
	return customers, nil
}

func (s *CustomerService) IsCustomerActive(customerID string) (bool, error) {
	customer, err := s.GetCustomer(customerID)
	if err != nil {
		return false, err
	}
	return customer.IsActive(), nil
}

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
