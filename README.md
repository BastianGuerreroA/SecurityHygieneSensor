# SecurityHygieneSensor

<p align="center">
  <strong>Sensor móvil de higiene y postura de seguridad del dispositivo para el ecosistema LifeSync-Games (LSG)</strong><br>
  <em>Desarrollado en Android nativo (Kotlin) — Universidad de Santiago de Chile (USACH)</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9%2B-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Plataforma-Android%208.0%2B%20(API%2026%2B)-3DDC84?logo=android&logoColor=white" alt="Plataforma Android">
  <img src="https://img.shields.io/badge/Arquitectura-MVVM%20%2B%20LiveData-blue" alt="Arquitectura MVVM">
  <img src="https://img.shields.io/badge/Integraci%C3%B3n-LSG%20Framework-blue" alt="LSG Framework">
  <img src="https://img.shields.io/badge/Licencia-Educativa%20%2F%20USACH-orange" alt="Licencia">
</p>

---

## Descripción General

**SecurityHygieneSensor** es una aplicación móvil nativa para Android diseñada para auditar y evaluar la **higiene de seguridad** del dispositivo del usuario.

El sensor opera como un componente externo integrado en el marco de trabajo **LifeSync-Games (LSG)** de la Universidad de Santiago de Chile (USACH). Su función principal es transformar métricas tangibles del entorno del dispositivo en puntuaciones que alimentan en tiempo real el **Perfil Multidimensional** del jugador, vinculando las prácticas de seguridad reales del usuario con mecánicas y ventajas dentro de los videojuegos, como **CyberSwipe**.

El diagnóstico evalúa cuatro pilares esenciales mediante servicios del sistema de Android, totalizando un puntaje máximo de **100 puntos**:

- **Bloqueo de Pantalla Seguro (40 pts):** Verifica si el dispositivo cuenta con un mecanismo de protección activo (PIN, patrón o contraseña) mediante `KeyguardManager.isDeviceSecure`, impidiendo el acceso físico no autorizado.
- **Autenticación Biométrica Fuerte (15 pts):** Detecta la disponibilidad y enrolamiento activo de hardware biométrico de clase 3 (huella dactilar o reconocimiento facial) mediante `BiometricManager.canAuthenticate(BIOMETRIC_STRONG)`.
- **Nivel de Parche de Seguridad del SO (30 pts):** Consulta `Build.VERSION.SECURITY_PATCH` para determinar si el sistema operativo cuenta con actualizaciones de seguridad recientes (lanzadas en los últimos 3 meses).
- **Restricción de Orígenes Desconocidos (15 pts):** Comprueba a través de `Settings.Secure.INSTALL_NON_MARKET_APPS` que la instalación de paquetes APK de fuentes no oficiales esté bloqueada para mitigar el ingreso de malware.

### Clasificación del Estado de Seguridad

| Rango de Puntaje | Porcentaje | Nivel de Riesgo | Indicador Visual |
| :--- | :--- | :--- | :--- |
| **75 – 100 pts** | 75% – 100% | **SEGURO** | Verde (`#4CAF50`) |
| **40 – 74 pts** | 40% – 74% | **RIESGO MEDIO** | Ámbar (`#FF9800`) |
| **0 – 39 pts** | 0% – 39% | **RIESGO ALTO** | Rojo (`#E53935`) |

---

## Características Principales

- **Auditoría Local No Invasiva:** Inspección directa del entorno del dispositivo mediante llamadas nativas de bajo nivel del sistema Android, sin necesidad de permisos intrusivos ni acceso a datos personales del usuario.
- **Ventana de Enfriamiento (Cooldown) de 24 Horas:** Para fomentar hábitos consistentes y prevenir la explotación o saturación de puntos, el sistema impone un límite de un escaneo diario. La fecha y hora se persisten localmente en `SharedPreferences` y la interfaz presenta un temporizador regresivo dinámico `HH:MM:SS`.
- **Auto-vinculación de Sensores LSG:** Al iniciar sesión o escanear, el cliente consulta los endpoints del jugador. Si el sensor de Higiene y Seguridad (`sensor_endpoint_id: 3`) no está vinculado, el cliente gestiona automáticamente el registro y enlace con `lsg-core-api` sin requerir intervención manual.
- **Gestión Segura de Sesión y Renovación Automática:** Conexión con `lsg-auth` mediante JWT. Monitorea la vigencia del token (`/token/remaining`) y lo renueva de forma transparente (`/token/refresh`) si restan menos de 5 minutos, protegiendo las peticiones subsiguientes.
- **Telemetría Dual (Ingesta + Ajuste de Perfil):** Tras completar el escaneo, la aplicación realiza un envío sincronizado:
  1. **Ingesta técnica (`/ingest/`):** Transmite un objeto JSON detallado con el estado de cada componente (`screen_lock_enabled`, `biometric_enabled`, `unknown_sources_enabled`, etc.).
  2. **Ajuste de puntos (`/points-adjust/`):** Acredita los puntos obtenidos a la **Dimensión Mental** (`attribute_id: 4`) del jugador para el videojuego asignado (`videogame_id: 14`).
- **Interfaz Reactiva con Animaciones Fluidas:** Implementación limpia en arquitectura MVVM, anillo de progreso animado (`ProgressBar` circular interpolado), tarjetas de estado con código de color dinámico y mensajes explicativos por componente.

---

## Requisitos del Sistema

### Para Usuarios (Dispositivo Móvil Android)

| Componente | Requisito Mínimo | Requisito Recomendado |
| :--- | :--- | :--- |
| **Sistema Operativo** | Android 8.0 (Oreo / API 26) | Android 11.0 o superior (API 30+) |
| **Arquitectura** | ARM64 (`arm64-v8a`) / ARMv7 | ARM64 (`arm64-v8a`) |
| **Hardware Biométrico** | Opcional (evaluado con 0 pts si no existe) | Lector de huellas o reconocimiento facial compatible con Biometric Strong |
| **Memoria RAM** | 2 GB | 3 GB o superior |
| **Almacenamiento** | 50 MB libres | 100 MB libres |
| **Conexión a Internet** | Wi-Fi o Datos Móviles activa (para autenticación y sincronización LSG) | Conexión de banda ancha o 4G/5G estable |

### Para Desarrolladores (Entorno de Trabajo)

- **Entorno Integrado (IDE):** Android Studio.
- **Java Development Kit (JDK):** OpenJDK 17 o Java 11 (configurado como Gradle JDK).
- **Android SDK:**
  - `compileSdk`: 36 (Android 15 / Vanilla Ice Cream)
  - `minSdk`: 26 (Android 8.0 Oreo)
  - `targetSdk`: 36
  - Android SDK Build-Tools 36+ y Platform-Tools actualizados.
- **Sistema de Compilación:** Gradle 8.9+ con Android Gradle Plugin (AGP) compatible.

---

## Instalación y Ejecución

### Opción A: Instalación en Dispositivo Físico (Usuario / Evaluador)

1. Descarga el paquete de instalación.
2. En tu dispositivo Android, dirígete a **Ajustes > Seguridad** y habilita la opción de **Instalar aplicaciones de fuentes desconocidas** para tu explorador de archivos o navegador.
3. Abre el archivo `.apk` descargado y confirma la instalación.
4. Inicia la aplicación e introduce tus credenciales de usuario de la plataforma **LifeSync-Games (LSG)**.
5. Pulsa sobre el botón **ANALIZAR SEGURIDAD** para realizar la auditoría y enviar tus puntos al perfil de jugador.

### Opción B: Ejecución desde el Código Fuente (Desarrollador)

1. **Clonar el repositorio.**
2. **Abrir el proyecto en Android Studio.**
3. **Compilar y generar el APK.**
4. **Instalar y depurar en dispositivo o emulador.**
