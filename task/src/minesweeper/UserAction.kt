package minesweeper

enum class UserAction(val actionValue: String) {
    MINE("mine"),
    FREE("free"),
    NULL("");

    companion object {
        fun getActionByValue(actionValue: String): UserAction {
            for (action in values()) {
                if (action.actionValue == actionValue) {
                    return action
                }
            }
            return NULL
        }
    }
}