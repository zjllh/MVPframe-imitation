package me.jessyan.mvparms.demo.mvp.ui.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import me.jessyan.mvparms.demo.R;
import me.jessyan.mvparms.demo.app.entity.news.NewsBean;
import me.jessyan.mvparms.demo.app.entity.zhihu.ZhihuDailyItem;
import me.jessyan.mvparms.demo.dao.TopNewsDao;
import me.jessyan.mvparms.demo.dao.ZhihuDao;
import me.jessyan.mvparms.demo.mvp.ui.activity.MainActivity;
import me.jessyan.mvparms.demo.mvp.ui.activity.TopNewsDescribeActivity;
import me.jessyan.mvparms.demo.mvp.ui.activity.ZhihuDescribeActivity;
import me.jessyan.mvparms.demo.utils.DensityUtil;
import me.jessyan.mvparms.demo.utils.DribbbleTarget;
import me.jessyan.mvparms.demo.utils.Help;
import me.jessyan.mvparms.demo.utils.ObservableColorMatrix;
import me.jessyan.mvparms.demo.widget.BadgedFourThreeImageView;


/**
 * Created by zjl on 2016/12/22.
 */

public class TopNewsAdapter extends BaseRecyclerViewAdapter implements MainActivity.LoadingMore{

    private static final int TYPE_LOADING_MORE = -1;
    private static final int NORMAL_ITEM = 1;
    boolean showLoadingMore;
    float width;
    int widthPx;
    int heighPx;
    private ArrayList<NewsBean> topNewitems = new ArrayList<>();
    private Context mContext;


    public TopNewsAdapter(Context context) {

        this.mContext = context;
        width = mContext.getResources().getDimension(R.dimen.image_width);
        widthPx = DensityUtil.dip2px(mContext, width);
        heighPx = widthPx * 3 / 4;
    }




    @Override
    public BaseRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case NORMAL_ITEM:
                return new TopNewsViewHolder(LayoutInflater.from(mContext).inflate(R.layout.topnews_layout_item, parent, false));

            case TYPE_LOADING_MORE:
                return new LoadingMoreHolder(LayoutInflater.from(mContext).inflate(R.layout.infinite_loading, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case NORMAL_ITEM:
                bindViewHolderNormal((TopNewsViewHolder) holder, position);
                break;
            case TYPE_LOADING_MORE:
                bindLoadingViewHold((LoadingMoreHolder) holder, position);
                break;
        }
    }

    private void bindLoadingViewHold(LoadingMoreHolder holder, int position) {
        holder.progressBar.setVisibility(showLoadingMore == true ? View.VISIBLE : View.INVISIBLE);
    }

    private void bindViewHolderNormal(final TopNewsViewHolder holder, final int position) {
        final NewsBean newsBeanItem = topNewitems.get(holder.getAdapterPosition());

        //   if (DBUtils.getDB(mContext).isRead(Config.TOPNEWS, newsBeanItem.getTitle(), 1)){
        if (TopNewsDao.getDB(mContext).isRead(newsBeanItem.getTitle(), 1)){
            holder.textView.setTextColor(Color.GRAY);
            holder.sourceTextview.setTextColor(Color.GRAY);
        }

        else {
            holder.textView.setTextColor(Color.BLACK);
            holder.sourceTextview.setTextColor(Color.BLACK);
        }


        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TopNewsDao.getDB(mContext).insertHasRead(newsBeanItem.getTitle(), 1);
                holder.textView.setTextColor(Color.GRAY);
                holder.sourceTextview.setTextColor(Color.GRAY);
                startTopnewsActivity( newsBeanItem, holder );

            }
        });
        holder.textView.setText(newsBeanItem.getTitle());
        holder.sourceTextview.setText(newsBeanItem.getSource());
        holder.linearLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startTopnewsActivity( newsBeanItem, holder );
                    }
                });


        Glide.with(mContext)
                .load(newsBeanItem.getImgsrc())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @SuppressLint("NewApi")
                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (!newsBeanItem.hasFadedIn) {
                            holder.imageView.setHasTransientState(true);
                            final ObservableColorMatrix cm = new ObservableColorMatrix();
                            final ObjectAnimator animator = ObjectAnimator.ofFloat(cm, ObservableColorMatrix.SATURATION, 0f, 1f);
                            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    holder.imageView.setColorFilter(new ColorMatrixColorFilter(cm));
                                }
                            });
                            animator.setDuration(2000L);
                            animator.setInterpolator(new AccelerateInterpolator());
                            animator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    holder.imageView.clearColorFilter();
                                    holder.imageView.setHasTransientState(false);
                                    animator.start();
                                    newsBeanItem.hasFadedIn = true;

                                }
                            });
                        }

                        return false;
                    }
                }).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop().override(widthPx, heighPx)
                .into(new DribbbleTarget(holder.imageView, false));


    }


    private void startTopnewsActivity(NewsBean newsBeanItem,RecyclerView.ViewHolder holder){

        Intent intent = new Intent(mContext, TopNewsDescribeActivity.class);
        intent.putExtra("docid", newsBeanItem.getDocid());
        intent.putExtra("title", newsBeanItem.getTitle());
        intent.putExtra("image", newsBeanItem.getImgsrc());
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            final android.support.v4.util.Pair<View, String>[] pairs = Help.createSafeTransitionParticipants
                    ((Activity) mContext, false,new android.support.v4.util.Pair<>(((TopNewsViewHolder)holder).imageView, mContext.getString(R.string.transition_topnew)),
                            new android.support.v4.util.Pair<>(((TopNewsViewHolder)holder).linearLayout, mContext.getString(R.string.transition_topnew_linear)));
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, pairs);
            mContext.startActivity(intent, options.toBundle());
        }else {

            mContext.startActivity(intent);

        }

    }

    @Override
    public int getItemCount() {
        return null == topNewitems ? 0 : topNewitems.size();
    }

    public void addItems(ArrayList<NewsBean> list) {
        ////        remove first unuse data
        list.remove(0);
        int n = list.size();
        topNewitems.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        if (position < getDataItemCount() && getDataItemCount() > 0) {
            return NORMAL_ITEM;
        }
        return TYPE_LOADING_MORE;
    }

    private int getDataItemCount() {
        return topNewitems.size();
    }

    @Override
    public void loadingStart() {
        if (showLoadingMore) return;
        showLoadingMore = true;
        notifyItemInserted(getLoadingMoreItemPosition());
    }
    @Override
    public void loadingfinish() {
        if (!showLoadingMore) return;
        final int loadingPos = getLoadingMoreItemPosition();
        showLoadingMore = false;
        notifyItemRemoved(loadingPos);
    }

    public void clearData() {
        topNewitems.clear();
        notifyDataSetChanged();
    }

    private int getLoadingMoreItemPosition() {
        return showLoadingMore ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }




    class LoadingMoreHolder extends BaseRecyclerViewHolder {
        ProgressBar progressBar;

        public LoadingMoreHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView;
        }
    }

    class TopNewsViewHolder extends BaseRecyclerViewHolder {
        final TextView textView;
        final LinearLayout linearLayout;
        final TextView sourceTextview;
        BadgedFourThreeImageView imageView;

        TopNewsViewHolder(View itemView) {
            super(itemView);
            imageView = (BadgedFourThreeImageView) itemView.findViewById(R.id.item_image_id);
            textView = (TextView) itemView.findViewById(R.id.item_text_id);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.zhihu_item_layout);
            sourceTextview= (TextView) itemView.findViewById(R.id.item_text_source_id);
        }
    }
}
