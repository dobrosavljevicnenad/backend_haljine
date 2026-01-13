const http = require('http');
const app = require('./app');

const server = http.createServer(app);

// Start servera
const PORT = process.env.PORT || 3000;
server.listen(PORT, () => console.log(`🚀 Server radi na portu ${PORT}`));
