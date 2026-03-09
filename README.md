# E-Commerce Multi-Module App

A Spring Boot multi-module project with two microservices:

| Service         | Port | Database   | Role                                      |
|-----------------|------|------------|-------------------------------------------|
| cart-service    | 8081 | MySQL      | Guest cart + session state management     |
| checkout-service| 8082 | Cassandra  | Order creation, cart sync at checkout     |

---

## Architecture

```
Browser / Frontend
       │
       │  GUEST_SESSION cookie (auto-managed)
       │
  ┌────▼────────────────┐          ┌──────────────────────────┐
  │    cart-service      │◄─────────│    checkout-service       │
  │    (port 8081)       │  REST    │    (port 8082)            │
  │                      │          │                           │
  │  MySQL               │          │  Cassandra                │
  │  ├─ cart             │          │  ├─ orders                │
  │  ├─ cart_item        │          │  └─ order_items           │
  │  └─ SPRING_SESSION   │          │                           │
  └─────────────────────-┘          └──────────────────────────┘
```

### Session Strategy
- Spring Session JDBC stores every guest's session in MySQL (`SPRING_SESSION` table).
- The session ID is the cart key. No login required.
- Session TTL: **30 minutes** (configurable in `application.yml`).

### Cart ↔ Checkout Sync
At checkout time:
1. Checkout service reads the cart via `GET /api/cart/internal/{sessionId}`.
2. Validates items (stock limits, pricing rules).
3. If changes found → pushes corrections to `POST /api/cart/internal/sync` (MySQL updated).
4. Saves immutable order snapshot to Cassandra.
5. Marks cart `CHECKED_OUT` via `POST /api/cart/internal/{sessionId}/checkout`.

The `updatedCart` field in `CheckoutResponse` is non-null when the cart was modified, so the frontend can display a diff to the user.

---

## Prerequisites

| Tool        | Version  |
|-------------|----------|
| Java        | 17+      |
| Maven       | 3.8+     |
| MySQL       | 8.0+     |
| Cassandra   | 4.x      |

---

## Database Setup

### MySQL
```sql
CREATE DATABASE IF NOT EXISTS cart_db;
-- Tables are auto-created by JPA (ddl-auto: update) and schema.sql
```

### Cassandra
```cql
-- Run once on your Cassandra node
CREATE KEYSPACE IF NOT EXISTS ecommerce
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
-- Tables are auto-created by Spring Data Cassandra (CREATE_IF_NOT_EXISTS)
```

---

## Configuration

### cart-service (`src/main/resources/application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cart_db
    username: root
    password: root   # ← change this
```

### checkout-service (`src/main/resources/application.yml`)
```yaml
spring:
  cassandra:
    contact-points: localhost
    port: 9042
    keyspace-name: ecommerce
    local-datacenter: datacenter1

cart-service:
  base-url: http://localhost:8081   # ← adjust if deploying separately
```

---

## Build & Run

```bash
# Build all modules
mvn clean install

# Run cart-service
cd cart-service
mvn spring-boot:run

# Run checkout-service (separate terminal)
cd checkout-service
mvn spring-boot:run
```

---

## API Reference

### Cart Service (port 8081)

| Method | Endpoint                              | Description                          |
|--------|---------------------------------------|--------------------------------------|
| GET    | `/api/cart`                           | Get/create cart for current session  |
| POST   | `/api/cart/items`                     | Add item to cart                     |
| PUT    | `/api/cart/items/{productId}`         | Update item quantity (0 = remove)    |
| DELETE | `/api/cart/items/{productId}`         | Remove item                          |
| DELETE | `/api/cart`                           | Clear entire cart                    |
| GET    | `/api/cart/internal/{sessionId}`      | [Internal] Get cart by session ID    |
| POST   | `/api/cart/internal/sync`             | [Internal] Sync cart changes         |
| POST   | `/api/cart/internal/{sessionId}/checkout` | [Internal] Mark cart checked out |

### Checkout Service (port 8082)

| Method | Endpoint                                | Description                    |
|--------|-----------------------------------------|--------------------------------|
| POST   | `/api/checkout`                         | Initiate checkout              |
| PUT    | `/api/checkout/{orderId}/confirm`       | Confirm order (post-payment)   |
| PUT    | `/api/checkout/{orderId}/cancel`        | Cancel order                   |
| GET    | `/api/checkout/{orderId}`               | Get order details              |
| GET    | `/api/checkout/session/{sessionId}`     | Get all orders for session     |

---

## Example Requests

### 1. Add item to cart
```bash
curl -c cookies.txt -b cookies.txt -X POST http://localhost:8081/api/cart/items \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PROD-001",
    "productName": "Wireless Headphones",
    "price": 49.99,
    "quantity": 2,
    "imageUrl": "https://example.com/headphones.jpg"
  }'
```

### 2. View cart
```bash
curl -c cookies.txt -b cookies.txt http://localhost:8081/api/cart
```

### 3. Checkout
```bash
# Get session ID from the cart response or the GUEST_SESSION cookie value
curl -X POST http://localhost:8082/api/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "<your-session-id>",
    "shippingAddress": "123 Main St, Karachi",
    "paymentMethod": "CREDIT_CARD"
  }'
```

### 4. Confirm order
```bash
curl -X PUT http://localhost:8082/api/checkout/{orderId}/confirm
```

---

## Project Structure

```
ecommerce-app/
├── pom.xml                          ← parent POM
├── cart-service/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ecommerce/cart/
│       │   ├── CartServiceApplication.java
│       │   ├── config/SessionConfig.java
│       │   ├── controller/CartController.java
│       │   ├── service/CartService.java
│       │   ├── service/CartServiceImpl.java
│       │   ├── repository/{CartRepository, CartItemRepository}.java
│       │   ├── entity/{Cart, CartItem}.java
│       │   ├── dto/{AddItemRequest, UpdateItemRequest, CartDto,
│       │   │        CartItemDto, CartSyncRequest}.java
│       │   └── exception/{CartNotFoundException, GlobalExceptionHandler}.java
│       └── resources/
│           ├── application.yml
│           └── schema.sql
└── checkout-service/
    ├── pom.xml
    └── src/main/
        ├── java/com/ecommerce/checkout/
        │   ├── CheckoutServiceApplication.java
        │   ├── config/{CassandraConfig, RestTemplateConfig}.java
        │   ├── controller/CheckoutController.java
        │   ├── service/{CheckoutService, CheckoutServiceImpl}.java
        │   ├── repository/{OrderRepository, OrderItemRepository}.java
        │   ├── entity/{Order, OrderItem}.java
        │   ├── client/CartServiceClient.java
        │   ├── dto/{CartDto, CartItemDto, CartSyncRequest,
        │   │        CheckoutRequest, CheckoutResponse}.java
        │   └── exception/{OrderNotFoundException, EmptyCartException,
        │                   GlobalExceptionHandler}.java
        └── resources/
            └── application.yml
```
