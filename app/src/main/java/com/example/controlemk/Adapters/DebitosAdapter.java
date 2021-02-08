package com.example.controlemk.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controlemk.Debitos;
import com.example.controlemk.DetalhesVenda;
import com.example.controlemk.Dominio.Entidades.Venda;
import com.example.controlemk.Dominio.Repositorio.VendasRepositorio;
import com.example.controlemk.OperacoesDatas;
import com.example.controlemk.R;

import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DebitosAdapter extends RecyclerView.Adapter<DebitosAdapter.ViewHolderCliente>{

    private List<Venda> dados;
    private Context context;
    public TextView txtTotDebitos;
    public String ordem;

    public DebitosAdapter(List<Venda> dados, Context context, TextView txtTotDebitos, String ordem) {
        this.dados = dados;
        this.context = context;
        this.txtTotDebitos = txtTotDebitos;
        this.ordem = ordem;
    }

    @NonNull
    @Override
    public DebitosAdapter.ViewHolderCliente onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAG","onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view =layoutInflater.inflate(R.layout.linha_debitos, parent, false);

        DebitosAdapter.ViewHolderCliente holderProduto = new DebitosAdapter.ViewHolderCliente(view,parent.getContext());

        return holderProduto;
    }

    @Override
    public void onBindViewHolder(final DebitosAdapter.ViewHolderCliente holder, final int position) {

        String TAG = "Debitos Adapter";
        Log.d(TAG, "onBindViewHolder: posição: " + position);
        VendasRepositorio vendRep = new VendasRepositorio(context);
        if ((dados != null) && (dados.size() > 0)) {
            final Venda venda = dados.get(position);
            Venda vendaNova = vendRep.buscarVenda(venda.id);
            venda.datasNaoPagas = vendaNova.datasNaoPagas;
            holder.txtNome.setText(venda.nome);
            float totParc = Float.parseFloat(venda.datasPag.get(0).split("=")[1]);
            holder.txtValParcela.setText(String.format("R$%.2f", totParc));
            holder.txtDataPagamento.setText(venda.datasPag.get(0).split("=")[0]);
            if (venda.datasNaoPagas.contains(venda.datasPag.get(0))) {
                holder.botaoPag.setChecked(false);
            } else {
                holder.botaoPag.setChecked(true);
            }
            OperacoesDatas op = new OperacoesDatas(context);
            int difDatas = op.subtracaoDatas(venda.datasPag.get(0), op.dataAtual());
            if (holder.botaoPag.isChecked()) {
                holder.txtStatus.setText("PAGO");
                holder.txtStatus.setTextColor(Color.GREEN);
            } else {
                if (difDatas >= 0) {
                    holder.txtStatus.setText("EM DIA");
                    holder.txtStatus.setTextColor(Color.BLACK);
                } else {
                    holder.txtStatus.setText("ATRASADO");
                    holder.txtStatus.setTextColor(Color.RED);
                }
            }
            holder.botaoPag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VendasRepositorio vendRep = new VendasRepositorio(context);
                    if (holder.botaoPag.isChecked()) {
                        venda.datasNaoPagas.remove(venda.datasPag.get(0));
                        holder.txtStatus.setText("PAGO");
                        holder.txtStatus.setTextColor(Color.GREEN);
                    } else {
                        venda.datasNaoPagas.add(venda.datasPag.get(0));
                        OperacoesDatas op = new OperacoesDatas(context);
                        int difDatas = op.subtracaoDatas(venda.datasPag.get(0), op.dataAtual());
                        if (difDatas >= 0) {
                            holder.txtStatus.setText("EM DIA");
                            holder.txtStatus.setTextColor(Color.BLACK);
                        } else {
                            holder.txtStatus.setText("ATRASADO");
                            holder.txtStatus.setTextColor(Color.RED);
                        }
                    }
                    Venda venda2 = vendRep.buscarVenda(venda.id);
                    venda2.datasNaoPagas = venda.datasNaoPagas;
                    vendRep.alterar(venda2);
                    if (!ordem.contentEquals("Todas as Parcelas")) {
                        float totDebitos = 0.0f;
                        for(Venda vend:dados){
                            if (!vend.datasPag.get(0).contentEquals(venda2.datasPag.get(0))) {
                                totDebitos += Float.parseFloat(vend.datasPag.get(0).split("=")[1]);
                            }
                        }
                        txtTotDebitos.setText(String.format("R$%.2f",totDebitos));
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }

    public class ViewHolderCliente extends RecyclerView.ViewHolder{

        public TextView txtNome, txtDataPagamento, txtValParcela, txtStatus;
        public ToggleButton botaoPag;

        public ViewHolderCliente(@NonNull View itemView, final Context context) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNome);
            txtDataPagamento = itemView.findViewById(R.id.txtDataPagamento);
            txtValParcela = itemView.findViewById(R.id.txtValParcela);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            botaoPag = itemView.findViewById(R.id.botaoPagar);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dados.size() > 0) {
                        VendasRepositorio vendRep = new VendasRepositorio(context);
                        Venda venda = vendRep.buscarVenda(dados.get(getLayoutPosition()).id);
                        Intent it = new Intent(context, DetalhesVenda.class);
                        it.putExtra("VENDA", venda);
                        ((AppCompatActivity) context).startActivityForResult(it, 2);
                    }
                }
            });
        }
    }
}
