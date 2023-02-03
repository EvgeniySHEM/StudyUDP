package ru.sanctio.studyudp;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

public class UDPClient {
    private DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] buffer;
    private String fileName;

    public static void main(String[] args) {
        try (DatagramSocket s = new DatagramSocket()){
            UDPClient client = new UDPClient();
            client.socket = s;
            String fileName = client.getFileName(); //запрашивается имя файла
            client.sendFileName(fileName); //имя файла отправляется на сервер для проверки наличия
            int size = client.getFileSize(); //сервер возвращает размер файла, если он есть
            boolean b = client.getFile(size); //считываем и записываем файл отправленный сервером
            if(b) {
                System.out.println("File \"" + fileName + "\" is ready");
            } else {
                System.out.println("File \"" + fileName + "\" not exists");
            }
        } catch (SocketException e) {
            System.out.println("Error #1: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error #2: " + e.getMessage());
        }
    }

    private boolean getFile(int size) throws IOException {
        if(size != UDPServer.ERROR) {
            buffer = new byte[size];
            packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            try(FileOutputStream fos = new FileOutputStream(fileName)){
                fos.write(buffer);
            }
            return true;
        }
        return false;
    }

    private int getFileSize() throws IOException {
        buffer = new byte[4];
        packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        DataInputStream dis = new DataInputStream(System.in);
        int size = dis.read(buffer);
        if(size != UDPServer.ERROR) {
            buffer = UDPServer.OK.getBytes();
            packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(UDPServer.UDP_SERVER_ADDRESS),
                    UDPServer.UDP_SERVER_PORT);
            socket.send(packet);
            return size;
        }
        return size;
    }
    private void sendFileName(String fileName) throws IOException {
        // ... проверки на валидность
        buffer = fileName.getBytes();
        packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(UDPServer.UDP_SERVER_ADDRESS),
                UDPServer.UDP_SERVER_PORT);
        socket.send(packet);
    }

    private String getFileName() {
        fileName = "test.txt";
        return fileName;
    }
}
