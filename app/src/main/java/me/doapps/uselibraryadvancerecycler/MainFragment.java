package me.doapps.uselibraryadvancerecycler;

import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;

/**
 * Created by William_ST on 13/09/15.
 */
public class MainFragment extends Fragment {

    private final String TAG = MainFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private ExampleDataProvider exampleDataProvider;
    public MainFragment(){
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        exampleDataProvider = new ExampleDataProvider();

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        final MySwipeableItemAdapter myItemAdapter = new MySwipeableItemAdapter(exampleDataProvider);
        myItemAdapter.setEventListener(new MySwipeableItemAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                Toast.makeText(getActivity(), "Open edit Income", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemPinned(final int position) {
                Toast.makeText(getActivity(), "¿Desea eliminar?", Toast.LENGTH_SHORT).show();
                Dialog.Builder builder = null;
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight) {
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        try {
                            exampleDataProvider.removeItem(position);
                            myItemAdapter.notifyItemRemoved(position);
                            super.onPositiveActionClicked(fragment);
                        } catch (Exception e) {
                            Log.e(TAG, "dialog " + e.toString());
                        }
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        super.onNegativeActionClicked(fragment);
                        Toast.makeText(getActivity(), "Dismiss()", Toast.LENGTH_SHORT).show();
                    }
                };

                builder.title("¿Desea eliminar el ingreso ... ?")
                        .positiveAction("ELIMINAR")
                        .negativeAction("CANCELAR");
                DialogFragment fragment = DialogFragment.newInstance(builder);
                fragment.show(((MainActivity) getActivity()).getSupportFragmentManager(), null);
            }

            @Override
            public void onItemViewClicked(View v, boolean pinned) {
                onItemViewClick(v, pinned);
            }
        });

        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(myItemAdapter);      // wrap for swiping

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            //mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z1)));
        }
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider), true));

        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        super.onDestroyView();
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    private void onItemViewClick(View v, boolean pinned) {
        int position = mRecyclerView.getChildPosition(v);
        if (position != RecyclerView.NO_POSITION) {
            AbstractDataProvider.Data data = exampleDataProvider.getItem(position);

            if (data.isPinnedToSwipeLeft()) {
                // unpin if tapped the pinned item
                data.setPinnedToSwipeLeft(false);
                notifyItemChanged(position);
            }
        }
    }

    public void notifyItemChanged(int position) {
        mAdapter.notifyItemChanged(position);
    }
}
