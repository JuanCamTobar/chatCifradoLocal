package ui;

import util.Conexion;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.SecureRandom;
import java.util.function.Consumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;

/**
 * Clase Chat que maneja el envío y recepción de mensajes cifrados
 * entre dos usuarios utilizando una conexión segura.
 */
public class Chat {

    // Instancia singleton de Chat
    private static Chat instance;

    // Longitud del IV (vector de inicialización) para GCM
    private static final int GCM_IV_LENGTH = 12;

    // Longitud del tag de autenticación para GCM
    private static final int GCM_TAG_LENGTH = 16;

    // Executor para manejar tareas concurrentes
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    // Nombre del usuario local
    private String nombreUser;

    // Nombre del usuario remoto
    private String nombreRemoto;

    // Constructor privado para el patrón singleton
    private Chat() {}

    /**
     * Obtiene la instancia singleton de Chat.
     * @return la instancia de Chat
     */
    public static Chat getInstance() {
        if (instance == null) {
            instance = new Chat();
        }
        return instance;
    }

    /**
     * Envía un mensaje cifrado al otro usuario.
     * @param mensaje el texto del mensaje a enviar
     * @throws Exception si ocurre un error durante el cifrado o envío
     */
    public void enviarMensaje(String mensaje) throws Exception {
        Conexion conexion = Conexion.getInstance();
        byte[] mensajeCifrado = encriptarMensaje(mensaje);
        DataOutputStream dos = conexion.getDos();
        dos.writeInt(mensajeCifrado.length);
        dos.write(mensajeCifrado);
        dos.flush();
    }

    /**
     * Recibe mensajes de forma asíncrona y los pasa al consumidor recibido.
     * @param onMessageReceived función que se ejecuta al recibir un mensaje
     */
    public void recibirMensaje(Consumer<String> onMessageReceived) {
        Conexion conexion = Conexion.getInstance();
        executor.submit(() -> {
            try {
                DataInputStream dis = conexion.getDis();
                while (true) {
                    int length = dis.readInt();
                    byte[] mensajeCifrado = new byte[length];
                    dis.readFully(mensajeCifrado);
                    String mensaje = desencriptarMensaje(mensajeCifrado);
                    Platform.runLater(() -> onMessageReceived.accept(mensaje));
                }
            } catch (Exception e) {
                Platform.runLater(() -> onMessageReceived.accept("Error: Conexión perdida - " + e.getMessage()));
            }
        });
    }

    /**
     * Envía el nombre del usuario cifrado.
     * @param nombre el nombre del usuario
     * @throws Exception si ocurre un error durante el cifrado o envío
     */
    public void enviarNombreUsuario(String nombre) throws Exception {
        Conexion conexion = Conexion.getInstance();
        byte[] nombreCifrado = encriptarMensaje(nombre);
        DataOutputStream dos = conexion.getDos();
        dos.writeInt(nombreCifrado.length);
        dos.write(nombreCifrado);
        dos.flush();
    }

    /**
     * Recibe el nombre de usuario cifrado y lo pasa al consumidor recibido.
     * @param onNombreReceived función que se ejecuta al recibir el nombre
     */
    public void recibirNombreUsuario(Consumer<String> onNombreReceived) {
        Conexion conexion = Conexion.getInstance();
        executor.submit(() -> {
            try {
                DataInputStream dis = conexion.getDis();
                int length = dis.readInt();
                byte[] nombreCifrado = new byte[length];
                dis.readFully(nombreCifrado);
                String nombre = desencriptarMensaje(nombreCifrado);
                Platform.runLater(() -> onNombreReceived.accept(nombre));
            } catch (Exception e) {
                Platform.runLater(() -> onNombreReceived.accept("Error: No se pudo recibir el nombre - " + e.getMessage()));
            }
        });
    }

    /**
     * Cifra un mensaje utilizando AES en modo GCM.
     * @param mensaje el mensaje a cifrar
     * @return el mensaje cifrado con IV prepended
     * @throws Exception si ocurre un error en el proceso de cifrado
     */
    public byte[] encriptarMensaje(String mensaje) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(Conexion.getInstance().getClaveCompartida(), "AES");
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] textoCifrado = cipher.doFinal(mensaje.getBytes("UTF-8"));
        byte[] mensajeCifrado = new byte[GCM_IV_LENGTH + textoCifrado.length];
        System.arraycopy(iv, 0, mensajeCifrado, 0, GCM_IV_LENGTH);
        System.arraycopy(textoCifrado, 0, mensajeCifrado, GCM_IV_LENGTH, textoCifrado.length);
        return mensajeCifrado;
    }

    /**
     * Descifra un mensaje recibido utilizando AES en modo GCM.
     * @param mensajeCifrado el mensaje cifrado con el IV al inicio
     * @return el mensaje descifrado en texto plano
     * @throws Exception si ocurre un error en el proceso de descifrado
     */
    public String desencriptarMensaje(byte[] mensajeCifrado) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] textoCifrado = new byte[mensajeCifrado.length - GCM_IV_LENGTH];
        System.arraycopy(mensajeCifrado, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(mensajeCifrado, GCM_IV_LENGTH, textoCifrado, 0, textoCifrado.length);
        SecretKeySpec keySpec = new SecretKeySpec(Conexion.getInstance().getClaveCompartida(), "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
        byte[] textoPlano = cipher.doFinal(textoCifrado);
        return new String(textoPlano, "UTF-8");
    }

    /**
     * Obtiene el nombre del usuario local.
     * @return el nombre del usuario
     */
    public String getNombreUser() {
        return nombreUser;
    }

    /**
     * Establece el nombre del usuario local.
     * @param nombreUser el nuevo nombre del usuario
     */
    public void setNombreUser(String nombreUser) {
        this.nombreUser = nombreUser;
    }

    /**
     * Obtiene el nombre del usuario remoto.
     * @return el nombre del usuario remoto
     */
    public String getNombreRemoto() {
        return nombreRemoto;
    }

    /**
     * Establece el nombre del usuario remoto.
     * @param nombreRemoto el nuevo nombre del usuario remoto
     */
    public void setNombreRemoto(String nombreRemoto) {
        this.nombreRemoto = nombreRemoto;
    }
}
