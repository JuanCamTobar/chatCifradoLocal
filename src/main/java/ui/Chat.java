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

public class Chat {

    private static Chat instance;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    private String nombreUser;
    private String nombreRemoto;

    private Chat() {}

    public static Chat getInstance() {
        if (instance == null) {
            instance = new Chat();
        }
        return instance;
    }

    public void enviarMensaje(String mensaje) throws Exception {
        Conexion conexion = Conexion.getInstance();
        byte[] mensajeCifrado = encriptarMensaje(mensaje);
        DataOutputStream dos = conexion.getDos();
        dos.writeInt(mensajeCifrado.length);
        dos.write(mensajeCifrado);
        dos.flush();
    }

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

    public void enviarNombreUsuario(String nombre) throws Exception {
        Conexion conexion = Conexion.getInstance();
        byte[] nombreCifrado = encriptarMensaje(nombre);
        DataOutputStream dos = conexion.getDos();
        dos.writeInt(nombreCifrado.length);
        dos.write(nombreCifrado);
        dos.flush();
    }

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

    public String getNombreUser() {
        return nombreUser;
    }

    public void setNombreUser(String nombreUser) {
        this.nombreUser = nombreUser;
    }

    public String getNombreRemoto() {
        return nombreRemoto;
    }

    public void setNombreRemoto(String nombreRemoto) {
        this.nombreRemoto = nombreRemoto;
    }
}