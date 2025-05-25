package util;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.crypto.KeyAgreement;

public class Conexion {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private static Conexion instance;
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private byte[] claveCompartida;

    private Conexion() {}

    public static Conexion getInstance() {
        if (instance == null) {
            instance = new Conexion();
        }
        return instance;
    }



    public void setServerSocket() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        serverSocket = new ServerSocket(5000);
        clientSocket = serverSocket.accept();
        initializeStreams();
    }

    public void setClientSocket(String ipServer) throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }
        clientSocket = new Socket(ipServer, 5000);
        initializeStreams();
    }

    private void initializeStreams() throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            dos = new DataOutputStream(clientSocket.getOutputStream());
            dis = new DataInputStream(clientSocket.getInputStream());
        }
    }

    public KeyPair diffieHellman() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
        return keyPairGen.generateKeyPair();
    }

    public void enviarClavePublica(PublicKey publicKey) throws IOException {
        if (dos == null) {
            throw new IOException("Flujo de salida no inicializado");
        }
        byte[] publicKeyBytes = publicKey.getEncoded();
        dos.writeInt(publicKeyBytes.length);
        dos.write(publicKeyBytes);
        dos.flush();
        System.out.println("Clave pública enviada.");
    }

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

    public byte[] generarClaveCompartida(PrivateKey privateKey, PublicKey receivedPublicKey) throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(receivedPublicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret(); // Clave compartida original
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] aesKey = sha256.digest(sharedSecret); // Ajusta a 256 bits (32 bytes)
        return aesKey;
    }

    public void setClaveCompartida(byte[] clave){
        claveCompartida=clave;
    }

    public byte[] getClaveCompartida(){
        return claveCompartida;
    }

    public DataOutputStream getDos(){
        return dos;
    }

    public DataInputStream getDis(){
        return dis;
    }

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