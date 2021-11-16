package Homework.Modul12;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class TrialServer extends Thread {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public TrialServer(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        Server.story.printStory(out);
        start();
    }

    @Override
    public void run() {
        String words;
        try {
            words = in.readLine();
            try {
                out.write(words + "\n");
                out.flush();
            } catch (IOException e) {
            }
            try {
                while (true) {
                    words = in.readLine();
                    if (words.equals("stop")) {
                        this.downService();
                        break;
                    }
                    System.out.println("Echoing: " + words);
                    Server.story.addStoryEl(words);
                    for (TrialServer ts : Server.serverList) {
                        ts.send(words);
                    }
                }
            } catch (NullPointerException e) {
            }


        } catch (IOException e) {
            this.downService();
        }
    }

    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException e) {
        }

    }

    private void downService() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (TrialServer ts : Server.serverList) {
                    if (ts.equals(this)) ts.interrupt();
                    Server.serverList.remove(this);
                }
            }
        } catch (IOException e) {
        }
    }
}

class Story {

    private LinkedList<String> story = new LinkedList<>();

    public void addStoryEl(String el) {
        if (story.size() >= 10) {
            story.removeFirst();
            story.add(el);
        } else {
            story.add(el);
        }
    }

    public void printStory(BufferedWriter writer) {
        if (story.size() > 0) {
            try {
                writer.write("History messages" + "\n");
                for (String ts : story) {
                    writer.write(ts + "\n");
                }
                writer.write("/...." + "\n");
                writer.flush();
            } catch (IOException e) {
            }

        }

    }
}

class Server {

    public static final int PORT = 7777;
    public static LinkedList<TrialServer> serverList = new LinkedList<>();
    public static Story story;

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        story = new Story();
        System.out.println("Server Started");
        try {
            while (true) {
                Socket socket = server.accept();
                try {
                    serverList.add(new TrialServer(socket));
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            server.close();
        }
    }
}