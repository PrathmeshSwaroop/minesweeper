package minesweeper.data

data class Cell(val i: Int, val j: Int, var cellState: State = State.DEFAULT, var explored: Boolean = false) {

    var vicinityMineCount: Int = 0

    var displayValue = State.DEFAULT.marker

    enum class State(val marker: Char) {
        DEFAULT('.'),
        MINE_MARKER('*'),
        MINE('X'),
        SAFE('/'),
        NUMBER('0')
    }
}