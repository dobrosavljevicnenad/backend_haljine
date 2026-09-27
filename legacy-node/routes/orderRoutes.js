const express = require('express');
const router = express.Router();

const autentifikacija = require('../middleware/auth');
const {
  orderOne,
  getOrders,
  orderMany,
  deleteOrderGroupByScreenshot,
} = require('../controllers/orderController');

router.post('/naruci/:id', autentifikacija, orderOne);
router.get('/narudzbine', autentifikacija, getOrders);
router.post('/naruci-vise', autentifikacija, orderMany);
router.delete('/narudzbine/grupa/:screenshotId', autentifikacija, deleteOrderGroupByScreenshot);

module.exports = router;
