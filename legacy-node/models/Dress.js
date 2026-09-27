const mongoose = require('mongoose');

const DressSchema = new mongoose.Schema({
  naziv: String,
  slika: String,
  cena: Number,
  lager: [
    {
      boja: String,
      kolicina: Number,
    },
  ],
});

module.exports = mongoose.model('Dress', DressSchema);
