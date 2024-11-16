import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class QuizClient extends JFrame {
    private JTextArea displayArea;
    private JTextField answerField;
    private JButton submitButton;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public QuizClient() {
        // 기본 창 설정
        setTitle("Quiz Client");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // GUI 구성 요소 생성
        displayArea = new JTextArea(); // 서버 메시지를 출력
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea); // 스크롤 가능하도록 설정

        JPanel inputPanel = new JPanel(); // 답변 입력 필드와 버튼 포함 패널
        answerField = new JTextField(20); // 텍스트 입력 필드
        submitButton = new JButton("Submit Answer"); // 답변 제출 버튼

        add(scrollPane, BorderLayout.CENTER);
        inputPanel.add(answerField);
        inputPanel.add(submitButton);
        add(inputPanel, BorderLayout.SOUTH);

        // 버튼 클릭 및 Enter 키로 답변 제출
        submitButton.addActionListener(e -> sendAnswer());
        answerField.addActionListener(e -> sendAnswer());

        // 창 닫기 이벤트 처리
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup(); 
            }
        });

        // 창을 화면 가운데로 위치
        setLocationRelativeTo(null);
    }

    private void connectToServer() {
        String serverIP = "localhost";
        int serverPort = 1234;

        try (BufferedReader fileReader = new BufferedReader(new FileReader("server_info.dat"))) {
            // 파일에서 서버 정보 읽기
            serverIP = fileReader.readLine();
            String portString = fileReader.readLine();
            if (portString != null) {
                serverPort = Integer.parseInt(portString);
            }
        } catch (IOException e) {
            displayMessage("Error reading server_info.dat: " + e.getMessage());
            return; // 서버 연결 시도 중단
        }

        try {
            // 서버와 연결
            socket = new Socket(serverIP, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 서버로부터 메시지 받기
            receiveMessages();

        } catch (IOException e) {
            displayMessage("Error connecting to server: " + e.getMessage());
        }
    }

    private void receiveMessages() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    displayMessage(message); // 서버 메시지를 화면에 출력
                    if (message.startsWith("Your final score")) {
                        break; // 최종 점수 메시지가 오면 반복문 종료
                    }
                }
            } catch (IOException e) {
                displayMessage("Connection error: " + e.getMessage());
            }
        }).start();
    }

    private void sendAnswer() {
        String answer = answerField.getText().trim(); // 답변 가져오기
        if (!answer.isEmpty()) {
            out.println(answer); // 서버로 답변 전송
            answerField.setText(""); // 입력 필드 초기화
        }
    }

    private void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            displayArea.append(message + "\n"); // 메시지를 출력
            displayArea.setCaretPosition(displayArea.getDocument().getLength());
        });
    }

    private void cleanup() {
        try {
            if (socket != null) { // 소켓 닫기
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            QuizClient client = new QuizClient(); // 클라이언트 인스턴스 생성
            client.setVisible(true);
            client.connectToServer(); // 서버 연결 시도
        });
    }
}
