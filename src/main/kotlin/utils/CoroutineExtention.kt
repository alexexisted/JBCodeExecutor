package utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


/**
 * @param executionLimit - Лимит ожидания результата, по истечении происходит вызов onError()
 * @param source - Источник данных
 * @param onComplete - Общий резльтат выполнения (при onError() / onSuccess())
 * @param onLoading - Действие во время выполнения
 * @param onError - Действие при ошибке
 * @param onSuccess - Результат выполнения
 * Обработчик ошибок по умолчанию доступен исключительно из вызова не в suspend блоке.
 * В случае вызова в suspend блоке (example: launch) - обработка ошибок делегируется родительской корутине.
 */
inline fun <reified T> CoroutineScope.execute(
    executionLimit: Long? = null,
    crossinline source: suspend () -> T,
    crossinline onLoading: (isLoading: Boolean) -> Unit = {},
    crossinline onComplete: (result: Result<T>) -> Unit = {},
    crossinline onError: (throwable: Throwable) -> Unit = {},
    crossinline onSuccess: (response: T) -> Unit = {},
) {
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        launch(Dispatchers.Main) {
            onLoading(false)
            onComplete(Result.failure(throwable))
            onError(throwable)
        }
    }

    // SupervisorJob гарантирует безопасность выполнения родительской корутины
    val execution = launch(exceptionHandler + SupervisorJob()) {
        onLoading(true)
        source().let { response ->
            onLoading(false)
            onSuccess(response)
            onComplete(Result.success(response))
        }
    }

    if (executionLimit != null) {
        launchWithDelay(delay = executionLimit) {
            if (!execution.isCompleted) {
                execution.cancel()
                exceptionHandler.handleException(coroutineContext, CancellationException())
            }
        }
    }
}

fun CoroutineScope.launchWithDelay(
    delay: Long = 500L,
    contextAction: CoroutineContext = Dispatchers.Main,
    onAction: CoroutineScope.() -> Unit
) =
    launch(Dispatchers.IO) {
        delay(delay)
        withContext(context = contextAction, block = onAction)
    }
