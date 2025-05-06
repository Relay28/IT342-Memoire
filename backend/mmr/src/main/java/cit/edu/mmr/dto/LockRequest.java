package cit.edu.mmr.dto;

import com.google.firebase.database.annotations.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

import java.util.Date;

public class LockRequest {

        @NotNull
        @FutureOrPresent
        private Date openDate;

        // getters and setters
        public Date getOpenDate() { return openDate; }
        public void setOpenDate(Date openDate) { this.openDate = openDate; }
}