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
    // Danh sách các bàn được hiển thị
    private List<TableOrder> tables;
    // Context để lấy tài nguyên và xử lý UI
    private Context context;
    // Listener để xử lý sự kiện khi nhấn nút "Clear"
    private OnClearClickListener listener;

    // Interface để giao tiếp callback khi nhấn nút Clear
    public interface OnClearClickListener {
        void onClear(TableOrder table); // truyền bàn được nhấn nút Clear
    }

    // Constructor nhận vào context, danh sách bàn và listener xử lý sự kiện
    public TableAdapter(Context context, List<TableOrder> tables, OnClearClickListener listener) {
        this.context = context;
        this.tables = tables;
        this.listener = listener;
    }

    // Tạo ViewHolder cho từng item bàn
    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_table.xml thành View
        View view = LayoutInflater.from(context).inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(view);
    }

    // Gán dữ liệu cho từng item bàn khi hiển thị
    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        // Lấy bàn ở vị trí hiện tại
        TableOrder table = tables.get(position);
        // Hiển thị tên bàn
        holder.tableNameTextView.setText(table.getNameTable());
        // Gán sự kiện khi nhấn nút Clear cho bàn hiện tại
        holder.clearButton.setOnClickListener(v -> listener.onClear(table));
    }

    // Trả về tổng số bàn trong danh sách
    @Override
    public int getItemCount() {
        return tables.size();
    }

    // ViewHolder dùng để giữ tham chiếu các view con trong item bàn
    public static class TableViewHolder extends RecyclerView.ViewHolder {
        TextView tableNameTextView; // Hiển thị tên bàn
        Button clearButton;          // Nút xóa hoặc làm trống bàn

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ view từ layout xml
            tableNameTextView = itemView.findViewById(R.id.tableNameTextView);
            clearButton = itemView.findViewById(R.id.clearButton);
        }
    }
}
