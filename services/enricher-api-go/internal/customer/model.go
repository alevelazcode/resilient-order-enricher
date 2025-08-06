package customer

// Customer represents a customer entity
type Customer struct {
	CustomerID string `json:"customerId" db:"customer_id"`
	Name       string `json:"name" db:"name"`
	Status     string `json:"status" db:"status"`
}

// CustomerRequest represents the request for customer creation/update
type CustomerRequest struct {
	Name   string `json:"name" validate:"required,min=2,max=100"`
	Status string `json:"status" validate:"required,oneof=ACTIVE INACTIVE"`
}

// CustomerResponse represents the response for customer operations
type CustomerResponse struct {
	CustomerID string `json:"customerId"`
	Name       string `json:"name"`
	Status     string `json:"status"`
}

// IsActive checks if the customer is active
func (c *Customer) IsActive() bool {
	return c.Status == "ACTIVE"
}

// ToResponse converts a Customer to CustomerResponse
func (c *Customer) ToResponse() CustomerResponse {
	return CustomerResponse{
		CustomerID: c.CustomerID,
		Name:       c.Name,
		Status:     c.Status,
	}
}
