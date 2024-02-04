FROM node AS builder
WORKDIR /app
COPY . .
COPY .env.docker .env.production
RUN npm install
RUN npm run build

FROM nginx
COPY --from=builder /app/build /usr/share/nginx/html
