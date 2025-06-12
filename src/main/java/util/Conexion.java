package util;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.crypto.KeyAgreement;

/**
 * Clase Singleton que gestiona la conexión entre dos pares usando sockets TCP.
 * También implementa el intercambio de claves mediante el algoritmo Diffie-Hellman.
 */
public class Conexion {
    private ServerSocket serverSocket; // Socket del lado servidor
    private Socket clientSocket; // Socket del lado cliente
    private DataOutputStream dos; // Flujo de salida para enviar datos
    private DataInputStream dis; // Flujo de entrada para recibir datos
    private static Conexion instance; // Instancia única de la clase
    private static final ExecutorService executor = Executors.newFixedThreadPool(3); // Hilo para operaciones asíncronas
    private byte[] claveCompartida; // Clave compartida generada con Diffie-Hellman

    // Constructor privado para el patrón Singleton
    private Conexion() {}

    /**
     * Devuelve la única instancia de la clase (Singleton).
     */
    public static Conexion getInstance() {
        if (instance == null) {
            instance = new Conexion();
        }
        return instance;
    }

    /**
     * Inicializa el servidor y espera una conexión entrante.
     * Abre el puerto 5000 y acepta la conexión.
     */
    public void setServerSocket() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        serverSocket = new ServerSocket(5000);
        clientSocket = serverSocket.accept();
        initializeStreams();
    }

    /**
     * Se conecta a un servidor remoto usando la dirección IP proporcionada.
     */
    public void setClientSocket(String ipServer) throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }
        clientSocket = new Socket(ipServer, 5000);
        initializeStreams();
    }

    /**
     * Inicializa los flujos de entrada y salida del socket.
     */
    private void initializeStreams() throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            dos = new DataOutputStream(clientSocket.getOutputStream());
            dis = new DataInputStream(clientSocket.getInputStream());
        }
    }

    /**
     * Genera un par de claves pública-privada para Diffie-Hellman.
     */
    public KeyPair diffieHellman() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
        return keyPairGen.generateKeyPair();
    }

    /**
     * Envía la clave pública a través del flujo de salida.
     */
    public void enviarClavePublica(PublicKey publicKey) throws IOException {
        if (dos == null) {
            throw new IOException("Flujo de salida no inicializado");
        }
        byte[] publicKeyBytes = publicKey.getEncoded();
        dos.writeInt(publicKeyBytes.length); // Enviar longitud primero
        dos.write(publicKeyBytes); // Enviar bytes de la clave
        dos.flush();
        System.out.println("Clave pública enviada.");
    }

    /**
     * Recibe la clave pública desde el otro par de manera asíncrona.
     * Llama al consumidor proporcionado con la clave recibida.
     */
    public void recibirClavePublica(Consumer<PublicKey> onKeyReceived) {
        executor.submit(() -> {
            try {
                if (dis == null) {
                    throw new IOException("Flujo de entrada no inicializado");
                }
                System.out.println("Esperando clave pública...");
                int length = dis.readInt();
                byte[] receivedBytes = new byte[length];
                dis.readFully(receivedBytes);

                KeyFactory keyFactory = KeyFactory.getInstance("DH");
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(receivedBytes);
                PublicKey receivedPublicKey = keyFactory.generatePublic(keySpec);

                System.out.println("Clave pública recibida.");
                onKeyReceived.accept(receivedPublicKey);
            } catch (Exception e) {
                System.err.println("Error al recibir clave pública: " + e.getMessage());
                throw new RuntimeException("Error al recibir clave pública", e);
            }
        });
    }

    /**
     * Genera la clave compartida a partir de la clave privada local y la clave pública del otro par.
     * Aplica SHA-256 a la clave compartida para obtener una clave simétrica de 256 bits.
     */
    public byte[] generarClaveCompartida(PrivateKey privateKey, PublicKey receivedPublicKey) throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(receivedPublicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret(); // Clave compartida original
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] aesKey = sha256.digest(sharedSecret); // Ajusta a 256 bits (32 bytes)
        return aesKey;
    }

    /**
     * Establece la clave compartida generada.
     */
    public void setClaveCompartida(byte[] clave){
        claveCompartida = clave;
    }

    /**
     * Devuelve la clave compartida generada.
     */
    public byte[] getClaveCompartida(){
        return claveCompartida;
    }

    /**
     * Devuelve el flujo de salida de datos.
     */
    public DataOutputStream getDos(){
        return dos;
    }

    /**
     * Devuelve el flujo de entrada de datos.
     */
    public DataInputStream getDis(){
        return dis;
    }

    /**
     * Cierra todas las conexiones y flujos abiertos.
     */
    public void cerrarConexion() {
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
}
