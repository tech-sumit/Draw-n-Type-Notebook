package tech.profusion.project.drawntypenotebook.dialogs;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import tech.profusion.project.drawntypenotebook.R;
import tech.profusion.project.drawntypenotebook.adapters.PhotoAdapter;
import tech.profusion.project.drawntypenotebook.behaviors.CustomBottomSheetBehavior;
import tech.profusion.project.drawntypenotebook.listeners.OnClickListener;
import tech.profusion.project.drawntypenotebook.utils.FileUtils;
import tech.profusion.project.drawntypenotebook.utils.LayoutUtils;
import tech.profusion.project.drawntypenotebook.utils.deprecated.AsyncTaskCompat;

public class SelectImageDialog extends BottomSheetDialogFragment {

    public static final String SELEC_IMAGE_DIALOG = "SELECT_IMAGE_DIALOG";

    // LISTENER
    private OnImageSelectListener onImageSelectListener;

    // VIEWS
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private LinearLayout mNoImages;

    // VARS
    private CustomBottomSheetBehavior mCustomBottomSheetBehavior;
    private int mRecyclerViewScrollAmount = 0;

    public SelectImageDialog() {
    }

    public static SelectImageDialog newInstance() {
        return new SelectImageDialog();
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_select_image, null);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_select_image);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_select_image);
        mNoImages = (LinearLayout) view.findViewById(R.id.ll_no_images);

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(LayoutUtils.GetFlowLayoutManager(getContext()));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AsyncTaskCompat.executeParallel(new LoadImagesFromStorage());
            }
        });

        AsyncTaskCompat.executeParallel(new LoadImagesFromStorage());

        setListeners();

        dialog.setContentView(view);

        mCustomBottomSheetBehavior = new CustomBottomSheetBehavior();
        mCustomBottomSheetBehavior.setLocked(false);
        mCustomBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        mCustomBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED)
                    dismiss();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        CoordinatorLayout.LayoutParams layoutParams =
                (CoordinatorLayout.LayoutParams) ((View) view.getParent()).getLayoutParams();
        layoutParams.setBehavior(mCustomBottomSheetBehavior);
    }

    private void setListeners(){
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mRecyclerViewScrollAmount += dy;

                if (mRecyclerViewScrollAmount > 0)
                    mCustomBottomSheetBehavior.setLocked(true);
                else {
                    mCustomBottomSheetBehavior.setLocked(false);
                    mRecyclerViewScrollAmount = 0;
                }
            }
        });
    }

    private class LoadImagesFromStorage extends AsyncTask<Void, Void, Void>{
        private List<File> imageList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mNoImages.setVisibility(View.INVISIBLE);

            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        @Override
        protected Void doInBackground(Void... voids) {
            imageList = FileUtils.GetSortedFilesByDate(
                    FileUtils.GetImageList(Environment.getExternalStorageDirectory()));
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            if (!isCancelled()){
                PhotoAdapter photoAdapter =
                        new PhotoAdapter(imageList,
                                new OnClickListener() {
                                    @Override
                                    public void onItemClickListener(View view, Object contentObject, int position) {
                                        ByteArrayOutputStream byteArrayOutputStream =
                                                new ByteArrayOutputStream();
                                        BitmapFactory.decodeFile(((File) contentObject).getPath())
                                                .compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
                                        if (onImageSelectListener != null) {
                                            onImageSelectListener.onSelectImage((File) contentObject);
                                            onImageSelectListener.onSelectImage(byteArrayOutputStream.toByteArray());
                                        }
                                        dismiss();
                                    }
                                });
                mRecyclerView.setAdapter(photoAdapter);

                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mSwipeRefreshLayout.setEnabled(false);
                    }
                });

                if (imageList.size() == 0)
                    mNoImages.setVisibility(View.VISIBLE);
            }
        }
    }

    // INTERFACES
    public void setOnImageSelectListener(OnImageSelectListener onImageSelectListener){
        this.onImageSelectListener = onImageSelectListener;
    }

    public interface OnImageSelectListener{
        void onSelectImage(File imageFile);
        void onSelectImage(byte[] imageBytes);
    }
}
