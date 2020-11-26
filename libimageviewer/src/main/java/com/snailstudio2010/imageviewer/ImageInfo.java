package com.snailstudio2010.imageviewer;

import java.util.List;

public class ImageInfo {

    private int index;
    private List<Info> list;
    private boolean showIndex;
    private boolean showDown;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<Info> getList() {
        return list;
    }

    public void setList(List<Info> list) {
        this.list = list;
    }

    public boolean isShowDown() {
        return showDown;
    }

    public void setShowDown(boolean showDown) {
        this.showDown = showDown;
    }

    public boolean isShowIndex() {
        return showIndex;
    }

    public void setShowIndex(boolean showIndex) {
        this.showIndex = showIndex;
    }

    public static class Info {
        private float x;
        private float y;
        private float width;
        private float height;
        private Object src;

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public Object getSrc() {
            return src;
        }

        public void setSrc(Object src) {
            this.src = src;
        }

//        public ObservableOnSubscribe<Bitmap> getBitmap(final Activity activity) {
//            return new ObservableOnSubscribe<Bitmap>() {
//                @Override
//                public void subscribe(final ObservableEmitter<Bitmap> emitter) {
//                    if (src instanceof Bitmap) {
//                        emitter.onNext((Bitmap) src);
//                        emitter.onComplete();
//                    } else if (src instanceof String) {
//                        activity.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                RxImageUtils.loadBitmap(activity,
//                                        (String) src,
//                                        new RxImageUtils.LoadBitmapListener() {
//                                            @Override
//                                            public void onLoad(Bitmap bitmap) {
//                                                if (bitmap != null) {
//                                                    emitter.onNext(bitmap);
//                                                    emitter.onComplete();
//                                                } else {
//                                                    emitter.onError(new RuntimeException("null"));
//                                                }
//                                            }
//                                        });
//                            }
//                        });
//                    } else {
//                        emitter.onError(new RuntimeException("null"));
//                    }
//                }
//            };
//        }
    }
}
