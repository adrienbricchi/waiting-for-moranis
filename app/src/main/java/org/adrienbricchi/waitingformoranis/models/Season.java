package org.adrienbricchi.waitingformoranis.models;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@Keep
@Entity
@NoArgsConstructor
public class Season {

    public static final String FIELD_SHOW_ID = "show_id";


    @PrimaryKey
    @ColumnInfo(name = FIELD_SHOW_ID)
    protected @NonNull String showId;

    protected String id;

    protected int number;
    protected String title;

    protected String imageUrl;
    protected Long calendarEventId;

    protected String status;
    protected @NonNull List<Release> airDates = new ArrayList<>();

    protected Long nextAirDate;
    protected boolean isUpdateNeededInCalendar = false;

}
