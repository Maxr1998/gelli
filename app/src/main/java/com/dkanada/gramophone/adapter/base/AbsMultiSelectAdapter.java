package com.dkanada.gramophone.adapter.base;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialcab.MaterialCab;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.interfaces.CabHolder;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsMultiSelectAdapter<VH extends RecyclerView.ViewHolder, I> extends RecyclerView.Adapter<VH> implements MaterialCab.Callback {
    private final Context context;
    private final CabHolder cabHolder;
    private int menuRes;

    private MaterialCab cab;
    private List<I> checked;

    public AbsMultiSelectAdapter(Context context, @Nullable CabHolder cabHolder, @MenuRes int menuRes) {
        this.context = context;
        this.cabHolder = cabHolder;
        this.menuRes = menuRes;

        this.checked = new ArrayList<>();
    }

    protected void setMultiSelectMenuRes(@MenuRes int menuRes) {
        this.menuRes = menuRes;
    }

    protected boolean toggleChecked(final int position) {
        if (cabHolder != null) {
            I identifier = getIdentifier(position);
            if (identifier == null) return false;

            if (!checked.remove(identifier)) checked.add(identifier);

            notifyItemChanged(position);
            updateCab();
            return true;
        }

        return false;
    }

    protected void checkAll() {
        if (cabHolder != null) {
            checked.clear();
            for (int i = 0; i < getItemCount(); i++) {
                I identifier = getIdentifier(i);
                if (identifier != null) {
                    checked.add(identifier);
                }
            }

            notifyDataSetChanged();
            updateCab();
        }
    }

    private void updateCab() {
        if (cabHolder != null) {
            if (cab == null || !cab.isActive()) {
                cab = cabHolder.openCab(menuRes, this);
            }

            final int size = checked.size();
            if (size <= 0) cab.finish();
            else if (size == 1) cab.setTitle(getName(checked.get(0)));
            else cab.setTitle(context.getString(R.string.x_selected, size));
        }
    }

    private void clearChecked() {
        checked.clear();
        notifyDataSetChanged();
    }

    protected boolean isChecked(I identifier) {
        return checked.contains(identifier);
    }

    protected boolean isInQuickSelectMode() {
        return cab != null && cab.isActive();
    }

    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.action_select_all) {
            checkAll();
        } else {
            onMultipleItemAction(menuItem, new ArrayList<>(checked));
            cab.finish();
            clearChecked();
        }

        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab materialCab) {
        clearChecked();
        return true;
    }

    protected String getName(I object) {
        return object.toString();
    }

    @Nullable
    protected abstract I getIdentifier(int position);

    protected abstract void onMultipleItemAction(MenuItem menuItem, List<I> selection);
}
