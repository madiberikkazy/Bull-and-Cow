package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Main entry point.
 *
 * Usage:
 *   Run with: java -jar grant-search.jar [path-to-pdf]
 *   Default PDF path: input.pdf  (in current directory)
 *
 * The program reads the PDF once, then lets you search by faculty code interactively.
 */
public class Main {

    // ANSI colors for a nicer console experience
    private static final String RESET   = "\033[0m";
    private static final String BOLD    = "\033[1m";
    private static final String CYAN    = "\033[36m";
    private static final String GREEN   = "\033[32m";
    private static final String YELLOW  = "\033[33m";
    private static final String RED     = "\033[31m";
    private static final String MAGENTA = "\033[35m";
    private static final String BLUE    = "\033[34m";

    public static void main(String[] args) {
        printBanner();

        // Determine PDF path
        String pdfPath = "input.pdf";
        if (args.length > 0) {
            pdfPath = args[0];
        }

        // Verify file exists
        if (!Files.exists(Paths.get(pdfPath))) {
            System.out.println(RED + "✗ File not found: " + pdfPath + RESET);
            System.out.println("  Please place 'input.pdf' in the same folder as the JAR,");
            System.out.println("  or run: java -jar grant-search.jar <path-to-pdf>");
            System.exit(1);
        }

        // Parse PDF
        System.out.println(CYAN + "📄 Reading PDF: " + pdfPath + RESET);
        System.out.println(CYAN + "   Please wait..." + RESET);

        Map<String, List<Applicant>> data;
        try {
            PdfParser parser = new PdfParser();
            data = parser.parse(pdfPath);
        } catch (IOException e) {
            System.out.println(RED + "✗ Failed to read PDF: " + e.getMessage() + RESET);
            e.printStackTrace();
            System.exit(1);
            return;
        }

        if (data.isEmpty()) {
            System.out.println(RED + "✗ No data found in the PDF. Please check the file." + RESET);
            System.exit(1);
        }

        System.out.println(GREEN + "✓ PDF parsed successfully!" + RESET);
        printDataSummary(data);

        // Interactive search loop
        Scanner scanner = new Scanner(System.in, "UTF-8");
        while (true) {
            System.out.println();
            System.out.print(BOLD + "Enter faculty code (e.g. B001) or 'list'/'exit': " + RESET);
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.isEmpty()) continue;

            if (input.equals("EXIT") || input.equals("QUIT") || input.equals("Q")) {
                System.out.println(CYAN + "Goodbye! / Сау болыңыз!" + RESET);
                break;
            }

            if (input.equals("LIST") || input.equals("L")) {
                printAvailableFaculties(data);
                continue;
            }

            // Search by faculty code
            if (data.containsKey(input)) {
                printFacultyResults(input, data.get(input));
            } else {
                System.out.println(RED + "✗ Faculty code '" + input + "' not found." + RESET);
                System.out.println("  Type 'list' to see available faculty codes.");
            }
        }
        scanner.close();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println(CYAN + BOLD +
                "╔══════════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(CYAN + BOLD +
                "║   2025-2026 БІЛІМ БЕРУ ГРАНТТАРЫ — GRANT SEARCH SYSTEM      ║" + RESET);
        System.out.println(CYAN + BOLD +
                "║            Education Grants Recipient Viewer                 ║" + RESET);
        System.out.println(CYAN + BOLD +
                "╚══════════════════════════════════════════════════════════════╝" + RESET);
    }

    private static void printDataSummary(Map<String, List<Applicant>> data) {
        int totalApplicants = data.values().stream().mapToInt(List::size).sum();
        System.out.println();
        System.out.println(BOLD + "  Faculties found : " + RESET + YELLOW + data.size() + RESET);
        System.out.println(BOLD + "  Total applicants: " + RESET + YELLOW + totalApplicants + RESET);
    }

    private static void printAvailableFaculties(Map<String, List<Applicant>> data) {
        System.out.println();
        System.out.println(BOLD + BLUE + "  Available Faculty Codes:" + RESET);
        System.out.println("  " + "─".repeat(60));

        for (Map.Entry<String, List<Applicant>> entry : data.entrySet()) {
            String code = entry.getKey();
            List<Applicant> list = entry.getValue();

            // Get faculty name from first applicant
            String facName = list.isEmpty() ? "" : list.get(0).getFacultyName();

            // Count quotas
            long general = list.stream()
                    .filter(a -> a.getQuota().contains("ЖАЛПЫ"))
                    .count();
            long rural = list.stream()
                    .filter(a -> a.getQuota().contains("АУЫЛ"))
                    .count();

            System.out.printf("  %s%-6s%s │ %-45s │ Барлығы: %s%3d%s (Жалпы: %d, Ауыл: %d)%n",
                    YELLOW + BOLD, code, RESET,
                    facName,
                    GREEN, list.size(), RESET,
                    general, rural);
        }
    }

    private static void printFacultyResults(String code, List<Applicant> applicants) {
        if (applicants.isEmpty()) {
            System.out.println(YELLOW + "  No applicants found for " + code + RESET);
            return;
        }

        String facName = applicants.get(0).getFacultyName();
        System.out.println();
        System.out.println(CYAN + BOLD +
                "╔══════════════════════════════════════════════════════════════╗" + RESET);
        System.out.printf((CYAN + BOLD + "║  %-60s  ║" + RESET) + "%n",
                code + " — " + facName);
        System.out.println(CYAN + BOLD +
                "╚══════════════════════════════════════════════════════════════╝" + RESET);

        // Separate by quota
        List<Applicant> general = new ArrayList<>();
        List<Applicant> rural   = new ArrayList<>();
        for (Applicant a : applicants) {
            if (a.getQuota().contains("АУЫЛ")) {
                rural.add(a);
            } else {
                general.add(a);
            }
        }

        if (!general.isEmpty()) {
            System.out.println();
            System.out.println(GREEN + BOLD + "  ── ЖАЛПЫ КОНКУРС (General Competition) ──" + RESET);
            printTableHeader();
            for (Applicant a : general) {
                printApplicantRow(a);
            }
            System.out.println("  " + "─".repeat(85));
            System.out.printf("  %s  Total: %d applicants%s%n", GREEN, general.size(), RESET);
        }

        if (!rural.isEmpty()) {
            System.out.println();
            System.out.println(MAGENTA + BOLD + "  ── АУЫЛ КВОТАСЫ (Rural Quota) ──" + RESET);
            printTableHeader();
            for (Applicant a : rural) {
                printApplicantRow(a);
            }
            System.out.println("  " + "─".repeat(85));
            System.out.printf("  %s  Total: %d applicants%s%n", MAGENTA, rural.size(), RESET);
        }

        System.out.println();
        System.out.println(BOLD + "  Grand Total: " + applicants.size() + " applicants" + RESET);
    }

    private static void printTableHeader() {
        System.out.println("  " + "─".repeat(85));
        System.out.printf("  %s%-4s │ %-46s │ %-12s │ %5s │ %s%s%n",
                BOLD, "№", "Аты-жөні (Full Name)", "ТЖК", "Балл", "Универ", RESET);
        System.out.println("  " + "─".repeat(85));
    }

    private static void printApplicantRow(Applicant a) {
        // Truncate very long names for display
        String name = a.getFullName();
        if (name.length() > 45) {
            name = name.substring(0, 43) + "..";
        }
        System.out.printf("  %-4d │ %-46s │ %-12s │ %5d │ %s%n",
                a.getNumber(),
                name,
                a.getTjk(),
                a.getScore(),
                a.getUniversity());
    }
}
