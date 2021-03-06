package com.mingxiu.apptest.view.layout;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.apkfuns.logutils.LogUtils;
import com.mingxiu.apptest.Contact;
import com.mingxiu.apptest.R;
import com.mingxiu.apptest.base.BaseViewHolder;
import com.mingxiu.apptest.base.RxManager;
import com.mingxiu.apptest.data.Data;
import com.mingxiu.apptest.data.Repository;
import com.mingxiu.apptest.view.viewholder.CommFooterVH;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;

/**
 * @author Administrator
 */
public class TRecyclerView<T extends Repository> extends LinearLayout {
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerview;
    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwiperefresh;
    @BindView(R.id.ll_emptyview)
    LinearLayout mLlEmptyview;
    private T model;
    private LinearLayoutManager mLayoutManager;
    private Context context;
    public CoreAdapter mCommAdapter = new CoreAdapter();
    private int begin = 0;
    private boolean isRefreshable = true, isHasHeadView = false, isEmpty = false;

    public RxManager mRxManager = new RxManager();
    private Map<String, String> param = new HashMap<>();

    public TRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public TRecyclerView(Context context, AttributeSet att) {
        super(context, att);
        init(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRxManager.clear();
    }

    public void init(Context context) {
        this.context = context;
        View layout = LayoutInflater.from(context).inflate(
                R.layout.layout_list_recyclerview, null);
        layout.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        addView(layout);
        ButterKnife.bind(this, layout);
        initView(context);
    }

    private void initView(Context context) {
        mSwiperefresh.setColorSchemeResources(android.R.color.holo_blue_bright);
        mSwiperefresh.setEnabled(isRefreshable);
        mSwiperefresh.setOnRefreshListener(() -> reFetch());
        mRecyclerview.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerview.setLayoutManager(mLayoutManager);
        mRecyclerview.setItemAnimator(new DefaultItemAnimator());
        mRecyclerview.setAdapter(mCommAdapter);
        mRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            protected int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mRecyclerview.getAdapter() != null
                        && newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisibleItem + 1 == mRecyclerview.getAdapter()
                        .getItemCount() && mCommAdapter.isHasMore)
                    fetch();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int arg0, int arg1) {
                super.onScrolled(recyclerView, arg0, arg1);
                lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            }
        });
        mRxManager.on(Contact.EVENT_DEL_ITEM, (arg0) -> mCommAdapter.removeItem((Integer) arg0));
        mRxManager.on(Contact.EVENT_UPDATE_ITEM, (arg0) -> {
                    if (model.getClass().getSimpleName().equals(((UpDateData) arg0).oj.getClass().getSimpleName())) {
                        mCommAdapter.upDateItem(((UpDateData) arg0).i, ((UpDateData) arg0).oj);
                    }
                }
        );
        mLlEmptyview.setOnClickListener((view -> {
            isEmpty = false;
            mLlEmptyview.setVisibility(View.GONE);
            mSwiperefresh.setVisibility(View.VISIBLE);
            reFetch();
        }));
    }

    public CoreAdapter getAdapter() {
        return mCommAdapter;
    }

    public void setRefreshing(boolean i) {
        mSwiperefresh.setRefreshing(i);
    }

    public TRecyclerView setIsRefreshable(boolean i) {
        isRefreshable = i;
        mSwiperefresh.setEnabled(i);
        return this;
    }

    public TRecyclerView setHeadView(Class<? extends BaseViewHolder> cla) {
        if (cla == null) {
            isHasHeadView = false;
            this.mCommAdapter.setHeadViewType(0, cla, null);
        } else
            try {
                Object obj = ((Activity) context).getIntent().getSerializableExtra(Contact.HEAD_DATA);
                int mHeadViewType = ((BaseViewHolder) (cla.getConstructor(View.class)
                        .newInstance(new LinearLayout(context)))).getType();
                this.mCommAdapter.setHeadViewType(mHeadViewType, cla, obj);
                isHasHeadView = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        return this;
    }

    public TRecyclerView setFooterView(Class<? extends BaseViewHolder> cla) {
        this.begin = 0;
        try {
            int mFooterViewType = ((BaseViewHolder) (cla.getConstructor(View.class)
                    .newInstance(new LinearLayout(context)))).getType();
            this.mCommAdapter.setFooterViewType(mFooterViewType, cla);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public void setEmpty() {
        if (!isHasHeadView && !isEmpty) {
            isEmpty = true;
            mLlEmptyview.setVisibility(View.VISIBLE);
            mSwiperefresh.setVisibility(View.GONE);
        }
    }

    public TRecyclerView setView(Class<? extends BaseViewHolder<T>> cla) {
        try {
            BaseViewHolder mIVH = ((BaseViewHolder) (cla.getConstructor(View.class)
                    .newInstance(new LinearLayout(context))));
            int mType = mIVH.getType();
            this.model = ((Class<T>) ((ParameterizedType) (cla
                    .getGenericSuperclass())).getActualTypeArguments()[0])
                    .newInstance();// 根据类的泛型类型获得model的实例
            this.mCommAdapter.setViewType(mType, cla);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public TRecyclerView setParam(String key, String value) {
        this.param.put(key, value);
        return this;
    }

    public TRecyclerView setData(List<T> datas) {
        if (isEmpty) {
            mLlEmptyview.setVisibility(View.GONE);
            mSwiperefresh.setVisibility(View.VISIBLE);
        }
        mCommAdapter.setBeans(datas, 1);
        return this;
    }

    public void reFetch() {
        this.begin = 0;
        mSwiperefresh.setRefreshing(true);
        fetch();
    }

    public void fetch() {
        begin++;
        if (isEmpty) {
            mLlEmptyview.setVisibility(View.GONE);
            mSwiperefresh.setVisibility(View.VISIBLE);
        }
        if (model == null) {
            Log.e("model", "null");
            return;
        }
        model.param = param;
        mRxManager.add(model.getPageAt(begin)
                .subscribe(
                        new Action1<Data>() {
                            @Override
                            public void call(Data subjects) {
                                mSwiperefresh.setRefreshing(false);
                                List<T> mList = new ArrayList<T>();
                                for (Object o : subjects.results) {
                                    T d = (T) model.clone();
                                    d.data = o;
                                    mList.add(d);
                                }
                                mCommAdapter.setBeans(mList, begin);
                                if (begin == 1 && (subjects.results == null || subjects.results.size() == 0))
                                    setEmpty();
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable e) {
                                e.printStackTrace();
                                setEmpty();
                            }
                        }
                ));
    }


    public class UpDateData {
        public int i;
        public T oj;

        public UpDateData(int i, T oj) {
            this.i = i;
            this.oj = oj;
        }
    }

    public class CoreAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        protected List<T> mItemList = new ArrayList<>();
        public boolean isHasMore = true;
        public int viewtype, isHasFooter = 1, isHasHader = 0, mHeadViewType;
        public Object mHeadData;
        public Class<? extends BaseViewHolder> mItemViewClass, mHeadViewClass, mFooterViewClass = CommFooterVH.class;
        public int mFooterViewType = CommFooterVH.LAYOUT_TYPE;

        public void setViewType(int i, Class<? extends BaseViewHolder> cla) {
            this.isHasMore = true;
            this.viewtype = i;
            this.mItemList = new ArrayList<>();
            this.mItemViewClass = cla;
            notifyDataSetChanged();
        }

        public void setHeadViewType(int i, Class<? extends BaseViewHolder> cla, Object data) {
            if (cla == null) {
                this.isHasHader = 0;
            } else {
                this.isHasHader = 1;
                this.mHeadViewType = i;
                this.mHeadViewClass = cla;
                this.mHeadData = data;
            }
        }

        public void setHeadViewData(Object data) {
            this.mHeadData = data;
        }

        public void setFooterViewType(int i, Class<? extends BaseViewHolder> cla) {
            this.mFooterViewType = i;
            this.mFooterViewClass = cla;
            this.mItemList = new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return isHasHader == 1 ? (position == 0 ? mHeadViewType
                    : (position + 1 == getItemCount() ? mFooterViewType : viewtype))
                    : (position + 1 == getItemCount() ? mFooterViewType : viewtype);
        }

        @Override
        public int getItemCount() {
            return mItemList.size() + isHasFooter + isHasHader;
        }

        public void setBeans(List<T> datas, int begin) {
            if (datas == null) datas = new ArrayList<>();
            this.isHasMore = datas.size() >= Contact.PAGE_COUNT;
            if (begin > 1) {
                this.mItemList.addAll(datas);
            } else {
                this.mItemList = datas;
            }
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            try {
                boolean isFoot = viewType == mFooterViewType;
                return (RecyclerView.ViewHolder) (viewType == mHeadViewType ? mHeadViewClass
                        .getConstructor(View.class).newInstance(
                                LayoutInflater.from(parent.getContext()).inflate(
                                        mHeadViewType, parent, false))
                        : (RecyclerView.ViewHolder) (isFoot ? mFooterViewClass : mItemViewClass)
                        .getConstructor(View.class).newInstance(
                                LayoutInflater.from(parent.getContext())
                                        .inflate(
                                                isFoot ? mFooterViewType
                                                        : viewtype, parent,
                                                false)));
            } catch (Exception e) {
                LogUtils.d("ViewHolderException", "onCreateViewHolder十有八九是xml写错了,哈哈");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((BaseViewHolder) holder).onBindViewHolder(holder.itemView,
                    position + 1 == getItemCount() ? (isHasMore ? new Object()
                            : null) : isHasHader == 1 && position == 0 ? mHeadData
                            : mItemList.get(position - isHasHader));
        }

        public void removeItem(int position) {
            mItemList.remove(position);
            notifyItemRemoved(position);
            if (mItemList.size() == 0) reFetch();
        }

        public void upDateItem(int position, T item) {
            mItemList.remove(position);
            mItemList.add(position, item);
            notifyItemChanged(position);
        }
    }

}