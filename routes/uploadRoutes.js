const express = require('express');
const router = express.Router();

const autentifikacija = require('../middleware/auth');
const upload = require('../config/multer');
const { uploadImage } = require('../controllers/uploadController');

router.post('/upload', autentifikacija, upload.single('slika'), uploadImage);

module.exports = router;
