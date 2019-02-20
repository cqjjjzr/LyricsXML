package com.github.charlie.lxml

class XMLValidatingFailedException: Exception {
    constructor(): super()
    constructor(msg: String): super(msg)
}

class IllegalLXMLException: Exception {
    constructor(): super()
    constructor(msg: String): super(msg)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    )
}