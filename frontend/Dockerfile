# Étape 1 : Construire l'application Angular
# Nouvelle ligne (version compatible Angular)
FROM node:20-alpine AS build
WORKDIR /app
# Copier les fichiers de dépendances et les installer
COPY package*.json ./
RUN npm ci
# Copier le reste du code et construire l'app pour la production
COPY . .
RUN npm run build --prod

# Étape 2 : Servir avec Nginx
FROM nginx:alpine
# Copier les fichiers construits depuis l'étape 1
COPY --from=build /app/dist/frontend /usr/share/nginx/html
# Exposer le port 80
EXPOSE 80
# Commande par défaut de Nginx
CMD ["nginx", "-g", "daemon off;"]
