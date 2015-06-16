package pl.ekhart.whack_a_mole;

import android.graphics.Bitmap;

/**
 * Created by Ekh on 2015-06-14.
 */
public class WidthHeigth<T> {
    public T width;
    public T height;

    public WidthHeigth(T width, T height) {
        this.width = width;
        this.height = height;
    }
}

class WidthHeigthInt extends WidthHeigth<Integer> {
    public WidthHeigthInt(Bitmap background) {
        super(background.getWidth(), background.getHeight());
    }
}
