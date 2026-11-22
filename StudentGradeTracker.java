import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StudentGradeTracker {
    private static class Student {
        private final int id;
        private final String name;
        private final List<Double> grades = new ArrayList<>();

        Student(int id, String name) {
            this.id = id;
            this.name = name;
        }

        void addGrade(double grade) {
            grades.add(grade);
        }

        double getAverage() {
            if (grades.isEmpty()) {
                return 0.0;
            }

            double total = 0.0;
            for (double grade : grades) {
                total += grade;
            }
            return total / grades.size();
        }

        double getHighestGrade() {
            double highest = Double.NEGATIVE_INFINITY;
            for (double grade : grades) {
                highest = Math.max(highest, grade);
            }
            return highest;
        }

        double getLowestGrade() {
            double lowest = Double.POSITIVE_INFINITY;
            for (double grade : grades) {
                lowest = Math.min(lowest, grade);
            }
            return lowest;
        }
    }

    private final List<Student> students = new ArrayList<>();
    private int nextStudentId = 1;

    public static void main(String[] args) {
        new StudentGradeTracker().run();
    }

    private void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Student Grade Tracker");

            while (true) {
                System.out.println("\n1. Add student");
                System.out.println("2. Add grade");
                System.out.println("3. View student report");
                System.out.println("4. View summary report");
                System.out.println("5. Exit");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        addStudent(scanner);
                        break;
                    case "2":
                        addGrade(scanner);
                        break;
                    case "3":
                        viewStudentReport(scanner);
                        break;
                    case "4":
                        viewSummaryReport();
                        break;
                    case "5":
                        System.out.println("Goodbye.");
                        return;
                    default:
                        System.out.println("Enter a number from 1 to 5.");
                }
            }
        }
    }

    private void addStudent(Scanner scanner) {
        System.out.print("Student name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Student name cannot be empty.");
            return;
        }

        Student student = new Student(nextStudentId++, name);
        students.add(student);
        System.out.println("Student added. ID: " + student.id);
    }

    private void addGrade(Scanner scanner) {
        Student student = selectStudent(scanner);
        if (student == null) {
            return;
        }

        System.out.print("Enter grade (0-100): ");
        try {
            double grade = Double.parseDouble(scanner.nextLine().trim());

            if (grade < 0 || grade > 100) {
                System.out.println("Grade must be between 0 and 100.");
                return;
            }

            student.addGrade(grade);
            System.out.println("Grade added for " + student.name + ".");
        } catch (NumberFormatException e) {
            System.out.println("Enter a valid number for the grade.");
        }
    }

    private Student selectStudent(Scanner scanner) {
        if (students.isEmpty()) {
            System.out.println("No students have been added yet.");
            return null;
        }

        System.out.println("Students:");
        for (Student student : students) {
            System.out.printf("ID: %d | Name: %s%n", student.id, student.name);
        }

        System.out.print("Enter student ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            for (Student student : students) {
                if (student.id == id) {
                    return student;
                }
            }

            System.out.println("Student ID not found.");
        } catch (NumberFormatException e) {
            System.out.println("Enter a valid student ID.");
        }

        return null;
    }

    private void viewStudentReport(Scanner scanner) {
        Student student = selectStudent(scanner);
        if (student == null) {
            return;
        }

        System.out.println("\nStudent Report");
        System.out.println("ID: " + student.id);
        System.out.println("Name: " + student.name);

        if (student.grades.isEmpty()) {
            System.out.println("No grades recorded.");
            return;
        }

        System.out.println("Grades: " + student.grades);
        System.out.printf("Average: %.2f%n", student.getAverage());
        System.out.printf("Highest: %.2f%n", student.getHighestGrade());
        System.out.printf("Lowest: %.2f%n", student.getLowestGrade());
    }

    private void viewSummaryReport() {
        if (students.isEmpty()) {
            System.out.println("No students have been added yet.");
            return;
        }

        double allGradesTotal = 0.0;
        int allGradesCount = 0;
        double overallHighest = Double.NEGATIVE_INFINITY;
        double overallLowest = Double.POSITIVE_INFINITY;

        System.out.println("\nSummary Report");

        for (Student student : students) {
            System.out.printf("%nID: %d | Name: %s%n", student.id, student.name);

            if (student.grades.isEmpty()) {
                System.out.println("No grades recorded.");
                continue;
            }

            System.out.println("Grades: " + student.grades);
            System.out.printf("Average: %.2f | Highest: %.2f | Lowest: %.2f%n",
                    student.getAverage(),
                    student.getHighestGrade(),
                    student.getLowestGrade());

            for (double grade : student.grades) {
                allGradesTotal += grade;
                allGradesCount++;
                overallHighest = Math.max(overallHighest, grade);
                overallLowest = Math.min(overallLowest, grade);
            }
        }

        if (allGradesCount == 0) {
            System.out.println("\nThere are no grades to summarize.");
            return;
        }

        System.out.println("\nOverall Grade Summary");
        System.out.printf("Average of all grades: %.2f%n",
                allGradesTotal / allGradesCount);
        System.out.printf("Highest grade: %.2f%n", overallHighest);
        System.out.printf("Lowest grade: %.2f%n", overallLowest);
        System.out.println("Number of grades: " + allGradesCount);
    }
}
