package com.kylin.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import com.google.gson.Gson;
import com.kylin.R;
import com.kylin.bean.BaseEntity;
import com.kylin.bean.ItemEntity;
import com.kylin.bean.LogoEntity;
import com.kylin.bean.MainEntity;
import com.kylin.bean.TimeEntity;
import com.kylin.bean.TitleEntity;
import com.kylin.utils.BitmapHelp;
import com.kylin.utils.BitmapViewUtils;
import com.kylin.utils.EntityUtils;
import com.kylin.view.ITabPage;
import com.kylin.view.Tab;
import com.kylin.view.TimeView;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.utils.LoggerUtils;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends Activity {

	public static final String TAG = "MainActivity";
	private Context mContext;
	private ImageView ivLogo;
	private LayoutInflater mLayoutInflater;
	private View view;
	private RelativeLayout rl_content;
	private LogoEntity logoEntity;
	private RelativeLayout rl;
	private MainEntity mainEntity;
	private BaseEntity networkEntity;
	private TimeEntity timeEntity;
	private ArrayList<TitleEntity> arrayTitle;
	ArrayList<TextView> arrTitle = new  ArrayList<TextView>();
	private ViewPager mViewPager;
	
//	private HashMap<Integer, ArrayList<ItemEntity>>  tabMap;
	private ArrayList<ArrayList<ItemEntity>> arrTab;
	
	/**  为VIewPager保存VIew界面的集合*/
	private List<View> mViewCache = new ArrayList<View>();
	private ViewPageAdapter mViewPageAdapter;
	private Tab tabTV;
	
	/** 最后 按键值  */  
	private int mLastKeyCode;
	public int mSelectedPageIndex;
	/**  焦点是否保持在菜单下   */ 
	private boolean mKeepMenuState ;
	private ImageView networkView;
	private TimeView timeView; 

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		
		init();
		initEntity();
		initView();
	}

	private void init() {
		BitmapUtils bitmapUtils = BitmapHelp.getBitmapUtils(mContext);
//        bitmapUtils.configDefaultLoadingImage(R.drawable.ic_launcher);
//        bitmapUtils.configDefaultLoadFailedImage(R.drawable.bitmap);
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
	}

	private void initEntity() {
		mainEntity 		= EntityUtils.getMainEntity();
		Gson gson = new Gson();
	    String jsonStudents = gson.toJson(mainEntity);
	    LoggerUtils.isDebug = true;
		LoggerUtils.e(TAG, jsonStudents);
		
		logoEntity 		= mainEntity.getLogoEntity();
		networkEntity 	= mainEntity.getNetworkEntity();
		timeEntity 		= mainEntity.getTimeEntity();
		arrayTitle 		= mainEntity.getArrayTitle();
		arrTab 			= mainEntity.getArrTab();
	}

	private void initView() {
		 rl = new RelativeLayout(this); 
		 RelativeLayout.LayoutParams mainParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT); 
		 setContentView(rl, mainParams);
		 initViewMainBackground();
		 initView_logo(logoEntity, R.drawable.icon);
		 initViewNetwork(networkEntity);
		 initViewTime(timeEntity);
		 for (TitleEntity titleEntity : arrayTitle) {
			 initViewTitle(titleEntity);
		}
		initViewPager();
		changeViewPageScroller();
		
	}




	private void initViewTime(TimeEntity timeEntity ) {
		timeView = new TimeView(mContext);
		timeView.setTextSize(timeEntity.textSize);
		initViewParameter(timeEntity, rl, timeView);
	}

	private void initViewPager() {
		Tab tab ;
		for (int i = 0; i < arrTab.size(); i++) {
			tab  =  new Tab(mContext);
			tab.setData(arrTab.get(i));
			mViewCache.add(tab);
		}
		
		RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT); 
		relLayoutParams.setMargins(0, 80, 0, 0);
		 
//		 if (entity.width != 0)  relLayoutParams.width  = entity.width;
//		 if (entity.height != 0) relLayoutParams.height = entity.height;
	 
		mViewPager = new ViewPager(mContext);
//		mViewPager.setFocusable(true);
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mViewPageAdapter = new ViewPageAdapter(mViewCache);
		mViewPager.setOffscreenPageLimit(5);
		mViewPager.setAdapter(mViewPageAdapter);
		mViewPager.setCurrentItem(0);
		setCurpager(0);
		rl.addView(mViewPager, relLayoutParams);
	}


	private void initViewTitle(TitleEntity titleEntity) {
		
		TextView textView = new TextView(mContext);
		textView.setText(titleEntity.text);
		textView.setFocusable(true);
		textView.setOnFocusChangeListener(new OnFocusChangeListener(titleEntity));
		textView.setTextSize(titleEntity.textSize);
		
		textView.setTag(titleEntity.position);
		
		textView.setGravity(Gravity.CENTER);
		// TODO Auto-generated method stub
		
		 RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT); 
		 relLayoutParams.setMargins(titleEntity.x, titleEntity.y, 0, 0);
		 
		 
		 if (titleEntity.width != 0)  relLayoutParams.width  = titleEntity.width;
		 if (titleEntity.height != 0) relLayoutParams.height = titleEntity.height;
		 rl.addView(textView, relLayoutParams);
		 
		 arrTitle.add(textView);
	}

	private void initViewMainBackground() {
		
//		BitmapUtils.
		BitmapUtils bitmapUtils = BitmapHelp.getBitmapUtils(mContext);
		bitmapUtils.display(rl, mainEntity.getBackground());
//		rl.setBackgroundResource(mainEntity.getBackground());
	}

	
	private void initViewNetwork(BaseEntity networkEntity ) {
		networkView = new ImageView(mContext);
		networkView.setBackgroundResource(R.drawable.main_page_image_wired_disconnect);
		initViewParameter(networkEntity, rl, networkView);
	}

	private void initView_logo(LogoEntity logoEntity,final int resid) {
		final ImageView view = new ImageView(mContext);
		if(logoEntity.background_url == null || "".equals(logoEntity.background_url)){
			view.setBackgroundResource(resid);
		}else {
			BitmapUtils bitmapUtils = BitmapHelp.getBitmapUtils(mContext);
			bitmapUtils.display(view, logoEntity.background_url,new BitmapLoadCallBack<View>() {
				@Override
				public void onLoadCompleted(View container, String uri,
						Bitmap bitmap, BitmapDisplayConfig config,
						BitmapLoadFrom from) {
					 BitmapViewUtils.fadeInDisplay(view, bitmap);
				}

				@Override
				public void onLoadFailed(View container, String uri,
						Drawable drawable) {
					view.setBackgroundResource(resid);
				}
			}  );
		} 
		 
		initViewParameter(logoEntity, rl, view);
		
	}
 
	private void initViewParameter(BaseEntity entity, RelativeLayout  parentView,View view) {
		view.setFocusable(entity.hasFocus);
		RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT); 
		relLayoutParams.setMargins(entity.x, entity.y, 0, 0);
		 
		if (entity.width != 0)  relLayoutParams.width  = entity.width;
		if (entity.height != 0) relLayoutParams.height = entity.height;
		parentView.addView(view, relLayoutParams);
	}

//	private void initView(BaseEntity entity) {
//		View view = null;
//		switch (entity.type) {
//		case 1:
//			view = new ImageView(mContext);
//			
//			if(entity.background_type == 0 ){
//				view.setBackgroundResource(entity.background_id);
//			}else {
//				BitmapUtils bitmapUtils = BitmapHelp.getBitmapUtils(mContext);
//				bitmapUtils.display(view, entity.background_url);
//			} 
//			break;
//		case 2:
//			view = new TimeView(mContext);
//			break;
//		case 4:
//			break;
//		case 5:
//			break;
//		default:
//			break;
//		}
//		view.setFocusable(entity.hasFocus);
//		RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT); 
//		relLayoutParams.setMargins(entity.x, entity.y, 0, 0);
//		 
//		if (entity.width != 0)  relLayoutParams.width  = entity.width;
//		if (entity.height != 0) relLayoutParams.height = entity.height;
//		rl.addView(view, relLayoutParams);
//	}
	
	
	class OnFocusChangeListener implements View.OnFocusChangeListener{

		private TitleEntity titleEntity;

		public OnFocusChangeListener(TitleEntity titleEntity) {
			this.titleEntity = titleEntity;
		}

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			mKeepMenuState = hasFocus;
			if (hasFocus) {
				((TextView)v).setTextSize(35);
				mViewPager.setCurrentItem(titleEntity.position-1);
				Log.e(TAG, ((TextView)v).getText().toString() + titleEntity.position +  "");
				v.setBackgroundResource(R.drawable.text_view_bg);
				v.bringToFront();
//				v.startAnimation(AnimationUtil.getInstance(mContext).mBigAnimation);
			}else {
				v.setBackgroundResource(android.R.color.transparent);
				((TextView)v).setTextSize(25);
				
				setCurpager(mSelectedPageIndex);
//				v.startAnimation(AnimationUtil.getInstance(mContext).mLittleAnimation);
			}
//			mContainer.invalidate();
		}
		
	}
	
	//反射机制   控制 viewpager滑动时间  为800
	 private void changeViewPageScroller() {
		try {
			Field mField = ViewPager.class.getDeclaredField("mScroller");
			mField.setAccessible(true);
			FixedSpeedScroller scroller;
			scroller = new FixedSpeedScroller(MainActivity.this,new AccelerateDecelerateInterpolator());
			mField.set(mViewPager, scroller);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	 
	 class FixedSpeedScroller extends Scroller {
			private int mDuration = 800;

			public FixedSpeedScroller(Context context) {
				super(context);
			}

			public FixedSpeedScroller(Context context, Interpolator interpolator) {
				super(context, interpolator);
			}

			@Override
			public void startScroll(int startX, int startY, int dx, int dy,
					int duration) {
				// Ignore received duration, use fixed one instead
				super.startScroll(startX, startY, dx, dy, mDuration);
			}

			@Override
			public void startScroll(int startX, int startY, int dx, int dy) {
				// Ignore received duration, use fixed one instead
				super.startScroll(startX, startY, dx, dy, mDuration);
			}

			public void setmDuration(int time) {
				mDuration = time;
			}

			public int getmDuration() {
				return mDuration;
			}

		};
		
		/**
		 * 页卡切换监听
		 */
		public class MyOnPageChangeListener implements OnPageChangeListener {

			@Override
			public void onPageScrollStateChanged(int arg0) {
				Log.e(TAG, "onPageScrollStateChanged  " + arg0 );
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				Log.e(TAG, "onPageScrolled  " + arg0 + "   "  + arg1 +"   " + arg2);
			}

			@Override
			public void onPageSelected(int arg0) {
				setCurpager(arg0);
				mSelectedPageIndex = arg0;
				Log.e(TAG, "onPageSelected  " + arg0  );
			}
			
		}

		public void setCurpager(int arg0) {
			for (int i = 0,  size = arrayTitle.size() ; i < size; i++) {
				if(i == arg0){
					arrTitle.get(i).setTextSize(35);
				}else {
					arrTitle.get(i).setTextSize(25);
				}
			}
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			if (KeyEvent.ACTION_DOWN == event.getAction()) {
				mLastKeyCode = event.getKeyCode();
//				if (mViewPageScrollState != ViewPager.SCROLL_STATE_IDLE) return true;
				View view = this.getCurrentFocus();
				ITabPage selectedPage = (ITabPage) mViewCache.get(mSelectedPageIndex);
				if (null != view) {
					if(mKeepMenuState ){//如果在tab menu而且  向up
						switch (mLastKeyCode) {
						case KeyEvent.KEYCODE_DPAD_UP :// UP
							
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN :// Down
							if (!selectedPage.canGoDown()) {// tab不能向下
								arrTitle.get(mSelectedPageIndex).requestFocus();
								return true;
							}
							
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT :// LEFT
							
							break;
						case KeyEvent.KEYCODE_DPAD_RIGHT :// RIGHT
							
							break;
						default:
							break;
						}
					}else { //----------------------- 非菜单下
						switch (mLastKeyCode) {
						case KeyEvent.KEYCODE_DPAD_UP :// UP
							
							break;
						case KeyEvent.KEYCODE_DPAD_DOWN :// Down
							if (!selectedPage.canGoDown()) {// tab不能向下
								arrTitle.get(mSelectedPageIndex).requestFocus();
								return true;
							}
							break;
						case KeyEvent.KEYCODE_DPAD_LEFT :// LEFT
							
							break;

						case KeyEvent.KEYCODE_DPAD_RIGHT :// RIGHT
							
							break;
						default:
							break;
						}
					}
				}
				
			}
			return super.dispatchKeyEvent(event);
		}

}
