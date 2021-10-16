package uk.co.avsoftware.trading.client.bybit.model

data class BybitError(
    var ret_code: Int = 0,
    var ret_msg: String = "",
    var ext_code: String? = null,
    var ext_info: String? = null
) : Throwable()