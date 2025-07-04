package com.octopus.edu.core.network.utils

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class NetworkResultCall<T : Any>(
    private val call: Call<T>,
) : Call<NetworkResponse<T>> {
    override fun execute(): Response<NetworkResponse<T>?> =
        throw UnsupportedOperationException("NetworkResponseCall doesn't support execute")

    override fun enqueue(callback: Callback<NetworkResponse<T>>) {
        call.enqueue(
            object : Callback<T> {
                override fun onResponse(
                    call: Call<T>,
                    response: Response<T>,
                ) {
                    with(this@NetworkResultCall) {
                        when {
                            response.isSuccessful -> {
                                val body = response.body()

                                if (body == null) {
                                    callback.onResponse(
                                        this,
                                        Response.success(
                                            NetworkResponse.Error(
                                                HttpException(response),
                                            ),
                                        ),
                                    )

                                    return@with
                                }

                                callback.onResponse(
                                    this,
                                    Response.success(NetworkResponse.Success(body)),
                                )
                            }

                            else -> {
                                callback.onResponse(
                                    this,
                                    Response.success(
                                        NetworkResponse.Error(
                                            HttpException(response),
                                        ),
                                    ),
                                )
                            }
                        }
                    }
                }

                override fun onFailure(
                    call: Call<T?>,
                    throwable: Throwable,
                ) {
                    callback.onResponse(
                        this@NetworkResultCall,
                        Response.success(NetworkResponse.Error(throwable)),
                    )
                }
            },
        )
    }

    override fun isExecuted(): Boolean = call.isExecuted

    override fun cancel() = call.cancel()

    override fun isCanceled(): Boolean = call.isCanceled

    override fun clone() = NetworkResultCall(call.clone())

    override fun request(): Request = call.request()

    override fun timeout(): Timeout = call.timeout()
}
