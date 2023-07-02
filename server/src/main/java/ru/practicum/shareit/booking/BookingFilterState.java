package ru.practicum.shareit.booking;

public enum BookingFilterState {
    ALL("ALL"),
    CURRENT("CURRENT"),
    PAST("PAST"),
    FUTURE("FUTURE"),
    WAITING("WAITING"),
    REJECTED("REJECTED");

    private final String value;

    BookingFilterState(String value) {
        this.value = value;
    }

    public static BookingFilterState findByValue(String value) {
        BookingFilterState result = null;
        for (BookingFilterState state : values()) {
            if (state.value.equalsIgnoreCase(value)) {
                result = state;
                break;
            }
        }
        return result;
    }
}

