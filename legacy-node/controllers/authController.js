const jwt = require('jsonwebtoken');

const SECRET_KEY = process.env.SECRET_KEY;

const korisnik = {
  username: process.env.USERNAME,
  password: process.env.PASSWORD,
};

exports.login = (req, res) => {
  const { username, password } = req.body;

  if (username === korisnik.username && password === korisnik.password) {
    const token = jwt.sign({ username }, SECRET_KEY, { expiresIn: '2h' });
    return res.json({ token });
  }

  return res.status(401).json({ error: 'Neispravno korisničko ime ili šifra' });
};
