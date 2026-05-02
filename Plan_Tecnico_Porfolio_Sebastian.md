# PLAN TÉCNICO — Portafolio Sebastian Montes de Oca Enriquez

> Backend Software Engineer · AI Systems · Mexico City  
> Versión 2.1 · Abril 2026

---

## 1. Contexto del proyecto

Este documento define el plan técnico completo para el desarrollo del portafolio personal de Sebastian Montes de Oca Enriquez, Backend Software Engineer con 2.5 años de experiencia especializado en Java, Spring Boot y sistemas de IA aplicada.

**Objetivos:**
- Proyectar el perfil técnico hacia un rol de AI Software Engineer en big tech en un horizonte de 6 meses.
- Demostrar en vivo arquitectura multi-agente con Google ADK Java + A2A mediante un sistema conversacional funcional embebido en el portafolio.

### 1.1 Identidad visual

| Elemento | Decisión |
|----------|----------|
| Temática | Club Necaxa (fútbol mexicano) — estadio, pasto con patrones de corte diagonal |
| Paleta primaria | `#CC0000` (Rojo Necaxa) · `#0A0A0A` (fondo) · `#FFFFFF` (texto) |
| Tipografía | Space Grotesk (headings) · JetBrains Mono (labels técnicos) |
| Estética | Dark theme premium. Champions League broadcast overlay — no fan page |
| Tagline | "Construyendo el backend que lleva los sistemas de IA a producción" |

---

## 2. Arquitectura del sistema

Sistema compuesto por 4 proyectos/servicios independientes, desplegados en un VPS de DigitalOcean con Nginx como proxy inverso.

Nota de alcance: este repositorio implementa únicamente `orchestrator`. `profile-agent`, `contact-agent` y `frontend` viven fuera de este repo para mantener servicios separados y comunicación A2A real por puerto.

```
portfolio/
├── orchestrator/      ← Spring Boot :8080 (público via Nginx)
├── profile-agent/     ← Spring Boot :9091 (interno)
├── contact-agent/     ← Spring Boot :9092 (interno)
└── frontend/          ← React + Vite
```

### 2.1 Infraestructura — VPS DigitalOcean

**Recursos:** 4 núcleos · 16 GB RAM · 200 GB almacenamiento

| Componente | Tecnología | Detalle |
|------------|------------|---------|
| Proxy inverso | Nginx | SSL termination — solo expone :8080 al exterior |
| Frontend | Nginx sirve build estático | `/` → React build |
| Orchestrator | Spring Boot :8080 | `/api/*` → OrchestratorAgent |
| Profile Agent | Spring Boot :9091 | Solo accesible internamente |
| Contact Agent | Spring Boot :9092 | Solo accesible internamente |
| Cache/Estado | Redis 7 Alpine | Sesiones, rate limiting, TTL configurable |
| SSL | Certbot / Let's Encrypt | Auto-renovación |
| Containers | Docker + Docker Compose | 5 servicios orquestados |

### 2.2 Stack tecnológico

#### Frontend

| Librería / Tool | Propósito |
|-----------------|-----------|
| React + Vite | SPA base — build rápido, HMR en dev |
| Tailwind CSS | Design system Necaxa (tokens de color, tipografía) |
| React Router | Navegación SPA — scroll a secciones con smooth scroll |
| Axios | HTTP client al orchestrator — interceptores para 429 |
| i18next | Selector ES/EN — español como idioma principal |

#### Backend — por servicio

| Dependencia | Orchestrator | Profile Agent | Contact Agent |
|-------------|:---:|:---:|:---:|
| Java 21 | ✓ | ✓ | ✓ |
| Spring Boot 4.x | ✓ | ✓ | ✓ |
| Google ADK Java 1.0.0 | ✓ | ✓ | ✓ |
| Spring Data Redis + Bucket4j | ✓ | — | — |
| Spring Mail | — | — | ✓ |
| Gemini Flash (via ADK) | ✓ | ✓ | ✓ |

---

## 3. Secciones del portafolio

Flujo: **Hero → Proyectos → Contacto**

### 3.1 Hero

| Elemento | Contenido |
|----------|-----------|
| Nav bar | `SEB.DEV — MEX` \| PROYECTOS · CONTACTO · [ES \| EN] |
| Nombre | Sebastian (blanco) / Montes de Oca Enriquez (rojo) |
| Título | `BACKEND SOFTWARE ENGINEER` (monospace) |
| Tagline | "Construyendo el backend que lleva los sistemas de IA a producción" |
| CTAs | VER PROYECTOS (rojo filled) · GITHUB (outline) |
| Stats bar | 3.5 Years Experience · 4+ AI Integrations · Java/Spring · MX/CDMX |
| Texture | Diagonal grass mowing stripes animadas — `position: fixed`, toda la página |

### 3.2 Proyectos

Tres proyectos. Contact Agent tiene jerarquía visual `FEATURED`.

---

#### 🔴 Contact Agent — Sistema multi-agente `[FEATURED — EN PRODUCCIÓN]`

| Campo | Valor |
|-------|-------|
| Jerarquía visual | Card destacada sobre Agent Lab y SupportOps |
| Tipo | Sistema multi-agente en producción real — interactuable en el portafolio mismo |
| Descripción | OrchestratorAgent + ProfileAgent + ContactAgent comunicados via A2A (Google ADK Java 1.0.0). Un único chat flotante, tres agentes especializados. |
| Stack | Java · Spring Boot · Google ADK 1.0.0 · A2A Protocol · Gemini Flash · Redis · Docker · Nginx |
| Diferenciador | No es un proyecto descrito — está corriendo en este servidor ahora mismo |
| Vínculo UI | CTA "Habla con el agente" abre el widget flotante |
| Status badge | 🟢 EN PRODUCCIÓN |

> **Narrativa de los 3 proyectos como progresión:**
> - **Agent Lab** → aprendiendo a construir agentes (laboratorio base)
> - **SupportOps** → agente operativo para negocio real (contact center)
> - **Contact Agent** → sistema multi-agente en producción, visible, funcional

---

#### Agent Lab — Agentes con Java & Spring AI `[EN DESARROLLO]`

| Campo | Valor |
|-------|-------|
| Stack | Java · Spring Boot · Spring AI · Docker · OpenAI API |
| Progress | Fase 0 — Base Mínima (1 de 6 fases) |
| Arquitectura | `HTTP Request → AgentChatController → AgentChatUseCase → AgentModelClient → Spring AI Adapter → LLM` |

---

#### SupportOps — Agente de Soporte por WhatsApp `[EN DESARROLLO]`

| Campo | Valor |
|-------|-------|
| Stack | Java · Spring Boot · Gemini API · WhatsApp Web · Google Calendar · Gmail |
| Flujo | `WhatsApp → Whitelist Auth → Gemini → Confirmación → Calendar → Email → Seguimiento` |
| Status | 🟡 Autenticación · 🟡 Interpretación · ⚫ Calendarización · ⚫ Notificación · ⚫ Seguimiento |

---

### 3.3 Contacto

- Email — con botón de copiar al portapapeles
- LinkedIn — apertura directa al perfil
- Chat widget flotante (ver sección 4)

---

## 4. Arquitectura multi-agente

### 4.1 Visión general

El Orchestrator es el único punto de entrada externo. Antes de delegar cualquier mensaje, ejecuta en orden: control de sesión → rate limiting → clasificación de intención.

```
[React Chat Widget — flotante en todo el sitio]
         | POST /api/agent/chat {sessionId, message, section}
         |
[1. SessionManager] — carga/crea sesión en Redis
[2. RateLimitFilter] — ¿tiene tokens disponibles?
         | NO + intent != CONTACT → respuesta del guardián
         | SÍ
[3. IntentClassifier] — Gemini Flash, 1 token
         |                          |
    A2A JSON-RPC              A2A JSON-RPC
         |                          |
[ProfileAgent :9091]      [ContactAgent :9092]
 RAG sobre knowledge/      Captura datos,
 Responde sobre Sebastian  envía correo
```

### 4.2 Los tres agentes

| Agente | Puerto | Modelo | Responsabilidad |
|--------|--------|--------|-----------------|
| OrchestratorAgent | `:8080` (público) | Gemini Flash | Sesiones, rate limiting, clasificación de intención, delegación via A2A |
| ProfileAgent | `:9091` (interno) | Gemini Flash | RAG sobre knowledge base — responde quién es Sebastian, stack, proyectos, decisiones técnicas |
| ContactAgent | `:9092` (interno) | Gemini Flash | Captura nombre, empresa, contacto y mensaje — envía correo al completar |

### 4.3 SessionManager — estado de orquestación en Redis

El Orchestrator mantiene dos capas de estado en Redis, separadas por responsabilidad:

```
session:{sessionId} → {
  currentAgent: "PROFILE" | "CONTACT" | null,
  messageCount: 23,
  contactCaptureStarted: false,
  createdAt: timestamp
}
TTL: 2 horas
```

**Por qué dos capas de sesión distintas:**

| Capa | Dónde vive | Qué guarda |
|------|------------|------------|
| Estado de orquestación | Redis (Orchestrator) | Conteo de mensajes, agente activo, si inició captura, TTL de sesión |
| Contexto conversacional del LLM | ADK InMemorySessionService (sub-agentes) | Historial de turnos, estado del flujo interno, datos capturados parcialmente |

El Orchestrator necesita saber cuántos mensajes lleva el usuario **antes** de invocar al ADK. Son responsabilidades distintas en capas distintas.

### 4.4 Rate limiting — diseño generoso

El límite es generoso a propósito: el objetivo es que los visitantes puedan explorar el perfil con comodidad antes de alcanzarlo.

| Parámetro | Valor |
|-----------|-------|
| Dónde vive | OrchestratorAgent — único punto de entrada externo |
| Estrategia | IP + sessionId combinado (cookie anónima) |
| Clave Redis | `rl:{ip}:{sessionId}` |
| **Límite por sesión** | **40 mensajes por hora** |
| **Límite por IP** | **8 sesiones distintas por hora** |
| TTL en Redis | 1 hora — auto-reset |
| Algoritmo | Token Bucket via Bucket4j |

### 4.5 El guardián — comportamiento al exceder el límite

El comportamiento al exceder el límite **depende de la intención del mensaje**. Nunca se bloquea una conversión.

```
Usuario excede límite + intención == CONTACT
→ Procesar normalmente — nunca bloquear una conversión

Usuario excede límite + intención == PROFILE o UNCLEAR
→ Respuesta del guardián:
  "Has explorado bastante sobre Sebastian y sus proyectos 😊
   Si quieres conocer más o tienes algo en mente,
   puedo avisarle que se ponga en contacto contigo.
   ¿Te gustaría dejarle un mensaje?"
```

> **Regla crítica:** si alguien exploró 40 mensajes sobre el perfil de Sebastian y en ese momento decide contactarlo, ese es exactamente el momento de mayor intención. Bloquearlo sería el peor error de UX posible.

### 4.6 Flujo completo con control de sesión y guardián

```
[Mensaje entrante]
        |
[SessionManager] — carga sesión, incrementa messageCount
        |
[RateLimitFilter]
        | messageCount > 40 Y intent != CONTACT
        → "¿Te gustaría que Sebastian se ponga en contacto contigo?"
        |
        | tokens disponibles
[IntentClassifier] — Gemini Flash, prompt mínimo de 1 token
        |
        | PROFILE         | CONTACT          | UNCLEAR
        |                 |                  |
[ProfileAgent]   [ContactAgent]   [Orchestrator responde
 :9091 via A2A    :9092 via A2A    directamente pidiendo
                                   clarificación]
```

### 4.7 OrchestratorAgent — routing de intención

Prompt mínimo de clasificación — responde **un solo token** (~200-300ms):

```
Clasifica este mensaje en una sola palabra:
- PROFILE : preguntas sobre Sebastian
- CONTACT : quiere contactar o dejar mensaje
- UNCLEAR : no está claro

Mensaje: "{input}"
Responde SOLO: PROFILE | CONTACT | UNCLEAR
```

### 4.8 ProfileAgent — RAG sobre knowledge base

Nunca alucina. Si la información no está en los documentos → responde que no tiene esa info y sugiere contactar directamente.

```
knowledge/
├── bio.md              ← quién es Sebastian, objetivo, contexto de carrera
├── stack.md            ← tecnologías, nivel real, por qué las usa, en qué contexto
├── agent-lab.md        ← qué es, por qué lo construyó, arquitectura, decisiones, aprendizajes
├── supportops.md       ← problema de negocio, arquitectura, trade-offs, estado actual
├── contact-agent.md    ← ADK, A2A, Gemini Flash, por qué estas decisiones, cómo funciona
└── faqs.md             ← preguntas frecuentes con respuestas reales de Sebastian
```

**Estructura de cada archivo:**
```markdown
## Qué es
## Por qué lo construí
## Cómo funciona
## Decisiones técnicas
## Lo que aprendí
```

### 4.9 ContactAgent — captura estructurada

| Paso | Campo | Comportamiento del agente |
|------|-------|--------------------------|
| 1 | Nombre | Saluda y pide nombre completo |
| 2 | Empresa | Confirma nombre y pregunta organización o proyecto |
| 3 | Contacto | Pregunta medio preferido: email o LinkedIn |
| 4 | Mensaje | Pregunta qué le quiere comunicar a Sebastian |
| 5 | Confirmación | Resume los 4 datos y pide confirmación antes de enviar |
| 6 | Cierre | Envía correo, confirma al visitante, emite `CAPTURE_COMPLETE` |

### 4.10 Chat widget — UX flotante con chips contextuales

Widget tipo WhatsApp visible en todo el portafolio. Chips cambian según la sección activa:

| Sección activa | Chips sugeridos |
|----------------|-----------------|
| Hero | "¿Qué tecnologías dominas?" · "¿En qué proyectos trabajas?" |
| Proyectos | "¿Cómo funciona el Agent Lab?" · "¿Qué es A2A en el Contact Agent?" |
| Contacto | "Quiero dejarle un mensaje" · "¿Cómo puedo contactar a Sebastian?" |

> Los chips son shortcuts — al clickearlos se envían como mensaje normal al orchestrator.

### 4.11 Estructura de servicios

```
portfolio/
├── frontend/                              ← React + Vite
│
├── orchestrator/                          ← Spring Boot :8080
│   └── src/main/java/com/sebastian/agent/orchestrator/
│       ├── api/
│       │   ├── ChatController.java
│       │   └── dto/  ChatRequest, ChatResponse
│       ├── application/
│       │   └── OrchestratorUseCase.java
│       └── infrastructure/
│           ├── ai/        OrchestratorAgentFactory.java
│           ├── a2a/       ProfileAgentClient.java
│           │              ContactAgentClient.java
│           ├── session/   SessionManager.java          ← Redis
│           ├── ratelimit/ RateLimitFilter.java
│           │              RateLimitService.java        ← Redis + Bucket4j
│           │              GuardianResponder.java       ← respuesta al exceder límite
│           └── config/    CorsConfig, RateLimitConfig
│
├── profile-agent/                         ← Spring Boot :9091
│   └── src/main/java/com/seb/profile/
│       ├── infrastructure/
│       │   ├── ai/  ProfileAgentFactory.java
│       │   ├── rag/ KnowledgeBaseLoader.java
│       │   └── a2a/ ProfileA2aServer.java
│       └── knowledge/
│           ├── bio.md  stack.md  agent-lab.md
│           └── supportops.md  contact-agent.md  faqs.md
│
└── contact-agent/                         ← Spring Boot :9092
    └── src/main/java/com/seb/contact/
        ├── infrastructure/
        │   ├── ai/   ContactAgentFactory.java
        │   ├── mail/ SmtpContactNotifier.java
        │   └── a2a/  ContactA2aServer.java
        └── domain/ports/
            └── ContactNotifier.java       ← interface
```

---

## 5. Plan de construcción por fases

> Sub-agentes primero — el orchestrator no puede probar el routing sin que ProfileAgent y ContactAgent estén corriendo.

| Fase | Qué construir | Resultado |
|------|---------------|-----------|
| 1 | Monorepo + 3 scaffolds Spring Boot | Base compilable: orchestrator, profile-agent, contact-agent |
| 2 | ContactAgent — ADK + A2AServer :9092 | Agente de captura funcionando, expuesto via A2A |
| 3 | ProfileAgent — ADK + RAG + A2AServer :9091 | Agente de perfil con knowledge base, expuesto via A2A |
| 4 | OrchestratorAgent — routing + RemoteA2aAgent | Orquestador conecta ambos sub-agentes via A2A |
| 5 | SessionManager + RateLimitFilter + GuardianResponder | Control completo: sesiones, límite generoso y guardián |
| 6 | SmtpContactNotifier en ContactAgent | Cierre del flujo — email real al completar captura |
| 7 | React + Vite scaffold + Chat widget flotante | Frontend con widget visible en todas las secciones |
| 8 | Chips de sugerencias contextuales por sección | UX completa — shortcuts según sección activa |
| 9 | i18next ES/EN | Último — no bloquea nada del flujo principal |
| 10 | Docker Compose 3 servicios + Nginx + VPS | Deploy completo en DigitalOcean |

---

## 6. Docker Compose — desarrollo local

```yaml
version: '3.8'
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  contact-agent:
    build: ./contact-agent
    ports:
      - "9092:9092"
    environment:
      - GOOGLE_API_KEY=${GOOGLE_API_KEY}
      - MAIL_USERNAME=${MAIL_USERNAME}
      - MAIL_PASSWORD=${MAIL_PASSWORD}

  profile-agent:
    build: ./profile-agent
    ports:
      - "9091:9091"
    environment:
      - GOOGLE_API_KEY=${GOOGLE_API_KEY}

  orchestrator:
    build: ./orchestrator
    ports:
      - "8080:8080"
    environment:
      - SPRING_REDIS_HOST=redis
      - GOOGLE_API_KEY=${GOOGLE_API_KEY}
      - PROFILE_AGENT_URL=http://profile-agent:9091
      - CONTACT_AGENT_URL=http://contact-agent:9092
      - RATE_LIMIT_MAX_MESSAGES=40
      - RATE_LIMIT_MAX_SESSIONS_PER_IP=8
    depends_on:
      - redis
      - profile-agent
      - contact-agent

  frontend:
    build: ./frontend
    ports:
      - "5173:5173"
    environment:
      - VITE_API_URL=http://localhost:8080
```

---

## 7. Decisiones arquitectónicas clave

| Decisión | Elegido | Por qué |
|----------|---------|---------|
| Modelo del agente | Gemini Flash via ADK | ~$0.001/conv · sin GPU · latencia < 1s |
| vs Ollama local | Descartado | Sin GPU: 15-40s latencia en CPU. Inaceptable en UX |
| Routing del Orchestrator | Gemini Flash — clasificación de 1 token | Mínima latencia. Prompt corto, respuesta `PROFILE\|CONTACT\|UNCLEAR` |
| A2A vs sub-agentes locales | **A2A real en puertos separados** | Demuestra microservicios de agentes reales. Arquitectura big tech |
| Sesiones y rate limiting | Orchestrator — único punto de entrada externo | Es el único que ve cada mensaje antes de cualquier sub-agente |
| Rate limit generoso | 40 mensajes/sesión · 8 sesiones/IP | Exploración cómoda del perfil antes de alcanzar el límite |
| Guardián inteligente | Bloquea PROFILE, nunca bloquea CONTACT | El momento de mayor intención de contacto es justo al límite — no bloquearlo |
| Dos capas de sesión | Redis (orquestación) + ADK InMemory (LLM) | Responsabilidades distintas: control de flujo vs contexto conversacional |
| Knowledge base | Markdown por proyecto con profundidad técnica | El agente responde qué, por qué y cómo — no solo describe superficialmente |
| Chat widget | Flotante en todo el sitio con chips por sección | UX tipo WhatsApp widget. Chips contextuales facilitan primera interacción |
| ADK en servicios | Solo en capa infraestructura | Controllers y UseCases no dependen de ADK — provider-agnostic por diseño |

---

## 8. Próximos pasos — Fase 1

1. Consolidar el contrato del Orchestrator: DTO HTTP, enums de dominio, respuesta uniforme y metadata futura.
2. Mantener tests unitarios y slice tests para sesión, rate limiting, routing y normalización de respuesta.
3. Agregar observabilidad mínima en Orchestrator: sessionId, intención, agente destino, estado de rate limit y tiempos.
4. Preparar `AgentClient` para delegación remota A2A sin acoplar el caso de uso a detalles del proveedor.
5. Implementar los servicios `profile-agent` y `contact-agent` en proyectos separados cuando el contrato del Orchestrator esté estable.
6. Validar la ejecución integrada en el VPS, no como flujo local por defecto.

---

> `● SEB.DEV — MEX · 2026`
