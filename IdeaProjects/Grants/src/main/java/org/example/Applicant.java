package org.example;

/**
 * Represents a single grant applicant from the PDF list.
 */
public class Applicant {

    private int number;
    private String tjk;       // Individual ID code (ТЖК)
    private String fullName;  // Last name, First name, Patronymic
    private int score;        // Total score (Жалпы балл)
    private String university; // University code (ЖЖОКБҰ)
    private String quota;     // "ЖАЛПЫ КОНКУРС" or "АУЫЛ КВОТАСЫ"
    private String facultyCode;
    private String facultyName;

    public Applicant(int number, String tjk, String fullName, int score,
                     String university, String quota, String facultyCode, String facultyName) {
        this.number = number;
        this.tjk = tjk;
        this.fullName = fullName;
        this.score = score;
        this.university = university;
        this.quota = quota;
        this.facultyCode = facultyCode;
        this.facultyName = facultyName;
    }

    public int getNumber()        { return number; }
    public String getTjk()        { return tjk; }
    public String getFullName()   { return fullName; }
    public int getScore()         { return score; }
    public String getUniversity() { return university; }
    public String getQuota()      { return quota; }
    public String getFacultyCode(){ return facultyCode; }
    public String getFacultyName(){ return facultyName; }

    @Override
    public String toString() {
        return String.format("  %3d. %-45s | ТЖК: %s | Балл: %3d | Универ: %s",
                number, fullName, tjk, score, university);
    }
}
