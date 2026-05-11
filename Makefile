# ══════════════════════════════════════════════════════════════════════
#  YallaTN — Docker / K8s helper commands
#  Usage: make <target>
#  Set REGISTRY to your Docker Hub username, e.g.:
#    make push REGISTRY=myusername
# ══════════════════════════════════════════════════════════════════════

REGISTRY  ?= yourdockerhubusername
TAG       ?= latest

IMAGES := frontend backend ml-travel ml-core

# ── Local development ──────────────────────────────────────────────────
.PHONY: up
up:
	docker compose up --build -d

.PHONY: down
down:
	docker compose down

.PHONY: logs
logs:
	docker compose logs -f

.PHONY: restart
restart:
	docker compose down && docker compose up --build -d

# ── Build all images with registry tags ───────────────────────────────
.PHONY: build
build:
	docker compose build
	docker tag yallatn-frontend:latest  $(REGISTRY)/yallatn-frontend:$(TAG)
	docker tag yallatn-backend:latest   $(REGISTRY)/yallatn-backend:$(TAG)
	docker tag yallatn-ml-travel:latest $(REGISTRY)/yallatn-ml-travel:$(TAG)
	docker tag yallatn-ml-core:latest   $(REGISTRY)/yallatn-ml-core:$(TAG)

# ── Smoke-test: check all services respond ────────────────────────────
.PHONY: test
test:
	@echo "Waiting for services to be healthy..."
	@sleep 10
	@curl -sf http://localhost/            | grep -q "yallatn\|html" && echo "✅ Frontend OK"     || echo "❌ Frontend FAIL"
	@curl -sf http://localhost/api/cities  2>&1 | grep -qv "Connection refused"  && echo "✅ Backend OK"      || echo "❌ Backend FAIL"
	@curl -sf http://localhost:5050/health 2>&1 | grep -qv "Connection refused"  && echo "✅ ML-Travel OK"    || echo "⚠️  ML-Travel no /health endpoint"
	@curl -sf http://localhost:8000/health 2>&1 | grep -qv "Connection refused"  && echo "✅ ML-Core OK"      || echo "⚠️  ML-Core no /health endpoint"

# ── Push to Docker Hub ────────────────────────────────────────────────
.PHONY: push
push: build
	docker login
	docker push $(REGISTRY)/yallatn-frontend:$(TAG)
	docker push $(REGISTRY)/yallatn-backend:$(TAG)
	docker push $(REGISTRY)/yallatn-ml-travel:$(TAG)
	docker push $(REGISTRY)/yallatn-ml-core:$(TAG)
	@echo "✅ All images pushed to Docker Hub as $(REGISTRY)/yallatn-*:$(TAG)"

# ── Clean up ──────────────────────────────────────────────────────────
.PHONY: clean
clean:
	docker compose down -v --remove-orphans
	docker image prune -f
