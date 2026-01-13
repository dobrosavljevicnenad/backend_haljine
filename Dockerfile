
# 📦 Baza: node image sa LTS verzijom
FROM node:18

# 📁 Radni direktorijum unutar kontejnera
WORKDIR /app

# 📄 Kopiraj package fajlove i instaliraj zavisnosti
COPY package*.json ./
RUN npm install

# 📄 Kopiraj ostatak aplikacije (uklj. app.js, .env, itd.)
COPY . .

# 🌍 Izloži port (npr. 3000 ili šta koristiš u app.js)
EXPOSE 3000

# ▶️ Komanda za pokretanje aplikacije
CMD ["node", "app.js"]
