package com.example.meugerenciadordefilmesesries;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FilmeAdapter extends RecyclerView.Adapter<FilmeAdapter.FilmeViewHolder> {

    private List<Filme> listaFilmes;
    private OnItemClickListener listener;
    private OnItemLongClickListener longListener;


    public interface OnItemClickListener {
        void onItemClick(Filme filme);
    }


    public interface OnItemLongClickListener {
        void onItemLongClick(Filme filme);
    }

    public FilmeAdapter(List<Filme> listaFilmes) {
        this.listaFilmes = listaFilmes;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longListener) {
        this.longListener = longListener;
    }

    @NonNull
    @Override
    public FilmeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filme, parent, false);
        return new FilmeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmeViewHolder holder, int position) {
        Filme filme = listaFilmes.get(position);

        holder.txtNome.setText(filme.getNome());
        holder.txtTipoGenero.setText(filme.getTipo() + " - " + filme.getGenero());
        String assistido = filme.isAssistido() ? "Assistido" : "Não assistido";
        holder.txtAnoNota.setText("Ano: " + filme.getAno() + " | Nota: " + filme.getNota() + " | " + assistido);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(filme);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                longListener.onItemLongClick(filme);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaFilmes.size();
    }

    static class FilmeViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtTipoGenero, txtAnoNota;

        FilmeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNome);
            txtTipoGenero = itemView.findViewById(R.id.txtTipoGenero);
            txtAnoNota = itemView.findViewById(R.id.txtAnoNota);
        }
    }
}
