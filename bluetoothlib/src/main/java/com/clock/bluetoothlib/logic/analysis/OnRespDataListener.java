/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.analysis;

/**
 * 数据发送后，回应的数据
 * @param <T>
 */
public interface OnRespDataListener<T> {
    void onResponse(T resultSrc);
}
