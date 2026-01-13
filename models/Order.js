const mongoose = require('mongoose');

const OrderSchema = new mongoose.Schema({
  haljinaId: mongoose.Schema.Types.ObjectId,
  naziv: String,
  slika: String,
  cena: Number,
  boja: String,
  kolicina: Number,
  screenshot: String,
  vreme: { type: Date, default: Date.now },
});

module.exports = mongoose.model('Order', OrderSchema);
