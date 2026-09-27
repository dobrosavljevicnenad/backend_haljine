const corsOptions = {
  origin: function (origin, callback) {
    console.log('Origin:', origin);
    const allowedOrigins = [
      'http://localhost:4200',
      'https://backend-za-haljine.fly.dev',
      'https://haljine.onrender.com',
      'https://backendhaljine-production-09a8.up.railway.app',
      'https://haljine.netlify.app',
    ];

    if (!origin || allowedOrigins.includes(origin)) {
      callback(null, true);
    } else {
      callback(new Error('Not allowed by CORS'));
    }
  },
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization'],
};

module.exports = corsOptions;
