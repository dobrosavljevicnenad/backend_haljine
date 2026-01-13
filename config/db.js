const mongoose = require('mongoose');

module.exports = function connectDB() {
  mongoose
    .connect(process.env.MONGODB_URI, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    })
    .then(() => console.log('✅ MongoDB konektovan'))
    .catch((err) => console.error('❌ Greška pri konekciji:', err));
};
