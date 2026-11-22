import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ChatbotApp {
    //This program does not call an API.

    private final Map<String, String> faq = new LinkedHashMap<>();
    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();

    private ChatbotApp() {
        trainDefaults();
        createWindow();
    }

    // Frequently asked questions the bot knows when it starts.
    private void trainDefaults() {
        faq.put("hello", "Hello! How can I help you?");
        faq.put("hi", "Hi! What can I help you with?");
        faq.put("what can you do", "I can answer questions from my FAQ list. You can teach me more using the Train button.");
        faq.put("what is your name", "I'm a Java chatbot.");
        faq.put("how are you", "I'm doing well. What would you like to know?");
        faq.put("thank you", "You're welcome!");
        faq.put("bye", "Goodbye!");
        faq.put("what is your purpose","My purpose is to answer common questions and help with simple tasks.");
        faq.put("who created you","I was created by a author as a Java chatbot project.");
        faq.put("what can you help me with","I can answer questions that are included in my FAQ list.");
        faq.put("how do i use this chatbot","Type a question in the message box and press Send.");
        faq.put("can you learn new answers","Yes. Use the Train button to add a question and its answer.");
        faq.put("do you remember previous chats","I can respond to questions during this session, but this version does not save chat history.");
        faq.put("what languages do you speak","This version is set up to understand English questions.");
        faq.put("are you a real person","No. I'm a computer program designed to respond to questions.");
        faq.put("what should i do if you do not know an answer","Use the Train button to teach me the question and the answer.");
        faq.put("is my information saved","This version keeps trained FAQs in memory while it is running. It does not save them after you close it.");
        faq.put("capital of india", "The capital of India is New Delhi.");
        faq.put("capital of karnataka", "The capital of Karnataka is Bengaluru.");
        faq.put("who is the father of java", "James Gosling is known as the father of Java.");
        faq.put("what is java", "Java is an object-oriented programming language.");
        faq.put("what is oop", "OOP means Object-Oriented Programming: classes, objects, inheritance, polymorphism, encapsulation and abstraction.");
        faq.put("national language of india", "India has no single national language; Hindi and English are the official languages of the central government.");
    }

    private void createWindow() {
        JFrame frame = new JFrame("Java Chatbot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 450);
        frame.setLocationRelativeTo(null);

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JButton sendButton = new JButton("Send");
        JButton trainButton = new JButton("Train");

        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.add(inputField, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(trainButton);
        buttons.add(sendButton);
        inputPanel.add(buttons, BorderLayout.EAST);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(event -> sendMessage());
        inputField.addActionListener(event -> sendMessage());
        trainButton.addActionListener(event -> trainFromGui());

        chatArea.append("Bot: Hello! Ask me something, or use Train to add an FAQ.\n");
        frame.setVisible(true);
        inputField.requestFocusInWindow();
    }

    private void sendMessage() {
        String question = inputField.getText().trim();
        if (question.isEmpty()) {
            return;
        }

        chatArea.append("You: " + question + "\n");
        chatArea.append("Bot: " + getAnswer(question) + "\n\n");
        inputField.setText("");
    }

    private String getAnswer(String question) {
        String normalizedQuestion = normalize(question);
        if (normalizedQuestion.isEmpty()) {
            return "Please enter a question.";
        }

        String bestAnswer = null;
        double bestScore = 0.0;

        for (Map.Entry<String, String> entry : faq.entrySet()) {
            double score = similarity(normalizedQuestion, normalize(entry.getKey()));
            if (score > bestScore) {
                bestScore = score;
                bestAnswer = entry.getValue();
            }
        }

        // Require a reasonable match to avoid answering unrelated questions.
        if (bestScore >= 0.30 && bestAnswer != null) {
            return bestAnswer;
        }
        return "I don't know that yet. Use Train to teach me a question and answer.";
    }

    /*
     * Basic NLP preprocessing:
     * lowercase text, remove punctuation, split into words, and remove common
     * words that usually add little meaning.
     */
    private String normalize(String text) {
        Set<String> stopWords = Set.of(
                "a", "an", "the", "is", "are", "am", "do", "does",
                "what", "how", "can", "i", "you", "your", "me", "my",
                "to", "of", "in", "on", "for", "please","u",
                "ur","pls","plz","tell","about","was","which"
        );

        String cleaned = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isEmpty()) {
            return "";
        }

        List<String> words = new ArrayList<>();
        for (String word : cleaned.split(" ")) {
            if (!stopWords.contains(word)) {
                words.add(word);
            }
        }
        if (words.isEmpty()) {
            return cleaned;
        }
        return String.join(" ", words);
    }

    // Compares the words in a user's question with each stored FAQ question.
    private double similarity(String first, String second) {
        Set<String> firstWords = new HashSet<>(Arrays.asList(first.split(" ")));
        Set<String> secondWords = new HashSet<>(Arrays.asList(second.split(" ")));
        firstWords.remove("");
        secondWords.remove("");

        if (firstWords.isEmpty() || secondWords.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(firstWords);
        intersection.retainAll(secondWords);

        Set<String> union = new HashSet<>(firstWords);
        union.addAll(secondWords);

        return (double) intersection.size() / union.size();
    }

    // Adds a question and answer to the bot's in-memory FAQ list.
    private void trainFromGui() {
        String question = JOptionPane.showInputDialog(
                null, "Question the bot should learn:", "Train Chatbot",
                JOptionPane.PLAIN_MESSAGE
        );
        if (question == null || question.trim().isEmpty()) {
            return;
        }

        String answer = JOptionPane.showInputDialog(
                null, "What should the bot answer?", "Train Chatbot",
                JOptionPane.PLAIN_MESSAGE
        );
        if (answer == null || answer.trim().isEmpty()) {
            return;
        }

        faq.put(question.trim(), answer.trim());
        chatArea.append("Bot: I learned a new FAQ question.\n\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatbotApp::new);
    }
}
