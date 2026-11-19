package com.example.my_archive;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * База данных SQLite: таблицы users, categories, doc_types, archive_cases.
 * Все данные вымышлены и нужны только для учебного проекта.
 * Используется один общий экземпляр на всё приложение (DbHelper.get).
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "archive.db";
    private static final int DB_VERSION = 1;

    /** Столбцы, по которым разрешена сортировка. */
    public static final String[] SORT_COLUMNS = {
            "c.case_number", "c.title", "c.case_year", "c.receipt_date"
    };

    /** Элемент справочника (категория или тип документа) для Spinner. */
    public static class RefItem {
        public final long id;
        public final String name;

        public RefItem(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static DbHelper instance;

    public static synchronized DbHelper get(Context context) {
        if (instance == null) {
            instance = new DbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "login TEXT NOT NULL UNIQUE, "
                + "password_hash TEXT NOT NULL, "
                + "full_name TEXT NOT NULL, "
                + "role TEXT NOT NULL)");

        db.execSQL("CREATE TABLE categories ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL UNIQUE)");

        db.execSQL("CREATE TABLE doc_types ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL UNIQUE)");

        db.execSQL("CREATE TABLE archive_cases ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "case_number TEXT NOT NULL UNIQUE, "
                + "title TEXT NOT NULL, "
                + "case_year INTEGER NOT NULL, "
                + "category_id INTEGER NOT NULL REFERENCES categories(_id), "
                + "doc_type_id INTEGER NOT NULL REFERENCES doc_types(_id), "
                + "receipt_date TEXT NOT NULL, "
                + "location TEXT NOT NULL, "
                + "description TEXT)");

        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS archive_cases");
        db.execSQL("DROP TABLE IF EXISTS doc_types");
        db.execSQL("DROP TABLE IF EXISTS categories");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // ---------------------------------------------------------------- начальные данные

    private void seed(SQLiteDatabase db) {
        insertUser(db, "admin", "admin123", "Администратор архива", "admin");
        insertUser(db, "archivist", "archive2026", "Сотрудник архива", "archivist");

        long catHr = insertRef(db, "categories", "Кадровые документы");
        long catFin = insertRef(db, "categories", "Финансовые документы");
        long catOrg = insertRef(db, "categories", "Организационно-распорядительные");
        long catEdu = insertRef(db, "categories", "Учебная документация");
        long catCon = insertRef(db, "categories", "Договорные документы");

        long tOrder = insertRef(db, "doc_types", "Приказ");
        long tContract = insertRef(db, "doc_types", "Договор");
        long tAct = insertRef(db, "doc_types", "Акт");
        long tFile = insertRef(db, "doc_types", "Личное дело");
        long tSheet = insertRef(db, "doc_types", "Ведомость");
        long tReport = insertRef(db, "doc_types", "Отчёт");
        long tProtocol = insertRef(db, "doc_types", "Протокол");

        seedCase(db, "125/2024", "Приказы по личному составу за 2024 год", 2024,
                catHr, tOrder, "2024-03-15", "Стеллаж 2, полка 4",
                "Приказы о приёме, переводе и увольнении (вымышленные данные)");
        seedCase(db, "126/2024", "Личные дела сотрудников, поступивших в 2024 году", 2024,
                catHr, tFile, "2024-04-02", "Стеллаж 2, полка 5",
                "Учебный пример, сведения вымышлены");
        seedCase(db, "041/2023", "Ведомости начисления заработной платы за 2023 год", 2023,
                catFin, tSheet, "2024-01-20", "Стеллаж 3, полка 1",
                "Годовой комплект ведомостей");
        seedCase(db, "042/2023", "Акты выполненных работ за 2023 год", 2023,
                catFin, tAct, "2024-01-22", "Стеллаж 3, полка 2",
                "Акты по договорам подряда");
        seedCase(db, "310/2022", "Протоколы заседаний педагогического совета 2022 года", 2022,
                catOrg, tProtocol, "2023-02-10", "Стеллаж 1, полка 3",
                "Протоколы №1–№10");
        seedCase(db, "311/2022", "Приказы по основной деятельности за 2022 год", 2022,
                catOrg, tOrder, "2023-02-14", "Стеллаж 1, полка 3",
                "Приказы директора");
        seedCase(db, "201/2025", "Договоры на поставку оборудования за 2025 год", 2025,
                catCon, tContract, "2025-12-05", "Стеллаж 4, полка 2",
                "Договоры с поставщиками (вымышленные организации)");
        seedCase(db, "202/2025", "Договоры на оказание услуг связи за 2025 год", 2025,
                catCon, tContract, "2025-12-09", "Стеллаж 4, полка 2",
                "Договоры и дополнительные соглашения");
        seedCase(db, "087/2021", "Учебные планы и рабочие программы 2021 года", 2021,
                catEdu, tReport, "2022-06-30", "Стеллаж 5, полка 1",
                "Комплект учебно-методической документации");
        seedCase(db, "088/2021", "Ведомости промежуточной аттестации за 2021 год", 2021,
                catEdu, tSheet, "2022-07-04", "Стеллаж 5, полка 2",
                "Сводные ведомости по группам");
        seedCase(db, "150/2024", "Годовой финансовый отчёт за 2024 год", 2024,
                catFin, tReport, "2025-02-18", "Стеллаж 3, полка 4",
                "Отчёт и приложения к нему");
        seedCase(db, "151/2025", "Протоколы заседаний методических комиссий 2025 года", 2025,
                catEdu, tProtocol, "2025-11-12", "Стеллаж 5, полка 3",
                "Протоколы №1–№8");
    }

    private void insertUser(SQLiteDatabase db, String login, String password,
                            String fullName, String role) {
        ContentValues v = new ContentValues();
        v.put("login", login);
        v.put("password_hash", Utils.sha256(password));
        v.put("full_name", fullName);
        v.put("role", role);
        db.insert("users", null, v);
    }

    private long insertRef(SQLiteDatabase db, String table, String name) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        return db.insert(table, null, v);
    }

    private void seedCase(SQLiteDatabase db, String number, String title, int year,
                          long categoryId, long docTypeId, String receiptDate,
                          String location, String description) {
        ContentValues v = new ContentValues();
        v.put("case_number", number);
        v.put("title", title);
        v.put("case_year", year);
        v.put("category_id", categoryId);
        v.put("doc_type_id", docTypeId);
        v.put("receipt_date", receiptDate);
        v.put("location", location);
        v.put("description", description);
        db.insert("archive_cases", null, v);
    }

    // ---------------------------------------------------------------- авторизация

    /** Возвращает имя сотрудника, если логин и пароль верны, иначе null. */
    public String authenticate(String login, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT full_name FROM users WHERE login = ? AND password_hash = ?",
                new String[]{login.trim(), Utils.sha256(password)});
        try {
            return c.moveToFirst() ? c.getString(0) : null;
        } finally {
            c.close();
        }
    }

    // ---------------------------------------------------------------- справочники

    public List<RefItem> getCategories() {
        return getRef("categories");
    }

    public List<RefItem> getDocTypes() {
        return getRef("doc_types");
    }

    private List<RefItem> getRef(String table) {
        List<RefItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT _id, name FROM " + table + " ORDER BY name", null);
        try {
            while (c.moveToNext()) {
                list.add(new RefItem(c.getLong(0), c.getString(1)));
            }
        } finally {
            c.close();
        }
        return list;
    }

    /** Годы, за которые есть дела (для фильтра). */
    public List<Integer> getYears() {
        List<Integer> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT DISTINCT case_year FROM archive_cases ORDER BY case_year DESC", null);
        try {
            while (c.moveToNext()) {
                list.add(c.getInt(0));
            }
        } finally {
            c.close();
        }
        return list;
    }

    // ---------------------------------------------------------------- дела: чтение

    private static final String SELECT_CASES =
            "SELECT c._id, c.case_number, c.title, c.case_year, "
                    + "c.category_id, k.name, c.doc_type_id, t.name, "
                    + "c.receipt_date, c.location, c.description "
                    + "FROM archive_cases c "
                    + "JOIN categories k ON k._id = c.category_id "
                    + "JOIN doc_types t ON t._id = c.doc_type_id ";

    private ArchiveCase readCase(Cursor c) {
        ArchiveCase a = new ArchiveCase();
        a.id = c.getLong(0);
        a.caseNumber = c.getString(1);
        a.title = c.getString(2);
        a.year = c.getInt(3);
        a.categoryId = c.getLong(4);
        a.categoryName = c.getString(5);
        a.docTypeId = c.getLong(6);
        a.docTypeName = c.getString(7);
        a.receiptDate = c.getString(8);
        a.location = c.getString(9);
        a.description = c.getString(10);
        return a;
    }

    /**
     * Список дел с поиском, фильтрами и сортировкой.
     *
     * @param search     часть номера или названия (пусто - без поиска)
     * @param year       год (0 - любой)
     * @param categoryId категория (0 - любая)
     * @param docTypeId  тип документа (0 - любой)
     * @param sortIndex  индекс в SORT_COLUMNS
     * @param ascending  true - по возрастанию
     */
    public List<ArchiveCase> getCases(String search, int year, long categoryId,
                                      long docTypeId, int sortIndex, boolean ascending) {
        StringBuilder sql = new StringBuilder(SELECT_CASES).append("WHERE 1 = 1 ");
        List<String> args = new ArrayList<>();

        if (year > 0) {
            sql.append("AND c.case_year = ? ");
            args.add(String.valueOf(year));
        }
        if (categoryId > 0) {
            sql.append("AND c.category_id = ? ");
            args.add(String.valueOf(categoryId));
        }
        if (docTypeId > 0) {
            sql.append("AND c.doc_type_id = ? ");
            args.add(String.valueOf(docTypeId));
        }

        int idx = (sortIndex >= 0 && sortIndex < SORT_COLUMNS.length) ? sortIndex : 0;
        sql.append("ORDER BY ").append(SORT_COLUMNS[idx])
                .append(ascending ? " ASC" : " DESC");

        // Поиск делаем в Java: LIKE в SQLite не различает регистр только для латиницы.
        String needle = search == null ? "" : search.trim().toLowerCase(Locale.getDefault());

        List<ArchiveCase> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(sql.toString(),
                args.toArray(new String[0]));
        try {
            while (c.moveToNext()) {
                ArchiveCase a = readCase(c);
                if (needle.isEmpty()
                        || a.caseNumber.toLowerCase(Locale.getDefault()).contains(needle)
                        || a.title.toLowerCase(Locale.getDefault()).contains(needle)) {
                    list.add(a);
                }
            }
        } finally {
            c.close();
        }
        return list;
    }

    public ArchiveCase getCase(long id) {
        Cursor c = getReadableDatabase().rawQuery(
                SELECT_CASES + "WHERE c._id = ?", new String[]{String.valueOf(id)});
        try {
            return c.moveToFirst() ? readCase(c) : null;
        } finally {
            c.close();
        }
    }

    /** Занят ли номер дела другим делом (excludeId - id редактируемого дела или -1). */
    public boolean isNumberTaken(String number, long excludeId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT 1 FROM archive_cases WHERE case_number = ? AND _id <> ?",
                new String[]{number, String.valueOf(excludeId)});
        try {
            return c.moveToFirst();
        } finally {
            c.close();
        }
    }

    // ---------------------------------------------------------------- дела: изменение

    private ContentValues toValues(ArchiveCase a) {
        ContentValues v = new ContentValues();
        v.put("case_number", a.caseNumber);
        v.put("title", a.title);
        v.put("case_year", a.year);
        v.put("category_id", a.categoryId);
        v.put("doc_type_id", a.docTypeId);
        v.put("receipt_date", a.receiptDate);
        v.put("location", a.location);
        v.put("description", a.description);
        return v;
    }

    /** Добавляет дело, возвращает его id (или -1 при ошибке). */
    public long insertCase(ArchiveCase a) {
        return getWritableDatabase().insert("archive_cases", null, toValues(a));
    }

    /** Обновляет дело, возвращает true, если запись изменена. */
    public boolean updateCase(ArchiveCase a) {
        return getWritableDatabase().update("archive_cases", toValues(a),
                "_id = ?", new String[]{String.valueOf(a.id)}) > 0;
    }

    /** Удаляет дело, возвращает true, если запись удалена. */
    public boolean deleteCase(long id) {
        return getWritableDatabase().delete("archive_cases",
                "_id = ?", new String[]{String.valueOf(id)}) > 0;
    }
}

