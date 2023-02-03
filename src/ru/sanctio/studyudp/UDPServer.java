package ru.sanctio.studyudp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;


public class UDPServer {
    public static final int UDP_SERVER_PORT = 12345;
    public static final String UDP_SERVER_ADDRESS = "localhost";
    public static final String OK = "Ok";
    public static final String NO = "No";
    public static final int ERROR = -1;
    public static final int BUFFER_SIZE = 255;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] buffer;
    private String fileName;
    private long fileLength;
    private boolean exists;

    public static void main(String[] args) {
        System.out.println("Server started...");
        try (DatagramSocket s = new DatagramSocket(UDP_SERVER_PORT)) {
            UDPServer server = new UDPServer();
            server.socket = s;
            /**
             * Ожидание запроса клиента с именем файла.
             */
            server.getFileName();
            /**
             * Отправка клиенту результата поиска файла: либо размера найденного
             * файла, либо кода ERROR, который означает отсутствие запрошенного
             * файла на сервере.
             */
            server.sendResult();
            /**
             * Отправка файла с сервера.
             */
            if (server.exists && server.getClientReply()) {
                server.sendFile();
                System.out.println("Server: File \"" + server.fileName
                        + "\" is sent successfully.");
            }
        } catch (SocketException ex) {
            System.out.println("Server error #1: " + ex.getMessage());
        } catch (IOException e) {
            /**
             * Произошла ошибка ввода/вывода файла.
             */
            System.err.println("Server error #2: " + e.getMessage());
        }
        System.out.println("Server stoped.");
    }

    /**
     * Чтение имени запрашиваемого файла с консоли.
     *
     * @return строку с именем файла.
     */
    private void getFileName() throws IOException {
        buffer = new byte[BUFFER_SIZE];
        packet = new DatagramPacket(buffer, BUFFER_SIZE);
        socket.receive(packet);
        fileName = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Step 1: file name: " + fileName);
    }

    private void sendResult() throws IOException {
        File f = new File(fileName);
        buffer = new byte[8];
        if (exists = f.exists()) {
            fileLength = f.length();
            ByteBuffer.wrap(buffer).putLong(fileLength);
        } else {
            ByteBuffer.wrap(buffer).putLong(ERROR);
        }
        packet.setData(buffer);
        packet.setLength(8);
        socket.send(packet);
        System.out.println("Step 2");
    }

    private boolean getClientReply() throws IOException {
        buffer = new byte[4];
        packet.setData(buffer);
        socket.receive(packet);
        String r = new String(packet.getData()).trim();
        System.out.println("Step 3");
        return r.equals("Ok");
    }

    private void sendFile() throws IOException {
        buffer = new byte[(int) fileLength];
        try (FileInputStream fis = new FileInputStream(fileName)) {
            int s = fis.read(buffer);
        }
        packet.setData(buffer);
        socket.send(packet);
        System.out.println("Step 4");
    }

}
