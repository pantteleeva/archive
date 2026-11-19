package com.example.my_archive;



import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Окно добавления нового дела и редактирования существующего. */
public class CaseEditActivity extends AppCompatActivity {

    public static final String EXTRA_CASE_ID = "case_id";

    private DbHelper db;
    private long caseId = -1;

    private EditText etNumber;
    private EditText etTitle;
    private EditText etYear;
    private EditText etDate;
    private EditText etLocation;
    private EditText etDescription;
    private Spinner spCategory;
    private Spinner spType;
    private List<DbHelper.RefItem> categories;
    private List<DbHelper.RefItem> types;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Session.require(this)) {
            return;
        }
        setContentView(R.layout.activity_case_edit);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = DbHelper.get(this);
        caseId = getIntent().getLongExtra(EXTRA_CASE_ID, -1);

        etNumber = findViewById(R.id.etNumber);
        etTitle = findViewById(R.id.etTitle);
        etYear = findViewById(R.id.etYear);
        etDate = findViewById(R.id.etDate);
        etLocation = findViewById(R.id.etLocation);
        etDescription = findViewById(R.id.etDescription);
        spCategory = findViewById(R.id.spCategory);
        spType = findViewById(R.id.spType);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        categories = db.getCategories();
        types = db.getDocTypes();
        spCategory.setAdapter(new ArrayAdapter<DbHelper.RefItem>(this,
                android.R.layout.simple_spinner_dropdown_item, categories));
        spType.setAdapter(new ArrayAdapter<DbHelper.RefItem>(this,
                android.R.layout.simple_spinner_dropdown_item, types));

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Заполняем форму только при первом создании окна (при повороте экрана
        // введённые значения восстанавливаются системой)
        if (savedInstanceState == null) {
            if (caseId > 0) {
                setTitle("Редактирование дела");
                fillForm(db.getCase(caseId));
            } else {
                setTitle("Новое дело");
                etDate.setText(Utils.isoToDisplay(Utils.todayIso()));
            }
        } else {
            setTitle(caseId > 0 ? "Редактирование дела" : "Новое дело");
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void fillForm(ArchiveCase c) {
        if (c == null) {
            Toast.makeText(this, "Дело не найдено", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        etNumber.setText(c.caseNumber);
        etTitle.setText(c.title);
        etYear.setText(String.valueOf(c.year));
        etDate.setText(Utils.isoToDisplay(c.receiptDate));
        etLocation.setText(c.location);
        etDescription.setText(c.description);
        spCategory.setSelection(indexOf(categories, c.categoryId));
        spType.setSelection(indexOf(types, c.docTypeId));
    }

    private int indexOf(List<DbHelper.RefItem> list, long id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == id) {
                return i;
            }
        }
        return 0;
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        String iso = Utils.displayToIso(etDate.getText().toString());
        if (iso.matches("\\d{4}-\\d{2}-\\d{2}")) {
            cal.set(Integer.parseInt(iso.substring(0, 4)),
                    Integer.parseInt(iso.substring(5, 7)) - 1,
                    Integer.parseInt(iso.substring(8, 10)));
        }
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                etDate.setText(String.format(Locale.US, "%02d.%02d.%04d", day, month + 1, year));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void save() {
        String number = etNumber.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String yearText = etYear.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        boolean ok = true;
        if (number.isEmpty()) {
            etNumber.setError("Заполните это поле");
            ok = false;
        }
        if (title.isEmpty()) {
            etTitle.setError("Заполните это поле");
            ok = false;
        }
        if (location.isEmpty()) {
            etLocation.setError("Заполните это поле");
            ok = false;
        }

        int year = 0;
        try {
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException ignored) {
        }
        if (year < 1900 || year > 2100) {
            etYear.setError("Укажите год от 1900 до 2100");
            ok = false;
        }

        if (!ok) {
            return;
        }

        Object cat = spCategory.getSelectedItem();
        Object typ = spType.getSelectedItem();
        if (!(cat instanceof DbHelper.RefItem) || !(typ instanceof DbHelper.RefItem)) {
            Toast.makeText(this, "Выберите категорию и тип документа", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (db.isNumberTaken(number, caseId)) {
                etNumber.setError("Дело с таким номером уже есть");
                return;
            }

            ArchiveCase c = new ArchiveCase();
            c.id = caseId;
            c.caseNumber = number;
            c.title = title;
            c.year = year;
            c.categoryId = ((DbHelper.RefItem) cat).id;
            c.docTypeId = ((DbHelper.RefItem) typ).id;
            c.receiptDate = Utils.displayToIso(etDate.getText().toString());
            c.location = location;
            c.description = description;

            boolean saved;
            if (caseId > 0) {
                saved = db.updateCase(c);
            } else {
                saved = db.insertCase(c) > 0;
            }

            if (saved) {
                Toast.makeText(this, "Дело сохранено", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Не удалось сохранить дело", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
