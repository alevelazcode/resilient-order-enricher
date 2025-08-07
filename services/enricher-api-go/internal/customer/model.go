// Package customer provides customer-related data models and business logic
// for the Resilient Order Enricher API.
//
// This package contains the core domain models for customer management,
// including data structures for customer information, request/response
// models, and utility methods for customer operations.
package customer

// Customer represents a customer entity in the system.
//
// This struct contains the core customer information including unique
// identifier, name, and status. It is used for both internal business
// logic and external API responses.
//
// Example usage:
//
//	customer := &Customer{
//		CustomerID: "customer-12345",
//		Name:       "John Doe",
//		Status:     "ACTIVE",
//	}
type Customer struct {
	// CustomerID is the unique identifier for the customer
	CustomerID string `json:"customerId" db:"customer_id"`
	// Name is the full name of the customer
	Name string `json:"name" db:"name"`
	// Status indicates the current status of the customer (ACTIVE, INACTIVE)
	Status string `json:"status" db:"status"`
}

// CustomerRequest represents the request payload for customer creation and updates.
//
// This struct is used for incoming API requests when creating or updating
// customer information. It includes validation tags for request validation.
//
// Example usage:
//
//	request := CustomerRequest{
//		Name:   "Jane Smith",
//		Status: "ACTIVE",
//	}
type CustomerRequest struct {
	// Name is the full name of the customer (required, 2-100 characters)
	Name string `json:"name" validate:"required,min=2,max=100"`
	// Status indicates the customer status (required, must be ACTIVE or INACTIVE)
	Status string `json:"status" validate:"required,oneof=ACTIVE INACTIVE"`
}

// CustomerResponse represents the response payload for customer operations.
//
// This struct is used for outgoing API responses when returning customer
// information to clients. It provides a clean, consistent response format.
//
// Example usage:
//
//	response := CustomerResponse{
//		CustomerID: "customer-12345",
//		Name:       "John Doe",
//		Status:     "ACTIVE",
//	}
type CustomerResponse struct {
	// CustomerID is the unique identifier for the customer
	CustomerID string `json:"customerId"`
	// Name is the full name of the customer
	Name string `json:"name"`
	// Status indicates the current status of the customer
	Status string `json:"status"`
}

// IsActive checks if the customer is currently active.
//
// This method returns true if the customer status is "ACTIVE", false otherwise.
// It provides a convenient way to check customer status without string comparison.
//
// Returns:
//   - bool: true if customer is active, false otherwise
//
// Example usage:
//
//	customer := &Customer{Status: "ACTIVE"}
//	if customer.IsActive() {
//		// Process active customer
//	}
func (c *Customer) IsActive() bool {
	return c.Status == "ACTIVE"
}

// ToResponse converts a Customer to CustomerResponse.
//
// This method creates a CustomerResponse from the current Customer instance,
// providing a clean way to convert internal models to API response models.
//
// Returns:
//   - CustomerResponse: the converted response object
//
// Example usage:
//
//	customer := &Customer{
//		CustomerID: "customer-12345",
//		Name:       "John Doe",
//		Status:     "ACTIVE",
//	}
//	response := customer.ToResponse()
func (c *Customer) ToResponse() CustomerResponse {
	return CustomerResponse{
		CustomerID: c.CustomerID,
		Name:       c.Name,
		Status:     c.Status,
	}
}
