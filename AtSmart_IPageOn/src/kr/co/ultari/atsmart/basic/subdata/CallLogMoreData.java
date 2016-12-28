package kr.co.ultari.atsmart.basic.subdata;

public class CallLogMoreData {
        private String callDate;
        private String callDuration;
        private String callNumber;
        private String callStatus;

        public CallLogMoreData( String date, String duration, String number, String status )
        {
                this.callDate = date;
                this.callDuration = duration;
                this.callNumber = number;
                this.callStatus = status;
        }

        public String getDate()
        {
                return callDate;
        }

        public String getDuration()
        {
                return callDuration;
        }

        public String getNumber()
        {
                return callNumber;
        }

        public String getStatus()
        {
                return callStatus;
        }
}
