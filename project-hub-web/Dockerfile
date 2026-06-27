FROM node:22-bookworm-slim AS build
WORKDIR /app
COPY package.json package-lock.json yarn.lock ./
ARG TARGETARCH
RUN npm ci && \
    case "$TARGETARCH" in \
      arm64) npm install @rolldown/binding-linux-arm64-gnu lightningcss-linux-arm64-gnu --no-save ;; \
      amd64) npm install @rolldown/binding-linux-x64-gnu lightningcss-linux-x64-gnu --no-save ;; \
      *) echo "Unsupported TARGETARCH: $TARGETARCH" && exit 1 ;; \
    esac
COPY . .
ARG VITE_API_URL=http://localhost:8090
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
