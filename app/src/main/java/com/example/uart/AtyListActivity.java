package com.example.uart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parse.Const;

import java.util.ArrayList;
import java.util.List;

public class AtyListActivity extends Activity{

    /**
     * 数据列表
     */
    private ArrayList<AtyData> dataList;

    private RecyclerView recyclerView;

    private AtyAdapter atyAdapter;


    private RadioButton rfmode2bytesidRadioButton;
    private RadioButton rfmode4bytesidRadioButton;

    private RadioButton hasRelayRadioButton;
    private RadioButton noRelayRadioButton;

    private EditText relayIdEditText;

    public interface MyItemClickListener {
        void onItemClick(View view, int position);
    }
    public interface MyItemLongClickListener{
        void onItemLongClick(View view, int position);
    }
//    public interface MyItemTouchLisenter{
//        void onItemTouch(View view,MotionEvent motionEvent,int position);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aty_list);

        initData();

        rfmode2bytesidRadioButton = (RadioButton)findViewById(R.id.rfmode2bytesidRadioButton);
        rfmode4bytesidRadioButton = (RadioButton)findViewById(R.id.rfmode4bytesidRadioButton);

        hasRelayRadioButton = (RadioButton)findViewById(R.id.hasRelayRadioButton);
        noRelayRadioButton = (RadioButton)findViewById(R.id.noRelayRadioButton);

        relayIdEditText = (EditText)findViewById(R.id.relayIdEditText);

        hasRelayRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(hasRelayRadioButton.isChecked()){
                    relayIdEditText.setVisibility(View.VISIBLE);
                }else {
                    relayIdEditText.setVisibility(View.GONE);
                }
            }
        });


        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        atyAdapter = new AtyAdapter(dataList, R.layout.item_aty);
        atyAdapter.setOnItemClickListener(new MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                AtyData atyData = dataList.get(position);
                if (atyData != null) {
//                    Toast.makeText(getBaseContext(), atyData.getAtyName(), Toast.LENGTH_SHORT).show();
                    try {
                        if(rfmode2bytesidRadioButton.isChecked()) {
                            Const.rf_type = Const.RF_TYPE.RF_TYPE_NODEID_2_BYTES;
                        }else{
                            Const.rf_type = Const.RF_TYPE.RF_TYPE_NODEID_4_BYTES;
                        }

                        if(noRelayRadioButton.isChecked()) {
                            Const.rf_transmisson_type = Const.RF_TRANSMISSON_TYPE.RF_TRANSMISSON_TYPE_NO_RELAY;
                        }else{
                            Const.rf_transmisson_type = Const.RF_TRANSMISSON_TYPE.RF_TRANSMISSON_TYPE_RELAY;
                            try{
                                Const.rf_relay_id = Long.parseLong(relayIdEditText.getText().toString(),16);
                            }catch (Exception ex){
                                Log.i("wdj",ex.getMessage());
                                Toast.makeText(getApplication(),"输入有误!",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        Intent intent = new Intent();
                        intent.setClassName(getApplicationContext(), atyData.getClassName());
                        startActivity(intent);
                    } catch (Exception ex) {
//                        Toast.makeText(getBaseContext(), atyData.getAtyName() + "---null", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        atyAdapter.setOnItemLongClickListener(new MyItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                AtyData atyData = dataList.get(position);
                if (atyData != null) {
                    Toast.makeText(getBaseContext(), atyData.getAtyName() + "longpress", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView.setAdapter(atyAdapter);
        recyclerView.addItemDecoration(new ItemDivider(getApplication(),R.drawable.separate_line_recyclerview));
    }


    /**
     * 初始化数据
     */
    private void initData(){
        dataList = new ArrayList<AtyData>();
        dataList.add(new AtyData("气表测试","com.example.activity.RLMeterTestActivity"));
        dataList.add(new AtyData("气表设置","com.example.activity.RLMeterSettingsActivity"));

        dataList.add(new AtyData("调试","com.example.uart.Main2Activity"));

//        dataList.add(new AtyData("滑动指示器","com.example.uart.ViewPagerIndicatorActivity"));
        dataList.add(new AtyData("设置掌机RF模块网络ID","com.example.activity.SetHandleRfNetIDActivity"));
        dataList.add(new AtyData("设置气表RF模块参数","com.example.activity.SetMeterRfParamActivity"));
//        dataList.add(new AtyData("更新固件","com.example.uart.UpdateActivity"));
//        dataList.add(new AtyData("问题测试","com.example.test.MeterTestActivity"));

        dataList.add(new AtyData("气表管理","com.example.activity.RLMeterManagerActivity"));

        dataList.add(new AtyData("利尔达","com.example.lierda.LierdaRfActivity"));

    }


    /**
     * 自定义adapter类 需要实现onCreateViewHolder  onBindViewHolder  getItemCount三个方法
     */
    class AtyAdapter extends RecyclerView.Adapter<AtyAdapter.MyViewHolder>{

        private List<AtyData> items;
        private int itemLayout;//可以以此判断对应视图 以加载不同视图数据

        private MyItemClickListener mItemClickListener;
        private MyItemLongClickListener mItemLongClickListener;

        /**
         * adapter构造函数
         * @param items items
         * @param itemLayout itemLayout
         */
        public AtyAdapter(List<AtyData> items,int itemLayout){
            this.items = items;
            this.itemLayout = itemLayout;
        }

        /**
         * 创建viewholder 即单元视图
         * @param viewGroup viewGroup
         * @param i 位置
         * @return return
         */
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(itemLayout, null);
            //不知道为什么在xml设置的“android:layout_width="match_parent"”无效了，需要在这里重新设置
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(lp);
            return new MyViewHolder(view, mItemClickListener,mItemLongClickListener);
        }

        /**
         * 绑定数据至视图
         * @param myViewHolder myViewHolder
         * @param i i
         */
        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
            myViewHolder.textView.setText(items.get(i).getAtyName());
            myViewHolder.textView.setTextColor(Color.parseColor("#FFFFFF"));
            if(i%2==0){
                Drawable drawable = getResources().getDrawable(R.drawable.normalselector);
                myViewHolder.itemView.setBackgroundDrawable(drawable);
            }else {
                Drawable drawable = getResources().getDrawable(R.drawable.normalselector);
                myViewHolder.itemView.setBackgroundDrawable(drawable);
            }
        }

        /**
         * 单元视图个数
         * @return return
         */
        @Override
        public int getItemCount() {
            return items.size();
        }

        public void setOnItemClickListener(MyItemClickListener listener){
            this.mItemClickListener = listener;
        }

        public void setOnItemLongClickListener(MyItemLongClickListener listener){
            this.mItemLongClickListener = listener;
        }

        /**
         * 自定义viewholder类  （对应一种单元视图的布局）
         */
        class MyViewHolder extends RecyclerView.ViewHolder{

            TextView textView;

            private MyItemClickListener mListener;
            private MyItemLongClickListener mLongClickListener;


            /**
             * ViewHolder 构造函数  指出视图内容（绑定控件）
             * @param itemView itemView
             * @param listener  listener
             * @param longClickListener longClickListener
             */
            public MyViewHolder(View itemView,MyItemClickListener listener,MyItemLongClickListener longClickListener) {
                super(itemView);
                //指出视图内容（绑定控件）
                textView = (TextView)itemView.findViewById(R.id.textView);

                this.mListener = listener;
                this.mLongClickListener = longClickListener;

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            //getPosition() is deprecated
                            mListener.onItemClick(view, getAdapterPosition());
                        }
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (mLongClickListener != null) {
                            //getPosition() is deprecated
                            mLongClickListener.onItemLongClick(view, getAdapterPosition());
                        }
                        return true;
                    }
                });

//                itemView.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View view, MotionEvent motionEvent) {
//                        if(mItemTouchLisenter != null){
//                            mItemTouchLisenter.onItemTouch(view,motionEvent,getAdapterPosition());
//                        }
//                        return true;
//                    }
//                });

            }
        }
    }


    /**
     * item数据类
     */
    class AtyData{

        private String atyName;
        private String className;



        public AtyData(String atyName,String className) {
            this.atyName = atyName;
            this.className = className;
        }


        public String getClassName() {
            return className;
        }
        public String getAtyName() {
            return atyName;
        }
    }


    /**
     * RecyclerView Item装饰 自定义类 用以绘制分割线
     */
    class ItemDivider extends RecyclerView.ItemDecoration{

        /**
         * 绘制item分割线的画笔，和设置其属性
         * 来绘制个性分割线
         */
        private Paint mPaint ;


        private int linewidth;

        private Drawable drawable;

        /**
         * 构造函数获取画分割线的额drawable
         * @param context
         * @param resId
         */
        public ItemDivider(Context context,int resId){
            this.drawable = context.getResources().getDrawable(resId);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG) ;
            mPaint.setColor(Color.BLUE);
         /*设置填充*/
            mPaint.setStyle(Paint.Style.FILL);

            linewidth = drawable.getIntrinsicHeight();
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {

                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();

                int childtop = child.getTop();
                int childbottom = child.getBottom();
                //以下计算主要用来确定绘制的位置
                final int top = child.getBottom() + params.bottomMargin;

                int height = drawable.getIntrinsicHeight();
                final int bottom = top + linewidth;
                //绘制drawable资源
                drawable.setBounds(left, top, right, bottom);
                drawable.draw(c);

//                //画笔绘制
//                c.drawRect(left, top, right, top+linewidth,mPaint);
            }
        }


        /**
         * 可以通过outRect.set()为每个Item设置一定的偏移量；
         * @param outRect outRect
         * @param view view
         * @param parent parent
         * @param state state
         */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            outRect.set(0, 0, 0, linewidth);
        }
    }
}
