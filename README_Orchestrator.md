# README — Orchestrator Agent

## Propósito de este proyecto

Este proyecto implementa el **Orchestrator Agent** del portafolio multi-agente.

Su responsabilidad es actuar como **único punto de entrada externo** para el chat del sitio y coordinar la conversación entre agentes especializados.

Este repositorio contiene **solo el servicio Orchestrator**.  
Profile Agent, Contact Agent y frontend se desarrollan como proyectos/servicios separados para poder correr en puertos distintos y comunicarse mediante A2A real.

Este repositorio **no** existe para construir una demo rápida y frágil.
Existe para practicar y estudiar diseño real de agentes con:

- Java
- Spring Boot
- Google ADK Java
- A2A Protocol
- Redis
- Docker

La intención es aprender a construir una pieza de arquitectura que sí tendría sentido en un producto real.

---

## Rol del Orchestrator en el sistema

Dentro de la arquitectura general del portafolio, el Orchestrator:

1. recibe los mensajes del frontend
2. gestiona o recupera la sesión
3. aplica rate limiting
4. clasifica la intención del mensaje
5. decide a qué agente delegar
6. se comunica con sub-agentes vía A2A
7. devuelve una respuesta uniforme al frontend

### Sub-agentes actuales

- **Profile Agent**  
  Responde preguntas sobre Sebastian, stack, experiencia, proyectos y decisiones técnicas.

- **Contact Agent**  
  Captura intención de contacto y guía al visitante para dejar un mensaje estructurado.

### Alcance de este repositorio

Este repo no contiene el código de Profile Agent, Contact Agent ni frontend.  
Aquí solo se implementan:

- endpoint público del chat
- manejo de sesión de orquestación
- rate limiting
- clasificación de intención
- contrato interno de delegación
- clientes/adaptadores para hablar con sub-agentes remotos
- normalización de respuesta hacia el frontend

La aplicación no se levanta localmente como flujo normal de trabajo. En este repo se prioriza codear y ejecutar tests; la ejecución completa ocurre en el VPS.

---

## Regla principal de colaboración con IA / Codex

Quiero desarrollar este proyecto **yo mismo** como práctica técnica.

Eso significa que cualquier asistente de código o IA debe comportarse como:

- mentor técnico
- guía de arquitectura
- apoyo para aterrizar decisiones
- revisor de diseño
- ayuda para detectar riesgos o errores

### Lo que sí debe hacer Codex o cualquier IA
- ayudarme a bajar ideas a diseño
- sugerir paquetes, contratos y responsabilidades
- revisar si la arquitectura tiene sentido
- proponer mejoras incrementales
- explicar trade-offs
- ayudarme a depurar problemas
- ayudarme a documentar
- generar ejemplos pequeños cuando sirvan para entender

### Lo que no debe hacer por defecto
- construir el proyecto completo sin que yo lo pida
- generar módulos enteros automáticamente
- cambiar la arquitectura sin justificarlo
- agregar complejidad innecesaria
- asumir que quiero “la solución final”
- convertir esto en una demo bonita pero difícil de operar

### Regla explícita para generación de código
La IA **no debe generar código completo por defecto**.

Debe priorizar este orden:

1. explicar la arquitectura
2. definir contratos
3. sugerir estructura
4. revisar mi implementación
5. proponer ejemplos pequeños
6. generar código completo solo si yo lo pido de forma explícita

---

## Objetivo técnico del Orchestrator

Construir un orquestador limpio, entendible y extensible que sirva como base para practicar:

- routing entre agentes
- sesiones
- rate limiting
- guardián de UX
- delegación A2A
- separación entre aplicación, dominio e infraestructura
- observabilidad mínima
- evolución futura a memoria, tracing y guardrails

Este proyecto no debe depender mentalmente de “un proveedor mágico”.
Debe diseñarse por **capacidades**.

---

## Qué hace y qué no hace este proyecto

### Sí hace
- exponer endpoint HTTP para chat
- manejar sesión del visitante
- limitar abuso sin romper la UX
- clasificar intención
- delegar a Profile Agent o Contact Agent
- responder al frontend con un contrato consistente
- centralizar políticas transversales del sistema

### No debe hacer todavía
- meter lógica de negocio de los sub-agentes
- responder preguntas del perfil directamente dentro del controller
- capturar formularios completos en el orquestador
- convertirse en un “super agente” que hace todo
- almacenar memoria larga innecesaria
- mezclar A2A, prompts, sesión, redis y DTOs en una sola clase monstruo

---

## Principios de diseño

Este proyecto debe seguir estos principios:

- separación clara de responsabilidades
- bajo acoplamiento y alta cohesión
- diseño orientado a interfaces
- núcleo desacoplado de la implementación concreta
- código simple antes que “ingenioso”
- configuración externa
- mantenibilidad por encima de velocidad ciega
- observabilidad mínima desde etapas tempranas
- complejidad solo cuando exista necesidad real

---

## Decisión tecnológica actual

### Base principal
- Java
- Spring Boot
- Google ADK Java
- A2A para comunicación entre agentes
- Redis para sesión y rate limiting

### Decisión importante
Aunque usemos ADK y A2A, el proyecto **no debe depender arquitectónicamente** de detalles del proveedor en el núcleo.

El dominio y la aplicación deben depender de **interfaces internas**.

---

## Rol de ADK y A2A en este proyecto

### Google ADK
Se usará como base para trabajar con agentes y capacidades agentic en Java.

### A2A
Se usará como mecanismo de comunicación entre el Orchestrator y los sub-agentes.

Eso significa que el Orchestrator **no invoca métodos internos acoplados** de otros servicios.
Debe hablar con ellos como agentes remotos mediante contratos claros.

### Importante
ADK y A2A son parte de la infraestructura y del mecanismo de integración.

No deben contaminar el núcleo del caso de uso más de lo necesario.

---

## Flujo funcional esperado

El flujo objetivo del Orchestrator es:

1. frontend envía `sessionId`, `message` y contexto opcional de sección
2. el Orchestrator obtiene o crea la sesión
3. incrementa conteo de interacción
4. evalúa rate limiting
5. clasifica intención del mensaje
6. decide el agente destino
7. delega la solicitud vía A2A
8. normaliza la respuesta
9. devuelve respuesta al frontend

### Rutas iniciales de intención
- `PROFILE`
- `CONTACT`
- `UNCLEAR`

### Reglas esperadas
- si la intención es `PROFILE`, delegar al Profile Agent
- si la intención es `CONTACT`, delegar al Contact Agent
- si la intención es `UNCLEAR`, responder pidiendo clarificación o aplicar fallback razonable
- si se excede el límite y la intención **no** es `CONTACT`, responder con guardián
- si se excede el límite y la intención **sí** es `CONTACT`, no bloquear el flujo

---

## Guardián de UX

Este orquestador no debe bloquear contacto legítimo por una política rígida.

### Regla crítica
Si un visitante ya exploró bastante el perfil y justo entonces quiere contactar, ese es un punto de intención alta.

Bloquearlo sería una mala decisión de producto.

### Comportamiento esperado
- exceso de límite + intención `PROFILE` o `UNCLEAR` → respuesta del guardián
- exceso de límite + intención `CONTACT` → permitir flujo

---

## Arquitectura sugerida

```text
src/main/java/com/sebastian/agent/orchestrator
  api
    ChatController
    dto/
  application
    OrchestratorUseCase
  domain
    model/
    ports/
    services/
  infrastructure
    a2a/
    ai/
    session/
    ratelimit/
    config/
    web/
```

---

## Responsabilidades por capa

### API
Responsable de:
- exponer endpoints
- validar entrada
- mapear request/response HTTP
- no contener lógica de orquestación real

### Application
Responsable de:
- coordinar el caso de uso principal
- ejecutar el flujo de sesión → límite → clasificación → delegación → respuesta

### Domain
Responsable de:
- definir modelos del núcleo
- definir puertos/interfaces
- representar decisiones del sistema sin depender de frameworks

### Infrastructure
Responsable de:
- integración con Redis
- integración con A2A
- implementación del clasificador
- configuración de ADK
- adaptadores técnicos

---

## Componentes mínimos esperados

### API
- `ChatController`
- `ChatRequest`
- `ChatResponse`

### Application
- `OrchestratorUseCase`

### Domain model
- `VisitorSession`
- `ChatIntent`
- `AgentType`
- `RateLimitStatus`
- `DelegatedRequest`
- `DelegatedResponse`

### Domain ports
- `SessionRepository`
- `RateLimitPolicy`
- `IntentClassifier`
- `AgentClient`
- `GuardianResponseBuilder`

### Infrastructure
- implementación Redis de `SessionStore`
- implementación Redis/Bucket4j de `RateLimitGateway`
- implementación ADK o provider-based de `IntentClassifier`
- clientes A2A para Profile Agent y Contact Agent
- configuración de propiedades
- health checks y observabilidad mínima

---

## Contratos internos sugeridos

Este proyecto debe pensar en interfaces internas como estas:

- `SessionRepository`
- `RateLimitPolicy`
- `IntentClassifier`
- `AgentClient`
- `TracingPublisher` (futuro)
- `ConversationMemoryStore` (futuro, solo si se justifica)

La lógica del caso de uso debe depender de estos contratos, no de clases concretas de infraestructura.

---

## Endpoint inicial esperado

### POST `/api/agent/chat`

Request esperado de alto nivel:
- `sessionId`
- `message`
- `section` opcional

Response esperado de alto nivel:
- `reply`
- `intent`
- `agentUsed`
- `sessionId`
- `rateLimitStatus`
- `metadata` opcional

`rateLimitStatus` se modela internamente como enum de dominio para evitar strings mágicos en el caso de uso. En HTTP se expone como texto estable para que el frontend reciba JSON simple.

No es necesario definir el contrato final completo desde el primer día, pero sí debe mantenerse consistente y fácil de evolucionar.

---

## Sesión y estado

El Orchestrator debe tener su propia capa de estado técnico para controlar:

- agente actual
- conteo de mensajes
- si ya arrancó un flujo de contacto
- timestamps de sesión
- TTL

### Importante
Esto no es lo mismo que la memoria conversacional interna del LLM o del sub-agente.

El Orchestrator controla el estado de **orquestación**.
Los sub-agentes controlan su propio estado conversacional si lo necesitan.

No mezclar esas responsabilidades.

---

## Rate limiting

Este proyecto debe practicar control de abuso sin destruir la experiencia de usuario.

### Objetivo
- evitar spam o abuso
- proteger recursos
- permitir exploración razonable
- no bloquear una conversión legítima

### Parámetros iniciales esperados
- límite por sesión
- límite por IP
- TTL de ventana
- política diferenciada según intención

### Importante
El rate limiting es una responsabilidad transversal del Orchestrator, no de los sub-agentes.

---

## Observabilidad mínima esperada

Desde etapas tempranas, el proyecto debe registrar al menos:

- request recibido
- sessionId
- intención clasificada
- agente destino
- resultado de rate limiting
- tiempo de delegación
- errores de integración

Esto no implica meter observabilidad enterprise desde el día 1.
Implica no volar a ciegas.

---

## Manejo de errores

El Orchestrator debe fallar de forma controlada.

Escenarios que deben contemplarse:

- Redis no disponible
- sub-agente no disponible
- timeout en llamada A2A
- clasificación fallida
- respuesta inesperada de sub-agente
- request inválido

### Regla
Los errores deben ser:
- visibles en logs
- entendibles para depuración
- manejados sin romper completamente la UX cuando sea posible

---

## Orden recomendado de implementación

1. generar el scaffold base del proyecto
2. definir paquetes base
3. crear DTOs HTTP
4. definir modelos del dominio
5. definir puertos internos
6. crear `OrchestratorUseCase`
7. implementar `SessionStore`
8. implementar `RateLimitGateway`
9. implementar `IntentClassifier`
10. implementar `AgentDelegationGateway`
11. integrar A2A con los sub-agentes
12. agregar observabilidad mínima
13. probar flujo completo end-to-end

---

## Qué evaluar en cada revisión

Cuando revisemos este proyecto, debemos evaluar:

- si el controller está demasiado gordo
- si el caso de uso está coordinando demasiado o demasiado poco
- si la infraestructura está contaminando el dominio
- si estamos acoplando el orquestador a ADK/A2A más de lo necesario
- si la política de rate limiting tiene sentido de producto real
- si la experiencia de usuario sigue siendo razonable
- si el diseño permitiría crecer a tracing, memoria y guardrails sin rehacer todo

---

## Qué no queremos hacer en esta fase

Todavía no queremos:

- multi-turn memory compleja dentro del orquestador
- workflows asíncronos innecesarios
- tool calling avanzado dentro del orchestrator
- meter RAG aquí
- usar el orquestador como agente especialista
- llenar el proyecto de abstracciones vacías
- meter features “porque se ven pro” aunque no resuelvan nada

---

## Criterio de éxito de esta fase

Esta fase se considera bien construida cuando:

- existe un endpoint funcional
- el flujo de orquestación es claro
- el controller no concentra la lógica
- sesión, límite y clasificación están separadas
- la delegación a sub-agentes pasa por contratos claros
- ADK y A2A están integrados sin romper la arquitectura
- el proyecto sigue siendo entendible de extremo a extremo

---

## Cómo debe ayudar la IA durante este proyecto

La IA debe actuar como:

- mentor técnico
- guía de implementación
- revisor de arquitectura
- apoyo de depuración

### Estilo esperado
- práctico
- directo
- técnico
- sin hype
- orientado a producto real
- con foco en costos, mantenibilidad, latencia y observabilidad

### Regla final
La prioridad no es terminar rápido.

La prioridad es construir correctamente, entender el sistema y practicar desarrollo real de agentes con Java, Spring Boot, ADK y A2A.

---

## Próximo paso sugerido

Antes de implementar detalles de ADK o A2A, dejar bien definidos:

- estructura de paquetes
- DTOs HTTP
- modelos del dominio
- interfaces internas
- propiedades de configuración
- contrato uniforme de respuesta

Después de eso, avanzar con la implementación incremental.
