# Modern Furniture E-commerce Demo

This repository hosts a lightweight, three-tier sample application that demonstrates an e-commerce workflow:

* **Backend:** Java 17 with Spring Boot, REST controllers, and Spring Data MongoDB.
* **Database:** MongoDB stores catalog, customer, cart, and order documents.
* **Frontend:** Static HTML/CSS/JavaScript storefront that interacts with the REST API.

The app showcases an online furniture shop where shoppers browse products, manage a cart, and place orders while administrators can manage catalog entries through the API.

## Architecture Overview

```
frontend/            Static storefront (HTML/CSS/JS)
backend/             Spring Boot REST API + business logic
└── src/main/java/com/example/ecommerce
    ├── controller   REST controllers & exception handling
    ├── dto          API request payloads
    ├── model        MongoDB document schemas
    ├── repository   Spring Data repositories
    └── service      Domain services with business rules
```

MongoDB collections persist products, customers, carts, and orders. Cart and order processing enforce stock validation and update product quantities to keep inventory consistent.

## Running the Application

### Prerequisites

* Java 17+
* Maven 3.9+
* MongoDB server (local or remote). The default connection string is `mongodb://localhost:27017/ecommerce`.
* Any static web server (optional) for the frontend, e.g. `npm serve`, `python -m http.server`, or your favorite dev server.

### Backend API

```bash
cd backend
mvn spring-boot:run
```

The service starts on <http://localhost:8080>. Sample data for products and customers loads on the first run.

Key endpoints:

* `GET /api/products` – List catalog products.
* `POST /api/products` – Create or update catalog items.
* `GET /api/customers` – Enumerate customers.
* `GET /api/carts/{customerId}` – Inspect the active cart for a customer.
* `POST /api/carts/{customerId}/items` – Add products to the cart with stock checks.
* `PATCH /api/carts/{customerId}/items/{productId}` – Change quantities, enforcing inventory rules.
* `POST /api/orders/checkout` – Convert a cart into an order, deducting inventory atomically.

### Frontend

Serve the `frontend/` directory with your preferred tool:

```bash
cd frontend
python -m http.server 5173
```

Then open <http://localhost:5173> in a browser. The interface allows you to:

1. Choose a customer.
2. Add products to the cart.
3. Update quantities or remove items.
4. Checkout to create orders and review the order history.

> **Tip:** Ensure the backend is running before interacting with the storefront to avoid API errors. If the API is offline the UI falls back to seeded customers but will display helpful messages for failed operations.

## Sample Data

On startup the backend seeds a curated modern furniture catalog and two sample customers. This makes it simple to explore the workflow immediately without additional setup.

## Extending the Demo

* Integrate authentication for real shoppers and administrators.
* Add payment processing or shipping integrations.
* Build a richer frontend with frameworks such as React or Vue.
* Deploy MongoDB Atlas and expose environment variables for secure configuration.

Enjoy exploring the full stack implementation!
