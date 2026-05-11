# ── Stage 1: Build Angular app ───────────────────────────────────────
FROM node:20-alpine AS build
WORKDIR /app

# Install deps first (cached layer — only re-runs if package.json changes)
COPY package*.json ./
RUN npm ci --legacy-peer-deps

# Copy source and build for production
COPY . .
RUN npm run build -- --configuration production

# ── Stage 2: Serve with nginx ─────────────────────────────────────────
FROM nginx:1.27-alpine

# Angular 17+ builder outputs to dist/<project>/browser/
COPY --from=build /app/dist/yallatn/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
