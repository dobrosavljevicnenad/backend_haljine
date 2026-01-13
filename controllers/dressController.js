const Dress = require('../models/Dress');
const cloudinary = require('../config/cloudinary');
const getCloudinaryPublicId = require('../utils/getCloudinaryPublicId');

exports.getAllDresses = async (req, res) => {
  const haljine = await Dress.find();
  res.json(haljine);
};

exports.createDress = async (req, res) => {
  const nova = new Dress(req.body);
  await nova.save();
  res.json({ message: 'OK' });
};

exports.updateDress = async (req, res) => {
  try {
    await Dress.findByIdAndUpdate(req.params.id, req.body, { new: true });
    res.json({ message: 'Izmenjeno' });
  } catch (err) {
    res.status(500).json({ error: 'Greška pri izmeni' });
  }
};

exports.deleteDress = async (req, res) => {
  try {
    const haljina = await Dress.findById(req.params.id);

    if (!haljina) return res.status(404).json({ error: 'Haljina nije pronađena' });

    const publicId = getCloudinaryPublicId(haljina.slika);

    console.log(publicId);

    if (publicId) {
      const cloudinaryResponse = await cloudinary.uploader.destroy(publicId);
      console.log('🧹 Cloudinary response:', cloudinaryResponse);
    }

    await Dress.findByIdAndDelete(req.params.id);

    res.json({ message: 'Haljina i slika su uspešno obrisane.' });
  } catch (err) {
    console.error('❌ Greška pri brisanju haljine:', err);
    res.status(500).json({ error: 'Greška pri brisanju haljine ili slike' });
  }
};

exports.addQuantity = async (req, res) => {
  const { boja, kolicina } = req.body;

  try {
    const result = await Dress.updateOne(
      { _id: req.params.id, 'lager.boja': boja },
      { $inc: { 'lager.$.kolicina': kolicina } }
    );

    if (result.modifiedCount === 0) {
      await Dress.updateOne(
        { _id: req.params.id },
        { $push: { lager: { boja, kolicina } } }
      );
    }

    res.json({ message: 'Količina ažurirana' });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Greška u dodavanju količine' });
  }
};

exports.deleteColor = async (req, res) => {
  const { id } = req.params;
  const { boja } = req.body;

  if (!boja) return res.status(400).send({ error: 'Boja nije prosleđena' });

  try {
    const haljina = await Dress.findById(id);
    if (!haljina) return res.status(404).send({ error: 'Haljina nije pronađena' });

    haljina.lager = haljina.lager.filter((entry) => entry.boja !== boja);
    await haljina.save();

    res.send({ message: 'Boja obrisana' });
  } catch (err) {
    console.error(err);
    res.status(500).send({ error: 'Greška na serveru' });
  }
};
