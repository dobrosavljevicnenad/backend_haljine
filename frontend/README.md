# Dress Inventory - Admin app

Angular 17 client for the [Dress Inventory API](../README.md): catalog and stock management,
cart checkout with payment screenshots and order history.

## Commands

| Command | Description |
| --- | --- |
| `npm start` | Dev server on http://localhost:4200, talks to the API on http://localhost:8080 |
| `npm run build` | Production build (`dist/webapp/browser`), API expected on the same origin under `/api` |
| `npm run test:ci` | Unit tests once in headless Chrome |
| `npm test` | Unit tests in watch mode |

## Structure

```
src/app
├── core            API contract (models), AuthService, JWT interceptor, InventoryApiService
├── login           login form
├── orders          order history with date filter and grouping by checkout
└── dress-list      dashboard: catalog, stock per color, cart, single-order dialog
```

- All HTTP calls go through `InventoryApiService`; components never build URLs.
- The JWT is kept in a signal and `localStorage` (SSR-safe) and attached by a functional interceptor,
  which also logs the user out when the API answers 401.
- The API base URL comes from `src/environments` (`/api` in production behind nginx,
  `http://localhost:8080/api` in development).
- In Docker the app is served by nginx, which also proxies `/api` to the backend (see `nginx.conf`).
