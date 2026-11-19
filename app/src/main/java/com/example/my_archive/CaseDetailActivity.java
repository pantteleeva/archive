package com.example.my_archive;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/** Окно просмотра подробной информации о деле с кнопками «Редактировать» и «Удалить». */
public class CaseDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CASE_ID = "case_id";

    private DbHelper db;
    private long caseId;
    private ArchiveCase current;

    private TextView tvNumber;
    private TextView tvTitle;
    private TextView tvYear;
    private TextView tvCategory;
    private TextView tvType;
    private TextView tvDate;
    private TextView tvLocation;
    private TextView tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Session.require(this)) {
            return;
        }
        setContentView(R.layout.activity_case_detail);
        setTitle("Карточка дела");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = DbHelper.get(this);
        caseId = getIntent().getLongExtra(EXTRA_CASE_ID, -1);

        tvNumber = findViewById(R.id.tvNumber);
        tvTitle = findViewById(R.id.tvTitle);
        tvYear = findViewById(R.id.tvYear);
        tvCategory = findViewById(R.id.tvCategory);
        tvType = findViewById(R.id.tvType);
        tvDate = findViewById(R.id.tvDate);
        tvLocation = findViewById(R.id.tvLocation);
        tvDescription = findViewById(R.id.tvDescription);

        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CaseDetailActivity.this, CaseEditActivity.class);
                i.putExtra(CaseEditActivity.EXTRA_CASE_ID, caseId);
                startActivity(i);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db == null) {
            return;
        }
        // Перечитываем данные: дело могли отредактировать на соседнем экране
        current = db.getCase(caseId);
        if (current == null) {
            Toast.makeText(this, "Дело не найдено", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvNumber.setText(current.caseNumber);
        tvTitle.setText(current.title);
        tvYear.setText(String.valueOf(current.year));
        tvCategory.setText(current.categoryName);
        tvType.setText(current.docTypeName);
        tvDate.setText(Utils.isoToDisplay(current.receiptDate));
        tvLocation.setText(current.location);
        tvDescription.setText(
                current.description == null || current.description.isEmpty()
                        ? "—" : current.description);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void confirmDelete() {
        if (current == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Удаление дела")
                .setMessage("Удалить дело «" + current.caseNumber + "»? Это действие нельзя отменить.")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            db.deleteCase(caseId);
                            Toast.makeText(CaseDetailActivity.this, "Дело удалено",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(CaseDetailActivity.this,
                                    "Ошибка удаления: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("Нет", null)
                .show();
    }
}
