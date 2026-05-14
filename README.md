# Garagem System

Backend de gerenciamento de estacionamento em Kotlin 2.1.21 / Java 21 / Spring Boot 3.5.5, organizado em
arquitetura hexagonal (Ports & Adapters) com três módulos Gradle:

```
garagem-system/        ← raiz (build settings + orquestração)
├── core/              ← domínio puro: entidades, portas (interfaces) e use cases. Sem Spring.
├── dataprovider/      ← adapters: JPA/MySQL + cliente HTTP do simulador. Implementa as portas do core.
└── app/               ← Spring Boot: controllers REST, wiring de beans, bootstrap.
```

A regra "implementações acopladas via interface" é aplicada estritamente: o `core` define
`SectorRepository`, `SpotRepository`, `ParkingSessionRepository` e `GarageConfigProvider`
como interfaces, e o módulo `dataprovider` fornece os adapters concretos (`SectorRepositoryAdapter`,
`SpotRepositoryAdapter`, `ParkingSessionRepositoryAdapter`, `GarageSimClient`). O `app` só monta
o grafo de dependências.

## Como rodar

### 1. Subir o simulador

```bash
docker run -d --network="host" cfontes0estapar/garage-sim:1.0.0
```

### 2. Subir o MySQL

```bash
docker compose up -d
```

### 3. Subir a aplicação

```bash
./gradlew :app:bootRun
```

A aplicação escuta em `http://localhost:3003` (porta exigida pelo webhook).
Variáveis opcionais:

- `GARAGE_SIM_BASE_URL` (default `http://localhost:3000`)
- `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DB`, `MYSQL_USER`, `MYSQL_PASSWORD`
- `SPRING_PROFILES_ACTIVE` (use `h2` para rodar sem MySQL)

### Testes

```bash
./gradlew test
```

## Endpoints

### `POST /webhook`

Recebe eventos `ENTRY`, `PARKED` e `EXIT` do simulador, sempre respondendo `200 OK`.

### `GET /revenue?sector={sector}&date={yyyy-MM-dd}`

```json
{
  "amount": 18.00,
  "currency": "BRL",
  "timestamp": "2025-01-01T12:00:00.000Z"
}
```

## Regras implementadas

- **Carga inicial**: ao iniciar, busca `GET /garage` no simulador e popula setores/vagas.
- **ENTRY**: cria a sessão. Lotação 100% global -> entrada recusada. O multiplicador
  de preço dinâmico é congelado no momento da entrada conforme a lotação total:
  `<25% → 0.9`, `<50% → 1.0`, `<75% → 1.1`, `<100% → 1.25`.
- **PARKED**: localiza a vaga por `(lat,lng)`, valida que o setor não está em 100%,
  marca a vaga como ocupada e congela o `basePrice` daquele setor na sessão.
- **EXIT**: libera a vaga e cobra `ceil(minutos/60) * basePrice * multiplicador`,
  com os 30 primeiros minutos gratuitos.
- **Receita por setor/data**: soma `amountCharged` das sessões com `exit_time` no dia.

