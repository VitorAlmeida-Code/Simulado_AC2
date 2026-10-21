package com.example.meugerenciadordefilmesesries;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText edtNome, edtAno, edtNota, edtBusca;
    private Spinner spnTipo, spnGenero, spnFiltroTipo;
    private CheckBox chkAssistido, chkSoAssistidos;
    private Button btnSalvar;
    private RecyclerView recyclerFilmes;
    private List<Filme> listaFilmes = new ArrayList<>();
    private List<Filme> listaFiltrada = new ArrayList<>();
    private FilmeAdapter adapter;
    private Filme filmeEditando = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        edtNome = findViewById(R.id.edtNome);
        edtAno = findViewById(R.id.edtAno);
        edtNota = findViewById(R.id.edtNota);
        edtBusca = findViewById(R.id.edtBusca);
        spnTipo = findViewById(R.id.spnTipo);
        spnGenero = findViewById(R.id.spnGenero);
        spnFiltroTipo = findViewById(R.id.spnFiltroTipo);
        chkAssistido = findViewById(R.id.chkAssistido);
        chkSoAssistidos = findViewById(R.id.chkSoAssistidos);
        btnSalvar = findViewById(R.id.btnSalvar);
        recyclerFilmes = findViewById(R.id.recyclerFilmes);

        recyclerFilmes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FilmeAdapter(listaFiltrada);
        recyclerFilmes.setAdapter(adapter);

        adapter.setOnItemClickListener(filme -> carregarParaEdicao(filme));
        adapter.setOnItemLongClickListener(filme -> excluirFilme(filme));

        btnSalvar.setOnClickListener(v -> salvarFilme());
        findViewById(R.id.btnFiltrar).setOnClickListener(v -> aplicarFiltros());
        chkSoAssistidos.setOnCheckedChangeListener((buttonView, isChecked) -> aplicarFiltros());

        carregarFilmes();
    }


    private void carregarFilmes() {
        db.collection("filmes")
                .get()
                .addOnSuccessListener(query -> {
                    listaFilmes.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Filme f = doc.toObject(Filme.class);
                        f.setId(doc.getId());
                        listaFilmes.add(f);
                    }
                    aplicarFiltros();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }


    private void aplicarFiltros() {
        String tipoFiltro = spnFiltroTipo.getSelectedItem().toString();
        boolean soAssistidos = chkSoAssistidos.isChecked();
        String busca = edtBusca.getText().toString().trim().toLowerCase();

        listaFiltrada.clear();
        for (Filme f : listaFilmes) {
            if (!tipoFiltro.equals("Todos") && !tipoFiltro.equals(f.getTipo())) {
                continue;
            }
            if (soAssistidos && !f.isAssistido()) {
                continue;
            }
            if (!busca.isEmpty() && (f.getNome() == null || !f.getNome().toLowerCase().contains(busca))) {
                continue;
            }
            listaFiltrada.add(f);
        }
        adapter.notifyDataSetChanged();
    }

    private boolean validarCampos() {
        if (edtNome.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Informe o nome!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spnTipo.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o tipo!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (spnGenero.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione o gênero!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (edtAno.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Informe o ano de lançamento!", Toast.LENGTH_SHORT).show();
            return false;
        }
        String nota = edtNota.getText().toString().trim();
        if (nota.isEmpty()) {
            Toast.makeText(this, "Informe a nota pessoal!", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            double valor = Double.parseDouble(nota);
            if (valor < 0 || valor > 10) {
                Toast.makeText(this, "A nota deve ser entre 0 e 10!", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Nota inválida!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void salvarFilme() {
        if (!validarCampos()) {
            return;
        }

        String nome = edtNome.getText().toString().trim();
        String tipo = spnTipo.getSelectedItem().toString();
        String genero = spnGenero.getSelectedItem().toString();
        int ano = Integer.parseInt(edtAno.getText().toString().trim());
        double nota = Double.parseDouble(edtNota.getText().toString().trim());
        boolean assistido = chkAssistido.isChecked();

        if (filmeEditando == null) {
            Filme filme = new Filme(null, nome, tipo, genero, ano, nota, assistido);
            db.collection("filmes")
                    .add(filme)
                    .addOnSuccessListener(doc -> {
                        filme.setId(doc.getId());
                        Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarFilmes();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            filmeEditando.setNome(nome);
            filmeEditando.setTipo(tipo);
            filmeEditando.setGenero(genero);
            filmeEditando.setAno(ano);
            filmeEditando.setNota(nota);
            filmeEditando.setAssistido(assistido);

            db.collection("filmes").document(filmeEditando.getId())
                    .set(filmeEditando)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarFilmes();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao atualizar: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }


    private void carregarParaEdicao(Filme filme) {
        edtNome.setText(filme.getNome());
        selecionarNoSpinner(spnTipo, R.array.tipos, filme.getTipo());
        selecionarNoSpinner(spnGenero, R.array.generos, filme.getGenero());
        edtAno.setText(String.valueOf(filme.getAno()));
        edtNota.setText(String.valueOf(filme.getNota()));
        chkAssistido.setChecked(filme.isAssistido());
        filmeEditando = filme;
        btnSalvar.setText("Atualizar");
    }


    private void excluirFilme(Filme filme) {
        db.collection("filmes").document(filme.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, filme.getNome() + " excluído!", Toast.LENGTH_SHORT).show();
                    if (filmeEditando != null && filmeEditando.getId().equals(filme.getId())) {
                        limparCampos();
                    }
                    carregarFilmes();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void selecionarNoSpinner(Spinner spinner, int arrayId, String valor) {
        String[] opcoes = getResources().getStringArray(arrayId);
        for (int i = 0; i < opcoes.length; i++) {
            if (opcoes[i].equals(valor)) {
                spinner.setSelection(i);
                return;
            }
        }
        spinner.setSelection(0);
    }

    private void limparCampos() {
        edtNome.setText("");
        spnTipo.setSelection(0);
        spnGenero.setSelection(0);
        edtAno.setText("");
        edtNota.setText("");
        chkAssistido.setChecked(false);
        filmeEditando = null;
        btnSalvar.setText("Salvar");
    }
}
