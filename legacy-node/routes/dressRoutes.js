const express = require('express');
const router = express.Router();

const autentifikacija = require('../middleware/auth');
const {
  getAllDresses,
  createDress,
  updateDress,
  deleteDress,
  addQuantity,
  deleteColor,
} = require('../controllers/dressController');

router.get('/haljine', autentifikacija, getAllDresses);
router.post('/haljine', autentifikacija, createDress);
router.put('/haljine/:id', autentifikacija, updateDress);
router.delete('/haljine/:id', autentifikacija, deleteDress);

router.post('/dodajKolicinu/:id', autentifikacija, addQuantity);
router.post('/obrisi-boju/:id', autentifikacija, deleteColor);

module.exports = router;
