package kaufland.com.swipelibrary

/**
 * Created by sbra0902 on 17.03.17.
 */

class SwipeResult {

    var settleX: Int = 0
        private set

    var notifyListener: Runnable?

    constructor(settleX: Int, notifyListener: Runnable) {
        this.settleX = settleX
        this.notifyListener = notifyListener
    }

    constructor(settleX: Int) {
        this.settleX = settleX
        this.notifyListener = null
    }
}
