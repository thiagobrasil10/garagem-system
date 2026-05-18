# Garagem System

Backend de gerenciamento de estacionamento em **Kotlin 2.1.21 / Java 21 / Spring Boot 3.5.5**,
organizado em **arquitetura hexagonal (Ports & Adapters)** com três módulos Gradle:

```
garagem-system/        (raiz: build settings + orquestração)
├── core/              domínio puro: entidades, portas (interfaces), use cases e PricingService. Sem Spring.
├── dataprovider/      adapters: JPA/MySQL + cliente HTTP do simulador. Implementa as portas do core.
└── app/               Spring Boot: controllers REST, wiring de beans, bootstrap da garagem.
```

A regra **"implementações acopladas via interface"** é aplicada estritamente: o `core` define
`SectorRepository`, `SpotRepository`, `ParkingSessionRepository` e `GarageConfigProvider`
como interfaces, e o módulo `dataprovider` fornece os adapters concretos
(`SectorRepositoryAdapter`, `SpotRepositoryAdapter`, `ParkingSessionRepositoryAdapter`,
`GarageSimClient`). O `app` só monta o grafo de dependências.

---

## Como rodar

### 1. Subir o simulador da Estapar

**Linux:**
```bash
docker run -d --network="host" cfontes0estapar/garage-sim:1.0.0
```

**macOS / Windows** (host networking não funciona — use port mapping):
```bash
docker run -d -p 3000:3000 cfontes0estapar/garage-sim:1.0.0
```
Ou utilize o `docker-compose.yml` deste projeto, que já mapeia a porta `3000` corretamente.

### 2. Subir o MySQL (via docker-compose)

```bash
docker compose up -d
```
Ou
```bash
docker-compose compose up -d
```
(dependnendo da versão do Docker Compose instalada)

### 3. Subir a aplicação

```bash
./gradlew :app:bootRun
```

A aplicação escuta em `http://localhost:3003` (porta exigida pelo webhook do simulador).

#### Variáveis de ambiente opcionais

| Variável | Default | Descrição |
|---|---|---|
| `GARAGE_SIM_BASE_URL` | `http://localhost:3000` | URL base do simulador |
| `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DB`, `MYSQL_USER`, `MYSQL_PASSWORD` | ver `application-mysql.yml` | Conexão MySQL |
| `SPRING_PROFILES_ACTIVE` | `mysql` | Use `h2` para rodar sem MySQL |

### Testes

```bash
./gradlew test
```

---

## Endpoints da API

### `POST /webhook`

Recebe eventos `ENTRY`, `PARKED` e `EXIT` do simulador.

**Entrada na garagem (ENTRY):**
```json
{
  "license_plate": "ZUL0001",
  "entry_time": "2025-01-01T12:00:00.000Z",
  "event_type": "ENTRY"
}
```

**Entrada na vaga (PARKED):**
```json
{
  "license_plate": "ZUL0001",
  "lat": -23.561684,
  "lng": -46.655981,
  "event_type": "PARKED"
}
```

**Saída da garagem (EXIT):**
```json
{
  "license_plate": "ZUL0001",
  "exit_time": "2025-01-01T14:00:00.000Z",
  "event_type": "EXIT"
}
```

**Response (todos os casos):**
```json
{
  "status": "SUCCESS",
  "event_type": "ENTRY",
  "message": "Entrada registrada para placa ZUL0001",
  "timestamp": "2026-05-14T15:30:00Z"
}
```

| HTTP | Cenário |
|---|---|
| `200` | Evento processado com sucesso |
| `400` | Campo obrigatório ausente ou `event_type` desconhecido |
| `404` | `PARKED` em coordenada sem vaga |
| `409` | ENTRY duplicado, EXIT sem sessão, setor/garagem lotado, vaga já ocupada |

---

### `POST /plate-status`

Consulta o status de um veículo dentro da garagem.

**Request:**
```json
{ "license_plate": "ZUL0001" }
```

**Response 200:**
```json
{
  "license_plate": "ZUL0001",
  "price_until_now": 18.00,
  "entry_time": "2026-05-14T12:00:00Z",
  "time_parked": "2026-05-14T12:05:30Z"
}
```

| HTTP | Cenário |
|---|---|
| `200` | Sessão aberta encontrada |
| `400` | `license_plate` ausente/vazio |
| `404` | Nenhuma sessão aberta para a placa |

---

### `POST /spot-status`

Consulta o status de uma vaga por geolocalização.

**Request:**
```json
{ "lat": -23.561684, "lng": -46.655981 }
```

**Response 200:**
```json
{
  "occupied": true,
  "entry_time": "2026-05-14T12:00:00Z",
  "time_parked": "2026-05-14T12:05:30Z"
}
```

| HTTP | Cenário |
|---|---|
| `200` | Vaga encontrada (ocupada ou livre) |
| `400` | `lat` ou `lng` ausentes |
| `404` | Nenhuma vaga cadastrada nas coordenadas informadas |

---

### `GET /revenue`

Faturamento de um setor em uma data específica.

**Request (body JSON):**
```json
{ "date": "2026-05-14", "sector": "A" }
```

**Response 200:**
```json
{
  "amount": 109.35,
  "currency": "BRL",
  "timestamp": "2026-05-14T20:00:00Z"
}
```

| HTTP | Cenário |
|---|---|
| `200` | Sempre retorna (amount = 0 quando não há faturamento no dia/setor) |
| `400` | `date` ou `sector` ausentes |

---

### `GET /garage`

Retorna a configuração da garagem persistida (setores + vagas), recebida do simulador na inicialização.

**Response 200:**
```json
{
  "garage": [
    { "sector": "A", "base_price": 40.5, "max_capacity": 10 },
    { "sector": "B", "base_price": 4.1,  "max_capacity": 20 }
  ],
  "spots": [
    { "id": 1, "sector": "A", "lat": -23.561684, "lng": -46.655981 }
  ]
}
```

---

## Regras de negócio implementadas

### Carga inicial
Ao subir, o `GarageBootstrap` chama `GET /garage` no simulador (porta 3000) e popula
as tabelas `sectors` e `spots` no banco.

### Preço dinâmico (congelado no ENTRY)
Conforme a lotação **total** da garagem no momento da entrada:

| Lotação | Multiplicador |
|---|---|
| `< 25%` | **0.90** (desconto 10%) |
| `< 50%` | **1.00** (preço cheio) |
| `< 75%` | **1.10** (acréscimo 10%) |
| `< 100%` | **1.25** (acréscimo 25%) |
| `= 100%` | Entrada recusada — garagem fechada |

### Eventos

- **ENTRY** — cria a sessão e congela o multiplicador. Recusa entrada se a garagem está 100% lotada
  ou se já existe sessão aberta para a placa.
- **PARKED** — localiza a vaga por `(lat, lng)`, valida que o setor não atingiu 100%,
  marca a vaga como ocupada e congela o `basePrice` do setor na sessão.
- **EXIT** — libera a vaga e cobra:
  ```
  valor = ceil(minutos / 60) * basePrice * multiplicador
  ```
  Os **primeiros 30 minutos são gratuitos**. Após isso, cobra-se por hora cheia (arredondamento para cima).

### Lotação 100%
Quando o setor atinge a capacidade máxima, novos `PARKED` são recusados até que
ocorra um `EXIT`. Idem para a garagem inteira no `ENTRY`.

### Receita
Soma do `amount_charged` de todas as sessões cujo `exit_time` cai dentro do dia consultado,
filtradas pelo `sector` informado.

---

## Stack técnica

- **Linguagem:** Kotlin 2.1.21 (alvo JVM 21)
- **Framework:** Spring Boot 3.5.5 (Web MVC + Data JPA + WebFlux para o cliente HTTP)
- **Banco:** MySQL 8 (produção) / H2 (testes via profile `h2`)
- **Build:** Gradle 8.13 Kotlin DSL — multi-módulo
- **Testes:** JUnit 5 + AssertJ + Spring Boot Test (`@SpringBootTest` + `TestRestTemplate`)
- **Arquitetura:** Hexagonal (Ports & Adapters) com 3 módulos isolados

---

## Estrutura de pacotes (resumo)

```
core/src/main/kotlin/org/example/core/
├── domain/         Sector, Spot, ParkingSession, GarageConfig
├── port/           SectorRepository, SpotRepository, ParkingSessionRepository, GarageConfigProvider
├── service/        PricingService (regras de cobrança e multiplicador)
└── usecase/        LoadGarage, HandleEntry, HandleParked, HandleExit,
                    GetRevenue, GetPlateStatus, GetSpotStatus

dataprovider/src/main/kotlin/org/example/dataprovider/
├── adapter/        Implementações das portas (Spring @Component)
└── jpa/            Entities + JpaRepository

app/src/main/kotlin/org/example/app/
├── config/         UseCaseConfig (wiring), GarageBootstrap
└── web/            Controllers REST (Webhook, Garage, Revenue, PlateStatus, SpotStatus)
```

---

## Exemplos de uso (curl)

```bash
# Consultar placa
curl -X POST http://localhost:3003/plate-status \
  -H 'Content-Type: application/json' \
  -d '{"license_plate":"ZUL0001"}'

# Consultar vaga
curl -X POST http://localhost:3003/spot-status \
  -H 'Content-Type: application/json' \
  -d '{"lat":-23.561684,"lng":-46.655981}'

# Faturamento
curl -X GET http://localhost:3003/revenue \
  -H 'Content-Type: application/json' \
  -d '{"date":"2026-05-14","sector":"A"}'

# Configuração da garagem
curl http://localhost:3003/garage
```
---
## Swagger UI
A documentação interativa da API está disponível em `http://localhost:3003/swagger-ui.html`
