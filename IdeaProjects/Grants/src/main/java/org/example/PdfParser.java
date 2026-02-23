package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

/**
 * Parses the grant recipients PDF.
 *
 * PDF QUIRKS HANDLED:
 * 1. Standalone page numbers (single/double digit lines) → skipped
 * 2. Names wrap to NEXT line after "NUM TJK ..."
 * 3. Names appear on PREVIOUS line before "NUM TJK SCORE UNI" (inverted layout)
 * 4. АУЫЛ КВОТАСЫ section starts mid-page
 * 5. Merged score+university: "102013" → score=102, univ=013
 * 6. Partial rows at page boundaries → gracefully dropped
 */
public class PdfParser {

    private static final Pattern ROW_START           = Pattern.compile("^(\\d{1,3})\\s+(\\d{9})\\s+(.+)$");
    private static final Pattern ROW_NUM_TJK_SCORE_UNI = Pattern.compile("^(\\d{1,3})\\s+(\\d{9})\\s+(\\d{2,3})\\s+(\\d{3})\\s*$");
    private static final Pattern ROW_TAIL            = Pattern.compile("^(.*?)\\s+(\\d{2,3})\\s+(\\d{3})\\s*$");
    private static final Pattern FACULTY_LINE        = Pattern.compile("^(B\\d{3})\\s*-\\s*(.+)$");
    private static final Pattern MERGED_SCORE_UNIV   = Pattern.compile("^(.*?)\\s+(\\d{5,6})\\s*$");

    private static final String QUOTA_GENERAL = "ЖАЛПЫ КОНКУРС";
    private static final String QUOTA_RURAL   = "АУЫЛ КВОТАСЫ";

    private static final Set<String> SKIP_LINES = new HashSet<>(Arrays.asList(
            "2025-2026 ОҚУ ЖЫЛЫНА АРНАЛҒАН БІЛІМ БЕРУ ГРАНТТАРЫ",
            "ИЕГЕРЛЕРІНІҢ ТІЗІМІ",
            "Жалпы конкурс бойынша білім беру гранты иегерлерінің тізімі",
            "КҮНДІЗГІ ТОЛЫҚ ОҚУ НЫСАНЫ",
            "№ ТЖК Тегі, Аты, Әкесінің аты Жалпы балл ЖЖОКБҰ",
            QUOTA_GENERAL,
            QUOTA_RURAL
    ));

    public Map<String, List<Applicant>> parse(String pdfPath) throws IOException {
        String fullText;
        try (PDDocument doc = PDDocument.load(new File(pdfPath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            fullText = stripper.getText(doc);
        }
        List<String> lines = cleanLines(fullText);
        List<Applicant> applicants = parseLines(lines);

        Map<String, List<Applicant>> result = new LinkedHashMap<>();
        for (Applicant a : applicants) {
            result.computeIfAbsent(a.getFacultyCode(), k -> new ArrayList<>()).add(a);
        }
        return result;
    }

    private List<String> cleanLines(String text) {
        List<String> result = new ArrayList<>();
        for (String line : text.split("\\r?\\n")) {
            String t = line.trim();
            if (t.isEmpty() || t.matches("^\\d{1,2}$")) continue;
            result.add(t);
        }
        return result;
    }

    private List<Applicant> parseLines(List<String> lines) {
        List<Applicant> applicants = new ArrayList<>();
        String facCode = "", facName = "", quota = QUOTA_GENERAL;
        boolean pendingRow = false;
        int pNum = 0; String pTjk = "", pName = "";
        String prefixName = "";

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (SKIP_LINES.contains(line)) {
                flush(applicants, pendingRow, pNum, pTjk, pName, quota, facCode, facName);
                pendingRow = false; prefixName = ""; continue;
            }

            Matcher fac = FACULTY_LINE.matcher(line);
            if (fac.matches()) {
                flush(applicants, pendingRow, pNum, pTjk, pName, quota, facCode, facName);
                pendingRow = false; prefixName = "";
                facCode = fac.group(1); facName = fac.group(2).trim(); quota = QUOTA_GENERAL;
                continue;
            }

            if (line.contains(QUOTA_RURAL)) {
                flush(applicants, pendingRow, pNum, pTjk, pName, quota, facCode, facName);
                pendingRow = false; prefixName = ""; quota = QUOTA_RURAL; continue;
            }

            // Short row: NUM TJK SCORE UNI — name was on previous line(s)
            Matcher shortRow = ROW_NUM_TJK_SCORE_UNI.matcher(line);
            if (shortRow.matches()) {
                flush(applicants, pendingRow, pNum, pTjk, pName, quota, facCode, facName);
                pendingRow = false;
                int num = Integer.parseInt(shortRow.group(1));
                String tjk = shortRow.group(2);
                int score = Integer.parseInt(shortRow.group(3));
                String univ = shortRow.group(4);
                String name = prefixName.trim();
                // Peek at next line for name suffix
                if (i + 1 < lines.size()) {
                    String next = lines.get(i + 1).trim();
                    if (!next.isEmpty() && !ROW_START.matcher(next).matches()
                            && !ROW_NUM_TJK_SCORE_UNI.matcher(next).matches()
                            && !SKIP_LINES.contains(next)
                            && !FACULTY_LINE.matcher(next).matches()
                            && !next.contains(QUOTA_RURAL)) {
                        name = (name + " " + next).trim();
                        i++;
                    }
                }
                if (!name.isEmpty()) {
                    applicants.add(new Applicant(num, tjk, name, score, univ, quota, facCode, facName));
                }
                prefixName = ""; continue;
            }

            // Full row start: NUM TJK <name+score+univ or just name>
            Matcher rowStart = ROW_START.matcher(line);
            if (rowStart.matches()) {
                flush(applicants, pendingRow, pNum, pTjk, pName, quota, facCode, facName);
                pendingRow = false; prefixName = "";
                int num = Integer.parseInt(rowStart.group(1));
                String tjk = rowStart.group(2);
                String rest = rowStart.group(3).trim();
                Applicant a = tryBuild(num, tjk, rest, quota, facCode, facName);
                if (a != null) {
                    applicants.add(a);
                } else {
                    pendingRow = true; pNum = num; pTjk = tjk; pName = rest;
                }
                continue;
            }

            // Continuation of pending multi-line name
            if (pendingRow) {
                pName = pName + " " + line;
                Applicant a = tryBuild(pNum, pTjk, pName, quota, facCode, facName);
                if (a != null) { applicants.add(a); pendingRow = false; }
                continue;
            }

            // Name fragment appearing BEFORE the row — accumulate as prefix
            prefixName = prefixName.isEmpty() ? line : prefixName + " " + line;
        }

        flush(applicants, pendingRow, pNum, pTjk, pName, quota, facCode, facName);
        return applicants;
    }

    private Applicant tryBuild(int num, String tjk, String rest,
                                String quota, String facCode, String facName) {
        rest = rest.trim();

        // Normal: "NAME SCORE UNI"
        Matcher tail = ROW_TAIL.matcher(rest);
        if (tail.matches() && !tail.group(1).trim().isEmpty()) {
            return new Applicant(num, tjk, tail.group(1).trim(),
                    Integer.parseInt(tail.group(2)), tail.group(3), quota, facCode, facName);
        }

        // Merged: "NAME SCOREUUNI" e.g. 102013
        Matcher merged = MERGED_SCORE_UNIV.matcher(rest);
        if (merged.matches()) {
            String name = merged.group(1).trim();
            String combo = merged.group(2);
            if (combo.length() >= 5 && !name.isEmpty()) {
                String univ = combo.substring(combo.length() - 3);
                String scorePart = combo.substring(0, combo.length() - 3);
                try {
                    int score = Integer.parseInt(scorePart);
                    if (score >= 50 && score <= 200) {
                        return new Applicant(num, tjk, name, score, univ, quota, facCode, facName);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }

    private void flush(List<Applicant> list, boolean pending,
                       int num, String tjk, String name,
                       String quota, String facCode, String facName) {
        if (!pending) return;
        Applicant a = tryBuild(num, tjk, name, quota, facCode, facName);
        if (a != null) list.add(a);
    }
}
