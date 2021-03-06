package me.jessyan.mvparms.demo.mvp.ui.activity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.jess.arms.utils.UiUtils;

import butterknife.BindView;
import me.jessyan.mvparms.demo.R;
import me.jessyan.mvparms.demo.di.component.AppComponent;
import me.jessyan.mvparms.demo.di.component.DaggerMeiziPhotoDescribeComponent;
import me.jessyan.mvparms.demo.di.module.MeiziPhotoDescribeModule;
import me.jessyan.mvparms.demo.mvp.contract.MeiziPhotoDescribeContract;
import me.jessyan.mvparms.demo.mvp.presenter.MeiziPhotoDescribePresenter;
import me.jessyan.mvparms.demo.mvp.ui.common.WEActivity;
import me.jessyan.mvparms.demo.utils.ColorUtils;
import me.jessyan.mvparms.demo.utils.GlideUtils;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by zjl on 2016/12/27.
 */

public class MeiziPhotoDescribeActivity extends WEActivity<MeiziPhotoDescribePresenter> implements MeiziPhotoDescribeContract.View{

    public static final String EXTRA_IMAGE_URL = "image";
    private static final float SCRIM_ADJUSTMENT = 0.075f;
    String mImageUrl;
    PhotoViewAttacher mPhotoViewAttacher;
    private boolean mIsHidden = false;

    @BindView(R.id.shot)
    ImageView mShot;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.background)
    RelativeLayout mRelativeLayout;

    @Override
    protected View initView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_picture, null, false);
    }

    @Override
    protected void initData() {
        parseIntent();
        getData();
        mPresenter.setupPhotoAttacher();
        mToolbar.setAlpha(0.7f);
        mRelativeLayout.setAlpha(0.3f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementEnterTransition().addListener(mListener);
            getWindow().setSharedElementEnterTransition(new ChangeBounds());
//            setStatusColor();
        }
    }

    private void getData() {
        Glide.with(this)
                .load(mImageUrl)
                .centerCrop()
                .listener(loadListener)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mShot);

        mPhotoViewAttacher = new PhotoViewAttacher(mShot);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandImageAndFinish();
            }
        });
    }

    private void expandImageAndFinish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        } else {
            finish();
        }
    }


    private void parseIntent() {
        mImageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
    }

    @Override
    public void setupPhotoAttacher() {
        mPhotoViewAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                hideOrShowToolbar();
            }
        });

        mPhotoViewAttacher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(MeiziPhotoDescribeActivity.this)
                        .setMessage(getString(R.string.save_meizi))
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Bitmap drawingCache = mPhotoViewAttacher.getImageView().getDrawingCache();
                                mPresenter.saveImage(drawingCache,getApplicationContext(),getCurrentFocus());
                            }
                        }).show();
                return true;
            }
        });
    }
    private void hideOrShowToolbar() {
        mToolbar.animate().translationY(mIsHidden ? 0 : -mToolbar.getHeight())
                .setInterpolator(new DecelerateInterpolator(2))
                .start();
        mIsHidden = !mIsHidden;
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showMessage(String message) {
        UiUtils.SnackbarText(message);
    }

    @Override
    public void launchActivity(Intent intent) {
        UiUtils.startActivity(intent);
    }

    @Override
    public void killMyself() {
        finish();
    }

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerMeiziPhotoDescribeComponent.builder().appComponent(appComponent).meiziPhotoDescribeModule(new MeiziPhotoDescribeModule(this)).build().inject(this);
    }


    private Transition.TransitionListener mListener = new Transition.TransitionListener() {
        @Override
        public void onTransitionStart(Transition transition) {
            Log.d("maat", "xingfeifei");
            mRelativeLayout.animate()
                    .alpha(1f)
                    .setDuration(1000L)
                    .setInterpolator(new AccelerateInterpolator())
                    .start();


        }

        @Override
        public void onTransitionEnd(Transition transition) {

        }

        @Override
        public void onTransitionCancel(Transition transition) {

        }

        @Override
        public void onTransitionPause(Transition transition) {

        }

        @Override
        public void onTransitionResume(Transition transition) {

        }
    };

    private RequestListener loadListener = new RequestListener<String, GlideDrawable>() {

        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            final Bitmap bitmap = GlideUtils.getBitmap(resource);
            final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    24, MeiziPhotoDescribeActivity.this.getResources().getDisplayMetrics());
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters()
                    .setRegion(0, 0, bitmap.getWidth() - 1, twentyFourDip)
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            boolean isDark;
                            @ColorUtils.Lightness int lightness = ColorUtils.isDark(palette);
                            if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
                                isDark = ColorUtils.isDark(bitmap, bitmap.getWidth() / 2, 0);
                            } else {
                                isDark = lightness == ColorUtils.IS_DARK;
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                int statusBarColor = getWindow().getStatusBarColor();
                                final Palette.Swatch topColor =
                                        ColorUtils.getMostPopulousSwatch(palette);
                                if (topColor != null &&
                                        (isDark || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                                    statusBarColor = ColorUtils.scrimify(topColor.getRgb(),
                                            isDark, SCRIM_ADJUSTMENT);
                                }

                                if (statusBarColor != getWindow().getStatusBarColor()) {
                                    ValueAnimator statusBarColorAnim = ValueAnimator.ofArgb(
                                            getWindow().getStatusBarColor(), statusBarColor);
                                    statusBarColorAnim.addUpdateListener(new ValueAnimator
                                            .AnimatorUpdateListener() {
                                        @SuppressLint("NewApi")
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator animation) {
                                            getWindow().setStatusBarColor(
                                                    (int) animation.getAnimatedValue());
                                        }
                                    });
                                    statusBarColorAnim.setDuration(1000L);
                                    statusBarColorAnim.setInterpolator(
                                            new AccelerateInterpolator());
                                    statusBarColorAnim.start();
                                }
                            }
                        }
                    });
            return false;
        }
    };

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            getWindow().getSharedElementEnterTransition().removeListener(mListener);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            finishAfterTransition();
        }else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRelativeLayout.animate()
                .alpha(1f)
                .setDuration(1000L)
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }
}
