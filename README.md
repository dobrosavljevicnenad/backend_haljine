# Dress Inventory & Order Management System

Full stack web application for managing boutique dress inventory with real-time stock tracking, secure ordering, and concurrency-safe updates.

---

## Features

### Inventory Management
- Add, update and delete dresses
- Track stock by color variants
- Atomic stock updates to prevent overselling
- Automatic cleanup of depleted variants

### Order System
- Single product ordering
- Multi-product cart ordering
- Real-time stock validation
- MongoDB transactional consistency

### Security
- JWT authentication
- Protected API routes
- Production CORS setup

### Media Storage
- Cloudinary image uploads
- Automatic image deletion

---

## Tech Stack

**Frontend**
- Angular  
- Tailwind CSS  

**Backend**
- Node.js  
- Express.js  
- MongoDB (Mongoose)  

**Cloud & Hosting**
- Cloudinary  
- Fly.io / Railway  
- Netlify  

---

## Architecture Highlights

- RESTful API design  
- Stateless JWT authentication  
- Atomic MongoDB operations  
- Transaction-based stock updates  
- Concurrency-safe inventory system  

---
