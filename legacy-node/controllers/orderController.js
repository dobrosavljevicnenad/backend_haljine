const mongoose = require('mongoose');
const Dress = require('../models/Dress');
const Order = require('../models/Order');
const cloudinary = require('../config/cloudinary');
const getCloudinaryPublicId = require('../utils/getCloudinaryPublicId');

exports.orderOne = async (req, res) => {
  const { boja, kolicina, screenshot } = req.body;

  const haljina = await Dress.findOne({
    _id: req.params.id,
    'lager.boja': boja,
  });

  if (!haljina) return res.status(404).send({ error: 'Haljina nije pronađena' });

  const bojaEntry = haljina.lager.find((e) => e.boja === boja);

  if (!bojaEntry || bojaEntry.kolicina < kolicina) {
    return res.status(400).send({ error: 'Nema dovoljno na lageru' });
  }

  await Dress.updateOne(
    { _id: req.params.id, 'lager.boja': boja },
    { $inc: { 'lager.$.kolicina': -kolicina } }
  );

  await Dress.updateOne(
    { _id: req.params.id },
    { $pull: { lager: { boja: boja, kolicina: { $lte: 0 } } } }
  );

  const narudzbina = {
    haljinaId: haljina._id,
    naziv: haljina.naziv,
    slika: haljina.slika,
    cena: haljina.cena,
    boja,
    kolicina,
    screenshot: screenshot || '',
  };

  await Order.create(narudzbina);

  res.json({ message: 'Naručeno' });
};

exports.getOrders = async (req, res) => {
  const narudzbine = await Order.find().sort({ vreme: -1 });
  res.json(narudzbine);
};

exports.orderMany = async (req, res) => {
  try {
    const { proizvodi, kupac } = req.body;
    if (!Array.isArray(proizvodi) || proizvodi.length === 0) {
      return res.status(400).json({ error: 'Nema proizvoda za naručiti' });
    }

    for (const p of proizvodi) {
      const haljina = await Dress.findOne({
        _id: p._id,
        'lager.boja': p.boja,
      });
      if (!haljina) {
        return res.status(404).json({
          error: `Haljina ${p.naziv} sa bojom ${p.boja} nije pronađena`,
        });
      }
      const bojaEntry = haljina.lager.find((e) => e.boja === p.boja);
      if (!bojaEntry || bojaEntry.kolicina < p.kolicina) {
        return res.status(400).json({
          error: `Nema dovoljno na lageru za ${p.naziv} (${p.boja})`,
        });
      }
    }

    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      for (const p of proizvodi) {
        await Dress.updateOne(
          { _id: p._id, 'lager.boja': p.boja },
          { $inc: { 'lager.$.kolicina': -p.kolicina } },
          { session }
        );

        await Dress.updateOne(
          { _id: p._id },
          { $pull: { lager: { boja: p.boja, kolicina: { $lte: 0 } } } },
          { session }
        );

        const novaNarudzbina = {
          haljinaId: p._id,
          naziv: p.naziv,
          slika: p.slika,
          cena: p.cena,
          boja: p.boja,
          kolicina: p.kolicina,
          screenshot: kupac.screenshot || '',
        };

        await Order.create([novaNarudzbina], { session });
      }

      await session.commitTransaction();
      session.endSession();

      res.json({ message: 'Uspešno naručeno više haljina' });
    } catch (err) {
      await session.abortTransaction();
      session.endSession();
      console.error(err);
      res.status(500).json({ error: 'Greška pri naručivanju proizvoda' });
    }
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Greška na serveru' });
  }
};

exports.deleteOrderGroupByScreenshot = async (req, res) => {
  try {
    const screenshotUrl = decodeURIComponent(req.params.screenshotId);

    const rezultat = await Order.deleteMany({ screenshot: screenshotUrl });

    const publicId = getCloudinaryPublicId(screenshotUrl);

    if (publicId) {
      const cloudinaryResponse = await cloudinary.uploader.destroy(publicId);
      console.log('🧹 Cloudinary response:', cloudinaryResponse);
    }

    res.json({
      message: `Obrisano ${rezultat.deletedCount} narudžbina i slika sa Cloudinary-a.`,
    });
  } catch (err) {
    console.error('❌ Greška pri grupnom brisanju:', err);
    res.status(500).json({ error: 'Greška pri brisanju porudžbina ili slike' });
  }
};
