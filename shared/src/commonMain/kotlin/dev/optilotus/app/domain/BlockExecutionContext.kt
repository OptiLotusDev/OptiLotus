package dev.optilotus.app.domain

class BlockExecutionContext {
    private val _output = mutableListOf<String>()
    val output: List<String> get() = _output.toList()

    private val _errors = mutableListOf<String>()
    val errors: List<String> get() = _errors.toList()

    fun writeOutput(text: String) {
        _output.add(text)
    }

    fun reportError(message: String) {
        _errors.add(message)
    }
}
