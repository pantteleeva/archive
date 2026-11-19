package com.example.my_archive;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/** Главное окно: список дел, поиск, фильтры, сортировка, добавление, правка и удаление. */
public class MainActivity extends AppCompatActivity {

    private static final String[] SORT_NAMES = {
            "Сортировка: номер дела", "Сортировка: название",
            "Сортировка: год", "Сортировка: дата поступления"
    };

    private DbHelper db;
    private EditText etSearch;
    private Spinner spYear;
    private Spinner spCategory;
    private Spinner spType;
    private Spinner spSort;
    private Button btnOrder;
    private View panelFilters;
    private TextView tvCount;
    private ListView lvCases;
    private CaseAdapter adapter;

    private final List<String> yearItems = new ArrayList<>();
    private ArrayAdapter<String> yearAdapter;
    private boolean ascending = true;
    private boolean ready = false;

    private final AdapterView.OnItemSelectedListener reloadListener =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (ready) {
                        loadCases();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Session.require(this)) {
            return;
        }
        setContentView(R.layout.activity_main);
        setTitle("Архив");

        db = DbHelper.get(this);

        ((TextView) findViewById(R.id.tvUser)).setText("Сотрудник: " + Session.userName);

        etSearch = findViewById(R.id.etSearch);
        panelFilters = findViewById(R.id.panelFilters);
        spYear = findViewById(R.id.spYear);
        spCategory = findViewById(R.id.spCategory);
        spType = findViewById(R.id.spType);
        spSort = findViewById(R.id.spSort);
        btnOrder = findViewById(R.id.btnOrder);
        tvCount = findViewById(R.id.tvCount);
        lvCases = findViewById(R.id.lvCases);
        Button btnToggle = findViewById(R.id.btnToggleFilters);
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnReset = findViewById(R.id.btnReset);

        // Список
        adapter = new CaseAdapter(this);
        lvCases.setAdapter(adapter);
        lvCases.setEmptyView(findViewById(R.id.tvEmpty));

        lvCases.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openDetail(adapter.getItem(position).id);
            }
        });
        lvCases.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showActions(adapter.getItem(position));
                return true;
            }
        });

        // Фильтры
        yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, yearItems);
        spYear.setAdapter(yearAdapter);

        List<DbHelper.RefItem> categories = new ArrayList<>();
        categories.add(new DbHelper.RefItem(0, "Все категории"));
        categories.addAll(db.getCategories());
        spCategory.setAdapter(new ArrayAdapter<DbHelper.RefItem>(this,
                android.R.layout.simple_spinner_dropdown_item, categories));

        List<DbHelper.RefItem> types = new ArrayList<>();
        types.add(new DbHelper.RefItem(0, "Все типы документов"));
        types.addAll(db.getDocTypes());
        spType.setAdapter(new ArrayAdapter<DbHelper.RefItem>(this,
                android.R.layout.simple_spinner_dropdown_item, types));

        spSort.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, SORT_NAMES));

        spYear.setOnItemSelectedListener(reloadListener);
        spCategory.setOnItemSelectedListener(reloadListener);
        spType.setOnItemSelectedListener(reloadListener);
        spSort.setOnItemSelectedListener(reloadListener);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (ready) {
                    loadCases();
                }
            }
        });

        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panelFilters.setVisibility(
                        panelFilters.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ascending = !ascending;
                updateOrderButton();
                loadCases();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ready = false;
                etSearch.setText("");
                spYear.setSelection(0);
                spCategory.setSelection(0);
                spType.setSelection(0);
                spSort.setSelection(0);
                ascending = true;
                updateOrderButton();
                ready = true;
                loadCases();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CaseEditActivity.class));
            }
        });

        updateOrderButton();
        ready = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ready) {
            return;
        }
        refreshYears();
        loadCases();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Выйти").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Session.logout(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ------------------------------------------------------------------ действия с делом

    private void openDetail(long id) {
        Intent i = new Intent(this, CaseDetailActivity.class);
        i.putExtra(CaseDetailActivity.EXTRA_CASE_ID, id);
        startActivity(i);
    }

    private void openEdit(long id) {
        Intent i = new Intent(this, CaseEditActivity.class);
        i.putExtra(CaseEditActivity.EXTRA_CASE_ID, id);
        startActivity(i);
    }

    /** Меню по долгому нажатию на дело: открыть, редактировать, удалить. */
    private void showActions(final ArchiveCase item) {
        String[] actions = {"Открыть", "Редактировать", "Удалить"};
        new AlertDialog.Builder(this)
                .setTitle("№ " + item.caseNumber)
                .setItems(actions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            openDetail(item.id);
                        } else if (which == 1) {
                            openEdit(item.id);
                        } else {
                            confirmDelete(item);
                        }
                    }
                })
                .show();
    }

    private void confirmDelete(final ArchiveCase item) {
        new AlertDialog.Builder(this)
                .setTitle("Удаление дела")
                .setMessage("Удалить дело «" + item.caseNumber + "»? Это действие нельзя отменить.")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            db.deleteCase(item.id);
                            Toast.makeText(MainActivity.this, "Дело удалено",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Ошибка удаления: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                        refreshYears();
                        loadCases();
                    }
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    // ------------------------------------------------------------------ вспомогательное

    private void updateOrderButton() {
        btnOrder.setText(ascending ? "По возрастанию ↑" : "По убыванию ↓");
    }

    /** Обновляет список годов в фильтре, сохраняя выбранное значение. */
    private void refreshYears() {
        String previous = selectedYear() > 0 ? String.valueOf(selectedYear()) : null;

        boolean wasReady = ready;
        ready = false;
        yearItems.clear();
        yearItems.add("Все годы");
        for (Integer y : db.getYears()) {
            yearItems.add(String.valueOf(y));
        }
        yearAdapter.notifyDataSetChanged();
        int pos = previous == null ? 0 : yearItems.indexOf(previous);
        spYear.setSelection(Math.max(pos, 0));
        ready = wasReady;
    }

    private int selectedYear() {
        int pos = spYear.getSelectedItemPosition();
        if (pos <= 0 || pos >= yearItems.size()) {
            return 0;
        }
        try {
            return Integer.parseInt(yearItems.get(pos));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long selectedId(Spinner spinner) {
        Object o = spinner.getSelectedItem();
        return (o instanceof DbHelper.RefItem) ? ((DbHelper.RefItem) o).id : 0;
    }

    private void loadCases() {
        List<ArchiveCase> list = db.getCases(
                etSearch.getText().toString(),
                selectedYear(),
                selectedId(spCategory),
                selectedId(spType),
                spSort.getSelectedItemPosition(),
                ascending);
        adapter.setItems(list);
        tvCount.setText("Найдено дел: " + list.size());
    }
}
