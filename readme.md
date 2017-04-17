![效果图](capture.gif)


3d卡片效果
=======

- 测试版本4.1.2，4.2.2，4.4.2可用

## 2014-9-11修改

- 修复设置高度`wrap_content`时显示不全的bug
- 修复点击后，翻转过程中向下滑动出现的bug

>注意：如果设置高度为`wrap_content`，为了保证动画视觉上的完整性，需要设置自身及所有关联窗口的`android:clipChildren`属性为false(代码中setClipChildren(false))。

## 使用


	CardView cardView = (CardView) findViewById(R.id.cardView1);
	//设置间距
	cardView.setItemSpace(Utils.convertDpToPixelInt(this, 20));
	//设置可见数量,默认4个
	cardView.setMaxVisibleCount(4);
	//点击顶部card监听
	cardView.setOnCardClickListener(new OnCardClickListener(){
		public void onCardClick(View view, int position) {
		}
	});


## 问题

- 测试2.3.2有严重bug（估计4.0版本之前都有此bug），不可用。

