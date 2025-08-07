FROM golang:1.23-alpine AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN go mod tidy
RUN go build -o enricher-api ./cmd/server

FROM alpine:3.20
COPY --from=builder /app/enricher-api /usr/local/bin/
EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/enricher-api"]
