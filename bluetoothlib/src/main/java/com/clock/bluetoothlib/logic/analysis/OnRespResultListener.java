/*
 * Copyright (C), 2019. by [clock - Individual developer] ,All rights reserved.
 * The article is original, writing is also the strength to live, reference please indicate the source and author.
 */

package com.clock.bluetoothlib.logic.analysis;

/**
 * 数据请求后，回应的最终结果
 */
public interface OnRespResultListener {
	void onSuccess(int code);
	void onFailure(int code);
}
