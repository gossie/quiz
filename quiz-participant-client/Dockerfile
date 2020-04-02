FROM mhart/alpine-node:11 AS builder
WORKDIR /app
COPY . .
COPY .env.docker .env.production
RUN npm install react-scripts -g --silent
RUN yarn install
RUN yarn run build

FROM mhart/alpine-node
RUN yarn global add serve
WORKDIR /app
COPY --from=builder /app/build .
CMD ["serve", "-p", "80", "-s", "."]doc
