package ru.sanctio.studyudp;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class UDPClient {
    private DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] buffer;
    private String fileName;

    public static void main(String[] args) {
        System.out.println("Client started...");
        try (DatagramSocket s = new DatagramSocket()) {
            UDPClient client = new UDPClient();
            client.socket = s;
            /**
             * Запрос у пользователя имени файла для загрузки.
             */
            client.getFileName();
            /**
             * Запрос файла с сервера.
             */
            client.sendFileName();
            /**
             * Чтение ответа сервера, содержащего либо размер файла, либо код
             * ERROR, который означает отсутствие запрошенного файла на сервере.
             */
            long size = client.getFileSize();
            if (size > UDPServer.ERROR) {
                try {
                    /**
                     * Загрузка файла с сервера.
                     */
                    client.getFile(size);
                    System.out.println("File \"" + client.fileName
                            + "\" is downloaded successfully.");
                } catch (IOException e) {
                    /**
                     * Произошла ошибка чтения файла.
                     */
                    System.err.println("File \"" + client.fileName
                            + "\" isn\'t downloaded.\n" + e.getMessage());
                }
            } else {
                /**
                 * Сервер вернул код ERROR, который означает, что на сервере нет
                 * файла с запрошенным именем.
                 */
                System.out.println("File \"" + client.fileName + "\" not found");
            }
        } catch (SocketException ex) {
            System.out.println("Error #1: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Error #2: " + ex.getMessage());
        }
        System.out.println("Client stoped.");
    }

    /**
     * Чтение имени запрашиваемого файла с консоли.
     *
     * @return строку с именем файла.
     */
    private void getFileName() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));) {
            /**
             * Цикл запроса имени файла должен выполняться до тех пор, пока
             * пользователь не введёт непустое имя файла.
             */
            do {
                System.out.println(">>> Input file name for download:");
                fileName = in.readLine();
            } while (fileName == null || fileName.trim().isEmpty());
        } catch (IOException e) {
            System.out.println("Errror: " + e.getMessage());
        }
        System.out.println("Step 1");
    }

    /**
     * Отправка запроса с именем файла.
     *
     * @param fileName строка с именем файла.
     * @throws IOException в случае общей ошибки ввода/вывода.
     */
    private void sendFileName() throws IOException {
        buffer = fileName.getBytes();
        /**
         * В пакет заносится адрес сервера, его порт и сериализованное имя
         * файла.
         */
        packet = new DatagramPacket(buffer, buffer.length,
                InetAddress.getByName(UDPServer.UDP_SERVER_ADDRESS),
                UDPServer.UDP_SERVER_PORT);
        socket.send(packet);
        System.out.println("Step 2");
    }

    /**
     * Получение первого пакета от сервера с размером файла и отправка пакета с
     * подтверждением о готовности принять файл.
     *
     * @return размер файла.
     * @throws IOException в случае общей ошибки ввода/вывода.
     */
    private long getFileSize() throws IOException {
        buffer = new byte[8];
        packet = new DatagramPacket(buffer, buffer.length);
        /**
         * Получение пакета с размером файла.
         */
        socket.receive(packet);
        long size = ByteBuffer.wrap(packet.getData()).getLong();
        if (size > UDPServer.ERROR && size < Integer.MAX_VALUE) {
            buffer = UDPServer.OK.getBytes();
        } else {
            buffer = UDPServer.NO.getBytes();
        }
        /**
         * Подготовка и отправка пакета с подтверждением о готовности принять
         * файл.
         */
        packet.setData(buffer);
        packet.setLength(buffer.length);
        socket.send(packet);
        System.out.println("Step 3");
        return size;
    }

    private void getFile(long size) throws IOException {
        int s = size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size;
        buffer = new byte[s];
        packet.setData(buffer);
        socket.receive(packet);
        try (FileOutputStream fos = new FileOutputStream("Copy-" + fileName)) {
            fos.write(buffer);
        }
        System.out.println("Step 4");
    }
}
