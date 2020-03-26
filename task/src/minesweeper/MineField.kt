package minesweeper

import minesweeper.data.Cell
import minesweeper.data.Mine
import java.util.*
import kotlin.random.Random

/**
 * Created by Prathmesh Swaroop on 23-Mar-2020
 * MineField Class containing the complete logic for mine sweeper game.
 */
class MineField(private val gridSize: Int, private var numberOfMines: Int) {
    companion object {
        const val PADDING = 3
        const val HORIZONTAL_PIPE = "—"
        const val VERTICAL_PIPE = "│"
        const val ASCII_CONST = 48
    }

    private val mines: LinkedList<Mine> = LinkedList()

    private val mineField: Array<Array<Cell>> = Array(gridSize) { i ->
        Array(gridSize) { j ->
            Cell(i, j)
        }
    }

    private val markedMineCount: Int
        get() {
            var count = 0
            for (row in mineField) {
                for (cell in row) {
                    if (cell.cellState == Cell.State.MINE_MARKER) {
                        count++
                    }
                }
            }
            return count
        }

    /**
     * Method to display the state of the mine field
     */
    fun showField() {
        for (i in 0..(mineField.lastIndex + PADDING)) {
            if (i == 0) {
                print(" $VERTICAL_PIPE")
                for (x in 0..mineField.lastIndex) {
                    print("${x + 1}")
                }
                print("$VERTICAL_PIPE\n")
            } else if (i == 1 || i - PADDING + 1 > mineField.lastIndex) {
                print("$HORIZONTAL_PIPE$VERTICAL_PIPE")
                for (x in 0..mineField.lastIndex) {
                    print(HORIZONTAL_PIPE)
                }
                print("$VERTICAL_PIPE\n")
            } else {
                print("${i - PADDING + 2}$VERTICAL_PIPE")
                for (j in 0..mineField.lastIndex) {
                    val cell = mineField[i - PADDING + 1][j]
                    print(cell.displayValue)
                }
                print("$VERTICAL_PIPE\n")
            }
        }
    }

    /**
     * Method to configure the new Mine Field
     * @param manualOverride is an optional parameter for testing purposes
     * @param mines is an optional parameter containing list of mines if manual override is enabled
     */
    fun configure(manualOverride: Boolean = false, mines: LinkedList<Mine> = LinkedList<Mine>()): MineField {
        if (manualOverride) {
            this.mines.clear()
            for (mine in mines) {
                mineField[mine.i][mine.j].cellState = Cell.State.MINE
                this.mines.add(mine)
            }
            this.numberOfMines = mines.size
        } else {
            createMines()
        }
        validateVicinityMineCount()
        return this
    }


    /**
     * Method to create a random mines add them in mine field
     */
    private fun createMines() {
        var index = 0
        if (numberOfMines > 60) {
            outer@ for (row in mineField) {
                for (cell in row) {
                    if (index == numberOfMines) {
                        break@outer
                    }
                    cell.cellState = Cell.State.MINE
                    mines.add(Mine(cell.i, cell.j, false))
                    //print("(${cell.i} , ${cell.j})-$index-")
                    index++
                }
            }
        } else {
            while (index < numberOfMines) {
                val i = Random.nextInt(0, mineField.lastIndex)
                val j = Random.nextInt(0, mineField.lastIndex)
                if (mineField[i][j].cellState == Cell.State.DEFAULT) {
                    mineField[i][j].cellState = Cell.State.MINE
                    mines.add(Mine(i, j, false))
                    //print("($i , $j)-$index-")
                    index++
                }
            }
        }

        println()
    }

    /**
     * Method to validate all cells and update their vicinity mine state
     */
    private fun resetVicinityMineCount() {
        for (i in mineField.indices) {
            for (j in mineField[i].indices) {
                mineField[i][j].vicinityMineCount = 0
            }
        }
    }

    /**
     * Method to validate all cells and update their vicinity mine state
     */
    private fun validateVicinityMineCount() {
        for (i in mineField.indices) {
            for (j in mineField[i].indices) {
                mineField[i][j].vicinityMineCount = getSurroundingMineCount(i, j)
            }
        }
    }

    /**
     * Method to calculate cell's vicinity mine count
     * @param i as x coordinate of field
     * @param j as y coordinate of field
     */
    private fun getSurroundingMineCount(i: Int, j: Int): Int {
        var mineCount = 0
        val lastIndex = mineField.lastIndex
        val pseudoSI = i - 1
        val pseudoSJ = j - 1
        val pseudoEI = i + 1
        val pseudoEJ = j + 1
        for (pseudoIndexI in pseudoSI..pseudoEI) {
            for (pseudoIndexJ in pseudoSJ..pseudoEJ) {
                if ((pseudoIndexI in 0..lastIndex)
                        && (pseudoIndexJ in 0..lastIndex)) {
                    if (mineField[pseudoIndexI][pseudoIndexJ].cellState == Cell.State.MINE || isMineCell(pseudoIndexI, pseudoIndexJ)) {
                        mineCount += 1
                    }
                }
            }
        }
        return mineCount
    }

    /**
     * Method to check if cell at passed coordinate is a mine cell or not
     * @param i as x coordinate of cell
     * @param j as y coordinate of cell
     * @return Boolean
     */
    fun isMineCell(i: Int, j: Int): Boolean {
        for (mine in mines) {
            if (mine.i == i && mine.j == j) {
                return true
            }
        }
        return false
    }

    /**
     * Custom Exception
     */
    class MineFieldException : Exception() {
        override val message: String?
            get() = "Invalid x y coordinate passed"
    }

    /**
     * Method to validate if all mines are marked by player
     * @return Boolean
     */
    fun isAllValidMinesMarked(): Boolean {
        for (mine in mines) {
            if (!mine.marked) {
                return false
            }
        }
        return (markedMineCount == mines.count())
    }


    /**
     * Method to validate if all cells are explored or not
     * @return Boolean
     */
    fun isAllCellsExplored(): Boolean {
        var unExploredCellCount = 0
        for (row in mineField) {
            for (cell in row) {
                if (cell.cellState != Cell.State.MINE && !cell.explored) {
                    unExploredCellCount++
                }
            }
        }
        return unExploredCellCount == 0
    }

    /**
     * Method to update/mark/un-mark mine flag for a valid mine
     * @param i as x coordinate of cell
     * @param j as y coordinate of cell
     * @param flagToSet as Boolean flag which needs to be updated
     * @return Boolean if mine cell is found and marked
     */
    private fun updateMineFlag(i: Int, j: Int, flagToSet: Boolean): Boolean {
        for (mine in mines) {
            if (mine.i == i && mine.j == j) {
                mine.marked = flagToSet
                return true
            }
        }
        return false
    }

    /**
     * PLAYER ACTION 1
     * Method is used which user performs an action to mark a mine
     * @param i as x coordinate of cell
     * @param j as y coordinate of cell
     */
    fun markMine(i: Int, j: Int) {
        if (i > mineField.lastIndex || j > mineField.lastIndex) {
            throw MineFieldException()
        }
        if (!mineField[i][j].explored) {
            mineField[i][j].cellState = if (mineField[i][j].cellState == Cell.State.MINE_MARKER) {
                updateMineFlag(i, j, false)
                Cell.State.DEFAULT
            } else {
                updateMineFlag(i, j, true)
                Cell.State.MINE_MARKER
            }
            mineField[i][j].displayValue = mineField[i][j].cellState.marker
        }
    }

    /**
     * PLAYER ACTION 2
     * Method is used which user performs an action to free or open a cell in mine field
     * @param i as x coordinate of cell
     * @param j as y coordinate of cell
     * @param userInitiated as flag to denote if action is initiated by user
     */
    fun freeCell(i: Int, j: Int, userInitiated: Boolean): Boolean {
        if (i > mineField.lastIndex || j > mineField.lastIndex) {
            throw MineFieldException()
        }
        if (userInitiated && (mineField[i][j].cellState == Cell.State.MINE || this@MineField.isMineCell(i, j))) {
            //GAME OVER SITUATION
            revealAllMines()
            return false
        } else if (!mineField[i][j].explored && mineField[i][j].cellState != Cell.State.MINE && !this@MineField.isMineCell(i, j)) {
            if (mineField[i][j].cellState == Cell.State.NUMBER || mineField[i][j].vicinityMineCount > 0) {
                mineField[i][j].explored = true
                mineField[i][j].cellState = Cell.State.NUMBER
                mineField[i][j].displayValue = (mineField[i][j].vicinityMineCount + ASCII_CONST).toChar()
                performSelfFreeOperation(i, j)
            } else if (!mineInCellVicinity(i, j)) {
                mineField[i][j].explored = true
                mineField[i][j].cellState = Cell.State.SAFE
                mineField[i][j].displayValue = mineField[i][j].cellState.marker
                performSelfFreeOperation(i, j)
            }
        }
        return true
    }

    /**
     * Method to reveal all mines with original mine marker in Game end situation
     */
    private fun revealAllMines() {
        for (mine in mines) {
            val cell = mineField[mine.i][mine.j]
            cell.cellState = Cell.State.MINE
            cell.displayValue = cell.cellState.marker
        }
    }

    /**
     * Method to open all free cells if there is no mine in near by vicinity
     * @param i as x coordinate of cell
     * @param j as y coordinate of cell
     */
    private fun performSelfFreeOperation(i: Int, j: Int) {
        val lastIndex = mineField.lastIndex
        val pseudoSI = i - 1
        val pseudoSJ = j - 1
        val pseudoEI = i + 1
        val pseudoEJ = j + 1
        for (pseudoIndexI in pseudoSI..pseudoEI) {
            for (pseudoIndexJ in pseudoSJ..pseudoEJ) {
                if ((pseudoIndexI in 0..lastIndex)
                        && (pseudoIndexJ in 0..lastIndex)) {
                    freeCell(pseudoIndexI, pseudoIndexJ, false)
                }
            }
        }
    }

    /**
     * Method to check if there is mine cell present in cell vicinity
     * @param i as x coordinate of cell
     * @param j as y coordinate of cell
     */
    private fun mineInCellVicinity(i: Int, j: Int): Boolean {
        val lastIndex = mineField.lastIndex
        val pseudoSI = i - 1
        val pseudoSJ = j - 1
        val pseudoEI = i + 1
        val pseudoEJ = j + 1
        for (pseudoIndexI in pseudoSI..pseudoEI) {
            for (pseudoIndexJ in pseudoSJ..pseudoEJ) {
                if ((pseudoIndexI in 0..lastIndex)
                        && (pseudoIndexJ in 0..lastIndex)) {
                    if (mineField[pseudoIndexI][pseudoIndexJ].cellState == Cell.State.MINE || isMineCell(pseudoIndexI, pseudoIndexJ)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Method to reconfigure the mine field as the user stepped on a mine very first time while freeing the cell
     * @param xCoordinate as x coordinate of existing mine
     * @param yCoordinate as y coordinate of existing mine
     */
    fun reconfigureMineOn(xCoordinate: Int, yCoordinate: Int, manualOverride: Boolean = false, manualX: Int = 0, manualY: Int = 0) {
        //1. Remove existing mine
        removeMine(xCoordinate, yCoordinate)

        //2. Generate new random coordinate for a new mine
        val newMineCoordinate = if (manualOverride) {
            Pair(manualX, manualY)
        } else {
            getNewMineCoordinate(xCoordinate, yCoordinate)
        }

        //3. Add a new mine
        addNewMine(newMineCoordinate.first, newMineCoordinate.second)

        //4. Reset all cell's vicinity mine count to 0
        resetVicinityMineCount()

        //5. Recalculate all cell's vicinity mine count
        validateVicinityMineCount()
    }

    /**
     * Method to add a new mine on field
     * @param xCoordinate as x coordinate of new mine
     * @param yCoordinate as y coordinate of new mine
     */
    private fun addNewMine(i: Int, j: Int) {
        mines.add(Mine(i, j, false))
        mineField[i][j].cellState = Cell.State.MINE
    }

    /**
     * Method to generate a new mine coordinate randomly
     * @param xCoordinate as x coordinate of existing mine
     * @param yCoordinate as y coordinate of existing mine
     */
    private fun getNewMineCoordinate(xCoordinate: Int, yCoordinate: Int): Pair<Int, Int> {
        var i = Random.Default.nextInt(0, mineField.lastIndex)
        var j = Random.Default.nextInt(0, mineField.lastIndex)
        while ((i == xCoordinate && j == yCoordinate)
                || isMineCell(i, j)) {
            i = Random.Default.nextInt(0, mineField.lastIndex)
            j = Random.Default.nextInt(0, mineField.lastIndex)
        }
        return Pair(i, j)
    }

    /**
     * Method to remove a mine from field kept at provided coordinate
     * @param xCoordinate as x coordinate of mine
     * @param yCoordinate as y coordinate of mine
     */
    private fun removeMine(xCoordinate: Int, yCoordinate: Int) {
        mines.remove(Mine(xCoordinate, yCoordinate, false))
        mineField[xCoordinate][yCoordinate].cellState = Cell.State.DEFAULT
    }
}