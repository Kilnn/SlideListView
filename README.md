# SlideListView
Use for Android ListView slide delete etc.

![](https://github.com/LonelyRoamer/SlideListView/blob/master/20150106071617515.gif)  

## How To Use

### 1、add in you layout.xml
    <com.roamer.slidelistview.SlideListView  
        xmlns:slide="http://schemas.android.com/apk/res-auto"  
        android:id="@+id/list_view"  
        android:layout_width="match_parent"  
        android:layout_height="match_parent"  
        slide:slideAnimationTime="200"  
        slide:slideLeftAction="scroll"  
        slide:slideMode="both"  
        slide:slideRightAction="scroll" />  

### 2、implement SlideBaseAdapter,overriad getFrontViewId()，getLeftBackViewId()，getRightBackViewId() method
    @Override  
    public int getFrontViewId(int position) {  
        return R.layout.row_front_view;  
    }  
      
    @Override  
    public int getLeftBackViewId(int position) {  
        return R.layout.row_left_back_view;  
    }  
      
    @Override  
    public int getRightBackViewId(int position) {  
        return R.layout.row_right_back_view;  
    }  

[See also](http://blog.csdn.net/lonelyroamer/article/details/42439875)
