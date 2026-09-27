const express = require('express');
const router = express.Router();

const autentifikacija = require('../middleware/auth');
const { checkToken } = require('../controllers/tokenController');

router.get('/proveri-token', autentifikacija, checkToken);

module.exports = router;
