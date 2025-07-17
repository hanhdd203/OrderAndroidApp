package vn.edu.fpt.shopapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import vn.edu.fpt.shopapp.R;
import vn.edu.fpt.shopapp.entity.TableOrder;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {
    private List<TableOrder> tables;
    private Context context;
    private OnClearClickListener listener;

    public interface OnClearClickListener {
        void onClear(TableOrder table);
    }

    public TableAdapter(Context context, List<TableOrder> tables, OnClearClickListener listener) {
        this.context = context;
        this.tables = tables;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        TableOrder table = tables.get(position);
        holder.tableNameTextView.setText(table.getNameTable());
        holder.clearButton.setOnClickListener(v -> listener.onClear(table));
    }

    @Override
    public int getItemCount() {
        return tables.size();
    }

    public static class TableViewHolder extends RecyclerView.ViewHolder {
        TextView tableNameTextView;
        Button clearButton;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tableNameTextView = itemView.findViewById(R.id.tableNameTextView);
            clearButton = itemView.findViewById(R.id.clearButton);
        }
    }
}
