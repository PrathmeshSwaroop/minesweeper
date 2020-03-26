package minesweeper

import java.util.*

const val GRID_SIZE = 9

fun main() {
    val scanner = Scanner(System.`in`)
    print("How many mines do you want on the field? ")
    val input = scanner.nextLine()
    val mineField = MineField(GRID_SIZE, input.toInt())
    mineField.configure().showField()
    var freeCountFlag = 0
    gameLoop@ while (true) {
        print("Set/unset mines marks or claim a cell as free: ")
        val coordinate = scanner.nextLine().split(" ")
        val action = UserAction.getActionByValue(coordinate[2])
        val xCoordinate = coordinate[1].toInt() - 1
        val yCoordinate = coordinate[0].toInt() - 1
        if (action == UserAction.MINE) {
            mineField.markMine(xCoordinate, yCoordinate)
            mineField.showField()
            if (mineField.isAllCellsExplored() || mineField.isAllValidMinesMarked()) {
                println("Congratulations! You found all mines!")
                break@gameLoop
            }
        } else {
            if (freeCountFlag == 0 && mineField.isMineCell(xCoordinate, yCoordinate)) {
                mineField.reconfigureMineOn(xCoordinate, yCoordinate)
            }
            freeCountFlag = 1
            val successful = mineField.freeCell(xCoordinate, yCoordinate, true)
            mineField.showField()
            if (!successful) {
                println("You stepped on a mine and failed!")
                break@gameLoop
            }
        }
    }
}