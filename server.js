const http = require('http');
const app = require('./app');

const server = http.createServer(app);

const PORT = process.env.PORT || 3000;
server.listen(PORT, '0.0.0.0', () => console.log(`🚀 Server radi na portu ${PORT}`));
