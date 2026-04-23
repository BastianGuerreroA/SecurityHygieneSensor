# SecurityHygieneSensor

## Descripción del Proyecto

Este repositorio contiene el código fuente del **Sensor de Higiene de Seguridad**, un componente de software desarrollado nativamente en Android (Kotlin). Su objetivo principal es capturar datos del entorno del dispositivo móvil del usuario para evaluar su estado básico de configuración de seguridad.

Este desarrollo forma parte de un proyecto de título de la Universidad de Santiago de Chile (USACH), el cual busca crear un videojuego serio para apoyar la capacitación en ciberseguridad de empleados de PYMEs chilenas. El sensor opera bajo el marco de trabajo **LifeSync-Games (LSG)**, actuando como un puente que transforma las métricas del mundo real en puntajes que modifican un perfil multidimensional dentro del videojuego.

---

## 🚀 Características Principales (Métricas Evaluadas)

El sensor se ejecuta para capturar la "Higiene de Seguridad" utilizando administradores del sistema de Android:

* **Bloqueo de Pantalla (PIN/Contraseña):** Verifica si el dispositivo está asegurado utilizando la clase `KeyguardManager`.
* **Biometría Activa:** Detecta si el hardware biométrico (huellas/rostro) está configurado y activo mediante `BiometricManager`.
* **Nivel de Parche de Seguridad:** Extrae la información de actualización del sistema operativo consultando `Build.VERSION.SECURITY_PATCH`.
* **Instalación de Apps Desconocidas:** Comprueba si el usuario tiene bloqueada la instalación de orígenes no oficiales leyendo `Settings.Secure.INSTALL_NON_MARKET_APPS`.

---

## 🛠️ Tecnologías y Requisitos

* **Entorno de Desarrollo:** Android Studio.
* **Lenguaje:** Kotlin.
* **Nivel de API Mínimo:** API 26 o superior.

---

## 📄 Licencia y Uso
Este repositorio es de carácter académico y ha sido desarrollado exclusivamente para fines de evaluación e integración en la propuesta de trabajo de título detallada anteriormente.

