package rit.csh.drink.model

import androidx.lifecycle.MutableLiveData

enum class Event {
    REFRESH_END,
    DROP_DRINK_END
}

class EventAlert {
    val event = MutableLiveData<Event>(null)

    fun complete(){
        event.value = null
    }

    fun setEvent(event: Event) {
        this.event.value = event
    }
}