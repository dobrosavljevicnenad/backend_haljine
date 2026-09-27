const express = require('express');
const cors = require('cors');
require('dotenv').config();

const corsOptions = require('./config/cors');
const connectDB = require('./config/db');
const noCache = require('./middleware/noCache');

const authRoutes = require('./routes/authRoutes');
const uploadRoutes = require('./routes/uploadRoutes');
const dressRoutes = require('./routes/dressRoutes');
const orderRoutes = require('./routes/orderRoutes');
const tokenRoutes = require('./routes/tokenRoutes');

const app = express();

// MongoDB konekcija
connectDB();

// Middleware
app.use(cors(corsOptions));
app.use(express.json());
app.use(noCache);

// Routes
app.use('/api', authRoutes);
app.use('/api', uploadRoutes);
app.use('/api', dressRoutes);
app.use('/api', orderRoutes);
app.use('/api', tokenRoutes);

app.get('/', (req, res) => {
  res.send('🎉 Backend API radi. Možeš koristiti /api/haljine kako bi dobio prikaz svih haljina.');
});

module.exports = app;
