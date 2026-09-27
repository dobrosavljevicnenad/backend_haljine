exports.uploadImage = (req, res) => {
  if (!req.file) return res.status(400).send({ error: 'Fajl nije prosleđen' });
  res.json({ url: req.file.path });
};
