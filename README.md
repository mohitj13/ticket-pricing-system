# Ticket Pricing System

A multi-module Spring Boot application for ticket transaction pricing calculation.

## Architecture

- **common**: Shared utilities and constants
- **pricing-service**: Pricing rules and discount engine
- **ticket-service**: Core ticket processing service

## Prerequisites

- Java 21
- Maven 3.8+

## Quick Start

### 1. Build the project
```bash
mvnw clean install
```

### 3. Run the application
```bash
cd ticket-service
..\mvnw spring-boot:run
```

### 4. Test the API
Request:

```bash
curl -X POST http://localhost:8080/api/v1/tickets/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "customers": [
      {
        "name": "John Doe",
        "age": 25
      },
      {
        "name": "Jane Smith",
        "dateOfBirth": 10
      }
    ],
    "transactionId": 1234
  }'
```

Response -
```json
{
  "transactionId": 1234,
  "totalCost": 30.00,
  "tickets": [
    {
      "ticketType": "CHILD",
      "quantity": 1,
      "totalCost": 5.00
    },
    {
      "ticketType": "ADULT",
      "quantity": 1,
      "totalCost": 25.00
    }
  ]
}
```


## API Endpoints

- `POST /api/v1/transactions` - Create a new ticket transaction process
- `GET /actuator/health` - Actuator health endpoint
- `GET /actuator/prometheus` - Metrics endpoint
-  http://localhost:8080/swagger-ui/index.html - Swagger Open API Specs

## Configuration

Key configuration files:
- `ticket-service/src/main/resources/application.yml` - Main configuration
- `pricing-service/src/main/resources/pricing-rules.yml` - Pricing rules

## Testing

```bash
# Run all tests
mvn test

# Run specific module tests
cd ticket-service && mvn test
```

## Ticket Types & Pricing

| Type   | Age Range | Base Price |
|--------|-----------|------------|
| CHILD  | 0-10      | $5.00      |
| TEEN   | 11-17     | $12.00     |
| ADULT  | 18-64     | $25.00     |
| SENIOR | 65+       | $25.00     |

## Discounts

- **Child Group Discount**: 25% off when purchasing 3+ tickets (child tickets only)
- **Senior Discount**: Additional 30% off for senior tickets

## Project Structure

```
ticket-pricing-system/
├── common/              # Shared utilities
├── pricing-service/     # Pricing rules module
├── ticket-service/      # Main service
└── pom.xml             # Parent POM
```