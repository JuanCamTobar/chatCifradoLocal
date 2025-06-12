### Juan Camilo Tobar - A00399905

# Chat cifrado

## Paso a paso de la creación del proyecto: 

**1. Configuración de la interfaz javafx:** Se configuro la estructura del proyecto y los respectivos archivos de configuración para los usos de javafx, en module-info se puso configuración de los modulos que son usables por javafx. Además, se crearon los respectivos .fxml para presentar una pantalla inicial de elección de usuario y el chat.

**2. Configuración de la conexión:** Para poder hacer que los dos nodos se conecten, se utiliza uno de los nodos como servidor (A) y uno como el cliente (B). Se utiliza Server Socket y Socket para establecer la conexión, luego se inicializan los flujos de entrada y salidad de datos para que sean utilizados por los nodos. Se aplico el patron de diseño singleton que maneje todo este proceso.

**3. Generación de claves publicas y clave compartida:** Se generan par de claves publicas y privadas con diffie hellman. Luego el nodo recibe la clave publica del otro nodo y genera la clave compartida con la clave publica y su clave privada. Posterior a esto, la clave compartida se configura para el sha-256 y está lista para encriptar y desencriptar los mensajes a partir de aquí.

**4. Envio y recepción de claves publica:** Para poder hacer el envio y recepción, primero se hace que el servidor escuche o espere la clave publica del cliente, posteriormente el cliente escucha y el servidor envia la clave publica. En este punto ambos tienen las claves.

**5. Envió y recepción de mensajes:** Cuando uno de los nodos envia un mensaje el otro tiene un hilo dispuesto a escuchar estos mensajes con un hilo especializado para esto, posteriomente se actualiza el area de mensajeria de javafx con el hilo principal.

## Dificultades

**Manejo de hilos:** Tuve dificultades manejando los hilos para la escucha de mensajes, nombres y claves publicas, utilice consumidores para que los metodos puedan ser consumidos y gestionar la variable de ingreso, además, se disponen solo 3 hilos para que el programa pueda gestionar todos los metodos asincronicos ya que el hilo principal queda manejando la interfaz javafx, y de ninguna manera se puede utilizar ese hilo ya que se bloquea la interfaz.

**Manejo de sockets:** El trabajo con sockets siempre se me dificulta ya que siempre se me cierran los sockets antes de tiempo, entonces toca ver como gestioar los hilos para que no se cierren los sockets y se mantengan abiertos hasta que la comunicación finalice.

## Conclusión:

Este proyecto me permitió comprender e implementar un sistema de comunicación segura usando sockets, JavaFX y cifrado con Diffie-Hellman. Aunque enfrenté retos importantes con el manejo de hilos y la gestión de sockets, logré integrar la lógica de cifrado con una interfaz funcional, garantizando la transmisión segura y fluida de mensajes entre nodos.