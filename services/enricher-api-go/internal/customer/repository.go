package customer

import (
	"errors"
	"sync"
)

var ErrCustomerNotFound = errors.New("customer not found")

// Repository defines the interface for customer data access
type Repository interface {
	GetByID(customerID string) (*Customer, error)
	Create(customer *Customer) error
	Update(customer *Customer) error
	Delete(customerID string) error
	List() ([]*Customer, error)
}

// InMemoryRepository implements Repository interface using in-memory storage
type InMemoryRepository struct {
	customers map[string]*Customer
	mutex     sync.RWMutex
}

// NewInMemoryRepository creates a new in-memory customer repository with sample data
func NewInMemoryRepository() *InMemoryRepository {
	repo := &InMemoryRepository{
		customers: make(map[string]*Customer),
		mutex:     sync.RWMutex{},
	}

	// Add sample customers
	sampleCustomers := []*Customer{
		{CustomerID: "customer-456", Name: "Jane Doe", Status: "ACTIVE"},
		{CustomerID: "customer-123", Name: "John Smith", Status: "ACTIVE"},
		{CustomerID: "customer-789", Name: "Alice Johnson", Status: "INACTIVE"},
		{CustomerID: "customer-101", Name: "Bob Wilson", Status: "ACTIVE"},
		{CustomerID: "customer-202", Name: "Carol Brown", Status: "ACTIVE"},
	}

	for _, customer := range sampleCustomers {
		repo.customers[customer.CustomerID] = customer
	}

	return repo
}

// GetByID retrieves a customer by ID
func (r *InMemoryRepository) GetByID(customerID string) (*Customer, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	customer, exists := r.customers[customerID]
	if !exists {
		return nil, ErrCustomerNotFound
	}

	// Return a copy to prevent external modifications
	customerCopy := *customer
	return &customerCopy, nil
}

// Create adds a new customer
func (r *InMemoryRepository) Create(customer *Customer) error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	if _, exists := r.customers[customer.CustomerID]; exists {
		return errors.New("customer already exists")
	}

	r.customers[customer.CustomerID] = customer
	return nil
}

// Update modifies an existing customer
func (r *InMemoryRepository) Update(customer *Customer) error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	if _, exists := r.customers[customer.CustomerID]; !exists {
		return ErrCustomerNotFound
	}

	r.customers[customer.CustomerID] = customer
	return nil
}

// Delete removes a customer
func (r *InMemoryRepository) Delete(customerID string) error {
	r.mutex.Lock()
	defer r.mutex.Unlock()

	if _, exists := r.customers[customerID]; !exists {
		return ErrCustomerNotFound
	}

	delete(r.customers, customerID)
	return nil
}

// List returns all customers
func (r *InMemoryRepository) List() ([]*Customer, error) {
	r.mutex.RLock()
	defer r.mutex.RUnlock()

	customers := make([]*Customer, 0, len(r.customers))
	for _, customer := range r.customers {
		customerCopy := *customer
		customers = append(customers, &customerCopy)
	}

	return customers, nil
}
