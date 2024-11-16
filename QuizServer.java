import java.io.*;
import java.net.*;

public class QuizServer {

    // 문제와 답을 2차원 배열에 저장
    private static final String[][] QA = {
        {"What is the 14th letter of the alphabet?", "n"},
        {"What is 6 X 8?", "48"},
        {"Which planet in the solar system is closest to the sun?", "mercury"},
        {"Which country has the Leaning Tower of Pisa?", "italy"},
        {"What is the atomic number of magnesium in the periodic table?", "12"}
    };

    public static void main(String[] args) {
        BufferedReader in = null;
        BufferedWriter out = null;
        ServerSocket listener = null;
        Socket socket = null;

        try {
            // 서버 소켓 생성 및 포트 1234에서 대기 시작
            listener = new ServerSocket(1234);
            System.out.println("Server started... waiting for client connections.");

            // 클라이언트의 연결 요청 대기
            socket = listener.accept();
            System.out.println("A new client has connected!");
            
            // 클라이언트와 데이터를 주고받기 위한 스트림 초기화
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            int score = 0; // 점수 저장
            String inputMessage; // 클라이언트의 답변 저장
            for (int i = 0; i < QA.length; i++) {
                // 질문을 클라이언트에게 전송
                out.write("QUESTION: " + QA[i][0] + "\n");
                out.flush();

                // 클라이언트로부터 답변을 받음
                inputMessage = in.readLine().toLowerCase();

                // 답을 평가
                if (inputMessage.equals(QA[i][1])) {
                    out.write("Correct!\n");
                    score++;
                } else {
                    out.write("Incorrect. The correct answer was: " + QA[i][1] + "\n");
                }
                out.flush();
            }

            // 최종 점수를 클라이언트에게 전송
            out.write("Your final score is: " + score + " out of " + QA.length + "\n");
            out.flush();
            System.out.println("Game Over. Final score sent to client.");

        } catch (IOException e) {
            // 예외 발생 시 에러 메시지 출력
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (socket != null) socket.close(); // 소켓 닫기
                if (listener != null) listener.close(); // 서버 소켓 닫기
            } catch (IOException e) {
                System.out.println("Error closing resources.");
            }
        }
    }
}
